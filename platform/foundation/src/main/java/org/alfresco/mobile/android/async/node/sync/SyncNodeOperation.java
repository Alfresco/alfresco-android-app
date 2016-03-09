/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.async.node.sync;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class SyncNodeOperation extends NodeOperation<Boolean>
{
    private static final String TAG = SyncNodeOperation.class.getName();

    private Boolean markSync;

    private Boolean isSynced = Boolean.FALSE;

    private boolean hasSyncParent;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SyncNodeRequest)
        {
            nodeIdentifier = ((SyncNodeRequest) request).getNodeIdentifier();
            this.markSync = ((SyncNodeRequest) request).markSync;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Boolean> doInBackground()
    {
        LoaderResult<Boolean> result = new LoaderResult<Boolean>();
        Cursor cursorId = null;
        try
        {
            result = super.doInBackground();

            if (node == null)
            {
                // Special error happen when the node identifier is wrong
                cursorId = SyncContentManager.getCursorForId(context, acc, nodeIdentifier);
                manageReferentialByRemoving(cursorId);
            }
            else
            {
                nodeIdentifier = node.getIdentifier();
                if (node.isFolder())
                {
                    isSynced = SyncContentManager.getInstance(context).isRootSynced(acc, nodeIdentifier);
                }
                else
                {
                    isSynced = SyncContentManager.getInstance(context).isSynced(acc, nodeIdentifier);
                }
                hasSyncParent = false;

                // Retrieve local sync info.
                cursorId = SyncContentManager.getCursorForId(context, acc, nodeIdentifier);

                // Check if parent is in sync or not
                Cursor parentCursorId = null;
                try
                {
                    if (parentFolder == null)
                    {
                        parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
                    }
                    parentCursorId = SyncContentManager.getCursorForId(context, acc, parentFolder.getIdentifier());
                    if (parentCursorId.getCount() == 1 && parentCursorId.moveToFirst())
                    {
                        hasSyncParent = true;
                    }
                }
                catch (Exception e)
                {
                    hasSyncParent = true;
                }
                finally
                {
                    CursorUtils.closeCursor(parentCursorId);
                }

                // CASE : UNFAVORITE
                if ((markSync == null && isSynced) || (markSync != null && !markSync && isSynced))
                {
                    isSynced = false;
                    manageReferentialByRemoving(cursorId);
                }
                // CASE : FAVORITE
                else if ((markSync == null && !isSynced) || (markSync != null && markSync && !isSynced))
                {
                    isSynced = true;
                    manageReferentialByAdding(cursorId);
                }
                // CASE : UNFAVORITE ALREADY UNFAVORITED
                else if (markSync != null && !markSync && !isSynced)
                {
                    manageReferentialByRemoving(cursorId);
                }
                else if (markSync != null && markSync && isSynced)
                {
                    manageReferentialByAdding(cursorId);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        finally
        {
            if (cursorId != null)
            {
                cursorId.close();
            }
        }

        result.setData(isSynced);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void manageReferentialByRemoving(Cursor cursorId)
    {
        // Special case where an error occured during sync
        // We remove all duplicate except the first one.
        if (cursorId.getCount() > 1)
        {
            cursorId.moveToNext();
            for (int i = 1; i < cursorId.getCount(); i++)
            {
                context.getContentResolver().delete(
                        SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)), null, null);
                cursorId.moveToNext();
            }
            cursorId = SyncContentManager.getCursorForId(context, acc, nodeIdentifier);
        }

        if (cursorId.getCount() == 1 && cursorId.moveToFirst())
        {
            if (SyncContentManager.getInstance(context).hasActivateSync(acc))
            {
                ContentValues cValues = new ContentValues();
                if (parentFolder != null)
                {
                    cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
                }
                if (cursorId.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID) > 0)
                {
                    // Unfavorite
                    cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, 0);
                }

                if (!hasSyncParent)
                {
                    cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_HIDDEN);
                }
                context.getContentResolver().update(
                        SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues, null,
                        null);

                if (node != null && node.isFolder())
                {
                    prepareChildrenFolderDelete(node.getIdentifier());
                }
            }
            else
            {
                if (node != null && node.isFolder())
                {
                    prepareChildrenFolderDelete((Folder) node);
                }
                context.getContentResolver().delete(
                        SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)), null, null);
            }
        }
    }

    private void manageReferentialByAdding(Cursor cursorId)
    {
        // Add to favorite
        if (cursorId.getCount() == 0)
        {
            ContentValues addValues = (hasSyncParent)
                    ? SyncContentManager.createContentValues(context,
                            AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId), 456,
                            parentFolderIdentifier, node, new GregorianCalendar().getTimeInMillis(), -1)
                    : SyncContentManager.createSyncRootContentValues(context,
                            AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId), 456,
                            parentFolderIdentifier, node, new GregorianCalendar().getTimeInMillis(), -1);

            // First time creation
            // Update local sync referential.
            context.getContentResolver().insert(SyncContentProvider.CONTENT_URI, addValues);
        }
        else if (cursorId.getCount() == 1 && cursorId.moveToFirst())
        {
            ContentValues cValues = new ContentValues();

            // Already present : Is it Hidden ?
            if (cursorId.getInt(SyncContentSchema.COLUMN_STATUS_ID) == SyncContentStatus.STATUS_HIDDEN)
            {
                cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_SUCCESSFUL);
            }

            // Already present in sync which means it's inside a
            // synced folder
            // We simply update the favorite
            cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, SyncContentProvider.FLAG_SYNC_SET);
            context.getContentResolver().update(
                    SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues, null, null);
        }
    }

    private void prepareChildrenFolderDelete(Folder folder)
    {
        Cursor childrenCursor = null;
        try
        {
            childrenCursor = context.getContentResolver()
                    .query(SyncContentProvider.CONTENT_URI,
                            SyncContentSchema.COLUMN_ALL, SyncContentProvider.getAccountFilter(acc) + " AND "
                                    + SyncContentSchema.COLUMN_PARENT_ID + " == '" + folder.getIdentifier() + "'",
                            null, null);

            while (childrenCursor.moveToNext())
            {
                context.getContentResolver().delete(
                        SyncContentManager.getUri(childrenCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), null, null);
                if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID)))
                {
                    prepareChildrenFolderDelete(folder);
                }
            }

        }
        catch (Exception e)
        {
            // DO Nothing
        }
        finally
        {
            CursorUtils.closeCursor(childrenCursor);
        }
    }

    private void prepareChildrenFolderDelete(String nodeId)
    {
        Cursor childrenCursor = null;
        try
        {
            childrenCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL,
                    SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_PARENT_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(nodeId) + "'",
                    null, null);

            ContentValues cValues = new ContentValues(1);
            while (childrenCursor.moveToNext())
            {
                cValues.clear();
                cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_HIDDEN);
                context.getContentResolver().update(
                        SyncContentManager.getUri(childrenCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues,
                        null, null);
                if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID)))
                {
                    prepareChildrenFolderDelete(childrenCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                }
            }

        }
        catch (Exception e)
        {
            // DO Nothing
        }
        finally
        {
            CursorUtils.closeCursor(childrenCursor);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Boolean> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new SyncNodeEvent(getRequestId(), result, node));

        if (markSync != null)
        {
            // Analytics
            AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    !markSync ? AnalyticsManager.ACTION_SYNC : AnalyticsManager.ACTION_UNSYNC,
                    node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                    result.hasException());
        }
    }
}

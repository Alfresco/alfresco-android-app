/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async.node.favorite;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncProvider;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class FavoriteNodeOperation extends NodeOperation<Boolean>
{
    private static final String TAG = FavoriteNodeOperation.class.getName();

    private Boolean value;

    private Boolean isFavorite = Boolean.FALSE;

    private boolean hasSyncParent;

    private Boolean batch = Boolean.FALSE;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FavoriteNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof FavoriteNodeRequest)
        {
            nodeIdentifier = ((FavoriteNodeRequest) request).getNodeIdentifier();
            this.value = ((FavoriteNodeRequest) request).markFavorite;
            this.batch = ((FavoriteNodeRequest) request).batchFavorite;
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
                cursorId = FavoritesSyncManager.getCursorForId(context, acc, nodeIdentifier);
                manageReferentialByRemoving(cursorId);
            }
            else
            {
                nodeIdentifier = node.getIdentifier();
                isFavorite = session.getServiceRegistry().getDocumentFolderService().isFavorite(node);
                hasSyncParent = false;

                // Retrieve local sync info.
                cursorId = FavoritesSyncManager.getCursorForId(context, acc, nodeIdentifier);

                // Check if parent is in sync or not
                Cursor parentCursorId = null;
                try
                {
                    if (parentFolder == null)
                    {
                        parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
                    }
                    parentCursorId = FavoritesSyncManager.getCursorForId(context, acc, parentFolder.getIdentifier());
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
                if ((value == null && isFavorite) || (value != null && !value && isFavorite))
                {
                    session.getServiceRegistry().getDocumentFolderService().removeFavorite(node);
                    isFavorite = false;
                    manageReferentialByRemoving(cursorId);
                }
                // CASE : FAVORITE
                else if ((value == null && !isFavorite) || (value != null && value && !isFavorite))
                {
                    session.getServiceRegistry().getDocumentFolderService().addFavorite(node);
                    isFavorite = true;
                    manageReferentialByAdding(cursorId);
                }
                // CASE : UNFAVORITE ALREADY UNFAVORITED
                else if (value != null && !value && !isFavorite)
                {
                    manageReferentialByRemoving(cursorId);
                }
                else if (value != null && value && isFavorite)
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

        result.setData(isFavorite);

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
                        FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), null, null);
                cursorId.moveToNext();
            }
            cursorId = FavoritesSyncManager.getCursorForId(context, acc, nodeIdentifier);
        }

        if (cursorId.getCount() == 1 && cursorId.moveToFirst())
        {
            if (FavoritesSyncManager.getInstance(context).hasActivateSync(acc))
            {
                ContentValues cValues = new ContentValues();
                cValues.put(FavoritesSyncSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
                if (cursorId.getInt(FavoritesSyncSchema.COLUMN_IS_FAVORITE_ID) > 0)
                {
                    // Unfavorite
                    cValues.put(FavoritesSyncSchema.COLUMN_IS_FAVORITE, 0);
                }

                if (!hasSyncParent)
                {
                    cValues.put(FavoritesSyncSchema.COLUMN_STATUS, FavoriteSyncStatus.STATUS_HIDDEN);
                }
                context.getContentResolver().update(
                        FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), cValues, null,
                        null);
            }
            else
            {
                if (node.isFolder())
                {
                    prepareChildrenFolderDelete((Folder) node);
                }
                context.getContentResolver().delete(
                        FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), null, null);
            }
        }
    }

    private void manageReferentialByAdding(Cursor cursorId)
    {
        // Add to favorite
        if (cursorId.getCount() == 0)
        {
            // First time creation
            // Update local sync referential.
            context.getContentResolver().insert(
                    FavoritesSyncProvider.CONTENT_URI,
                    FavoritesSyncManager.createFavoriteContentValues(context,
                            AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId), 456,
                            parentFolderIdentifier, node, new GregorianCalendar().getTimeInMillis(), -1));
        }
        else if (cursorId.getCount() == 1 && cursorId.moveToFirst())
        {
            // Already present in sync which means it's inside a
            // synced folder
            // We simply update the favorite
            ContentValues cValues = new ContentValues();
            cValues.put(FavoritesSyncSchema.COLUMN_IS_FAVORITE, FavoritesSyncProvider.FLAG_FAVORITE);
            context.getContentResolver().update(
                    FavoritesSyncManager.getUri(cursorId.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), cValues, null,
                    null);
        }
    }

    private void prepareChildrenFolderDelete(Folder folder)
    {
        Cursor childrenCursor = null;
        try
        {
            childrenCursor = context.getContentResolver().query(
                    FavoritesSyncProvider.CONTENT_URI,
                    FavoritesSyncSchema.COLUMN_ALL,
                    FavoritesSyncProvider.getAccountFilter(acc) + " AND " + FavoritesSyncSchema.COLUMN_PARENT_ID
                            + " == '" + NodeRefUtils.getCleanIdentifier(folder.getIdentifier()) + "'", null, null);

            while (childrenCursor.moveToNext())
            {
                context.getContentResolver().delete(
                        FavoritesSyncManager.getUri(childrenCursor.getLong(FavoritesSyncSchema.COLUMN_ID_ID)), null,
                        null);
                if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(FavoritesSyncSchema.COLUMN_MIMETYPE_ID)))
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

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Boolean> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new FavoriteNodeEvent(getRequestId(), result, node));
    }
}

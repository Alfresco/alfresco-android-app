/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.favorite;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.node.NodeOperationThread;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.utils.CursorUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class FavoriteNodeThread extends NodeOperationThread<Boolean>
{
    private static final String TAG = FavoriteNodeThread.class.getName();

    private Boolean value;

    private Boolean isFavorite = Boolean.FALSE;

    private boolean hasSyncParent;

    private Boolean batch = Boolean.FALSE;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FavoriteNodeThread(Context ctx, OperationRequest request)
    {
        super(ctx, request);
        if (request instanceof FavoriteNodeRequest)
        {
            this.value = ((FavoriteNodeRequest) request).markAsFavorite();
            this.batch = ((FavoriteNodeRequest) request).isBatch();
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

            isFavorite = session.getServiceRegistry().getDocumentFolderService().isFavorite(node);
            hasSyncParent = false;

            // Retrieve local sync info.
            cursorId = SynchroManager.getCursorForId(context, acc, node.getIdentifier());

            // Check if parent is in sync or not
            Cursor parentCursorId = null;
            try
            {
                if (parentFolder == null)
                {
                    parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
                }
                parentCursorId = SynchroManager.getCursorForId(context, acc, parentFolder.getIdentifier());
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

                // Update Sync Info
                if (cursorId.getCount() == 1 && cursorId.moveToFirst())
                {
                    if (SynchroManager.getInstance(context).hasActivateSync(acc))
                    {
                        ContentValues cValues = new ContentValues();
                        cValues.put(SynchroSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
                        if (cursorId.getInt(SynchroSchema.COLUMN_IS_FAVORITE_ID) > 0)
                        {
                            // Unfavorite
                            cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, 0);
                        }

                        if (!hasSyncParent)
                        {
                            cValues.put(SynchroSchema.COLUMN_STATUS, SyncOperation.STATUS_HIDDEN);
                        }
                        context.getContentResolver().update(
                                SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)), cValues, null,
                                null);
                    }
                    else
                    {
                        if (node.isFolder())
                        {
                            prepareChildrenFolderDelete((Folder) node);
                        }
                        context.getContentResolver().delete(
                                SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)), null, null);
                    }
                }
            }
            // CASE : FAVORITE
            else if ((value == null && !isFavorite) || (value != null && value && !isFavorite))
            {
                session.getServiceRegistry().getDocumentFolderService().addFavorite(node);
                isFavorite = true;

                // Add to favorite
                if (cursorId.getCount() == 0)
                {
                    // First time creation
                    // Update local sync referential.
                    context.getContentResolver().insert(
                            SynchroProvider.CONTENT_URI,
                            SynchroManager.createFavoriteContentValues(context,
                                    AccountManager.retrieveAccount(context, accountId), SyncDownloadRequest.TYPE_ID,
                                    parentFolderIdentifier, node, new GregorianCalendar().getTimeInMillis(), -1));
                }
                else if (cursorId.getCount() == 1 && cursorId.moveToFirst())
                {
                    // Already present in sync which means it's inside a
                    // synced folder
                    // We simply update the favorite
                    ContentValues cValues = new ContentValues();
                    cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, SynchroProvider.FLAG_FAVORITE);
                    context.getContentResolver().update(
                            SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)), cValues, null, null);
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
    private void prepareChildrenFolderDelete(Folder folder)
    {
        Cursor childrenCursor = null;
        try
        {
            childrenCursor = context.getContentResolver().query(
                    SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL,
                    SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_PARENT_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(folder.getIdentifier()) + "'", null, null);

            while (childrenCursor.moveToNext())
            {
                context.getContentResolver().delete(
                        SynchroManager.getUri(childrenCursor.getLong(SynchroSchema.COLUMN_ID_ID)), null, null);
                if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(SynchroSchema.COLUMN_MIMETYPE_ID)))
                {
                    prepareChildrenFolderDelete(folder);
                }
            }

        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        finally
        {
            CursorUtils.closeCursor(childrenCursor);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_FAVORITE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_NODE, node);
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putString(IntentIntegrator.EXTRA_FAVORITE, isFavorite.toString());
        b.putBoolean(IntentIntegrator.EXTRA_BATCH_FAVORITE, batch);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}

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
package org.alfresco.mobile.android.async.node.favorite;

import java.util.GregorianCalendar;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationStatus;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesProvider;
import org.alfresco.mobile.android.platform.favorite.FavoritesSchema;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class FavoriteNodeOperation extends NodeOperation<Boolean>
{
    private static final String TAG = FavoriteNodeOperation.class.getName();

    private Boolean value;

    private Boolean isFavorite = Boolean.FALSE;

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
                cursorId = FavoritesManager.getCursorForId(context, acc, nodeIdentifier);
                manageReferentialByRemoving(cursorId);
            }
            else
            {
                nodeIdentifier = node.getIdentifier();
                isFavorite = session.getServiceRegistry().getDocumentFolderService().isFavorite(node);

                // Retrieve local sync info.
                cursorId = FavoritesManager.getCursorForId(context, acc, nodeIdentifier);

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
        if (cursorId.getCount() >= 1)
        {
            cursorId.moveToNext();
            for (int i = 0; i < cursorId.getCount(); i++)
            {
                context.getContentResolver()
                        .delete(FavoritesManager.getUri(cursorId.getLong(FavoritesSchema.COLUMN_ID_ID)), null, null);
                cursorId.moveToNext();
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
            context.getContentResolver().insert(FavoritesProvider.CONTENT_URI,
                    FavoritesManager.createFavoriteContentValues(context,
                            AlfrescoAccountManager.getInstance(context).retrieveAccount(accountId),
                            parentFolderIdentifier, node, new GregorianCalendar().getTimeInMillis()));
        }
        else if (cursorId.getCount() == 1 && cursorId.moveToFirst())
        {
            ContentValues cValues = new ContentValues();
            cValues.put(FavoritesSchema.COLUMN_STATUS, OperationStatus.STATUS_SUCCESSFUL);
            cValues.put(FavoritesSchema.COLUMN_IS_FAVORITE, FavoritesProvider.FLAG_FAVORITE);
            context.getContentResolver().update(FavoritesManager.getUri(cursorId.getLong(FavoritesSchema.COLUMN_ID_ID)),
                    cValues, null, null);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Boolean> result)
    {
        super.onPostExecute(result);

        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                isFavorite ? AnalyticsManager.ACTION_FAVORITE : AnalyticsManager.ACTION_UNFAVORITE,
                node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                result.hasException());

        EventBusManager.getInstance().post(new FavoriteNodeEvent(getRequestId(), result, node));
    }
}

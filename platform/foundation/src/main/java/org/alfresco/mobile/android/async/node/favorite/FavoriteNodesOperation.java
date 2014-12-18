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

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.ui.node.favorite.FavoritesNodeTemplate;

import android.util.Log;

public class FavoriteNodesOperation extends ListingOperation<PagingResult>
{
    private static final String TAG = FavoriteNodesOperation.class.getName();

    protected int mode;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FavoriteNodesOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("rawtypes")
    protected LoaderResult<PagingResult> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult> result = new LoaderResult<PagingResult>();
            PagingResult pagingResult = null;

            try
            {
                if (listingContext == null || listingContext.getFilter() == null
                        || listingContext.getFilter().getFilterValue(FavoritesNodeTemplate.FILTER_KEY_MODE) == null)
                {
                    mode = FavoriteNodesRequest.MODE_BOTH;
                }
                else
                {
                    mode = (Integer) listingContext.getFilter().getFilterValue(FavoritesNodeTemplate.FILTER_KEY_MODE);
                }

                switch (mode)
                {
                    case FavoriteNodesRequest.MODE_DOCUMENTS:
                        pagingResult = session.getServiceRegistry().getDocumentFolderService()
                                .getFavoriteDocuments(listingContext);
                        break;
                    case FavoriteNodesRequest.MODE_FOLDERS:
                        pagingResult = session.getServiceRegistry().getDocumentFolderService()
                                .getFavoriteFolders(listingContext);
                        break;
                    case FavoriteNodesRequest.MODE_BOTH:
                        pagingResult = session.getServiceRegistry().getDocumentFolderService()
                                .getFavoriteNodes(listingContext);
                        break;

                    default:
                        break;
                }
            }
            catch (AlfrescoServiceException e)
            {
                // Do Nothing
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult>();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<PagingResult> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new FavoriteNodesEvent(getRequestId(), result, mode));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
}

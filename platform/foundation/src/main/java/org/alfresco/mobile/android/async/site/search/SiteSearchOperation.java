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
package org.alfresco.mobile.android.async.site.search;

import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class SiteSearchOperation extends ListingOperation<PagingResult<Site>>
{
    private static final String TAG = SiteSearchOperation.class.getName();

    protected String keyword;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SiteSearchOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SiteSearchRequest)
        {
            this.keyword = ((SiteSearchRequest) request).keyword;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Site>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Site>> result = new LoaderResult<PagingResult<Site>>();
            PagingResult<Site> pagingResult = null;

            try
            {
                pagingResult = session.getServiceRegistry().getSiteService().search(keyword, listingContext);
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult<Site>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Site>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new SiteSearchEvent(getRequestId(), keyword, result));
    }
}

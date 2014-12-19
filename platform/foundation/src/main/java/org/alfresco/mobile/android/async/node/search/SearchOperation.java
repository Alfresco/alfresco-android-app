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
package org.alfresco.mobile.android.async.node.search;

import org.alfresco.mobile.android.api.model.KeywordSearchOptions;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class SearchOperation extends ListingOperation<PagingResult<Node>>
{
    private static final String TAG = SearchOperation.class.getName();

    private String keywords;

    private KeywordSearchOptions sp;

    private String statement;

    private SearchLanguage language;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SearchOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SearchRequest)
        {
            this.keywords = ((SearchRequest) request).keywords;
            this.sp = ((SearchRequest) request).sp;
            this.statement = ((SearchRequest) request).statement;
            this.language = ((SearchRequest) request).language;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Node>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Node>> result = new LoaderResult<PagingResult<Node>>();
            PagingResult<Node> pagingResult = null;

            try
            {
                if (keywords != null)
                {
                    pagingResult = session.getServiceRegistry().getSearchService()
                            .keywordSearch(keywords, sp, listingContext);
                }
                else if (statement != null)
                {
                    pagingResult = session.getServiceRegistry().getSearchService()
                            .search(statement, language, listingContext);
                }
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
        return new LoaderResult<PagingResult<Node>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Node>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new SearchEvent(getRequestId(), result));
    }
}

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
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.ListingOperationRequest;

import android.content.Context;

public class SearchRequest extends ListingOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_SEARCH_LIST;

    final String keywords;

    final KeywordSearchOptions sp;

    final String statement;

    final SearchLanguage language;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected SearchRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext, String keywords,
            KeywordSearchOptions options, String statement, SearchLanguage language)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, listingContext);
        this.keywords = keywords;
        this.sp = options;
        this.statement = statement;
        this.language = language;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getCacheKey()
    {
        return "SearchOperationRequest";
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends ListingOperationRequest.Builder
    {
        protected String keywords;

        protected KeywordSearchOptions sp;

        protected String statement;

        protected SearchLanguage language;

        protected Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(String keywords, KeywordSearchOptions sp)
        {
            this();
            this.keywords = keywords;
            this.sp = sp;
        }

        public Builder(String statement, SearchLanguage language)
        {
            this();
            this.statement = statement;
            this.language = language;
        }

        public SearchRequest build(Context context)
        {
            return new SearchRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, listingContext, keywords, sp, statement, language);
        }
    }

}

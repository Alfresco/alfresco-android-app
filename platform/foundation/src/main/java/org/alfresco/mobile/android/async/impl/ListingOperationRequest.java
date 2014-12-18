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
package org.alfresco.mobile.android.async.impl;

import java.io.Serializable;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.async.OperationRequest;

import android.content.Context;
import android.database.Cursor;

public abstract class ListingOperationRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    protected static final String PROP_LISTINGCONTEXT = "listingContext";

    protected static final String PROP_LISTINGCONTEXT_MAXITEMS = "listingContext_MaxItems";

    protected static final String PROP_LISTINGCONTEXT_SKIPCOUNT = "listingContext_SkipCount";

    protected static final String PROP_LISTINGCONTEXT_SORTING = "listingContext_Sorting";

    protected static final String PROP_LISTINGCONTEXT_SORTASCENDING = "listingContext_SortAscending";

    protected ListingContext listingContext;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ListingOperationRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, ListingContext listingContext)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.listingContext = listingContext;
        persistListingContext();
    }

    public ListingOperationRequest(Cursor cursor)
    {
        super(cursor);
        this.listingContext = retrieveListingContext();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public void persistListingContext()
    {
        if (listingContext != null)
        {
            persistentProperties.put(PROP_LISTINGCONTEXT, Boolean.TRUE.toString());
            persistentProperties.put(PROP_LISTINGCONTEXT_MAXITEMS, Integer.toString(listingContext.getMaxItems()));
            persistentProperties.put(PROP_LISTINGCONTEXT_SKIPCOUNT, Integer.toString(listingContext.getSkipCount()));
            persistentProperties.put(PROP_LISTINGCONTEXT_SORTING, listingContext.getSortProperty());
            persistentProperties.put(PROP_LISTINGCONTEXT_SORTASCENDING,
                    Boolean.toString(listingContext.isSortAscending()));
            if (listingContext.getFilter() != null)
            {
                for (Entry<String, Serializable> item : listingContext.getFilter().getFilters().entrySet())
                {
                    // TODO Implement filter parsing ==> Use MapUtils
                }
            }
        }
    }

    public ListingContext retrieveListingContext()
    {
        if (persistentProperties == null) { return null; }
        if (persistentProperties.containsKey(PROP_LISTINGCONTEXT))
        {
            int maxItems = Integer.parseInt((String) persistentProperties.remove(PROP_LISTINGCONTEXT_MAXITEMS));
            int skipCount = Integer.parseInt((String) persistentProperties.remove(PROP_LISTINGCONTEXT_SKIPCOUNT));
            String sorting = (String) persistentProperties.remove(PROP_LISTINGCONTEXT_SORTING);
            boolean sortingModifier = Boolean.parseBoolean((String) persistentProperties
                    .remove(PROP_LISTINGCONTEXT_SORTASCENDING));
            return new ListingContext(sorting, maxItems, skipCount, sortingModifier);
        }
        else
        {
            return null;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static abstract class Builder extends OperationRequest.OperationBuilder
    {
        protected ListingContext listingContext;

        protected Builder()
        {

        }

        public OperationBuilder setListingContext(ListingContext listingContext)
        {
            this.listingContext = listingContext;
            return this;
        }
    }

}

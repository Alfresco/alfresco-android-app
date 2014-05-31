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
package org.alfresco.mobile.android.ui.node.favorite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;
import android.util.Log;

import com.squareup.otto.Subscribe;

/**
 * Displays a fragment list of document and folders.
 * 
 * @author Jean Marie Pascal
 */
public class FavoritesNodeFragment extends BaseGridFragment implements FavoritesNodeTemplate
{
    private static final String TAG = FavoritesNodeFragment.class.getName();

    protected List<Node> selectedItems = new ArrayList<Node>(1);

    private Boolean activateThumbnail;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public FavoritesNodeFragment()
    {
        emptyListMessageId = R.string.empty_child;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    protected void onRetrieveParameters(Bundle bundle)
    {
        if (bundle.containsKey(ARGUMENT_BASED_ON_TEMPLATE))
        {
            // Configuration is ON
            // From template we have to retrieve values from the bundle.
            // Values are already decoded from Template key.
            ListingFilter lf = createFilterFromBundle(bundle);
            if (originListing == null && lf != null)
            {
                originListing = new ListingContext();
            }
            if (lf != null)
            {
                originListing.setFilter(lf);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        if (listingContext == null)
        {
            ListingFilter lf = new ListingFilter();
            lf.addFilter(FILTER_KEY_MODE, FavoriteNodesRequest.MODE_BOTH);
            listingContext = new ListingContext();
            listingContext.setFilter(lf);
        }
        return new FavoriteNodesRequest.Builder(listingContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(FavoriteNodesEvent event)
    {
        displayData(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Boolean hasActivateThumbnail()
    {
        return activateThumbnail;
    }

    public void setActivateThumbnail(Boolean activateThumbnail)
    {
        this.activateThumbnail = activateThumbnail;
    }

    // //////////////////////////////////////////////////////////////////////
    // LISTING FILTER
    // //////////////////////////////////////////////////////////////////////
    public static ListingFilter createFilterFromBundle(Bundle b)
    {
        ListingFilter f = new ListingFilter();
        boolean hasValue = false;
        for (String key : FILTERS_KEYS)
        {
            if (b.containsKey(key))
            {
                f.addFilter(key, b.getInt(key));
                hasValue = true;
            }
        }
        return (hasValue) ? f : null;
    }

    @SuppressWarnings("unchecked")
    public static void addFilter(Map<String, Object> json, Bundle b)
    {
        if (json.containsKey("filters"))
        {
            Map<String, Object> filter = (Map<String, Object>) json.get("filters");
            b.putAll(createFilterBundle(filter));
        }
    }

    public static Bundle createFilterBundle(Map<String, Object> properties)
    {
        Bundle b = new Bundle();
        Boolean hasFilter = false;
        for (Entry<String, Object> item : properties.entrySet())
        {
            if (FILTERS_KEY_REGISTRY.containsKey(item.getKey()) && FILTERS_VALUE_REGISTRY.containsKey(item.getValue()))
            {
                hasFilter = true;
                b.putInt(FILTERS_KEY_REGISTRY.get(item.getKey()), FILTERS_VALUE_REGISTRY.get(item.getValue()));
            }
            else
            {
                Log.w(TAG, "Error during parsing filter info : " + item.getKey() + " " + item.getValue());
            }
        }

        if (hasFilter)
        {
            b.putBoolean(ARGUMENT_HAS_FILTER, true);
        }
        return b;
    }

    @SuppressWarnings("serial")
    private static final Map<String, Integer> FILTERS_VALUE_REGISTRY = new HashMap<String, Integer>()
    {
        {
            put(FavoritesNodeTemplate.FILTER_MODE_ALL, FavoriteNodesRequest.MODE_BOTH);
            put(FavoritesNodeTemplate.FILTER_MODE_DOCUMENTS, FavoriteNodesRequest.MODE_DOCUMENTS);
            put(FavoritesNodeTemplate.FILTER_MODE_FOLDERS, FavoriteNodesRequest.MODE_FOLDERS);
        }
    };

    @SuppressWarnings("serial")
    private static final Map<String, String> FILTERS_KEY_REGISTRY = new HashMap<String, String>()
    {
        {
            put(FavoritesNodeTemplate.FILTER_KEY_MODE, FavoritesNodeTemplate.FILTER_KEY_MODE);
        }
    };

    private static final String[] FILTERS_KEYS = new String[] {

    };

}

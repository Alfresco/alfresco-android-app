/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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
/*******************************************************************************
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.text.Html;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@TargetApi(11)
public abstract class BaseGridFragment extends CommonGridFragment
{
    public static final String TAG = BaseGridFragment.class.getName();

    protected String requestId;

    @SuppressWarnings("rawtypes")
    protected List<Object> selectedItem = new ArrayList<Object>(1);

    // /////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ////////////////////////////////////////////////////////////
    @Override
    public void onStop()
    {
        Operator.with(getActivity()).cancel(requestId);
        super.onStop();
    }

    // /////////////////////////////////////////////////////////////
    // LOAD MORE
    // ////////////////////////////////////////////////////////////
    @SuppressWarnings("rawtypes")
    protected void displayData(OperationEvent<?> event)
    {
        // Responsible to check if the event received is the one created by the
        // fragment.
        if (requestId == null || !requestId.equals(event.requestId)) { return; }

        if (displayAsList)
        {
            gv.setColumnWidth(getDPI(getResources().getDisplayMetrics(), 2000));
        }

        if (adapter == null)
        {
            adapter = onAdapterCreation();
            ((BaseListAdapter) adapter).setFragmentSettings(getArguments());
        }
        if (event.hasException)
        {
            onResultError(event.exception);
        }
        else
        {
            displayPagingData((PagingResult<?>) event.data);
            setListShown(true);
        }
        AccessibilityUtils.sendAccessibilityEvent(getActivity());
        if (refreshHelper != null)
        {
            refreshHelper.setRefreshComplete();
        }
    }

    @SuppressWarnings("unchecked")
    protected void displayPagingData(PagingResult<?> data)
    {
        if (!isFullLoad)
        {
            if ((data == null || data.getTotalItems() == 0 || data.getList().isEmpty()) && !hasmore)
            {
                gv.setEmptyView(ev);
                isFullLoad = Boolean.TRUE;
                if (adapter != null)
                {
                    gv.invalidateViews();
                    gv.setAdapter(null);
                }
                prepareEmptyView(ev, (ImageView) ev.findViewById(R.id.empty_picture),
                        (TextView) ev.findViewById(R.id.empty_text),
                        (TextView) ev.findViewById(R.id.empty_text_description));
            }
            else
            {
                if (!isDataPresent(data))
                {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
                    {
                        ((ArrayAdapter<Object>) adapter).addAll(data.getList());
                    }
                    else
                    {
                        for (Object item : data.getList())
                        {
                            ((ArrayAdapter<Object>) adapter).add(item);
                        }
                    }
                    hasmore = data.hasMoreItems();
                    if (doesLoadMore())
                    {
                        loadMore();
                    }
                    gv.invalidateViews();
                    gv.setAdapter(adapter);
                }
            }
            setListShown(true);
        }
        if (selectedPosition != 0)
        {
            gv.setSelection(selectedPosition);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isDataPresent(PagingResult<?> data)
    {
        ArrayAdapter<Object> arrayAdapter = ((ArrayAdapter<Object>) adapter);
        return !arrayAdapter.isEmpty()
                && !(data.getList() != null && !data.getList().contains(
                        arrayAdapter.getItem(arrayAdapter.getCount() - 1)));
    }

    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        if (bundle.containsKey(ARGUMENT_LABEL))
        {
            title = bundle.getString(ARGUMENT_LABEL);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // REQUEST
    // //////////////////////////////////////////////////////////////////////
    protected void performRequest(ListingContext listingContext)
    {
        requestId = Operator.with(getActivity(), getAccount()).load(onCreateOperationRequest(listingContext));
    }

    /**
     * Responsible to create the the Main operationRequest to populate the
     * gridview
     */
    protected abstract OperationBuilder onCreateOperationRequest(ListingContext listingContext);

    /**
     * Use this method to launch a request with different parameter than the
     * default one provided by fragment arguments
     * 
     * @param builder
     */
    protected void performRequest(OperationBuilder builder)
    {
        // Reset all indicators
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;

        // Prepare and execute the request
        requestId = Operator.with(getActivity(), getAccount()).load(builder);
    }

    // //////////////////////////////////////////////////////////////////////
    // REFRESH
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        if (!ConnectivityUtils.hasNetwork(getActivity()))
        {
            if (refreshHelper != null)
            {
                refreshHelper.setRefreshComplete();
            }
            Crouton.cancelAllCroutons();
            Crouton.showText(getActivity(), Html.fromHtml(getString(R.string.error_session_nodata)), Style.INFO,
                    (ViewGroup) (getRootView().getParent()));
            return;
        }

        reload();
    }

    private void reload()
    {
        // Reset all indicators
        isFullLoad = Boolean.FALSE;
        hasmore = Boolean.FALSE;
        skipCount = 0;
        adapter = null;

        currentListing = copyListing(originListing);

        // Event refresh
        onPrepareRefresh();

        // Execute the request
        performRequest(currentListing);
    }
}

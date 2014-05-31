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
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;

import android.annotation.TargetApi;
import android.widget.ArrayAdapter;

@TargetApi(11)
public abstract class BaseGridFragment extends CommonGridFragment
{
    public static final String TAG = BaseGridFragment.class.getName();

    protected static final String ARGUMENT_BASED_ON_TEMPLATE = "basedOnTemplate";

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
            onDataDisplayed();
        }
        AccessibilityUtils.sendAccessibilityEvent(getActivity());
        refreshHelper.setRefreshComplete();
    }

    /** Event after data has been displayed. */
    protected void onDataDisplayed()
    {
        // Can be used by deriaved classes.
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
        if (arrayAdapter.isEmpty())
        {
            return false;
        }
        else
        {
            return !(data.getList() != null && !data.getList().contains(
                    arrayAdapter.getItem(arrayAdapter.getCount() - 1)));
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

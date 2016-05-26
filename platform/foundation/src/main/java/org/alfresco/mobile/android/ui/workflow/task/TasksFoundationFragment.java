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
package org.alfresco.mobile.android.ui.workflow.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.model.config.ConfigConstants;
import org.alfresco.mobile.android.api.services.WorkflowService;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.workflow.task.TasksEvent;
import org.alfresco.mobile.android.async.workflow.task.TasksRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import com.squareup.otto.Subscribe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Displays a fragment list of document and folders.
 * 
 * @author Jean Marie Pascal
 */
public class TasksFoundationFragment extends BaseGridFragment implements TasksTemplate
{
    private static final String TAG = TasksFoundationFragment.class.getName();

    protected List<Task> selectedItems = new ArrayList<>(1);

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public TasksFoundationFragment()
    {
        emptyListMessageId = R.string.empty_tasks;
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
    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        if (listingContext == null)
        {
            ListingFilter lf = new ListingFilter();
            lf.addFilter(WorkflowService.FILTER_KEY_STATUS, WorkflowService.FILTER_STATUS_ACTIVE);
            listingContext = new ListingContext();
            listingContext.setFilter(lf);
        }
        return new TasksRequest.Builder(listingContext);
    }

    @Subscribe
    public void onResult(TasksEvent event)
    {
        displayData(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static ListingFilter createFilter(Map<String, Object> properties)
    {
        ListingFilter f = new ListingFilter();
        for (Entry<String, Object> item : properties.entrySet())
        {
            if (FILTERS_KEY_REGISTRY.containsKey(item.getKey()) && FILTERS_VALUE_REGISTRY.containsKey(item.getValue()))
            {
                f.addFilter(FILTERS_KEY_REGISTRY.get(item.getKey()), FILTERS_VALUE_REGISTRY.get(item.getValue()));
            }
            else
            {
                Log.w(TAG, "Error during parsing filter info : " + item.getKey() + " " + item.getValue());
            }
        }
        return f;
    }

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
        if (json.containsKey(ConfigConstants.FILTERS_VALUE))
        {
            Map<String, Object> filter = (Map<String, Object>) json.get(ConfigConstants.FILTERS_VALUE);
            b.putAll(createFilterBundle(filter));
        }
    }

    public static void addFilter(Intent intent, Bundle b)
    {
        Map<String, Object> filter = new HashMap<>(intent.getData().getQueryParameterNames().size());
        for (String key : intent.getData().getQueryParameterNames())
        {
            filter.put(key, intent.getData().getQueryParameter(key));
        }
        b.putAll(createFilterBundle(filter));
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

    private static final Map<String, Integer> FILTERS_VALUE_REGISTRY = new HashMap<String, Integer>()
    {
        {
            put(TasksTemplate.FILTER_STATUS_ANY, WorkflowService.FILTER_STATUS_ANY);
            put(TasksTemplate.FILTER_STATUS_ACTIVE, WorkflowService.FILTER_STATUS_ACTIVE);
            put(TasksTemplate.FILTER_STATUS_COMPLETE, WorkflowService.FILTER_STATUS_COMPLETE);

            put(TasksTemplate.FILTER_DUE_TODAY, WorkflowService.FILTER_DUE_TODAY);
            put(TasksTemplate.FILTER_DUE_TOMORROW, WorkflowService.FILTER_DUE_TOMORROW);
            put(TasksTemplate.FILTER_DUE_7DAYS, WorkflowService.FILTER_DUE_7DAYS);
            put(TasksTemplate.FILTER_DUE_OVERDUE, WorkflowService.FILTER_DUE_OVERDUE);
            put(TasksTemplate.FILTER_DUE_NODATE, WorkflowService.FILTER_DUE_NODATE);

            put(TasksTemplate.FILTER_PRIORITY_LOW, WorkflowService.FILTER_PRIORITY_LOW);
            put(TasksTemplate.FILTER_PRIORITY_MEDIUM, WorkflowService.FILTER_PRIORITY_MEDIUM);
            put(TasksTemplate.FILTER_PRIORITY_HIGH, WorkflowService.FILTER_PRIORITY_HIGH);

            put(TasksTemplate.FILTER_ASSIGNEE_ME, WorkflowService.FILTER_ASSIGNEE_ME);
            put(TasksTemplate.FILTER_ASSIGNEE_UNASSIGNED, WorkflowService.FILTER_ASSIGNEE_UNASSIGNED);
            put(TasksTemplate.FILTER_ASSIGNEE_ALL, WorkflowService.FILTER_ASSIGNEE_ALL);
            put(TasksTemplate.FILTER_NO_ASSIGNEE, WorkflowService.FILTER_NO_ASSIGNEE);

            put(TasksTemplate.FILTER_INITIATOR_ME, WorkflowService.FILTER_INITIATOR_ME);
            put(TasksTemplate.FILTER_INITIATOR_ANY, WorkflowService.FILTER_INITIATOR_ANY);
        }
    };

    private static final String[] FILTERS_KEYS = new String[] { WorkflowService.FILTER_KEY_ASSIGNEE,
            WorkflowService.FILTER_KEY_DUE, WorkflowService.FILTER_KEY_INITIATOR, WorkflowService.FILTER_KEY_PRIORITY,
            WorkflowService.FILTER_KEY_STATUS

    };

    private static final Map<String, String> FILTERS_KEY_REGISTRY = new HashMap<String, String>()
    {
        {
            put(TasksTemplate.FILTER_KEY_ASSIGNEE, WorkflowService.FILTER_KEY_ASSIGNEE);
            put(TasksTemplate.FILTER_KEY_DUE, WorkflowService.FILTER_KEY_DUE);
            put(TasksTemplate.FILTER_KEY_INITIATOR, WorkflowService.FILTER_KEY_INITIATOR);
            put(TasksTemplate.FILTER_KEY_PRIORITY, WorkflowService.FILTER_KEY_PRIORITY);
            put(TasksTemplate.FILTER_KEY_STATUS, WorkflowService.FILTER_KEY_STATUS);

        }
    };
}

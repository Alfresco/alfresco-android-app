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
package org.alfresco.mobile.android.application.fragments.builder;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.configuration.manager.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.ListingTemplate;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Goal is to create a Fragment based on configuration provided.
 * 
 * @author jpascal
 */
public abstract class AlfrescoFragmentBuilder
{
    private static final String TAG = AlfrescoFragmentBuilder.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    protected int menuIconId;

    protected int menuTitleId;

    protected OnClickListener onClick;

    protected WeakReference<Activity> activity;

    protected String[] templateArguments = new String[0];

    protected boolean sessionRequired = true;

    protected boolean hasBackStack = true;

    protected Map<String, Object> configuration;

    protected Bundle extraConfiguration;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected AlfrescoFragmentBuilder()
    {

    }

    /**
     * Used by the Factory
     * 
     * @param activity
     */
    public AlfrescoFragmentBuilder(Activity activity)
    {
        this(activity, null, null);
    }

    /**
     * Used by the configurationManager
     * 
     * @param activity
     * @param configuration
     */
    public AlfrescoFragmentBuilder(Activity activity, Map<String, Object> configuration)
    {
        this(activity, configuration, null);
    }

    /**
     * Used by ?
     * 
     * @param activity
     * @param configuration
     * @param b
     */
    public AlfrescoFragmentBuilder(Activity activity, Map<String, Object> configuration, Bundle b)
    {
        this.onClick = onDefaultClick;
        this.activity = new WeakReference<Activity>(activity);
        this.configuration = configuration;
        this.extraConfiguration = b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Activity getActivity()
    {
        return activity.get();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public AlfrescoFragmentBuilder addExtra(Bundle b)
    {
        if (b == null || b.isEmpty()) { return this; }
        if (extraConfiguration == null)
        {
            this.extraConfiguration = b;
        }
        else
        {
            extraConfiguration.putAll(b);
        }
        return this;
    }

    public AlfrescoFragmentBuilder back(boolean hasBackStack)
    {
        this.hasBackStack = hasBackStack;
        return this;
    }

    public boolean hasBackStack()
    {
        return hasBackStack;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DEFAULT CLICK LISTENER
    // Used for creating the fragment associated to the configuration like menu
    // ///////////////////////////////////////////////////////////////////////////
    protected OnClickListener onDefaultClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // Retrieve configuration
            AlfrescoFragmentBuilder builder = (AlfrescoFragmentBuilder) v.getTag();

            // Create the properties bundle
            FragmentDisplayer.load(builder).into(FragmentDisplayer.PANEL_LEFT);

            // Remove
            if (getActivity() instanceof MainActivity)
            {
                ((MainActivity) getActivity()).hideSlideMenu();
            }
        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // MENU CONFIGURATION
    // Responsible to display the fragment after selection
    // ///////////////////////////////////////////////////////////////////////////
    public void createMenuItem(Button v)
    {
        v.setCompoundDrawablesWithIntrinsicBounds(menuIconId, 0, 0, 0);
        v.setText(menuTitleId);
        v.setTag(this);
        v.setOnClickListener(onClick);
    }

    public Bundle createArguments()
    {
        return prepareArguments(configuration, extraConfiguration);
    }

    public Fragment createFragment()
    {
        Fragment frag = createFragment(createArguments());
        if (frag == null) { return null; }
        // Retrieve session if fragment need it.
        if (sessionRequired && frag instanceof AlfrescoFragment)
        {
            ((AlfrescoFragment) frag).setSession(SessionUtils.getSession(activity.get()));
        }
        return frag;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DISPLAY
    // ///////////////////////////////////////////////////////////////////////////
    public void display()
    {
        // Display Fragment
        FragmentDisplayer.load(this).into(FragmentDisplayer.PANEL_LEFT);
    }

    public void display(int viewId)
    {
        // Display Fragment
        FragmentDisplayer.load(this).into(viewId);
    }

    public void displayAsDialog()
    {
        // Display Fragment
        FragmentDisplayer.load(this).into(FragmentDisplayer.PANEL_DIALOG);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT CREATION
    // ///////////////////////////////////////////////////////////////////////////
    protected Fragment createFragment(Bundle b)
    {
        return null;
    }

    protected Bundle prepareArguments(Map<String, Object> properties, Bundle extra)
    {
        // Create the properties bundle
        Bundle b = new Bundle();

        // Configuration from the SERVER
        if (properties != null)
        {
            // Retrieve pagination
            retrievePagination(properties, b);

            // Retrieve onItemSelected
            retrieveOnItemSelected(properties, b);

            // Retrieve Simple list of configuration
            for (String argument : templateArguments)
            {
                retrieveArguments(properties, b, argument);
            }

            // Retrieve custom Arguments
            retrieveCustomArgument(properties, b);
        }

        // Configuration from the CODE
        BundleUtils.addIfNotEmpty(b, extraConfiguration);

        // Add latest Extra parameters if necessary
        BundleUtils.addIfNotEmpty(b, extra);

        return b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PARAMETERS PARSING
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Must be implements in children classes.
     * 
     * @param properties
     * @param b
     */
    protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
    {
        return;
    }

    protected static void retrieveArguments(Map<String, Object> json, Bundle b, String templateKey)
    {
        if (json.containsKey(templateKey))
        {
            b.putString(templateKey, JSONConverter.getString(json, templateKey));
        }
    }

    @SuppressWarnings("unchecked")
    protected static void retrievePagination(Map<String, Object> json, Bundle b)
    {
        if (json.containsKey(ConfigurationConstant.PAGINATION))
        {
            Map<String, Object> pagination = (Map<String, Object>) json.get(ConfigurationConstant.PAGINATION);
            parse(pagination, b);
        }
    }

    private void retrieveOnItemSelected(Map<String, Object> json, Bundle b)
    {
        if (json.containsKey(ConfigurationConstant.ON_ITEM_SELECTED))
        {
            Map<String, Object> onItemSelected = JSONConverter.getMap(json.get(ConfigurationConstant.ON_ITEM_SELECTED));
            if (onItemSelected.containsKey(ConfigurationConstant.VIEW))
            {
                b.putString(ConfigurationConstant.ON_ITEM_SELECTED,
                        JSONConverter.getString(onItemSelected, ConfigurationConstant.VIEW));
            }
        }
    }

    protected static ListingContext parse(Map<String, Object> json, Bundle b)
    {
        ListingContext lc = new ListingContext();
        try
        {
            if (json.containsKey(ListingTemplate.ARGUMENT_MAX_ITEMS))
            {
                lc.setMaxItems(JSONConverter.getInteger(json, ListingTemplate.ARGUMENT_MAX_ITEMS).intValue());
            }
            if (json.containsKey(ListingTemplate.ARGUMENT_ORDER_BY))
            {
                String orderByValue = JSONConverter.getString(json, ListingTemplate.ARGUMENT_ORDER_BY);
                String[] orderBy = orderByValue.split(" ");
                if (!TextUtils.isEmpty(orderBy[0]))
                {
                    lc.setSortProperty(orderBy[0]);
                }
                if (!TextUtils.isEmpty(orderBy[1]))
                {
                    if ("ASC".equals(orderBy[1].toUpperCase()))
                    {
                        lc.setIsSortAscending(true);
                    }
                    else if ("DESC".equals(orderBy[1].toUpperCase()))
                    {
                        lc.setIsSortAscending(false);
                    }
                }
            }
            if (json.containsKey(ListingTemplate.ARGUMENT_SKIP_COUNT))
            {
                lc.setSkipCount(JSONConverter.getInteger(json, ListingTemplate.ARGUMENT_SKIP_COUNT).intValue());
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, "Error during parsing : " + json);
        }
        b.putSerializable(ListingTemplate.ARGUMENT_LISTING, lc);
        return lc;
    }
}

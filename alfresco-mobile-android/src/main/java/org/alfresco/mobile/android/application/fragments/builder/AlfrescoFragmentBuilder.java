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
package org.alfresco.mobile.android.application.fragments.builder;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.config.ViewConfig;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.ConfigModelHelper;
import org.alfresco.mobile.android.application.configuration.model.ViewConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.template.ListingTemplate;
import org.alfresco.mobile.android.ui.template.ViewTemplate;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
    protected ViewConfigModel viewConfigModel;

    protected int menuIconId;

    protected int menuTitleId;

    protected OnClickListener onClick;

    protected WeakReference<FragmentActivity> activity;

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
    public AlfrescoFragmentBuilder(FragmentActivity activity)
    {
        this(activity, null, null);
    }

    /**
     * Used by the configurationManager
     * 
     * @param activity
     * @param configuration
     */
    public AlfrescoFragmentBuilder(FragmentActivity activity, Map<String, Object> configuration)
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
    public AlfrescoFragmentBuilder(FragmentActivity activity, Map<String, Object> configuration, Bundle b)
    {
        this.onClick = onDefaultClick;
        this.activity = new WeakReference<>(activity);
        this.configuration = configuration;
        this.extraConfiguration = b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public FragmentActivity getActivity()
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

            // Generally it's used when creating a menu item.
            if (AnalyticsManager.getInstance(getActivity()) != null)
            {
                // AnalyticsManager.getInstance(getActivity()).reportScreen(builder.viewConfigModel.getSimpleType());
            }

            // Views can requires a session.
            // We check against the activity to display the right info.
            if (builder.sessionRequired && getActivity() instanceof MainActivity)
            {
                if (!((MainActivity) getActivity()).hasSessionAvailable()) { return; }
            }

            int panel = FragmentDisplayer.PANEL_LEFT;
            if (builder instanceof LeafFragmentBuilder)
            {
                builder.back(true);
                if (DisplayUtils.hasLeftPane(getActivity()))
                {
                    panel = FragmentDisplayer.PANEL_CENTRAL;
                }
            }

            // Create the properties bundle
            FragmentDisplayer.load(builder).into(panel);

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
    public void createMenuItem(ViewConfig config, Button v)
    {
        if (viewConfigModel != null)
        {
            menuIconId = viewConfigModel.getIconResId();
            menuTitleId = viewConfigModel.getLabelId();
        }

        if (config != null && !TextUtils.isEmpty(config.getIconIdentifier()))
        {
            menuIconId = ConfigModelHelper.getDarkIconId(config);
        }

        if (menuIconId != -1)
        {
            v.setCompoundDrawablesWithIntrinsicBounds(menuIconId, 0, 0, 0);
        }
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

        // Analytics
        // Report only fragment & report at creation enable (cf. pager case)
        if (AnalyticsManager.getInstance(getActivity()) != null
                && AnalyticsManager.getInstance(getActivity()).isEnable())
        {
            if (frag instanceof AnalyticsManager.FragmentAnalyzed
                    && ((AnalyticsManager.FragmentAnalyzed) frag).reportAtCreationEnable())
            {
                if (viewConfigModel != null)
                {
                    // Track only Main Menu Selection
                    AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.PREFIX_MENU
                            .concat(((AnalyticsManager.FragmentAnalyzed) frag).getScreenName()));
                }
                else
                {
                    // Track all fragment created
                    AnalyticsHelper.reportScreen(getActivity(),
                            ((AnalyticsManager.FragmentAnalyzed) frag).getScreenName());
                }
            }
        }

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
        FragmentDisplayer.load(this).asDialog();
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

        // Retrieve title & description
        retrieveTitle(properties, b);

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

    protected static void retrieveTitle(Map<String, Object> json, Bundle b)
    {
        if (json == null) { return; }

        if (json.containsKey(ViewTemplate.ARGUMENT_LABEL))
        {
            b.putString(ViewTemplate.ARGUMENT_LABEL, JSONConverter.getString(json, ViewTemplate.ARGUMENT_LABEL));
        }

        if (json.containsKey(ViewTemplate.ARGUMENT_DESCRIPTION))
        {
            b.putString(ViewTemplate.ARGUMENT_DESCRIPTION,
                    JSONConverter.getString(json, ViewTemplate.ARGUMENT_DESCRIPTION));
        }
    }

    private void retrieveOnItemSelected(Map<String, Object> json, Bundle b)
    {
        if (json == null) { return; }

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

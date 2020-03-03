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
package org.alfresco.mobile.android.application.fragments.user;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurationConstant;
import org.alfresco.mobile.android.application.configuration.model.view.PeopleConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.async.person.PersonsEvent;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.person.PeopleFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3.0
 * @author jpascal
 */
public class UsersFragment extends PeopleFragment
{
    public static final String TAG = UsersFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public UsersFragment()
    {
        emptyListMessageId = R.string.person_not_found;
        reportAtCreation = false;
    }

    public static UsersFragment newInstanceByTemplate(Bundle b)
    {
        UsersFragment cbf = new UsersFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (keywords != null)
        {
            screenName = AnalyticsManager.SCREEN_SEARCH_RESULT_USERS;
        }
        else if (siteShortName != null)
        {
            screenName = AnalyticsManager.SCREEN_SITES_MEMBERS;
        }
        AnalyticsHelper.reportScreen(getActivity(), screenName);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume()
    {
        if (keywords != null)
        {
            title = String.format(getString(R.string.search_title), keywords);
        }
        else if (siteShortName != null)
        {
            title = getString(R.string.members);
        }
        super.onResume();
    }

    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Person item = (Person) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.containsKey(item.getIdentifier());
            selectedItems.clear();
        }
        l.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.put(item.getIdentifier(), item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            if (getArguments().containsKey(ConfigurationConstant.ON_ITEM_SELECTED))
            {
                ConfigManager configurationManager = ConfigManager.getInstance(getActivity());
                if (configurationManager != null && configurationManager.hasConfig(getAccount().getId()))
                {
                    Bundle b = new Bundle();
                    b.putString("personId", item.getIdentifier());

                    // TODO Configuration ONItemSelected
                    /*
                     * FragmentTemplateConfigurator config = new
                     * FragmentTemplateConfigurator(getActivity(),
                     * configurationManager
                     * .getConfig(SessionUtils.getAccount(getActivity())),
                     * getArguments()
                     * .getString(ConfigurationConstant.ON_ITEM_SELECTED));
                     * config.displayFragment(b);
                     */
                }
            }
            else
            {
                // Show properties
                UserProfileFragment.with(getActivity()).accountId(getAccount().getId()).personId(item.getIdentifier())
                        .display();
            }
        }
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(R.drawable.ic_empty_search_people);
        firstEmptyMessage.setText(R.string.people_search_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.people_search_empty_description);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new UserAdapter(this, R.layout.row_two_lines_caption_divider_circle, new ArrayList<Person>(0),
                selectedItems);
    }

    @Subscribe
    public void onResult(PersonsEvent results)
    {
        super.onResult(results);
        gv.setColumnWidth(DisplayUtils.getDPI(getResources().getDisplayMetrics(), 1000));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new PeopleConfigModel(configuration);
            templateArguments = new String[] { PeopleConfigModel.ARGUMENT_SITE_SHORTNAME,
                    PeopleConfigModel.ARGUMENT_KEYWORDS };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder keywords(String keywords)
        {
            extraConfiguration.putString(PeopleConfigModel.ARGUMENT_KEYWORDS, keywords);
            return this;
        }

        public Builder siteShortName(String siteShortName)
        {
            extraConfiguration.putString(PeopleConfigModel.ARGUMENT_SITE_SHORTNAME, siteShortName);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

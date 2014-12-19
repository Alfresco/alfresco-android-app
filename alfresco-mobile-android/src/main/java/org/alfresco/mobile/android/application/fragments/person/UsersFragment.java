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
package org.alfresco.mobile.android.application.fragments.person;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.config.ConfigManager;
import org.alfresco.mobile.android.application.config.manager.ConfigurationConstant;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.async.person.PersonsEvent;
import org.alfresco.mobile.android.ui.person.PersonAdapter;
import org.alfresco.mobile.android.ui.person.PersonsFragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3.0
 * @author jpascal
 */
public class UsersFragment extends PersonsFragment
{
    public static final String TAG = UsersFragment.class.getName();

    private static final String ARGUMENT_TITLE = "title";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public UsersFragment()
    {
        emptyListMessageId = R.string.person_not_found;
    }

    public static UsersFragment newInstanceByTemplate(Bundle b)
    {
        UsersFragment cbf = new UsersFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        mTitle = (String) bundle.get(ARGUMENT_TITLE);
    }

    @Override
    protected String onCreateTitle(String title)
    {
        if (keywords != null)
        {
            return String.format(getString(R.string.search_title), keywords);
        }
        else
        {
            return super.onCreateTitle(title);
        }
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

                    //TODO Configuration ONItemSelected
                    /*FragmentTemplateConfigurator config = new FragmentTemplateConfigurator(getActivity(),
                            configurationManager.getConfig(SessionUtils.getAccount(getActivity())), getArguments()
                                    .getString(ConfigurationConstant.ON_ITEM_SELECTED));
                    config.displayFragment(b);*/
                }
            }
            else
            {
                // Show properties
                UserProfileFragment.with(getActivity()).personId(item.getIdentifier()).display();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new UserAdapter(this, R.layout.sdk_grid_row, new ArrayList<Person>(0), selectedItems);
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
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            menuIconId = R.drawable.ic_person_light;
            menuTitleId = R.string.user_profile;
            templateArguments = new String[] { ARGUMENT_SITE_SHORTNAME, ARGUMENT_KEYWORDS, ARGUMENT_QUERY };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder keywords(String keywords)
        {
            extraConfiguration.putString(ARGUMENT_KEYWORDS, keywords);
            return this;
        }

        public Builder title(String title)
        {
            extraConfiguration.putString(ARGUMENT_TITLE, title);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}

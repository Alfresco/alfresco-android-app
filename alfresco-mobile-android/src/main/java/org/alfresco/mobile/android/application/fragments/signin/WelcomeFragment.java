/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.signin;

import java.util.Map;

import org.alfresco.mobile.android.application.BuildConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.platform.intent.AlfrescoIntentAPI;
import org.alfresco.mobile.android.platform.mdm.MDMEvent;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public class WelcomeFragment extends AlfrescoFragment
{
    public static final String TAG = WelcomeFragment.class.getName();

    private static final int NUM_PAGES = 3;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public WelcomeFragment()
    {
        super();
        requiredSession = false;
    }

    public static WelcomeFragment newInstanceByTemplate(Bundle b)
    {
        WelcomeFragment cbf = new WelcomeFragment();
        cbf.setArguments(b);
        return cbf;
    }


    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (getRootView() != null) { return getRootView(); }

        setRootView(inflater.inflate(R.layout.fr_welcome, container, false));
        return getRootView();
    }

    protected void displayLogin()
    {
        // Instantiate a ViewPager and a PagerAdapter.
        Bundle extras = getArguments();
        if (extras != null && extras.containsKey(WelcomeActivity.EXTRA_ADD_ACCOUNT))
        {
            hide(R.id.welcome_title);
            hide(R.id.welcome_pager);
            hide(R.id.welcome_pager_indicator);
            LinearLayout layout = (LinearLayout) viewById(R.id.welcome_page_actions_container);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }
        else if (getActivity().findViewById(R.id.double_panel) != null)
        {
            hide(R.id.welcome_title);
            hide(R.id.welcome_pager);
            hide(R.id.welcome_pager_indicator);
            LinearLayout layout = (LinearLayout) viewById(R.id.welcome_page_actions_container);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }
        else
        {
            // TODO Uncomment with marketing material
            /*
             * CircleIndicator defaultIndicator = (CircleIndicator)
             * viewById(R.id.welcome_pager_indicator); mPager = (ViewPager)
             * viewById(R.id.welcome_pager); mPagerAdapter = new
             * ScreenSlidePagerAdapter(getFragmentManager());
             * mPager.setAdapter(mPagerAdapter);
             * defaultIndicator.setViewPager(mPager);
             */
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Request MDM Info
        MDMManager mdmManager = MDMManager.getInstance(getActivity());
        mdmManager.requestConfig(getActivity(), BuildConfig.APPLICATION_ID);

        if (mdmManager.hasConfig())
        {
            // Display progressbar until MDM Event
            show(R.id.homescreen_configuration);
        }
        else if (getActivity().getIntent() != null
                && AlfrescoIntentAPI.ACTION_CREATE_ACCOUNT.equals(getActivity().getIntent().getAction()))
        {
            Bundle b = getActivity().getIntent().getExtras();
            if (b.containsKey(AlfrescoIntentAPI.EXTRA_ALFRESCO_REPOSITORY_URL)
                    && b.containsKey(AlfrescoIntentAPI.EXTRA_ALFRESCO_USERNAME))
            {
                // Display progressbar until MDM Event
                show(R.id.homescreen_configuration);
                hide(R.id.homescreen_login);

                FragmentDisplayer
                        .load(AccountSignInFragment.with(getActivity())
                                .repoUrl(b.getString(AlfrescoIntentAPI.EXTRA_ALFRESCO_REPOSITORY_URL))
                                .shareUrl(b.getString(AlfrescoIntentAPI.EXTRA_ALFRESCO_SHARE_URL))
                                .username(b.getString(AlfrescoIntentAPI.EXTRA_ALFRESCO_USERNAME)).back(false))
                        .animate(null).into(FragmentDisplayer.PANEL_LEFT);
            }
            else
            {
                displayLogin();
            }
        }
        else
        {
            displayLogin();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onMDMEvent(MDMEvent event)
    {
        if (event.exception != null)
        {
            displayError(event.exception.getMessage());
        }
        else
        {
            FragmentDisplayer.load(AccountSignInFragment.with(getActivity()).back(false)).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    private void displayError(String errorMessage)
    {
        hide(R.id.homescreen_config_progress);
        hide(R.id.homescreen_config_message);
        show(R.id.homescreen_config_error);

        TextView txt = (TextView) viewById(R.id.homescreen_config_error);
        txt.setText(Html.fromHtml(String.format(getString(R.string.error_mdm_loading_configuration), errorMessage)));
        HolderUtils.makeMultiLine(txt, 5);
        txt.setHorizontallyScrolling(false);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
    {
        public ScreenSlidePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            Fragment fr = new MarketingPageFragment();
            Bundle b = new Bundle();
            b.putInt(MarketingPageFragment.ARGUMENT_POSITION, position);
            fr.setArguments(b);
            return fr;
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }
    }
}

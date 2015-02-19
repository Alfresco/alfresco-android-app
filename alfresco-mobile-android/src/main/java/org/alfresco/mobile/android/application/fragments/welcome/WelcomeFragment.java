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
package org.alfresco.mobile.android.application.fragments.welcome;

import java.util.Map;

import org.alfresco.mobile.android.application.BuildConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.account.AccountSignInFragment;
import org.alfresco.mobile.android.application.fragments.account.AccountTypesFragment;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.mdm.MDMEvent;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * It's the first screen seens by the user when the application starts. Display
 * the first step of AlfrescoAccount creation wizard.
 * 
 * @author Jean Marie Pascal
 */
public class WelcomeFragment extends AlfrescoFragment
{
    public static final String TAG = WelcomeFragment.class.getName();

    public WelcomeFragment()
    {
        requiredSession = false;
        checkSession = false;
    }

    public static WelcomeFragment newInstanceByTemplate(Bundle b)
    {
        WelcomeFragment bf = new WelcomeFragment();
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onStart()
    {
        setRetainInstance(true);
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.app_name);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.app_name, false);
        }

        setRootView(inflater.inflate(R.layout.app_homescreen, container, false));

        // Request Mobile Iron Info
        MobileIronManager mdmManager = MobileIronManager.getInstance(getActivity());
        if (mdmManager != null)
        {
            mdmManager.requestConfig(getActivity(), BuildConfig.APPLICATION_ID);

            // Display progressbar until MDM Event
            show(R.id.homescreen_configuration);
            hide(R.id.homescreen_login);
            hide(R.id.help_guide);
        }
        else
        {
            Button login = (Button) viewById(R.id.homescreen_login);
            login.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AccountTypesFragment.with(getActivity()).display();
                }
            });
            TextView tv = (TextView) viewById(R.id.help_guide);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return getRootView();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onMDMEvent(MDMEvent event)
    {
        if (event.exception != null)
        {
            hide(R.id.homescreen_config_progress);
            hide(R.id.homescreen_config_message);
            show(R.id.homescreen_config_error);

            TextView txt = (TextView) viewById(R.id.homescreen_config_error);
            txt.setText(Html.fromHtml(String.format(getString(R.string.error_mdm_loading_configuration),
                    event.exception.getMessage())));
            txt.setMaxLines(5);
            txt.setSingleLine(false);
            txt.setHorizontallyScrolling(false);
        }
        else
        {
            FragmentDisplayer.load(AccountSignInFragment.with(getActivity()).back(false)).animate(null)
                    .into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity appActivity)
    {
        return new Builder(appActivity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
            this.menuIconId = R.drawable.ic_repository_dark;
            this.menuTitleId = R.string.menu_browse_root;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

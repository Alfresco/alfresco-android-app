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
package org.alfresco.mobile.android.application.fragments.account;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class AccountTypesFragment extends AlfrescoFragment
{
    public static final String TAG = AccountTypesFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountTypesFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
        checkSession = false;
        requiredSession = false;
    }

    protected static AccountTypesFragment newInstanceByTemplate(Bundle b)
    {
        AccountTypesFragment cbf = new AccountTypesFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.account_wizard_step1_description);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.account_wizard_step1_description,
                    !(getActivity() instanceof WelcomeActivity));
        }

        setRootView(inflater.inflate(R.layout.app_wizard_account_step1, container, false));

        Button step1 = (Button) viewById(R.id.alfresco_server);
        step1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AccountEditFragment.with(getActivity()).display();
            }
        });

        step1 = (Button) viewById(R.id.alfresco_cloud);
        step1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AccountOAuthFragment.with(getActivity()).isCreation(true).display();
            }
        });

        return getRootView();
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_application_logo);
        }
        getActivity().invalidateOptionsMenu();
        AccessibilityUtils.sendAccessibilityEvent(getActivity());
        super.onStart();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
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
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

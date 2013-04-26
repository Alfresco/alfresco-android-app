/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AccountTypesFragment extends DialogFragment
{
    public static final String TAG = "AccountTypesFragment";

    public AccountTypesFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle("Select Account");
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle("Select Account");
        }

        View v = inflater.inflate(R.layout.app_wizard_account_step1, container, false);

        Button step1 = (Button) v.findViewById(R.id.alfresco_server);
        step1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AccountEditFragment newFragment = new AccountEditFragment();
                FragmentDisplayer.replaceFragment(getActivity(), newFragment,
                        DisplayUtils.getMainPaneId(getActivity()), AccountEditFragment.TAG, true);
            }
        });

        step1 = (Button) v.findViewById(R.id.alfresco_cloud);
        step1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance();
                FragmentDisplayer.replaceFragment(getActivity(), newFragment,
                        DisplayUtils.getMainPaneId(getActivity()), AccountOAuthFragment.TAG, true);
            }
        });

        TextView t2 = (TextView) v.findViewById(R.id.cloud_signup_hint);
        if (getActivity() instanceof MainActivity)
        {
            t2.setText(R.string.account_wizard_alfresco_help_inside_app);
        }
        else
        {
            t2.setText(R.string.account_wizard_alfresco_help);
        }
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }
    
    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }
}

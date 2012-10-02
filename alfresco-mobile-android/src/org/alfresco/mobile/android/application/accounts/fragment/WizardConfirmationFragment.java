/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.accounts.fragment;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class WizardConfirmationFragment extends DialogFragment
{
    public static final String TAG = "ConfirmationFragment";

    public WizardConfirmationFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null){
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_cloud);
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null){
            getDialog().setTitle(R.string.sign_up_cloud);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        } else {
            getActivity().getActionBar().show();
            getActivity().setTitle(R.string.sign_up_cloud);
        }
        

        View v = inflater.inflate(R.layout.app_cloud_signup, container, false);

        Button done = (Button) v.findViewById(R.id.done);
        done.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
                ActionManager.actionRefresh(WizardConfirmationFragment.this, IntentIntegrator.CATEGORY_REFRESH_OTHERS,
                        IntentIntegrator.ACCOUNT_TYPE);
            }
        });
        
        return v;
    }

    private View findViewByIdInternal(int id){
        if (getDialog() != null){
            return getDialog().findViewById(id);
        } else {
            return getActivity().findViewById(id);
        }
    }
}

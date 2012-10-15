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
package org.alfresco.mobile.android.application.fragments.properties;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.documentfolder.actions.UpdateNodeDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeUpdateListener;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UpdateDialogFragment extends UpdateNodeDialogFragment
{
    
    public static final String TAG = "UpdateDialogFragment";
    
    private ProgressDialog mProgressDialog;

    public UpdateDialogFragment()
    {
    }

    public static UpdateDialogFragment newInstance(Node node)
    {
        UpdateDialogFragment adf = new UpdateDialogFragment();
        adf.setArguments(createBundle(node));
        return adf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getsession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getsession(getActivity());
        View v = super.onCreateView(inflater, container, savedInstanceState);

        onUpdateListener = new OnNodeUpdateListener()
        {

            @Override
            public void beforeUpdate(Node node)
            {
                /*mProgressDialog = ProgressDialog.show(getActivity(), "Update in Progress", "Contacting your server...", true,
                        true, new OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                if (getActivity() != null
                                        && getActivity().getLoaderManager().getLoader(UpdateNodeLoader.ID) != null)
                                {
                                    getActivity().getLoaderManager().destroyLoader(UpdateNodeLoader.ID);
                                }
                            }
                        });*/
            }

            @Override
            public void afterUpdate(Node node)
            {
                getDialog().dismiss();
                ActionManager.actionRefresh(UpdateDialogFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                        IntentIntegrator.NODE_TYPE);
                ((MainActivity) getActivity()).setCurrentNode(node);
            }

            @Override
            public void onExeceptionDuringUpdate(Exception arg0)
            {
                // TODO Auto-generated method stub
                getDialog().dismiss();
            }
        };

        return v;
    }

    @Override
    public void onDestroy()
    {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        super.onDismiss(dialog);
    }
    
}

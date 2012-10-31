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
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.documentfolder.actions.UpdateNodeDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeUpdateListener;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class UpdateDialogFragment extends UpdateNodeDialogFragment
{
    
    public static final String TAG = "UpdateDialogFragment";
    
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
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        View v = super.onCreateView(inflater, container, savedInstanceState);
        
        Node node = (Node) getArguments().getSerializable(ARGUMENT_NODE);
        if (node != null && node.isFolder())
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, MimeTypeManager.getIcon(node.getName()));
        }

        onUpdateListener = new OnNodeUpdateListener()
        {
            public boolean hasWaiting = false;

            @Override
            public void beforeUpdate(Node node)
            {
                if (!hasWaiting && getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null){
                    new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
                }
                hasWaiting = true;
            }

            @Override
            public void afterUpdate(Node node)
            {
                ActionManager.actionRefresh(UpdateDialogFragment.this, IntentIntegrator.CATEGORY_REFRESH_ALL,
                        IntentIntegrator.NODE_TYPE);
                ((MainActivity) getActivity()).setCurrentNode(node);
                getDialog().dismiss();
            }

            @Override
            public void onExeceptionDuringUpdate(Exception e)
            {
                ActionManager.actionDisplayError(UpdateDialogFragment.this, e);
                Log.e(TAG, Log.getStackTraceString(e));
                getDialog().dismiss();
            }
        };

        return v;
    }
}

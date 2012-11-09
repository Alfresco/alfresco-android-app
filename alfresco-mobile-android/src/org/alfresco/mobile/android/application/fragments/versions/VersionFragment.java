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
package org.alfresco.mobile.android.application.fragments.versions;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.version.VersionsFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class VersionFragment extends VersionsFragment
{

    public static final String TAG = "VersionFragment";

    public VersionFragment()
    {
    }

    public static VersionFragment newInstance(Node n)
    {
        VersionFragment bf = new VersionFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Node n = (Node) l.getItemAtPosition(position);
        ((MainActivity) getActivity()).addPropertiesFragment(n, true);
    }

    @Override
    public void onStart()
    {
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().setTitle(getString(R.string.document_version_header) + " : " + node.getName());
        }
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }
    
    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }
}

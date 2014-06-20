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
package org.alfresco.mobile.android.application.fragments.versions;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.version.VersionsFragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class VersionFragment extends VersionsFragment
{

    public static final String TAG = "VersionFragment";
    
    private static final String ARGUMENT_FOLDER = "parentFolderNode";

    private Folder parentFolder;

    public VersionFragment()
    {
    }

    public static VersionFragment newInstance(Node n, Folder parentFolder)
    {
        VersionFragment bf = new VersionFragment();
        Bundle b = createBundleArgs(n);
        b.putParcelable(ARGUMENT_FOLDER, (Parcelable) parentFolder);
        bf.setArguments(b);
        return bf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_FOLDER))
        {
            parentFolder = bundle.getParcelable(ARGUMENT_FOLDER);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Document versionedDoc = (Document) l.getItemAtPosition(position);
        if (versionedDoc.getVersionLabel() != null
                && !versionedDoc.getVersionLabel().equals(((Document) node).getVersionLabel()))
        {
            ((MainActivity) getActivity()).addPropertiesFragment(versionedDoc, parentFolder, true);
        }
    }

    @Override
    public void onResume()
    {
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), R.string.document_version_header);
        }
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }
}

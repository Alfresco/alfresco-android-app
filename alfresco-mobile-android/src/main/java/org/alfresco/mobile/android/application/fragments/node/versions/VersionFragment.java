/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.fragments.node.versions;

import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.node.version.DocumentVersionsEvent;
import org.alfresco.mobile.android.ui.node.version.VersionsNodeFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class VersionFragment extends VersionsNodeFragment
{
    public static final String TAG = "VersionFragment";

    private static final String ARGUMENT_FOLDER = "parentFolderNode";

    private Folder parentFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public VersionFragment()
    {
        reportAtCreation = false;
    }

    protected static VersionFragment newInstanceByTemplate(Bundle b)
    {
        VersionFragment cbf = new VersionFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        parentFolder = bundle.getParcelable(ARGUMENT_FOLDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(R.color.secondary_background));
        return v;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RESULTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(DocumentVersionsEvent results)
    {
        super.onResult(results);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ITEMS ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Document versionedDoc = (Document) l.getItemAtPosition(position);
        if (versionedDoc.getVersionLabel() != null && !versionedDoc.getVersionLabel().equals(node.getVersionLabel()))
        {
            NodeDetailsFragment.with(getActivity()).node(versionedDoc).parentFolder(parentFolder).display();
        }
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
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        public Builder parentFolder(Folder folder)
        {
            extraConfiguration.putSerializable(ARGUMENT_FOLDER, folder);
            return this;
        }

        public Builder node(Node node)
        {
            extraConfiguration.putSerializable(ARGUMENT_NODE, node);
            return this;
        }
    }
}

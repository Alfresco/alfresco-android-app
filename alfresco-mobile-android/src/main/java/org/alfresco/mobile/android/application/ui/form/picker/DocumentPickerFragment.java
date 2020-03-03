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
package org.alfresco.mobile.android.application.ui.form.picker;

import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.favorite.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.site.browser.BrowserSitesPagerFragment;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesRequest;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class DocumentPickerFragment extends AlfrescoFragment
{
    public static final String TAG = DocumentPickerFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public DocumentPickerFragment()
    {
    }

    protected static DocumentPickerFragment newInstanceByTemplate(Bundle b)
    {
        DocumentPickerFragment bf = new DocumentPickerFragment();
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        container.setVisibility(View.VISIBLE);
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        setRootView(inflater.inflate(R.layout.app_document_picker, container, false));

        if (getSession() == null) { return getRootView(); }

        // BUTTONS
        Button b = (Button) viewById(R.id.picker_root);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DocumentFolderBrowserFragment.with(getActivity()).folder(getSession().getRootFolder()).display();
            }
        });

        b = (Button) viewById(R.id.picker_sites);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BrowserSitesPagerFragment.with(getActivity()).display();
            }
        });

        b = (Button) viewById(R.id.picker_favorites);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FavoritesFragment.with(getActivity()).setMode(FavoriteNodesRequest.MODE_FOLDERS).display();
            }
        });

        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        SessionUtils.checkSession(getActivity(), getSession());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        if (getArguments() != null && getArguments().containsKey(PrivateIntent.EXTRA_DOCUMENTS))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_attachments));
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_attachments));
        }
        getActivity().invalidateOptionsMenu();

        super.onResume();
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

        public Builder fieldId(String fieldId)
        {
            BundleUtils.addIfNotNull(extraConfiguration, PrivateDialogActivity.EXTRA_FIELD_ID, fieldId);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

    public interface onPickDocumentFragment
    {
        void onNodeSelected(String fieldId, Map<String, Node> p);

        void onNodeClear(String fieldId);

        Map<String, Node> getNodeSelected(String fieldId);
    }
}

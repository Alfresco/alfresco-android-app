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

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.comments.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.tags.TagsListNodeFragment;
import org.alfresco.mobile.android.application.fragments.versions.VersionFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class ExtraDetailsFragment extends BaseFragment implements OnTabChangeListener
{
    public static final String TAG = "ExtraDetailsFragment";

    public static final String ARGUMENT_NODE = "commentedNode";

    private TabHost mTabHost;

    private Node node;

    public static ExtraDetailsFragment newInstance(Node n)
    {
        ExtraDetailsFragment bf = new ExtraDetailsFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_NODE, node);
        return args;
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
        if (node == null) node = getArguments().getParcelable(ARGUMENT_NODE);
        if (container == null) { return null; }
        View v = inflater.inflate(R.layout.app_details_extra, container, false);

        mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        setupTabs();
        return v;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mTabHost != null)
        {
            mTabHost.setCurrentTabByTag(TAB_COMMENTS);
        }
    }

    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_TAGS = "Tags";

    private void setupTabs()
    {
        mTabHost.setup(); // you must call this before adding your tabs!

        if (node.isDocument()) mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_versions, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.action_comments, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TAGS, R.string.action_tags, android.R.id.tabcontent));
        mTabHost.setOnTabChangedListener(this);
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId)
    {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId)
    {
        if (TAB_COMMENTS.equals(tabId))
            addComments(node);
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
            addVersions((Document) node);
        else if (TAB_TAGS.equals(tabId)) addTags(node);
    }

    public void addComments(Node n)
    {
        BaseFragment frag = CommentsFragment.newInstance(n);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, CommentsFragment.TAG, false);
    }

    public void addVersions(Document d)
    {
        BaseFragment frag = VersionFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, android.R.id.tabcontent, VersionFragment.TAG, false);
    }

    public void addTags(Node d)
    {
        BaseFragment frag = TagsListNodeFragment.newInstance(d);
        frag.setSession(alfSession);
        FragmentDisplayer
                .replaceFragment(getActivity(), frag, android.R.id.tabcontent, TagsListNodeFragment.TAG, false);
    }
}

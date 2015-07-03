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
package org.alfresco.mobile.android.application.fragments.node.details;

import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.node.comment.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.PreviewFragment;
import org.alfresco.mobile.android.application.fragments.node.versions.VersionFragment;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionEvent;
import org.alfresco.mobile.android.async.node.RetrieveNodeEvent;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeEvent;
import org.alfresco.mobile.android.async.node.download.DownloadEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeEvent;
import org.alfresco.mobile.android.async.node.like.LikeNodeEvent;
import org.alfresco.mobile.android.async.node.sync.SyncNodeEvent;
import org.alfresco.mobile.android.async.node.update.UpdateContentEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.squareup.otto.Subscribe;

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class TabsNodeDetailsFragment extends NodeDetailsFragment implements OnTabChangeListener
{
    public static final String TAG = TabsNodeDetailsFragment.class.getName();

    private TabHost mTabHost;

    private static final String TAB_SELECTED = "tabSelected";

    protected Integer tabSelected = null;

    protected Integer tabSelection = null;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public TabsNodeDetailsFragment()
    {
        requiredSession = true;
        checkSession = true;
        setHasOptionsMenu(true);
    }

    protected static TabsNodeDetailsFragment newInstanceByTemplate(Bundle b)
    {
        TabsNodeDetailsFragment bf = new TabsNodeDetailsFragment();
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        // Manage Tabs
        if (savedInstanceState != null)
        {
            tabSelection = savedInstanceState.getInt(TAB_SELECTED);
            savedInstanceState.remove(TAB_SELECTED);
        }
        return getRootView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (tabSelected != null)
        {
            outState.putInt(TAB_SELECTED, tabSelected);
        }
    }

    @Override
    public void onResume()
    {
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).setCurrentNode(node);
        }
        getActivity().invalidateOptionsMenu();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), R.string.details);
        }
        super.onResume();
    }

    // //////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void displayTabs()
    {
        mTabHost = (TabHost) viewById(android.R.id.tabhost);
        setupTabs();
    }

    @Override
    protected void displayPartsOffline(NodeSyncPlaceHolder refreshedNode)
    {
        displayTabs();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(RetrieveNodeEvent event)
    {
        super.onResult(event);
    }

    @Subscribe
    public void onLikeEvent(LikeNodeEvent event)
    {
        super.onLikeEvent(event);
    }

    @Subscribe
    public void onIsFavoriteEvent(FavoritedNodeEvent event)
    {
        super.onIsFavoriteEvent(event);
    }

    @Subscribe
    public void onSyncNodeEvent(SyncNodeEvent event)
    {
        super.onSyncNodeEvent(event);
    }

    @Subscribe
    public void onFavoriteEvent(FavoriteNodeEvent event)
    {
        super.onFavoriteNodeEvent(event);
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        super.onDocumentUpdated(event);
    }

    @Subscribe
    public void onContentUpdated(UpdateContentEvent event)
    {
        super.onContentUpdated(event);
    }

    @Subscribe
    public void onNodeDeleted(DeleteNodeEvent event)
    {
        super.onNodeDeleted(event);
    }

    @Subscribe
    public void onDocumentDownloaded(DownloadEvent event)
    {
        super.onDocumentDownloaded(event);
    }

    @Subscribe
    public void onFileProtectionEvent(FileProtectionEvent event)
    {
        super.onFileProtectionEvent(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU
    // ///////////////////////////////////////////////////////////////////////////
    private static final String TAB_PREVIEW = "Preview";

    private static final String TAB_METADATA = "Metadata";

    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_SUMMARY = "Summary";

    private void setupTabs()
    {
        if (mTabHost == null) { return; }

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);

        if (node instanceof NodeSyncPlaceHolder)
        {
            mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
            return;
        }

        if (DisplayUtils.hasCentralPane(getActivity()) && node.isDocument() && ((Document) node).isLatestVersion())
        {
            mTabHost.addTab(newTab(TAB_PREVIEW, R.string.preview, android.R.id.tabcontent));
        }
        mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.comments, android.R.id.tabcontent));
        if (node.isDocument())
        {
            mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_version, android.R.id.tabcontent));
        }

        mTabHost.setCurrentTab(1);
        mTabHost.setCurrentTab(0);

        if (tabSelection != null)
        {
            if (tabSelection == 0) { return; }
            int index = (node.isDocument()) ? tabSelection : tabSelection - 1;
            mTabHost.setCurrentTab(index);
        }
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
        if (TAB_METADATA.equals(tabId))
        {
            tabSelected = 1;
            addMetadata();
        }
        else if (TAB_COMMENTS.equals(tabId))
        {
            tabSelected = 2;
            addComments(node);
        }
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
        {
            tabSelected = 3;
            addVersions((Document) node);
        }
        else if (TAB_PREVIEW.equals(tabId))
        {
            tabSelected = 0;
            addPreview(node);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void addPreview(Node n)
    {
        addPreview(n, android.R.id.tabcontent);
    }

    public void addPreview(Node n, int layoutId)
    {
        PreviewFragment.with(getActivity()).node(n).touchEnable(DisplayUtils.hasCentralPane(getActivity())).back(false)
                .display(layoutId);
    }

    public void addComments(Node n, int layoutId)
    {
        CommentsFragment.with(getActivity()).node(n).back(false).display(layoutId);
    }

    public void addComments(Node n)
    {
        addComments(n, android.R.id.tabcontent);
    }

    public void addMetadata()
    {
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            NodePropertiesFragment.with(getActivity()).node(node).parentFolder(parentNode).back(false)
                    .display(android.R.id.tabcontent);
        }
        else
        {
            NodeSummaryFragment.with(getActivity()).node(node).parentFolder(parentNode).back(false)
                    .display(android.R.id.tabcontent);
        }
    }

    public void addVersions(Document d, int layoutId)
    {
        VersionFragment.with(getActivity()).node(d).parentFolder(parentNode).back(false).display(layoutId);
    }

    public void addVersions(Document d)
    {
        addVersions(d, android.R.id.tabcontent);
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends NodeDetailsFragment.Builder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

}

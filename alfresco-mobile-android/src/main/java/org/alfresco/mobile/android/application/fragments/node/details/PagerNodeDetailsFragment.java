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
package org.alfresco.mobile.android.application.fragments.node.details;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
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
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.otto.Subscribe;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

/**
 * Responsible to display details of a specific Node.
 *
 * @author Jean Marie Pascal
 */
public class PagerNodeDetailsFragment extends NodeDetailsFragment
{
    public static final String TAG = PagerNodeDetailsFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PagerNodeDetailsFragment()
    {
        setHasOptionsMenu(true);
        screenName = AnalyticsManager.SCREEN_NODE_SUMMARY;
        reportAtCreation = false;
    }

    protected static PagerNodeDetailsFragment newInstanceByTemplate(Bundle b)
    {
        PagerNodeDetailsFragment bf = new PagerNodeDetailsFragment();
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String onPrepareTitle()
    {
        return getString(R.string.details);
    }

    @Override
    public void onResume()
    {
        ((MainActivity) getActivity()).setCurrentNode(node);
        getActivity().invalidateOptionsMenu();
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // ///////////////////////////////////////////////////////////////////////////
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void displayTabs()
    {
        // Retrieve pager & pager tabs
        ViewPager viewPager = (ViewPager) viewById(R.id.view_pager);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) viewById(R.id.tabs);
        if (tabs == null) { return; }

        tabs.setBackgroundColor(getActivity().getResources().getColor(R.color.grey_lighter));
        NodeDetailsPagerAdapter adapter = new NodeDetailsPagerAdapter(getChildFragmentManager(), getActivity(), node,
                parentNode);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);
        pageChangeListener.onPageSelected(0);
        viewPager.setCurrentItem(0);
        tabs.setViewPager(viewPager);
        tabs.setTextColor(getResources().getColor(android.R.color.black));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // super.onActivityResult(requestCode, resultCode, data);

        // notifying nested fragments (support library bug fix)
        final FragmentManager childFragmentManager = getChildFragmentManager();

        if (childFragmentManager != null)
        {
            final List<Fragment> nestedFragments = childFragmentManager.getFragments();

            if (nestedFragments == null || nestedFragments.size() == 0) return;

            for (Fragment childFragment : nestedFragments)
            {
                if (childFragment != null && !childFragment.isDetached() && !childFragment.isRemoving())
                {
                    childFragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {

        }

        @Override
        public void onPageSelected(int position)
        {
            if (AnalyticsManager.getInstance(getActivity()) != null)
            {
                boolean isTablet = !getActivity().getResources().getBoolean(R.bool.fr_details_summary);
                if (node instanceof NodeSyncPlaceHolder)
                {
                    screenName = (isTablet) ? AnalyticsManager.SCREEN_NODE_PROPERTIES
                            : AnalyticsManager.SCREEN_NODE_SUMMARY;
                }
                else if (node instanceof Folder)
                {
                    switch (position + 1)
                    {
                        case NodeDetailsPagerAdapter.TAB_METADATA:
                            screenName = (isTablet) ? AnalyticsManager.SCREEN_NODE_PROPERTIES
                                    : AnalyticsManager.SCREEN_NODE_SUMMARY;
                            break;
                        case NodeDetailsPagerAdapter.TAB_COMMENTS:
                            screenName = AnalyticsManager.SCREEN_NODE_COMMENTS;
                            break;
                        default:
                            break;
                    }
                }
                else
                {
                    int relativePosition = position;
                    if (!isTablet)
                    {
                        relativePosition++;
                    }
                    switch (relativePosition)
                    {
                        case NodeDetailsPagerAdapter.TAB_PREVIEW:
                            screenName = AnalyticsManager.SCREEN_NODE_PREVIEW;
                            break;
                        case NodeDetailsPagerAdapter.TAB_METADATA:
                            screenName = (isTablet) ? AnalyticsManager.SCREEN_NODE_PROPERTIES
                                    : AnalyticsManager.SCREEN_NODE_SUMMARY;
                            break;
                        case NodeDetailsPagerAdapter.TAB_COMMENTS:
                            screenName = AnalyticsManager.SCREEN_NODE_COMMENTS;
                            break;
                        case NodeDetailsPagerAdapter.TAB_HISTORY:
                            screenName = AnalyticsManager.SCREEN_NODE_VERSIONS;
                            break;
                        default:
                            break;
                    }
                }
                AnalyticsHelper.reportScreen(getActivity(), screenName);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    };

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(RetrieveNodeEvent event)
    {
        super.onResult(event);
        if (getActivity() == null) { return; }
        if (getRootView() == null) { return; }
        if (event.hasException)
        {
            show(R.id.pager_empty);
            displayEmptyView();
            if (viewById(R.id.empty_text) != null)
            {
                if (event.exception != null && event.exception instanceof NullPointerException)
                {
                    ((TextView) viewById(R.id.empty_text)).setText(R.string.node_details_file_not_found);
                }
                else
                {
                    ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
                }
            }
        }
        else
        {
            hide(R.id.pager_empty);
        }
    }

    @Subscribe
    public void onLikeEvent(LikeNodeEvent event)
    {
        super.onLikeEvent(event);
    }

    @Subscribe
    public void onFavoriteEvent(FavoritedNodeEvent event)
    {
        super.onIsFavoriteEvent(event);
    }

    @Subscribe
    public void onFavoriteEvent(FavoriteNodeEvent event)
    {
        super.onFavoriteNodeEvent(event);
    }

    @Subscribe
    public void onSyncNodeEvent(SyncNodeEvent event)
    {
        super.onSyncNodeEvent(event);
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        try
        {
            super.onDocumentUpdated(event);
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
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
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends NodeDetailsFragment.Builder
    {
        // //////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
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

// ///////////////////////////////////////////////////////////////////////////
// INTERNAL CLASSES
// ///////////////////////////////////////////////////////////////////////////
class NodeDetailsPagerAdapter extends FragmentStatePagerAdapter
{
    protected static final int TAB_PREVIEW = 0;

    protected static final int TAB_METADATA = 1;

    protected static final int TAB_COMMENTS = 2;

    protected static final int TAB_HISTORY = 3;

    private WeakReference<FragmentActivity> activity;

    private final Node node;

    private final Folder parentFolder;

    private int numberOfFragment = 3;

    private boolean isTabletLayout = false;

    public NodeDetailsPagerAdapter(android.support.v4.app.FragmentManager fm, FragmentActivity activity, Node node,
            Folder parentFolder)
    {
        super(fm);
        this.activity = new WeakReference<>(activity);
        this.node = node;
        this.parentFolder = parentFolder;

        if (node instanceof NodeSyncPlaceHolder)
        {
            // Summary (without preview)
            numberOfFragment = 1;

        }
        else if (node instanceof Folder)
        {
            // Summary (without preview) / Comments
            numberOfFragment = 2;
        }
        else if (!activity.getResources().getBoolean(R.bool.fr_details_summary))
        {
            // Preview / Properties / Comments / Versions
            isTabletLayout = true;
            numberOfFragment = 4;
        }
        else
        {
            // Summary / Comments / Versions
            numberOfFragment = 3;
        }

    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fr = null;
        if (node instanceof NodeSyncPlaceHolder)
        {
            if (!activity.get().getResources().getBoolean(R.bool.fr_details_summary))
            {
                fr = new NodePropertiesFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                        .isFavorite(true).createFragment();
            }
            else
            {
                fr = new NodeSummaryFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                        .isFavorite(true).createFragment();
            }
        }
        else if (node instanceof Folder)
        {
            switch (position + 1)
            {
                case TAB_METADATA:
                    if (activity.get().getResources().getBoolean(R.bool.fr_details_summary))
                    {
                        fr = new NodeSummaryFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                                .createFragment();
                    }
                    else
                    {
                        fr = new NodePropertiesFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                                .createFragment();
                    }
                    break;
                case TAB_COMMENTS:
                    fr = CommentsFragment.with(activity.get()).node(node).createFragment();
                    break;
                default:
                    break;
            }
        }
        else
        {
            int relativePosition = position;
            if (!isTabletLayout)
            {
                relativePosition++;
            }
            switch (relativePosition)
            {
                case TAB_PREVIEW:
                    fr = PreviewFragment.with(activity.get()).node(node)
                            .touchEnable(DisplayUtils.hasCentralPane(activity.get())).createFragment();
                    break;
                case TAB_METADATA:
                    if (activity.get().getResources().getBoolean(R.bool.fr_details_summary))
                    {
                        fr = new NodeSummaryFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                                .createFragment();
                    }
                    else
                    {
                        fr = new NodePropertiesFragment.Builder(activity.get()).node(node).parentFolder(parentFolder)
                                .createFragment();
                    }
                    break;
                case TAB_COMMENTS:
                    fr = CommentsFragment.with(activity.get()).node(node).createFragment();
                    break;
                case TAB_HISTORY:
                    fr = VersionFragment.with(activity.get()).node(node).parentFolder(parentFolder).createFragment();
                    break;
                default:
                    break;
            }
        }
        return fr;
    }

    @Override
    public int getCount()
    {
        return numberOfFragment;
    }

    public CharSequence getPageTitle(int position)
    {
        int titleId = 0;
        if (node instanceof NodeSyncPlaceHolder)
        {
            titleId = R.string.metadata;
        }
        else if (node instanceof Folder)
        {
            switch (position + 1)
            {
                case TAB_METADATA:
                    titleId = R.string.metadata;
                    break;
                case TAB_COMMENTS:
                    titleId = R.string.comments;
                    break;
                default:
                    break;
            }
        }
        else
        {
            int relativePosition = position;
            if (!isTabletLayout)
            {
                relativePosition++;
            }
            switch (relativePosition)
            {
                case TAB_PREVIEW:
                    titleId = R.string.preview;
                    break;
                case TAB_METADATA:
                    titleId = R.string.metadata;
                    break;
                case TAB_COMMENTS:
                    titleId = R.string.comments;
                    break;
                case TAB_HISTORY:
                    titleId = R.string.action_version;
                    break;
                default:
                    break;
            }
        }
        return activity.get().getString(titleId).toUpperCase();
    }

}

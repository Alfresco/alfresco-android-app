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
package org.alfresco.mobile.android.application.fragments.node.rendition;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.details.DetailsFragmentTemplate;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenEvent;
import org.alfresco.mobile.android.async.node.browse.NodeChildrenRequest;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.node.browse.NodeBrowserTemplate;

import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CarouselPreviewFragment extends AlfrescoFragment implements DetailsFragmentTemplate
{
    public static final String TAG = CarouselPreviewFragment.class.getName();

    private List<Node> nodes = new ArrayList<Node>();

    private Node node;

    private String nodePath;

    private String nodeIdentifier;

    private DocumentFolderBrowserFragment frag;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public CarouselPreviewFragment()
    {
        screenName = AnalyticsManager.SCREEN_NODE_GALLERY;
    }

    protected static CarouselPreviewFragment newInstanceByTemplate(Bundle b)
    {
        CarouselPreviewFragment bf = new CarouselPreviewFragment();
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Retrieve arguments
        if (getArguments() != null)
        {
            node = (Node) getArguments().get(ARGUMENT_NODE);
            nodePath = (String) getArguments().get(ARGUMENT_PATH);
            nodeIdentifier = (String) getArguments().get(ARGUMENT_NODE_ID);
        }

        // Retrieve nodes
        frag = (DocumentFolderBrowserFragment) ((getActivity()).getSupportFragmentManager()
                .findFragmentByTag(DocumentFolderBrowserFragment.TAG));
        if (frag != null && nodes.isEmpty())
        {
            List<Node> tmpNodes = frag.getNodes();
            for (Node node : tmpNodes)
            {
                if (node.isDocument())
                {
                    nodes.add(node);
                }
            }
        }

        if (node == null && frag != null)
        {
            node = frag.getSelectedNodes();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.app_gallery, container, false));

        // If nodes are available.
        if (nodes != null && !nodes.isEmpty() || node != null)
        {
            displayGallery();
        }
        else if (!TextUtils.isEmpty(nodeIdentifier))
        {
            Operator.with(getActivity()).load(new NodeChildrenRequest.Builder(nodeIdentifier));
            displayLoading();
        }
        else if (!TextUtils.isEmpty(nodePath))
        {
            Operator.with(getActivity()).load(
                    new NodeChildrenRequest.Builder(NodeBrowserTemplate.ARGUMENT_PATH, nodePath));
            displayLoading();
        }
        else
        {
            displayEmptyView();
        }

        return getRootView();
    }

    protected void displayGallery()
    {
        ViewPager viewPager = (ViewPager) viewById(R.id.view_pager);
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager(), nodes,
                getActivity());
        viewPager.setAdapter(adapter);
        if (node != null)
        {
            viewPager.setCurrentItem(nodes.indexOf(node));
        }
        else if (nodes.size() > 0 && frag != null)
        {
            frag.highLight(nodes.get(0));
        }
        viewPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int location)
            {
                if (frag != null)
                {
                    frag.highLight(nodes.get(location));
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                // Nothing special
            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
                // Nothing special
            }
        });

        viewPager.setPageTransformer(true, new DepthPageTransformer());

        PagerTabStrip pagerTabStrip = (PagerTabStrip) viewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.accent));

        getActivity().invalidateOptionsMenu();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayData()
    {
        hide(R.id.empty);
        hide(R.id.progressbar);
        show(R.id.view_pager);
    }

    protected void displayEmptyView()
    {
        show(R.id.empty);
        hide(R.id.progressbar);
        hide(R.id.view_pager);
    }

    protected void displayLoading()
    {
        show(R.id.progressbar);
        hide(R.id.view_pager);
        hide(R.id.empty);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(NodeChildrenEvent event)
    {
        if (event.hasException)
        {
            displayEmptyView();
            ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
        }
        else if (getActivity() != null)
        {
            nodes = event.data.getList();
            displayData();
            displayGallery();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
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
            this.extraConfiguration = new Bundle();
            this.menuIconId = R.drawable.ic_gallery_dark;
            this.menuTitleId = R.string.display_gallery;
            templateArguments = new String[] { ARGUMENT_NODE_ID, ARGUMENT_PATH };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder node(Node node)
        {
            extraConfiguration.putSerializable(ARGUMENT_NODE, node);
            return this;
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
class DepthPageTransformer implements ViewPager.PageTransformer
{
    private static float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position)
    {
        int pageWidth = view.getWidth();

        if (position < -1)
        { // [-Infinity,-1)
          // This page is way off-screen to the left.
            view.setAlpha(0);

        }
        else if (position <= 0)
        { // [-1,0]
          // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        }
        else if (position <= 1)
        { // (0,1]
          // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        }
        else
        { // (1,+Infinity]
          // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}

class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
{
    private List<Node> nodes = new ArrayList<Node>();

    private WeakReference<FragmentActivity> activity;

    public ScreenSlidePagerAdapter(FragmentManager fm, List<Node> nodes, FragmentActivity activity)
    {
        super(fm);
        this.nodes = nodes;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public Fragment getItem(int position)
    {
        return PreviewFragment.with(activity.get()).node(nodes.get(position)).touchEnable(true).createFragment();
    }

    @Override
    public int getCount()
    {
        return nodes.size();
    }

    public CharSequence getPageTitle(int position)
    {
        return nodes.get(position).getName();
    }
}

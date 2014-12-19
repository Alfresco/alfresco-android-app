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
package org.alfresco.mobile.android.application.fragments.node.rendition;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GalleryPreviewFragment extends AlfrescoFragment
{
    public static final String TAG = GalleryPreviewFragment.class.getName();

    public static final String ARGUMENT_NODE = "node";

    private List<Node> nodes = new ArrayList<Node>();

    private Node node;

    private DocumentFolderBrowserFragment frag;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public GalleryPreviewFragment()
    {
    }

    protected static GalleryPreviewFragment newInstanceByTemplate(Bundle b)
    {
        GalleryPreviewFragment bf = new GalleryPreviewFragment();
        bf.setArguments(b);
        return bf;
    };

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setSession(SessionUtils.getSession(getActivity()));
        SessionUtils.checkSession(getActivity(), getSession());
        View v = inflater.inflate(R.layout.app_gallery, container, false);

        // Retrieve nodes
        frag = (DocumentFolderBrowserFragment) ((getActivity()).getFragmentManager()
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

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_NODE))
        {
            node = (Node) getArguments().get(ARGUMENT_NODE);
        }
        else
        {
            node = frag.getSelectedNodes();
        }

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(getActivity().getFragmentManager(), nodes,
                getActivity());
        viewPager.setAdapter(adapter);
        if (node != null)
        {
            viewPager.setCurrentItem(nodes.indexOf(node));
        }
        else if (nodes.size() > 0)
        {
            frag.unselect();
            frag.highLight(nodes.get(0));
        }
        viewPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int location)
            {
                frag.unselect();
                frag.highLight(nodes.get(location));
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

        PagerTabStrip pagerTabStrip = (PagerTabStrip) v.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.blue_light));

        return v;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
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

    private WeakReference<Activity> activity;

    public ScreenSlidePagerAdapter(FragmentManager fm, List<Node> nodes, Activity activity)
    {
        super(fm);
        this.nodes = nodes;
        this.activity = new WeakReference<Activity>(activity);
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

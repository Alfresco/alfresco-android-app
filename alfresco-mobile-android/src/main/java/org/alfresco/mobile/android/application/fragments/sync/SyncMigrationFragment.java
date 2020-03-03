/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.mobile.android.application.fragments.sync;

import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SyncMigrationFragment extends AlfrescoFragment
{
    public static final String TAG = SyncMigrationFragment.class.getName();

    public static final int REQUEST_CODE = 980;

    private static final int NUM_PAGES = 3;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private Button skip;

    private Button done;

    private Button next;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public SyncMigrationFragment()
    {
        super();
    }

    public static SyncMigrationFragment newInstanceByTemplate(Bundle b)
    {
        SyncMigrationFragment cbf = new SyncMigrationFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (getRootView() != null) { return getRootView(); }

        setRootView(inflater.inflate(R.layout.fr_version_info, container, false));

        // Button
        skip = Button.class.cast(viewById(R.id.skip));
        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                endInfo();
            }
        });

        next = Button.class.cast(viewById(R.id.next));
        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
            }
        });

        done = Button.class.cast(viewById(R.id.done));
        done.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                endInfo();
            }
        });

        // Instantiate a ViewPager and a PagerAdapter.
        CircleIndicator defaultIndicator = (CircleIndicator) viewById(R.id.welcome_pager_indicator);
        mPager = (ViewPager) viewById(R.id.welcome_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        defaultIndicator.setViewPager(mPager);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if (position == NUM_PAGES - 1)
                {
                    skip.setText(R.string.sync_info_more_information);
                    skip.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            displayMoreInformation();
                        }
                    });
                    next.setVisibility(View.GONE);
                    done.setVisibility(View.VISIBLE);
                }
                else if (position < NUM_PAGES - 1)
                {
                    skip.setText(R.string.sync_info_skip);
                    skip.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            endInfo();
                        }
                    });
                    skip.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    done.setVisibility(View.GONE);
                }
                else if (position == NUM_PAGES)
                {
                    skip.setText(R.string.sync_info_skip);
                    skip.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            endInfo();
                        }
                    });
                    endInfo();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        return getRootView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mPager != null)
        {
            mPager.clearOnPageChangeListeners();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private void endInfo()
    {
        SyncContentManager.saveStateInfo(getActivity());
        getActivity().finish();
    }

    private void displayMoreInformation()
    {
        getActivity().setResult(FragmentActivity.RESULT_OK);
        getActivity().finish();
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
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter
    {
        public ScreenSlidePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            Fragment fr = new SyncMigrationPagerFragment();
            Bundle b = new Bundle();
            b.putInt(SyncMigrationPagerFragment.ARGUMENT_POSITION, position);
            fr.setArguments(b);
            return fr;
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }
    }
}

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
package org.alfresco.mobile.android.application.fragments.node.favorite;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesEvent;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.node.favorite.FavoritesNodeFragment;
import org.alfresco.mobile.android.ui.template.ListingTemplate;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

public class FavoritesFragment extends FavoritesNodeFragment
{
    public static final String TAG = FavoritesFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesFragment()
    {
        emptyListMessageId = R.string.empty_favorites;
    }

    public static FavoritesFragment newInstanceByTemplate(Bundle b)
    {
        FavoritesFragment cbf = new FavoritesFragment();
        cbf.setArguments(b);
        b.putBoolean(ARGUMENT_BASED_ON_TEMPLATE, true);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onResume()
    {
        super.onResume();
        UIUtils.displayTitle(getActivity(), R.string.menu_favorites);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RESULT
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new NodeAdapter(getActivity(), R.layout.sdk_grid_row, new ArrayList<Node>(0), selectedItems,
                ListingModeFragment.MODE_LISTING);
    }

    @Override
    @Subscribe
    public void onResult(FavoriteNodesEvent event)
    {
        super.onResult(event);
        gv.setColumnWidth(DisplayUtils.getDPI(getResources().getDisplayMetrics(), 1000));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        Node item = (Node) g.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
            selectedItems.clear();
        }
        g.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        g.setItemChecked(position, true);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            if (item.isFolder())
            {
                DocumentFolderBrowserFragment.with(getActivity()).folder((Folder) item).shortcut(true).display();
            }
            else
            {
                // Show properties
                NodeDetailsFragment.with(getActivity()).nodeId(item.getIdentifier()).display();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);

            menuIconId = R.drawable.ic_favorite_dark;
            menuTitleId = R.string.menu_favorites;
            templateArguments = new String[] { FILTER_KEY_MODE };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder setMode(int mode)
        {
            ListingContext lc = null;
            if (extraConfiguration == null)
            {
                extraConfiguration = new Bundle();
            }
            else
            {
                lc = (ListingContext) extraConfiguration.getSerializable(ListingTemplate.ARGUMENT_LISTING);
            }

            if (!extraConfiguration.containsKey(ListingTemplate.ARGUMENT_LISTING))
            {
                lc = new ListingContext();
            }

            ListingFilter lf = lc.getFilter();
            if (lf == null)
            {
                lf = new ListingFilter();
            }

            lf.addFilter(FILTER_KEY_MODE, mode);
            lc.setFilter(lf);

            extraConfiguration.putSerializable(ListingTemplate.ARGUMENT_LISTING, lc);

            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
        {
            // Add Listing Filter as arguments for the view.
            FavoritesNodeFragment.addFilter(properties, b);
        }

        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

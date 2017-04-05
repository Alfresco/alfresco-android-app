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
package org.alfresco.mobile.android.application.fragments.node.favorite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.ListingFilter;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.FavoritesConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsActionMode;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodesEvent;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.SelectableFragment;
import org.alfresco.mobile.android.ui.node.favorite.FavoritesNodeFragment;
import org.alfresco.mobile.android.ui.template.ListingTemplate;

import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoritesFragment extends FavoritesNodeFragment implements SelectableFragment
{
    public static final String TAG = FavoritesFragment.class.getName();

    protected List<Node> selectedItems = new ArrayList<Node>(1);

    private AbstractActions<Node> nActions;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesFragment()
    {
        emptyListMessageId = R.string.empty_favorites;
        screenName = AnalyticsManager.SCREEN_FAVORITES;
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
    public String onPrepareTitle()
    {
        return getString(R.string.menu_favorites);
    }

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setImageResource(R.drawable.ic_empty_favorites);
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        firstEmptyMessage.setText(R.string.favorites_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.favorites_empty_description);
    }

    @Override
    public void onStop()
    {
        if (nActions != null)
        {
            nActions.finish();
        }
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // RESULT
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new FavoriteNodeAdapter(this, R.layout.row_two_lines_progress, new ArrayList<Node>(0), selectedItems,
                ListingModeFragment.MODE_LISTING);
    }

    @Override
    @Subscribe
    public void onResult(FavoriteNodesEvent event)
    {
        super.onResult(event);
        gv.setColumnWidth(DisplayUtils.getDPI(getResources().getDisplayMetrics(), 1000));
        FavoritesManager.getInstance(getActivity()).sync(getAccount());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        Node item = (Node) g.getItemAtPosition(position);

        // In other case, listing mode
        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
        }
        g.setItemChecked(position, true);

        if (nActions != null && nActions.hasMultiSelectionEnabled())
        {
            nActions.selectNode(item);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && item.isDocument() && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(item);
            }
        }

        if (hideDetails)
        {
            FragmentDisplayer.clearCentralPane(getActivity());
            if (nActions != null && !nActions.hasMultiSelectionEnabled())
            {
                nActions.finish();
            }
        }
        else if (nActions == null || (nActions != null && !nActions.hasMultiSelectionEnabled()))
        {
            if (item.isFolder())
            {
                FragmentDisplayer.clearCentralPane(getActivity());
                DocumentFolderBrowserFragment.with(getActivity()).folder((Folder) item).shortcut(true).display();
            }
            else
            {
                NodeDetailsFragment.with(getActivity()).nodeId(item.getIdentifier()).display();
            }
        }

        if (nActions != null && nActions.hasMultiSelectionEnabled())
        {
            adapter.notifyDataSetChanged();
        }
    }

    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        // We disable long click during import mode.
        if (mode == MODE_IMPORT || mode == MODE_PICK) { return false; }

        if (nActions != null && nActions instanceof NodeDetailsActionMode)
        {
            nActions.finish();
        }

        Node n = (Node) l.getItemAtPosition(position);
        boolean b;
        l.setItemChecked(position, true);
        b = startSelection(n);
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
            FragmentDisplayer.with(getActivity()).remove(android.R.id.tabcontent);
        }
        return b;
    }

    private boolean startSelection(Node item)
    {
        if (nActions != null) { return false; }

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeActions(FavoritesFragment.this, selectedItems);
        nActions.setOnFinishModeListener(new AbstractActions.onFinishModeListener()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                unselect();
                refreshListView();
                displayFab(-1, null);
            }
        });

        displayFab(R.drawable.ic_done_all_white, onMultiSelectionFabClickListener());
        getActivity().startActionMode(nActions);
        adapter.notifyDataSetChanged();
        return true;
    }

    public void unselect()
    {
        selectedItems.clear();
    }

    @Override
    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            displayFab(R.drawable.ic_close_dark, onCancelMultiSelectionFabClickListener());
            nActions.selectNodes(((NodeAdapter) adapter).getNodes());
            adapter.notifyDataSetChanged();
        }
    }

    protected View.OnClickListener onMultiSelectionFabClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectAll();
            }
        };
    }

    protected View.OnClickListener onCancelMultiSelectionFabClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (nActions != null)
                {
                    nActions.finish();
                }
            }
        };
    }

    private void displayFab(int iconId, View.OnClickListener listener)
    {
        if (listener != null)
        {
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(iconId);
            fab.setOnClickListener(listener);
            fab.show(true);
        }
        else
        {
            fab.setVisibility(View.GONE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onFavoriteNodeEvent(FavoriteNodeEvent event)
    {
        if (event.hasException) { return; }
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
            refresh();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS & HELPERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            viewConfigModel = new FavoritesConfigModel(configuration);
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

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
package org.alfresco.mobile.android.application.fragments.node.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.KeywordSearchOptions;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.model.view.RepositorySearchConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.browser.ProgressNodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsActionMode;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.search.SearchEvent;
import org.alfresco.mobile.android.async.node.search.SearchRequest;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.SelectableFragment;
import org.alfresco.mobile.android.ui.node.search.SearchNodesFragment;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3
 * @author Jean Marie Pascal
 */
public class DocumentFolderSearchFragment extends SearchNodesFragment implements SelectableFragment
{

    public static final String TAG = DocumentFolderSearchFragment.class.getName();

    private static final String ARGUMENT_TITLE = "title";

    private static final String KEYWORD = "{keyword}";

    private static final String QUERY_FOLDER = "SELECT * FROM cmis:folder where CONTAINS('~cmis:name:\\\'{keyword}\\\'')";

    protected List<Node> selectedItems = new ArrayList<Node>(1);

    private AbstractActions<Node> nActions;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public DocumentFolderSearchFragment()
    {
        super();
        loadState = LOAD_VISIBLE;
        displayAsList = false;
        reportAtCreation = false;
        screenName = AnalyticsManager.SCREEN_SEARCH_RESULT_FILES;
    }

    protected static DocumentFolderSearchFragment newInstanceByTemplate(Bundle b)
    {
        DocumentFolderSearchFragment cbf = new DocumentFolderSearchFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        if (TextUtils.isEmpty(title) && bundle.containsKey(ARGUMENT_TITLE))
        {
            title = (String) bundle.get(ARGUMENT_TITLE);
        }
        if (keywords != null)
        {
            title = String.format(getString(R.string.search_title), keywords);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new ProgressNodeAdapter(this, R.layout.row_two_lines_progress, null, new ArrayList<Node>(0),
                selectedItems, ListingModeFragment.MODE_LISTING);
    }

    @Subscribe
    public void onResult(SearchEvent event)
    {
        super.onResult(event);
        if (adapter == null) { return; }
        if (getSession() instanceof CloudSession)
        {
            ((NodeAdapter) adapter).setActivateThumbnail(false);
        }
        else
        {
            ((NodeAdapter) adapter).setActivateThumbnail(true);
        }
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

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // //////////////////////////////////////////////////////////////////////
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
        nActions = new NodeActions(DocumentFolderSearchFragment.this, selectedItems);
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

    @Override
    protected void prepareEmptyView(View ev, ImageView emptyImageView, TextView firstEmptyMessage,
            TextView secondEmptyMessage)
    {
        emptyImageView.setLayoutParams(DisplayUtils.resizeLayout(getActivity(), 275, 275));
        emptyImageView.setImageResource(
                searchFolderOnly ? R.drawable.ic_empty_search_folders : R.drawable.ic_empty_search_documents);
        firstEmptyMessage.setText(R.string.node_search_empty_title);
        secondEmptyMessage.setVisibility(View.VISIBLE);
        secondEmptyMessage.setText(R.string.node_search_empty_description);
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        if (searchFolderOnly && statement == null)
        {
            statement = QUERY_FOLDER.replace(KEYWORD, keywords);
            language = SearchLanguage.CMIS.value();
        }

        if (statement != null && language == null)
        {
            language = SearchLanguage.CMIS.value();
        }

        if (statement != null)
        {
            return new SearchRequest.Builder(statement, SearchLanguage.fromValue(language))
                    .setListingContext(listingContext);
        }
        else if (keywords != null) { return new SearchRequest.Builder(keywords,
                new KeywordSearchOptions(f, includeDescendants, fullText, isExact)).setListingContext(listingContext); }
        return null;
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
            viewConfigModel = new RepositorySearchConfigModel(configuration);
            this.templateArguments = new String[] { ARGUMENT_KEYWORDS, ARGUMENT_STATEMENT };
        }

        @Override
        protected void retrieveCustomArgument(Map<String, Object> properties, Bundle b)
        {
            if (properties.containsKey(ARGUMENT_SEARCH_FOLDER))
            {
                b.putBoolean(ARGUMENT_SEARCH_FOLDER, JSONConverter.getBoolean(properties, ARGUMENT_SEARCH_FOLDER));
            }
            if (properties.containsKey(ARGUMENT_FULLTEXT))
            {
                b.putBoolean(ARGUMENT_FULLTEXT, JSONConverter.getBoolean(properties, ARGUMENT_FULLTEXT));
            }
            if (properties.containsKey(ARGUMENT_EXACTMATCH))
            {
                b.putBoolean(ARGUMENT_EXACTMATCH, JSONConverter.getBoolean(properties, ARGUMENT_EXACTMATCH));
            }
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder keywords(String keywords)
        {
            extraConfiguration.putString(ARGUMENT_KEYWORDS, keywords);
            return this;
        }

        public Builder query(String query)
        {
            extraConfiguration.putString(ARGUMENT_STATEMENT, query);
            return this;
        }

        public Builder searchFolder(Boolean searchFolder)
        {
            extraConfiguration.putBoolean(ARGUMENT_SEARCH_FOLDER, searchFolder);
            return this;
        }

        public Builder parentFolder(Folder parentFolder)
        {
            extraConfiguration.putSerializable(ARGUMENT_PARENTFOLDER, parentFolder);
            return this;
        }

        public Builder title(String title)
        {
            extraConfiguration.putString(ARGUMENT_TITLE, title);
            return this;
        }
    }
}

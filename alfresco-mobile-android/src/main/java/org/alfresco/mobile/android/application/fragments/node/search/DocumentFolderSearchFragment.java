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
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.NodeAdapter;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.node.search.SearchEvent;
import org.alfresco.mobile.android.async.node.search.SearchRequest;
import org.alfresco.mobile.android.ui.node.search.SearchNodesFragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

/**
 * @since 1.3
 * @author Jean Marie Pascal
 */
public class DocumentFolderSearchFragment extends SearchNodesFragment
{

    public static final String TAG = DocumentFolderSearchFragment.class.getName();

    private static final String ARGUMENT_TITLE = "title";

    private static final String KEYWORD = "{keyword}";

    private static final String QUERY_FOLDER = "SELECT * FROM cmis:folder where CONTAINS('~cmis:name:\\\'{keyword}\\\'')";

    private List<Node> selectedItems = new ArrayList<Node>(1);

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public DocumentFolderSearchFragment()
    {
        super();
        loadState = LOAD_VISIBLE;
        displayAsList = false;
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
        mTitle = (String) bundle.get(ARGUMENT_TITLE);
    }

    @Override
    protected String onCreateTitle(String title)
    {
        if (keywords != null)
        {
            return String.format(getString(R.string.search_title), keywords);
        }
        else
        {
            return super.onCreateTitle(title);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new NodeAdapter(this, R.layout.sdk_grid_row, new ArrayList<Node>(0), selectedItems, -1);
    }

    @Subscribe
    public void onResult(SearchEvent event)
    {
        super.onResult(event);
        if (adapter == null){return;}
        if (getSession() instanceof CloudSession)
        {
            ((NodeAdapter) adapter).setActivateThumbnail(false);
        }
        else
        {
            ((NodeAdapter) adapter).setActivateThumbnail(true);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // //////////////////////////////////////////////////////////////////////
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        Node item = (Node) g.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
            selectedItems.clear();
        }
        g.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        g.setItemChecked(position, true);
        g.setSelection(position);
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
                FragmentDisplayer.with(getActivity()).remove(android.R.id.tabcontent);
            }
            selectedItems.clear();
        }
        else
        {
            if (item.isDocument())
            {
                // Show properties
                NodeDetailsFragment.with((Activity) getActivity()).nodeId(item.getIdentifier()).display();
            }
            else
            {
                DocumentFolderBrowserFragment.with(getActivity()).folder((Folder) item).shortcut(true).display();
            }
        }
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
        else if (keywords != null) { return new SearchRequest.Builder(keywords, new KeywordSearchOptions(f,
                includeDescendants, fullText, isExact)).setListingContext(listingContext); }
        return null;
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
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };

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

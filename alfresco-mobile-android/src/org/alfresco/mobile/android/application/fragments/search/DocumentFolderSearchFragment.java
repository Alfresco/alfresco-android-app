/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.search;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.SearchLoader;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.browser.NodeAdapter;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

/**
 * @version 1.3
 * @author jpascal
 */
public class DocumentFolderSearchFragment extends GridSearchFragment
{

    public static final String TAG = DocumentFolderSearchFragment.class.getName();

    private static final String PARAM_SITE = "site";

    private static final String PARAM_KEYWORD = "keyword";

    private static final String PARAM_SEARCH_FOLDER = "searchFolder";

    private List<Node> selectedItems = new ArrayList<Node>(1);

    private String keywords;

    private boolean searchFolder = false;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public DocumentFolderSearchFragment()
    {
        super();
        initLoader = false;
    }

    public static DocumentFolderSearchFragment newInstance(Folder parentFolder, Site site)
    {
        DocumentFolderSearchFragment ssf = new DocumentFolderSearchFragment();
        Bundle b = new Bundle();
        b.putSerializable(FOLDER, parentFolder);
        b.putSerializable(PARAM_SITE, site);
        ListingContext lc = new ListingContext();
        lc.setSortProperty("");
        lc.setIsSortAscending(true);
        b.putAll(createBundleArgs(lc, LOAD_MANUAL));
        ssf.setArguments(b);
        return ssf;
    }

    public static DocumentFolderSearchFragment newInstance(String query, boolean searchFolder)
    {
        DocumentFolderSearchFragment ssf = new DocumentFolderSearchFragment();
        Bundle b = new Bundle();
        ListingContext lc = new ListingContext();
        lc.setSortProperty("");
        lc.setIsSortAscending(true);
        b.putAll(createBundleArgs(lc, LOAD_MANUAL));
        b.putSerializable(PARAM_KEYWORD, query);
        b.putBoolean(PARAM_SEARCH_FOLDER, searchFolder);
        ssf.setArguments(b);
        return ssf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        setActivateThumbnail(false);
        View v = inflater.inflate(R.layout.sdk_grid, container, false);
        if (alfSession == null) { return v; }

        init(v, R.string.empty_child);

        gv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (getArguments() != null)
        {
            searchFolder = getArguments().getBoolean(PARAM_SEARCH_FOLDER);
            keywords = getArguments().getString(PARAM_KEYWORD);
        }

        if (keywords != null && !keywords.isEmpty())
        {
            if (searchFolder)
            {
                searchFolder(null, keywords);
            }
            else
            {
                search(keywords, true, false);
            }
        }

        return v;
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected void search(Folder parentFolder, String keywords, boolean fullText, boolean isExact)
    {
        Bundle b = new Bundle();
        b.putAll(getArguments());
        b.putString(KEYWORDS, keywords);
        b.putBoolean(INCLUDE_CONTENT, fullText);
        b.putBoolean(EXACTMATCH, isExact);

        // Reduce voluntary result list for cloud.
        if (alfSession instanceof CloudSession)
        {
            b.putSerializable(ARGUMENT_GRID, new ListingContext("", MAX_RESULT_ITEMS, 0, false));
        }
        if (parentFolder != null)
        {
            b.putSerializable(FOLDER, parentFolder);
        }

        reload(b, SearchLoader.ID, this);
    }

    private static final String KEYWORD = "{keyword}";

    private static final String QUERY_FOLDER = "SELECT * FROM cmis:folder where CONTAINS('~cmis:name:\\\'{keyword}\\\'')";

    protected void searchFolder(Folder parentFolder, String keyword)
    {

        Bundle b = new Bundle();
        b.putAll(getArguments());
        b.putString(STATEMENT, QUERY_FOLDER.replace(KEYWORD, keyword));
        b.putString(LANGUAGE, SearchLanguage.CMIS.value());

        // Reduce voluntary result list for cloud.
        if (alfSession instanceof CloudSession)
        {
            b.putSerializable(ARGUMENT_GRID, new ListingContext("", MAX_RESULT_ITEMS, 0, false));
        }
        if (parentFolder != null)
        {
            b.putSerializable(FOLDER, parentFolder);
        }

        reload(b, SearchLoader.ID, this);
    }

    // //////////////////////////////////////////////////////////////////////
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Node>>> arg0, LoaderResult<PagingResult<Node>> results)
    {
        if (adapter == null)
        {
            gv.setColumnWidth(DisplayUtils.getDPI(getResources().getDisplayMetrics(), 240));
            adapter = new NodeAdapter(this, R.layout.app_grid_progress_row, new ArrayList<Node>(0), selectedItems, -1);
        }
        if (alfSession instanceof CloudSession)
        {
            ((NodeAdapter) adapter).setActivateThumbnail(false);
        }
        else
        {
            ((NodeAdapter) adapter).setActivateThumbnail(true);
        }

        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            Log.d(TAG, "Result : " + results.getData().getTotalItems());
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        gv.setEmptyView(ev);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // //////////////////////////////////////////////////////////////////////
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Node item = (Node) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
            selectedItems.clear();
        }
        l.setItemChecked(position, true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
                FragmentDisplayer.removeFragment(getActivity(), android.R.id.tabcontent);
            }
            selectedItems.clear();
        }
        else
        {
            if (item.isDocument())
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier());
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
            else
            {
                ((MainActivity) getActivity()).addNavigationFragment((Folder) item, true);
            }
        }

    }
}

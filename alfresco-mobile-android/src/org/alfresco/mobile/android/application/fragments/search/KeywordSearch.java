/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.browser.NodeAdapter;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.search.SearchFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.ActionBar;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class KeywordSearch extends SearchFragment
{

    public static final String TAG = "SearchFragment";

    private static final String PARAM_SITE = "site";

    private static final int MAX_RESULT_ITEMS = 30;

    private Site site;

    private Folder tmpParentFolder;

    private TextView pathView;

    private List<Node> selectedItems = new ArrayList<Node>(1);

    public static KeywordSearch newInstance(Folder parentFolder, Site site)
    {
        KeywordSearch ssf = new KeywordSearch();
        Bundle b = new Bundle();
        b.putSerializable(FOLDER, parentFolder);
        b.putSerializable(PARAM_SITE, site);
        ssf.setArguments(b);
        return ssf;
    }

    public static KeywordSearch newInstance(String keywords)
    {
        KeywordSearch ssf = new KeywordSearch();
        ssf.setArguments(createBundleArgs(keywords));
        return ssf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        setActivateThumbnail(false);
        View v = inflater.inflate(R.layout.app_search, container, false);

        init(v, R.string.empty_child);

        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final EditText searchForm = (EditText) v.findViewById(R.id.search_query);
        searchForm.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        pathView = (TextView) v.findViewById(R.id.search_path);
        tmpParentFolder = null;
        site = null;
        if (getArguments() != null)
        {
            site = (Site) getArguments().getSerializable(PARAM_SITE);
            tmpParentFolder = (Folder) getArguments().getSerializable(FOLDER);
        }

        final Folder parentFolder = tmpParentFolder;
        if (parentFolder != null)
        {
            searchForm.requestFocus();
        }

        searchForm.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (event != null
                        && (event.getAction() == KeyEvent.ACTION_DOWN)
                        && ((actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))
                {
                    if (searchForm.getText().length() > 0)
                    {
                        search(parentFolder, searchForm.getText().toString(), true, false);
                    }
                    else
                    {
                        MessengerManager.showLongToast(getActivity(), getString(R.string.search_form_hint));
                    }
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    protected void search(Folder parentFolder, String keywords, boolean fullText, boolean isExact)
    {
        Bundle b = new Bundle();
        b.putString(KEYWORDS, keywords);
        b.putBoolean(INCLUDE_CONTENT, fullText);
        b.putBoolean(EXACTMATCH, isExact);

        // Reduce voluntary result list for cloud.
        if (alfSession instanceof CloudSession)
        {
            b.putSerializable(ARGUMENT_LISTING, new ListingContext("", MAX_RESULT_ITEMS, 0, false));
        }
        if (parentFolder != null)
        {
            b.putSerializable(FOLDER, parentFolder);
        }

        reload(b, SearchLoader.ID, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
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
            // Show properties
            ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier());
            DisplayUtils.switchSingleOrTwo(getActivity(), true);
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        setTitle();
        setActivateThumbnail(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void setTitle()
    {
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);

        title = getString(R.string.search);
        if (tmpParentFolder != null)
        {
            String pathValue = (String) tmpParentFolder.getPropertyValue(PropertyIds.PATH);
            if (site != null)
            {
                String[] path = pathValue.split("/");
                pathValue = "";
                if (path.length == 4)
                {
                    pathValue = site.getTitle();
                }
                else
                {
                    pathValue = site.getTitle() + "/";
                    for (int i = 4; i < path.length; i++)
                    {
                        pathValue += path[i] + "/";
                    }
                }
            }
            pathView.setText(pathValue);
        }
        getActivity().setTitle(title);
    }

    public void onPause()
    {
        getActivity().invalidateOptionsMenu();
        super.onPause();
    }

    public void onLoaderException(Exception e)
    {
        Log.e(TAG, Log.getStackTraceString(e));
        MessengerManager.showToast(getActivity(), R.string.error_general);
        setListShown(true);
        lv.setEmptyView(ev);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Node>>> arg0, LoaderResult<PagingResult<Node>> results)
    {
        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Node>(0), selectedItems, -1);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(true);

        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    public void unselect()
    {
        selectedItems.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Avoid background stretching
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }
}

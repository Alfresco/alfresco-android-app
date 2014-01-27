/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Date;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class SearchFragment extends BaseCursorListFragment
{
    public static final String TAG = SearchFragment.class.getName();

    private static final String PARAM_SITE = "site";

    private static final String PARAM_FOLDER = "parentFolder";

    private View rootView;

    private AlfrescoSession alfSession;

    private CursorLoader loader;

    private int optionPosition = 0;

    private ImageView searchIcon;

    private EditText searchForm;

    private int searchKey = HistorySearch.TYPE_DOCUMENT;

    private BaseFragment frag;

    private SearchOptionAdapter optionAdapter;

    private TextView pathView;

    private Site site;

    private Folder tmpParentFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SearchFragment()
    {
        emptyListMessageId = R.string.empty_accounts;
        title = R.string.accounts_manage;
    }

    public static SearchFragment newInstance()
    {
        return new SearchFragment();
    }

    public static SearchFragment newInstance(Folder parentFolder, Site site)
    {
        SearchFragment ssf = new SearchFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_FOLDER, parentFolder);
        b.putSerializable(PARAM_SITE, site);
        ssf.setArguments(b);
        return ssf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        rootView = inflater.inflate(R.layout.app_search_history, container, false);
        init(rootView, emptyListMessageId);

        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        // Folder path
        if (getArguments() != null && getArguments().containsKey(PARAM_FOLDER))
        {
            pathView = (TextView) rootView.findViewById(R.id.search_path);
            site = (Site) getArguments().getSerializable(PARAM_SITE);
            tmpParentFolder = (Folder) getArguments().getSerializable(PARAM_FOLDER);
            rootView.findViewById(R.id.search_path_panel).setVisibility(View.VISIBLE);
            displayPathOption();
        }
        else
        {
            rootView.findViewById(R.id.search_path_panel).setVisibility(View.GONE);
        }

        // Search Icon
        searchIcon = (ImageView) rootView.findViewById(R.id.search_icon);

        // Search Input
        searchForm = (EditText) rootView.findViewById(R.id.search_query);
        searchForm.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
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
                        search(searchForm.getText().toString());
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

        // Advanced Button
        LinearLayout bAdvanced = (LinearLayout) rootView.findViewById(R.id.advanced_search);
        bAdvanced.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                displayAdvancedSearch();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setListShown(adapter != null);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setDisplayShowCustomEnabled(false);
            displaySearchOptionHeader();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        else
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // SEARCH OPTIONS
    // //////////////////////////////////////////////////////////////////////
    private void displaySearchOptionHeader()
    {
        // /QUICK PATH
        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            optionAdapter = new SearchOptionAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    SearchOptionAdapter.getSearchOptions(alfSession, (tmpParentFolder != null)));

            OnNavigationListener mOnNavigationListener = new OnNavigationListener()
            {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId)
                {
                    optionPosition = itemPosition;
                    updateForm(optionAdapter.getItem(itemPosition));
                    return true;
                }
            };
            getActivity().getActionBar().setListNavigationCallbacks(optionAdapter, mOnNavigationListener);
            getActivity().getActionBar().setSelectedNavigationItem(optionPosition);
        }
    }

    private void displayPathOption()
    {
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
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    private void search(String keywords)
    {
        search(keywords, null);
    }

    public void search(String keywords, HistorySearch search)
    {
        // History search so we use the query
        if (search != null && search.getAdvanced() == 1)
        {

            switch (searchKey)
            {
                case HistorySearch.TYPE_PERSON:
                    frag = PersonSearchFragment.newInstance(search.getQuery(), null);
                    frag.setSession(alfSession);
                    FragmentDisplayer.replaceFragment(getActivity(), frag,
                            DisplayUtils.getLeftFragmentId(getActivity()), PersonSearchFragment.TAG, true, false);
                    break;
                default:
                    frag = DocumentFolderSearchFragment.newInstance(search.getQuery());
                    frag.setSession(alfSession);
                    FragmentDisplayer.replaceFragment(getActivity(), frag,
                            DisplayUtils.getLeftFragmentId(getActivity()), DocumentFolderSearchFragment.TAG, true,
                            false);
                    break;
            }

            // Update
            HistorySearchManager.update(getActivity(), search.getId(), search.getAccountId(), search.getType(),
                    search.getAdvanced(), search.getDescription(), search.getQuery(), new Date().getTime());
            return;
        }

        switch (searchKey)
        {
            case HistorySearch.TYPE_DOCUMENT:
                // Display results
                frag = DocumentFolderSearchFragment.newInstance(keywords, false, tmpParentFolder, null);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        DocumentFolderSearchFragment.TAG, true, false);
                break;
            case HistorySearch.TYPE_PERSON:
                frag = PersonSearchFragment.newInstance(keywords, null);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        PersonSearchFragment.TAG, true, false);
                break;
            default:
                frag = DocumentFolderSearchFragment.newInstance(keywords, true);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        DocumentFolderSearchFragment.TAG, true, false);
                break;
        }

        // Save history or update
        if (search == null)
        {
            HistorySearchManager.createHistorySearch(getActivity(), SessionUtils.getAccount(getActivity()).getId(),
                    searchKey, 0, keywords, null, new Date().getTime());
        }
        else
        {
            HistorySearchManager.update(getActivity(), search.getId(), search.getAccountId(), search.getType(),
                    search.getAdvanced(), search.getDescription(), search.getQuery(), new Date().getTime());
        }

    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    private void updateForm(int id)
    {
        int hintId = R.string.search_form_hint;
        int iconId = R.drawable.ic_search;
        switch (id)
        {
            case HistorySearch.TYPE_PERSON:
                hintId = R.string.search_person_hint;
                iconId = R.drawable.ic_person;
                break;
            case HistorySearch.TYPE_DOCUMENT:
                hintId = R.string.search_form_hint;
                iconId = R.drawable.ic_office;
                break;
            case HistorySearch.TYPE_FOLDER:
                hintId = R.string.search_form_hint;
                iconId = R.drawable.ic_repository_dark;
                break;
            default:
                break;
        }

        // Reset form
        searchIcon.setImageResource(iconId);
        searchForm.getText().clear();
        searchForm.setHint(hintId);
        searchKey = id;

        // Refresh History
        refreshHistory();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        loader = new CursorLoader(getActivity(), HistorySearchManager.CONTENT_URI, HistorySearchManager.COLUMN_ALL,
                HistorySearchSchema.COLUMN_TYPE + " = " + searchKey, null,
                HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP + " DESC " + " LIMIT 20");

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        if (adapter == null)
        {
            adapter = new HistorySearchCursorAdapter(getActivity(), cursor, R.layout.sdk_list_row);
        }
        else
        {
            adapter.changeCursor(cursor);
        }
        lv.setAdapter(adapter);

        setEmptyShown(false);
        setListShown(true);
    }

    // /////////////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String keywords = cursor.getString(HistorySearchSchema.COLUMN_DESCRIPTION_ID);
        search(keywords, HistorySearchManager.createHistorySearch(cursor));
    }

    private void refreshHistory()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void displayAdvancedSearch()
    {
        AdvancedSearchFragment frag = AdvancedSearchFragment.newInstance(searchKey, site, tmpParentFolder);
        frag.setSession(alfSession);
        FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                AdvancedSearchFragment.TAG, true, true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Menu menu)
    {
        // TODO Auto-generated method stub

    }
}

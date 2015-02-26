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
package org.alfresco.mobile.android.application.fragments.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.search.DocumentFolderSearchFragment;
import org.alfresco.mobile.android.application.fragments.user.UsersFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.providers.search.HistorySearch;
import org.alfresco.mobile.android.application.providers.search.HistorySearchCursorAdapter;
import org.alfresco.mobile.android.application.providers.search.HistorySearchManager;
import org.alfresco.mobile.android.application.providers.search.HistorySearchSchema;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseCursorGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public class SearchFragment extends BaseCursorGridFragment
{
    public static final String TAG = SearchFragment.class.getName();

    private static final String ARGUMENT_SITE = "site";

    private static final String ARGUMENT_FOLDER = "parentFolder";

    private int optionPosition = 0;

    private ImageView searchIcon;

    private EditText searchForm;

    private int searchKey = HistorySearch.TYPE_DOCUMENT;

    private SearchOptionAdapter optionAdapter;

    private TextView pathView;

    private Site site;

    private Folder tmpParentFolder;

    private AbstractActions<Long> nActions;

    protected List<Long> selectedItems = new ArrayList<Long>(1);

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SearchFragment()
    {
        titleId = R.string.search;
        requiredSession = true;
        checkSession = true;
        setHasOptionsMenu(true);
    }

    protected static SearchFragment newInstanceByTemplate(Bundle b)
    {
        SearchFragment bf = new SearchFragment();
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        site = (Site) getArguments().getSerializable(ARGUMENT_SITE);
        tmpParentFolder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);

        if (tmpParentFolder == null)
        {
            hide(R.id.search_path_panel);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        setRootView(inflater.inflate(R.layout.app_search_history, container, false));
        init(getRootView(), emptyListMessageId);

        // Folder path
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_FOLDER))
        {
            pathView = (TextView) viewById(R.id.search_path);
            show(R.id.search_path_panel);
            displayPathOption();
        }
        else
        {
            hide(R.id.search_path_panel);
        }

        // Search Icon
        searchIcon = (ImageView) viewById(R.id.search_icon);

        // Search Input
        searchForm = (EditText) viewById(R.id.search_query);
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
                        UIUtils.hideKeyboard(getActivity());
                    }
                    else
                    {
                        AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                                getString(R.string.search_form_hint));
                    }
                    return true;
                }
                return false;
            }
        });

        // Speech to Text
        Boolean hasTextToSpeech = ActionUtils.hasSpeechToText(getActivity());
        ImageButton speechToText = (ImageButton) viewById(R.id.search_microphone);
        if (hasTextToSpeech)
        {
            speechToText.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    speechToText();
                }
            });
        }
        else
        {
            hide(R.id.search_microphone);
        }

        return getRootView();
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
            refreshSilently();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RequestCode.TEXT_TO_SPEECH:
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchForm.setText(text.get(0));
                    search(text.get(0));
                }
                break;
            }
            default:
                break;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        getMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_search_option:
                displayAdvancedSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // //////////////////////////////////////////////////////////////////////
    // SEARCH OPTIONS
    // //////////////////////////////////////////////////////////////////////
    private void speechToText()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

        try
        {
            if (intent.resolveActivity(getActivity().getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showAlertCrouton(getActivity(),
                        getString(R.string.feature_disable));
                return;
            }
            startActivityForResult(intent, RequestCode.TEXT_TO_SPEECH);
        }
        catch (ActivityNotFoundException a)
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.file_editor_error_speech);
        }
    }

    private void displaySearchOptionHeader()
    {
        // /QUICK PATH
        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            optionAdapter = new SearchOptionAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    SearchOptionAdapter.getSearchOptions(getSession(), (tmpParentFolder != null)));

            OnNavigationListener mOnNavigationListener = new OnNavigationListener()
            {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId)
                {
                    if (itemPosition == optionPosition) { return true; }
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
        StringBuilder pathBuilder = new StringBuilder();
        if (tmpParentFolder != null)
        {
            String pathValue = tmpParentFolder.getPropertyValue(PropertyIds.PATH);
            if (site != null)
            {
                String[] path = pathValue.split("/");

                if (path.length == 4)
                {
                    pathBuilder.append(site.getTitle());
                }
                else
                {
                    pathBuilder.append(site.getTitle());
                    pathBuilder.append("/");
                    for (int i = 4; i < path.length; i++)
                    {
                        pathBuilder.append(path[i]);
                        pathBuilder.append("/");
                    }
                }
            }
            pathView.setText(pathBuilder.toString());
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
                    UsersFragment.with(getActivity()).keywords(search.getQuery()).title(search.getDescription())
                            .display();
                    break;
                default:
                    DocumentFolderSearchFragment.with(getActivity()).query(search.getQuery())
                            .title(search.getDescription()).display();
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
                DocumentFolderSearchFragment.with(getActivity()).keywords(keywords).searchFolder(false)
                        .parentFolder(tmpParentFolder).display();
                break;
            case HistorySearch.TYPE_PERSON:
                UsersFragment.with(getActivity()).keywords(keywords).display();
                break;
            default:
                DocumentFolderSearchFragment.with(getActivity()).keywords(keywords).searchFolder(true).display();
                break;
        }

        // Save history or update
        if (search == null)
        {
            HistorySearchManager.createHistorySearch(getActivity(), SessionUtils.getAccount(getActivity()).getId(),
                    searchKey, 0, getQueryDescription(keywords, tmpParentFolder, site), keywords, new Date().getTime());
        }
        else
        {
            HistorySearchManager.update(getActivity(), search.getId(), search.getAccountId(), search.getType(),
                    search.getAdvanced(), search.getDescription(), search.getQuery(), new Date().getTime());
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // QUERY DESCRIPTION
    // //////////////////////////////////////////////////////////////////////
    private static final String DOCUMENT_LIBRARY_PATTERN = "/Sites/%s/documentLibrary";

    private static String getQueryDescription(String keywords, Folder folder, Site site)
    {
        StringBuilder builder = new StringBuilder();
        if (folder != null)
        {
            // If Site Documentlibrary we display the site name instead of
            // folder name
            if (site != null
                    && String.format(DOCUMENT_LIBRARY_PATTERN, site.getIdentifier()).equalsIgnoreCase(
                            (String) folder.getPropertyValue(PropertyIds.PATH)))
            {
                addParameter(builder, "in", site.getTitle());
            }
            else
            {
                addParameter(builder, "in", folder.getName());
            }
            builder.append(" ");
        }
        builder.append(keywords);
        return builder.toString();
    }

    private static void addParameter(StringBuilder builder, String key, String value)
    {
        if (TextUtils.isEmpty(value)) { return; }
        if (builder.length() != 0)
        {
            builder.append(" ");
        }
        builder.append(key);
        builder.append(":");
        builder.append(value);
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    private void updateForm(int id)
    {
        int hintId = R.string.search_form_hint;
        int iconId = R.drawable.ic_search_light;

        switch (id)
        {
            case HistorySearch.TYPE_PERSON:
                hintId = R.string.search_person_hint;
                // iconId = R.drawable.ic_person;
                break;
            case HistorySearch.TYPE_DOCUMENT:
                hintId = R.string.search_form_hint;
                // iconId = R.drawable.ic_office;
                break;
            case HistorySearch.TYPE_FOLDER:
                hintId = R.string.search_form_hint;
                // iconId = R.drawable.ic_repository_light;
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
        refreshSilently();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected BaseAdapter onAdapterCreation()
    {
        return new HistorySearchCursorAdapter(getActivity(), null, R.layout.sdk_list_row, selectedItems);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(getActivity(), HistorySearchManager.CONTENT_URI, HistorySearchManager.COLUMN_ALL,
                HistorySearchSchema.COLUMN_TYPE + " = " + searchKey, null,
                HistorySearchSchema.COLUMN_LAST_REQUEST_TIMESTAMP + " DESC " + " LIMIT 20");
    }

    @Override
    protected void performRequest(ListingContext lcorigin)
    {
        getLoaderManager().initLoader(0, null, this);
    }

    // /////////////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        long searchId = cursor.getLong(HistorySearchSchema.COLUMN_ID_ID);

        // In other case, listing mode
        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.contains(searchId);
        }
        l.setItemChecked(position, true);

        if (nActions != null && nActions.hasMultiSelectionEnabled())
        {
            nActions.selectNode(searchId);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(searchId);
            }
        }

        if (hideDetails)
        {
            if (nActions != null && !nActions.hasMultiSelectionEnabled())
            {
                nActions.finish();
            }
        }
        else if (nActions == null || (nActions != null && !nActions.hasMultiSelectionEnabled()))
        {
            String keywords = cursor.getString(HistorySearchSchema.COLUMN_QUERY_ID);
            search(keywords, HistorySearchManager.createHistorySearch(cursor));
        }
        refreshListView();
    }

    @Override
    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        if (nActions != null && nActions instanceof HistorySearchActions)
        {
            nActions.finish();
        }

        Cursor c = (Cursor) l.getItemAtPosition(position);
        long searchId = c.getLong(HistorySearchSchema.COLUMN_ID_ID);
        boolean b = true;
        l.setItemChecked(position, true);
        b = startSelection(searchId);
        refreshListView();
        return b;
    }

    private boolean startSelection(long item)
    {
        if (nActions != null) { return false; }

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new HistorySearchActions(SearchFragment.this, selectedItems);
        nActions.setOnFinishModeListener(new AbstractActions.onFinishModeListener()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                unselect();
                refreshListView();
            }
        });
        getActivity().startActionMode(nActions);
        return true;
    }

    public void unselect()
    {
        selectedItems.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void displayAdvancedSearch()
    {
        AdvancedSearchFragment.with(getActivity()).searchType(searchKey).site(site).folder(tmpParentFolder).display();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Menu menu)
    {
        MenuItem mi = menu.add(Menu.NONE, R.id.menu_search_option, Menu.FIRST, R.string.search_advanced);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
    {
        public static final int ICON_ID = R.drawable.ic_search_dark;

        public static final int LABEL_ID = R.string.menu_search;

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
            this.menuIconId = ICON_ID;
            this.menuTitleId = LABEL_ID;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder folder(Folder folder)
        {
            extraConfiguration.putSerializable(ARGUMENT_FOLDER, folder);
            return this;
        }

        public Builder site(Site site)
        {
            extraConfiguration.putSerializable(ARGUMENT_SITE, site);
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

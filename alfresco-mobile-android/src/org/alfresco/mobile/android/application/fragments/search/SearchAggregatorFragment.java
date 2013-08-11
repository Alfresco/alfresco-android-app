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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.person.PersonSearchFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @since 1.3
 * @author jpascal
 */
public class SearchAggregatorFragment extends BaseFragment implements OnMenuItemClickListener
{

    public static final String TAG = SearchAggregatorFragment.class.getName();

    private static final int MENU_ITEM_PERSON = 1;

    private static final int MENU_ITEM_DOCUMENT = 2;

    private static final int MENU_ITEM_FOLDER = 4;

    private static final int MENU_ITEM_ADVANCED = 0;

    private View vRoot;

    private EditText searchForm;

    private int searchKey = MENU_ITEM_DOCUMENT;

    private ImageView searchIcon;

    private BaseFragment frag;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public SearchAggregatorFragment()
    {
    }

    public static SearchAggregatorFragment newInstance()
    {
        SearchAggregatorFragment bf = new SearchAggregatorFragment();
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }

        // Retrieve session object
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);

        // Create View
        vRoot = inflater.inflate(R.layout.app_search_aggreagator, container, false);
        if (alfSession == null) { return vRoot; }

        // Search Switcher
        FrameLayout layout = (FrameLayout) vRoot.findViewById(R.id.search_switcher);
        layout.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                getMenu(popup.getMenu());
                popup.setOnMenuItemClickListener(SearchAggregatorFragment.this);
                popup.show();
            }
        });

        // Search Icon
        searchIcon = (ImageView) vRoot.findViewById(R.id.search_icon);

        // Search Input
        searchForm = (EditText) vRoot.findViewById(R.id.search_query);
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

        updateForm(searchKey);

        return vRoot;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    private void search(String keywords)
    {
        switch (searchKey)
        {
            case MENU_ITEM_DOCUMENT:
                frag = DocumentFolderSearchFragment.newInstance(keywords, false);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, R.id.search_list_group,
                        DocumentFolderSearchFragment.TAG, false, false);
                break;
            case MENU_ITEM_PERSON:
                frag = PersonSearchFragment.newInstance(keywords);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, R.id.search_list_group,
                        PersonSearchFragment.TAG, false, false);
                break;
            default:
                frag = DocumentFolderSearchFragment.newInstance(keywords, true);
                frag.setSession(alfSession);
                FragmentDisplayer.replaceFragment(getActivity(), frag, R.id.search_list_group,
                        DocumentFolderSearchFragment.TAG, false, false);
                break;
        }
    }

    private void advancedSearch()
    {
        switch (searchKey)
        {
            case MENU_ITEM_DOCUMENT:
                break;
            case MENU_ITEM_PERSON:
                break;
            default:
                break;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    private void getMenu(Menu menu)
    {
        //menu.add(Menu.NONE, MENU_ITEM_ADVANCED, Menu.FIRST + MENU_ITEM_ADVANCED, R.string.search_advanced);

        switch (searchKey)
        {
            case MENU_ITEM_PERSON:
                menu.add(Menu.NONE, MENU_ITEM_DOCUMENT, Menu.FIRST + MENU_ITEM_DOCUMENT, R.string.search_documents);
                menu.add(Menu.NONE, MENU_ITEM_FOLDER, Menu.FIRST + MENU_ITEM_FOLDER, R.string.search_folders);
                break;
            case MENU_ITEM_DOCUMENT:
                menu.add(Menu.NONE, MENU_ITEM_PERSON, Menu.FIRST + MENU_ITEM_PERSON, R.string.search_person);
                menu.add(Menu.NONE, MENU_ITEM_FOLDER, Menu.FIRST + MENU_ITEM_FOLDER, R.string.search_folders);
                break;
            case MENU_ITEM_FOLDER:
                menu.add(Menu.NONE, MENU_ITEM_DOCUMENT, Menu.FIRST + MENU_ITEM_DOCUMENT, R.string.search_documents);
                menu.add(Menu.NONE, MENU_ITEM_PERSON, Menu.FIRST + MENU_ITEM_PERSON, R.string.search_person);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if (item.getItemId() == MENU_ITEM_ADVANCED)
        {
            advancedSearch();
        }
        else
        {
            updateForm(item.getItemId());
        }
        return true;
    }

    private void updateForm(int id)
    {
        int hintId = R.string.search_form_hint;
        int iconId = R.drawable.ic_search;
        switch (id)
        {
            case MENU_ITEM_PERSON:
                hintId = R.string.search_person_hint;
                iconId = R.drawable.ic_person;
                break;
            case MENU_ITEM_DOCUMENT:
                hintId = R.string.search_form_hint;
                iconId = R.drawable.ic_office;
                break;
            case MENU_ITEM_FOLDER:
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

        // Remove fragment
        FragmentDisplayer.remove(getActivity(), frag);
    }
}

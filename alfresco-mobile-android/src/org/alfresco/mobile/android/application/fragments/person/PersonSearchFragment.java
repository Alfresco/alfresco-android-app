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
package org.alfresco.mobile.android.application.fragments.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.workflow.ProcessTasksLoader;
import org.alfresco.mobile.android.application.fragments.workflow.TaskDetailsFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @since 1.3
 * @author jpascal
 */
public class PersonSearchFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<Person>>>, ListingModeFragment
{

    public static final String TAG = PersonSearchFragment.class.getName();

    private static final String PARAM_KEYWORD = "keyWord";

    private Map<String, Person> selectedItems = new HashMap<String, Person>(1);

    private View vRoot;

    private int mode = MODE_LISTING;

    private String pickFragmentTag;

    private onPickPersonFragment fragmentPick;

    private Button validation;

    private boolean singleChoice = true;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PersonSearchFragment()
    {
        loaderId = ProcessTasksLoader.ID;
        callback = this;
        emptyListMessageId = R.string.person_not_found;
        initLoader = false;
        checkSession = false;
    }

    public static PersonSearchFragment newInstance()
    {
        PersonSearchFragment bf = new PersonSearchFragment();
        return bf;
    }

    public static PersonSearchFragment newInstance(int mode, String fragmentTag, boolean singleChoice)
    {
        PersonSearchFragment bf = new PersonSearchFragment();
        Bundle b = new Bundle();
        b.putInt(PARAM_MODE, mode);
        b.putBoolean(PARAM_SINGLE_CHOICE, singleChoice);
        b.putString(PARAM_FRAGMENT_TAG, fragmentTag);
        bf.setArguments(b);
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

        if (getArguments() != null && getArguments().containsKey(PARAM_MODE))
        {
            mode = getArguments().getInt(PARAM_MODE);
            singleChoice = getArguments().getBoolean(PARAM_SINGLE_CHOICE);
            pickFragmentTag = getArguments().getString(PARAM_FRAGMENT_TAG);
            fragmentPick = ((onPickPersonFragment) getFragmentManager().findFragmentByTag(pickFragmentTag));
        }

        // Create View
        vRoot = inflater.inflate(R.layout.app_search_person, container, false);
        if (alfSession == null) { return vRoot; }

        // Init list
        init(vRoot, R.string.person_not_found);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Init form search
        final EditText searchForm = (EditText) vRoot.findViewById(R.id.search_query);
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
                        View vr  =  vRoot.findViewById(R.id.empty_focus);
                        vr.requestFocus();
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

        setListShown(true);

        if (getMode() == MODE_PICK)
        {
            vRoot.findViewById(R.id.pick_actions).setVisibility(View.VISIBLE);
            validation = (Button) vRoot.findViewById(R.id.validate);
            validation.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onSelect(selectedItems);
                    if (getDialog() != null)
                    {
                        getDialog().dismiss();
                    }
                    else
                    {
                        getFragmentManager().popBackStack();
                    }
                }
            });

            Button cancel = (Button) vRoot.findViewById(R.id.cancel);
            cancel.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (getDialog() != null)
                    {
                        getDialog().dismiss();
                    }
                    else
                    {
                        getFragmentManager().popBackStack();
                    }
                }
            });
        }
        else
        {
            vRoot.findViewById(R.id.pick_actions).setVisibility(View.GONE);
        }

        return vRoot;
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            if (fragmentPick instanceof TaskDetailsFragment)
            {
                getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_reassign);
                getDialog().setTitle(R.string.process_choose_assignee);
            }
        }
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        vRoot.setVisibility(View.VISIBLE);
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
    // LOADERS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult<Person>>> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        return new PersonSearchLoader(getActivity(), alfSession, args.getString(PARAM_KEYWORD));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Person>>> arg0,
            LoaderResult<PagingResult<Person>> results)
    {
        if (adapter == null)
        {
            if (mode == MODE_PICK)
            {
                selectedItems = fragmentPick.retrieveSelection();
            }

            adapter = new PersonAdapter(this, R.layout.sdk_list_row, new ArrayList<Person>(0), selectedItems);
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Person>>> arg0)
    {
        // TODO Auto-generated method stub

    }

    // //////////////////////////////////////////////////////////////////////
    // Public Method
    // //////////////////////////////////////////////////////////////////////
    protected void search(String keywords)
    {
        Bundle b = new Bundle();
        b.putString(PARAM_KEYWORD, keywords);
        reload(b, loaderId, this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Person item = (Person) l.getItemAtPosition(position);

        if (mode == MODE_PICK && !singleChoice)
        {
            l.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
        else if (mode == MODE_PICK && !singleChoice)
        {
            l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        else if (mode == MODE_LISTING && DisplayUtils.hasCentralPane(getActivity()))
        {
            l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.containsKey(item.getIdentifier());
            if (mode == MODE_PICK && !singleChoice)
            {
                selectedItems.remove(item.getIdentifier());
            }
            else
            {
                selectedItems.clear();
            }
        }
        l.setItemChecked(position, true);
        v.setSelected(true);

        selectedItems.put(item.getIdentifier(), item);

        if (hideDetails)
        {
            if (mode == MODE_PICK)
            {
                selectedItems.remove(item.getIdentifier());
                if (selectedItems.isEmpty())
                {
                    validation.setEnabled(false);
                }
            }
            else if (mode == MODE_LISTING && DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
                selectedItems.clear();
            }
        }
        else
        {
            if (mode == MODE_LISTING)
            {
                // Show properties
                ((MainActivity) getActivity()).addPersonProfileFragment(item.getIdentifier());
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
            else if (mode == MODE_PICK)
            {
                validation.setEnabled(true);
            }
        }
    }

    @Override
    public int getMode()
    {
        return mode;
    }

    public void onSelect(Map<String, Person> selectedItems)
    {
        if (fragmentPick != null)
        {
            fragmentPick.onSelect(selectedItems);
        }
    }
}

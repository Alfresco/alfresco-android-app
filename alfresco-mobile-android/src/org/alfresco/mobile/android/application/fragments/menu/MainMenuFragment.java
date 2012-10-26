/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.menu;

import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.fragment.AccountFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountLoginLoaderCallback;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsLoader;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainMenuFragment extends Fragment implements LoaderCallbacks<List<Account>>, OnItemSelectedListener
{
    private View view = null;
    private ArrayAdapter<String> adapter = null;
    private int loaderId;
    private LoaderCallbacks<?> callback;
    List<Account> results = null;
    
    public static final String TAG = "MainMenuFragment";

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        view = inflater.inflate(R.layout.app_main_menu, container, false);
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        
        getActivity().invalidateOptionsMenu();
        DisplayUtils.hideLeftTitlePane(getActivity());
        ((MainActivity) getActivity()).clearScreen();
        
        loaderId = AccountsLoader.ID;
        callback = this;
        
        getLoaderManager().restartLoader(loaderId, getArguments(), callback);
        getLoaderManager().getLoader(loaderId).forceLoad();
    }

    @Override
    public Loader<List<Account>> onCreateLoader(int id, Bundle args)
    {
        return new AccountsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Account>> arg0, List<Account> results)
    {
        this.results = results;
        
        if (adapter  == null)
        {
            //adapter = new AccountAdapter(getActivity(), R.layout.account_list_row, new ArrayList<Account>(0));
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.account_list_row, R.id.toptext);
        }
        else
        {
            adapter.clear();
        }
        
        int currentAccountIdx = 0;
        Account currentAccount = SessionUtils.getAccount(getActivity());
        for (int i = 0;  i < results.size();  i++)
        {
            String desc = results.get(i).getDescription();
            
            if (desc.length() > 0)
            {
                adapter.add(desc);
            }
            else
            {
                adapter.add(results.get(i).getUsername());
            }
                
            
            if (results.get(i).getId() == currentAccount.getId())
            {
                currentAccountIdx = i;
            }
        }
        
        adapter.add(getString(R.string.manage_accounts));
        
        Spinner s = (Spinner)view.findViewById(R.id.accounts_spinner);
        s.setAdapter(adapter);
        s.setSelection(currentAccountIdx);
        s.setOnItemSelectedListener(this);
    }

    @Override
    public void onLoaderReset(Loader<List<Account>> arg0)
    {
    }

    public void clearScreen()
    {
        /*
         * if (DisplayUtils.hasRightPane(this)) {
         * FragmentDisplayer.removeFragment(this,
         * DisplayUtils.getRightFragmentId(this));
         * DisplayUtils.hide(DisplayUtils.getRightPane(this)); }
         */
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            DisplayUtils.getCentralPane(getActivity()).setBackgroundResource(R.drawable.background_grey_alfresco);
        }
        if (DisplayUtils.hasLeftPane(getActivity()))
        {
            DisplayUtils.show(DisplayUtils.getLeftPane(getActivity()));
            // FragmentDisplayer.removeFragment(this,
            // DisplayUtils.getLeftFragmentId(this));
            // DisplayUtils.getLeftPane(this).setBackgroundResource(android.R.color.transparent);
        }
    }

    
    public void displayMainMenu()
    {
        Fragment f = new MainMenuFragment();
        FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getLeftFragmentId(getActivity()), MainMenuFragment.TAG, false);
        //hideSlideMenu();
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        if (position < results.size())
        {
            Account acc = results.get(position);
            AlfrescoSession sesh = SessionUtils.getsession(getActivity());
            
            if (sesh != null && (!acc.getUrl().equals(sesh.getBaseUrl()) || !acc.getUsername().equals(sesh.getPersonIdentifier())))
            {
                SessionUtils.setsession(getActivity(), null);
                
                AccountLoginLoaderCallback call = new AccountLoginLoaderCallback(getActivity(), acc);
                LoaderManager lm = getLoaderManager();
                lm.restartLoader(SessionLoader.ID, null, call);
                lm.getLoader(SessionLoader.ID).forceLoad();
                clearScreen();
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                displayMainMenu();
                
                SessionUtils.setAccount(getActivity(), acc);
            } 
        }
        else
        {
            //Manage accounts item selected...
            
            Fragment f = new AccountFragment();
            FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getLeftFragmentId(getActivity()), AccountFragment.TAG, true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }
}

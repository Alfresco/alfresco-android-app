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

import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.fragment.AccountAdapter;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class MainMenuFragment extends Fragment implements OnItemSelectedListener
{
    private View view = null;

    private AccountAdapter adapter = null;

    private List<Account> accounts;

    private int accountIndex = 0;

    public static final String TAG = "MainMenuFragment";

    public static final String SLIDING_TAG = "SlidingMenuFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null)
        {
            view = inflater.inflate(R.layout.app_main_menu, container, false);
        }
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

        DisplayUtils.hideLeftTitlePane(getActivity());
        getActivity().setTitle(R.string.app_name);

        getActivity().invalidateOptionsMenu();
        if (isAdded() && TAG.equals(getTag()))
        {
            ((MainActivity) getActivity()).clearScreen();
        }
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        accounts = ((MainActivity) getActivity()).getAccounts();
        refreshAccounts();
        
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    public void setAccounts(List<Account> accounts)
    {
        this.accounts = accounts;
        if (isAdded())
        {
            refreshAccounts();
        }
    }

    public void refreshAccounts()
    {
        if (accounts == null) { return; }

         if (adapter == null)
        {
            adapter = new AccountAdapter(getActivity(), R.layout.app_account_list_row, accounts);
        }
        else
        {
            adapter.refreshData(accounts);
        }

        Spinner s = (Spinner) view.findViewById(R.id.accounts_spinner);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(this);

        Account currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount == null)
        {
            currentAccount = AccountsPreferences.getDefaultAccount(getActivity(), accounts);
        }
        if (currentAccount == null)
        {
            accountIndex = 0;
        }
        else
        {
            for (int i = 0; i < accounts.size(); i++)
            {
                if (currentAccount != null && accounts.get(i).getId() == currentAccount.getId())
                {
                    accountIndex = i;
                    break;
                }
            }
        }

        s.setSelection(accountIndex);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        if (position < accounts.size())
        {
            Account selectedAccount = accounts.get(position);
            Account currentAccount = SessionUtils.getAccount(getActivity());
            if (currentAccount != null && selectedAccount != null && currentAccount.getId() != selectedAccount.getId())
            {
                hideSlidingMenu(true);
                accountIndex = position;
                ((MainActivity) getActivity()).loadAccount(selectedAccount);
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        else if (position == accounts.size())
        {
            // Manage accounts item selected...
            ((MainActivity) getActivity()).displayAccounts();
            hideSlidingMenu(false);
        }
        else if (position == accounts.size() + 1)
        {
            ((MainActivity) getActivity()).displayNetworks();
            hideSlidingMenu(false);
        }
    }

    private void hideSlidingMenu(boolean goHome)
    {
        if (SLIDING_TAG.equals(getTag()))
        {
            ((MainActivity) getActivity()).toggleSlideMenu();
            if (goHome)
            {
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ((MainActivity) getActivity()).clearScreen();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }
}

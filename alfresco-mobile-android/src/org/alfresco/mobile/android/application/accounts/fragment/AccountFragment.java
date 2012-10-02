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
package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class AccountFragment extends BaseListFragment implements LoaderCallbacks<List<Account>>
{

    public static final String TAG = "AccountFragment";

    public AccountFragment()
    {
        loaderId = AccountsLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_accounts;
        checkSession = false;
    }

    @Override
    public void onStart()
    {
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Override
    public Loader<List<Account>> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        return new AccountsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Account>> arg0, List<Account> results)
    {
        if (adapter == null)
            adapter = new AccountAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Account>(0));

        PagingResult<Account> pagingResultFiles = new PagingResultImpl<Account>(results, false, results.size());
        displayPagingData(pagingResultFiles, loaderId, callback);
    }

    @Override
    public void onLoaderReset(Loader<List<Account>> arg0)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ((MainActivity) getActivity()).addAccountDetails(((Account) l.getItemAtPosition(position)).getId());
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////

    public void refresh()
    {
        reload(bundle, loaderId, callback);
        ((MainActivity) getActivity()).refreshAccount();
    }

    public void add()
    {
        /*FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(CreateAccountDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        CreateAccountDialogFragment newFragment = new CreateAccountDialogFragment();
        newFragment.show(ft, CreateAccountDialogFragment.TAG);*/
        WizardSelectAccountFragment newFragment = new WizardSelectAccountFragment();
        FragmentDisplayer.replaceFragment(getActivity(), newFragment, DisplayUtils.getMainPaneId(getActivity()),
                WizardSelectAccountFragment.TAG, true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_ADD, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_ADD,
                R.string.action_add_account);
        mi.setIcon(R.drawable.ic_account_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

}

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
package org.alfresco.mobile.android.application.fragments.account;

import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.DeleteAccountEvent;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.ui.fragments.SelectableGridFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public class AccountsFragment extends SelectableGridFragment<AlfrescoAccount>
{
    public static final String TAG = AccountsFragment.class.getName();

    private List<AlfrescoAccount> accountListing;

    // private List<Long> selectedAccounts = new ArrayList<Long>(1);

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public AccountsFragment()
    {
        emptyListMessageId = R.string.empty_accounts;
        titleId = R.string.accounts_manage;
        requiredSession = false;
        checkSession = false;
        retrieveDataOnCreation = false;
        displayAsList = true;
        setHasOptionsMenu(true);
    }

    protected static AccountsFragment newInstanceByTemplate(Bundle b)
    {
        AccountsFragment cbf = new AccountsFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        retrieveAccountList();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new AccountsAdapter(getActivity(), accountListing, R.layout.sdk_grid_row, selectedItems);
    }

    @Override
    public void onStart()
    {
        UIUtils.displayTitle(getActivity(), R.string.accounts_manage);
        super.onStart();
        setListShown(true);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // ITEM SELECTION
    // ////////////////////////////////////////////////////////////
    protected void onItemUnselected(AlfrescoAccount selectedAccount)
    {
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(DisplayUtils.getCentralFragmentId(getActivity()));
        }
    }

    protected void onItemSelected(AlfrescoAccount selectedAccount)
    {
        displayAccountDetails(selectedAccount.getId());
    }

    protected boolean equalsItems(AlfrescoAccount o1, AlfrescoAccount o2)
    {
        if (o1 == null || o2 == null) return false;
        return o2.getId() == o1.getId();
    }

    // /////////////////////////////////////////////////////////////
    // REFRESH
    // ////////////////////////////////////////////////////////////
    @Override
    public void refresh()
    {
        // Event refresh
        onPrepareRefresh();
        adapter = onAdapterCreation();
        gv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        refreshHelper.setRefreshComplete();
    }

    // /////////////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////////////
    public void add()
    {
        AccountTypesFragment.with(getActivity()).display();
    }

    public void unselect()
    {
        selectedItem.clear();
    }

    public void select(AlfrescoAccount account)
    {
        selectedItem.clear();
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItem.add(account);
            refreshListView();
            displayAccountDetails(account.getId());
        }
    }

    private void displayAccountDetails(long accountId)
    {
        if (getActivity() instanceof MainActivity)
        {
            AccountDetailsFragment.with(getActivity()).accountId(accountId).display();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(Context context, Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.menu_account_add, Menu.FIRST, R.string.action_add_account);
        mi.setIcon(R.drawable.ic_account_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_account_add:
                add();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        retrieveAccountList();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            for (AlfrescoAccount acc : accountListing)
            {
                if (event.data.getId() == acc.getId())
                {
                    selectedItems.add(acc);
                    break;
                }
            }
        }
        refresh();
    }

    @Subscribe
    public void onAccountDeleted(DeleteAccountEvent event)
    {
        retrieveAccountList();
        for (AlfrescoAccount acc : accountListing)
        {
            if (event.account.getId() == acc.getId())
            {
                accountListing.remove(acc);
                break;
            }
        }
        refresh();
    }

    private void retrieveAccountList()
    {
        accountListing = AlfrescoAccountManager.retrieveAccounts(getActivity());
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
            templateArguments = new String[] {};
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}

package org.alfresco.mobile.android.application.accounts.fragment;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.BaseActivity;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.AccountProvider;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class AccountsFragment extends BaseCursorListFragment
{
    public static final String TAG = AccountsFragment.class.getName();

    protected List<Long> selectedAccounts = new ArrayList<Long>(1);

    private AccountsReceiver receiver;

    public AccountsFragment()
    {
        emptyListMessageId = R.string.empty_accounts;
        title = R.string.accounts_manage;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        adapter = new AccountCursorAdapter(getActivity(), null, R.layout.sdk_list_row, selectedAccounts);
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        if (receiver == null)
        {
            receiver = new AccountsReceiver();
            IntentFilter filters = new IntentFilter(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filters);
        }

        DisplayUtils.hideLeftTitlePane(getActivity());
        getActivity().setTitle(R.string.accounts_manage);
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Override
    public void onPause()
    {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    // /////////////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        long accountId = cursor.getLong(AccountSchema.COLUMN_ID_ID);

        Boolean hideDetails = false;
        if (!selectedAccounts.isEmpty())
        {
            hideDetails = selectedAccounts.get(0).equals(accountId);
            selectedAccounts.clear();
        }
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        l.setSelection(position);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedAccounts.add(accountId);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedAccounts.clear();
        }
        else
        {
            displayAccountDetails(accountId);
        }
    }

    public void add()
    {
        AccountTypesFragment newFragment = new AccountTypesFragment();
        FragmentDisplayer.replaceFragment(getActivity(), newFragment, DisplayUtils.getMainPaneId(getActivity()),
                AccountTypesFragment.TAG, true);
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
    }

    public void unselect()
    {
        selectedAccounts.clear();
    }

    private void displayAccountDetails(long accountId)
    {
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).addAccountDetails(accountId);
        }
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_ACCOUNT_ADD, Menu.FIRST + MenuActionItem.MENU_ACCOUNT_ADD,
                R.string.action_add_account);
        mi.setIcon(R.drawable.ic_account_add);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        setListShown(false);
        Uri baseUri = AccountProvider.CONTENT_URI;
        return new CursorLoader(getActivity(), baseUri, AccountSchema.COLUMN_ALL, null, null, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class AccountsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                selectedAccounts.clear();
                getActivity().getFragmentManager().popBackStack(AccountTypesFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if (intent.getExtras() != null && intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    long accountId = intent.getLongExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, -1);
                    
                    ((BaseActivity)getActivity()).setCurrentAccount(accountId);
                    
                    if (DisplayUtils.hasCentralPane(getActivity()))
                    {
                        selectedAccounts.add(accountId);
                        refreshListView();
                    }
                    displayAccountDetails(accountId);
                }
            }
        }
    }

}

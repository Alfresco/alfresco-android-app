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
package org.alfresco.mobile.android.application.fragments.menu;

import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountProvider;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.accounts.fragment.AccountCursorAdapter;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class MainMenuFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemSelectedListener
{
    private View rootView = null;

    private AccountCursorAdapter cursorAdapter;

    private Spinner spinnerAccount;

    private Cursor accountCursor;

    private int accountIndex;

    public static final String TAG = "MainMenuFragment";

    public static final String SLIDING_TAG = "SlidingMenuFragment";

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.app_main_menu, container, false);

        spinnerAccount = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (accountCursor != null)
        {
            accountCursor.close();
            accountCursor = null;
        }
        cursorAdapter = new AccountCursorAdapter(getActivity(), null, R.layout.app_account_list_row, null);
        spinnerAccount.setAdapter(cursorAdapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        DisplayUtils.hideLeftTitlePane(getActivity());
        getActivity().setTitle(R.string.app_name);

        getActivity().invalidateOptionsMenu();
        if (isAdded() && TAG.equals(getTag())
                && getActivity().getFragmentManager().findFragmentByTag(GeneralPreferences.TAG) == null
                && getActivity().getFragmentManager().findFragmentByTag(AboutFragment.TAG) == null)
        {
            ((MainActivity) getActivity()).clearScreen();
        }
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (!isVisible() && TAG.equals(getTag()))
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DPUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshData()
    {
        refresh();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DROPDOWN EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
        int accountId = cursor.getInt(AccountSchema.COLUMN_ID_ID);

        switch (accountId)
        {
            case AccountCursorAdapter.NETWORK_ITEM:
                ((MainActivity) getActivity()).displayNetworks();
                hideSlidingMenu(false);
                break;
            case AccountCursorAdapter.MANAGE_ITEM:
                ((MainActivity) getActivity()).displayAccounts();
                hideSlidingMenu(false);
                break;

            default:
                Account currentAccount = SessionUtils.getAccount(getActivity());
                if (currentAccount != null && cursor.getCount() > 1
                        && currentAccount.getId() != cursor.getLong(AccountSchema.COLUMN_ID_ID))
                {
                    hideSlidingMenu(true);

                    // Request session loading for the selected account.
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                            new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT).putExtra(
                                    IntentIntegrator.EXTRA_ACCOUNT_ID, cursor.getLong(AccountSchema.COLUMN_ID_ID)));

                    // Update dropdown menu (eventual new items to display)
                    cursorAdapter.swapCursor(AccountCursorAdapter.createMergeCursor(getActivity(), accountCursor));
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CALLBACKS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = AccountProvider.CONTENT_URI;
        return new CursorLoader(getActivity(), baseUri, AccountSchema.COLUMN_ALL, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        accountCursor = cursor;
        cursorAdapter.changeCursor(AccountCursorAdapter.createMergeCursor(getActivity(), accountCursor));
        if (cursor.getCount() > 0)
        {
            refresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        cursorAdapter.changeCursor(null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void refresh()
    {
        if (accountCursor == null) { return; }
        if (accountCursor.isClosed()) { return; }

        Account currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount == null)
        {
            currentAccount = AccountsPreferences.getDefaultAccount(getActivity());
        }

        if (currentAccount == null) { return; }

        for (int i = 0; i < accountCursor.getCount(); i++)
        {
            accountCursor.moveToPosition(i);
            if (accountCursor.getLong(AccountSchema.COLUMN_ID_ID) == currentAccount.getId())
            {
                accountIndex = accountCursor.getPosition();
                break;
            }
        }

        Log.d(TAG, getTag() + " : " + accountIndex);
        spinnerAccount.setSelection(accountIndex);
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

}

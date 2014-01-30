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
package org.alfresco.mobile.android.application.fragments.menu;

import java.util.Map;

import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.accounts.fragment.AccountCursorAdapter;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.configuration.ConfigurationContext;
import org.alfresco.mobile.android.application.configuration.ConfigurationManager;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

public class MainMenuFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemSelectedListener
{
    private AccountCursorAdapter cursorAdapter;

    private Spinner spinnerAccount;

    private Cursor accountCursor;

    private int accountIndex;

    private MainMenuReceiver receiver;

    private Button menuFavorites;

    private Button menuSlidingFavorites;

    private ConfigurationManager configurationManager;

    private View rootView;

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

        menuFavorites = (Button) rootView.findViewById(R.id.menu_favorites);

        if (SLIDING_TAG.equals(getTag()))
        {
            menuSlidingFavorites = (Button) rootView.findViewById(R.id.menu_favorites);
        }

        configurationManager = ApplicationManager.getInstance(getActivity()).getConfigurationManager();
        if (configurationManager != null
                && configurationManager.getConfigurationState() == ConfigurationManager.STATE_HAS_CONFIGURATION)
        {
            configure(configurationManager.getConfig(SessionUtils.getAccount(getActivity())));
        }
        else
        {
            display();
        }

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

        if (isAdded() && TAG.equals(getTag())
                && getActivity().getFragmentManager().findFragmentByTag(GeneralPreferences.TAG) == null
                && getActivity().getFragmentManager().findFragmentByTag(AboutFragment.TAG) == null)
        {
            ((MainActivity) getActivity()).clearScreen();
        }

        UIUtils.displayTitle(getActivity(), R.string.app_name);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_SYNCHRO_COMPLETED);
        intentFilter.addAction(IntentIntegrator.ACTION_CONFIGURATION_MENU);
        receiver = new MainMenuReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);

        displayFavoriteStatut();

        if (configurationManager != null
                && configurationManager.getConfigurationState() == ConfigurationManager.STATE_HAS_CONFIGURATION)
        {
            configure(configurationManager.getConfig(SessionUtils.getAccount(getActivity())));
        }
        else
        {
            display();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (!isVisible() && TAG.equals(getTag()))
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DPUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshData()
    {
        refresh();
        displayFavoriteStatut();
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
        return new CursorLoader(getActivity(), AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL, null, null, null);
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
    private void configure(ConfigurationContext configurationContext)
    {
        if (configurationContext != null && configurationContext.getJson() != null
                && configurationContext.getJson().containsKey(ConfigurationManager.CATEGORY_ROOTMENU))
        {
            Map<String, Object> menuConfig = (Map<String, Object>) configurationContext.getJson().get(
                    ConfigurationManager.CATEGORY_ROOTMENU);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_ACTIVITIES, R.id.menu_browse_activities);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_REPOSITORY, R.id.menu_browse_root);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_SITES, R.id.menu_browse_my_sites);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_TASKS, R.id.menu_workflow);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_FAVORITES, R.id.menu_favorites);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_SEARCH, R.id.menu_search);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_LOCAL_FILES, R.id.menu_downloads);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_NOTIFICATIONS, R.id.menu_notifications);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_SHARED, R.id.menu_browse_shared, true);
            hideOrDisplay(menuConfig, ConfigurationManager.MENU_MYFILES, R.id.menu_browse_userhome, true);
        }
        else
        {
            display();
        }
    }

    private void display()
    {
        Account acc = SessionUtils.getAccount(getActivity());

        rootView.findViewById(R.id.menu_browse_activities).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_browse_root).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_browse_my_sites).setVisibility(View.VISIBLE);
        if (acc != null && acc.getTypeId() == Account.TYPE_ALFRESCO_CLOUD)
        {
            rootView.findViewById(R.id.menu_workflow).setVisibility(View.GONE);
        }
        else
        {
            rootView.findViewById(R.id.menu_workflow).setVisibility(View.VISIBLE);
        }
        rootView.findViewById(R.id.menu_favorites).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_search).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_downloads).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_notifications).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.menu_browse_shared).setVisibility(View.GONE);
        rootView.findViewById(R.id.menu_browse_userhome).setVisibility(View.GONE);
    }

    private void hideOrDisplay(Map<String, Object> menuConfig, String configKey, int viewId)
    {
        hideOrDisplay(menuConfig, configKey, viewId, false);
    }

    private void hideOrDisplay(Map<String, Object> menuConfig, String configKey, int viewId, boolean forceHide)
    {
        if (menuConfig.containsKey(configKey))
        {
            Map<String, Object> itemVisibility = (Map<String, Object>) menuConfig.get(configKey);
            if (itemVisibility.containsKey(ConfigurationManager.PROP_VISIBILE)
                    && JSONConverter.getBoolean(itemVisibility, ConfigurationManager.PROP_VISIBILE))
            {
                rootView.findViewById(viewId).setVisibility(View.VISIBLE);
            }
            else if (!itemVisibility.containsKey(ConfigurationManager.PROP_VISIBILE))
            {
                if (forceHide)
                {
                    rootView.findViewById(viewId).setVisibility(View.GONE);
                }
                else
                {
                    rootView.findViewById(viewId).setVisibility(View.VISIBLE);
                }
            }
            else
            {
                rootView.findViewById(viewId).setVisibility(View.GONE);
            }
        }
        else
        {
            if (forceHide)
            {
                rootView.findViewById(viewId).setVisibility(View.GONE);
            }
            else
            {
                rootView.findViewById(viewId).setVisibility(View.VISIBLE);
            }
        }
    }

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
            }
        }
    }

    public void hideWorkflowMenu(Account currentAccount)
    {
        if (rootView == null || rootView.findViewById(R.id.menu_workflow) == null) { return; }
        if (currentAccount.getTypeId() == Account.TYPE_ALFRESCO_CLOUD
                || currentAccount.getTypeId() == Account.TYPE_ALFRESCO_TEST_OAUTH)
        {
            rootView.findViewById(R.id.menu_workflow).setVisibility(View.GONE);
        }
        else
        {
            rootView.findViewById(R.id.menu_workflow).setVisibility(View.VISIBLE);
        }
    }

    public void displayFavoriteStatut()
    {
        Cursor statutCursor = null;
        Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_favorite);
        Drawable statut = null;

        try
        {
            Account acc = SessionUtils.getAccount(getActivity());
            Boolean hasSynchroActive = GeneralPreferences.hasActivateSync(getActivity(), acc);

            if (hasSynchroActive && acc != null)
            {
                statutCursor = getActivity().getContentResolver().query(
                        SynchroProvider.CONTENT_URI,
                        SynchroSchema.COLUMN_ALL,
                        SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_STATUS + " == "
                                + SyncOperation.STATUS_REQUEST_USER, null, null);
                if (statutCursor.getCount() > 0)
                {
                    statut = getActivity().getResources().getDrawable(R.drawable.ic_warning_light);
                }
                statutCursor.close();

                if (menuSlidingFavorites != null)
                {
                    menuSlidingFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
                }
                menuFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            if (statutCursor != null)
            {
                statutCursor.close();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class MainMenuReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());
            if (intent.getAction() == null) { return; }

            if (IntentIntegrator.ACTION_SYNCHRO_COMPLETED.equals(intent.getAction()))
            {
                displayFavoriteStatut();
            }
            else if (IntentIntegrator.ACTION_CONFIGURATION_MENU.equals(intent.getAction()))
            {
                configurationManager = ApplicationManager.getInstance(getActivity()).getConfigurationManager();
                if (configurationManager != null
                        && configurationManager.getConfigurationState() == ConfigurationManager.STATE_HAS_CONFIGURATION)
                {
                    configure(configurationManager.getConfig(AccountManager.retrieveAccount(context, intent.getExtras()
                            .getLong(IntentIntegrator.EXTRA_ACCOUNT_ID))));
                }
                else
                {
                    display();
                }
            }
        }
    }
}

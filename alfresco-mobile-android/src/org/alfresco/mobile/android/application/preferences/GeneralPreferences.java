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

package org.alfresco.mobile.android.application.preferences;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.favorites.FavoriteAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoriteAlertDialogFragment.OnFavoriteChangeListener;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Manage global application preferences.
 * 
 * @author Jean Marie Pascal
 */
public class GeneralPreferences extends PreferenceFragment
{

    public static final String TAG = "GeneralPreferencesFragment";

    public static final String HAS_ACCESSED_PAID_SERVICES = "HasAccessedPaidServices";

    public static final String REQUIRES_ENCRYPT = "RequiresEncrypt";

    public static final String ENCRYPTION_USER_INTERACTION = "EncryptionUserInteraction";

    public static final String PRIVATE_FOLDERS = "privatefolders";

    private static final String PRIVATE_FOLDERS_BUTTON = "privatefoldersbutton";

    private static final String SYNCHRO_PREFIX = "SynchroEnable-";

    private static final String SYNCHRO_WIFI_PREFIX = "SynchroWifiEnable-";

    private static final String SYNCHRO_DISPLAY_PREFIX = "SynchroDisplayEnable-";

    private Account account;

    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference privateFoldersPref = findPreference(PRIVATE_FOLDERS_BUTTON);

        // DATA PROTECTION
        if (!sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false))
        {
            privateFoldersPref.setSelectable(false);
            privateFoldersPref.setEnabled(false);
            privateFoldersPref.setSummary(R.string.data_protection_unavailable);
            sharedPref.edit().putBoolean(PRIVATE_FOLDERS, false).commit();
        }
        else
        {
            privateFoldersPref.setSelectable(true);
            privateFoldersPref.setEnabled(true);
            privateFoldersPref.setSummary(sharedPref.getBoolean(PRIVATE_FOLDERS, false) ? R.string.data_protection_on
                    : R.string.data_protection_off);
        }

        privateFoldersPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final File folder = StorageManager.getPrivateFolder(getActivity(), "", null);
                if (folder != null)
                {
                    DataProtectionUserDialogFragment.newInstance(false).show(getActivity().getFragmentManager(), DataProtectionUserDialogFragment.TAG);
                }
                else
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.sdinaccessible));
                }

                return false;
            }
        });

        // PASSCODE
        Boolean passcodeEnable = sharedPref.getBoolean(PasscodePreferences.KEY_PASSCODE_ENABLE, false);
        Preference pref = findPreference(getString(R.string.passcode_title));

        boolean isActivate = sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false);
        pref.setSelectable(isActivate);
        pref.setEnabled(isActivate);

        int summaryId = R.string.passcode_disable;
        if (passcodeEnable)
        {
            summaryId = R.string.passcode_enable;
        }
        pref.setSummary(summaryId);

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Fragment f = new PasscodePreferences();
                FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getMainPaneId(getActivity()),
                        PasscodePreferences.TAG, true);
                return false;
            }
        });

        // FAVORITE SYNC
        final CheckBoxPreference cpref = (CheckBoxPreference) findPreference(getString(R.string.favorite_sync));
        final CheckBoxPreference wifiPref = (CheckBoxPreference) findPreference(getString(R.string.favorite_sync_wifi));
        account = SessionUtils.getAccount(getActivity());

        if (account == null)
        {
            cpref.setSelectable(false);
            wifiPref.setSelectable(false);
            return;
        }

        Boolean syncEnable = sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false);
        cpref.setChecked(syncEnable);

        Boolean syncWifiEnable = sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), true);
        wifiPref.setChecked(syncWifiEnable);
        if (syncWifiEnable)
        {
            wifiPref.setSummary(R.string.settings_favorite_sync_data_wifi);
        }
        else
        {
            wifiPref.setSummary(R.string.settings_favorite_sync_data_all);
        }

        cpref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                boolean isSync = false;
                if (preference instanceof CheckBoxPreference)
                {
                    isSync = ((CheckBoxPreference) preference).isChecked();
                }

                if (isSync)
                {
                    sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), isSync).commit();
                    if (SynchroManager.getInstance(getActivity()).canSync(account))
                    {
                        SynchroManager.getInstance(getActivity()).sync(account);
                    }
                }
                else
                {
                    OnFavoriteChangeListener favListener = new FavoriteAlertDialogFragment.OnFavoriteChangeListener()
                    {
                        @Override
                        public void onPositive()
                        {
                            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), false).commit();
                            cpref.setChecked(false);
                            SynchroManager.getInstance(getActivity()).unsync(account);
                        }

                        @Override
                        public void onNegative()
                        {
                            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), true).commit();
                            cpref.setChecked(true);
                        }
                    };
                    FavoriteAlertDialogFragment.newInstance(favListener).show(getActivity().getFragmentManager(),
                            FavoriteAlertDialogFragment.TAG);
                    return true;
                }

                return false;
            }
        });

        wifiPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                boolean isWifiOnly = false;
                if (preference instanceof CheckBoxPreference)
                {
                    isWifiOnly = ((CheckBoxPreference) preference).isChecked();
                }
                sharedPref.edit().putBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), isWifiOnly).commit();
                
                if (isWifiOnly)
                {
                    wifiPref.setSummary(R.string.settings_favorite_sync_data_wifi);
                }
                else
                {
                    wifiPref.setSummary(R.string.settings_favorite_sync_data_all);
                }
                
                return false;
            }
        });

        getActivity().invalidateOptionsMenu();

    }

    public static boolean hasWifiOnlySync(Context context, Account account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), false);
        }
        return false;
    }

    public static boolean hasActivateSync(Context context, Account account)
    {
        if (account != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false);
        }
        return false;
    }

    public static void setActivateSync(Activity activity, boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), isActive).commit();
        }
    }

    public static void setDisplayActivateSync(Activity activity, boolean isActive)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            sharedPref.edit().putBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), isActive).commit();
        }
    }

    public static boolean hasDisplayedActivateSync(Activity activity)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            return sharedPref.getBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), false);
        }
        return false;
    }
}

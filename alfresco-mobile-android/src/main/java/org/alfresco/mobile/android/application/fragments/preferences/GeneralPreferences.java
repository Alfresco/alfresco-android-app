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
package org.alfresco.mobile.android.application.fragments.preferences;

import java.io.File;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.config.MenuConfigFragment;
import org.alfresco.mobile.android.application.fragments.sync.DisableSyncDialogFragment;
import org.alfresco.mobile.android.application.fragments.sync.DisableSyncDialogFragment.OnFavoriteChangeListener;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Manage global application preferences.
 * 
 * @author Jean Marie Pascal
 */
public class GeneralPreferences extends PreferenceFragment
{

    public static final String TAG = GeneralPreferences.class.getName();

    public static final String HAS_ACCESSED_PAID_SERVICES = "HasAccessedPaidServices";

    private static final String PRIVATE_FOLDERS_BUTTON = "privatefoldersbutton";

    private AlfrescoAccount account;

    private MobileIronManager mdmManager;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public GeneralPreferences()
    {
    }

    protected static GeneralPreferences newInstanceByTemplate(Bundle b)
    {
        GeneralPreferences cbf = new GeneralPreferences();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        mdmManager = MobileIronManager.getInstance(getActivity());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Title
        UIUtils.displayTitle(getActivity(), R.string.settings);

        // Preferences
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference privateFoldersPref = findPreference(PRIVATE_FOLDERS_BUTTON);

        // DATA PROTECTION
        if (!sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false))
        {
            privateFoldersPref.setSelectable(false);
            privateFoldersPref.setEnabled(false);
            privateFoldersPref.setSummary(R.string.data_protection_unavailable);
            DataProtectionManager.getInstance(getActivity()).setDataProtectionEnable(false);
        }
        else
        {
            privateFoldersPref.setSelectable(true);
            privateFoldersPref.setEnabled(true);
            privateFoldersPref
                    .setSummary(DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable() ? R.string.data_protection_on
                            : R.string.data_protection_off);
        }

        privateFoldersPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final File folder = AlfrescoStorageManager.getInstance(getActivity()).getPrivateFolder("", null);
                if (folder != null)
                {
                    DataProtectionUserDialogFragment.newInstance(false).show(getActivity().getFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);
                }
                else
                {
                    AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                            getString(R.string.sdinaccessible));
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
                PasscodePreferences.with(getActivity()).display();
                return false;
            }
        });

        // In case of MDM we disable all enterprise feature
        if (mdmManager != null)
        {
            pref.setEnabled(false);
            pref.setSummary(R.string.mdm_managed);
            privateFoldersPref.setEnabled(false);
            privateFoldersPref.setSummary(R.string.mdm_managed);
        }

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

        Boolean syncEnable = FavoritesSyncManager.getInstance(getActivity()).hasActivateSync(account);
        cpref.setChecked(syncEnable);
        cpref.setTitle(getString(R.string.settings_favorite_sync));
        cpref.setSummary(String.format(getString(R.string.settings_favorite_sync_summary), account.getTitle()));

        Boolean syncWifiEnable = FavoritesSyncManager.getInstance(getActivity()).hasWifiOnlySync(account);

        if (wifiPref != null)
        {
            wifiPref.setChecked(!syncWifiEnable);
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
                    FavoritesSyncManager.getInstance(getActivity()).setActivateSync(account, isSync);
                    if (FavoritesSyncManager.getInstance(getActivity()).canSync(account))
                    {
                        FavoritesSyncManager.getInstance(getActivity()).sync(account);
                    }
                }
                else
                {
                    OnFavoriteChangeListener favListener = new OnFavoriteChangeListener()
                    {
                        @Override
                        public void onPositive()
                        {
                            FavoritesSyncManager.getInstance(getActivity()).setActivateSync(account, false);
                            cpref.setChecked(false);
                            FavoritesSyncManager.getInstance(getActivity()).unsync(account);
                        }

                        @Override
                        public void onNegative()
                        {
                            FavoritesSyncManager.getInstance(getActivity()).setActivateSync(account, true);
                            cpref.setChecked(true);
                        }
                    };
                    DisableSyncDialogFragment.newInstance(favListener).show(getActivity().getFragmentManager(),
                            DisableSyncDialogFragment.TAG);
                    return true;
                }

                return false;
            }
        });

        // Check if 3G Present
        if (!ConnectivityUtils.hasMobileConnectivity(getActivity()) && wifiPref != null)
        {
            PreferenceCategory mCategory = (PreferenceCategory) findPreference(getString(R.string.favorite_sync_group));
            mCategory.removePreference(wifiPref);
        }

        if (wifiPref != null)
        {
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
                    FavoritesSyncManager.getInstance(getActivity()).setWifiOnlySync(account, isWifiOnly);
                    return false;
                }
            });
        }

        // Profiles & custom menu Configuration
        Preference manageCustomMenuPref = findPreference(getString(R.string.custom_menu_manage));

        if (ConfigManager.getInstance(getActivity()).hasRemoteConfig(account.getId())
                && ConfigManager.getInstance(getActivity()).getRemoteConfig(account.getId()).hasViewConfig())
        {
            manageCustomMenuPref.setEnabled(false);
            manageCustomMenuPref.setSummary(R.string.settings_custom_menu_disable);
        }
        else
        {
            manageCustomMenuPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    MenuConfigFragment.with(getActivity()).display();
                    return false;
                }
            });
        }

        getActivity().invalidateOptionsMenu();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshDataProtection()
    {
        Preference privateFoldersPref = findPreference(PRIVATE_FOLDERS_BUTTON);
        privateFoldersPref
                .setSummary(DataProtectionManager.getInstance(getActivity()).hasDataProtectionEnable() ? R.string.data_protection_on
                        : R.string.data_protection_off);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
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
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }

}

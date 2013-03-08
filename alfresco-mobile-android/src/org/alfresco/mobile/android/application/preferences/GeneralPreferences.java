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
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }
        View v = super.onCreateView(inflater, container, savedInstanceState);

        v.setBackgroundColor(Color.WHITE);

        return v;
    }

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference privateFoldersPref = findPreference(PRIVATE_FOLDERS_BUTTON);

        // DATA PROTECTION
        if (/* isDeviceRooted() || */sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false) == false)
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
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final boolean checked = prefs.getBoolean(PRIVATE_FOLDERS, false);

                final File folder = StorageManager.getPrivateFolder(getActivity(), "", "", "");
                if (folder != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.data_protection));
                    builder.setMessage(getString(checked ? R.string.unprotect_question : R.string.protect_question));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            dialog.dismiss();

                            if (checked)
                            {
                                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                EncryptionDialogFragment fragment = EncryptionDialogFragment.decryptAll(folder);
                                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                                fragmentTransaction.commit();
                            }
                            else
                            {
                                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                EncryptionDialogFragment fragment = EncryptionDialogFragment.encryptAll(folder
                                        .getPath());
                                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                                fragmentTransaction.commit();
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int item)
                        {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
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
    }

    private static final String TEST_KEYS = "test-keys";

    private static final String PATH_SUPERUSER_APK = "/system/app/Superuser.apk";

    public static boolean isDeviceRooted()
    {

        // get from build info
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains(TEST_KEYS)) { return true; }

        // check if /system/app/Superuser.apk is present
        try
        {
            File file = new File(PATH_SUPERUSER_APK);
            if (file.exists()) { return true; }
        }
        catch (Throwable e1)
        {
            // ignore
        }

        return false;
    }

}

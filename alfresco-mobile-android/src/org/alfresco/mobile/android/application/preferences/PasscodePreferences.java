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

import java.text.MessageFormat;
import java.util.Date;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.security.PassCodeDialogFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Manage application preferences associated to Passcode feature.
 * 
 * @author Jean Marie Pascal
 */
public class PasscodePreferences extends PreferenceFragment
{

    public static final String KEY_PASSCODE_ENABLE = "PasscodeEnabled";

    public static final String KEY_PASSCODE_VALUE = "PasscodeValue";

    public static final String KEY_PASSCODE_TIMEOUT = "PasscodeTimeOut";

    public static final String KEY_PASSCODE_ACTIVATED_AT = "PasscodeActivatedAt";

    public static final String KEY_PASSCODE_ATTEMPT = "PasscodeAttempt";

    public static final String KEY_PASSCODE_MAX_ATTEMPT = "PasscodeMaxAttempt";

    public static final String TAG = "PasscodePreferencesFragment";

    private static final String DEFAULT_TIMEOUT = "300000";

    private static final int ONE_MINUTE = 60000;

    private static final long DEFAULT_ACTIVATION_TIME = -1;

    private boolean valueHasChanged;

    private boolean passcodeEnable;

    private boolean editionEnable;

    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        v.setBackgroundColor(Color.WHITE);

        return v;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.passcode_preferences);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        passcodeEnable = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
        boolean maxAttemptActivated = (sharedPref.getInt(KEY_PASSCODE_MAX_ATTEMPT, 0) == 0) ? false : true;
        long timeout = Long.parseLong(sharedPref.getString(KEY_PASSCODE_TIMEOUT, "300000"));

        // ENABLE PASSCODE
        Preference pref = findPreference(getString(R.string.passcode_enable_key));
        if (pref instanceof CheckBoxPreference)
        {
            ((CheckBoxPreference) pref).setChecked(passcodeEnable);
        }
        else if (pref instanceof SwitchPreference)
        {
            pref.setSelectable(false);
            ((SwitchPreference) pref).setChecked(passcodeEnable);
        }
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if (valueHasChanged)
                {
                    valueHasChanged = false;
                    return true;
                }
                PassCodeDialogFragment f = null;
                if (passcodeEnable)
                {
                    f = PassCodeDialogFragment.disable();
                }
                else
                {
                    f = PassCodeDialogFragment.enable();
                }
                f.show(getActivity().getFragmentManager(), PassCodeDialogFragment.TAG);
                return false;
            }
        });

        // CHANGE PASSCODE
        pref = findPreference(getString(R.string.passcode_change));
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                editionEnable = true;
                PassCodeDialogFragment f = PassCodeDialogFragment.modify();
                f.show(getActivity().getFragmentManager(), PassCodeDialogFragment.TAG);
                return false;
            }
        });

        // ERASE DATA
        pref = findPreference(getString(R.string.passcode_erase_data));
        ((CheckBoxPreference) pref).setChecked(maxAttemptActivated);
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if ((Boolean) newValue)
                {
                    sharedPref.edit().putInt(KEY_PASSCODE_MAX_ATTEMPT, 10).commit();
                }
                else
                {
                    sharedPref.edit().remove(KEY_PASSCODE_MAX_ATTEMPT).commit();
                }
                return true;
            }
        });

        // TIMEOUT
        pref = findPreference(getString(R.string.passcode_timeout));
        int minutes = Math.round(timeout / ONE_MINUTE);
        pref.setSummary(String.format(getString(R.string.passcode_timeout_summary), minutes + ""));
        pref.setSummary(String.format(MessageFormat.format(getString(R.string.passcode_timeout_summary), minutes),
                minutes));
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                sharedPref.edit().putString(KEY_PASSCODE_TIMEOUT, (String) newValue).commit();
                int minutes = Math.round(Long.parseLong((String) newValue) / ONE_MINUTE);
                preference.setSummary(String.format(
                        MessageFormat.format(getString(R.string.passcode_timeout_summary), minutes), minutes));
                return true;
            }
        });
    }

    /**
     * This method is used to refresh passcode preferences screen.
     */
    public void refresh()
    {
        if (editionEnable)
        {
            editionEnable = false;
        }
        else
        {
            valueHasChanged = true;
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        passcodeEnable = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);

        Preference pref = (Preference) findPreference(getString(R.string.passcode_enable_key));
        // Depending on Android version we use different component.
        // Checkbox for A < 14 and Switch for A > 14
        if (pref instanceof CheckBoxPreference)
        {
            ((CheckBoxPreference) pref).setChecked(passcodeEnable);
        }
        else if (pref instanceof SwitchPreference)
        {
            ((SwitchPreference) pref).setChecked(passcodeEnable);
        }
    }

    /**
     * Utility method to flag when the application has been activated for the
     * last time. This time is used to check passcode timeout.
     * 
     * @param context
     */
    public static void updateLastActivityDisplay(Context context)
    {
        if (context instanceof HomeScreenActivity || context instanceof PassCodeActivity) { return; }
        if (context instanceof MainActivity && !((MainActivity) context).hasActivateCheckPasscode()) { return; }
        updateLastActivity(context);
    }

    public static void updateLastActivity(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPref.edit();
        editor.putLong(KEY_PASSCODE_ACTIVATED_AT, new Date().getTime());
        editor.commit();
    }

    /**
     * Determines if the application has passcode feature enable
     * 
     * @param context
     * @return true if the passcode must be prompt.
     */
    public static boolean hasPasscodeEnable(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean passcodeActivated = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);
        if (!passcodeActivated) { return false; }

        long activationTime = sharedPref.getLong(KEY_PASSCODE_ACTIVATED_AT, DEFAULT_ACTIVATION_TIME);
        if (activationTime == DEFAULT_ACTIVATION_TIME) { return false; }

        long durationTime = Long.parseLong(sharedPref.getString(KEY_PASSCODE_TIMEOUT, DEFAULT_TIMEOUT));
        long now = new Date().getTime();

        boolean isTimeOut = (now - activationTime) > durationTime;
        if (!isTimeOut)
        {
            updateLastActivity(context);
        }

        return isTimeOut;
    }
}

package org.alfresco.mobile.android.application.preferences;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.security.PassCodeDialogFragment;

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

public class PasscodePreferencesFragment extends PreferenceFragment
{

    public static final String KEY_PASSCODE_ENABLE = "PasscodeEnabled";

    public static final String TAG = "PasscodePreferencesFragment";

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Boolean passcodeEnable = sharedPref.getBoolean(KEY_PASSCODE_ENABLE, false);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.passcode_preferences);

        Preference pref = findPreference(getString(R.string.passcode_enable_key));
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
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
    }

}

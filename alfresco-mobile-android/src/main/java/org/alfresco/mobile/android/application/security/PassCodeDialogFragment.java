/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.security;

import static org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences.KEY_PASSCODE_ACTIVATED_AT;
import static org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences.KEY_PASSCODE_ATTEMPT;
import static org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences.KEY_PASSCODE_ENABLE;
import static org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences.KEY_PASSCODE_MAX_ATTEMPT;
import static org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences.KEY_PASSCODE_VALUE;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.preferences.PasscodePreferences;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This Fragment is responsible to prompt pin code form. It allows user to use
 * the app.<br/>
 * 
 * @author Jean Marie Pascal
 */
public class PassCodeDialogFragment extends DialogFragment
{
    public static final String ARGUMENT_MODE = "Mode";

    public static final String ARGUMENT_INFO = "info";

    public static final int MODE_INFO = 1;

    public static final int MODE_CREATE = 1;

    public static final int MODE_UPDATE = 2;

    public static final int MODE_DELETE = 3;

    public static final int MODE_USER_REQUEST = 4;

    private static final int PASSCODE_LENGTH = 4;

    /** Public Fragment TAG. */
    public static final String TAG = PassCodeDialogFragment.class.getName();

    private EditText passwordEditText;

    private EditText focusValue;

    private int[] passcode = new int[PASSCODE_LENGTH];

    private int[] confirmPasscode = new int[PASSCODE_LENGTH];

    private boolean needConfirmation = false;

    private TextView title;

    private TextView errorMessage;

    private SharedPreferences sharedPref;

    private boolean editionEnable;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public static PassCodeDialogFragment disable()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_MODE, MODE_DELETE);
        fragment.setArguments(b);
        return fragment;
    }

    public static PassCodeDialogFragment enable()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_MODE, MODE_CREATE);
        fragment.setArguments(b);
        return fragment;
    }

    public static PassCodeDialogFragment define()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_MODE, MODE_CREATE);
        b.putInt(ARGUMENT_INFO, MODE_INFO);
        fragment.setArguments(b);
        return fragment;
    }

    public static PassCodeDialogFragment modify()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_MODE, MODE_UPDATE);
        fragment.setArguments(b);
        return fragment;
    }

    public static PassCodeDialogFragment requestPasscode()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARGUMENT_MODE, MODE_USER_REQUEST);
        fragment.setArguments(b);
        return fragment;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog_MinWidth);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            // getDialog().setTitle(R.string.passcode_preference);
        }

        final View v = inflater.inflate(R.layout.app_passcode, (ViewGroup) this.getView());

        title = (TextView) v.findViewById(R.id.passcode_hint);

        if (getArguments().containsKey(ARGUMENT_INFO))
        {
            ((TextView) v.findViewById(R.id.passcode_admin)).setText(R.string.passcode_admin_title);
            v.findViewById(R.id.passcode_admin).setVisibility(View.VISIBLE);
        }
        else
        {

        }

        errorMessage = (TextView) v.findViewById(R.id.passcode_error);

        passwordEditText = (EditText) v.findViewById(R.id.passcode);
        passwordEditText.setInputType(InputType.TYPE_NULL);

        focusValue = passwordEditText;

        ImageView backButton = (ImageView) v.findViewById(R.id.delete_passcode);
        backButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearAll();
            }
        });

        int[] ids = new int[] { R.id.keyboard_0, R.id.keyboard_1, R.id.keyboard_2, R.id.keyboard_3, R.id.keyboard_4,
                R.id.keyboard_5, R.id.keyboard_6, R.id.keyboard_7, R.id.keyboard_8, R.id.keyboard_9,
                R.id.keyboard_back };

        Button key = null;
        for (int i = 0; i < ids.length; i++)
        {
            key = (Button) v.findViewById(ids[i]);
            key.setOnClickListener(keyboardClickListener);
        }

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (getActivity() instanceof MainActivity)
        {
            UIUtils.displayTitle(getActivity(), R.string.menu_settings);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.passcode_preference);
        }
        if (getDialog() != null)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            UIUtils.hideKeyboard(getActivity());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Avoid background stretching
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (getActivity().getSupportFragmentManager().findFragmentByTag(PasscodePreferences.TAG) != null)
        {
            ((PasscodePreferences) getActivity().getSupportFragmentManager().findFragmentByTag(PasscodePreferences.TAG))
                    .refresh();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private OnClickListener keyboardClickListener = new OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            int i = 0;
            switch (v.getId())
            {
                case R.id.keyboard_0:
                    i = 0;
                    break;
                case R.id.keyboard_1:
                    i = 1;
                    break;
                case R.id.keyboard_2:
                    i = 2;
                    break;
                case R.id.keyboard_3:
                    i = 3;
                    break;
                case R.id.keyboard_4:
                    i = 4;
                    break;
                case R.id.keyboard_5:
                    i = 5;
                    break;
                case R.id.keyboard_6:
                    i = 6;
                    break;
                case R.id.keyboard_7:
                    i = 7;
                    break;
                case R.id.keyboard_8:
                    i = 8;
                    break;
                case R.id.keyboard_9:
                    i = 9;
                    break;
                default:
                    break;
            }

            initCode(focusValue.length(), i);

            focusValue.setText(focusValue.getText().append("X"));

            if (focusValue.getText().length() == 4)
            {
                validate();
            }
        }
    };

    private void initCode(int index, int value)
    {
        if (needConfirmation)
        {
            confirmPasscode[index] = value;
        }
        else
        {
            passcode[index] = value;
        }
    }

    private void validate()
    {
        int mode = getArguments().getInt(ARGUMENT_MODE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (mode)
        {
            case MODE_USER_REQUEST:
                if (checkValue())
                {
                    Editor editor = sharedPref.edit();
                    editor.putLong(KEY_PASSCODE_ACTIVATED_AT, -1);
                    editor.remove(KEY_PASSCODE_ATTEMPT);
                    editor.apply();
                    getActivity().setResult(FragmentActivity.RESULT_OK);
                    getActivity().finish();
                }
                break;
            case MODE_CREATE:
                create();
                break;
            case MODE_DELETE:
                delete();
                break;
            case MODE_UPDATE:
                if (!editionEnable)
                {
                    update();
                }
                else
                {
                    create();
                }
                break;
            default:
                break;
        }
    }

    private void create()
    {
        if (!needConfirmation)
        {
            clearAll();
            title.setText(R.string.passcode_confirmation);
            errorMessage.setVisibility(View.INVISIBLE);
            needConfirmation = true;
        }
        else if (needConfirmation && getUserPassCode(true) != null)
        {
            Editor editor = sharedPref.edit();
            editor.putBoolean(KEY_PASSCODE_ENABLE, true);
            editor.remove(KEY_PASSCODE_ATTEMPT);
            editor.putString(KEY_PASSCODE_VALUE, getUserPassCode(true));
            editor.apply();
            dismiss();
            errorMessage.setVisibility(View.INVISIBLE);
            if (getActivity().getSupportFragmentManager().findFragmentByTag(PasscodePreferences.TAG) != null)
            {
                ((PasscodePreferences) getActivity().getSupportFragmentManager()
                        .findFragmentByTag(PasscodePreferences.TAG)).refresh();
            }
            else
            {
                editor.putLong(KEY_PASSCODE_ACTIVATED_AT, -1);
                editor.remove(KEY_PASSCODE_ATTEMPT);
                editor.apply();
                getActivity().setResult(FragmentActivity.RESULT_OK);
                getActivity().finish();
            }
        }
        else
        {
            clearAll();
            needConfirmation = false;
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(R.string.passcode_error_confirmation);
            title.setText(R.string.passcode_title);
        }
    }

    private void delete()
    {
        String passCodeValue = sharedPref.getString(KEY_PASSCODE_VALUE, "0000");
        if (passCodeValue.equals(getUserPassCode(false)))
        {
            Editor editor = sharedPref.edit();
            editor.putBoolean(KEY_PASSCODE_ENABLE, false);
            editor.remove(KEY_PASSCODE_ATTEMPT);
            editor.remove(KEY_PASSCODE_VALUE);
            editor.apply();
            dismiss();
            errorMessage.setVisibility(View.INVISIBLE);
            if (getActivity().getSupportFragmentManager().findFragmentByTag(PasscodePreferences.TAG) != null)
            {
                ((PasscodePreferences) getActivity().getSupportFragmentManager()
                        .findFragmentByTag(PasscodePreferences.TAG)).refresh();
            }
        }
        else
        {
            clearAll();
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(R.string.passcode_unknown);
            checkAttempts();
        }
    }

    private void update()
    {
        if (checkValue())
        {
            editionEnable = true;
            clearAll();
            title.setText(R.string.passcode_edit);
            errorMessage.setVisibility(View.INVISIBLE);
        }
        else
        {
            clearAll();
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(R.string.passcode_unknown);
            // checkAttempts();
        }
    }

    private boolean checkValue()
    {
        String passCodeValue = sharedPref.getString(KEY_PASSCODE_VALUE, "0000");
        if (passCodeValue.equals(getUserPassCode(false)))
        {
            return true;
        }
        else
        {
            clearAll();
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText(R.string.passcode_unknown);
            checkAttempts();
        }
        return false;
    }

    private void checkAttempts()
    {
        int attempts = sharedPref.getInt(KEY_PASSCODE_ATTEMPT, 1);
        int maxAttempts = sharedPref.getInt(KEY_PASSCODE_MAX_ATTEMPT, -1);

        if (maxAttempts > 0 && attempts >= maxAttempts)
        {
            WaitingDialogFragment fr = new WaitingDialogFragment();
            fr.setCancelable(false);
            fr.show(getActivity().getSupportFragmentManager(), WaitingDialogFragment.TAG);

            DataCleaner cleaner = new DataCleaner(getActivity());
            cleaner.execute();
            dismiss();
            return;
        }

        Editor editor = sharedPref.edit();
        editor.putInt(KEY_PASSCODE_ATTEMPT, attempts + 1);
        editor.apply();
    }

    private String getUserPassCode(boolean needConfirmation)
    {
        String passcodeValue = "";
        for (int i = 0; i < passcode.length; i++)
        {
            if (needConfirmation && passcode[i] != confirmPasscode[i])
            {
                break;
            }
            else
            {
                passcodeValue += passcode[i];
            }
        }

        if (passcodeValue.length() != PASSCODE_LENGTH)
        {
            passcodeValue = null;
        }
        return passcodeValue;
    }

    private void clearAll()
    {
        passwordEditText.setText("");
        focusValue = passwordEditText;
        passwordEditText.requestFocus();
    }
}

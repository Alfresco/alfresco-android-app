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
package org.alfresco.mobile.android.application.fragments.security;

import org.alfresco.mobile.android.application.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * This Fragment is responsible to prompt pin code form. It allows user to use
 * the app.<br/>
 * 
 * @author Jean Marie Pascal
 */
public class PassCodeDialogFragment extends DialogFragment
{
    public static final String PARAM_MODE = "Mode";

    public static final int MODE_CREATE = 0;

    public static final int MODE_UPDATE = 1;

    public static final int MODE_DELETE = 2;

    /** Public Fragment TAG. */
    public static final String TAG = "PassCodeDialogFragment";

    private EditText value1;

    private EditText value2;

    private EditText value3;

    private EditText value4;

    private EditText focuValue;

    private int[] passcode = new int[4];

    
    public static PassCodeDialogFragment disable()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(PARAM_MODE, MODE_DELETE);
        fragment.setArguments(b);
        return fragment;
    }
    
    public static PassCodeDialogFragment enable()
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        Bundle b = new Bundle();
        b.putInt(PARAM_MODE, MODE_CREATE);
        fragment.setArguments(b);
        return fragment;
    }
    
    public static PassCodeDialogFragment newInstance(Bundle bundle)
    {
        PassCodeDialogFragment fragment = new PassCodeDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_passcode, (ViewGroup) this.getView());

        value1 = (EditText) v.findViewById(R.id.passcode_1);
        value2 = (EditText) v.findViewById(R.id.passcode_2);
        value3 = (EditText) v.findViewById(R.id.passcode_3);
        value4 = (EditText) v.findViewById(R.id.passcode_4);

        value1.setInputType(InputType.TYPE_NULL);
        value2.setInputType(InputType.TYPE_NULL);
        value3.setInputType(InputType.TYPE_NULL);
        value4.setInputType(InputType.TYPE_NULL);

        value1.setOnFocusChangeListener(listener);
        value2.setOnFocusChangeListener(listener);
        value3.setOnFocusChangeListener(listener);
        value4.setOnFocusChangeListener(listener);

        focuValue = value1;

        int[] ids = new int[] { R.id.keyboard_0, R.id.keyboard_1, R.id.keyboard_2, R.id.keyboard_3, R.id.keyboard_4,
                R.id.keyboard_5, R.id.keyboard_6, R.id.keyboard_7, R.id.keyboard_8, R.id.keyboard_9, R.id.keyboard_back };

        Button key = null;
        for (int i = 0; i < ids.length; i++)
        {
            key = (Button) v.findViewById(ids[i]);
            key.setOnClickListener(keyboardClickListener);
        }

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.create_document_title).setView(v).create();
    }

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
                    i = 3;
                    break;
                case R.id.keyboard_5:
                    i = 3;
                    break;
                case R.id.keyboard_6:
                    i = 3;
                    break;
                case R.id.keyboard_7:
                    i = 3;
                    break;
                case R.id.keyboard_8:
                    i = 3;
                    break;
                case R.id.keyboard_9:
                    i = 3;
                    break;
                case R.id.keyboard_back:
                    previous(focuValue);
                    return;
                default:
                    break;
            }

            focuValue.setText("X");

            switch (focuValue.getId())
            {
                case R.id.passcode_1:
                    value2.requestFocus();
                    focuValue = value2;
                    passcode[0] = i;
                    break;
                case R.id.passcode_2:
                    value3.requestFocus();
                    focuValue = value3;
                    passcode[1] = i;
                    break;
                case R.id.passcode_3:
                    value4.requestFocus();
                    focuValue = value4;
                    passcode[2] = i;
                    break;
                case R.id.passcode_4:
                    passcode[3] = i;
                    validate();
                    break;
                default:
                    break;
            }
        }
    };

    private void validate()
    {
        int mode = getArguments().getInt(PARAM_MODE);

        switch (mode)
        {
            case MODE_CREATE:
                
                break;

            default:
                break;
        }
        
        
    }

    private OnFocusChangeListener listener = new OnFocusChangeListener()
    {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
            switch (v.getId())
            {
                case R.id.passcode_1:
                    clear(value1);
                    break;
                case R.id.passcode_2:
                    clear(value2);
                    break;
                case R.id.passcode_3:
                    clear(value3);
                    break;
                case R.id.passcode_4:
                    clear(value4);
                    break;
                default:
                    break;
            }
        }
    };

    private void previous(View v)
    {
        switch (v.getId())
        {
            case R.id.passcode_1:
                break;
            case R.id.passcode_2:
                value1.requestFocus();
                break;
            case R.id.passcode_3:
                value2.requestFocus();
                break;
            case R.id.passcode_4:
                value3.requestFocus();
                break;
            default:
                break;
        }
    }

    private void clear(EditText v)
    {
        if (v.hasFocus() && v.getText().length() == 1)
        {
            v.getText().clear();
        }
        focuValue = v;
    }

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(focuValue.getWindowToken(), 0);

        super.onStart();
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
}

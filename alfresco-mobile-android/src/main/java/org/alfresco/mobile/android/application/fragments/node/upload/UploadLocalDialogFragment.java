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
package org.alfresco.mobile.android.application.fragments.node.upload;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * This Fragment is responsible to prompt user for file name during import.<br/>
 * 
 * @author Jean Marie Pascal
 */
public class UploadLocalDialogFragment extends DialogFragment
{

    /** Public Fragment TAG. */
    public static final String TAG = "ImportLocalDialogFragment";

    public static final String ARGUMENT_ACCOUNT = "account";

    public static final String ARGUMENT_FILE = "file";

    public static UploadLocalDialogFragment newInstance(AlfrescoAccount account, File f)
    {
        UploadLocalDialogFragment fragment = new UploadLocalDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARGUMENT_ACCOUNT, account);
        b.putSerializable(ARGUMENT_FILE, f);
        fragment.setArguments(b);
        return fragment;
    }

    public static UploadLocalDialogFragment newInstance(Bundle bundle)
    {
        UploadLocalDialogFragment fragment = new UploadLocalDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AlfrescoAccount currentAccount = (AlfrescoAccount) getArguments().get(ARGUMENT_ACCOUNT);
        File f = AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(currentAccount);
        final File file = (File) getArguments().get(ARGUMENT_FILE);

        final File folderStorage = f;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_create_document, (ViewGroup) this.getView());

        v.findViewById(R.id.document_extension).setVisibility(View.GONE);

        final EditText textName = ((EditText) v.findViewById(R.id.document_name));
        final Button validate = UIUtils.initValidation(v, R.string.create);
        validate.setEnabled(false);
        final Button cancel = UIUtils.initCancel(v, R.string.cancel);

        textName.setText(file.getName());
        validate.setEnabled(true);
        validate.setText(R.string.action_import);
        // This Listener is responsible to enable or not the validate button and
        // error message.
        textName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    validate.setEnabled(true);
                    textName.setError(null);
                }
                else
                {
                    validate.setEnabled(false);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        cancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String fileName = textName.getText().toString();

                File newFile = new File(folderStorage, fileName);

                if (newFile.exists())
                {
                    // If the file already exist, we prompt a warning message.
                    textName.setError(getString(R.string.create_document_filename_error));
                    return;
                }
                else
                {
                    UIUtils.hideKeyboard(getActivity());

                    DataProtectionManager.getInstance(getActivity()).copyAndEncrypt(file, newFile);

                    if (getActivity() instanceof PublicDispatcherActivity)
                    {
                        getActivity().finish();
                    }
                }
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.import_document_name).setView(v).create();
    }

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

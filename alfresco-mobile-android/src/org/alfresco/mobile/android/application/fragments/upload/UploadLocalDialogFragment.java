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
package org.alfresco.mobile.android.application.fragments.upload;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.security.CipherUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This Fragment is responsible to prompt user for file name during import.<br/>
 * 
 * @author Jean Marie Pascal
 */
public class UploadLocalDialogFragment extends DialogFragment
{

    /** Public Fragment TAG. */
    public static final String TAG = "ImportLocalDialogFragment";

    public static final String PARAM_ACCOUNT = "account";

    public static final String PARAM_FILE = "file";

    public static UploadLocalDialogFragment newInstance(Account account, File f)
    {
        UploadLocalDialogFragment fragment = new UploadLocalDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(PARAM_ACCOUNT, account);
        b.putSerializable(PARAM_FILE, f);
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
        final Account currentAccount = (Account) getArguments().get(PARAM_ACCOUNT);
        File f = StorageManager.getDownloadFolder(getActivity(), currentAccount);
        final File file = (File) getArguments().get(PARAM_FILE);

        final File folderStorage = f;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_create_document, (ViewGroup) this.getView());

        ((TextView) v.findViewById(R.id.document_extension)).setVisibility(View.GONE);

        final EditText textName = ((EditText) v.findViewById(R.id.document_name));
        final TextView errorMessage = ((TextView) v.findViewById(R.id.error_message));
        final Button validate = (Button) v.findViewById(R.id.create_document);
        final Button cancel = (Button) v.findViewById(R.id.cancel);

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
                    errorMessage.setVisibility(View.GONE);
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
                    errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                else
                {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    EncryptionDialogFragment fragment = (CipherUtils.isEncryptionActive(getActivity())) ? EncryptionDialogFragment
                            .copyAndEncrypt(file.getPath(), newFile.getPath(), currentAccount)
                            : EncryptionDialogFragment.copy(file.getPath(), newFile.getPath(), currentAccount);
                    fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                    fragmentTransaction.commit();
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

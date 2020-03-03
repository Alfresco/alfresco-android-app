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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.create.CreateDirectoryRequest;
import org.alfresco.mobile.android.async.file.update.RenameFileRequest;
import org.alfresco.mobile.android.platform.io.IOUtils;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

public class FileNameDialogFragment extends DialogFragment
{

    public static final String TAG = FileNameDialogFragment.class.getName();

    private static final String ARGUMENT_FOLDER = "folder";

    private static final String ARGUMENT_FILE_RENAME = "fileToRename";

    private File parentFile;

    private File fileToRename;

    public FileNameDialogFragment()
    {
    }

    public static Bundle createBundle(File folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        return args;
    }

    public static Bundle createBundle(File folder, File fileToRename)
    {
        Bundle args = createBundle(folder);
        args.putSerializable(ARGUMENT_FILE_RENAME, fileToRename);
        return args;
    }

    public static DialogFragment newInstance(File parent)
    {
        FileNameDialogFragment adf = new FileNameDialogFragment();
        adf.setArguments(createBundle(parent));
        adf.setRetainInstance(true);
        return adf;
    }

    public static DialogFragment newInstance(File parent, File fileToRename)
    {
        FileNameDialogFragment adf = new FileNameDialogFragment();
        adf.setArguments(createBundle(parent, fileToRename));
        adf.setRetainInstance(true);
        return adf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        // Init File
        if (getArguments().containsKey(ARGUMENT_FILE_RENAME))
        {
            fileToRename = (File) getArguments().get(ARGUMENT_FILE_RENAME);
        }
        parentFile = (File) getArguments().get(ARGUMENT_FOLDER);

        String title = null;
        if (fileToRename != null)
        {
            title = getString(R.string.action_rename) + " : " + fileToRename.getName();
        }
        else
        {
            title = getString(R.string.folder_create);
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_create_document, (ViewGroup) this.getView());

        final MaterialEditText textName = ((MaterialEditText) v.findViewById(R.id.document_name));

        if (fileToRename != null)
        {
            textName.setText("." + IOUtils.extractFileExtension(fileToRename.getName()));
        }

        textName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    if (UIUtils.hasInvalidName(s.toString().trim()))
                    {
                        textName.setError(getString(R.string.filename_error_character));
                        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                    else if ((new File(parentFile, s.toString().trim()).exists()))
                    {
                        textName.setError(getString(R.string.create_document_filename_error));
                        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                    else
                    {
                        textName.setError(null);
                    }
                }
                else
                {
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    textName.setError(null);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                .title(title).customView(v, true).positiveText((fileToRename != null) ? R.string.ok : R.string.create)
                .negativeText(R.string.cancel).callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        UIUtils.hideKeyboard(getActivity());

                        if (fileToRename != null) {
                            String operationId = Operator.with(getActivity())
                                    .load(new RenameFileRequest.Builder(fileToRename,
                                            textName.getText().toString().trim())
                                            .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

                            OperationWaitingDialogFragment
                                    .newInstance(CreateDirectoryRequest.TYPE_ID, R.drawable.ic_edit,
                                            getString(R.string.action_rename), null, null, 0, operationId)
                                    .show(getActivity().getSupportFragmentManager(),
                                            OperationWaitingDialogFragment.TAG);
                        } else {
                            String operationId = Operator.with(getActivity())
                                    .load(new CreateDirectoryRequest.Builder((File) getArguments().get(ARGUMENT_FOLDER),
                                            textName.getText().toString().trim())
                                            .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));

                            OperationWaitingDialogFragment
                                    .newInstance(CreateDirectoryRequest.TYPE_ID, R.drawable.ic_add_folder,
                                            getString(R.string.folder_create), null, null, 0, operationId)
                                    .show(getActivity().getSupportFragmentManager(),
                                            OperationWaitingDialogFragment.TAG);
                        }

                        dismiss();
                    }
                }).build();

        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

        return dialog;
    }

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onStart();
    }

    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance())
        {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
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

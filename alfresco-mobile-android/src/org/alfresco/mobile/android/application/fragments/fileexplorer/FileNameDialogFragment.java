/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.file.create.CreateDirectoryRequest;
import org.alfresco.mobile.android.application.operations.batch.file.update.RenameRequest;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;

import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (fileToRename != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_edit);
        }
        else
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_folder);
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Init File
        if (getArguments().containsKey(ARGUMENT_FILE_RENAME))
        {
            fileToRename = (File) getArguments().get(ARGUMENT_FILE_RENAME);
        }
        parentFile = (File) getArguments().get(ARGUMENT_FOLDER);

        if (fileToRename != null)
        {
            getDialog().setTitle(getString(R.string.action_rename) + " : " + fileToRename.getName());
        }
        else
        {
            getDialog().setTitle(R.string.folder_create);
        }

        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.app_create_document, container, false);

        int width = (int) Math
                .round(UIUtils.getScreenDimension(getActivity())[0]
                        * (Float.parseFloat(getResources().getString(android.R.dimen.dialog_min_width_major).replace(
                                "%", "")) * 0.01));
        v.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

        final EditText textName = ((EditText) v.findViewById(R.id.document_name));
        final TextView errorMessage = ((TextView) v.findViewById(R.id.error_message));
        final Button validate = (Button) v.findViewById(R.id.create_document);
        final Button cancel = (Button) v.findViewById(R.id.cancel);

        if (fileToRename != null)
        {
            textName.setText("." + IOUtils.extractFileExtension(fileToRename.getName()));
            validate.setText(R.string.ok);
        }

        textName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    validate.setEnabled(true);
                    if (UIUtils.hasInvalidName(s.toString().trim()))
                    {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.filename_error_character);
                        validate.setEnabled(false);
                    }
                    else if ((new File(parentFile, s.toString().trim()).exists()))
                    {
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.create_document_filename_error);
                        validate.setEnabled(false);
                    }
                    else
                    {
                        errorMessage.setVisibility(View.GONE);
                    }
                }
                else
                {
                    validate.setEnabled(false);
                    errorMessage.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        validate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);

                OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils
                        .getAccount(getActivity()));
                if (fileToRename != null)
                {
                    group.enqueue(new RenameRequest(fileToRename, textName.getText().toString().trim())
                            .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                    
                    OperationWaitingDialogFragment.newInstance(CreateDirectoryRequest.TYPE_ID,
                            R.drawable.ic_edit, getString(R.string.action_rename), null, null, 0).show(
                            getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
                }
                else
                {
                    group.enqueue(new CreateDirectoryRequest((File) getArguments().get(ARGUMENT_FOLDER), textName
                            .getText().toString().trim()).setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                    
                    OperationWaitingDialogFragment.newInstance(CreateDirectoryRequest.TYPE_ID,
                            R.drawable.ic_add_folder, getString(R.string.folder_create), null, null, 0).show(
                            getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
                }
                
                BatchOperationManager.getInstance(getActivity()).enqueue(group);

                dismiss();
            }
        });

        cancel.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                FileNameDialogFragment.this.dismiss();
            }
        });

        return v;
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

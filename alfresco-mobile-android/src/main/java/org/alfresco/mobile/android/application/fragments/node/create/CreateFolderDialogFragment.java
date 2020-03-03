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
package org.alfresco.mobile.android.application.fragments.node.create;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

public abstract class CreateFolderDialogFragment extends AlfrescoFragment
{
    public static final String TAG = CreateFolderDialogFragment.class.getName();

    public static final String ARGUMENT_FOLDER = "folder";

    private MaterialEditText tv;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateFolderDialogFragment()
    {
        screenName = AnalyticsManager.SCREEN_NODE_CREATE_FOLDER_FORM;
    }

    public static Bundle createBundle(Folder folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        return args;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_NODE_CREATE_NAME);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                .title(R.string.folder_create).customView(createView(LayoutInflater.from(getActivity()), null), true)
                .positiveText(R.string.create).negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        UIUtils.hideKeyboard(getActivity(), tv);

                        String operationId = Operator.with(getActivity(), SessionUtils.getAccount(getActivity()))
                                .load(new CreateFolderRequest.Builder((Folder) getArguments().get(ARGUMENT_FOLDER),
                                        tv.getText().toString().trim()));

                        OperationWaitingDialogFragment
                                .newInstance(CreateFolderRequest.TYPE_ID, R.drawable.ic_add_folder,
                                        getString(R.string.folder_create), null,
                                        (Folder) getArguments().get(ARGUMENT_FOLDER), 0, operationId)
                                .show(getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);

                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        dialog.dismiss();
                    }
                }).build();
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        return dialog;
    }

    private View createView(LayoutInflater inflater, ViewGroup container)
    {
        View v = inflater.inflate(R.layout.sdk_create_folder, container, false);
        tv = (MaterialEditText) v.findViewById(R.id.folder_name);
        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (tv.getText().length() == 0)
                {
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    tv.setError(null);
                    tv.setHint(getString(R.string.folder_name_hint));
                }
                else
                {
                    tv.setFloatingLabelText(getString(R.string.folder_name));
                    tv.setHint(getString(R.string.folder_name_hint));
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    if (UIUtils.hasInvalidName(tv.getText().toString().trim()))
                    {
                        tv.setError(getString(R.string.filename_error_character));
                        tv.requestFocus();
                        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                    else
                    {
                        tv.setError(null);
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        return v;
    }

    @Override
    public void onStart()
    {
        // getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
        // R.drawable.mime_folder);
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

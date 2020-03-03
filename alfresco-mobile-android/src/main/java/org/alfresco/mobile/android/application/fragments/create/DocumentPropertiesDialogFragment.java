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
package org.alfresco.mobile.android.application.fragments.create;

import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.ARGUMENT_ACCOUNT;
import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.ARGUMENT_DOCUMENT_TYPE;
import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.ARGUMENT_FRAGMENT_TAG;
import static org.alfresco.mobile.android.application.fragments.create.EditorsDialogFragment.ARGUMENT_EDITOR;

import java.io.File;
import java.io.IOException;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * This Fragment is responsible to prompt user for property (limited at the
 * name) associated to the document.<br/>
 * 
 * @author Jean Marie Pascal
 */
public class DocumentPropertiesDialogFragment extends DialogFragment
{

    /** Public Fragment TAG. */
    public static final String TAG = DocumentPropertiesDialogFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static DocumentPropertiesDialogFragment newInstance(Bundle bundle)
    {
        DocumentPropertiesDialogFragment fragment = new DocumentPropertiesDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_NODE_CREATE_FORM);

        final String fragmentTag = (String) getArguments().get(ARGUMENT_FRAGMENT_TAG);
        final AlfrescoAccount currentAccount = (AlfrescoAccount) getArguments().get(ARGUMENT_ACCOUNT);
        final DocumentTypeRecord documentType = (DocumentTypeRecord) getArguments().get(ARGUMENT_DOCUMENT_TYPE);
        final ResolveInfo editor = (ResolveInfo) getArguments().get(ARGUMENT_EDITOR);

        File f = null;
        if (FileExplorerFragment.TAG.equals(fragmentTag))
        {
            // If creation inside the download area, we store it inside
            // download.
            f = AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(currentAccount);
        }
        else
        {
            // If creation inside a repository folder, we store temporarly
            // inside the capture.
            f = AlfrescoStorageManager.getInstance(getActivity()).getCaptureFolder(currentAccount);
        }

        final File folderStorage = f;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_create_document, (ViewGroup) this.getView());

        ((TextView) v.findViewById(R.id.document_extension)).setText(documentType.extension);

        final MaterialEditText textName = ((MaterialEditText) v.findViewById(R.id.document_name));
        // This Listener is responsible to enable or not the validate button and
        // error message.
        textName.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    textName.setFloatingLabelText(getString(R.string.content_name));
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    if (UIUtils.hasInvalidName(s.toString().trim()))
                    {
                        textName.setError(getString(R.string.filename_error_character));
                        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                    else
                    {
                        textName.setError(null);
                    }
                }
                else
                {
                    textName.setHint(R.string.create_document_name_hint);
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
                .title(R.string.create_document_title).customView(v, true).positiveText(R.string.create)
                .negativeText(R.string.cancel).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        dialog.dismiss();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        String fileName = textName.getText().toString().trim().concat(documentType.extension);

                        File newFile = new File(folderStorage, fileName);

                        if (newFile.exists() && FileExplorerFragment.TAG.equals(fragmentTag))
                        {
                            // If the file already exist, we prompt a warning
                            // message.
                            textName.setError(getString(R.string.create_document_filename_error));
                            return;
                        }
                        else
                        {
                            try
                            {
                                // If there's a template we create the file
                                // based on
                                // this template.
                                if (documentType.templatePath != null)
                                {
                                    AssetManager assetManager = getActivity().getAssets();
                                    IOUtils.copyFile(assetManager.open(documentType.templatePath), newFile);
                                }
                                else
                                {
                                    newFile.createNewFile();
                                }
                            }
                            catch (IOException e1)
                            {
                                Log.e(TAG, Log.getStackTraceString(e1));
                            }
                        }

                        // We create the Intent based on informations we grab
                        // previously.
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        Uri data;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            data = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", newFile);
                        } else {
                            data = Uri.fromFile(newFile);
                        }

                        intent.setDataAndType(data, documentType.mimetype);
                        intent.setComponent(new ComponentName(editor.activityInfo.applicationInfo.packageName,
                                editor.activityInfo.name));

                        try
                        {
                            Fragment fr = getFragmentManager().findFragmentByTag(fragmentTag);
                            if (fr != null && fr.isVisible())
                            {
                                if (fr instanceof DocumentFolderBrowserFragment)
                                {
                                    // During Creation on a specific folder.
                                    ((DocumentFolderBrowserFragment) fr).setCreateFile(newFile);
                                }
                                else if (fr instanceof FileExplorerFragment)
                                {
                                    // During Creation inside the download
                                    // folder.
                                    ((FileExplorerFragment) fr).setCreateFile(newFile);
                                }
                                fr.startActivity(intent);
                            }
                        }
                        catch (ActivityNotFoundException e)
                        {
                            AlfrescoNotificationManager.getInstance(getActivity())
                                    .showToast(R.string.error_unable_open_file);
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

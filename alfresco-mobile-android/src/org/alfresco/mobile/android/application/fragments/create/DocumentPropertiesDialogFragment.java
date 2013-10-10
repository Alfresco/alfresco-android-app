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
package org.alfresco.mobile.android.application.fragments.create;

import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.PARAM_ACCOUNT;
import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.PARAM_DOCUMENT_TYPE;
import static org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment.PARAM_FRAGMENT_TAG;
import static org.alfresco.mobile.android.application.fragments.create.EditorsDialogFragment.PARAM_EDITOR;

import java.io.File;
import java.io.IOException;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.commons.data.DocumentTypeRecord;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    public static DocumentPropertiesDialogFragment newInstance(Bundle bundle)
    {
        DocumentPropertiesDialogFragment fragment = new DocumentPropertiesDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final String fragmentTag = (String) getArguments().get(PARAM_FRAGMENT_TAG);
        final Account currentAccount = (Account) getArguments().get(PARAM_ACCOUNT);
        final DocumentTypeRecord documentType = (DocumentTypeRecord) getArguments().get(PARAM_DOCUMENT_TYPE);
        final ResolveInfo editor = (ResolveInfo) getArguments().get(PARAM_EDITOR);

        File f = null;
        if (FileExplorerFragment.TAG.equals(fragmentTag))
        {
            // If creation inside the download area, we store it inside
            // download.
            f = StorageManager.getDownloadFolder(getActivity(), currentAccount);
        }
        else
        {
            // If creation inside a repository folder, we store temporarly
            // inside the capture.
            f = StorageManager.getCaptureFolder(getActivity(), currentAccount);
        }

        final File folderStorage = f;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.app_create_document, (ViewGroup) this.getView());

        ((TextView) v.findViewById(R.id.document_extension)).setText(documentType.extension);

        final EditText textName = ((EditText) v.findViewById(R.id.document_name));
        final TextView errorMessage = ((TextView) v.findViewById(R.id.error_message));
        final Button validate = (Button) v.findViewById(R.id.create_document);
        final Button cancel = (Button) v.findViewById(R.id.cancel);

        // This Listener is responsible to enable or not the validate button and
        // error message.
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

        cancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String fileName = textName.getText().toString().trim().concat(documentType.extension);

                File newFile = new File(folderStorage, fileName);

                if (newFile.exists() && FileExplorerFragment.TAG.equals(fragmentTag))
                {
                    // If the file already exist, we prompt a warning message.
                    errorMessage.setVisibility(View.VISIBLE);
                    return;
                }
                else
                {
                    try
                    {
                        // If there's a template we create the file based on
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
                Uri data = Uri.fromFile(newFile);
                intent.setDataAndType(data, documentType.mimetype);
                intent.setComponent(new ComponentName(editor.activityInfo.applicationInfo.packageName,
                        editor.activityInfo.name));

                try
                {
                    Fragment fr = getFragmentManager().findFragmentByTag(fragmentTag);
                    if (fr != null && fr.isVisible())
                    {
                        if (fr instanceof ChildrenBrowserFragment)
                        {
                            // During Creation on a specific folder.
                            ((ChildrenBrowserFragment) fr).setCreateFile(newFile);
                        }
                        else if (fr instanceof FileExplorerFragment)
                        {
                            // During Creation inside the download folder.
                            ((FileExplorerFragment) fr).setCreateFile(newFile);
                        }
                        fr.startActivity(intent);
                    }
                }
                catch (ActivityNotFoundException e)
                {
                    MessengerManager.showToast(getActivity(), R.string.error_unable_open_file);
                }
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.create_document_title).setView(v).create();
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

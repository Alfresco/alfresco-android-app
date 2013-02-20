/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.browser;


import static org.alfresco.mobile.android.application.fragments.browser.UploadFragment.ARGUMENT_FOLDER;
import static org.alfresco.mobile.android.application.fragments.browser.UploadFragment.ARGUMENT_CONTENT_FILE;
import static org.alfresco.mobile.android.application.fragments.browser.UploadFragment.ARGUMENT_CONTENT_NAME;
import static org.alfresco.mobile.android.application.fragments.browser.UploadFragment.ARGUMENT_CONTENT_DESCRIPTION;
import static org.alfresco.mobile.android.application.fragments.browser.UploadFragment.ARGUMENT_CONTENT_TAGS;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.api.model.impl.TagImpl;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.integration.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.integration.upload.UploadService;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Specific override for Uploading content. This fragment is responsible to
 * display the upload UI component. When user click on upload button, it creates
 * an UploadFragment (UI less) responsible to maintain callback methods. The
 * UploadFragment may disappear to support batch upload/download.
 * 
 * @author Jean Marie Pascal
 */
public abstract class CreateDocumentDialogFragment extends BaseFragment
{
    public static final String TAG = "CreateContentDialogFragment";

    public static final String ARGUMENT_IS_CREATION = "isCreation";

    private EditText editTags;

    private List<Tag> selectedTags = new ArrayList<Tag>();

    public CreateDocumentDialogFragment()
    {
    }

    public static Bundle createBundle(Folder folder)
    {
        return createBundle(folder, null, null);
    }

    public static Bundle createBundle(Folder folder, ContentFile f, Boolean isCreation)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        args.putSerializable(ARGUMENT_CONTENT_FILE, f);
        args.putBoolean(ARGUMENT_IS_CREATION, isCreation);
        return args;
    }

    @Override
    public void onStart()
    {
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_file);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.content_upload);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View v = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        final EditText tv = (EditText) v.findViewById(R.id.content_name);
        final EditText desc = (EditText) v.findViewById(R.id.content_description);
        TextView tsize = (TextView) v.findViewById(R.id.content_size);

        editTags = (EditText) v.findViewById(R.id.content_tags);

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                CreateDocumentDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = (Button) v.findViewById(R.id.create_content);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Bundle b = new Bundle();
                b.putAll(getArguments());
                b.putString(ARGUMENT_CONTENT_NAME, tv.getText().toString());
                if (desc.getText() != null && desc.getText().length() > 0)
                {
                    b.putString(ARGUMENT_CONTENT_DESCRIPTION, desc.getText().toString());
                }
                onValidateTags();
                if (selectedTags != null && !selectedTags.isEmpty())
                {
                    ArrayList<String> listTagValue = new ArrayList<String>(selectedTags.size());
                    for (Tag tag : selectedTags)
                    {
                        listTagValue.add(tag.getValue());
                    }
                    b.putStringArrayList(ARGUMENT_CONTENT_TAGS, listTagValue);
                }
                b.putSerializable(ARGUMENT_CONTENT_FILE, getArguments().getSerializable(ARGUMENT_CONTENT_FILE));
                bcreate.setEnabled(false);

                // Dismiss the dialog
                CreateDocumentDialogFragment.this.dismiss();

                if (getActivity() instanceof MainActivity)
                {
                    // Use UploadFragment to manage upload
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    UploadFragment uploadFragment = UploadFragment.newInstance(b);
                    fragmentTransaction.add(uploadFragment, uploadFragment.getFragmentTransactionTag());
                    fragmentTransaction.commit();
                }
                else if (getActivity() instanceof PublicDispatcherActivity)
                {
                    b.putParcelable(UploadService.ARGUMENT_SESSION, SessionUtils.getSession(getActivity()));
                    UploadService.updateImportService(getActivity(), b);
                    getActivity().finish();
                }
            }
        });

        if (getArguments().getSerializable(ARGUMENT_CONTENT_FILE) != null)
        {
            ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);
            tv.setText(f.getFileName());
            tsize.setText(Formatter.formatFileSize(getActivity(), f.getLength()));
            tsize.setVisibility(View.VISIBLE);
            bcreate.setEnabled(true);
        }
        else
        {
            tsize.setVisibility(View.GONE);
        }

        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (tv.getText().length() == 0)
                {
                    bcreate.setEnabled(false);
                }
                else
                {
                    bcreate.setEnabled(true);
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

    private void onValidateTags()
    {
        String s = editTags.getText().toString();
        String[] listValues = s.split(",");
        for (int i = 0; i < listValues.length; i++)
        {
            if (listValues[i] != null && !listValues[i].isEmpty())
            {
                selectedTags.add(new TagImpl(listValues[i].trim()));
            }
        }
    }
}

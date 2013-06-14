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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.api.model.impl.TagImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.application.operations.batch.node.create.RetrieveDocumentNameRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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

    private static final String ARGUMENT_FOLDER = "folder";

    private static final String ARGUMENT_CONTENT_FILE = "ContentFile";

    private static final String ARGUMENT_IS_CREATION = "isCreation";

    private EditText editTags;

    private List<Tag> selectedTags = new ArrayList<Tag>();

    private CreateDocumentReceiver receiver;

    private String recommandedName = null;
    
    private String originalName = null;

    private ContentFile contentFile;

    private View pb;

    private EditText tv;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
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

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.content_upload);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View v = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        pb = (TextView) v.findViewById(R.id.document_error);
        tv = (EditText) v.findViewById(R.id.content_name);
        final EditText desc = (EditText) v.findViewById(R.id.content_description);
        TextView tsize = (TextView) v.findViewById(R.id.content_size);

        editTags = (EditText) v.findViewById(R.id.content_tags);

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                File uploadFile = ((ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE)).getFile();

                // If the file is a temporary file, remove it on cancellation of
                // dialog.
                if (StorageManager.isTempFile(getActivity(), uploadFile)) uploadFile.delete();

                CreateDocumentDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = (Button) v.findViewById(R.id.create_content);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                createDocument(tv, desc, bcreate);
            }
        });

        if (getArguments().getSerializable(ARGUMENT_CONTENT_FILE) != null)
        {
            contentFile = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);
            originalName = contentFile.getFileName();
            tv.setText(originalName);
            tsize.setText(Formatter.formatFileSize(getActivity(), contentFile.getLength()));
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
                if (originalName.equals(tv.getText().toString()))
                {
                    pb.setVisibility(View.VISIBLE);
                }
                else
                {
                    pb.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }
        });

        editTags.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    createDocument(tv, desc, bcreate);
                    handled = true;
                }
                return handled;
            }
        });

        Folder parentFolder = getParent();
        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new RetrieveDocumentNameRequest(parentFolder.getIdentifier(), contentFile.getFileName())
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(getActivity()).enqueue(group);

        return v;
    }

    @Override
    public void onStart()
    {

        if (getArguments().getSerializable(ARGUMENT_CONTENT_FILE) != null)
        {
            ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, MimeTypeManager.getIcon(f.getFileName()));
        }
        else
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_file);
        }
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_RETRIEVE_NAME_COMPLETED);
            receiver = new CreateDocumentReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTIONS
    // //////////////////////////////////////////////////////////////////////
    private void createDocument(EditText tv, EditText desc, Button bcreate)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(3);
        String documentName = tv.getText().toString();
        
        if (originalName.equals(documentName) && recommandedName != null && !recommandedName.equals(originalName))
        {
            documentName = recommandedName;
        }
        
        if (desc.getText() != null && desc.getText().length() > 0)
        {
            props.put(ContentModel.PROP_DESCRIPTION, desc.getText().toString());
        }
        onValidateTags();
        List<String> listTagValue = null;
        if (selectedTags != null && !selectedTags.isEmpty())
        {
            listTagValue = new ArrayList<String>(selectedTags.size());
            for (Tag tag : selectedTags)
            {
                listTagValue.add(tag.getValue());
            }
        }
        ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);
        bcreate.setEnabled(false);
        Folder parentFolder = (Folder) getArguments().get(ARGUMENT_FOLDER);
        Boolean isCreation = getArguments().getBoolean(ARGUMENT_IS_CREATION);

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new CreateDocumentRequest(parentFolder.getIdentifier(), documentName, props, listTagValue, f,
                isCreation));
        BatchOperationManager.getInstance(getActivity()).enqueue(group);

        if (getActivity() instanceof PublicDispatcherActivity)
        {
            getActivity().finish();
        }

        // Dismiss the dialog
        CreateDocumentDialogFragment.this.dismiss();
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

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    private Folder getParent()
    {
        return (Folder) getArguments().get(ARGUMENT_FOLDER);
    }

    // //////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // //////////////////////////////////////////////////////////////////////
    public class CreateDocumentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());

            if (intent.getExtras() != null
                    && intent.getAction().equals(IntentIntegrator.ACTION_RETRIEVE_NAME_COMPLETED))
            {
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);
                recommandedName = b.getString(IntentIntegrator.EXTRA_DOCUMENT_NAME);
                if (!recommandedName.equals(originalName))
                {
                    pb.setVisibility(View.VISIBLE);
                }

            }
        }
    }
}

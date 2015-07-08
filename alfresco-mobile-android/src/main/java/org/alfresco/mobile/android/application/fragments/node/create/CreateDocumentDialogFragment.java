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
package org.alfresco.mobile.android.application.fragments.node.create;

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
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.model.config.ItemConfig;
import org.alfresco.mobile.android.api.model.impl.TagImpl;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.configuration.CreateConfigManager;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.create.CreateDocumentRequest;
import org.alfresco.mobile.android.async.node.create.RetrieveDocumentNameEvent;
import org.alfresco.mobile.android.async.node.create.RetrieveDocumentNameRequest;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.holder.SingleLineViewHolder;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
public abstract class CreateDocumentDialogFragment extends AlfrescoFragment
{
    public static final String TAG = CreateDocumentDialogFragment.class.getSimpleName();

    private static final String ARGUMENT_FOLDER = "folder";

    private static final String ARGUMENT_CONTENT_FILE = "ContentFile";

    private static final String ARGUMENT_IS_CREATION = "isCreation";

    private EditText editTags;

    private List<Tag> selectedTags = new ArrayList<Tag>();

    private String recommandedName = null;

    private String tempName = null;

    private ContentFile contentFile;

    private EditText tv;

    private CreateConfigManager createConfigManager;

    private ItemConfig type;

    private Button bcreate;

    private boolean requestCheck = true;

    private boolean requestInProgress = false;

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

        // Configuration available ?
        ConfigService configService = ConfigManager.getInstance(getActivity()).getConfig(getAccount().getId(),
                ConfigTypeIds.CREATION);
        if (configService != null)
        {
            createConfigManager = new CreateConfigManager(getActivity(), configService, (ViewGroup) getRootView());
        }

        View rootView = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        tv = (EditText) rootView.findViewById(R.id.content_name);
        final EditText desc = (EditText) rootView.findViewById(R.id.content_description);
        TextView tsize = (TextView) rootView.findViewById(R.id.content_size);

        editTags = (EditText) rootView.findViewById(R.id.content_tags);

        Button button = (Button) rootView.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                File uploadFile = ((ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE)).getFile();

                // If the file is a temporary file, remove it on cancellation of
                // dialog.
                if (AlfrescoStorageManager.getInstance(getActivity()).isTempFile(uploadFile))
                {
                    uploadFile.delete();
                }

                CreateDocumentDialogFragment.this.dismiss();
            }
        });

        bcreate = UIUtils.initValidation(rootView, R.string.confirm);
        bcreate.setEnabled(false);
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
            tempName = contentFile.getFileName();
            tv.setText(tempName);
            tsize.setText(Formatter.formatFileSize(getActivity(), contentFile.getLength()));
            tsize.setVisibility(View.VISIBLE);
        }
        else
        {
            tsize.setVisibility(View.GONE);
        }

        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                tempName = tv.getText().toString();
                if (tv.getText().length() == 0)
                {
                    bcreate.setEnabled(false);
                    tv.setError(null);
                }
                else
                {
                    if (UIUtils.hasInvalidName(tv.getText().toString().trim()))
                    {
                        tv.setError(getString(R.string.filename_error_character));
                        bcreate.setEnabled(false);
                    }
                    else
                    {
                        tv.setError(null);
                        bcreate.setEnabled(false);
                        if (!requestInProgress)
                        {
                            Operator.with(getActivity(), getAccount()).load(
                                    new RetrieveDocumentNameRequest.Builder(getParent(), tv.getText().toString()));
                            requestCheck = false;
                            requestInProgress = true;
                        }
                        else
                        {
                            requestCheck = true;
                        }
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

        editTags.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (!bcreate.isEnabled()) { return false; }
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

        Operator.with(getActivity(), getAccount()).load(
                new RetrieveDocumentNameRequest.Builder(parentFolder, contentFile.getFileName()));

        // Custom type
        if (createConfigManager != null)
        {
            DisplayUtils.show(rootView, R.id.types_group);
            Spinner spinner = (Spinner) rootView.findViewById(R.id.types_spinner);
            TypeAdapter adapter = new TypeAdapter(getActivity(), R.layout.row_single_line,
                    createConfigManager.retrieveCreationDocumentTypeList());
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
                {
                    type = (ItemConfig) parent.getItemAtPosition(pos);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0)
                {
                    // DO Nothing
                }
            });
            if (adapter.isEmpty())
            {
                DisplayUtils.hide(rootView, R.id.types_group);
            }
        }
        else
        {
            DisplayUtils.hide(rootView, R.id.types_group);
        }

        return rootView;
    }

    @Override
    public void onStart()
    {

        if (getArguments().getSerializable(ARGUMENT_CONTENT_FILE) != null)
        {
            ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    MimeTypeManager.getInstance(getActivity()).getIcon(f.getFileName()));
        }
        else
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_256_generic);
        }
        super.onStart();
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTIONS
    // //////////////////////////////////////////////////////////////////////
    private void createDocument(EditText tv, EditText desc, Button bcreate)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        String documentName = tv.getText().toString().trim();

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

        Operator.with(getActivity(), getAccount()).load(
                new CreateDocumentRequest.Builder(parentFolder, documentName, (type != null) ? type.getIdentifier()
                        : null, f, props, listTagValue, isCreation)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));

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
    // EVENTS RECEIVER
    // //////////////////////////////////////////////////////////////////////
    public void onRetrieveDocumentName(RetrieveDocumentNameEvent event)
    {
        requestInProgress = false;
        recommandedName = event.data;
        if (!recommandedName.equals(event.originalName))
        {
            tv.setError(getString(R.string.create_document_filename_error));
            bcreate.setEnabled(false);
        }
        else
        {
            tv.setError(null);
            bcreate.setEnabled(true);
        }

        if (requestCheck)
        {
            bcreate.setEnabled(false);
            Operator.with(getActivity(), getAccount()).load(
                    new RetrieveDocumentNameRequest.Builder(getParent(), tv.getText().toString()));
            requestCheck = false;
            requestInProgress = true;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // ADAPTER
    // //////////////////////////////////////////////////////////////////////
    public class TypeAdapter extends BaseListAdapter<ItemConfig, SingleLineViewHolder>
    {
        public TypeAdapter(Activity context, int textViewResourceId, List<ItemConfig> listItems)
        {
            super(context, textViewResourceId, listItems);
            this.vhClassName = SingleLineViewHolder.class.getCanonicalName();
        }

        @Override
        protected void updateTopText(SingleLineViewHolder vh, ItemConfig item)
        {
            vh.topText.setText(item.getLabel());
        }

        @Override
        protected void updateBottomText(SingleLineViewHolder vh, ItemConfig item)
        {
        }

        @Override
        protected void updateIcon(SingleLineViewHolder vh, ItemConfig item)
        {
            DisplayUtils.hide(vh.icon);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return getView(position, convertView, parent);
        }
    }
}

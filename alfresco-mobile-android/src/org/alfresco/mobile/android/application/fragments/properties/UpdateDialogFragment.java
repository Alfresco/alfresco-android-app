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
package org.alfresco.mobile.android.application.fragments.properties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.documentfolder.actions.UpdateNodeDialogFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class UpdateDialogFragment extends UpdateNodeDialogFragment
{

    private Node node;

    private Folder folder;

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String TAG = "UpdateDialogFragment";

    public UpdateDialogFragment()
    {
    }

    public static UpdateDialogFragment newInstance(Folder folder, Node node)
    {
        UpdateDialogFragment adf = new UpdateDialogFragment();
        Bundle b = new Bundle(createBundle(node));
        b.putParcelable(ARGUMENT_FOLDER, folder);
        adf.setArguments(b);
        return adf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        node = (Node) getArguments().getSerializable(ARGUMENT_NODE);
        folder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);

        getDialog().setTitle(R.string.edit_metadata);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        View v = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        final EditText tv = (EditText) v.findViewById(R.id.content_name);
        final EditText desc = (EditText) v.findViewById(R.id.content_description);
        TextView tsize = (TextView) v.findViewById(R.id.content_size);

        v.findViewById(R.id.tags_line).setVisibility(View.GONE);
        desc.setImeOptions(EditorInfo.IME_ACTION_DONE);

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                UpdateDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = (Button) v.findViewById(R.id.create_content);
        bcreate.setText(R.string.update);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                updateNode(tv, desc, bcreate);
            }
        });

        desc.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    updateNode(tv, desc, bcreate);
                    handled = true;
                }
                return handled;
            }
        });

        if (node != null)
        {
            tv.setText(node.getName());
            if (node.isDocument())
            {
                tsize.setText(Formatter.formatFileSize(getActivity(), ((Document) node).getContentStreamLength()));
                tsize.setVisibility(View.VISIBLE);
            }

            if (RepositoryVersionHelper.isAlfrescoProduct(alfSession)
                    && node.getProperty(ContentModel.PROP_DESCRIPTION) != null
                    && node.getProperty(ContentModel.PROP_DESCRIPTION).getValue() != null)
            {
                desc.setText(node.getProperty(ContentModel.PROP_DESCRIPTION).getValue().toString());
            }

            bcreate.setEnabled(true);

        }
        else
        {
            tsize.setVisibility(View.GONE);
        }

        final EditText textName = ((EditText) v.findViewById(R.id.content_name));
        final TextView errorMessage = ((TextView) v.findViewById(R.id.error_message));
        final Button validate = (Button) v.findViewById(R.id.create_content);

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

        return v;
    }

    @Override
    public void onStart()
    {
        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_edit);
        }
        super.onStart();
    }

    protected void updateNode(EditText tv, EditText desc, Button bcreate)
    {
        bcreate.setEnabled(false);

        Map<String, Serializable> props = new HashMap<String, Serializable>(2);
        props.put(ContentModel.PROP_NAME, tv.getText().toString().trim());
        if (desc.getText() != null && desc.getText().length() > 0)
        {
            props.put(ContentModel.PROP_DESCRIPTION, desc.getText().toString());
        }

        OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
        group.enqueue(new UpdatePropertiesRequest(folder, node, props).setNotificationTitle(node.getName())
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(getActivity()).enqueue(group);
        dismiss();
    }

}

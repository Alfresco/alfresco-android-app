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
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.update.UpdatePropertiesRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.documentfolder.actions.UpdateNodeDialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
        View v = super.onCreateView(inflater, container, savedInstanceState);

        node = (Node) getArguments().getSerializable(ARGUMENT_NODE);
        folder = (Folder) getArguments().getSerializable(ARGUMENT_FOLDER);

        return v;
    }

    protected void updateNode(EditText tv, EditText desc, Button bcreate)
    {
        bcreate.setEnabled(false);

        Map<String, Serializable> props = new HashMap<String, Serializable>(2);
        props.put(ContentModel.PROP_NAME, tv.getText().toString());
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

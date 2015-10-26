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
package org.alfresco.mobile.android.ui.node.update;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.async.node.update.UpdateNodeRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

public abstract class UpdateNodeDialogFragment extends AlfrescoFragment
{
    public static final String TAG = "UpdateNodeDialogFragment";

    protected static final String ARGUMENT_NODE = "node";

    protected Node node;

    private MaterialEditText tv, desc, tags;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public UpdateNodeDialogFragment()
    {
    }

    public static Bundle createBundle(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        node = (Node) getArguments().getSerializable(ARGUMENT_NODE);

        return new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                .title(R.string.edit_metadata).customView(createView(LayoutInflater.from(getActivity()), null), true)
                .positiveText(R.string.update).negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        updateNode(tv, desc);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        dialog.dismiss();
                    }
                }).show();
    }

    private View createView(LayoutInflater inflater, ViewGroup container)
    {
        View rootView = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        tv = (MaterialEditText) rootView.findViewById(R.id.content_name);
        desc = (MaterialEditText) rootView.findViewById(R.id.content_description);
        tags = (MaterialEditText) rootView.findViewById(R.id.content_tags);
        tags.setVisibility(View.GONE);
        TextView tsize = (TextView) rootView.findViewById(R.id.content_size);

        desc.setImeOptions(EditorInfo.IME_ACTION_DONE);
        desc.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    updateNode(tv, desc);
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
            else
            {
                tsize.setVisibility(View.GONE);
            }

            if (RepositoryVersionHelper.isAlfrescoProduct(getSession())
                    && node.getProperty(ContentModel.PROP_DESCRIPTION) != null
                    && node.getProperty(ContentModel.PROP_DESCRIPTION).getValue() != null)
            {
                desc.setText(node.getProperty(ContentModel.PROP_DESCRIPTION).getValue().toString());
            }
        }
        else
        {
            tsize.setVisibility(View.GONE);
        }

        // This Listener is responsible to enable or not the validate button and
        // error message.
        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    if (UIUtils.hasInvalidName(s.toString().trim()))
                    {
                        tv.setError(getString(R.string.filename_error_character));
                        ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    }
                    else
                    {
                        tv.setError(null);
                    }
                }
                else
                {
                    ((MaterialDialog) getDialog()).getActionButton(DialogAction.POSITIVE).setEnabled(false);
                    tv.setError(null);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        return rootView;
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////
    protected void updateNode(EditText tv, EditText desc)
    {
        Map<String, Serializable> props = new HashMap<>(2);
        props.put(ContentModel.PROP_NAME, tv.getText().toString());
        if (desc.getText() != null && desc.getText().length() > 0)
        {
            props.put(ContentModel.PROP_DESCRIPTION, desc.getText().toString());
        }

        new UpdateNodeRequest.Builder(node, props);
    }
}

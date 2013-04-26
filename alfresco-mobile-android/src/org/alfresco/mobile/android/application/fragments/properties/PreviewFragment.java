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

import java.io.File;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.browser.DownloadDialogFragment;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PreviewFragment extends BaseFragment
{

    public static final String TAG = "PreviewFragment";

    public static final String ARGUMENT_NODE = "node";

    protected File tempFile = null;
    
    public File getTempFile()
    {
        return tempFile;
    }
    
    public void setTempFile (File tempFile)
    {
        this.tempFile = tempFile;
    }
    
    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static PreviewFragment newInstance(Node n)
    {
        PreviewFragment bf = new PreviewFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    private Node node;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());
        View v = inflater.inflate(R.layout.app_preview, container, false);

        node = (Node) getArguments().get(ARGUMENT_NODE);
        if (node == null) { return null; }

        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(getActivity());

        ImageView preview = (ImageView) v.findViewById(R.id.preview);
        int iconId = R.drawable.mime_folder;
        if (node.isDocument())
        {
            iconId = MimeTypeManager.getIcon(node.getName(), true);
            if (((Document) node).isLatestVersion())
            {
                renditionManager.preview((ImageView) preview, node, iconId, DisplayUtils.getWidth(getActivity()));
            }
        }
        else
        {
            preview.setImageResource(iconId);
        }

        preview.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openin();
            }
        });

        return v;
    }

    public void openin()
    {
        Bundle b = new Bundle();
        
        if (CipherUtils.isEncryptionActive(getActivity()))
        {
            tempFile = IOUtils.makeTempFile(NodeActions.getDownloadFile(getActivity(), node));
            b.putString(DownloadDialogFragment.ARGUMENT_TEMPFILE, tempFile.getPath());
        }
        
        b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
        b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
        DialogFragment frag = new DownloadDialogFragment();
        frag.setArguments(b);
        frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
    }
}

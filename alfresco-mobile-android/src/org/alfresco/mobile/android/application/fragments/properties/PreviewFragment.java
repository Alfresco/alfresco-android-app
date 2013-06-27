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
import java.util.Date;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.fragments.browser.DownloadDialogFragment;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.RenditionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.database.Cursor;
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

    public void setTempFile(File tempFile)
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

        RenditionManager renditionManager = ApplicationManager.getInstance(getActivity()).getRenditionManager(
                getActivity());

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

        // 3 cases
        SynchroManager syncManager = SynchroManager.getInstance(getActivity());
        Account acc = SessionUtils.getAccount(getActivity());

        DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentByTag(DetailsFragment.TAG);
        
        if (syncManager.isSynced(SessionUtils.getAccount(getActivity()), node))
        {
            final File syncFile = syncManager.getSyncFile(acc, node);
            long datetime = syncFile.lastModified();
            detailsFragment.setDownloadDateTime(new Date(datetime));

            if (DataProtectionManager.getInstance(getActivity()).isEncryptionEnable())
            {
                // IF sync file + sync activate + data protection
                ActionManager.actionView(this, syncFile, new ActionManagerListener()
                {
                    @Override
                    public void onActivityNotFoundException(ActivityNotFoundException e)
                    {
                        OpenAsDialogFragment.newInstance(syncFile).show(getActivity().getFragmentManager(),
                                OpenAsDialogFragment.TAG);
                    }
                });
            }
            else
            {
                // If sync file + sync activate
                ActionManager.openIn(this, syncFile, MimeTypeManager.getMIMEType(syncFile.getName()),
                        PublicIntent.REQUESTCODE_SAVE_BACK);
            }
        }
        else
        {
            // Other case
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
        }
    }
    
    /*private void openin()
    {
        Bundle b = new Bundle();
        DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentByTag(DetailsFragment.TAG);
        if (isSynced() && detailsFragment != null)
        {
            File syncFile = getSyncFile();
            long datetime = syncFile.lastModified();
            detailsFragment.setDownloadDateTime(new Date(datetime));
            ActionManager.openIn(detailsFragment, getSyncFile(), MimeTypeManager.getMIMEType(syncFile.getName()),
                    PublicIntent.REQUESTCODE_SAVE_BACK);
        }
        else
        {
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
        }
    }*/

    private boolean isSynced()
    {
        if (node.isFolder()) { return false; }
        Cursor favoriteCursor = getActivity().getContentResolver()
                .query(SynchroProvider.CONTENT_URI,
                        SynchroSchema.COLUMN_ALL,
                        SynchroSchema.COLUMN_NODE_ID + " LIKE '"
                                + NodeRefUtils.getCleanIdentifier(node.getIdentifier()) + "%'", null, null);
        return (favoriteCursor.getCount() == 1)
                && GeneralPreferences.hasActivateSync(getActivity(), SessionUtils.getAccount(getActivity()));
    }

    private File getSyncFile()
    {
        if (node.isFolder()) { return null; }
        return StorageManager.getSynchroFile(getActivity(), SessionUtils.getAccount(getActivity()), (Document) node);
    }
}

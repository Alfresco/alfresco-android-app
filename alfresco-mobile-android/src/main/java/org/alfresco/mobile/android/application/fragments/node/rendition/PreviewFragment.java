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
package org.alfresco.mobile.android.application.fragments.node.rendition;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.NodeImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.details.PagerNodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.download.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.download.DownloadDocumentHolder;
import org.alfresco.mobile.android.application.fragments.utils.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.rendition.RenditionBuilder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.rendition.RenditionRequest;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

/**
 * @since 1.1
 * @author Jean Marie Pascal
 */
public class PreviewFragment extends AlfrescoFragment
{

    public static final String TAG = "PreviewFragment";

    public static final String ARGUMENT_NODE = "node";

    public static final String ARGUMENT_TOUCH_ENABLED = "touchEnabled";

    private File tempFile = null;

    private Node node;

    protected boolean isRestrictable = false;

    private boolean touchEnabled;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PreviewFragment()
    {
        requiredSession = true;
        checkSession = true;
        reportAtCreation = false;
    }

    protected static PreviewFragment newInstanceByTemplate(Bundle b)
    {
        PreviewFragment cbf = new PreviewFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            node = (Node) getArguments().get(ARGUMENT_NODE);
            if (getArguments().containsKey(ARGUMENT_TOUCH_ENABLED))
            {
                touchEnabled = getArguments().getBoolean(ARGUMENT_TOUCH_ENABLED);
            }
        }

        if (node != null && node instanceof NodeSyncPlaceHolder)
        {
            checkSession = false;
            requiredSession = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);

        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }

        setRootView(inflater.inflate(R.layout.app_preview, container, false));

        if (getSession() == null) { return getRootView(); }
        if (node == null) { return null; }

        // Detect if isRestrictable
        isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        ImageView iv = (ImageView) viewById(R.id.preview);
        int iconId = R.drawable.mime_folder;
        iv.setTag(viewById(R.id.preview_message));
        if (node.isDocument() && node instanceof NodeImpl)
        {
            iconId = MimeTypeManager.getInstance(getActivity()).getIcon(node.getName(), true);
            if (((Document) node).isLatestVersion())
            {
                ((ImageViewTouch) iv).setScaleEnabled(false);
                ((ImageViewTouch) iv).setDoubleTapEnabled(false);
                ((ImageViewTouch) iv).setDisplayType(DisplayType.NONE);

                RenditionBuilder request = RenditionManager.with(getActivity()).loadNode(node).placeHolder(iconId)
                        .rendition(RenditionRequest.RENDITION_PREVIEW);
                if (touchEnabled)
                {
                    request.touchViewEnable(true);
                }
                request.into(iv);
            }
        }
        else if (node.isDocument() && node instanceof NodeSyncPlaceHolder)
        {
            iv.setImageResource(MimeTypeManager.getInstance(getActivity()).getIcon(node.getName(), true));
        }
        else
        {
            iv.setImageResource(iconId);
        }

        return getRootView();
    }

    public void openin()
    {
        if (isRestrictable) { return; }

        Bundle b = new Bundle();

        // 3 cases
        SyncContentManager syncManager = SyncContentManager.getInstance(getActivity());
        AlfrescoAccount acc = SessionUtils.getAccount(getActivity());

        NodeDetailsFragment detailsFragment = (NodeDetailsFragment) getFragmentManager().findFragmentByTag(
                PagerNodeDetailsFragment.TAG);

        if (syncManager.isSynced(SessionUtils.getAccount(getActivity()), node))
        {
            final File syncFile = syncManager.getSyncFile(acc, node);
            if (syncFile == null) { return; }
            long datetime = syncFile.lastModified();
            detailsFragment.setDownloadDateTime(new Date(datetime));

            if (DataProtectionManager.getInstance(getActivity()).isEncryptionEnable())
            {
                // IF sync file + sync activate + data protection
                ActionUtils.actionView(this, syncFile, new ActionManagerListener()
                {
                    @Override
                    public void onActivityNotFoundException(ActivityNotFoundException e)
                    {
                        OpenAsDialogFragment.newInstance(syncFile).show(getActivity().getSupportFragmentManager(),
                                OpenAsDialogFragment.TAG);
                    }
                });
            }
            else
            {
                // If sync file + sync activate
                ActionUtils.openIn(detailsFragment, syncFile,
                        MimeTypeManager.getInstance(getActivity()).getMIMEType(syncFile.getName()));
            }
        }
        else
        {
            // Other case
            DownloadDocumentHolder.getInstance().setDocument(node);
            b.putBoolean(DownloadDialogFragment.ARGUMENT_DOCUMENT, true);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getActivity().getSupportFragmentManager(), DownloadDialogFragment.TAG);
        }
    }

    public File getTempFile()
    {
        return tempFile;
    }

    public void setTempFile(File tempFile)
    {
        this.tempFile = tempFile;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

        public Builder node(Node node)
        {
            extraConfiguration.putSerializable(ARGUMENT_NODE, node);
            return this;
        }

        public Builder touchEnable(boolean touchEnabled)
        {
            extraConfiguration.putBoolean(ARGUMENT_TOUCH_ENABLED, touchEnabled);
            return this;
        }
    }
}

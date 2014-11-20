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
package org.alfresco.mobile.android.application.fragments.node.details;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.download.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.PreviewFragment;
import org.alfresco.mobile.android.application.fragments.sync.EnableSyncDialogFragment;
import org.alfresco.mobile.android.application.fragments.sync.EnableSyncDialogFragment.OnSyncChangeListener;
import org.alfresco.mobile.android.application.fragments.utils.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.DataProtectionManagerImpl;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionEvent;
import org.alfresco.mobile.android.async.node.NodeRequest;
import org.alfresco.mobile.android.async.node.RetrieveNodeEvent;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeRequest;
import org.alfresco.mobile.android.async.node.like.LikeNodeEvent;
import org.alfresco.mobile.android.async.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentEvent;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.rendition.RenditionRequest;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

public abstract class NodeDetailsFragment extends AlfrescoFragment implements DetailsFragmentTemplate
{
    private static final String TAG = NodeDetailsFragment.class.getName();

    protected Folder parentNode;

    protected Node node;

    protected String nodeIdentifier;

    protected boolean isRestrictable = false;

    protected boolean favoriteOffline = false;

    protected RenditionManagerImpl renditionManager;

    protected Date downloadDateTime;

    protected int layoutId;

    protected PreviewFragment replacementPreviewFragment = null;

    protected File tempFile = null;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeDetailsFragment()
    {
        requiredSession = true;
        checkSession = true;
        layoutId = R.layout.app_details;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Retrieve arguments
        if (getArguments() != null)
        {
            node = (Node) getArguments().get(ARGUMENT_NODE);
            nodeIdentifier = (String) getArguments().get(ARGUMENT_NODE_ID);
            parentNode = (Folder) getArguments().get(ARGUMENT_FOLDER_PARENT);
            favoriteOffline = getArguments().containsKey(ARGUMENT_FAVORITE);
            if (favoriteOffline)
            {
                checkSession = false;
                requiredSession = false;
            }

        }
        // If no Node we display nothing.
        if (node == null && nodeIdentifier == null) { return; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Define the View
        setRootView(inflater.inflate(layoutId, container, false));
        if (!getArguments().containsKey(ARGUMENT_FAVORITE))
        {
            if (getSession() == null) { return getRootView(); }
        }

        // If node not present we display nothing.
        if (node == null && nodeIdentifier == null)
        {
            displayEmptyView();
        }
        // If node present we display everything
        else if (node != null)
        {
            // Detect if isRestrictable
            isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);
            displayParts();
        }
        // If node Identifier we need to retrieve the Node Object first.
        else if (nodeIdentifier != null)
        {
            if (eventBusRequired)
            {
                EventBusManager.getInstance().register(this);
            }
            Operator.with(getActivity()).load(new NodeRequest.Builder(null, nodeIdentifier));
            displayLoading();
        }

        return getRootView();
    }

    @Override
    public void onStop()
    {
        getActivity().invalidateOptionsMenu();
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).setCurrentNode(null);
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        File tmpFile = null;
        boolean isSynced = FavoritesSyncManager.getInstance(getActivity()).isSynced(
                SessionUtils.getAccount(getActivity()), node);
        boolean modified = false;
        Date d = null;

        switch (requestCode)
        {
        // Save Back : When a file has been opened by 3rd party app.
            case RequestCode.SAVE_BACK:
            case RequestCode.DECRYPTED:
                // File opened when user tap the preview
                // Retrieve the File
                if (replacementPreviewFragment != null)
                {
                    tempFile = replacementPreviewFragment.getTempFile();
                }

                // Check if SYNC is ON
                if (isSynced)
                {
                    // We use the sync file stored locally
                    tmpFile = FavoritesSyncManager.getInstance(getActivity()).getSyncFile(
                            SessionUtils.getAccount(getActivity()), node);
                }
                else
                {
                    // We retrieve the temporary file
                    tmpFile = (tempFile != null ? tempFile : NodeActions.getTempFile(getActivity(), node));
                }

                // Keep the file reference final
                final File dlFile = tmpFile;

                // Check if the file has been modified (lastmodificationDate)
                long datetime = dlFile.lastModified();
                d = new Date(datetime);
                modified = (d != null && downloadDateTime != null) ? d.after(downloadDateTime) : false;

                if (node instanceof NodeSyncPlaceHolder && modified)
                {
                    // Offline mode
                    if (isSynced)
                    {
                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();

                        int operationStatut = FavoriteSyncStatus.STATUS_PENDING;
                        if (requestCode == RequestCode.DECRYPTED)
                        {
                            operationStatut = FavoriteSyncStatus.STATUS_MODIFIED;
                        }

                        cValues.put(FavoritesSyncSchema.COLUMN_STATUS, operationStatut);
                        getActivity().getContentResolver().update(
                                FavoritesSyncManager.getInstance(getActivity()).getUri(
                                        SessionUtils.getAccount(getActivity()), node.getIdentifier()), cValues, null,
                                null);
                    }

                    // Encrypt sync file if necessary
                    AlfrescoStorageManager.getInstance(getActivity()).manageFile(dlFile);
                }
                else if (modified && getSession() != null
                        && getSession().getServiceRegistry().getDocumentFolderService().getPermissions(node) != null
                        && getSession().getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
                {
                    // File modified + Sync File
                    if (isSynced)
                    {
                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();

                        int operationStatut = FavoriteSyncStatus.STATUS_PENDING;
                        if (requestCode == RequestCode.DECRYPTED)
                        {
                            operationStatut = FavoriteSyncStatus.STATUS_MODIFIED;
                        }

                        cValues.put(FavoritesSyncSchema.COLUMN_STATUS, operationStatut);
                        getActivity().getContentResolver().update(
                                FavoritesSyncManager.getInstance(getActivity()).getUri(
                                        SessionUtils.getAccount(getActivity()), node.getIdentifier()), cValues, null,
                                null);

                        // Sync if it's possible.
                        if (FavoritesSyncManager.getInstance(getActivity()).canSync(
                                SessionUtils.getAccount(getActivity())))
                        {
                            FavoritesSyncManager.getInstance(getActivity()).sync(
                                    SessionUtils.getAccount(getActivity()), node);
                        }
                    }
                    else
                    {
                        // File is temporary (after dl from server)
                        // We request the user if he wants to save back
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.save_back);
                        builder.setMessage(String.format(getResources().getString(R.string.save_back_description),
                                node.getName()));
                        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int item)
                            {
                                update(dlFile);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int item)
                            {
                                DataProtectionManager.getInstance(getActivity()).checkEncrypt(
                                        SessionUtils.getAccount(getActivity()), dlFile);
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                else
                {
                    // File with no modification
                    // Encrypt sync file if necessary
                    // Delete otherwise
                    AlfrescoStorageManager.getInstance(getActivity()).manageFile(dlFile);
                }
                break;
            case RequestCode.FILEPICKER:
                if (data != null && PrivateIntent.ACTION_PICK_FILE.equals(data.getAction()))
                {
                    ActionUtils.actionPickFile(getFragmentManager().findFragmentByTag(TAG), RequestCode.FILEPICKER);
                }
                else if (data != null && data.getData() != null)
                {
                    String tmpPath = ActionUtils.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        File f = new File(tmpPath);
                        update(f);
                    }
                    else
                    {
                        // Error case : Unable to find the file path associated
                        // to user pick.
                        // Sample : Picasa image case
                        ActionUtils.actionDisplayError(NodeDetailsFragment.this, new AlfrescoAppException(
                                getString(R.string.error_unknown_filepath), true));
                    }
                }
                break;
            default:
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayParts()
    {
        displayData();
        if (node instanceof Document || node instanceof Folder)
        {
            displayParts(node);
        }
        else if (node instanceof NodeSyncPlaceHolder)
        {
            displayPartsOffline((NodeSyncPlaceHolder) node);
        }
        getActivity().invalidateOptionsMenu();
    }

    protected void displayParts(Node refreshedNode)
    {
        display(refreshedNode);
    }

    protected void displayPartsOffline(NodeSyncPlaceHolder refreshedNode)
    {

    }

    protected void display(Node refreshedNode)
    {
        // Detect if restrictable
        isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);
        renditionManager = RenditionManagerImpl.getInstance(getActivity());

        // Display all parts
        displayTabs();
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            displayHeader();
            displayToolsBar();
        }
    }

    protected void displayHeader()
    {
        TextView tv = (TextView) viewById(R.id.title);
        tv.setText(node.getName());
        tv = (TextView) viewById(R.id.details);
        tv.setText(Formatter.createContentBottomText(getActivity(), node, true));

        if (isRestrictable)
        {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_encrypt, 0);
        }

        // Preview + Thumbnail
        displayIcon(node, R.drawable.mime_folder, (ImageView) viewById(R.id.icon), false);
    }

    protected void displayToolsBar()
    {
        // BUTTONS
        ImageView b = (ImageView) viewById(R.id.action_openin);
        if (node.isDocument() && ((DocumentImpl) node).hasAllowableAction(Action.CAN_GET_CONTENT_STREAM.value())
                && ((Document) node).getContentStreamLength() > 0 && !isRestrictable)
        {
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openin();
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }

        b = (ImageView) viewById(R.id.action_geolocation);
        if (node.isDocument() && node.hasAspect(ContentModel.ASPECT_GEOGRAPHIC))
        {
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ActionUtils.actionShowMap(NodeDetailsFragment.this, node.getName(),
                            node.getProperty(ContentModel.PROP_LATITUDE).getValue().toString(),
                            node.getProperty(ContentModel.PROP_LONGITUDE).getValue().toString());
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }

        b = (ImageView) viewById(R.id.like);
        if (getSession() != null && getSession().getRepositoryInfo() != null
                && getSession().getRepositoryInfo().getCapabilities() != null
                && getSession().getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            isLiked(b);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    like(v);
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
            if (viewById(R.id.like_progress) != null)
            {
                viewById(R.id.like_progress).setVisibility(View.GONE);
            }
        }

        // BUTTONS
        b = (ImageView) viewById(R.id.action_favorite);
        if (!isRestrictable)
        {
            isFavorite(b);
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    favorite(v);
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
            viewById(R.id.favorite_progress).setVisibility(View.GONE);
        }

        b = (ImageView) viewById(R.id.action_share);
        if (node.isDocument() && !isRestrictable)
        {
            b.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    share();
                }
            });
        }
        else
        {
            b.setVisibility(View.GONE);
        }
    }

    protected void displayTabs()
    {

    }

    protected void displayPreview()
    {
        displayIcon(node, R.drawable.mime_256_folder, (ImageView) viewById(R.id.preview), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void displayData()
    {
        hide(R.id.empty);
        hide(R.id.progressbar);
        show(R.id.properties_details);
    }

    protected void displayEmptyView()
    {
        show(R.id.empty);
        hide(R.id.progressbar);
        hide(R.id.properties_details);
    }

    protected void displayLoading()
    {
        show(R.id.progressbar);
        hide(R.id.properties_details);
        hide(R.id.empty);
    }

    private void displayIcon(Node node, int defaultIconId, ImageView iv, boolean isLarge)
    {
        if (iv == null) { return; }

        int iconId = defaultIconId;
        if (node.isDocument())
        {
            MimeType mime = MimeTypeManager.getInstance(getActivity()).getMimetype(node.getName());
            iconId = MimeTypeManager.getInstance(getActivity()).getIcon(node.getName(), isLarge);
            if (((Document) node).isLatestVersion())
            {
                if (isLarge)
                {
                    RenditionManager.with(getActivity()).loadNode(node).placeHolder(iconId)
                            .rendition(RenditionRequest.RENDITION_PREVIEW).into(iv);
                }
                else
                {
                    RenditionManager.with(getActivity()).loadNode(node).placeHolder(iconId).into(iv);
                }
            }
            else
            {
                iv.setImageResource(iconId);
            }
            AccessibilityUtils.addContentDescription(iv,
                    mime != null ? mime.getDescription() : ((Document) node).getContentStreamMimeType());

            if (!isRestrictable && !AccessibilityUtils.isEnabled(getActivity()))
            {
                iv.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        openin();
                    }
                });
            }
        }
        else
        {
            iv.setImageResource(defaultIconId);
            AccessibilityUtils.addContentDescription(iv, R.string.mime_folder);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void share()
    {
        if (node.isFolder()) { return; }

        if (node instanceof NodeSyncPlaceHolder)
        {
            FavoritesSyncManager syncManager = FavoritesSyncManager.getInstance(getActivity());
            AlfrescoAccount acc = SessionUtils.getAccount(getActivity());
            final File syncFile = syncManager.getSyncFile(acc, node);
            if (syncFile == null || !syncFile.exists())
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                        getString(R.string.sync_document_not_available));
                return;
            }
            long datetime = syncFile.lastModified();
            setDownloadDateTime(new Date(datetime));

            if (DataProtectionManager.getInstance(getActivity()).isEncryptionEnable())
            {
                // IF sync file + sync activate + data protection
                ActionUtils.actionSend(getActivity(), syncFile, new ActionManagerListener()
                {
                    @Override
                    public void onActivityNotFoundException(ActivityNotFoundException e)
                    {
                    }
                });
            }
            else
            {
                // If sync file + sync activate
                ActionUtils.actionSend(getActivity(), syncFile);
            }
            return;
        }

        if (getSession() instanceof RepositorySession)
        {
            // Only sharing as attachment is allowed when we're not on a cloud
            // account
            Bundle b = new Bundle();
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getActivity().getFragmentManager(), DownloadDialogFragment.TAG);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.link_or_attach);

            builder.setPositiveButton(R.string.full_attachment, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    Bundle b = new Bundle();
                    b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
                    b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
                    DialogFragment frag = new DownloadDialogFragment();
                    frag.setArguments(b);
                    frag.show(getFragmentManager(), DownloadDialogFragment.TAG);
                }
            });
            builder.setNegativeButton(R.string.link_to_repo, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    if (parentNode != null)
                    {
                        String path = parentNode.getPropertyValue(PropertyIds.PATH);
                        if (path.length() > 0)
                        {
                            if (path.startsWith("/Sites/"))
                            {
                                // Get past the '/Sites/'
                                String sub1 = path.substring(7);
                                // Find end of site name
                                int idx = sub1.indexOf('/');
                                if (idx == -1)
                                {
                                    idx = sub1.length();
                                }
                                String siteName = sub1.substring(0, idx);
                                String nodeID = NodeRefUtils.getCleanIdentifier(node.getIdentifier());
                                String fullPath = String.format(getString(R.string.cloud_share_url),
                                        ((CloudSession) getSession()).getNetwork().getIdentifier(), siteName, nodeID);
                                ActionUtils.actionShareLink(NodeDetailsFragment.this, fullPath);
                            }
                            else
                            {
                                Log.i(getString(R.string.app_name), "Site path not as expected: no /sites/");
                            }
                        }
                        else
                        {
                            Log.i(getString(R.string.app_name), "Site path not as expected: no parent path");
                        }
                    }
                    else
                    {
                        Log.i(getString(R.string.app_name), "Site path not as expected: No parent folder");
                    }

                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void openin()
    {
        if (isRestrictable) { return; }

        Bundle b = new Bundle();

        // 3 cases
        FavoritesSyncManager syncManager = FavoritesSyncManager.getInstance(getActivity());
        AlfrescoAccount acc = SessionUtils.getAccount(getActivity());

        if (syncManager.isSynced(SessionUtils.getAccount(getActivity()), node))
        {
            final File syncFile = syncManager.getSyncFile(acc, node);
            if (syncFile == null || !syncFile.exists())
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(
                        getString(R.string.sync_document_not_available));
                return;
            }
            long datetime = syncFile.lastModified();
            setDownloadDateTime(new Date(datetime));

            if (DataProtectionManager.getInstance(getActivity()).isEncryptionEnable())
            {
                // IF sync file + sync activate + data protection
                ActionUtils.actionView(this, syncFile, new ActionManagerListener()
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
                ActionUtils.openIn(this, syncFile,
                        MimeTypeManager.getInstance(getActivity()).getMIMEType(syncFile.getName()),
                        RequestCode.SAVE_BACK);
            }
        }
        else
        {
            // Other case
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, (Document) node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_OPEN);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getActivity().getFragmentManager(), DownloadDialogFragment.TAG);
        }
    }

    public void setDownloadDateTime(Date downloadDateTime)
    {
        this.downloadDateTime = downloadDateTime;
    }

    public void download()
    {
        if (isRestrictable) { return; }

        if (node instanceof Document)
        {
            NodeActions.download(getActivity(), parentNode, (Document) node);
        }
    }

    public void update()
    {
        ActionUtils.actionPickFile(this, RequestCode.FILEPICKER);
    }

    public void update(File f)
    {
        Operator.with(getActivity(), getAccount()).load(
                new UpdateContentRequest.Builder(parentNode, (Document) node, new ContentFileProgressImpl(f))
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
    }

    public void delete()
    {
        NodeActions.delete(getActivity(), this, node);
    }

    public void edit()
    {
        NodeActions.edit(getActivity(), parentNode, node);
    }

    public void like(View v)
    {
        viewById(R.id.like_progress).setVisibility(View.VISIBLE);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(
                new LikeNodeRequest.Builder(node, false));

    }

    public void isLiked(View v)
    {
        viewById(R.id.like_progress).setVisibility(View.VISIBLE);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(
                new LikeNodeRequest.Builder(node, true));
    }

    public void isFavorite(View v)
    {
        viewById(R.id.favorite_progress).setVisibility(View.VISIBLE);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(
                new FavoritedNodeRequest.Builder(node));
    }

    public void favorite(View v)
    {
        if (isRestrictable) { return; }

        if (!FavoritesSyncManager.getInstance(getActivity()).hasDisplayedActivateSync())
        {
            EnableSyncDialogFragment.newInstance(new OnSyncChangeListener()
            {
                @Override
                public void onPositive()
                {
                    FavoritesSyncManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()));
                    FavoritesSyncManager.getInstance(getActivity()).setActivateSync(true);
                }

                @Override
                public void onNegative()
                {
                    FavoritesSyncManager.getInstance(getActivity()).setActivateSync(false);
                }
            }).show(getActivity().getFragmentManager(), EnableSyncDialogFragment.TAG);
            FavoritesSyncManager.getInstance(getActivity()).setDisplayActivateSync(true);
        }

        viewById(R.id.favorite_progress).setVisibility(View.VISIBLE);
        if (parentNode != null && node != null)
        {
            Operator.with(getActivity(), getAccount()).load(new FavoriteNodeRequest.Builder(parentNode, node));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public static void getMenu(AlfrescoSession session, Activity activity, Menu menu, Node node)
    {
        MenuItem mi;

        if (node == null) { return; }
        if (node instanceof NodeSyncPlaceHolder) { return; }

        boolean isRestrict = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0 && !isRestrict)
            {
                mi = menu.add(Menu.NONE, R.id.menu_action_download, Menu.FIRST, R.string.download);
                mi.setIcon(R.drawable.ic_download_dark);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((Document) node).isLatestVersion()
                    && ((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value()))
            {
                mi = menu.add(Menu.NONE, R.id.menu_action_update, Menu.FIRST + 130, R.string.update);
                mi.setIcon(R.drawable.ic_upload);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (!(session instanceof CloudSession))
            {
                mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST + 500, R.string.process_start_review);
                mi.setIcon(R.drawable.ic_start_review);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST + 10, R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (session.getServiceRegistry().getDocumentFolderService().getPermissions(node).canDelete())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public void getMenu(Menu menu)
    {
        getMenu(getSession(), getActivity(), menu, node);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_action_download:
                download();
                return true;
            case R.id.menu_action_share:
                share();
                return true;
            case R.id.menu_action_open:
                openin();
                return true;
            case R.id.menu_action_update:
                Intent i = new Intent(PrivateIntent.ACTION_PICK_FILE, null, getActivity(),
                        PublicDispatcherActivity.class);
                i.putExtra(PrivateIntent.EXTRA_FOLDER, AlfrescoStorageManager.getInstance(getActivity())
                        .getDownloadFolder(getAccount()));
                i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                startActivityForResult(i, RequestCode.FILEPICKER);
            case R.id.menu_action_edit:
                edit();
                return true;
            case R.id.menu_action_delete:
                delete();
                return true;
            case R.id.menu_workflow_add:
                Intent in = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(),
                        PrivateDialogActivity.class);
                Document doc = (Document) getCurrentNode();
                in.putExtra(PrivateIntent.EXTRA_DOCUMENT, (Serializable) doc);
                in.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                startActivity(in);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Node getCurrentNode()
    {
        return node;
    }

    public Folder getParentNode()
    {
        return parentNode;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(RetrieveNodeEvent event)
    {
        if (event.hasException)
        {
            displayEmptyView();
            ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
        }
        else if (getActivity() != null)
        {
            node = event.data;
            parentNode = event.parentFolder;
            displayParts();
        }
    }

    @Subscribe
    public void onLikeEvent(LikeNodeEvent event)
    {
        View progressView = viewById(R.id.like_progress);
        ImageView likeButton = (ImageView) viewById(R.id.like);
        if (likeButton == null) { return; }

        if (progressView != null)
        {
            progressView.setVisibility(View.GONE);
        }
        if (event.data == null)
        {
            Log.e(TAG, Log.getStackTraceString(event.exception));
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.error_retrieve_likes);
        }
        else if (event.data)
        {
            likeButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_like));
            AccessibilityUtils.addContentDescription(likeButton, R.string.unlike);
        }
        else
        {
            likeButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_unlike));
            AccessibilityUtils.addContentDescription(likeButton, R.string.like);
        }
    }

    @Subscribe
    public void onFavoriteEvent(FavoritedNodeEvent event)
    {
        View progressView = viewById(R.id.favorite_progress);
        ImageView favoriteButton = (ImageView) viewById(R.id.action_favorite);
        if (favoriteButton == null) { return; }

        if (progressView != null)
        {
            progressView.setVisibility(View.GONE);
        }
        if (event.data == null)
        {
            Log.e(TAG, Log.getStackTraceString(event.exception));
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.error_retrieve_favorite);
        }
        else if (event.data)
        {
            favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_favorite_dark));
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.unfavorite);
        }
        else
        {
            favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_unfavorite_dark));
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.favorite);
        }
    }

    @Subscribe
    public void onFavoriteEvent(FavoriteNodeEvent event)
    {
        View progressView = viewById(R.id.favorite_progress);
        ImageView favoriteButton = (ImageView) viewById(R.id.action_favorite);
        if (favoriteButton == null) { return; }

        if (progressView != null)
        {
            progressView.setVisibility(View.GONE);
        }
        if (event.data == null)
        {
            Log.e(TAG, Log.getStackTraceString(event.exception));
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.error_retrieve_favorite);
        }
        else if (event.data)
        {
            favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_favorite_dark));
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.unfavorite);
        }
        else
        {
            favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_unfavorite_dark));
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.favorite);
        }
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        Node updatedNode = event.data;

        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().getFragmentManager().popBackStack(NodeDetailsFragment.getDetailsTag(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if (((DocumentFolderBrowserFragment) getActivity().getFragmentManager().findFragmentByTag(
                DocumentFolderBrowserFragment.TAG)) != null)
        {
            ((DocumentFolderBrowserFragment) getActivity().getFragmentManager().findFragmentByTag(
                    DocumentFolderBrowserFragment.TAG)).select(updatedNode);
        }
        NodeDetailsFragment.with(getActivity()).node(updatedNode).parentFolder(event.parentFolder).display();

        AlfrescoNotificationManager.getInstance(getActivity()).showToast(
                String.format(getResources().getString(R.string.update_sucess), event.initialNode.getName()));
    }

    @Subscribe
    public void onContentUpdated(UpdateContentEvent event)
    {
        Node updatedNode = event.data;

        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().getFragmentManager().popBackStack(NodeDetailsFragment.getDetailsTag(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if (((DocumentFolderBrowserFragment) getActivity().getFragmentManager().findFragmentByTag(
                DocumentFolderBrowserFragment.TAG)) != null)
        {
            ((DocumentFolderBrowserFragment) getActivity().getFragmentManager().findFragmentByTag(
                    DocumentFolderBrowserFragment.TAG)).select(updatedNode);
        }
        NodeDetailsFragment.with(getActivity()).node(updatedNode).parentFolder(event.parentFolder).display();

        AlfrescoNotificationManager.getInstance(getActivity()).showToast(
                String.format(getResources().getString(R.string.update_sucess), event.node.getName()));
    }

    @Subscribe
    public void onNodeDeleted(DeleteNodeEvent event)
    {
        ((MainActivity) getActivity()).setCurrentNode(null);
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(getDetailsTag());
        }
        else
        {
            getFragmentManager().popBackStack(getDetailsTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        return;
    }

    @Subscribe
    public void onFileProtectionEvent(FileProtectionEvent event)
    {
        if (event.hasException) { return; }
        if (getFragment(WaitingDialogFragment.TAG) != null)
        {
            ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
        }
        if (!event.encryptionAction)
        {
            int actionIntent = event.intentAction;
            if (actionIntent == DataProtectionManager.ACTION_NONE || actionIntent == 0) { return; }

            File f = event.protectedFile;
            downloadDateTime = new Date(f.lastModified());
            DataProtectionManagerImpl.getInstance(getActivity()).executeAction(this, actionIntent, f);
        }
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static String getDetailsTag()
    {
        if (AndroidVersion.isJBMR1OrAbove())
        {
            return PagerNodeDetailsFragment.TAG;
        }
        else
        {
            return TabsNodeDetailsFragment.TAG;
        }
    }

    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            this.extraConfiguration = new Bundle();
            this.menuIconId = R.drawable.ic_repository_light;
            this.menuTitleId = R.string.menu_browse_root;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder nodeId(String nodeIdentifier)
        {
            extraConfiguration.putString(ARGUMENT_NODE_ID, nodeIdentifier);
            return this;
        }

        public Builder node(Node node)
        {
            extraConfiguration.putSerializable(ARGUMENT_NODE, node);
            return this;
        }

        public Builder parentFolder(Folder parentFolder)
        {
            extraConfiguration.putSerializable(ARGUMENT_FOLDER_PARENT, parentFolder);
            return this;
        }

        public Builder path(String nodePath)
        {
            extraConfiguration.putSerializable(ARGUMENT_PATH, nodePath);
            return this;
        }

        public Builder isFavorite(Boolean isFavorite)
        {
            extraConfiguration.putSerializable(ARGUMENT_FAVORITE, isFavorite);
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            if (AndroidVersion.isJBMR1OrAbove())
            {
                return PagerNodeDetailsFragment.newInstanceByTemplate(b);
            }
            else
            {
                return TabsNodeDetailsFragment.newInstanceByTemplate(b);
            }
        }
    }
}

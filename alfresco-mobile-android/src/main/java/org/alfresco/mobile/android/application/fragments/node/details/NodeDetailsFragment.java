/*******************************************************************************
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
import org.alfresco.mobile.android.api.model.config.ConfigTypeIds;
import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.configuration.model.view.NodeDetailsConfigModel;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.download.DownloadDialogFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.PreviewFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.fragments.utils.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.application.managers.DataProtectionManagerImpl;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionEvent;
import org.alfresco.mobile.android.async.node.NodeByPathRequest;
import org.alfresco.mobile.android.async.node.NodeRequest;
import org.alfresco.mobile.android.async.node.RetrieveNodeEvent;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeEvent;
import org.alfresco.mobile.android.async.node.download.DownloadEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeRequest;
import org.alfresco.mobile.android.async.node.like.LikeNodeEvent;
import org.alfresco.mobile.android.async.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.async.node.sync.SyncNodeEvent;
import org.alfresco.mobile.android.async.node.sync.SyncNodeRequest;
import org.alfresco.mobile.android.async.node.update.UpdateContentEvent;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mdm.MDMManager;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentScanEvent;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolderFormatter;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.rendition.RenditionRequest;
import org.alfresco.mobile.android.ui.template.ViewTemplate;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
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

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public abstract class NodeDetailsFragment extends AlfrescoFragment implements DetailsFragmentTemplate
{
    private static final String TAG = NodeDetailsFragment.class.getName();

    protected Folder parentNode;

    protected Node node;

    protected String nodePath;

    protected String nodeIdentifier;

    protected boolean isRestrictable = false;

    protected boolean favoriteOffline = false;

    protected RenditionManagerImpl renditionManager;

    protected Date downloadDateTime;

    protected int layoutId;

    protected PreviewFragment replacementPreviewFragment = null;

    protected File tempFile = null;

    protected String shareUrl = null;

    protected boolean isSynced = false;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public NodeDetailsFragment()
    {
        requiredSession = false;
        checkSession = false;
        layoutId = R.layout.app_details;
        reportAtCreation = false;
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
            nodePath = (String) getArguments().get(ARGUMENT_PATH);
            nodeIdentifier = (String) getArguments().get(ARGUMENT_NODE_ID);
            parentNode = (Folder) getArguments().get(ARGUMENT_FOLDER_PARENT);
            favoriteOffline = getArguments().containsKey(ARGUMENT_FAVORITE);
            title = (String) getArguments().get(ViewTemplate.ARGUMENT_LABEL);
            if (favoriteOffline)
            {
                checkSession = false;
                requiredSession = false;
            }

        }
        // If no Node we display nothing.
        if (node == null && nodeIdentifier == null && TextUtils.isEmpty(nodePath)) { return; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Define the View
        setRootView(inflater.inflate(layoutId, container, false));

        displayLoading();

        // If node not present we display nothing.
        if (node == null && nodeIdentifier == null && TextUtils.isEmpty(nodePath))
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
        else if (nodePath != null)
        {
            if (eventBusRequired)
            {
                EventBusManager.getInstance().register(this);
            }
            Operator.with(getActivity()).load(new NodeByPathRequest.Builder(nodePath));
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
        super.onActivityResult(requestCode, resultCode, data);
        File tmpFile = null;
        isSynced = SyncContentManager.getInstance(getActivity()).isSynced(SessionUtils.getAccount(getActivity()), node);
        downloadDateTime = getDownloadDateTime();
        boolean modified = false;
        Date d = null;

        switch (requestCode)
        {
            // Save Back : When a file has been opened by 3rd party app.
            case RequestCode.SAVE_BACK:
            case RequestCode.DECRYPTED:
                removeDownloadDateTime();
                // if (isSynced) { return; }

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
                    tmpFile = SyncContentManager.getInstance(getActivity())
                            .getSyncFile(SessionUtils.getAccount(getActivity()), node);
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
                modified = (d != null && downloadDateTime != null) && d.after(downloadDateTime);

                if (node instanceof NodeSyncPlaceHolder && modified)
                {
                    // Offline mode
                    if (isSynced)
                    {
                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();

                        int operationStatut = SyncContentStatus.STATUS_PENDING;
                        if (requestCode == RequestCode.DECRYPTED)
                        {
                            operationStatut = SyncContentStatus.STATUS_MODIFIED;
                        }

                        cValues.put(SyncContentSchema.COLUMN_STATUS, operationStatut);
                        getActivity().getContentResolver()
                                .update(SyncContentManager.getInstance(getActivity())
                                        .getUri(SessionUtils.getAccount(getActivity()), node.getIdentifier()), cValues,
                                null, null);
                    }

                    // Encrypt sync file if necessary
                    AlfrescoStorageManager.getInstance(getActivity()).manageFile(dlFile);
                }
                else if (modified && getSession() != null && ConfigurableActionHelper.isVisible(getActivity(),
                        getAccount(), getSession(), node, ConfigurableActionHelper.ACTION_NODE_EDIT))
                {
                    // File modified + Sync File
                    if (isSynced)
                    {
                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();

                        int operationStatut = SyncContentStatus.STATUS_PENDING;
                        if (requestCode == RequestCode.DECRYPTED)
                        {
                            operationStatut = SyncContentStatus.STATUS_MODIFIED;
                        }

                        cValues.put(SyncContentSchema.COLUMN_STATUS, operationStatut);
                        getActivity().getContentResolver()
                                .update(SyncContentManager.getInstance(getActivity())
                                        .getUri(SessionUtils.getAccount(getActivity()), node.getIdentifier()), cValues,
                                null, null);

                        // Sync if it's possible.
                        if (SyncContentManager.getInstance(getActivity())
                                .canSync(SessionUtils.getAccount(getActivity())))
                        {
                            SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()),
                                    node.getIdentifier());
                        }
                    }
                    else
                    {
                        // File is temporary (after dl from server)
                        // We request the user if he wants to save back
                        new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                                .title(R.string.save_back)
                                .content(String.format(getResources().getString(R.string.save_back_description),
                                        node.getName()))
                                .positiveText(R.string.confirm).negativeText(R.string.cancel)
                                .callback(new MaterialDialog.ButtonCallback()
                                {
                                    @Override
                                    public void onPositive(MaterialDialog dialog)
                                    {
                                        update(dlFile);
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog)
                                    {
                                        DataProtectionManager.getInstance(getActivity())
                                                .checkEncrypt(SessionUtils.getAccount(getActivity()), dlFile);
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                }
                else
                {
                    DataProtectionManager.getInstance(getActivity())
                            .checkEncrypt(SessionUtils.getAccount(getActivity()), dlFile);

                    if (isSynced)
                    {
                        // Update statut of the sync reference
                        ContentValues cValues = new ContentValues();
                        cValues.put(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP, dlFile.lastModified());
                        getActivity().getContentResolver()
                                .update(SyncContentManager.getInstance(getActivity())
                                        .getUri(SessionUtils.getAccount(getActivity()), node.getIdentifier()), cValues,
                                null, null);
                    }

                    // File with no modification
                    // Encrypt sync file if necessary
                    // Delete otherwise
                    AlfrescoStorageManager.getInstance(getActivity()).manageFile(dlFile);
                }
                break;
            case RequestCode.FILEPICKER:
                if (data != null && PrivateIntent.ACTION_PICK_FILE.equals(data.getAction()))
                {
                    ActionUtils.actionPickFile(this.getParentFragment(),
                            RequestCode.FILEPICKER);
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
                        ActionUtils.actionDisplayError(NodeDetailsFragment.this,
                                new AlfrescoAppException(getString(R.string.error_unknown_filepath), true));
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
        if (node == null)
        {
            displayEmptyView();
            ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
            return;
        }

        if (node instanceof Document || node instanceof Folder)
        {
            displayParts(node);
        }
        else if (node instanceof NodeSyncPlaceHolder)
        {
            displayPartsOffline((NodeSyncPlaceHolder) node);
        }
        getActivity().invalidateOptionsMenu();
        displayData();
    }

    protected void displayParts(Node refreshedNode)
    {
        display(refreshedNode);
    }

    protected void displayPartsOffline(NodeSyncPlaceHolder refreshedNode)
    {
        display(refreshedNode);
    }

    protected void display(Node refreshedNode)
    {
        // Detect if restrictable
        isRestrictable = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);
        renditionManager = RenditionManagerImpl.getInstance(getActivity());

        // Display all parts
        displayTabs();
        if (!getResources().getBoolean(R.bool.fr_details_summary))
        {
            displayHeader();
            displayToolsBar();
        }
    }

    protected void displayHeader()
    {
        String topText = node.getName();
        String bottomText = null;
        if (node instanceof NodeSyncPlaceHolder)
        {
            bottomText = NodeSyncPlaceHolderFormatter.createContentBottomText(getActivity(), (NodeSyncPlaceHolder) node,
                    true);
        }
        else if (node.isDocument())
        {
            bottomText = Formatter.createContentBottomText(getActivity(), node, true);
        }
        else if (node.isFolder() && node.getPropertyValue(PropertyIds.PATH) != null)
        {
            bottomText = UIUtils.getParentDirPath((String) node.getPropertyValue(PropertyIds.PATH));
        }

        View v = viewById(R.id.details_header);
        if (v == null) { return; }
        TwoLinesViewHolder vh = HolderUtils.configure(v, topText, bottomText, -1);
        vh.topText.setId(UIUtils.generateViewId());
        vh.bottomText.setId(UIUtils.generateViewId());

        if (isRestrictable)
        {
            vh.bottomText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_encrypt, 0);
        }

        if (node.isFolder())
        {
            vh.bottomText.setEllipsize(TextUtils.TruncateAt.START);
        }

        // Thumbnail only for tablet
        displayRendition(node, R.drawable.mime_folder, vh.icon, false);
        vh.icon.setVisibility(View.VISIBLE);
    }

    protected void displayToolsBar()
    {
        // BUTTONS
        ImageView b = (ImageView) viewById(R.id.action_openin);
        if (b == null) { return; }
        if ((node instanceof Document && ((Document) node).getContentStreamLength() > 0 && !isRestrictable)
                || (node instanceof NodeSyncPlaceHolder && !isRestrictable))
        {
            // Tablet : Header thumbnail must be clickable
            if (viewById(R.id.icon) != null)
            {
                viewById(R.id.icon).setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        openin();
                    }
                });
            }

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
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_OPEN);

        b = (ImageView) viewById(R.id.action_like);
        if (node instanceof NodeSyncPlaceHolder)
        {
            b.setVisibility(View.GONE);
            viewById(R.id.like_progress).setVisibility(View.GONE);
        }
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
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_LIKE);

        // BUTTONS
        b = (ImageView) viewById(R.id.action_favorite);
        if (node instanceof NodeSyncPlaceHolder)
        {
            b.setVisibility(View.GONE);
            viewById(R.id.favorite_progress).setVisibility(View.GONE);
        }
        else if (!isRestrictable)
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
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_FAVORITE);

        // SYNC
        b = (ImageView) viewById(R.id.action_sync);

        if (node instanceof NodeSyncPlaceHolder)
        {
            b.setVisibility(View.VISIBLE);
            b.setImageResource(R.drawable.ic_synced_dark);
        }
        else if (SyncContentManager.getInstance(getActivity()).hasActivateSync(getAccount()))
        {
            isSynced = (node.isFolder())
                    ? SyncContentManager.getInstance(getActivity()).isRootSynced(getAccount(), node)
                    : SyncContentManager.getInstance(getActivity()).isSynced(getAccount(), node);

            if (isSynced && !isRootSynced(b))
            {
                b.setVisibility(View.GONE);
            }
            else if (node instanceof NodeSyncPlaceHolder)
            {
                b.setVisibility(View.VISIBLE);
                b.setImageResource(R.drawable.ic_synced_dark);
            }
            else if (!isRestrictable)
            {
                b.setImageResource(isSynced ? R.drawable.ic_synced_dark : R.drawable.ic_sync_light);
                b.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        isSynced = (node.isFolder())
                                ? SyncContentManager.getInstance(getActivity()).isRootSynced(getAccount(), node)
                                : SyncContentManager.getInstance(getActivity()).isSynced(getAccount(), node);

                        sync(v);
                    }
                });
            }
            else
            {
                b.setVisibility(View.GONE);
            }
        }
        else
        {
            b.setVisibility(View.GONE);
        }
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_SYNC);

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
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_SHARE);

        b = (ImageView) viewById(R.id.action_open_in_alfresco_editor);

        if (node.isDocument())
        {
            String mimetype = node.getPropertyValue(PropertyIds.CONTENT_STREAM_MIME_TYPE);
            if (!TextUtils.isEmpty(mimetype) && mimetype.startsWith(MimeType.TYPE_TEXT))
            {
                b.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        openin(true);
                    }
                });
            }
            else
            {
                b.setVisibility(View.GONE);
            }
        }
        else
        {
            b.setVisibility(View.GONE);
        }
        hideActionIfNecessary(b, ConfigurableActionHelper.ACTION_NODE_EDIT_WITH_ALFRESCO);
    }

    protected void displayTabs()
    {

    }

    protected void displayPreview()
    {
        displayRendition(node, R.drawable.mime_256_folder, (ImageView) viewById(R.id.preview), true);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected void hideActionIfNecessary(View v, int actionId)
    {
        if (!ConfigurableActionHelper.isVisible(getContext(), getAccount(), getSession(), node, actionId))
        {
            v.setVisibility(View.GONE);
        }
    }

    protected void displayData()
    {
        hide(R.id.empty);
        hide(R.id.progressbar_group);
        hide(R.id.progressbar);
        show(R.id.properties_details);
    }

    protected void displayEmptyView()
    {
        show(R.id.empty);
        if (viewById(R.id.empty) != null)
        {
            viewById(R.id.empty).setBackgroundColor(getResources().getColor(R.color.secondary_background));
        }
        show(R.id.progressbar_group);
        hide(R.id.progressbar);
        hide(R.id.properties_details);
    }

    protected void displayLoading()
    {
        show(R.id.progressbar_group);
        show(R.id.progressbar);
        hide(R.id.properties_details);
        hide(R.id.empty);
    }

    private void displayRendition(Node node, int defaultIconId, ImageView iv, boolean isLarge)
    {
        if (iv == null) { return; }

        int iconId = defaultIconId;
        if (node.isDocument())
        {
            MimeType mime = MimeTypeManager.getInstance(getActivity()).getMimetype(node.getName());
            iconId = MimeTypeManager.getInstance(getActivity()).getIcon(node.getName(), isLarge);
            if (node instanceof Document && ((Document) node).isLatestVersion())
            {
                if (isLarge)
                {
                    RenditionManager.with(getActivity()).loadNode(node).placeHolder(iconId)
                            .rendition(RenditionRequest.RENDITION_PREVIEW)
                            .touchViewEnable(DisplayUtils.hasCentralPane(getActivity())).into(iv);

                }
                else
                {
                    RenditionManager.with(getActivity()).loadNode(node).placeHolder(iconId).into(iv);
                }
            }
            else
            {
                iv.setImageResource(iconId);
                if (iv instanceof ImageViewTouch)
                {
                    ((ImageViewTouch) iv).setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
                    if (((ViewGroup) iv.getParent()).findViewById(R.id.preview_message) != null)
                    {
                        ((ViewGroup) iv.getParent()).findViewById(R.id.preview_message).setVisibility(View.VISIBLE);
                    }
                }
            }

            AccessibilityUtils.addContentDescription(iv, mime != null ? mime.getDescription()
                    : (String) node.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE).getValue());

            if (!isRestrictable && !AccessibilityUtils.isEnabled(getActivity()) && iv instanceof ImageViewTouch)
            {
                ((ImageViewTouch) iv).setDoubleTapEnabled(false);
                ((ImageViewTouch) iv).setDoubleTapListener(new ImageViewTouch.OnImageViewTouchDoubleTapListener()
                {
                    @Override
                    public void onDoubleTap()
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
            SyncContentManager syncManager = SyncContentManager.getInstance(getActivity());
            AlfrescoAccount acc = SessionUtils.getAccount(getActivity());
            final File syncFile = syncManager.getSyncFile(acc, node);
            if (syncFile == null || !syncFile.exists())
            {
                AlfrescoNotificationManager.getInstance(getActivity())
                        .showLongToast(getString(R.string.sync_document_not_available));
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
                ActionUtils.actionSend(getActivity(), syncFile, (String) null);
            }
            return;
        }

        ConfigService configService = ConfigManager.getInstance(getActivity()).getConfig(getAccount().getId(),
                ConfigTypeIds.REPOSITORY);
        if (configService != null && configService.getRepositoryConfig() != null)
        {
            shareUrl = configService.getRepositoryConfig().getShareURL();
            if (!TextUtils.isEmpty(shareUrl) && !shareUrl.endsWith("/"))
            {
                shareUrl.concat("/");
            }
        }

        if (MDMManager.getInstance(getActivity()).hasConfig())
        {
            String tmpShareURL = (String) MDMManager.getInstance(getActivity()).getShareURL();
            if (!TextUtils.isEmpty(tmpShareURL))
            {
                shareUrl = tmpShareURL;
            }
        }

        if (getSession() instanceof RepositorySession && shareUrl == null)
        {
            // Only sharing as attachment is allowed when we're not on a cloud
            // account
            Bundle b = new Bundle();
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getActivity().getSupportFragmentManager(), DownloadDialogFragment.TAG);

            // Analytics
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    AnalyticsManager.ACTION_SHARE,
                    node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                    false);
        }
        else
        {
            new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo).title(R.string.app_name)
                    .content(R.string.link_or_attach).positiveText(R.string.full_attachment)
                    .negativeText(R.string.link_to_repo).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            Bundle b = new Bundle();
                            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, node);
                            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, DownloadDialogFragment.ACTION_EMAIL);
                            DialogFragment frag = new DownloadDialogFragment();
                            frag.setArguments(b);
                            frag.show(getActivity().getSupportFragmentManager(), DownloadDialogFragment.TAG);
                            dialog.dismiss();

                            // Analytics
                            AnalyticsHelper.reportOperationEvent(getActivity(),
                                    AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT, AnalyticsManager.ACTION_SHARE,
                                    node.isDocument() ? ((Document) node).getContentStreamMimeType()
                                            : AnalyticsManager.TYPE_FOLDER,
                                    1, false);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog)
                        {
                            if (parentNode != null)
                            {
                                String path = parentNode.getPropertyValue(PropertyIds.PATH);
                                if (path.length() > 0)
                                {
                                    String fullPath = null;
                                    if (getSession() instanceof RepositorySession)
                                    {
                                        fullPath = shareUrl
                                                .concat(String.format(getString(R.string.onpremise_share_url),
                                                        NodeRefUtils.getCleanIdentifier(
                                                                NodeRefUtils.getNodeIdentifier(node.getIdentifier()))));
                                        ActionUtils.actionShareLink(NodeDetailsFragment.this, fullPath);
                                    }
                                    else if (path.startsWith("/Sites/"))
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
                                        fullPath = String.format(getString(R.string.cloud_share_url),
                                                ((CloudSession) getSession()).getNetwork().getIdentifier(), siteName,
                                                nodeID);

                                        ActionUtils.actionShareLink(NodeDetailsFragment.this, fullPath);
                                    }
                                    else
                                    {
                                        Log.i(getString(R.string.app_name), "Site path not as expected: no /sites/");
                                    }

                                    // Analytics
                                    AnalyticsHelper.reportOperationEvent(getActivity(),
                                            AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                                            AnalyticsManager.ACTION_SHARE_AS_LINK,
                                            node.isDocument() ? ((Document) node).getContentStreamMimeType()
                                                    : AnalyticsManager.TYPE_FOLDER,
                                            1, false);
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
                    }).show();
        }
    }

    public void openin()
    {
        openin(false);
    }

    public void openin(boolean withAlfresco)
    {
        if (isRestrictable) { return; }

        Bundle b = new Bundle();

        // 3 cases
        SyncContentManager syncManager = SyncContentManager.getInstance(getActivity());
        AlfrescoAccount acc = getAccount();

        if (syncManager.isSynced(SessionUtils.getAccount(getActivity()), node))
        {
            final File syncFile = syncManager.getSyncFile(acc, node);
            if (syncFile == null || !syncFile.exists())
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showAlertCrouton(getActivity(),
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
                        OpenAsDialogFragment.newInstance(syncFile).show(getActivity().getSupportFragmentManager(),
                                OpenAsDialogFragment.TAG);
                    }
                });
            }
            else
            {
                if (withAlfresco)
                {
                    ActionUtils.openWithAlfrescoTextEditor(this, syncFile,
                            MimeTypeManager.getInstance(getActivity()).getMIMEType(syncFile.getName()),
                            RequestCode.SAVE_BACK);

                }
                else
                {
                    ActionUtils.openIn(this, syncFile,
                            MimeTypeManager.getInstance(getActivity()).getMIMEType(syncFile.getName()),
                            RequestCode.SAVE_BACK);
                }
            }
        }
        else
        {
            // Other case
            b.putParcelable(DownloadDialogFragment.ARGUMENT_DOCUMENT, node);
            b.putInt(DownloadDialogFragment.ARGUMENT_ACTION, withAlfresco
                    ? DownloadDialogFragment.ACTION_OPEN_WITH_ALFRESCO : DownloadDialogFragment.ACTION_OPEN);
            DialogFragment frag = new DownloadDialogFragment();
            frag.setArguments(b);
            frag.show(getActivity().getSupportFragmentManager(), DownloadDialogFragment.TAG);
        }

        // Analytics
        if (node instanceof Document)
        {
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    AnalyticsManager.ACTION_OPEN,
                    node.isDocument() ? ((Document) node).getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER, 1,
                    false, AnalyticsManager.INDEX_FILE_SIZE,
                    node.isDocument() ? ((Document) node).getContentStreamLength() : 0);
        }
        else if (node instanceof NodeSyncPlaceHolder)
        {
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    AnalyticsManager.ACTION_OPEN_OFFLINE,
                    node.getPropertyValue(PropertyIds.CONTENT_STREAM_MIME_TYPE) != null
                            ? node.getPropertyValue(PropertyIds.CONTENT_STREAM_MIME_TYPE).toString()
                            : AnalyticsManager.TYPE_FOLDER,
                    1, false, AnalyticsManager.INDEX_FILE_SIZE,
                    node.getPropertyValue(PropertyIds.CONTENT_STREAM_LENGTH) != null
                            ? Long.parseLong(node.getPropertyValue(PropertyIds.CONTENT_STREAM_LENGTH).toString()) : 0);
        }
    }

    public void setDownloadDateTime(Date downloadDateTime)
    {
        this.downloadDateTime = downloadDateTime;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.edit().putLong("Dltime", downloadDateTime.getTime()).commit();
    }

    public Date getDownloadDateTime()
    {
        Date d = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long activationTime = sharedPref.getLong("Dltime", -1);
        if (activationTime != -1)
        {
            d = new Date();
            d.setTime(activationTime);
        }
        return d;
    }

    public void removeDownloadDateTime()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.edit().remove("Dltime").commit();
    }

    public void download()
    {
        if (isRestrictable) { return; }

        if (node instanceof Document)
        {
            NodeActions.download(getActivity(), parentNode, (Document) node);

            // Analytics
            AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                    AnalyticsManager.ACTION_DOWNLOAD, ((Document) node).getContentStreamMimeType(), 1, false,
                    AnalyticsManager.INDEX_FILE_SIZE, ((Document) node).getContentStreamLength());
        }
    }

    public void update()
    {
        ActionUtils.actionPickFile(this, RequestCode.FILEPICKER);
    }

    public void update(File f)
    {
        Operator.with(getActivity(), getAccount())
                .load(new UpdateContentRequest.Builder(parentNode, (Document) node, new ContentFileProgressImpl(f))
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
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity()))
                .load(new LikeNodeRequest.Builder(node, false));

    }

    public void isLiked(View v)
    {
        viewById(R.id.like_progress).setVisibility(View.VISIBLE);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity()))
                .load(new LikeNodeRequest.Builder(node, true));
    }

    public void isFavorite(View v)
    {
        viewById(R.id.favorite_progress).setVisibility(View.VISIBLE);
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity()))
                .load(new FavoritedNodeRequest.Builder(node));
    }

    public void favorite(View v)
    {
        if (isRestrictable) { return; }

        viewById(R.id.favorite_progress).setVisibility(View.VISIBLE);
        if (node != null)
        {
            Operator.with(getActivity(), getAccount()).load(new FavoriteNodeRequest.Builder(parentNode, node));
        }
    }

    public boolean isRootSynced(ImageView v)
    {
        boolean isRootSynced = SyncContentManager.getInstance(getActivity()).isRootSynced(getAccount(), node);
        if (isRootSynced)
        {
            v.setImageResource(isRootSynced ? R.drawable.ic_synced_dark : R.drawable.ic_sync_light);
            v.setVisibility(View.VISIBLE);
        }
        else
        {
            v.setVisibility(View.GONE);
        }
        return isRootSynced;
    }

    public void sync(View v)
    {
        if (isRestrictable) { return; }
        if (node != null)
        {
            Operator.with(getActivity(), getAccount()).load(new SyncNodeRequest.Builder(parentNode, node, !isSynced));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Context context, AlfrescoSession session, Menu menu, Node node)
    {
        MenuItem mi;

        if (node == null) { return; }
        if (node instanceof NodeSyncPlaceHolder) { return; }

        boolean isRestrict = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0 && !isRestrict && ConfigurableActionHelper.isVisible(
                    getActivity(), getAccount(), getSession(), node, ConfigurableActionHelper.ACTION_NODE_DOWNLOAD))
            {
                mi = menu.add(Menu.NONE, R.id.menu_action_download, Menu.FIRST, R.string.download);
                mi.setIcon(R.drawable.ic_download_light);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((Document) node).isLatestVersion() && ConfigurableActionHelper.isVisible(getActivity(), getAccount(),
                    getSession(), node, ConfigurableActionHelper.ACTION_NODE_UPDATE))
            {
                mi = menu.add(Menu.NONE, R.id.menu_action_update, Menu.FIRST + 130, R.string.update);
                mi.setIcon(R.drawable.ic_upload);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (!(session instanceof CloudSession) && ConfigurableActionHelper.isVisible(getActivity(), getAccount(),
                    getSession(), node, ConfigurableActionHelper.ACTION_NODE_REVIEW))
            {
                mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST + 500, R.string.process_start_review);
                mi.setIcon(R.drawable.ic_start_review);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (session == null) { return; }

        if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), node,
                ConfigurableActionHelper.ACTION_NODE_EDIT))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST + 10, R.string.edit);
            mi.setIcon(R.drawable.ic_properties);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (node.hasAspect(ContentModel.ASPECT_GEOGRAPHIC))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_location, Menu.FIRST + 50, R.string.geolocation);
            mi.setIcon(R.drawable.ic_location);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), node,
                ConfigurableActionHelper.ACTION_NODE_DELETE))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    public void getMenu(Menu menu)
    {
        getMenu(getActivity(), getSession(), menu, node);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
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
                i.putExtra(PrivateIntent.EXTRA_FOLDER,
                        AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(getAccount()));
                i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                startActivityForResult(i, RequestCode.FILEPICKER);
                return true;
            case R.id.menu_action_edit:
                edit();
                return true;
            case R.id.menu_action_delete:
                delete();
                return true;
            case R.id.menu_action_location:
                ActionUtils.actionShowMap(this, node.getName(),
                        node.getProperty(ContentModel.PROP_LATITUDE).getValue().toString(),
                        node.getProperty(ContentModel.PROP_LONGITUDE).getValue().toString());
                return true;
            case R.id.menu_workflow_add:
                Intent in = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(),
                        PrivateDialogActivity.class);
                Document doc = (Document) getCurrentNode();
                in.putExtra(PrivateIntent.EXTRA_DOCUMENT, (Serializable) doc);
                in.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                startActivity(in);

                // Analytics
                AnalyticsHelper.reportOperationEvent(getActivity(), AnalyticsManager.CATEGORY_DOCUMENT_MANAGEMENT,
                        AnalyticsManager.ACTION_START_REVIEW, doc.getContentStreamMimeType(), 1, false);

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
        if (getActivity() == null) { return; }
        if (getRootView() == null) { return; }
        if (event.hasException)
        {
            displayEmptyView();
            if (((TextView) viewById(R.id.empty_text)) != null)
            {
                ((TextView) viewById(R.id.empty_text)).setText(R.string.empty_child);
            }
        }
        else
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
        ImageView likeButton = (ImageView) viewById(R.id.action_like);
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
            likeButton.setImageResource(R.drawable.ic_like);
            AccessibilityUtils.addContentDescription(likeButton, R.string.unlike);
        }
        else
        {
            likeButton.setImageResource(R.drawable.ic_unlike);
            AccessibilityUtils.addContentDescription(likeButton, R.string.like);
        }
    }

    @Subscribe
    public void onIsFavoriteEvent(FavoritedNodeEvent event)
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
            favoriteButton.setImageResource(R.drawable.ic_favorite_light);
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.unfavorite);
        }
        else
        {
            favoriteButton.setImageResource(R.drawable.ic_unfavorite_dark);
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.favorite);
        }
    }

    @Subscribe
    public void onFavoriteNodeEvent(FavoriteNodeEvent event)
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
            favoriteButton.setImageResource(R.drawable.ic_favorite_light);
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.unfavorite);
        }
        else
        {
            favoriteButton.setImageResource(R.drawable.ic_unfavorite_dark);
            AccessibilityUtils.addContentDescription(favoriteButton, R.string.favorite);
        }

        if (!DisplayUtils.hasCentralPane(getActivity()) && getFragment(DocumentFolderBrowserFragment.TAG) != null)
        {
            ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).onFavoriteNodeEvent(event);
        }
    }

    @Subscribe
    public void onSyncCompleted(SyncContentScanEvent event)
    {
        if (!DisplayUtils.hasCentralPane(getActivity()) && getFragment(DocumentFolderBrowserFragment.TAG) != null)
        {
            ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).onSyncCompleted(event);
        }
    }

    @Subscribe
    public void onSyncNodeEvent(SyncNodeEvent event)
    {
        ImageView syncButton = (ImageView) viewById(R.id.action_sync);
        if (syncButton == null) { return; }

        if (event.data == null)
        {
            Log.e(TAG, Log.getStackTraceString(event.exception));
            // AlfrescoNotificationManager.getInstance(getActivity()).showToast(R.string.error_retrieve_favorite);
        }
        else if (event.data)
        {
            this.isSynced = event.data;
            syncButton.setImageResource(R.drawable.ic_synced_dark);
            AccessibilityUtils.addContentDescription(syncButton, R.string.unsync);

        }
        else
        {
            this.isSynced = event.data;
            syncButton.setImageResource(R.drawable.ic_sync_light);
            AccessibilityUtils.addContentDescription(syncButton, R.string.sync);
        }

        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            if (SyncContentManager.getInstance(getActivity()).canSync(SessionUtils.getAccount(getActivity())))
            {
                if (event.node != null)
                {
                    SyncContentManager.getInstance(getActivity()).sync(SessionUtils.getAccount(getActivity()),
                            event.node.getIdentifier());
                }
                else
                {
                    SyncContentManager.getInstance(getActivity()).sync(AnalyticsManager.LABEL_SYNC_ACTION,
                            SessionUtils.getAccount(getActivity()));
                }
            }
        }
    }

    @Subscribe
    public void onDocumentDownloaded(DownloadEvent event)
    {
        if (!DisplayUtils.hasCentralPane(getActivity()) && getFragment(DocumentFolderBrowserFragment.TAG) != null)
        {
            ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG))
                    .onDocumentDownloaded(event);
        }
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        if (AlfrescoExceptionHelper.checkEventException(getActivity(), event)) { return; }
        Node updatedNode = event.data;

        if (updatedNode == null || node == null) { return; }
        if (updatedNode.getIdentifier() == null || node.getIdentifier() == null) { return; }

        if (NodeRefUtils.getCleanIdentifier(updatedNode.getIdentifier())
                .equals(NodeRefUtils.getCleanIdentifier(node.getIdentifier())))
        {
            if (!DisplayUtils.hasCentralPane(getActivity()))
            {
                if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
                {
                    ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG))
                            .onDocumentUpdated(event);
                }
                getActivity().getSupportFragmentManager().popBackStack(NodeDetailsFragment.getDetailsTag(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            else if (getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DocumentFolderBrowserFragment.TAG) != null)
            {
                ((DocumentFolderBrowserFragment) getActivity().getSupportFragmentManager()
                        .findFragmentByTag(DocumentFolderBrowserFragment.TAG)).highLight(updatedNode);
            }
            else if (getActivity().getSupportFragmentManager().findFragmentByTag(SyncFragment.TAG) != null)
            {
                ((SyncFragment) getActivity().getSupportFragmentManager().findFragmentByTag(SyncFragment.TAG))
                        .highLight(updatedNode);
            }
            NodeDetailsFragment.with(getActivity()).node(updatedNode).parentFolder(event.parentFolder).display();

            AlfrescoNotificationManager.getInstance(getActivity()).showInfoCrouton(getActivity(),
                    String.format(getResources().getString(R.string.update_sucess), event.initialNode.getName()));
        }
    }

    @Subscribe
    public void onContentUpdated(UpdateContentEvent event)
    {
        if (AlfrescoExceptionHelper.checkEventException(getActivity(), event)) { return; }
        Node updatedNode = event.data;
        if (updatedNode == null || node == null) { return; }
        if (updatedNode.getIdentifier() == null || node.getIdentifier() == null) { return; }

        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            getActivity().getSupportFragmentManager().popBackStack(NodeDetailsFragment.getDetailsTag(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if (getActivity().getSupportFragmentManager().findFragmentByTag(DocumentFolderBrowserFragment.TAG) != null)
        {
            ((DocumentFolderBrowserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DocumentFolderBrowserFragment.TAG)).select(updatedNode);
        }
        NodeDetailsFragment.with(getActivity()).node(updatedNode).parentFolder(event.parentFolder).display();

        AlfrescoNotificationManager.getInstance(getActivity()).showInfoCrouton(getActivity(),
                String.format(getResources().getString(R.string.update_sucess), event.node.getName()));
    }

    @Subscribe
    public void onNodeDeleted(DeleteNodeEvent event)
    {
        if (AlfrescoExceptionHelper.checkEventException(getActivity(), event)) { return; }

        ((MainActivity) getActivity()).setCurrentNode(null);
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            FragmentDisplayer.with(getActivity()).remove(getDetailsTag());
        }
        else
        {
            getFragmentManager().popBackStack(getDetailsTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (getFragment(DocumentFolderBrowserFragment.TAG) != null)
            {
                ((DocumentFolderBrowserFragment) getFragment(DocumentFolderBrowserFragment.TAG)).onNodeDeleted(event);
            }
        }

        AlfrescoNotificationManager.getInstance(getActivity()).showInfoCrouton(getActivity(),
                String.format(getResources().getString(R.string.delete_sucess), event.data.getName()));
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
        return getActivity().getSupportFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static String getDetailsTag()
    {
        return PagerNodeDetailsFragment.TAG;
    }

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
            this.extraConfiguration = new Bundle();
            viewConfigModel = new NodeDetailsConfigModel(configuration);
            templateArguments = new String[] { ARGUMENT_NODE_ID, ARGUMENT_PATH };
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
            return PagerNodeDetailsFragment.newInstanceByTemplate(b);
        }
    }
}

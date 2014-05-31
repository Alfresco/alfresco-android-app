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
import java.util.Date;
import java.util.Map;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.node.comment.CommentsFragment;
import org.alfresco.mobile.android.application.fragments.node.rendition.PreviewFragment;
import org.alfresco.mobile.android.application.fragments.node.versions.VersionFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionEvent;
import org.alfresco.mobile.android.async.node.RetrieveNodeEvent;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeEvent;
import org.alfresco.mobile.android.async.node.favorite.FavoritedNodeEvent;
import org.alfresco.mobile.android.async.node.like.LikeNodeEvent;
import org.alfresco.mobile.android.async.node.update.UpdateNodeEvent;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.FavoritesSyncManager;
import org.alfresco.mobile.android.sync.FavoritesSyncSchema;
import org.alfresco.mobile.android.sync.operations.FavoriteSyncStatus;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.squareup.otto.Subscribe;

/**
 * Responsible to display details of a specific Node.
 * 
 * @author Jean Marie Pascal
 */
public class TabsNodeDetailsFragment extends NodeDetailsFragment implements OnTabChangeListener
{
    public static final String TAG = TabsNodeDetailsFragment.class.getName();

    private TabHost mTabHost;

    private static final String TAB_SELECTED = "tabSelected";

    protected Integer tabSelected = null;

    protected Integer tabSelection = null;

    protected File tempFile = null;

    protected PreviewFragment replacementPreviewFragment = null;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public TabsNodeDetailsFragment()
    {
        requiredSession = true;
        checkSession = true;
        setHasOptionsMenu(true);
    }

    protected static TabsNodeDetailsFragment newInstanceByTemplate(Bundle b)
    {
        TabsNodeDetailsFragment bf = new TabsNodeDetailsFragment();
        bf.setArguments(b);
        return bf;
    };

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        // Manage Tabs
        if (savedInstanceState != null)
        {
            tabSelection = savedInstanceState.getInt(TAB_SELECTED);
            savedInstanceState.remove(TAB_SELECTED);
        }
        return getRootView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (tabSelected != null)
        {
            outState.putInt(TAB_SELECTED, tabSelected);
        }
    }

    @Override
    public void onResume()
    {
        ((MainActivity) getActivity()).setCurrentNode(node);
        getActivity().invalidateOptionsMenu();
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), R.string.details);
        }
        super.onResume();
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
                        ActionUtils.actionDisplayError(TabsNodeDetailsFragment.this, new AlfrescoAppException(
                                getString(R.string.error_unknown_filepath), true));
                    }
                }
                break;
            default:
                break;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // CREATE PARTS
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected void displayTabs()
    {
        mTabHost = (TabHost) viewById(android.R.id.tabhost);
        setupTabs();
    }

    @Override
    protected void displayPartsOffline(NodeSyncPlaceHolder refreshedNode)
    {
        // TODO
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onResult(RetrieveNodeEvent event)
    {
        super.onResult(event);
    }

    @Subscribe
    public void onLikeEvent(LikeNodeEvent event)
    {
        super.onLikeEvent(event);
    }

    @Subscribe
    public void onFavoriteEvent(FavoritedNodeEvent event)
    {
        super.onFavoriteEvent(event);
    }

    @Subscribe
    public void onFavoriteEvent(FavoriteNodeEvent event)
    {
        super.onFavoriteEvent(event);
    }

    @Subscribe
    public void onDocumentUpdated(UpdateNodeEvent event)
    {
        super.onDocumentUpdated(event);
    }

    @Subscribe
    public void onNodeDeleted(DeleteNodeEvent event)
    {
        super.onNodeDeleted(event);
    }

    @Subscribe
    public void onFileProtectionEvent(FileProtectionEvent event)
    {
        super.onFileProtectionEvent(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU
    // ///////////////////////////////////////////////////////////////////////////
    private static final String TAB_PREVIEW = "Preview";

    private static final String TAB_METADATA = "Metadata";

    private static final String TAB_COMMENTS = "Comments";

    private static final String TAB_HISTORY = "History";

    private static final String TAB_SUMMARY = "Summary";

    private void setupTabs()
    {
        if (mTabHost == null) { return; }

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);

        if (DisplayUtils.hasCentralPane(getActivity()) && node.isDocument() && ((Document) node).isLatestVersion())
        {
            mTabHost.addTab(newTab(TAB_PREVIEW, R.string.preview, android.R.id.tabcontent));
        }
        mTabHost.addTab(newTab(TAB_METADATA, R.string.metadata, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_COMMENTS, R.string.comments, android.R.id.tabcontent));
        if (node.isDocument())
        {
            mTabHost.addTab(newTab(TAB_HISTORY, R.string.action_version, android.R.id.tabcontent));
        }

        mTabHost.setCurrentTab(1);
        mTabHost.setCurrentTab(0);

        if (tabSelection != null)
        {
            if (tabSelection == 0) { return; }
            int index = (node.isDocument()) ? tabSelection : tabSelection - 1;
            mTabHost.setCurrentTab(index);
        }
    }

    private TabSpec newTab(String tag, int labelId, int tabContentId)
    {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId)
    {
        if (TAB_METADATA.equals(tabId))
        {
            tabSelected = 1;
            addMetadata(node);
        }
        else if (TAB_COMMENTS.equals(tabId))
        {
            tabSelected = 2;
            addComments(node);
        }
        else if (TAB_HISTORY.equals(tabId) && node.isDocument())
        {
            tabSelected = 3;
            addVersions((Document) node);
        }
        else if (TAB_PREVIEW.equals(tabId))
        {
            tabSelected = 0;
            addPreview(node);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TAB MENU ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void addPreview(Node n)
    {
        addPreview(n, android.R.id.tabcontent, false);
    }

    public void addPreview(Node n, int layoutId, boolean backstack)
    {
        PreviewFragment.with(getActivity()).node(n).back(false).display(layoutId);
    }

    public void addComments(Node n, int layoutId, boolean backstack)
    {
        CommentsFragment.with(getActivity()).node(n).back(false).display(layoutId);
    }

    public void addComments(Node n)
    {
        addComments(n, android.R.id.tabcontent, false);
    }

    public void addMetadata(Node n)
    {
        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            NodePropertiesFragment.with(getActivity()).node(node).parentFolder(parentNode).back(false)
                    .display(android.R.id.tabcontent);
        }
        else
        {
            NodeSummaryFragment.with(getActivity()).node(node).parentFolder(parentNode).back(false)
                    .display(android.R.id.tabcontent);
        }
    }

    public void addVersions(Document d, int layoutId, boolean backstack)
    {
        VersionFragment.with(getActivity()).node(d).parentFolder(parentNode).back(false).display(layoutId);
    }

    public void addVersions(Document d)
    {
        addVersions(d, android.R.id.tabcontent, false);
    }

    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends NodeDetailsFragment.Builder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }

}

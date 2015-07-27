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
package org.alfresco.mobile.android.application.fragments.actions;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.node.browser.DocumentFolderBrowserFragment;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.details.PagerNodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.node.search.DocumentFolderSearchFragment;
import org.alfresco.mobile.android.application.fragments.node.update.UpdateDialogFragment;
import org.alfresco.mobile.android.application.fragments.sync.SyncFragment;
import org.alfresco.mobile.android.application.fragments.utils.ResolveNamingConflictFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.ConfigManager;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.async.node.download.DownloadRequest;
import org.alfresco.mobile.android.async.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.async.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.async.node.sync.SyncNodeRequest;
import org.alfresco.mobile.android.async.utils.NodePlaceHolder;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class NodeActions extends AbstractActions<Node>
{
    public static final String TAG = NodeActions.class.getName();

    private List<Folder> selectedFolder = new ArrayList<Folder>();

    private List<Document> selectedDocument = new ArrayList<Document>();

    private Folder parentFolder;

    public NodeActions(Fragment f, List<Node> selectedNodes)
    {
        this.fragmentRef = new WeakReference<>(f);
        this.activityRef = new WeakReference<>(f.getActivity());
        this.selectedItems = selectedNodes;
        for (Node node : selectedNodes)
        {
            addNode(node);
        }
        if (f instanceof DocumentFolderBrowserFragment)
        {
            this.parentFolder = ((DocumentFolderBrowserFragment) getFragment()).getParent();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    protected String createTitle()
    {
        String title = "";

        if (selectedItems.size() == 1)
        {
            title = getFragment().getString(R.string.multi_selection_enable);
        }
        else
        {
            int size = selectedDocument.size();
            if (size > 0)
            {
                title += String.format(getFragment().getResources()
                        .getQuantityString(R.plurals.selected_document, size), size);
            }
            size = selectedFolder.size();
            if (size > 0)
            {
                if (!title.isEmpty())
                {
                    title += " | ";
                }
                title += String.format(
                        getFragment().getResources().getQuantityString(R.plurals.selected_folders, size), size);
            }
        }

        return title;
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // LIST MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////////////

    public void selectNodes(List<Node> nodes)
    {
        selectedDocument.clear();
        selectedFolder.clear();
        super.selectNodes(nodes);
    }

    protected void addNode(Node n)
    {
        super.addNode(n);
        if (n == null) { return; }
        if (n instanceof NodePlaceHolder) { return; }
        if (n.isDocument())
        {
            selectedDocument.add((Document) n);
        }
        else
        {
            selectedFolder.add((Folder) n);
        }
    }

    protected void removeNode(Node n)
    {
        super.removeNode(n);
        if (n.isDocument())
        {
            selectedDocument.remove(n);
        }
        else
        {
            selectedFolder.remove(n);
        }
    }

    private void selectAll()
    {
        if (getFragment() instanceof DocumentFolderBrowserFragment)
        {
            ((DocumentFolderBrowserFragment) getFragment()).selectAll();
        }
        else if (getFragment() instanceof DocumentFolderSearchFragment)
        {
            ((DocumentFolderSearchFragment) getFragment()).selectAll();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////////////
    protected void getMenu(FragmentActivity activity, Menu menu)
    {
        MenuItem mi;
        SubMenu createMenu;

        if (selectedFolder.isEmpty())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_download_all, Menu.FIRST, R.string.download);
            mi.setIcon(R.drawable.ic_download_light);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            if (!(SessionUtils.getSession(activity) instanceof CloudSession))
            {
                mi = menu.add(Menu.NONE, R.id.menu_workflow_review_attachments, Menu.FIRST + 500,
                        R.string.process_start_review);
                mi.setIcon(R.drawable.ic_start_review);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        // SYNC
        if (SyncContentManager.getInstance(getActivity()).hasActivateSync(getAccount())
                && !SyncContentManager.getInstance(getActivity()).isSynced(getAccount(), parentFolder))
        {
            createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_sync_group, Menu.FIRST, R.string.sync);
            createMenu.setIcon(R.drawable.ic_sync_light);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            createMenu.add(Menu.NONE, R.id.menu_action_sync_group_sync, Menu.FIRST + 1, R.string.sync);
            createMenu.add(Menu.NONE, R.id.menu_action_sync_group_unsync, Menu.FIRST + 2, R.string.unsync);
        }

        // FAVORITES
        createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_favorite_group, Menu.FIRST + 135, R.string.favorite);
        createMenu.setIcon(R.drawable.ic_favorite_light);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_favorite, Menu.FIRST + 1, R.string.favorite);
        createMenu.add(Menu.NONE, R.id.menu_action_favorite_group_unfavorite, Menu.FIRST + 2, R.string.unfavorite);

        // LIKE
        AlfrescoSession alfSession = SessionUtils.getSession(activity);
        if (alfSession != null && alfSession.getRepositoryInfo() != null
                && alfSession.getRepositoryInfo().getCapabilities() != null
                && alfSession.getRepositoryInfo().getCapabilities().doesSupportLikingNodes())
        {
            createMenu = menu.addSubMenu(Menu.NONE, R.id.menu_action_like_group, Menu.FIRST + 150, R.string.like);
            createMenu.setIcon(R.drawable.ic_like);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            createMenu.add(Menu.NONE, R.id.menu_action_like_group_like, Menu.FIRST + 1, R.string.like);
            createMenu.add(Menu.NONE, R.id.menu_action_like_group_unlike, Menu.FIRST + 2, R.string.unlike);
        }

        if (parentFolder != null
                && alfSession.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder).canDelete())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        mi = menu.add(Menu.NONE, R.id.menu_select_all, Menu.FIRST + 2000, R.string.select_all);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case R.id.menu_node_details:
                NodeDetailsFragment.with(getActivity()).node(selectedItems.get(0)).display();
                break;
            case R.id.menu_action_update:
                update(getActivity().getSupportFragmentManager().findFragmentByTag(PagerNodeDetailsFragment.TAG));
                b = true;
                break;
            case R.id.menu_action_sync_group_sync:
                sync(true);
                b = true;
                break;
            case R.id.menu_action_sync_group_unsync:
                sync(false);
                b = true;
                break;
            case R.id.menu_action_favorite_group_favorite:
                favorite(true);
                b = true;
                break;
            case R.id.menu_action_favorite_group_unfavorite:
                favorite(false);
                b = true;
                break;
            case R.id.menu_action_like_group_like:
                like(true);
                b = true;
                break;
            case R.id.menu_action_like_group_unlike:
                like(false);
                b = true;
                break;
            case R.id.menu_action_download_all:
            case R.id.menu_action_download:
                download();
                b = true;
                break;
            case R.id.menu_action_edit:
                edit(getActivity(), parentFolder, selectedItems.get(0));
                b = true;
                break;
            case R.id.menu_action_delete:
            case R.id.menu_action_delete_folder:
                delete(getActivity(), getFragment(), new ArrayList<Node>(selectedItems));
                b = true;
                break;
            case R.id.menu_select_all:
                selectAll();
                b = false;
                break;
            case R.id.menu_workflow_review_attachments:
                startReview();
                b = true;
                break;
            default:
                break;
        }
        if (b)
        {
            selectedItems.clear();
            selectedDocument.clear();
            selectedFolder.clear();
            mode.finish();
        }
        return b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void startReview()
    {
        Intent it = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(), PrivateDialogActivity.class);
        it.putParcelableArrayListExtra(PrivateIntent.EXTRA_DOCUMENTS, (ArrayList<? extends Parcelable>) selectedItems);
        getActivity().startActivity(it);
    }

    private void favorite(boolean doFavorite)
    {
        if (getFragment() instanceof DocumentFolderBrowserFragment)
        {
            ((DocumentFolderBrowserFragment) getFragment()).favorite(selectedItems, doFavorite, false);
        }
        else
        {
            ArrayList<String> items = new ArrayList<>(selectedItems.size());
            for (Node node : selectedItems)
            {
                items.add(node.getIdentifier());
            }
            favorite(getFragment(), items, doFavorite);
        }
    }

    private void sync(boolean doSync)
    {
        if (getFragment() instanceof DocumentFolderBrowserFragment)
        {
            ((DocumentFolderBrowserFragment) getFragment()).sync(selectedItems, doSync, false);
        }
        else
        {
            ArrayList<String> items = new ArrayList<>(selectedItems.size());
            for (Node node : selectedItems)
            {
                items.add(node.getIdentifier());
            }
            sync(getFragment(), items, doSync);
        }
    }

    private void like(boolean doLike)
    {
        if (getFragment() instanceof DocumentFolderBrowserFragment)
        {
            List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
            for (Node node : selectedItems)
            {
                requestsBuilder.add(new LikeNodeRequest.Builder(node, false, doLike)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
            }
            String operationId = Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(
                    requestsBuilder);

            if (getFragment() instanceof DocumentFolderBrowserFragment)
            {
                int titleId = R.string.unlike;
                int iconId = R.drawable.ic_unlike;
                if (doLike)
                {
                    titleId = R.string.like;
                    iconId = R.drawable.ic_like;
                }
                OperationWaitingDialogFragment.newInstance(LikeNodeRequest.TYPE_ID, iconId,
                        getFragment().getString(titleId), null, null, selectedItems.size(), operationId).show(
                        getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
            }
        }
        else
        {
            ArrayList<String> items = new ArrayList<>(selectedItems.size());
            for (Node node : selectedItems)
            {
                items.add(node.getIdentifier());
            }
            like(getFragment(), items, doLike);
        }
    }

    public static void download(FragmentActivity activity, Folder parentFolder, Document doc)
    {
        // Check if File exist
        File folder = AlfrescoStorageManager.getInstance(activity).getDownloadFolder(SessionUtils.getAccount(activity));
        if (folder != null && new File(folder, doc.getName()).exists())
        {
            ResolveNamingConflictFragment.newInstance(parentFolder, doc).show(activity.getSupportFragmentManager(),
                    ResolveNamingConflictFragment.TAG);
        }
        else
        {
            Operator.with(activity).load(
                    new DownloadRequest.Builder(parentFolder, doc, false)
                            .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
    }

    public void download()
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (Document doc : selectedDocument)
        {
            requestsBuilder.add(new DownloadRequest.Builder(parentFolder, doc, false)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        Operator.with(getActivity(), SessionUtils.getAccount(getActivity())).load(requestsBuilder);
    }

    public static void edit(final FragmentActivity activity, final Folder folder, final Node node)
    {
        ConfigManager configurationManager = ConfigManager.getInstance(activity);
        if (configurationManager != null && configurationManager.hasConfig(SessionUtils.getAccount(activity).getId()))
        {
            try
            {
                Intent i = new Intent(activity, PrivateDialogActivity.class);
                i.setAction(PrivateDialogActivity.ACTION_EDIT_NODE);
                i.putExtra(PrivateIntent.EXTRA_FOLDER, (Parcelable) folder);
                i.putExtra(PrivateIntent.EXTRA_NODE, (Parcelable) node);
                activity.startActivity(i);
            }
            catch (ActivityNotFoundException e)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                        R.string.error_unable_share_content);
            }
        }
        else
        {
            FragmentDisplayer.with(activity).remove(UpdateDialogFragment.TAG);

            // Create and show the dialog.
            UpdateDialogFragment.with(activity).parentFolder(folder).node(node).displayAsDialog();
        }
    }

    public static void update(Fragment f)
    {
        ActionUtils.actionPickFile(f, RequestCode.FILEPICKER);
    }

    public static void delete(final FragmentActivity activity, final Fragment f, Node node)
    {
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        delete(activity, f, nodes);
    }

    public static void delete(final FragmentActivity activity, final Fragment f, final List<Node> nodes)
    {
        Folder tmpParent = null;
            tmpParent = ((DocumentFolderBrowserFragment) f).getParent();
        final Folder parent = tmpParent;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.delete);
        String nodeDescription = nodes.size() + "";
        if (nodes.size() == 1)
        {
            nodeDescription = nodes.get(0).getName();
        }
        String description = String.format(
                activity.getResources().getQuantityString(R.plurals.delete_items, nodes.size()), nodeDescription);
        builder.setMessage(description);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                String operationId = null;
                if (nodes.size() == 1)
                {
                    operationId = Operator.with(activity).load(
                            new DeleteNodeRequest.Builder(parent, nodes.get(0))
                                    .setNotificationVisibility(OperationRequest.VISIBILITY_TOAST));
                }
                else
                {
                    List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(nodes.size());
                    for (Node node : nodes)
                    {
                        requestsBuilder.add(new DeleteNodeRequest.Builder(parent, node)
                                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                    }
                    operationId = Operator.with(activity, SessionUtils.getAccount(activity)).load(requestsBuilder);

                    OperationWaitingDialogFragment.newInstance(DeleteNodeRequest.TYPE_ID, R.drawable.ic_delete,
                            f.getString(R.string.delete), null, parent, nodes.size(), operationId).show(
                            f.getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public static void favorite(Fragment fr, List<String> selectedItems, boolean doFavorite)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (String nodeId : selectedItems)
        {
            requestsBuilder.add(new FavoriteNodeRequest.Builder(nodeId, doFavorite, true)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        String operationId = Operator.with(fr.getActivity(), SessionUtils.getAccount(fr.getActivity())).load(
                requestsBuilder);

        if (fr instanceof DocumentFolderBrowserFragment || fr instanceof SyncFragment
                || fr instanceof DocumentFolderSearchFragment)
        {
            int titleId = R.string.unfavorite;
            int iconId = R.drawable.ic_unfavorite_dark;
            if (doFavorite)
            {
                titleId = R.string.favorite;
                iconId = R.drawable.ic_favorite_light;
            }
            OperationWaitingDialogFragment.newInstance(FavoriteNodeRequest.TYPE_ID, iconId, fr.getString(titleId),
                    null, null, selectedItems.size(), operationId).show(fr.getActivity().getSupportFragmentManager(),
                    OperationWaitingDialogFragment.TAG);
        }
    }

    public static void sync(Fragment fr, List<String> selectedItems, boolean doFavorite)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (String nodeId : selectedItems)
        {
            requestsBuilder.add(new SyncNodeRequest.Builder(nodeId, doFavorite, true)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        String operationId = Operator.with(fr.getActivity(), SessionUtils.getAccount(fr.getActivity())).load(
                requestsBuilder);

        if (fr instanceof DocumentFolderBrowserFragment || fr instanceof SyncFragment
                || fr instanceof DocumentFolderSearchFragment)
        {
            int titleId = R.string.unsync;
            int iconId = R.drawable.ic_synced;
            if (doFavorite)
            {
                titleId = R.string.sync;
                iconId = R.drawable.ic_sync_light;
            }
            OperationWaitingDialogFragment.newInstance(SyncNodeRequest.TYPE_ID, iconId, fr.getString(titleId), null,
                    null, selectedItems.size(), operationId).show(fr.getActivity().getSupportFragmentManager(),
                    OperationWaitingDialogFragment.TAG);
        }
    }

    public static void like(Fragment fr, List<String> selectedItems, boolean doLike)
    {
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedItems.size());
        for (String node : selectedItems)
        {
            requestsBuilder.add(new LikeNodeRequest.Builder(node, false, doLike)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        String operationId = Operator.with(fr.getActivity(), SessionUtils.getAccount(fr.getActivity())).load(requestsBuilder);

        if (fr instanceof DocumentFolderBrowserFragment || fr instanceof SyncFragment
                || fr instanceof DocumentFolderSearchFragment)
        {
            int titleId = R.string.unlike;
            int iconId = R.drawable.ic_unlike;
            if (doLike)
            {
                titleId = R.string.like;
                iconId = R.drawable.ic_like;
            }
            OperationWaitingDialogFragment.newInstance(LikeNodeRequest.TYPE_ID, iconId,
                    fr.getActivity().getString(titleId), null, null, selectedItems.size(), operationId).show(
                    fr.getActivity().getSupportFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }
}

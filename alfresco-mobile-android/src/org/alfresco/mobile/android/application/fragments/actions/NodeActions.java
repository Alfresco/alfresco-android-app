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
package org.alfresco.mobile.android.application.fragments.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.properties.UpdateDialogFragment;
import org.alfresco.mobile.android.application.integration.OperationManager;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.OperationRequestGroup;
import org.alfresco.mobile.android.application.integration.node.delete.DeleteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.download.DownloadRequest;
import org.alfresco.mobile.android.application.integration.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.integration.node.like.LikeNodeRequest;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

@TargetApi(11)
public class NodeActions implements ActionMode.Callback
{
    public static final String TAG = "NodeActions";

    private List<Node> selectedNodes = new ArrayList<Node>();

    private List<Folder> selectedFolder = new ArrayList<Folder>();

    private List<Document> selectedDocument = new ArrayList<Document>();

    private onFinishModeListerner mListener;

    private ActionMode mode;

    private Activity activity = null;

    private Fragment fragment;

    private Folder parentFolder;

    public NodeActions(Fragment f, List<Node> selectedNodes)
    {
        this.fragment = f;
        this.activity = f.getActivity();
        this.selectedNodes = selectedNodes;
        for (Node node : selectedNodes)
        {
            addNode(node);
        }
        if (f instanceof ChildrenBrowserFragment)
        {
            this.parentFolder = ((ChildrenBrowserFragment) fragment).getParent();
        }
    }
    
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        this.mode = mode;
        getMenu(menu);
        return false;
    }
    
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        mode.setTitle(createTitle());
        return true;
    }
    
    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        mListener.onFinish();
        selectedNodes.clear();
    }

    public void finish()
    {
        mode.finish();
    }
    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private String createTitle()
    {
        String title = "";

        if (selectedNodes.size() == 1)
        {
            title = selectedNodes.get(0).getName();
        }
        else
        {
            int size = selectedDocument.size();
            if (size > 0)
            {
                title += String.format(activity.getResources().getQuantityString(R.plurals.selected_document, size),
                        size);
            }
            size = selectedFolder.size();
            if (size > 0)
            {
                if (!title.isEmpty())
                {
                    title += " | ";
                }
                title += String.format(activity.getResources().getQuantityString(R.plurals.selected_folders, size),
                        size);
            }
        }

        return title;
    }
    // ///////////////////////////////////////////////////////////////////////////////////
    // LIST MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////////////
    public void selectNode(Node n)
    {
        if (selectedNodes.contains(n))
        {
            removeNode(n);
        }
        else
        {
            addNode(n);
        }
        if (selectedNodes.isEmpty())
        {
            mode.finish();
        }
        else
        {
            mode.setTitle(createTitle());
            mode.invalidate();
        }
    }

    public void selectNodes(List<Node> nodes)
    {
        selectedNodes.clear();
        selectedDocument.clear();
        selectedFolder.clear();
        for (Node node : nodes)
        {
            addNode(node);
        }
        mode.setTitle(createTitle());
        mode.invalidate();
    }
    
    private void addNode(Node n)
    {
        if (n == null) { return; }

        if (!selectedNodes.contains(n))
        {
            selectedNodes.add(n);
        }
        if (n.isDocument())
        {
            selectedDocument.add((Document) n);
        }
        else
        {
            selectedFolder.add((Folder) n);
        }
    }

    private void removeNode(Node n)
    {
        selectedNodes.remove(n);
        if (n.isDocument())
        {
            selectedDocument.remove((Document) n);
        }
        else
        {
            selectedFolder.remove((Folder) n);
        }
    }

    private void selectAll()
    {
        ((ChildrenBrowserFragment) fragment).selectAll();
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////////////
    private void getMenu(Menu menu)
    {
        menu.clear();
        getMenu(activity, menu);
    }

    private void getMenu(Activity activity, Menu menu)
    {
        MenuItem mi;
        SubMenu createMenu;

        if (selectedFolder.isEmpty())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DOWNLOAD_ALL, Menu.FIRST + MenuActionItem.MENU_DOWNLOAD_ALL,
                    R.string.download);
            mi.setIcon(R.drawable.ic_download_dark);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP, Menu.FIRST
                    + MenuActionItem.MENU_FAVORITE_GROUP, R.string.favorite);
            createMenu.setIcon(R.drawable.ic_favorite_dark);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, Menu.FIRST
                    + MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, R.string.favorite);
            createMenu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, Menu.FIRST
                    + MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, R.string.unfavorite);
        }

        createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP, Menu.FIRST
                + MenuActionItem.MENU_LIKE_GROUP, R.string.like);
        createMenu.setIcon(R.drawable.ic_like);
        createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        createMenu.add(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP_LIKE,
                Menu.FIRST + MenuActionItem.MENU_LIKE_GROUP_LIKE, R.string.like);
        createMenu.add(Menu.NONE, MenuActionItem.MENU_LIKE_GROUP_UNLIKE, Menu.FIRST
                + MenuActionItem.MENU_LIKE_GROUP_UNLIKE, R.string.unlike);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE, R.string.delete);
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_SELECT_ALL, Menu.FIRST + MenuActionItem.MENU_SELECT_ALL,
                R.string.select_all);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        mi = menu.add(Menu.NONE, MenuActionItem.MENU_OPERATIONS, Menu.FIRST + MenuActionItem.MENU_OPERATIONS,
                R.string.operations);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
    
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DETAILS:
                ((MainActivity) activity).addPropertiesFragment(selectedNodes.get(0));
                DisplayUtils.switchSingleOrTwo(activity, false);
                break;
            case MenuActionItem.MENU_UPDATE:
                update(activity.getFragmentManager().findFragmentByTag(DetailsFragment.TAG));
                b = true;
                break;
            case MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE:
                favorite(true);
                b = true;
                break;
            case MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE:
                favorite(false);
                b = true;
                break;
            case MenuActionItem.MENU_LIKE_GROUP_LIKE:
                like(true);
                b = true;
                break;
            case MenuActionItem.MENU_LIKE_GROUP_UNLIKE:
                like(false);
                b = true;
                break;
            case MenuActionItem.MENU_DOWNLOAD_ALL:
            case MenuActionItem.MENU_DOWNLOAD:
                download();
                b = true;
                break;
            case MenuActionItem.MENU_EDIT:
                edit(activity, parentFolder, selectedNodes.get(0));
                b = true;
                break;
            case MenuActionItem.MENU_DELETE:
            case MenuActionItem.MENU_DELETE_FOLDER:
                delete(activity, fragment, new ArrayList<Node>(selectedNodes));
                b = true;
                break;
            case MenuActionItem.MENU_SELECT_ALL:
                selectAll();
                b = false;
                break;
            case MenuActionItem.MENU_OPERATIONS:
                activity.startActivity( new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS));
                b = false;
                break;
            default:
                break;
        }
        if (b)
        {
            selectedNodes.clear();
            selectedDocument.clear();
            selectedFolder.clear();
            mode.finish();
        }
        return b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void favorite(boolean doFavorite)
    {
        OperationRequestGroup group = new OperationRequestGroup(activity, SessionUtils.getAccount(activity));
        for (Node node : selectedNodes)
        {
            group.enqueue(new FavoriteNodeRequest(parentFolder, node, doFavorite)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }

        OperationManager.getInstance(activity).enqueue(group);

        if (fragment instanceof ChildrenBrowserFragment)
        {
            int titleId = R.string.unfavorite;
            int iconId = R.drawable.ic_unfavorite_dark;
            if (doFavorite)
            {
                titleId = R.string.favorite;
                iconId = R.drawable.ic_favorite_dark;
            }
            OperationWaitingDialogFragment.newInstance(FavoriteNodeRequest.TYPE_ID, iconId,
                    fragment.getString(titleId), null, parentFolder, selectedNodes.size()).show(
                    fragment.getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
        }
    }

    private void like(boolean doLike)
    {
        OperationRequestGroup group = new OperationRequestGroup(activity, SessionUtils.getAccount(activity));
        for (Node node : selectedNodes)
        {
            group.enqueue(new LikeNodeRequest(parentFolder, node, doLike)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        }
        OperationManager.getInstance(activity).enqueue(group);

        if (fragment instanceof ChildrenBrowserFragment)
        {
            int titleId = R.string.unlike;
            int iconId = R.drawable.ic_unlike;
            if (doLike)
            {
                titleId = R.string.like;
                iconId = R.drawable.ic_like;
            }
            OperationWaitingDialogFragment.newInstance(LikeNodeRequest.TYPE_ID, iconId, fragment.getString(titleId),
                    null, parentFolder, selectedNodes.size()).show(fragment.getActivity().getFragmentManager(),
                    OperationWaitingDialogFragment.TAG);
        }
    }
    
    public static void download(Activity activity, Document doc)
    {
        OperationRequestGroup group = new OperationRequestGroup(activity, SessionUtils.getAccount(activity));
        group.enqueue(new DownloadRequest(doc));
        OperationManager.getInstance(activity).enqueue(group);
    }

    public void download()
    {
        OperationRequestGroup group = new OperationRequestGroup(activity, SessionUtils.getAccount(activity));
        for (Document doc : selectedDocument)
        {
            group.enqueue(new DownloadRequest(parentFolder, doc));
        }
        OperationManager.getInstance(activity).enqueue(group);
    }

    public static void delete(final Activity activity, final Fragment f, Node node)
    {
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        delete(activity, f, nodes);
    }

    public static void delete(final Activity activity, final Fragment f, final List<Node> nodes)
    {
        Folder tmpParent = null;
        if (f instanceof ChildrenBrowserFragment)
        {
            tmpParent = ((ChildrenBrowserFragment) f).getParent();
        }
        else if (f instanceof DetailsFragment)
        {
            tmpParent = ((DetailsFragment) f).getParentNode();
        }
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
                OperationRequestGroup group = new OperationRequestGroup(activity, SessionUtils.getAccount(activity));

                if (nodes.size() == 1)
                {
                    group.enqueue(new DeleteNodeRequest(parent, nodes.get(0))
                            .setNotificationVisibility(OperationRequest.VISIBILITY_TOAST));
                }
                else
                {
                    for (Node node : nodes)
                    {
                        group.enqueue(new DeleteNodeRequest(parent, node)
                                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                    }

                    if (f instanceof ChildrenBrowserFragment)
                    {
                        OperationWaitingDialogFragment.newInstance(DeleteNodeRequest.TYPE_ID, R.drawable.ic_delete,
                                f.getString(R.string.delete), null, parent, nodes.size()).show(
                                f.getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
                    }
                }

                OperationManager.getInstance(activity).enqueue(group);

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

    public static void edit(final Activity activity, final Folder folder, final Node node)
    {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        UpdateDialogFragment newFragment = UpdateDialogFragment.newInstance(folder, node);
        newFragment.show(ft, UpdateDialogFragment.TAG);
    }

    public static void update(Fragment f)
    {
        ActionManager.actionPickFile(f, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    public static File getDownloadFile(final Activity activity, final Node node)
    {
        if (activity != null && node != null && SessionUtils.getAccount(activity) != null)
        {
            File folder = StorageManager.getDownloadFolder(activity, SessionUtils.getAccount(activity));
            if (folder != null) { return new File(folder, node.getName()); }
        }

        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////////////
    public interface onFinishModeListerner
    {
        void onFinish();
    }
    
    public void setOnFinishModeListerner(onFinishModeListerner mListener)
    {
        this.mListener = mListener;
    }
}

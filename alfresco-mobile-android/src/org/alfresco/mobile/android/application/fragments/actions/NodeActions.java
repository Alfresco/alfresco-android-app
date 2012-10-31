/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.CommentCreateLoader;
import org.alfresco.mobile.android.api.asynchronous.NodeDeleteLoader;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.properties.UpdateDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.actions.DeleteLoaderCallback;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeDeleteListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.DownloadManager.Request;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

@TargetApi(11)
public class NodeActions implements ActionMode.Callback
{

    private ArrayList<Node> nodes = new ArrayList<Node>();

    private onFinishModeListerner mListener;

    private ActionMode mode;

    private Activity activity;

    private Fragment fragment;

    public NodeActions(Fragment f, Node node)
    {
        this.fragment = f;
        this.activity = f.getActivity();
        nodes.add(node);
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ACTION MODE
    // ///////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_UPDATE:
                update(activity.getFragmentManager().findFragmentByTag(DetailsFragment.TAG));
                b = true;
                break;
            case MenuActionItem.MENU_DOWNLOAD:
                download(activity, nodes.get(0));
                b = true;
                break;
            case MenuActionItem.MENU_EDIT:
                edit(activity, nodes.get(0));
                b = true;
                break;
            case MenuActionItem.MENU_DELETE:
            case MenuActionItem.MENU_DELETE_FOLDER:
                delete(activity, fragment, nodes.get(0));
                b = true;
                break;
            default:
                break;
        }
        mode.finish();
        nodes.clear();
        return b;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        return true;
    }

    private void getMenu(Menu menu, Node node)
    {
        menu.clear();
        if (node.isDocument())
        {
            DetailsFragment.getMenu(SessionUtils.getSession(activity), activity, menu, node);
        }
        else
        {
            ChildrenBrowserFragment.getMenu(SessionUtils.getSession(activity), menu, (Folder) node, true);
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        mListener.onFinish();
        nodes.clear();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        this.mode = mode;
        getMenu(menu, nodes.get(0));
        return false;
    }

    public void addNode(Node n)
    {
        nodes.clear();
        nodes.add(n);
        mode.setTitle(n.getName());
        mode.invalidate();
    }

    public void setOnFinishModeListerner(onFinishModeListerner mListener)
    {
        this.mListener = mListener;
    }

    public void finish()
    {
        mode.finish();
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////////////
    public interface onFinishModeListerner
    {
        public void onFinish();
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////////////
    public static void delete(final Activity activity, final Fragment f, final Node node)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.action_delete);
        builder.setMessage(activity.getResources().getString(R.string.action_delete_desc) + " " + node.getName());
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                Log.d("Delete Node", node.getName());
                DeleteLoaderCallback up = new DeleteLoaderCallback(SessionUtils.getSession(activity), activity, node);
                up.setOnDeleteListener(new OnNodeDeleteListener()
                {
                    @Override
                    public void afterDelete(Node node)
                    {
                        Bundle b = new Bundle();
                        b.putString(PublicIntent.EXTRA_NODE, node.getIdentifier());
                        ActionManager.actionRefresh(f, IntentIntegrator.CATEGORY_REFRESH_DELETE,
                                PublicIntent.NODE_TYPE, b);
                    }

                    @Override
                    public void beforeDelete(Node arg0)
                    {

                    }

                    @Override
                    public void onExeceptionDuringDeletion(Exception arg0)
                    {
                        // TODO Auto-generated method stub
                    }
                });
                activity.getLoaderManager().restartLoader(NodeDeleteLoader.ID, null, up);
                activity.getLoaderManager().getLoader(NodeDeleteLoader.ID).forceLoad();

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

    public static void edit(final Activity activity, final Node node)
    {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        UpdateDialogFragment newFragment = UpdateDialogFragment.newInstance(node);
        newFragment.show(ft, UpdateDialogFragment.TAG);
    }
    
    public static void update(Fragment f){
        ActionManager.actionPickFile(f, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    public static void download(final Activity activity, final Node node)
    {
        Uri uri = Uri.parse(((AbstractDocumentFolderServiceImpl) SessionUtils.getSession(activity).getServiceRegistry()
                .getDocumentFolderService()).getDownloadUrl((Document) node));

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        DownloadManager manager = (DownloadManager) activity.getSystemService(Activity.DOWNLOAD_SERVICE);

        Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setVisibleInDownloadsUi(false).setTitle(node.getName())
                .setDescription(node.getDescription())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(getDownloadFile(activity, node)));

        AuthenticationProvider auth = ((AbstractAlfrescoSessionImpl) SessionUtils.getSession(activity))
                .getAuthenticationProvider();
        Map<String, List<String>> httpHeaders = auth.getHTTPHeaders();
        if (httpHeaders != null)
        {
            for (Map.Entry<String, List<String>> header : httpHeaders.entrySet())
            {
                if (header.getValue() != null)
                {
                    for (String value : header.getValue())
                    {
                        request.addRequestHeader(header.getKey(), value);
                    }
                }
            }
        }

        manager.enqueue(request);
    }

    public static File getDownloadFile(final Activity activity, final Node node)
    {
        File folder = StorageManager.getDownloadFolder(activity, SessionUtils.getAccount(activity).getUrl() + "",
                SessionUtils.getAccount(activity).getUsername());
        return new File(folder, node.getName());
    }

}

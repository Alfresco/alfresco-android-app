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
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.NodeDeleteLoader;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.properties.UpdateDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.documentfolder.actions.DeleteLoaderCallback;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeDeleteListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

    static private Activity activity = null;

    private Fragment fragment;

    public NodeActions(Fragment f, Node node)
    {
        this.fragment = f;
        this.activity = f.getActivity();
        nodes.add(node);
    }
    
    protected void finalize()
    {
        activity = null;
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
        builder.setTitle(R.string.delete);
        builder.setMessage(activity.getResources().getString(R.string.delete_description) + " " + node.getName());
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

    public static void update(Fragment f)
    {
        ActionManager.actionPickFile(f, PublicIntent.REQUESTCODE_FILEPICKER);
    }

    public static void download(final Activity activity, final Node node)
    {
        Uri uri = Uri.parse(((AbstractDocumentFolderServiceImpl) SessionUtils.getSession(activity).getServiceRegistry()
                .getDocumentFolderService()).getDownloadUrl((Document) node));

        File dlFile = getDownloadFile(activity, node);
        if (dlFile == null)
        {
            MessengerManager.showLongToast(activity, activity.getString(R.string.sdinaccessible));
            return; 
        }

        DownloadManager manager = (DownloadManager) activity.getSystemService(Activity.DOWNLOAD_SERVICE);

        Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setVisibleInDownloadsUi(false).setTitle(node.getName())
                .setDescription(node.getDescription())
                .setDestinationUri(Uri.fromFile(dlFile));
        
        if (AndroidVersion.isICSOrAbove()){
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        } else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        }

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
        if (activity != null && node != null && SessionUtils.getAccount(activity) != null)
        {
            File folder = StorageManager.getDownloadFolder(activity, SessionUtils.getAccount(activity).getUrl(), SessionUtils.getAccount(activity).getUsername());
            if (folder != null)
            {
                return new File(folder, node.getName());
            }
        }
        
        return null;
    }

    static class DownloadReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Query query = new Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
                if (c.moveToFirst())
                {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex))
                    {
                        Uri filenameUri = Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                        String filename = filenameUri.getPath();
                        
                        //Only if we're currently in a download initiated with Activity present
                        if (activity != null  &&  StorageManager.shouldEncryptDecrypt(context, filename))
                        {
                            FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                            EncryptionDialogFragment fragment = EncryptionDialogFragment.encrypt(filename);
                            fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                            fragmentTransaction.commit();
                        }
                    }
                }
            }
        }
    };
}

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

import java.util.ArrayList;

import org.alfresco.mobile.android.api.asynchronous.NodeDeleteLoader;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.actions.DeleteLoaderCallback;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeDeleteListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DELETE:
                Log.d("Delete Node", nodes.get(0).getName());
                delete(activity, fragment, nodes.get(0));
                mode.finish();
                nodes.clear();
                return true;
            default:
                break;
        }
        return false;
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
           DetailsFragment.getMenu(SessionUtils.getsession(activity), activity, menu, node);
        } else {
            ChildrenBrowserFragment.getMenu(SessionUtils.getsession(activity), menu, (Folder) node, true);
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
                DeleteLoaderCallback up = new DeleteLoaderCallback(SessionUtils.getsession(activity), activity, node);
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

}

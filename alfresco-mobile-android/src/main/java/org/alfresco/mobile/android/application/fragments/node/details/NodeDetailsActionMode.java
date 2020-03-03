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
package org.alfresco.mobile.android.application.fragments.node.details;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class NodeDetailsActionMode extends AbstractActions<Node>
{

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public NodeDetailsActionMode(Fragment f, Node selectedNode)
    {
        this.fragmentRef = new WeakReference<>(f);
        this.activityRef = new WeakReference<>(f.getActivity());
        this.selectedItems = new ArrayList<>(1);
        addNode(selectedNode);
        this.multiSelectionEnable = false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected String createTitle()
    {
        String title = "";
        if (selectedItems.size() == 1)
        {
            title = getCurrentItem().getName();
        }
        return title;
    }

    protected void getMenu(FragmentActivity activity, Menu menu)
    {
        if (selectedItems.isEmpty() || selectedItems.size() > 1) { return; }

        MenuItem mi;
        Node node = getCurrentItem();
        if (node == null) { return; }
        if (node instanceof NodeSyncPlaceHolder) { return; }

        boolean isRestrict = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0 && !isRestrict && ConfigurableActionHelper
                    .isVisible(getActivity(), getAccount(), ConfigurableActionHelper.ACTION_NODE_DOWNLOAD))
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

            if (!(SessionUtils.getSession(activity) instanceof CloudSession) && ConfigurableActionHelper
                    .isVisible(getActivity(), getAccount(), ConfigurableActionHelper.ACTION_NODE_REVIEW))
            {
                mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST + 500, R.string.process_start_review);
                mi.setIcon(R.drawable.ic_start_review);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), node,
                ConfigurableActionHelper.ACTION_NODE_EDIT))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST + 50, R.string.edit);
            mi.setIcon(R.drawable.ic_properties);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (ConfigurableActionHelper.isVisible(getActivity(), getAccount(), getSession(), node,
                ConfigurableActionHelper.ACTION_NODE_DELETE))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_action_download:
                ((NodeDetailsFragment) getFragment()).download();
                return true;
            case R.id.menu_action_share:
                ((NodeDetailsFragment) getFragment()).share();
                return true;
            case R.id.menu_action_open:
                ((NodeDetailsFragment) getFragment()).openin();
                return true;
            case R.id.menu_action_update:
                Intent i = new Intent(PrivateIntent.ACTION_PICK_FILE, null, getActivity(),
                        PublicDispatcherActivity.class);
                i.putExtra(PrivateIntent.EXTRA_FOLDER,
                        AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(getAccount()));
                i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                getFragment().startActivityForResult(i, RequestCode.FILEPICKER);
                return true;
            case R.id.menu_action_edit:
                ((NodeDetailsFragment) getFragment()).edit();
                return true;
            case R.id.menu_action_delete:
                ((NodeDetailsFragment) getFragment()).delete();
                return true;
            case R.id.menu_workflow_add:
                Intent in = new Intent(PrivateIntent.ACTION_START_PROCESS, null, getActivity(),
                        PrivateDialogActivity.class);
                in.putExtra(PrivateIntent.EXTRA_DOCUMENT, (Serializable) getCurrentItem());
                in.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                getFragment().startActivity(in);
                return true;
        }
        return false;
    }

}

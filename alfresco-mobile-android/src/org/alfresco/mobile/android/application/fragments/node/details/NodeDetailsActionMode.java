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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.actions.AbstractActions;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.utils.NodeSyncPlaceHolder;
import org.apache.chemistry.opencmis.commons.enums.Action;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
        this.fragmentRef = new WeakReference<Fragment>(f);
        this.activityRef = new WeakReference<Activity>(f.getActivity());
        this.selectedItems = new ArrayList<Node>(1);
        addNode(selectedNode);
        this.multiSelectionEnable = false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected CharSequence createTitle()
    {
        String title = "";
        if (selectedItems.size() == 1)
        {
            title = getCurrentItem().getName();
        }
        return title;
    }

    protected void getMenu(Activity activity, Menu menu)
    {
        if (selectedItems.isEmpty() || selectedItems.size() > 1) { return; }

        MenuItem mi;
        Node node = getCurrentItem();
        if (node == null) { return; }
        if (node instanceof NodeSyncPlaceHolder) { return; }

        boolean isRestrict = node.hasAspect(ContentModel.ASPECT_RESTRICTABLE);

        if (node.isDocument())
        {
            if (((Document) node).getContentStreamLength() > 0 && !isRestrict)
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_DOWNLOAD, Menu.FIRST + MenuActionItem.MENU_DOWNLOAD,
                        R.string.download);
                mi.setIcon(R.drawable.ic_download_dark);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (((Document) node).isLatestVersion()
                    && ((DocumentImpl) node).hasAllowableAction(Action.CAN_SET_CONTENT_STREAM.value()))
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPDATE, Menu.FIRST + MenuActionItem.MENU_UPDATE,
                        R.string.update);
                mi.setIcon(R.drawable.ic_upload);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            if (!(SessionUtils.getSession(activity) instanceof CloudSession))
            {
                mi = menu.add(Menu.NONE, R.id.menu_workflow_add, Menu.FIRST
                        + MenuActionItem.MENU_WORKFLOW_ADD, R.string.process_start_review);
                mi.setIcon(R.drawable.ic_start_review);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (SessionUtils.getSession(activity).getServiceRegistry().getDocumentFolderService().getPermissions(node)
                .canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT, R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (SessionUtils.getSession(activity).getServiceRegistry().getDocumentFolderService().getPermissions(node)
                .canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, 1000 + MenuActionItem.MENU_DELETE, R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }
    
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        Boolean b = false;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_DOWNLOAD:
                ((NodeDetailsFragment) getFragment()).download();
                return true;
            case MenuActionItem.MENU_SHARE:
                ((NodeDetailsFragment) getFragment()).share();
                return true;
            case MenuActionItem.MENU_OPEN_IN:
                ((NodeDetailsFragment) getFragment()).openin();
                return true;
            case MenuActionItem.MENU_UPDATE:
                Intent i = new Intent(PrivateIntent.ACTION_PICK_FILE, null, getActivity(),
                        PublicDispatcherActivity.class);
                i.putExtra(PrivateIntent.EXTRA_FOLDER,
                        AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(getAccount()));
                i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, getAccount().getId());
                getFragment().startActivityForResult(i, RequestCode.FILEPICKER);
            case MenuActionItem.MENU_EDIT:
                ((NodeDetailsFragment) getFragment()).edit();
                return true;
            case MenuActionItem.MENU_DELETE:
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
        if (b)
        {
            selectedItems.clear();
            mode.finish();
        }
        return b;
    }


}

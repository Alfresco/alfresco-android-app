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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.DocumentCreateLoader;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.NavigationFragment;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateFolderDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;
import org.alfresco.mobile.android.ui.manager.ActionManager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

@TargetApi(11)
public class ChildrenBrowserFragment extends NavigationFragment implements RefreshFragment
{

    public static final String TAG = "ChildrenNavigationFragment";

    private ProgressDialog mProgressDialog;

    public ChildrenBrowserFragment()
    {
    }

    public static ChildrenBrowserFragment newInstance(Folder folder)
    {
        return newInstance(folder, null, null);
    }

    public static ChildrenBrowserFragment newInstance(String folderPath)
    {
        return newInstance(null, folderPath, null);
    }

    public static ChildrenBrowserFragment newInstance(Site site)
    {
        return newInstance(null, null, site);
    }

    public static ChildrenBrowserFragment newInstance(Folder parentFolder, String pathFolder, Site site)
    {
        ChildrenBrowserFragment bf = new ChildrenBrowserFragment();
        ListingContext lc = new ListingContext();
        lc.setSortProperty(DocumentFolderService.SORT_PROPERTY_NAME);
        lc.setIsSortAscending(true);
        Bundle b = createBundleArgs(parentFolder, pathFolder, site);
        b.putAll(createBundleArgs(lc, LOAD_AUTO));
        bf.setArguments(b);
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        if (RepositoryVersionHelper.isAlfrescoProduct(alfSession))
        {
            setActivateThumbnail(true);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        DisplayUtils.setLeftTitle(getActivity(), title);
        getActivity().invalidateOptionsMenu();
        super.onStart();
        
        if (parentFolder != null){
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            
            List<String> path = new ArrayList<String>(0);
            path.add("/");
            path.add("alfresco");
            SpinnerAdapter adapter = new PathAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, path);
            
            OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId)
                {
                    return false;
                }
                
            };
            
            getActivity().getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty()){
            hideDetails = selectedItems.get(0).equals(item);
        }

        selectedItems.clear();
        selectedItems.add(item);
        if (nActions != null)
        {
            selectedItems.clear();
            selectedItems.add(item);
            nActions.addNode(selectedItems.get(0));
            refreshListView();
            ((MainActivity) getActivity()).addPropertiesFragment(item);
            return;
        }
        else
        {
            // startSelection(v, (Node) l.getItemAtPosition(position));
        }

        super.onListItemClick(l, v, position, id);

        if (hideDetails)
        {
            ((MainActivity) getActivity()).clearScreen();
            selectedItems.clear();
            refreshListView();
        }
        else
        {
            if (item.isFolder())
            {
                // Browse
                ((MainActivity) getActivity()).addNavigationFragment((Folder) item);
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(item);
                refreshListView();
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    private NodeActions nActions;

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        Node n = (Node) l.getItemAtPosition(position);
        boolean b = startSelection(v, n);
        ((MainActivity) getActivity()).addPropertiesFragment(n);
        return b;
    };

    private boolean startSelection(View v, Node item)
    {
        if (nActions != null) { return false; }

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new NodeActions(ChildrenBrowserFragment.this, item);
        nActions.setOnFinishModeListerner(new onFinishModeListerner()
        {
            @Override
            public void onFinish()
            {
                nActions = null;
                selectedItems.clear();
                refreshListView();
            }
        });
        getActivity().startActionMode(nActions);
        v.setSelected(true);
        v.findViewById(R.id.choose).setVisibility(View.VISIBLE);
        selectedItems.add(item);
        return true;
    }

    // //////////////////////////////////////////////////////////////////////
    // ACTION BAR ITEM
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_FILEPICKER && data != null && data.getData() != null)
        {
            createFile(new File(ActionManager.getPath(getActivity(), data.getData())));
        }
    }

    public void createFile(File f)
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(AddContentDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddContentDialogFragment newFragment = AddContentDialogFragment.newInstance(parentFolder, f);

        newFragment.setOnCreateListener(new OnNodeCreateListener()
        {
            @Override
            public void afterContentCreation(Node node)
            {
                refresh();
            }

            @Override
            public void beforeContentCreation(Folder arg0, String arg1, Map<String, Serializable> arg2, ContentFile arg3)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onExeceptionDuringCreation(Exception arg0)
            {
                // TODO Auto-generated method stub

            }
        });

        newFragment.show(ft, AddContentDialogFragment.TAG);
    }

    public void createFolder()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(CreateFolderDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddFolderDialogFragment newFragment = AddFolderDialogFragment.newInstance(parentFolder);

        newFragment.setOnCreateListener(new OnNodeCreateListener()
        {
            @Override
            public void afterContentCreation(Node node)
            {
                mProgressDialog.dismiss();
                refresh();
            }

            @Override
            public void beforeContentCreation(Folder arg0, String arg1, Map<String, Serializable> arg2, ContentFile arg3)
            {
                mProgressDialog = ProgressDialog.show(getActivity(), "Please wait", "Contacting your server...", true,
                        true, new OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                getActivity().getLoaderManager().destroyLoader(DocumentCreateLoader.ID);
                            }
                        });

            }

            @Override
            public void onExeceptionDuringCreation(Exception arg0)
            {
                mProgressDialog.dismiss();
            }
        });

        newFragment.show(ft, CreateFolderDialogFragment.TAG);
    }

    public void refresh()
    {
        if (parentFolder == null)
        {
            parentFolder = SessionUtils.getSession(getActivity()).getRootFolder();
        }
        super.refresh();
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu)
    {
        getMenu(alfSession, menu, parentFolder);
    }

    public static void getMenu(AlfrescoSession session, Menu menu, Folder parentFolder, boolean extended)
    {
        MenuItem mi;

        if (!extended && parentFolder != null)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SEARCH, Menu.FIRST, R.string.action_search);
            mi.setIcon(R.drawable.ic_search);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        Permissions permission = session.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder);

        if (!extended && parentFolder != null && permission.canAddChildren())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_FOLDER, Menu.FIRST + MenuActionItem.MENU_CREATE_FOLDER,
                    R.string.action_create_folder);
            mi.setIcon(R.drawable.ic_add_folder);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!extended && parentFolder != null && permission.canAddChildren())
        {
            SubMenu devCaptureMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE, R.string.action_upload);
            devCaptureMenu.setIcon(android.R.drawable.ic_menu_add);
            devCaptureMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            devCaptureMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, R.string.action_photo);
            devCaptureMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, R.string.action_video);
            devCaptureMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, R.string.action_audio);
            devCaptureMenu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                    R.string.action_upload);
        }

        if (extended && parentFolder != null && permission.canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                    R.string.action_edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (extended && parentFolder != null && permission.canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE_FOLDER, Menu.FIRST + MenuActionItem.MENU_DELETE_FOLDER,
                    R.string.action_delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!extended && parentFolder != null)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                    R.string.action_refresh);
            mi.setIcon(R.drawable.ic_refresh);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public static void getMenu(AlfrescoSession session, Menu menu, Folder parentFolder)
    {
        getMenu(session, menu, parentFolder, false);
    }

    @Override
    public void onStop()
    {
        if (nActions != null)
        {
            nActions.finish();
        }
        super.onStop();
    }

}

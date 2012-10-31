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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.NodeChildrenLoader;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.NavigationFragment;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateFolderDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

@TargetApi(11)
public class ChildrenBrowserFragment extends NavigationFragment implements RefreshFragment
{

    public static final String TAG = "ChildrenNavigationFragment";

    private boolean shortcutAlreadyVisible = false;

    private Folder importFolder;

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

        setRetainInstance(true);
        alfSession = SessionUtils.getSession(getActivity());
        if (RepositoryVersionHelper.isAlfrescoProduct(alfSession))
        {
            setActivateThumbnail(true);
        }
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (tmpFile != null)
        {
            importFolder = ((MainActivity) getActivity()).getImportParent();
            createFile(tmpFile);
        }
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        getActivity().setTitle(R.string.app_name);
        if (shortcutAlreadyVisible)
        {
            displayPathShortcut();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView listView = (ListView) v.findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setClickable(true);

        return v;
    }

    @Override
    public void onPause()
    {
        getActivity().invalidateOptionsMenu();
        getActivity().setTitle(R.string.app_name);
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        super.onPause();
    }

    private void displayPathShortcut()
    {
        // /QUICK PATH
        if (parentFolder != null)
        {
            //
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            String pathValue = parentFolder.getProperty(PropertyIds.PATH).getValue();

            String[] path = pathValue.split("/");
            if (path.length == 0)
            {
                path = new String[] { "/" };
            }

            List<String> listFolder = new ArrayList<String>(path.length);
            for (int i = path.length - 1; i > -1; i--)
            {
                pathValue = path[i];

                if (pathValue.isEmpty())
                {
                    pathValue = "/";
                }
                listFolder.add(pathValue);
            }

            if (((MainActivity) getActivity()).isDisplayFromSite() != null && listFolder.size() > 3)
            {
                for (int i = 0; i < 3; i++)
                {
                    listFolder.remove(listFolder.size() - 1);
                }
                listFolder.add(listFolder.size() - 1, ((MainActivity) getActivity()).isDisplayFromSite().getTitle());
                listFolder.remove(listFolder.size() - 1);
            }

            SpinnerAdapter adapter = new PathAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    listFolder);

            OnNavigationListener mOnNavigationListener = new OnNavigationListener()
            {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId)
                {
                    for (int i = 0; i < itemPosition; i++)
                    {
                        getFragmentManager().popBackStack();
                    }
                    return true;
                }

            };

            getActivity().getActionBar().setListNavigationCallbacks(adapter, mOnNavigationListener);

            shortcutAlreadyVisible = true;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
        }
        l.setItemChecked(position, true);

        selectedItems.clear();

        if (nActions != null)
        {
            selectedItems.clear();
            selectedItems.add(item);
            nActions.addNode(selectedItems.get(0));
            ((MainActivity) getActivity()).addPropertiesFragment(item);
            return;
        }

        if (item.isDocument() && DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
                FragmentDisplayer.removeFragment(getActivity(), android.R.id.tabcontent);
            }
            selectedItems.clear();
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
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Node>>> loader, LoaderResult<PagingResult<Node>> results)
    {
        if (loader instanceof NodeChildrenLoader)
        {
            parentFolder = ((NodeChildrenLoader) loader).getParentFolder();
            importFolder = parentFolder;
        }

        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), alfSession, R.layout.sdk_list_row, new ArrayList<Node>(0),
                    selectedItems);
        }

        if (results.hasException())
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(true);
        getActivity().invalidateOptionsMenu();
        displayPathShortcut();
    }

    private NodeActions nActions;

    private File tmpFile;

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        Node n = (Node) l.getItemAtPosition(position);
        l.setItemChecked(position, true);
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
            }
        });
        getActivity().startActionMode(nActions);
        selectedItems.clear();
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
            tmpFile = new File(ActionManager.getPath(getActivity(), data.getData()));
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
        AddContentDialogFragment newFragment = AddContentDialogFragment.newInstance(importFolder, f);

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
        tmpFile = null;
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
                ActionManager.actionRefresh(ChildrenBrowserFragment.this, IntentIntegrator.CATEGORY_REFRESH_OTHERS,
                        IntentIntegrator.NODE_TYPE);
            }

            public boolean hasWaiting = false;

            @Override
            public void beforeContentCreation(Folder arg0, String arg1, Map<String, Serializable> arg2, ContentFile arg3)
            {
                if (!hasWaiting && getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null){
                    new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
                }
                hasWaiting = true;
            }

            @Override
            public void onExeceptionDuringCreation(Exception e)
            {
                ActionManager.actionDisplayError(ChildrenBrowserFragment.this, e);
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

    public void delete()
    {
        if (!selectedItems.isEmpty() && selectedItems.size() == 1)
        {
            NodeActions.delete(getActivity(), this, selectedItems.get(0));
        }
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

    public Folder getImportFolder()
    {
        return importFolder;
    }

}

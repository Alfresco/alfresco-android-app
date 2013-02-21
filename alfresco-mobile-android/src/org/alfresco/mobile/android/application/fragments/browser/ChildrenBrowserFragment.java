/*******************************************************************************
0 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.integration.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.documentfolder.NavigationFragment;
import org.alfresco.mobile.android.ui.documentfolder.actions.CreateFolderDialogFragment;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

/**
 * Display a dialogFragment to retrieve information about the content of a
 * specific folder.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(11)
public class ChildrenBrowserFragment extends NavigationFragment implements RefreshFragment
{
    /** If enable, the fragment allows the user to manage the content. */
    public static final int MODE_LISTING = 0;

    /** If enable, the user can't manage the content. It's a read only mode. */
    public static final int MODE_IMPORT = 1;

    public static final String TAG = "ChildrenBrowserFragment";

    private boolean shortcutAlreadyVisible = false;

    private Folder importFolder;

    private File createFile;

    private long lastModifiedDate;

    /** By default, the fragment is in Listing mode. */
    private int mode = MODE_LISTING;

    private Button importButton;

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
        if (alfSession == null)
        {

        }
        else if (RepositoryVersionHelper.isAlfrescoProduct(alfSession))
        {
            setActivateThumbnail(true);
        }

        // In case of Import mode, we disable thumbnails.
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            setActivateThumbnail(false);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        int titleId = R.string.app_name;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            mode = MODE_IMPORT;
            titleId = R.string.import_document_title;
            checkImportButton();
        }

        // If the fragment is resumed after user content creation action, we
        // have to check if the file has been modified or not. Depending on
        // result we prompt the upload dialog or we do nothing (no modification
        // / blank file)
        if (createFile != null)
        {
            if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
            {
                tmpFile = createFile;
            }
            else
            {
                createFile.delete();
                createFile = null;
            }
        }

        if (tmpFile != null)
        {
            importFolder = ((MainActivity) getActivity()).getImportParent();
            createFile(tmpFile);
        }

        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
            getActivity().setTitle(titleId);
            if (shortcutAlreadyVisible)
            {
                displayPathShortcut();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = null;
        // In case of Import mode, we wrap the listing with buttons.
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            v = inflater.inflate(R.layout.app_browser_import, container, false);
            init(v, emptyListMessageId);

            importButton = (Button) v.findViewById(R.id.action_import);
        }
        else
        {
            v = super.onCreateView(inflater, container, savedInstanceState);

            ListView listView = (ListView) v.findViewById(R.id.listView);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setClickable(true);

            listView.setDivider(null);
            listView.setDividerHeight(0);

            listView.setBackgroundColor(getResources().getColor(R.color.grey_lighter));
        }
        return v;
    }

    @Override
    public void onPause()
    {
        getActivity().invalidateOptionsMenu();

        int titleId = R.string.app_name;
        if (getActivity() instanceof PublicDispatcherActivity)
        {
            titleId = R.string.import_document_title;
        }
        getActivity().setTitle(titleId);
        if (getActivity().getActionBar() != null)
        {
            getActivity().getActionBar().setDisplayShowTitleEnabled(true);
            getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        super.onPause();
    }

    private void displayPathShortcut()
    {
        // /QUICK PATH
        if (parentFolder != null && getActivity().getActionBar() != null)
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

        // In case of import mode, we disable selection of document.
        // It's only possible to select a folder for navigation purpose.
        if (mode == MODE_IMPORT && getActivity() instanceof PublicDispatcherActivity)
        {
            l.setChoiceMode(ListView.CHOICE_MODE_NONE);
            if (item.isFolder())
            {
                ((PublicDispatcherActivity) getActivity()).addNavigationFragment((Folder) item);
            }
            return;
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getIdentifier().equals(item.getIdentifier());
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
                // Improvement : Create a common interface ?
                if (getActivity() instanceof MainActivity)
                {
                    ((MainActivity) getActivity()).addNavigationFragment((Folder) item);
                }
                else if (getActivity() instanceof PublicDispatcherActivity)
                {
                    ((PublicDispatcherActivity) getActivity()).addNavigationFragment((Folder) item);
                }
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
        
        if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentNode() != null)
        {
            selectedItems.clear();
            selectedItems.add(((MainActivity) getActivity()).getCurrentNode());
        }
        
        if (loader instanceof NodeChildrenLoader)
        {
            parentFolder = ((NodeChildrenLoader) loader).getParentFolder();
            importFolder = parentFolder;
        }

        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), alfSession, R.layout.sdk_list_row, new ArrayList<Node>(0),
                    selectedItems, mode);
        }

        if (results.hasException())
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(hasActivateThumbnail());
        getActivity().invalidateOptionsMenu();
        displayPathShortcut();
        checkImportButton();
    }

    /**
     * Helper method to enable/disable the import button depending on mode and
     * permission.
     */
    private void checkImportButton()
    {
        if (mode == MODE_IMPORT)
        {
            boolean enable = false;
            if (parentFolder != null)
            {
                Permissions permission = alfSession.getServiceRegistry().getDocumentFolderService()
                        .getPermissions(parentFolder);
                enable = permission.canAddChildren();
            }
            importButton.setEnabled(enable);
        }
    }

    private NodeActions nActions;

    private File tmpFile;

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        // We disable long click during import mode.
        if (mode == MODE_IMPORT) { return false; }

        Node n = (Node) l.getItemAtPosition(position);
        l.setItemChecked(position, true);
        boolean b = startSelection(v, n);
        ((MainActivity) getActivity()).addPropertiesFragment(n);
        DisplayUtils.switchSingleOrTwo(getActivity(), true);
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
        switch (requestCode)
        {
            case PublicIntent.REQUESTCODE_DECRYPTED:
                try
                {
                    String filename = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("RequiresEncrypt", "");
                    if (filename != null && filename.length() > 0)
                    {
                        if (CipherUtils.encryptFile(getActivity(), filename, true) == false)
                            MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                        else
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("RequiresEncrypt", "").commit();
                    }
                }
                catch (Exception e)
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                    e.printStackTrace();
                }
                break;
                
            case PublicIntent.REQUESTCODE_FILEPICKER:
                if (data != null && data.getData() != null)
                {
                    String tmpPath = ActionManager.getPath(getActivity(), data.getData());
                    if (tmpPath != null)
                    {
                        tmpFile = new File(tmpPath);
                        if (StorageManager.shouldEncryptDecrypt(getActivity(), tmpPath))
                        {
                            try
                            {
                                if (CipherUtils.decryptFile(getActivity(), tmpPath) == false)
                                    MessengerManager.showLongToast(getActivity(), getString(R.string.decryption_failed));
                            }
                            catch (Exception e)
                            {
                                MessengerManager.showLongToast(getActivity(), getString(R.string.decryption_failed));
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        // Error case : Unable to find the file path associated
                        // to user pick.
                        // Sample : Picasa image case
                        ActionManager.actionDisplayError(ChildrenBrowserFragment.this, new AlfrescoAppException(
                                getString(R.string.error_unknown_filepath), true));
                    }
                }
                break;
            default:
                break;
        }
    }

    public void createFile(File f)
    {
        // Create and show the dialog.
        AddContentDialogFragment newFragment = AddContentDialogFragment.newInstance(importFolder, f,
                (createFile != null));
        newFragment.show(getActivity().getFragmentManager(), AddContentDialogFragment.TAG);
        tmpFile = null;
        createFile = null;
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
                if (!hasWaiting && getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
                {
                    new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
                }
                hasWaiting = true;
            }

            @Override
            public void onExeceptionDuringCreation(Exception e, Folder parentFolder, String name,
                    Map<String, Serializable> props, ContentFile contentFile)
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

        if (parentFolder == null) { return; }

        Permissions permission = session.getServiceRegistry().getDocumentFolderService().getPermissions(parentFolder);

        if (!extended && parentFolder != null && permission.canAddChildren())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_FOLDER, Menu.FIRST + MenuActionItem.MENU_CREATE_FOLDER,
                    R.string.folder_create);
            mi.setIcon(R.drawable.ic_add_folder);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!extended && parentFolder != null && permission.canAddChildren())
        {
            SubMenu createMenu = menu.addSubMenu(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE, R.string.upload);
            createMenu.setIcon(android.R.drawable.ic_menu_add);
            createMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_CREATE_DOCUMENT, Menu.FIRST
                    + MenuActionItem.MENU_CREATE_DOCUMENT, R.string.create_document);

            createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO, R.string.take_photo);

            if (AndroidVersion.isICSOrAbove())
            {
                createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, Menu.FIRST
                        + MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO, R.string.make_video);
            }

            createMenu.add(Menu.NONE, MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, Menu.FIRST
                    + MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO, R.string.record_audio);
            createMenu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                    R.string.content_upload);

        }

        if (extended && parentFolder != null && permission.canEdit())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT, R.string.edit);
            mi.setIcon(R.drawable.ic_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (extended && parentFolder != null && permission.canDelete())
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE_FOLDER, Menu.FIRST + MenuActionItem.MENU_DELETE_FOLDER,
                    R.string.delete);
            mi.setIcon(R.drawable.ic_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        if (!extended && parentFolder != null)
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                    R.string.refresh);
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

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    public void setCreateFile(File newFile)
    {
        this.createFile = newFile;
        this.lastModifiedDate = newFile.lastModified();
    }

    public void unselect()
    {
        selectedItems.clear();
    }

}

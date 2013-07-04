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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.file.delete.DeleteFileRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Manage all local file actions like a sdcard file manager.
 * 
 * @author Jean Marie Pascal
 */
public class FileActions implements ActionMode.Callback
{

    private List<File> selectedFiles = new ArrayList<File>();

    private List<File> selectedFolder = new ArrayList<File>();

    private List<File> selectedFile = new ArrayList<File>();

    private onFinishModeListerner mListener;

    private ActionMode mode;

    private Activity activity;

    private Fragment fragment;

    public FileActions(Fragment f, List<File> files)
    {
        this.fragment = f;
        this.activity = f.getActivity();
        this.selectedFiles = files;
        for (File file : files)
        {
            addFile(file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ACTION MODE
    // ///////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_UPLOAD:
                upload(fragment, new ArrayList<File>(selectedFiles));
                mode.finish();
                selectedFiles.clear();
                return true;
            case MenuActionItem.MENU_DELETE:
                delete(fragment, new ArrayList<File>(selectedFiles));
                mode.finish();
                selectedFiles.clear();
                return true;
            case MenuActionItem.MENU_EDIT:
                edit(fragment, selectedFiles.get(0));
                mode.finish();
                selectedFiles.clear();
                return true;
            case MenuActionItem.MENU_SHARE:
                share(fragment, selectedFiles);
                mode.finish();
                selectedFiles.clear();
                return true;
            case MenuActionItem.MENU_SEND:
                send(fragment, selectedFiles);
                mode.finish();
                selectedFiles.clear();
                return true;
            case MenuActionItem.MENU_SELECT_ALL:
                selectAll();
                return false;
            default:
                break;
        }
        return false;
    }

    private void upload(Fragment fr, ArrayList<File> files)
    {
        ActionManager.actionSendDocumentsToAlfresco(fr, files);
    }

    private void send(Fragment fragment, List<File> selectedFiles)
    {
        Intent pickResult = new Intent();
        if (selectedFiles.size() == 0)
        {
            pickResult.setData(Uri.fromFile(selectedFiles.get(0)));
        }
        else if (selectedFiles.size() > 0)
        {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            for (File file : selectedFiles)
            {
                Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            pickResult.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        fragment.getActivity().setResult(Activity.RESULT_OK, pickResult);
        fragment.getActivity().finish();

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        return true;
    }

    private void getMenu(Menu menu)
    {
        menu.clear();

        MenuItem mi;

        if (fragment instanceof ListingModeFragment)
        {
            switch (((ListingModeFragment) fragment).getMode())
            {
                case FileExplorerFragment.MODE_LISTING:
                    if (selectedFolder.isEmpty())
                    {
                        mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                                R.string.upload);
                        mi.setIcon(R.drawable.ic_upload);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                        
                        mi = menu.add(Menu.NONE, MenuActionItem.MENU_SHARE, Menu.FIRST + MenuActionItem.MENU_SHARE,
                                R.string.share);
                        mi.setIcon(R.drawable.ic_share);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }

                    mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                            R.string.delete);
                    mi.setIcon(R.drawable.ic_delete);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    break;

                case FileExplorerFragment.MODE_PICK_FILE:

                    if (selectedFolder.isEmpty())
                    {
                        mi = menu.add(Menu.NONE, MenuActionItem.MENU_SEND, Menu.FIRST + MenuActionItem.MENU_SEND,
                                R.string.action_select);
                        mi.setIcon(R.drawable.ic_upload);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }

                    break;

                default:
                    break;
            }
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SELECT_ALL, Menu.FIRST + MenuActionItem.MENU_SELECT_ALL,
                    R.string.select_all);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        mListener.onFinish();
        selectedFiles.clear();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        this.mode = mode;
        getMenu(menu);
        return false;
    }

    public void setOnFinishModeListerner(onFinishModeListerner mListener)
    {
        this.mListener = mListener;
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

        if (selectedFiles.size() == 1)
        {
            title = selectedFiles.get(0).getName();
        }
        else
        {
            int size = selectedFile.size();
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
    public void selectFile(File n)
    {
        if (selectedFiles.contains(n))
        {
            removeNode(n);
        }
        else
        {
            addFile(n);
        }
        if (selectedFiles.isEmpty())
        {
            mode.finish();
        }
        else
        {
            mode.setTitle(createTitle());
            mode.invalidate();
        }
    }

    public void selectFiles(List<File> files)
    {
        selectedFiles.clear();
        selectedFile.clear();
        selectedFolder.clear();
        for (File node : files)
        {
            addFile(node);
        }
        mode.setTitle(createTitle());
        mode.invalidate();
    }

    private void addFile(File file)
    {
        if (file == null) { return; }

        if (!selectedFiles.contains(file))
        {
            selectedFiles.add(file);
        }
        if (file.isFile())
        {
            selectedFile.add(file);
        }
        else
        {
            selectedFolder.add(file);
        }
    }

    private void removeNode(File file)
    {
        selectedFiles.remove(file);
        if (file.isFile())
        {
            selectedFile.remove(file);
        }
        else
        {
            selectedFolder.remove(file);
        }
    }

    private void selectAll()
    {
        if (fragment instanceof FileExplorerFragment)
        {
            ((FileExplorerFragment) fragment).selectAll();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////////////
    public interface onFinishModeListerner
    {
        void onFinish();
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////////////
    public static void share(final Fragment fr, final List<File> files)
    {
        ActionManager.actionSendDocuments(fr, files);
    }

    public static void edit(final Fragment f, final File file)
    {
        FragmentTransaction ft = f.getActivity().getFragmentManager().beginTransaction();
        Fragment prev = f.getActivity().getFragmentManager().findFragmentByTag(FileNameDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        FileNameDialogFragment.newInstance(file.getParentFile(), file).show(ft, FileNameDialogFragment.TAG);
    }

    public static void delete(final Fragment f, final List<File> files)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(f.getActivity());
        builder.setTitle(R.string.delete);
        String nodeDescription = files.size() + "";
        if (files.size() == 1)
        {
            nodeDescription = files.get(0).getName();
        }
        String description = String
                .format(f.getActivity().getResources().getQuantityString(R.plurals.delete_items, files.size()),
                        nodeDescription);
        builder.setMessage(description);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                OperationsRequestGroup group = new OperationsRequestGroup(f.getActivity(), SessionUtils.getAccount(f
                        .getActivity()));

                if (files.size() == 1)
                {
                    group.enqueue(new DeleteFileRequest(files.get(0))
                            .setNotificationVisibility(OperationRequest.VISIBILITY_TOAST));
                }
                else
                {
                    for (File file : files)
                    {
                        group.enqueue(new DeleteFileRequest(file)
                                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                    }

                    if (f instanceof FileExplorerFragment)
                    {
                        OperationWaitingDialogFragment.newInstance(DeleteFileRequest.TYPE_ID, R.drawable.ic_delete,
                                f.getString(R.string.delete), null, null, files.size()).show(
                                f.getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
                    }
                }

                BatchOperationManager.getInstance(f.getActivity()).enqueue(group);

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

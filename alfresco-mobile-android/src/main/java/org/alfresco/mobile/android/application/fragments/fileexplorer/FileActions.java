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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.delete.DeleteFileRequest;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.operation.OperationWaitingDialogFragment;

import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

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

    protected WeakReference<Fragment> fragmentRef;

    public FileActions(Fragment f, List<File> files)
    {
        this.fragmentRef = new WeakReference<Fragment>(f);
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
            case R.id.menu_upload:
                upload(getFragment(), new ArrayList<File>(selectedFiles));
                mode.finish();
                selectedFiles.clear();
                return true;
            case R.id.menu_action_delete:
                delete(getFragment(), new ArrayList<File>(selectedFiles));
                mode.finish();
                selectedFiles.clear();
                return true;
            case R.id.menu_action_edit:
                edit(getFragment(), selectedFiles.get(0));
                mode.finish();
                selectedFiles.clear();
                return true;
            case R.id.menu_action_share:
                share(getFragment(), selectedFiles);
                mode.finish();
                selectedFiles.clear();
                return true;
            case R.id.menu_file_send:
                send(getFragment(), selectedFiles);
                mode.finish();
                selectedFiles.clear();
                return true;
            case R.id.menu_select_all:
                selectAll();
                return false;
            default:
                break;
        }
        return false;
    }

    private void upload(Fragment fr, ArrayList<File> files)
    {
        ActionUtils.actionSendDocumentsToAlfresco(fr, files);
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
        fragment.getActivity().setResult(FragmentActivity.RESULT_OK, pickResult);
        fragment.getActivity().finish();

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        mode.setTitle(createTitle());
        return true;
    }

    private void getMenu(Menu menu)
    {
        menu.clear();

        MenuItem mi;

        if (getFragment() instanceof ListingModeFragment)
        {
            switch (((ListingModeFragment) getFragment()).getMode())
            {
                case FileExplorerFragment.MODE_LISTING:
                    if (selectedFolder.isEmpty())
                    {
                        mi = menu.add(Menu.NONE, R.id.menu_upload, Menu.FIRST + 30, R.string.upload);
                        mi.setIcon(R.drawable.ic_upload);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                        mi = menu.add(Menu.NONE, R.id.menu_action_share, Menu.FIRST + 100, R.string.share);
                        mi.setIcon(R.drawable.ic_share);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }

                    mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
                    mi.setIcon(R.drawable.ic_delete);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    break;

                case FileExplorerFragment.MODE_PICK:

                    if (selectedFolder.isEmpty())
                    {
                        mi = menu.add(Menu.NONE, R.id.menu_file_send, Menu.FIRST, R.string.action_upload);
                        mi.setIcon(R.drawable.ic_upload);
                        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }

                    break;

                default:
                    break;
            }
            mi = menu.add(Menu.NONE, R.id.menu_select_all, Menu.FIRST + 200, R.string.select_all);
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
        if (mode != null)
        {
            mode.finish();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private String createTitle()
    {
        String title = "";

        if (selectedFile.size() == 1)
        {
            title = getFragment().getString(R.string.multi_selection_enable);
        }
        else
        {
            int size = selectedFile.size();
            if (size > 0)
            {
                title += String.format(
                        getFragment().getResources().getQuantityString(R.plurals.selected_document, size), size);
            }
            size = selectedFolder.size();
            if (size > 0)
            {
                if (!title.isEmpty())
                {
                    title += " | ";
                }
                title += String.format(getFragment().getResources().getQuantityString(R.plurals.selected_folders, size),
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
        if (mode == null) { return; }
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
        if (mode == null) { return; }
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
        if (getFragment() instanceof FileExplorerFragment)
        {
            ((FileExplorerFragment) getFragment()).selectAll();
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
        ActionUtils.actionSendDocuments(fr, files);
    }

    public static void edit(final Fragment f, final File file)
    {
        FragmentDisplayer.with(f.getActivity()).remove(FileNameDialogFragment.TAG);

        // Create and show the dialog.
        FileNameDialogFragment.newInstance(file.getParentFile(), file)
                .show(f.getActivity().getSupportFragmentManager().beginTransaction(), FileNameDialogFragment.TAG);
    }

    public static void delete(final Fragment f, final List<File> files)
    {
        String nodeDescription = files.size() + "";
        if (files.size() == 1)
        {
            nodeDescription = files.get(0).getName();
        }
        String description = String.format(
                f.getActivity().getResources().getQuantityString(R.plurals.delete_items, files.size()),
                nodeDescription);

        new MaterialDialog.Builder(f.getActivity()).iconRes(R.drawable.ic_application_logo).title(R.string.delete)
                .content(Html.fromHtml(description)).positiveText(R.string.confirm).negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        String operationId;

                        if (files.size() == 1)
                        {
                            operationId = Operator.with(f.getActivity())
                                    .load(new DeleteFileRequest.Builder(files.get(0))
                                            .setNotificationVisibility(OperationRequest.VISIBILITY_TOAST));
                        }
                        else
                        {
                            List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(files.size());
                            for (File file : files)
                            {
                                requestsBuilder.add(new DeleteFileRequest.Builder(file)
                                        .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                            }
                            operationId = Operator.with(f.getActivity()).load(requestsBuilder);

                            if (f instanceof FileExplorerFragment)
                            {
                                OperationWaitingDialogFragment
                                        .newInstance(DeleteFileRequest.TYPE_ID, R.drawable.ic_delete,
                                                f.getString(R.string.delete), null, null, files.size(), operationId)
                                        .show(f.getActivity().getSupportFragmentManager(),
                                                OperationWaitingDialogFragment.TAG);
                            }
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog)
                    {
                        dialog.dismiss();
                    }
                }).show();
    }

    // /////////////////////////////////////////////////////////////
    // UTILITIES
    // ////////////////////////////////////////////////////////////
    protected Fragment getFragment()
    {
        return fragmentRef.get();
    }
}

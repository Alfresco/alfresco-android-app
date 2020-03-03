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
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.capture.DeviceCaptureHelper;
import org.alfresco.mobile.android.application.configuration.model.view.LocalConfigModel;
import org.alfresco.mobile.android.application.configuration.model.view.LocalFilesConfigModel;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.ListingFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.fragments.utils.OpenAsDialogFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.DataProtectionManagerImpl;
import org.alfresco.mobile.android.async.file.browse.FilesEvent;
import org.alfresco.mobile.android.async.file.create.CreateDirectoryEvent;
import org.alfresco.mobile.android.async.file.delete.DeleteFileEvent;
import org.alfresco.mobile.android.async.file.encryption.FileProtectionEvent;
import org.alfresco.mobile.android.async.file.update.RenameFileEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils.ActionManagerListener;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Pair;
import android.view.View;
import android.widget.GridView;

import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.otto.Subscribe;

/**
 * LocalFileBrowserFragment is responsible to display the content of Download
 * Folder.
 * 
 * @author Jean Marie Pascal
 */
public class FileExplorerFragment extends FileExplorerFoundationFragment
{
    public static final String TAG = FileExplorerFragment.class.getName();

    private File privateFolder;

    private FileActions nActions;

    private File createFile;

    private boolean isShortCut = false;

    private long lastModifiedDate;

    private int menuId;

    private boolean hasAudioRecorder = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public FileExplorerFragment()
    {
        emptyListMessageId = R.string.empty_download;
        setHasOptionsMenu(true);
        screenName = AnalyticsManager.SCREEN_LOCAL_FILES_BROWSER;
    }

    public static FileExplorerFragment newInstanceByTemplate(Bundle b)
    {
        FileExplorerFragment cbf = new FileExplorerFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void displayTitle()
    {
        if (isShortCut)
        {
            FileExplorerHelper.displayNavigationMode((AlfrescoActivity) getActivity(), getMode(), false, menuId);
            getActionBar().setDisplayUseLogoEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(false);
        }
        else
        {
            super.displayTitle();
        }
    }

    protected void onRetrieveParameters(Bundle bundle)
    {
        super.onRetrieveParameters(bundle);
        isShortCut = getArguments().getBoolean(ARGUMENT_SHORTCUT);
        menuId = getArguments().getInt(ARGUMENT_MENU_ID);
    }

    @Override
    @Subscribe
    public void onResult(FilesEvent event)
    {
        super.onResult(event);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        AlfrescoAccount acc = SessionUtils.getAccount(getActivity());
        Bundle b = getArguments();
        if (b == null)
        {
            if (acc != null)
            {
                parent = AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(acc);
                if (parent == null)
                {
                    AlfrescoNotificationManager.getInstance(getActivity())
                            .showLongToast(getString(R.string.sdinaccessible));
                    return;
                }
            }
            else
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(getString(R.string.loginfirst));
                return;
            }
        }
        if (AlfrescoStorageManager.getInstance(getActivity()) != null
                && AlfrescoStorageManager.getInstance(getActivity()).getRootPrivateFolder() != null)
        {
            privateFolder = AlfrescoStorageManager.getInstance(getActivity()).getRootPrivateFolder().getParentFile();
        }
        displayTitle();

        // Test Audio Recording
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        hasAudioRecorder = intent.resolveActivity(getActivity().getPackageManager()) != null;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // If the fragment is resumed after user content creation action, we
        // have to check if the file has been modified or not. Depending on
        // result we prompt the upload dialog or we do nothing (no modification
        // / blank file)
        if (createFile != null)
        {
            if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
            {
                refresh();
            }
            else
            {
                createFile.delete();
            }
        }

        getActivity().invalidateOptionsMenu();
        refreshListView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case RequestCode.CREATE:
                if (createFile != null)
                {
                    if (createFile.length() > 0 && lastModifiedDate < createFile.lastModified())
                    {
                        refresh();
                    }
                    else
                    {
                        createFile.delete();
                    }
                }
                break;
            default:
                break;
        }
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

    // //////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
    {
        final File file = (File) l.getItemAtPosition(position);

        if (getMode() == MODE_PICK)
        {
            if (nActions != null)
            {
                nActions.selectFile(file);
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (file.isDirectory())
                {
                    displayNavigation(file, true);
                }
                else
                {
                    Intent pickResult = new Intent();
                    pickResult.setData(Uri.fromFile(file));
                    getActivity().setResult(FragmentActivity.RESULT_OK, pickResult);
                    getActivity().finish();
                }
            }
            return;
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getPath().equals(file.getPath());
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectFile(file);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
        }

        if (hideDetails)
        {
            return;
        }
        else if (nActions == null)
        {
            if (file.isDirectory())
            {
                displayNavigation(file, true);
            }
            else
            {
                ActionUtils.actionView(this, file, new ActionManagerListener()
                {
                    @Override
                    public void onActivityNotFoundException(ActivityNotFoundException e)
                    {
                        OpenAsDialogFragment.newInstance(file).show(getActivity().getSupportFragmentManager(),
                                OpenAsDialogFragment.TAG);
                    }
                });
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onListItemLongClick(GridView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        File item = (File) l.getItemAtPosition(position);

        selectedItems.clear();
        selectedItems.add(item);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new FileActions(FileExplorerFragment.this, selectedItems);
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
        adapter.notifyDataSetChanged();

        return true;
    }

    private void displayNavigation(File file, boolean backstack)
    {
        if (getMode() == MODE_PICK)
        {
            FileExplorerFragment.with(getActivity()).file(file).menuId(menuId).isShortCut(true).mode(getMode())
                    .display();
        }
        else
        {
            FileExplorerFragment.with(getActivity()).file(file).display();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // MENU
    // //////////////////////////////////////////////////////////////////////
    @Override
    protected View.OnClickListener onPrepareFabClickListener()
    {
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return null; }
        if (mode != MODE_LISTING) { return null; }
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BottomSheet.Builder builder = new BottomSheet.Builder(getActivity(), R.style.M_StyleDialog)
                        .title(R.string.add_menu);

                if (parent != null && privateFolder != null && !parent.getPath().startsWith(privateFolder.getPath()))
                {
                    builder.sheet(R.id.menu_create_folder, R.drawable.ic_repository_light, R.string.folder_create);
                }
                builder.sheet(R.id.menu_create_document, R.drawable.ic_doc_light, R.string.create_document);
                builder.sheet(R.id.menu_device_capture_camera_photo, R.drawable.ic_camera, R.string.take_photo);
                builder.sheet(R.id.menu_device_capture_camera_video, R.drawable.ic_videos, R.string.make_video);
                if (hasAudioRecorder)
                {
                    builder.sheet(R.id.menu_device_capture_mic_audio, R.drawable.ic_microphone, R.string.record_audio);
                }
                if (ScanSnapManager.getInstance(getActivity()) != null
                        && ScanSnapManager.getInstance(getActivity()).hasScanSnapApplication())
                {
                    builder.sheet(R.id.menu_scan_document, R.drawable.ic_camera, R.string.scan);
                }
                builder.grid().listener(new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        onOptionsItemSelected(which);
                    }
                }).show();
            }
        };
    }

    public boolean onOptionsItemSelected(int id)
    {
        switch (id)
        {
            case R.id.menu_create_folder:
                createFolder();
                return true;
            case R.id.menu_create_document:
                DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment
                        .newInstance(SessionUtils.getAccount(getActivity()), TAG);
                dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
                return true;
            case R.id.menu_device_capture_camera_photo:
            case R.id.menu_device_capture_camera_video:
            case R.id.menu_device_capture_mic_audio:
                Pair<DeviceCapture, String> capture = DeviceCaptureHelper.createDeviceCapture((BaseActivity) getActivity(), id, false);
                if (getActivity() instanceof MainActivity)
                {
                    ((MainActivity) getActivity()).setCapture(capture.first, capture.second);
                }
                return true;
            case R.id.menu_scan_document:
                if (ScanSnapManager.getInstance(getActivity()) != null)
                {
                    ScanSnapManager.getInstance(getActivity()).startPresetChooser(getActivity());
                }
                return true;
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////
    public void setCreateFile(File newFile)
    {
        this.createFile = newFile;
        this.lastModifiedDate = newFile.lastModified();
    }

    public void selectAll()
    {
        if (nActions != null && adapter != null)
        {
            nActions.selectFiles(((FileExplorerAdapter) adapter).getFiles());
            adapter.notifyDataSetChanged();
        }
    }

    public File getParent()
    {
        return parent;
    }

    /**
     * Remove a site object inside the listing without requesting an HTTP call.
     */
    public void remove(File file)
    {
        if (adapter != null)
        {
            ((FileExplorerAdapter) adapter).remove(file.getPath());
            if (adapter.isEmpty())
            {
                displayEmptyView();
            }
        }
    }

    public void createFolder()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(FileNameDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }

        // Create and show the dialog.
        FileNameDialogFragment.newInstance(getParent()).show(ft, FileNameDialogFragment.TAG);

    }

    // //////////////////////////////////////////////////////////////////////
    // EVENTS RECEIVER
    // //////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onCreateDirectoryEvent(CreateDirectoryEvent event)
    {
        if (event.hasException) { return; }
        if (event.parentFolder.equals(getParent().getPath()))
        {
            ((FileExplorerAdapter) adapter).replaceFile(event.data);
        }
        refresh();
        gv.setSelection(selectedPosition);
    }

    @Subscribe
    public void onDeleteFileEvent(DeleteFileEvent event)
    {
        if (event.hasException) { return; }
        remove(event.data);
        refresh();
        gv.setSelection(selectedPosition);
    }

    @Subscribe
    public void onUpdateFileEvent(RenameFileEvent event)
    {
        if (event.hasException) { return; }
        if (event.parentFolder.equals(getParent().getPath()))
        {
            remove(event.originalFile);
            ((FileExplorerAdapter) adapter).replaceFile(event.data);
        }
        refresh();
        gv.setSelection(selectedPosition);
    }

    @Subscribe
    public void onFileProtectionEvent(FileProtectionEvent event)
    {
        if (event.hasException) { return; }
        if (getFragment(WaitingDialogFragment.TAG) != null)
        {
            ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
        }
        if (!event.encryptionAction)
        {
            DataProtectionManagerImpl.getInstance(getActivity()).executeAction(getActivity(), event.intentAction,
                    event.protectedFile);
        }
        refresh();
    }

    private Fragment getFragment(String tag)
    {
        return getActivity().getSupportFragmentManager().findFragmentByTag(tag);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends ListingFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
            sessionRequired = false;
            viewConfigModel = new LocalConfigModel(configuration);
            templateArguments = new String[] { ARGUMENT_FILE, ARGUMENT_PATH, ARGUMENT_SHORTCUT, ARGUMENT_MENU_ID };
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder file(File parentFolder)
        {
            extraConfiguration.putSerializable(LocalFilesConfigModel.ARGUMENT_FILE, parentFolder);
            return this;
        }

        public Builder isShortCut(boolean isShortCut)
        {
            extraConfiguration.putBoolean(LocalFilesConfigModel.ARGUMENT_SHORTCUT, isShortCut);
            return this;
        }

        public Builder menuId(int menuId)
        {
            extraConfiguration.putInt(LocalFilesConfigModel.ARGUMENT_MENU_ID, menuId);
            return this;
        }

        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

    }
}

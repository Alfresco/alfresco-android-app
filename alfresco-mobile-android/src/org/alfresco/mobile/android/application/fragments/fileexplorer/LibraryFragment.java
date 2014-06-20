/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.fragments.BaseCursorListFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ListView;

public class LibraryFragment extends BaseCursorListFragment implements ListingModeFragment
{
    public static final String TAG = LibraryFragment.class.getName();

    protected List<File> selectedItems = new ArrayList<File>();

    private int mediaTypeId;

    private static final String PARAM_MEDIATYPE_ID = "org.alfresco.mobile.android.application.param.mediatypeid";

    private static final String PARAM_SHORTCUT = "org.alfresco.mobile.android.application.param.shortcut";

    private static final String PARAM_MENUID = "org.alfresco.mobile.android.application.param.menu.id";

    public LibraryFragment()
    {
        emptyListMessageId = R.string.empty_download;
        title = R.string.accounts_manage;
    }

    public static LibraryFragment newInstance(int mediaType)
    {
        return newInstance(mediaType, MODE_LISTING);
    }

    public static LibraryFragment newInstance(int mediaType, int displayMode)
    {
        LibraryFragment bf = new LibraryFragment();
        Bundle settings = new Bundle();
        settings.putInt(PARAM_MEDIATYPE_ID, mediaType);
        settings.putInt(PARAM_MODE, displayMode);
        bf.setArguments(settings);
        return bf;
    }

    public static LibraryFragment newInstance(int mediaType, int displayMode, boolean displayShortcut, int menuId)
    {
        LibraryFragment bf = new LibraryFragment();
        Bundle settings = new Bundle();
        settings.putInt(PARAM_MEDIATYPE_ID, mediaType);
        settings.putInt(PARAM_MODE, displayMode);
        settings.putBoolean(PARAM_SHORTCUT, displayShortcut);
        settings.putInt(PARAM_MENUID, menuId);
        bf.setArguments(settings);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mediaTypeId = (Integer) getArguments().get(PARAM_MEDIATYPE_ID);

        adapter = new LibraryCursorAdapter(this, null, R.layout.app_list_progress_row, selectedItems, mediaTypeId,
                getMode());
        lv.setAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart()
    {
        if (getDialog() == null && getArguments().getBoolean(PARAM_SHORTCUT))
        {
            getActivity().getActionBar().show();
            FileExplorerHelper.displayNavigationMode(getActivity(), getMode(), false,
                    getArguments().getInt(PARAM_MENUID));
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        getActivity().invalidateOptionsMenu();
        super.onStart();
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

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // ///////////////////////////////////////////////////////////////////////////
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        File selectedFile = new File(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));

        if (getMode() == MODE_PICK)
        {
            if (nActions != null)
            {
                nActions.selectFile(selectedFile);
                adapter.notifyDataSetChanged();
            }
            else
            {
                Intent pickResult = new Intent();
                pickResult.setData(Uri.fromFile(selectedFile));
                getActivity().setResult(Activity.RESULT_OK, pickResult);
                getActivity().finish();
            }
            return;
        }

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).getPath().equals(selectedFile.getPath());
        }
        l.setItemChecked(position, true);

        if (nActions != null)
        {
            nActions.selectFile(selectedFile);
            if (selectedItems.size() == 0)
            {
                hideDetails = true;
            }
        }
        else
        {
            selectedItems.clear();
            if (!hideDetails && DisplayUtils.hasCentralPane(getActivity()))
            {
                selectedItems.add(selectedFile);
            }
        }

        if (hideDetails)
        {
            selectedItems.clear();
        }
        else if (nActions == null)
        {
            // Show properties
            ActionManager.actionView(this, selectedFile, new ActionManagerListener()
            {
                @Override
                public void onActivityNotFoundException(ActivityNotFoundException e)
                {
                    Bundle b = new Bundle();
                    b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_unable_open_file_title);
                    b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, R.string.error_unable_open_file);
                    b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                    ActionManager.actionDisplayDialog(getActivity(), b);
                }
            });
            selectedItems.clear();
        }
        adapter.notifyDataSetChanged();

    }

    // //////////////////////////////////////////////////////////////////////
    // ACTION MODE
    // //////////////////////////////////////////////////////////////////////
    private FileActions nActions;

    public boolean onItemLongClick(ListView l, View v, int position, long id)
    {
        if (nActions != null) { return false; }

        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        File selectedFile = new File(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));

        selectedItems.clear();
        selectedItems.add(selectedFile);

        // Start the CAB using the ActionMode.Callback defined above
        nActions = new FileActions(LibraryFragment.this, selectedItems);
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
    };

    // ///////////////////////////////////////////////////////////////////////////
    // LISTING MODE
    // ///////////////////////////////////////////////////////////////////////////
    public int getMode()
    {
        Bundle b = getArguments();
        return b.getInt(PARAM_MODE);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CURSOR ADAPTER
    // ///////////////////////////////////////////////////////////////////////////
    private static final List<String> OFFICE_EXTENSION = new ArrayList<String>()
    {
        private static final long serialVersionUID = 1L;

        {
            add("pdf");
            add("doc");
            add("docx");
            add("xls");
            add("xlsx");
            add("ppt");
            add("pptx");
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        mediaTypeId = (Integer) getArguments().get(PARAM_MEDIATYPE_ID);

        //String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?";
        StringBuilder selection = new StringBuilder(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?");
        String selectionFinal = selection.toString();
        List<String> argumentsList = new ArrayList<String>();
        argumentsList.add(Integer.toString(mediaTypeId));
        if (mediaTypeId == 0)
        {
            String mimeType = null;
            selection.append(" AND " + MediaStore.Files.FileColumns.MIME_TYPE + " IN (");
            for (String extension : OFFICE_EXTENSION)
            {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                argumentsList.add(mimeType);
                selection.append( " ? ,");
            }
            selectionFinal = selection.toString().substring(0, selection.lastIndexOf(",")) + ")";
        }
        String[] arguments = new String[argumentsList.size()];
        arguments = argumentsList.toArray(arguments);

        setListShown(false);
        String[] projection = new String[] { MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA };
        Uri baseUri = MediaStore.Files.getContentUri("external");
        return new CursorLoader(getActivity(), baseUri, projection, selectionFinal, arguments,
                MediaStore.Files.FileColumns.DATA + " ASC");
    }

}

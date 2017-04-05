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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileActions.onFinishModeListerner;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.BaseCursorGridFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class LibraryFragment extends BaseCursorGridFragment
{
    public static final String TAG = LibraryFragment.class.getName();

    protected List<File> selectedItems = new ArrayList<File>();

    private int mediaTypeId;

    private int menuId;

    private boolean isShortCut = false;

    private static final String ARGUMENT_MEDIATYPE_ID = "mediatypeid";

    private static final String ARGUMENT_MENU_ID = "menuId";

    private static final String ARGUMENT_SHORTCUT = "shortcut";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public LibraryFragment()
    {
        emptyListMessageId = R.string.empty_download;
        mode = ListingModeFragment.MODE_LISTING;
        setHasOptionsMenu(true);
    }

    public static LibraryFragment newInstanceByTemplate(Bundle b)
    {
        LibraryFragment cbf = new LibraryFragment();
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
            getActionBar().show();
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

    @Override
    public String onPrepareTitle()
    {
        return getString(R.string.menu_local_files);
    }

    @Override
    public void onStart()
    {
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
    // Override
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRetrieveParameters(Bundle bundle)
    {
        mediaTypeId = (Integer) bundle.get(ARGUMENT_MEDIATYPE_ID);
        menuId = bundle.getInt(ARGUMENT_MENU_ID);
        isShortCut = bundle.getBoolean(ARGUMENT_SHORTCUT);
    }

    @Override
    protected BaseAdapter onAdapterCreation()
    {
        return new LibraryCursorAdapter(this, null, R.layout.row_two_lines_caption_divider, selectedItems, mediaTypeId,
                getMode());
    }

    @Override
    protected void performRequest(ListingContext lcorigin)
    {
        int hasWriteExternalStoragePermission = PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= 23)
        {
            hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED)
        {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTION
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(GridView l, View v, int position, long id)
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
                getActivity().setResult(FragmentActivity.RESULT_OK, pickResult);
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
            new MaterialDialog.Builder(getActivity()).iconRes(R.drawable.ic_application_logo)
                    .title(R.string.error_unable_open_file_title)
                    .content(Html.fromHtml(getString(R.string.error_unable_open_file)))
                    .positiveText(android.R.string.ok).show();
            selectedItems.clear();
        }
        adapter.notifyDataSetChanged();

    }

    // //////////////////////////////////////////////////////////////////////
    // ACTION MODE
    // //////////////////////////////////////////////////////////////////////
    private FileActions nActions;

    @Override
    public boolean onListItemLongClick(GridView l, View v, int position, long id)
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
        mediaTypeId = (Integer) getArguments().get(ARGUMENT_MEDIATYPE_ID);

        // String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?";
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
                selection.append(" ? ,");
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
        menu.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
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
            menuIconId = R.drawable.ic_download_dark;
            menuTitleId = R.string.menu_local_files;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder mediaType(int mediaType)
        {
            extraConfiguration.putInt(ARGUMENT_MEDIATYPE_ID, mediaType);
            return this;
        }

        public Builder mode(int displayMode)
        {
            extraConfiguration.putInt(ARGUMENT_MODE, displayMode);
            return this;
        }

        public Builder menuId(int menuId)
        {
            extraConfiguration.putInt(ARGUMENT_MENU_ID, menuId);
            return this;
        }

        public Builder isShortCut(boolean isShortCut)
        {
            extraConfiguration.putBoolean(ARGUMENT_SHORTCUT, isShortCut);
            return this;
        }

        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }

    }
}

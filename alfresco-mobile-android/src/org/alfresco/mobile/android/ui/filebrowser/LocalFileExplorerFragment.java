/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.ui.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public abstract class LocalFileExplorerFragment extends BaseListFragment implements LoaderCallbacks<List<File>>
{

    public static final String TAG = "LocalFileExplorerFragment";

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_FOLDERPATH = "folderPath";

    public static final String MODE = "mode";
    
    public static final int MODE_LISTING = 1;

    public static final int MODE_PICK_FILE = 2;
    
    public static final int MODE_IMPORT = 3;


    protected List<File> selectedItems = new ArrayList<File>(1);

    private int titleId;

    public LocalFileExplorerFragment()
    {
        loaderId = LocalFileExplorerLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_child;
    }

    public static Bundle createBundleArgs(File folder)
    {
        return createBundleArgs(folder, null);
    }

    public static Bundle createBundleArgs(String folderPath)
    {
        return createBundleArgs(null, folderPath);
    }

    public static Bundle createBundleArgs(File folder, String path)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        args.putSerializable(ARGUMENT_FOLDERPATH, path);
        return args;
    }
    
    @Override
    public void onStart()
    {
        retrieveTitle();
        if (getDialog() != null)
        {
            getDialog().setTitle(titleId);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle(titleId);
        }
        super.onStart();
    }
    
    private void retrieveTitle(){
        Bundle b = getArguments();
        if (b.getInt(MODE) == MODE_LISTING)
        {
            titleId = R.string.menu_downloads;
        }
        else if (b.getInt(MODE) == MODE_PICK_FILE)
        {
            titleId = R.string.upload_pick_document;
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.sdk_list, null);

        init(v, emptyListMessageId);
        
        setRetainInstance(true);
        if (initLoader)
        {
            continueLoading(loaderId, callback);
        }
        
        retrieveTitle();
        return new AlertDialog.Builder(getActivity()).setTitle(titleId).setView(v).create();
    }

    protected int getMode()
    {
        Bundle b = getArguments();
        return b.getInt(MODE);
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle ba)
    {
        if (!hasmore && lv != null)
        {
            setListShown(false);
        }

        // Case Init & case Reload
        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;
        File f = null;
        String path = null;

        if (bundle != null)
        {
            f = (File) bundle.getSerializable(ARGUMENT_FOLDER);
            path = bundle.getString(ARGUMENT_FOLDERPATH);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }
        calculateSkipCount(lc);

        Loader<List<File>> loader = null;
        if (path != null)
        {
            title = path.substring(path.lastIndexOf("/") + 1, path.length());
            loader = new LocalFileExplorerLoader(getActivity(), new File(path));
        }
        else if (f != null)
        {
            title = f.getName();
            loader = new LocalFileExplorerLoader(getActivity(), f);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> results)
    {

        if (adapter == null)
        {
            adapter = new LocalFileExplorerAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<File>(0),
                    selectedItems);
        }

        PagingResult<File> pagingResultFiles = new PagingResultImpl<File>(results, false, results.size());
        displayPagingData(pagingResultFiles, loaderId, callback);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> arg0)
    {
        // TODO Auto-generated method stub
    }

    public void refresh()
    {
        refresh(loaderId, callback);
    }
}

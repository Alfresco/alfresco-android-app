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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public abstract class AbstractFileExplorerFragment extends BaseListFragment implements LoaderCallbacks<List<File>>,
        ListingModeFragment
{

    public static final String TAG = AbstractFileExplorerFragment.class.getName();

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_FOLDERPATH = "folderPath";

    protected List<File> selectedItems = new ArrayList<File>(1);

    private int titleId;
    
    public AbstractFileExplorerFragment()
    {
        loaderId = FileExplorerLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_child;
    }

    public static Bundle createBundleArgs(File folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        return args;
    }

    private void retrieveTitle()
    {
        switch (getMode())
        {
            case MODE_LISTING:
                titleId = R.string.menu_local_files;
                break;
            case MODE_PICK_FILE:
                titleId = R.string.upload_pick_document;
                break;
            default:
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
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

    public int getMode()
    {
        Bundle b = getArguments();
        return b.getInt(PARAM_MODE);
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
            loader = new FileExplorerLoader(getActivity(), new File(path));
        }
        else if (f != null)
        {
            title = f.getName();
            loader = new FileExplorerLoader(getActivity(), f);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> results)
    {

        if (adapter == null)
        {
            adapter = new FileExplorerAdapter(this, R.layout.app_list_progress_row, getMode(), new ArrayList<File>(0),
                    selectedItems);
        }

        if (results != null){
            PagingResult<File> pagingResultFiles = new PagingResultImpl<File>(results, false, results.size());
            displayPagingData(pagingResultFiles, loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<File>> arg0)
    {
        // DO Nothing
    }

    public void refresh()
    {
        refresh(loaderId, callback);
    }
    
    protected Fragment getFragment(String tag)
    {
        return getActivity().getFragmentManager().findFragmentByTag(tag);
    }
}

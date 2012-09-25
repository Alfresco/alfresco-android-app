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
package org.alfresco.mobile.android.ui.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;

public abstract class LocalFileExplorerFragment extends BaseListFragment implements LoaderCallbacks<List<File>>
{

    public static final String TAG = "LocalFileExplorerFragment";

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_FOLDERPATH = "folderPath";

    protected List<File> selectedItems = new ArrayList<File>(1);

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
    public Loader<List<File>> onCreateLoader(int id, Bundle ba)
    {

        if (!hasmore)
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
            loader = new LocalFileExplorerLoader(getActivity(), new File(path), lc);
        }
        else if (f != null)
        {
            title = f.getName();
            loader = new LocalFileExplorerLoader(getActivity(), f, lc);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> results)
    {

        if (adapter == null)
        {
            adapter = new LocalFileExplorerAdapter(getActivity(), R.layout.sdk_list_item, new ArrayList<File>(0),
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

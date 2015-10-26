/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.file.browse.FilesEvent;
import org.alfresco.mobile.android.async.file.browse.FilesRequest;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.ui.fragments.BaseGridFragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.squareup.otto.Subscribe;

public abstract class FileExplorerFoundationFragment extends BaseGridFragment implements FileExplorerFragmentTemplate
{
    public static final String TAG = FileExplorerFoundationFragment.class.getName();

    protected List<File> selectedItems = new ArrayList<File>(1);

    protected File parent;

    protected String path;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public FileExplorerFoundationFragment()
    {
        emptyListMessageId = R.string.empty_child;
        requiredSession = false;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    protected void onRetrieveParameters(Bundle bundle)
    {
        path = bundle.getString(FileExplorerFragmentTemplate.ARGUMENT_PATH);
        File tmpParent = (File) bundle.getSerializable(ARGUMENT_FILE);

        // By default if nothing provided we open the app download folder.
        if (path == null && tmpParent == null)
        {
            parent = AlfrescoStorageManager.getInstance(getActivity()).getDownloadFolder(getAccount());
        }

        if (path != null && tmpParent == null)
        {
            parent = new File(path);
        }

        if (tmpParent != null && parent == null)
        {
            parent = tmpParent;
        }
    }

    @Override
    public String onPrepareTitle()
    {
        switch (getMode())
        {
            case MODE_LISTING:
                title = getString(R.string.menu_local_files);
                break;
            case MODE_PICK:
                title = getString(R.string.upload_pick_document);
                break;
            default:
                break;
        }

        if (path != null)
        {
            title = path.substring(path.lastIndexOf("/") + 1, path.length());
        }
        else if (parent != null)
        {
            title = parent.getName();
        }
        return title;
    }

    @Override
    protected ArrayAdapter<?> onAdapterCreation()
    {
        return new FileExplorerAdapter(this, R.layout.row_two_lines_progress, getMode(), new ArrayList<File>(0),
                selectedItems);
    }

    @Override
    protected OperationBuilder onCreateOperationRequest(ListingContext listingContext)
    {
        return new FilesRequest.Builder(parent).setListingContext(listingContext);
    }

    @Subscribe
    public void onResult(FilesEvent event)
    {
        displayData(event);
    }

    // //////////////////////////////////////////////////////////////////////
    // INTERNALS
    // //////////////////////////////////////////////////////////////////////

    public int getMode()
    {
        return mode;
    }
}

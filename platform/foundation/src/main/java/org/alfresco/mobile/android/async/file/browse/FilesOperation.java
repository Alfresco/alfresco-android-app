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
package org.alfresco.mobile.android.async.file.browse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.FileComparator;

import android.util.Log;

public class FilesOperation extends ListingOperation<PagingResult<File>>
{
    private static final String TAG = FilesOperation.class.getName();

    private File directoryFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public FilesOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof FilesRequest)
        {
            this.directoryFile = ((FilesRequest) request).file;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<File>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<File>> result = new LoaderResult<PagingResult<File>>();
            PagingResult<File> pagingResult = null;
            ArrayList<File> fileList = new ArrayList<File>();

            try
            {
                if (directoryFile.isDirectory())
                {
                    File[] childs = directoryFile.listFiles();
                    if (childs != null)
                    {
                        for (File child : childs)
                        {
                            if (!child.isHidden() && !child.getName().startsWith("."))
                            {
                                fileList.add(child);
                            }
                        }
                    }
                }
                Collections.sort(fileList, new FileComparator(true));
                pagingResult = new PagingResultImpl<File>(fileList, false, fileList.size());
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(pagingResult);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<PagingResult<File>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<File>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new FilesEvent(getRequestId(), result, directoryFile));
    }
}

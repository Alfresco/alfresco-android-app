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
package org.alfresco.mobile.android.async.file.create;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.FileOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

public class CreateDirectoryOperation extends FileOperation<File>
{
    protected String folderName;

    protected Map<String, Serializable> properties;

    protected File newFolderFile = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateDirectoryOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof CreateDirectoryRequest)
        {
            this.folderName = ((CreateDirectoryRequest) request).folderName;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<File> doInBackground()
    {
        LoaderResult<File> result = new LoaderResult<File>();

        try
        {
            super.doInBackground();

            if (file != null)
            {
                newFolderFile = new File(file, folderName);
                newFolderFile.mkdirs();
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(newFolderFile);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<File> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CreateDirectoryEvent(getRequestId(), result, file));
    }
}

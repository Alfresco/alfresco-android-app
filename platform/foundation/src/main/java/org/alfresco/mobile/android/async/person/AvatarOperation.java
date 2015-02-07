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
package org.alfresco.mobile.android.async.person;

import java.io.File;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.io.IOUtils;

import android.util.Log;

public class AvatarOperation extends BaseOperation<String>
{
    private static final String TAG = AvatarOperation.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AvatarOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<String> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<String> result = new LoaderResult<String>();
            File accountFile = null;

            try
            {
                Person p = session.getServiceRegistry().getPersonService().getPerson(((AvatarRequest) request).personIdentifier);
                ContentFile contentFile = session.getServiceRegistry().getPersonService().getAvatar(p);
                File accountFolder = AlfrescoStorageManager.getInstance(context).getPrivateFolder(getAccount());
                accountFile = new File(accountFolder, p.getIdentifier().concat(".jpg"));
                accountFile.createNewFile();
                IOUtils.copyFile(contentFile.getFile().getPath(), accountFile.getPath());
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(accountFile != null ? accountFile.getPath() : null);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<String>();
    }

    @Override
    protected void onPostExecute(LoaderResult<String> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new AvatarEvent(getRequestId(), result));
    }
}

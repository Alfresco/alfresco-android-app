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
package org.alfresco.mobile.android.application.config.async;

import org.alfresco.mobile.android.api.services.ConfigService;
import org.alfresco.mobile.android.api.services.ConfigServiceFactory;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class ConfigurationOperation extends BaseOperation<ConfigService>
{
    private static final String TAG = ConfigurationOperation.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ConfigurationOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<ConfigService> doInBackground()
    {
        LoaderResult<ConfigService> result = new LoaderResult<ConfigService>();
        ConfigService config = null;

        try
        {
            super.doInBackground();

            config = ConfigServiceFactory.buildConfigService(context.getPackageName(), ((ConfigurationRequest) request).parameters);

            result.setData(config);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<ConfigService> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new ConfigurationEvent(getRequestId(), result, accountId));
    }
}

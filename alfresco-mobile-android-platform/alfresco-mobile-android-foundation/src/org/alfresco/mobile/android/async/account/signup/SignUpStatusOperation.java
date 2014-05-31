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
package org.alfresco.mobile.android.async.account.signup;

import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.data.CloudSignupRequest;

import android.util.Log;

public class SignUpStatusOperation extends ListingOperation<Boolean>
{
    private static final String TAG = SignUpStatusOperation.class.getName();

    private String apiKey;

    private CloudSignupRequest signUpRequest;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SignUpStatusOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SignUpStatusRequest)
        {
            this.signUpRequest = ((SignUpStatusRequest) request).signUpRequest;
            this.apiKey = ((SignUpStatusRequest) request).apiKey;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<Boolean> doInBackground()
    {
        try
        {
            LoaderResult<Boolean> result = new LoaderResult<Boolean>();
            Boolean activated = false;

            try
            {
                activated = CloudSignupRequest.checkAccount(context, signUpRequest, apiKey);
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(activated);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<Boolean>();
    }

    @Override
    protected void onPostExecute(LoaderResult<Boolean> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new SignUpStatusEvent(getRequestId(), result, signUpRequest));
    }
}

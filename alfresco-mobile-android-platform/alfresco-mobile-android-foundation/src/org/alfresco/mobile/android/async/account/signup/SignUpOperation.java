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

public class SignUpOperation extends ListingOperation<CloudSignupRequest>
{
    private static final String TAG = SignUpOperation.class.getName();

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private String apiKey;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SignUpOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SignUpRequest)
        {
            this.firstName = ((SignUpRequest) request).firstName;
            this.lastName = ((SignUpRequest) request).lastName;
            this.emailAddress = ((SignUpRequest) request).emailAddress;
            this.password = ((SignUpRequest) request).password;
            this.apiKey = ((SignUpRequest) request).apiKey;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<CloudSignupRequest> doInBackground()
    {
        try
        {
            LoaderResult<CloudSignupRequest> result = new LoaderResult<CloudSignupRequest>();
            CloudSignupRequest request = null;

            try
            {
                request = CloudSignupRequest.signup(context, firstName, lastName, emailAddress, password, apiKey);
            }
            catch (Exception e)
            {
                result.setException(e);
            }

            result.setData(request);

            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<CloudSignupRequest>();
    }

    @Override
    protected void onPostExecute(LoaderResult<CloudSignupRequest> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new SignUpEvent(getRequestId(), result));
    }
}

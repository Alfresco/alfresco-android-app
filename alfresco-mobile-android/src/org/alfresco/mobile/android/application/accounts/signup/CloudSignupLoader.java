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
package org.alfresco.mobile.android.application.accounts.signup;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;

import android.content.Context;

/**
 * Provides an asynchronous loader to create a CloudSignupRequest object.
 * 
 * @author Jean Marie Pascal
 */
public class CloudSignupLoader extends AbstractBaseLoader<LoaderResult<CloudSignupRequest>>
{
    /** Unique SessionLoader identifier. */
    public static final int ID = CloudSignupLoader.class.hashCode();

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password;

    private String apiKey;

    /**
     * @param context : Android Context
     * @param url : Base url to the repository
     * @param username : username with which we want to create a session
     * @param password : password with which we want to create a session
     */
    public CloudSignupLoader(Context context, String firstName, String lastName, String emailAddress, String password,
            String apiKey)
    {
        super(context);
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.apiKey = apiKey;
    }

    @Override
    public LoaderResult<CloudSignupRequest> loadInBackground()
    {
        LoaderResult<CloudSignupRequest> result = new LoaderResult<CloudSignupRequest>();
        CloudSignupRequest request = null;
        try
        {
            request = CloudSignupRequest.signup(firstName, lastName, emailAddress, password, apiKey);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(request);

        return result;
    }

}

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
 * Provides an asynchronous loader to check if a CloudSignupRequest has been
 * validated.
 * 
 * @author Jean Marie Pascal
 */
public class CloudSignupStatusLoader extends AbstractBaseLoader<LoaderResult<Boolean>>
{
    /** Unique SessionLoader identifier. */
    public static final int ID = CloudSignupStatusLoader.class.hashCode();

    private CloudSignupRequest request;

    private String apikey;

    public CloudSignupStatusLoader(Context context, CloudSignupRequest request, String apikey)
    {
        super(context);
        this.request = request;
        this.apikey = apikey;
    }

    @Override
    public LoaderResult<Boolean> loadInBackground()
    {
        LoaderResult<Boolean> result = new LoaderResult<Boolean>();
        Boolean activated = false;
        try
        {
            activated = CloudSignupRequest.checkAccount(request, apikey);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(activated);

        return result;
    }
}

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
package org.alfresco.mobile.android.async.activitystream;

import org.alfresco.mobile.android.api.model.ActivityEntry;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class ActivityStreamOperation extends ListingOperation<PagingResult<ActivityEntry>>
{
    private static final String TAG = ActivityStreamOperation.class.getName();

    protected String username;

    protected String siteName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public ActivityStreamOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof ActivityStreamRequest)
        {
            this.username = ((ActivityStreamRequest) request).userName;
            this.siteName = ((ActivityStreamRequest) request).siteIdentifier;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<ActivityEntry>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<ActivityEntry>> result = new LoaderResult<>();
            PagingResult<ActivityEntry> pagingResult = null;

            try
            {
                if (username != null && siteName == null)
                {
                    pagingResult = session.getServiceRegistry().getActivityStreamService()
                            .getActivityStream(username, listingContext);
                }
                else if (siteName != null)
                {
                    pagingResult = session.getServiceRegistry().getActivityStreamService()
                            .getSiteActivityStream(siteName, listingContext);
                }
                else
                {
                    pagingResult = session.getServiceRegistry().getActivityStreamService()
                            .getActivityStream(listingContext);
                }
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
        return new LoaderResult<>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<ActivityEntry>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new ActivityStreamEvent(getRequestId(), siteName, result));
    }
}

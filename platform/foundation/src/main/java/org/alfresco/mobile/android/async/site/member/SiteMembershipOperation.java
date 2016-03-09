/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.async.site.member;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;

import android.util.Log;

public class SiteMembershipOperation extends BaseOperation<Site>
{
    private static final String TAG = SiteMembershipOperation.class.getName();

    protected Boolean isJoining;

    private Site site;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SiteMembershipOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SiteMembershipRequest)
        {
            this.isJoining = ((SiteMembershipRequest) request).isJoining();
            this.site = ((SiteMembershipRequest) request).getSite();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<Site> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<Site> result = new LoaderResult<Site>();
            try
            {
                result.setData(null);

                if (site != null && isJoining)
                {
                    result.setData(session.getServiceRegistry().getSiteService().joinSite(site));
                }
                else if (site != null && !isJoining)
                {
                    result.setData(session.getServiceRegistry().getSiteService().leaveSite(site));
                }
            }
            catch (Exception e)
            {
                result.setException(e);
            }
            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<Site>();
    }

    @Override
    protected void onPostExecute(LoaderResult<Site> result)
    {
        super.onPostExecute(result);

        // Analytics
        AnalyticsHelper.reportOperationEvent(context, AnalyticsManager.CATEGORY_SITE_MANAGEMENT,
                AnalyticsManager.ACTION_MEMBERSHIP,
                isJoining ? AnalyticsManager.LABEL_JOIN : AnalyticsManager.LABEL_LEAVE, 1, result.hasException());

        EventBusManager.getInstance().post(new SiteMembershipEvent(getRequestId(), result, site, isJoining));
    }
}

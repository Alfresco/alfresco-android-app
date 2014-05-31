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
package org.alfresco.mobile.android.async.site;

import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.BaseOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class SiteFavoriteOperation extends BaseOperation<Site>
{
    private static final String TAG = SiteFavoriteOperation.class.getName();

    private Site site;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public SiteFavoriteOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof SiteFavoriteRequest)
        {
            this.site = ((SiteFavoriteRequest) request).getSite();
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
                if (site.isFavorite())
                {
                    result.setData(session.getServiceRegistry().getSiteService().removeFavoriteSite(site));
                }
                else
                {
                    result.setData(session.getServiceRegistry().getSiteService().addFavoriteSite(site));
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
        EventBusManager.getInstance().post(new SiteFavoriteEvent(getRequestId(), result, site));
    }
}

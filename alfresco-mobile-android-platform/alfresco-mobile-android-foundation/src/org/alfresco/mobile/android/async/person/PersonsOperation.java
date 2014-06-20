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

import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class PersonsOperation extends ListingOperation<PagingResult<Person>>
{
    private static final String TAG = PersonsOperation.class.getName();

    protected Site site;

    protected String keywords;

    protected String siteShortName;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public PersonsOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof PersonsRequest)
        {
            this.site = ((PersonsRequest) request).getSite();
            this.keywords = ((PersonsRequest) request).getKeywords();
            this.siteShortName = ((PersonsRequest) request).getSiteShortName();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Person>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Person>> result = new LoaderResult<PagingResult<Person>>();
            PagingResult<Person> pagingResult = null;

            try
            {
                if (siteShortName != null && site == null)
                {
                    site = session.getServiceRegistry().getSiteService().getSite(siteShortName);
                }

                if (site != null)
                {
                    pagingResult = session.getServiceRegistry().getSiteService().getAllMembers(site, listingContext);
                }
                else if (keywords != null)
                {
                    pagingResult = session.getServiceRegistry().getPersonService().search(keywords, listingContext);
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
        return new LoaderResult<PagingResult<Person>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Person>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new PersonsEvent(getRequestId(), site, keywords, result));
    }
}

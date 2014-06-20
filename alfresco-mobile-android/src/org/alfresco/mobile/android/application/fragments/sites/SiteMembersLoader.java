/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.sites;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.content.Context;

/**
 * @since 1.3.0
 * @author jpascal
 *
 */
public class SiteMembersLoader extends AbstractPagingLoader<LoaderResult<PagingResult<Person>>>
{

    public static final int ID = SiteMembersLoader.class.hashCode();

    private Site site;

    public SiteMembersLoader(Context context, AlfrescoSession session, Site site)
    {
        super(context);
        this.session = session;
        this.site = site;
    }

    @Override
    public LoaderResult<PagingResult<Person>> loadInBackground()
    {
        LoaderResult<PagingResult<Person>> result = new LoaderResult<PagingResult<Person>>();
        PagingResult<Person> p = null;

        try
        {
            p = session.getServiceRegistry().getSiteService().getAllMembers(site, listingContext);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(p);

        return result;
    }

}

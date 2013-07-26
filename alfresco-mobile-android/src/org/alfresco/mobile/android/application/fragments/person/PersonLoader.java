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
package org.alfresco.mobile.android.application.fragments.person;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Person;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.content.Context;

/**
 * @since 1.3
 * @author jpascal
 *
 */
public class PersonLoader  extends AbstractPagingLoader<LoaderResult<Person>>
{

    public static final int ID = PersonLoader.class.hashCode();

    private String personIdentifier;

    public PersonLoader(Context context, AlfrescoSession session, String personIdentifier)
    {
        super(context);
        this.session = session;
        this.personIdentifier = personIdentifier;
    }

    @Override
    public LoaderResult<Person> loadInBackground()
    {
        LoaderResult<Person> result = new LoaderResult<Person>();
        Person p = null;
        
        try
        {
            p = session.getServiceRegistry().getPersonService().getPerson(personIdentifier);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(p);

        return result;
    }
}

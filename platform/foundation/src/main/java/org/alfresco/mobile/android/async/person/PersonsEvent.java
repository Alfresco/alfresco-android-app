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
import org.alfresco.mobile.android.async.OperationEvent;

public class PersonsEvent extends OperationEvent<PagingResult<Person>>
{
    public final Site site;

    public final String keywords;

    public PersonsEvent(String requestId, Site site, String keywords, LoaderResult<PagingResult<Person>> results)
    {
        super(requestId, results);
        this.site = site;
        this.keywords = keywords;
    }
}

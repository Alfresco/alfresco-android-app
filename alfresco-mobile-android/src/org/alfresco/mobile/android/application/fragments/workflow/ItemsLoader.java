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
package org.alfresco.mobile.android.application.fragments.workflow;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.content.Context;

/**
 * @author jpascal
 */
public class ItemsLoader extends AbstractPagingLoader<LoaderResult<PagingResult<Node>>>
{
    /** Unique SitesLoader identifier. */
    public static final int ID = ItemsLoader.class.hashCode();
    
    private Task task;

    public ItemsLoader(Context context, AlfrescoSession session, Task task)
    {
        super(context);
        this.session = session;
        this.task = task;
    }

    @Override
    public LoaderResult<PagingResult<Node>> loadInBackground()
    {
        LoaderResult<PagingResult<Node>> result = new LoaderResult<PagingResult<Node>>();
        PagingResult<Node> pagingResult = null;

        try
        {
            pagingResult = session.getServiceRegistry().getWorkflowService().getDocuments(task, listingContext);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(pagingResult);

        return result;
    }
}

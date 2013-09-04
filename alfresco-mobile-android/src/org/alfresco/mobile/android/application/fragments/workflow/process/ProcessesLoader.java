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
package org.alfresco.mobile.android.application.fragments.workflow.process;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.content.Context;
import android.util.Log;

/**
 * @author jpascal
 */
public class ProcessesLoader extends AbstractPagingLoader<LoaderResult<PagingResult<Process>>>
{
    /** Unique SitesLoader identifier. */
    public static final int ID = ProcessesLoader.class.hashCode();

    public ProcessesLoader(Context context, AlfrescoSession session)
    {
        super(context);
        this.session = session;
    }

    @Override
    public LoaderResult<PagingResult<Process>> loadInBackground()
    {
        LoaderResult<PagingResult<Process>> result = new LoaderResult<PagingResult<Process>>();
        PagingResult<Process> pagingResult = null;

        try
        {
            pagingResult = session.getServiceRegistry().getWorkflowService().getProcesses(listingContext);
        }
        catch (Exception e)
        {
            Log.d("TasksLoader", Log.getStackTraceString(e));
            result.setException(e);
        }

        result.setData(pagingResult);

        return result;
    }
}

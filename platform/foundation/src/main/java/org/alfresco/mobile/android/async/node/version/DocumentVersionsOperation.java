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
package org.alfresco.mobile.android.async.node.version;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class DocumentVersionsOperation extends ListingOperation<PagingResult<Document>>
{
    private static final String TAG = DocumentVersionsOperation.class.getName();

    private Document document;

    private String nodeIdentifier;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DocumentVersionsOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof DocumentVersionsRequest)
        {
            this.document = ((DocumentVersionsRequest) request).getDocument();
            this.nodeIdentifier = ((DocumentVersionsRequest) request).getNodeIdentifier();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<PagingResult<Document>> doInBackground()
    {
        try
        {
            super.doInBackground();

            LoaderResult<PagingResult<Document>> result = new LoaderResult<PagingResult<Document>>();
            PagingResult<Document> pagingResult = null;

            try
            {
                if (nodeIdentifier != null && document == null)
                {
                    document = (Document) session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(nodeIdentifier);
                }

                pagingResult = session.getServiceRegistry().getVersionService().getVersions(document, listingContext);
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
        return new LoaderResult<PagingResult<Document>>();
    }

    @Override
    protected void onPostExecute(LoaderResult<PagingResult<Document>> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new DocumentVersionsEvent(getRequestId(), document, result));
    }
}

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
package org.alfresco.mobile.android.async.node.update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

public class UpdateNodeOperation extends NodeOperation<Node>
{
    private static final String TAG = UpdateNodeOperation.class.getName();

    /** list of property values that must be applied. */
    private Map<String, Serializable> properties;

    /** Binary Content of the future document. */
    private ContentFile contentFile;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpdateNodeOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof UpdateNodeRequest)
        {
            this.contentFile = ((UpdateNodeRequest) request).getContentFile();
            this.properties = ((UpdateNodeRequest) request).getProperties();
        }
        // ignoreParentFolder = true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Node> doInBackground()
    {
        LoaderResult<Node> result = new LoaderResult<Node>();
        Node resultNode = null;

        try
        {
            result = super.doInBackground();

            if (properties != null)
            {
                ArrayList<String> tags = null;
                if (properties.containsKey(ContentModel.PROP_TAGS) && properties.get(ContentModel.PROP_TAGS) != null)
                {
                    tags = (ArrayList<String>) properties.get(ContentModel.PROP_TAGS);
                    properties.remove(ContentModel.PROP_TAGS);
                }
                resultNode = session.getServiceRegistry().getDocumentFolderService().updateProperties(node, properties);

                if (tags != null && !tags.isEmpty())
                {
                    session.getServiceRegistry().getTaggingService().addTags(resultNode, tags);
                }
            }

            if (contentFile != null)
            {
                resultNode = session.getServiceRegistry().getDocumentFolderService()
                        .updateContent((Document) node, contentFile);
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(resultNode);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Node> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new UpdateNodeEvent(getRequestId(), result, node, parentFolder));
    }
}

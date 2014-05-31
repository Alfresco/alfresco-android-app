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
package org.alfresco.mobile.android.async.node.comment;

import org.alfresco.mobile.android.api.model.Comment;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.NodeOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.util.Log;

public class CreateCommentOperation extends NodeOperation<Comment>
{
    private static final String TAG = CreateCommentOperation.class.getName();

    private Comment updatedComment = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateCommentOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Comment> doInBackground()
    {
        LoaderResult<Comment> result = new LoaderResult<Comment>();
        try
        {
            result = super.doInBackground();
            updatedComment = session.getServiceRegistry().getCommentService()
                    .addComment(node, ((CreateCommentRequest) request).comment);
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }

        result.setData(updatedComment);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostExecute(LoaderResult<Comment> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new CreateCommentEvent(getRequestId(), result));
    }
}

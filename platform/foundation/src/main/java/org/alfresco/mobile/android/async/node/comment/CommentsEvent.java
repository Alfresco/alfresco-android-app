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
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationEvent;

public class CommentsEvent extends OperationEvent<PagingResult<Comment>>
{
    public final Node node;

    public CommentsEvent(String requestId, Node node, LoaderResult<PagingResult<Comment>> result)
    {
        super(requestId, result);
        this.node = node;
    }
}

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
package org.alfresco.mobile.android.async.node.create;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationEvent;

public class RetrieveDocumentNameEvent extends OperationEvent<String>
{
    public final Folder parentFolder;

    public final String originalName;

    public RetrieveDocumentNameEvent(String requestId, LoaderResult<String> results, String originalName,
            Folder parentFolder)
    {
        super(requestId, results);
        this.originalName = originalName;
        this.parentFolder = parentFolder;
    }

}

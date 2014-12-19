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
package org.alfresco.mobile.android.sync.operations;

import org.alfresco.mobile.android.async.OperationStatus;

public interface FavoriteSyncStatus extends OperationStatus
{
    int STATUS_HIDDEN = 64;

    int STATUS_REQUEST_USER = 128;

    int STATUS_MODIFIED = 256;

    /**
     * Local Document has modification but remote document has been unfavorited.
     */
    int REASON_NODE_UNFAVORITED = 100;

    /** Local Document has modification but remote document has been deleted. */
    int REASON_NODE_DELETED = 101;

    /**
     * Local Document has modification and remote document has modification too.
     */
    int REASON_LOCAL_MODIFICATION = 102;

    /**
     * Local Document has modification and remote document has modification too
     * but the user doesn't have the right to upload it.
     */
    int REASON_NO_PERMISSION = 103;

}

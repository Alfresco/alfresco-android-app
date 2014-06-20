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
package org.alfresco.mobile.android.application.operations.batch.sync;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

public class CleanSyncFavoriteRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 1010;

    public static final String MIME_SYNC = "UnSync";
    
    private String accountUsername;

    private String accountUrl;

    private boolean isDeletion;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CleanSyncFavoriteRequest(Account acc, boolean isDeletion)
    {
        super();
        requestTypeId = TYPE_ID;

        this.accountUsername = acc.getUsername();
        this.accountUrl = acc.getUrl();
        this.isDeletion = isDeletion;

        setMimeType(MIME_SYNC);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getRequestIdentifier()
    {
        return MIME_SYNC + "_" + getAccountId();
    }

    public String getAccountUsername()
    {
        return accountUsername;
    }

    public String getAccountUrl()
    {
        return accountUrl;
    }

    public Boolean isDeletion()
    {
        return isDeletion;
    }
}


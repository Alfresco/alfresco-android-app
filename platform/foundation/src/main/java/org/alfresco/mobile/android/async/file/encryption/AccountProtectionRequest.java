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
package org.alfresco.mobile.android.async.file.encryption;

import java.io.File;

import org.alfresco.mobile.android.async.file.FileOperationRequest;

import android.content.Context;

public class AccountProtectionRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 453;

    final boolean doEncrypt;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected AccountProtectionRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, File file, boolean doEncrypt)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, file);
        this.doEncrypt = doEncrypt;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends FileOperationRequest.Builder
    {
        private boolean doEncrypt;

        protected Builder()
        {
        }

        public Builder(File file, boolean doEncrypt)
        {
            super(file);
            this.doEncrypt = doEncrypt;
        }

        public AccountProtectionRequest build(Context context)
        {
            return new AccountProtectionRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, file, doEncrypt);
        }
    }
}

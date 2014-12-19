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
package org.alfresco.mobile.android.application.config.async;

import java.util.Map;

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class ConfigurationRequest extends BaseOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_CONFIGURATION_READ;

    public static final String MIMETYPE = "Configuration";

    final Map<String, Object> parameters;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected ConfigurationRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, Map<String, Object> parameters)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId);
        this.parameters = parameters;
    }

    public ConfigurationRequest(Cursor cursor)
    {
        super(cursor);
        this.parameters = null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUEST BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        private Map<String, Object> parameters;

        public Builder()
        {
            super();
        }

        public Builder( Map<String, Object> parameters)
        {
            super();
            this.parameters = parameters;
            requestTypeId = TYPE_ID;
            mimeType = MIMETYPE;
        }

        public ConfigurationRequest build(Context context)
        {
            return new ConfigurationRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, parameters);
        }
    }
}

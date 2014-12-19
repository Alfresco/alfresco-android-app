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
package org.alfresco.mobile.android.async.file.open;

import java.io.File;
import java.nio.charset.Charset;

import org.alfresco.mobile.android.async.OperationRequestIds;
import org.alfresco.mobile.android.async.file.FileOperationRequest;
import org.alfresco.mobile.android.async.impl.BaseOperationRequest;

import android.content.Context;
import android.database.Cursor;

public class OpenFileRequest extends FileOperationRequest
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = OperationRequestIds.ID_FILE_READ;

    private static final String PROP_CHARSET = "charset";

    final Charset charset;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected OpenFileRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId, File file, Charset charset)
    {
        super(context, accountId, networkId, notificationVisibility, title, mimeType, requestTypeId, file);
        this.charset = charset;

        // Save extra info
        persistentProperties.put(PROP_CHARSET, charset.toString());
    }

    public OpenFileRequest(Cursor cursor)
    {
        super(cursor);
        this.charset = Charset.forName((String) persistentProperties.remove(PROP_CHARSET));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT VALUES / PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Builder
    // ///////////////////////////////////////////////////////////////////////////
    public static class Builder extends BaseOperationRequest.Builder
    {
        private File file;

        private Charset charset;

        public Builder()
        {
            requestTypeId = TYPE_ID;
        }

        public Builder(File file, String charset)
        {
            this();
            this.file = file;
            this.charset = Charset.forName(charset);
        }

        public OpenFileRequest build(Context context)
        {
            return new OpenFileRequest(context, accountId, networkId, notificationVisibility, title, mimeType,
                    requestTypeId, file, charset);
        }
    }

}

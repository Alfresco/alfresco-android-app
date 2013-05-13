/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.integration.file.update;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class RenameCallback extends AbstractOperationCallback<File>
{
    public RenameCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.update);
        complete = getBaseContext().getString(R.string.update_sucess);
    }

    @Override
    public void onPostExecute(Operation<File> task, File results)
    {
        super.onPostExecute(task, results);
        scanFile(context, results.getPath(), MimeTypeMap.getSingleton().getFileExtensionFromUrl(results.getPath()));
        // context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(results)));
    }

    public static void scanFile(Context context, String path, String mimeType)
    {
        Client client = new Client(path, mimeType);
        MediaScannerConnection connection = new MediaScannerConnection(context, client);
        client.connection = connection;
        connection.connect();
    }

    private static final class Client implements MediaScannerConnectionClient
    {
        private final String path;

        private final String mimeType;

        MediaScannerConnection connection;

        public Client(String path, String mimeType)
        {
            this.path = path;
            this.mimeType = mimeType;
        }

        @Override
        public void onMediaScannerConnected()
        {
            connection.scanFile(path, mimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri)
        {
            connection.disconnect();

        }
    }
}

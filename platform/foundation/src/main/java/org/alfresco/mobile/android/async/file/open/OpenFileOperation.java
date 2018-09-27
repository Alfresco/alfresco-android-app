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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.async.LoaderResult;
import org.alfresco.mobile.android.async.OperationAction;
import org.alfresco.mobile.android.async.OperationsDispatcher;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.file.FileOperationRequest;
import org.alfresco.mobile.android.async.impl.ListingOperation;
import org.alfresco.mobile.android.platform.EventBusManager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class OpenFileOperation extends ListingOperation<String>
{
    private static final String TAG = OpenFileOperation.class.getName();

    private File file;
    private Uri uri;

    private Charset charset;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public OpenFileOperation(Operator operator, OperationsDispatcher dispatcher, OperationAction action)
    {
        super(operator, dispatcher, action);
        if (request instanceof OpenFileRequest)
        {
            this.uri = ((OpenFileRequest) request).uri;
            this.file = ((FileOperationRequest) request).file;
            this.charset = ((OpenFileRequest) request).charset;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<String> doInBackground()
    {
        try
        {
            String s = null;
            LoaderResult<String> result = new LoaderResult<String>();

            try
            {
                if (file != null) {
                    s = readFile(file.getPath());
                } else {
                    s = readFile(context, uri);
                }
            }
            catch (Exception e)
            {
                result.setException(e);
                Log.e(TAG, Log.getStackTraceString(e));
            }

            result.setData(s);
            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return new LoaderResult<String>();
    }

    @Override
    protected void onPostExecute(LoaderResult<String> result)
    {
        super.onPostExecute(result);
        EventBusManager.getInstance().post(new OpenFileEvent(getRequestId(), result));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Internals
    // ///////////////////////////////////////////////////////////////////////////
    private String readFile(String path) throws IOException
    {
        FileInputStream stream = new FileInputStream(new File(path));
        try
        {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return charset.decode(bb).toString();
        }
        catch (Exception e)
        {
            // DO Nothing
            return null;
        }
        finally
        {
            IOUtils.closeStream(stream);
        }
    }

    private String readFile(Context context, Uri uri) throws IOException {
        FileInputStream stream = (FileInputStream) context.getContentResolver().openInputStream(uri);

        try
        {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return charset.decode(bb).toString();
        }
        catch (Exception e)
        {
            // DO Nothing
            return null;
        }
        finally
        {
            IOUtils.closeStream(stream);
        }
    }
}

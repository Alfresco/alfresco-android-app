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
package org.alfresco.mobile.android.application.fragments.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;

import android.app.Activity;
import android.util.Log;

/**
 * Loader responsible to do text file loading
 * 
 * @author Luke Jagger
 */
public class TextEditorLoader extends AbstractBaseLoader<LoaderResult<String>>
{
    /** Unique identifier. */
    public static final int ID = TextEditorLoader.class.hashCode();

    private static final String TAG = "TextEditorLoader";

    private File file;

    private Charset charset;

    public TextEditorLoader(Activity fr, File file, Charset charset)
    {
        super(fr);

        this.file = file;
        this.charset = charset;
    }

    @Override
    public LoaderResult<String> loadInBackground()
    {
        String s = "";
        LoaderResult<String> result = new LoaderResult<String>();

        try
        {
            s = readFile(file.getPath());
        }
        catch (Exception e)
        {
            result.setException(e);
            Log.e(TAG, Log.getStackTraceString(e));
        }
        
        result.setData(s);

        return result;
    }

    private String readFile(String path) throws IOException
    {
        FileInputStream stream = new FileInputStream(new File(path));
        try
        {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return charset.decode(bb).toString();
        }
        finally
        {
            stream.close();
        }
    }
}

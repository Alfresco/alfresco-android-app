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
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Scanner;
import java.util.Vector;

import org.alfresco.mobile.android.api.asynchronous.AbstractBaseLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

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
    
    public TextEditorLoader(Fragment fr, File file)
    {
        super(fr.getActivity());
        
        this.file = file;
    }

    @Override
    public LoaderResult<String> loadInBackground()
    {
        String s = new String();
        LoaderResult<String> result = new LoaderResult<String>();
        //long fileLen = file.length();
        //if (fileLen < Integer.MAX_VALUE)
        {
            try
            {
                s = new Scanner(file, "UTF-8" ).useDelimiter("\\A").next();
            }
            catch (Exception e)
            {
                result.setException(e);
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            result.setData(s);
        }
        //else
        //    result.setException(new Exception("Out of memory"));
        
        return result;
    }
}

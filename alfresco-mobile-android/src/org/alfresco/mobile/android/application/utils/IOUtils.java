/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/


package org.alfresco.mobile.android.application.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

public class IOUtils
{
    public static String writeAsset (Context c, String assetFilename) throws IOException
    {
        String newFilename = "";
        
        if (Environment.getExternalStorageState().equals (Environment.MEDIA_MOUNTED))
        {
            newFilename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + assetFilename;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            
            try
            {
                InputStream is = c.getAssets().open(assetFilename);
                OutputStream os = new FileOutputStream(newFilename);
                
                bis = new BufferedInputStream(is);
                bos = new BufferedOutputStream(os);
                byte[] buf = new byte[1024];
        
                int n = 0;
                int o = 0;
                while ((n = bis.read(buf, o, buf.length)) > 0) 
                {
                    bos.write(buf, 0, n);
                }
            }
            catch (IOException e)
            {
                newFilename = "";
            }
    
            if (bis != null)
            {
                bis.close();
            }
            if (bos != null)
            {
                bos.close();
            }
        }
        
        return newFilename;
    }

}

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

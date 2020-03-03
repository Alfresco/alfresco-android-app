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
package org.alfresco.mobile.android.application.extension.samsung.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.extension.samsung.pen.SNoteSDKDialogFragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;

public class SNoteUtils
{
    private static final String TAG = SNoteUtils.class.getName();

    private SNoteUtils()
    {
    }

    public static boolean processUnsupportedException(final FragmentActivity activity, SsdkUnsupportedException e)
    {
        e.printStackTrace();
        int errType = e.getType();
        SNoteSDKDialogFragment.newInstance(errType).show(activity.getSupportFragmentManager(),
                SNoteSDKDialogFragment.TAG);
        if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) { return false; }
        return true;
    }

    public static Bitmap decodeFile(File f, int requiredSize, int dpiClassification)
    {
        InputStream fis = null;
        Bitmap bmp = null;
        try
        {
            fis = new BufferedInputStream(new FileInputStream(f));
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            // Find the correct scale value. It should be the power of 2.
            int scale = calculateInSampleSize(o, requiredSize, requiredSize);

            // decode with inSampleSize
            fis = new BufferedInputStream(new FileInputStream(f));
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dpiClassification;
            o.inJustDecodeBounds = false;
            o.inPurgeable = true;
            bmp = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            IOUtils.closeStream(fis);
        }
        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        // Log.d(TAG, "height:" + height + "width" + width);

        return inSampleSize;
    }
}

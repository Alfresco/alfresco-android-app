/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.utils;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class VideoCapture extends DeviceCapture
{
    private static final long serialVersionUID = 1L;

    public VideoCapture(Activity parent, Folder folder)
    {
        super(parent, folder);
    }

    @Override
    public boolean hasDevice()
    {
        return (parentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) || parentActivity
                .getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT));
    }

    @Override
    public boolean captureData()
    {
        if (hasDevice())
        {
            try
            {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                payload = new File(Environment.getExternalStorageDirectory(), createFilename("", "mp4"));

                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(payload));
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                //Represents a limit of 300Mb
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 314572800L);

                parentActivity.startActivityForResult(intent, getRequestCode());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }

            return true;
        }
        else
            return false;
    }

    @Override
    protected void payloadCaptured(int requestCode, int resultCode, Intent data)
    {
    }
}

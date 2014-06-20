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
package org.alfresco.mobile.android.application.capture;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.utils.MessengerUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class VideoCapture extends DeviceCapture
{
    public static final String TAG = "VideoCapture";

    private static final long serialVersionUID = 1L;

    public VideoCapture(Activity parent, Folder folder)
    {
        this(parent, folder, null);
    }

    public VideoCapture(Activity parent, Folder folder, File parentFolder)
    {
        super(parent, folder, parentFolder);
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

                File folder = parentFolder;
                if (folder != null)
                {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                    payload = new File(folder.getPath(), createFilename("VID", "mp4"));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(payload));
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    // Represents a limit of 300Mb
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 314572800L);

                    parentActivity.startActivityForResult(intent, getRequestCode());
                }
                else
                {
                    MessengerUtils.showLongToast(parentActivity, parentActivity.getString(R.string.sdinaccessible));
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, Log.getStackTraceString(e));
                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected boolean payloadCaptured(int requestCode, int resultCode, Intent data)
    {
        return true;
    }
}

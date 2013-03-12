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
package org.alfresco.mobile.android.application.utils;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoCapture extends DeviceCapture
{
    private static final String TAG = "PhotoCapture";

    private static final long serialVersionUID = 1L;

    public PhotoCapture(Activity parent, Folder folder)
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
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File folder = StorageManager.getCaptureFolder(parentActivity, SessionUtils.getAccount(parentActivity)
                        .getUrl(), SessionUtils.getAccount(parentActivity).getUsername());
                if (folder != null)
                {
                    payload = new File(folder.getPath(), createFilename("", "jpg"));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(payload));

                    parentActivity.startActivityForResult(intent, getRequestCode());
                }
                else
                {
                    MessengerManager.showLongToast(parentActivity, parentActivity.getString(R.string.sdinaccessible));
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
    protected void payloadCaptured(int requestCode, int resultCode, Intent data)
    {
    }
}

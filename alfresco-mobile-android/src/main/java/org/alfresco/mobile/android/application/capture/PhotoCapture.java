/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.capture;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.FileProvider;
import android.util.Log;

public class PhotoCapture extends DeviceCapture
{
    private static final String TAG = "PhotoCapture";

    private static final long serialVersionUID = 1L;

    public PhotoCapture(FragmentActivity parent, Folder folder)
    {
        this(parent, folder, null, false);
    }

    public PhotoCapture(FragmentActivity parent, Folder folder, File parentFolder, boolean upload)
    {
        super(parent, folder, parentFolder, upload);
    }

    @Override
    public boolean hasDevice()
    {
        return ActionUtils.hasCameraAvailable(parentActivity);
    }

    @Override
    public boolean captureData()
    {
        if (hasDevice())
        {
            try
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(context.getPackageManager()) == null)
                {
                    AlfrescoNotificationManager.getInstance(context).showAlertCrouton(parentActivity,
                            context.getString(R.string.feature_disable));
                    return false;
                }

                File folder = parentFolder;
                if (folder != null)
                {
                    payload = new File(folder.getPath(), createFilename("IMG_", "jpg"));

                    Uri data;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        data = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", payload);
                    } else {
                        data = Uri.fromFile(payload);
                    }

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, data);

                    parentActivity.startActivityForResult(intent, getRequestCode());
                }
                else
                {
                    AlfrescoNotificationManager.getInstance(parentActivity).showLongToast(
                            parentActivity.getString(R.string.sdinaccessible));
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

    public int getRequestCode()
    {
        return 300;
    }
}

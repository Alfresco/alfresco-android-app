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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

public class AudioCapture extends DeviceCapture
{
    public static final String TAG = "AudioCapture";

    private static final long serialVersionUID = 1L;

    public AudioCapture(FragmentActivity parent, Folder folder)
    {
        this(parent, folder, null, false);
    }

    public AudioCapture(FragmentActivity parent, Folder folder, File parentFolder, boolean upload)
    {
        super(parent, folder, parentFolder, upload);
        // Default MIME type if it cannot be retrieved from Uri later.
        mimeType = "audio/3gpp";
    }

    @Override
    public boolean hasDevice()
    {
        return (parentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE));
    }

    @Override
    public boolean captureData()
    {
        if (hasDevice())
        {
            try
            {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                if (intent.resolveActivity(context.getPackageManager()) == null)
                {
                    AlfrescoNotificationManager.getInstance(context).showAlertCrouton(parentActivity,
                            context.getString(R.string.feature_disable));
                    return false;
                }
                else
                {
                    parentActivity.startActivityForResult(intent, getRequestCode());
                }
            }
            catch (Exception e)
            {
                AlfrescoNotificationManager.getInstance(context).showAlertCrouton(parentActivity,
                        context.getString(R.string.no_voice_recorder));
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
        Uri savedUri = data.getData();

        try
        {
            File folder = parentFolder;
            if (folder != null)
            {
                String filePath = getAudioFilePathFromUri(savedUri);
                String fileType = getAudioFileTypeFromUri(savedUri);
                String newFilePath = folder.getPath() + "/"
                        + createFilename("AUDIO", filePath.substring(filePath.lastIndexOf(".") + 1));
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    copyFile(savedUri, newFilePath);
                } else {
                    copyFile(filePath, newFilePath);
                    parentActivity.getContentResolver().delete(savedUri, null, null);
                }

                new File(filePath).delete();

                payload = new File(newFilePath);

                if (!fileType.isEmpty())
                {
                    mimeType = fileType;
                }

                return true;
            }
            else
            {
                AlfrescoNotificationManager.getInstance(parentActivity).showLongToast(
                        parentActivity.getString(R.string.sdinaccessible));
                return false;
            }
        }
        catch (IOException e)
        {
            AlfrescoNotificationManager.getInstance(context).showLongToast(context.getString(R.string.cannot_capture));
            Log.d(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    private String getAudioFilePathFromUri(Uri uri)
    {
        Cursor cursor = parentActivity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);

            return (index != -1 ? cursor.getString(index) : "");
        }
        else
        {
            return "";
        }
    }

    private String getAudioFileTypeFromUri(Uri uri)
    {
        Cursor cursor = parentActivity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE);
            return (index != -1 ? cursor.getString(index) : "");
        }
        else
        {
            return "";
        }
    }

    private void copyFile(Uri fileName, String newFileName) throws IOException
    {
        copyFile(parentActivity.getContentResolver().openInputStream(fileName), new FileOutputStream(newFileName));
    }

    private void copyFile(String fileName, String newFileName) throws IOException
    {
        copyFile(new FileInputStream(fileName), new FileOutputStream(newFileName));
    }

    private void copyFile(InputStream sourceStream, OutputStream destinationStream) throws IOException
    {
        try {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = sourceStream.read(buf)) > 0)
            {
                destinationStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            throw new IOException("Error during copy file", e);
        } finally {
            IOUtils.closeStream(sourceStream);
            IOUtils.closeStream(destinationStream);
        }
    }

    public int getRequestCode()
    {
        return 302;
    }
}

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
package org.alfresco.mobile.android.platform.intent;

import java.io.File;

import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

public class BaseActionUtils
{
    /**
     * Allow to open a file with an associated application installed in the
     * device and saved it backed via a requestcode...
     * 
     * @param myFile
     * @param mimeType
     */
    public static void openIn(Fragment fr, File myFile, String mimeType, int requestCode)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(myFile);
        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            if (fr.getParentFragment() != null)
            {
                fr.getParentFragment().startActivityForResult(intent, requestCode);
            }
            else
            {
                fr.startActivityForResult(intent, requestCode);
            }
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_file);
        }
    }

    public static void openIn(Fragment fr, File myFile, String mimeType)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(myFile);
        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            fr.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_file);
        }
    }

    /**
     * Allow to send a link to other application installed in the device.
     * 
     * @param fr
     * @param url
     */
    public static void actionShareLink(Fragment fr, String url)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, R.string.share_url_long);
            i.putExtra(Intent.EXTRA_TEXT, url);
            fr.startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_url)));
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_share_link);
        }
    }

    /**
     * Allow user to share a file with other applications.
     * 
     * @param fr
     * @param contentFile
     */
    public static void actionShareContent(Fragment fr, File contentFile)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getInstance(fr.getActivity()).getMIMEType(contentFile.getName()));
            fr.startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_share_content);
        }
    }

    /**
     * Allow to show map
     */
    public static void actionShowMap(Fragment fr, String name, String lattitude, String longitude)
    {
        try
        {
            final String uri = "geo:0,0?q=" + lattitude + "," + longitude + " (" + name + ")";
            fr.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_map);
        }
    }

    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionPickFile(Fragment f, int requestCode)
    {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        f.startActivityForResult(Intent.createChooser(i, f.getText(R.string.content_app_pick_file)), requestCode);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(Context context, Uri uri)
    {
        // DocumentProvider
        if (AndroidVersion.isKitKatOrAbove() && DocumentsContract.isDocumentUri(context, uri))
        {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary"
                        .equalsIgnoreCase(type)) { return Environment.getExternalStorageDirectory() + "/" + split[1]; }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) { return uri.getPath(); }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
    {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally
        {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public interface ActionManagerListener
    {
        void onActivityNotFoundException(ActivityNotFoundException e);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ERRORS & DIALOG
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionDisplayError(Fragment f, Exception e)
    {
        AlfrescoActivity activity = (AlfrescoActivity) f.getActivity();

        activity.removeWaitingDialog();
        String errorMessage = activity.getString(R.string.error_general);
        if (e instanceof AlfrescoAppException && ((AlfrescoAppException) e).isDisplayMessage())
        {
            errorMessage = e.getMessage();
        }
        AlfrescoNotificationManager.getInstance(activity).showLongToast(errorMessage);
        CloudExceptionUtils.handleCloudException(activity, e, false);
    }

}

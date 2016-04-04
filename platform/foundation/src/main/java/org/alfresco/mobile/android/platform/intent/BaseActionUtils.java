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
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.provider.CursorUtils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

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

    /**
     * Utils to get File path
     * 
     * @param uri
     * @return
     */
    public static String getPath(Context context, Uri uri)
    {
        String s = null;
        Cursor cursor = null;
        try
        {
            String scheme = uri.getScheme();
            if (scheme.equals("content"))
            {
                String[] projection = { MediaStore.Files.FileColumns.DATA };
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                cursor.moveToFirst();
                s = cursor.getString(columnIndex);
            }
            else if (scheme.equals("file"))
            {
                s = uri.getPath();
            }
            // Log.d("ActionManager", "URI:" + uri + " - S:" + s);
        }
        catch (Exception e)
        {

        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
        return s;
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
        Intent i = new Intent(PrivateIntent.ACTION_DISPLAY_ERROR);
        if (e != null)
        {
            i.putExtra(PrivateIntent.EXTRA_ERROR_DATA, e);
        }
        LocalBroadcastManager.getInstance(f.getActivity()).sendBroadcast(i);
    }
}

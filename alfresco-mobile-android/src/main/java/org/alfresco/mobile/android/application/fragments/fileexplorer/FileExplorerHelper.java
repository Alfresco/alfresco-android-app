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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;

import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;

public final class FileExplorerHelper
{

    private FileExplorerHelper()
    {
    }

    public static final String FILEEXPLORER_PREFS = "org.alfresco.mobile.android.fileexplorer.preferences";

    private static final String FILEEXPLORER_DEFAULT = "org.alfresco.mobile.android.fileexplorer.preferences.default";

    public static void displayNavigationMode(final AlfrescoActivity activity, final int mode, final boolean backStack,
            int menuId)
    {
        ActionBar bar = activity.getAppActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ShortCutFolderMenuAdapter adapter = new ShortCutFolderMenuAdapter(activity);

        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener()
        {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId)
            {
                SharedPreferences prefs = activity.getSharedPreferences(FILEEXPLORER_PREFS, 0);
                int currentSelection = prefs.getInt(FILEEXPLORER_DEFAULT, 1);

                if (!backStack && itemPosition == currentSelection) { return true; }

                File currentLocation = null;
                int mediatype = -1;
                boolean thirdPartyApp = false;
                switch (itemPosition)
                {
                    case 1:
                        currentLocation = AlfrescoStorageManager.getInstance(activity)
                                .getDownloadFolder(((BaseActivity) activity).getCurrentAccount());
                        break;
                    case 3:
                        currentLocation = FileExplorerHelper.requestWriteExternalStorage(activity, null);
                        break;
                    case 4:
                        currentLocation = FileExplorerHelper.requestWriteExternalStorage(activity,
                                Environment.DIRECTORY_DOWNLOADS);
                        break;
                    case 6:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
                        break;
                    case 7:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
                        break;
                    case 8:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                        break;
                    case 9:
                        mediatype = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                        break;
                    case 11:
                        thirdPartyApp = true;
                        break;
                    default:
                        break;
                }

                if (thirdPartyApp)
                {
                    if (activity instanceof PublicDispatcherActivity)
                    {
                        activity.setResult(RequestCode.FILEPICKER, new Intent(PrivateIntent.ACTION_PICK_FILE));
                        activity.finish();
                    }
                    return true;
                }
                else if (currentLocation != null)
                {
                    if (!backStack)
                    {
                        activity.getSupportFragmentManager().popBackStack();
                    }

                    FileExplorerFragment.with(activity).menuId(itemPosition).isShortCut(true).file(currentLocation)
                            .mode(mode).display();
                }
                else if (mediatype >= 0)
                {
                    if (requestWriteExternalStorage(activity, null) != null)
                    {
                        if (!backStack)
                        {
                            activity.getSupportFragmentManager().popBackStack();
                        }

                        LibraryFragment.with(activity).mediaType(mediatype).mode(mode).isShortCut(true)
                                .menuId(itemPosition).display();
                    }
                }
                prefs.edit().putInt(FILEEXPLORER_DEFAULT, itemPosition).apply();
                return true;
            }

        };
        bar.setListNavigationCallbacks(adapter, mOnNavigationListener);
        bar.setSelectedNavigationItem(menuId);
    }

    public static File requestWriteExternalStorage(Activity activity, final String externalFolder)
    {
        try
        {
            if (Build.VERSION.SDK_INT < 23) { return (externalFolder == null)
                    ? Environment.getExternalStorageDirectory()
                    : Environment.getExternalStoragePublicDirectory(externalFolder); }

            int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    ActivityCompat.requestPermissions(activity,
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, externalFolder == null
                                    ? BaseActivity.REQUEST_PERMISSION_SD : BaseActivity.REQUEST_PERMISSION_DL);
                    return null;
                }
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        externalFolder == null ? BaseActivity.REQUEST_PERMISSION_SD
                                : BaseActivity.REQUEST_PERMISSION_DL);
                return null;
            }
            else
            {
                return (externalFolder == null) ? Environment.getExternalStorageDirectory()
                        : Environment.getExternalStoragePublicDirectory(externalFolder);
            }
        }
        catch (Exception e)
        {
            Log.d(FileExplorerMenuFragment.TAG, Log.getStackTraceString(e));
        }
        return null;
    }
}

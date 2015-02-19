/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.extension.scansnap;

import java.util.List;

import org.alfresco.mobile.android.application.extension.scansnap.presets.BusinessCardPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.DefaultPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.DocumentPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.PhotoPreset;
import org.alfresco.mobile.android.application.extension.scansnap.presets.ScanSnapPreset;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.extensions.ScanSnapManager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

public class ScanSnapManagerImpl extends ScanSnapManager
{
    private static final String TAG = ScanSnapManagerImpl.class.getSimpleName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static ScanSnapManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ScanSnapManagerImpl(context);
            }

            return (ScanSnapManager) mInstance;
        }
    }

    protected ScanSnapManagerImpl(Context context)
    {
        super(context);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void startPresetChooser(Activity activity)
    {
        if (hasScanSnapApplication())
        {
            ScanSnapPresetsDialogFragment dialogft = ScanSnapPresetsDialogFragment.newInstance();
            dialogft.show(activity.getFragmentManager(), ScanSnapPresetsDialogFragment.TAG);
        }
    }

    @Override
    public void scan(Activity activity, Integer presetId)
    {
        if (hasScanSnapApplication())
        {
            AlfrescoNotificationManager notification = AlfrescoNotificationManager.getInstance(appContext);
            try
            {
                ScanSnapPreset preset;
                switch (presetId)
                {
                    case DefaultPreset.ID:
                        preset = new DefaultPreset();
                        break;
                    case DocumentPreset.ID:
                        preset = new DocumentPreset();
                        break;
                    case PhotoPreset.ID:
                        preset = new PhotoPreset();
                        break;
                    case BusinessCardPreset.ID:
                        preset = new BusinessCardPreset();
                        break;
                    default:
                        preset = new DefaultPreset();
                        break;
                }

                Uri uri = preset.generateURI();
                Log.d(ScanSnapManagerImpl.TAG, uri.toString());

                // check whether the url format is correct
                if (uri.getScheme() == null)
                {
                    notification.showToast(appContext.getResources().getString(R.string.err_msg_url_parse));
                    return;
                }

                Intent in = new Intent();
                in.setData(uri);
                activity.startActivity(in);
            }
            catch (ActivityNotFoundException e)
            {
                // specified application can not be found
                notification.showToast(appContext.getResources().getString(R.string.err_msg_launch_failed));
            }
            catch (NullPointerException e)
            {
                notification.showToast(appContext.getResources().getString(R.string.err_msg_url_parse));
            }
        }
    }

    @Override
    public boolean hasScanSnapApplication()
    {
        try
        {
            Uri uri = Uri.parse("scansnap:///Scan&OutMode=3");

            Intent in = new Intent();
            in.setData(uri);

            final PackageManager mgr = appContext.getPackageManager();
            List<ResolveInfo> list = mgr.queryIntentActivities(in, PackageManager.MATCH_DEFAULT_ONLY);

            return list.size() > 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}

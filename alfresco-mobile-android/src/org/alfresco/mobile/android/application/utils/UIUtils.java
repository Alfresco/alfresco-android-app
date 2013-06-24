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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.help.HelpDialogFragment;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;

/**
 * Utility around UI Management.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UIUtils
{

    /**
     * Set the background view with the drawable associated.
     * 
     * @param v
     * @param background
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View v, Drawable background)
    {
        if (AndroidVersion.isJBOrAbove())
        {
            v.setBackground(background);
        }
        else
        {
            v.setBackgroundDrawable(background);
        }
    }

    public static int[] getScreenDimension(Activity activity)
    {
        int width = 0;
        int height = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        if (AndroidVersion.isHCMR2OrAbove())
        {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        else
        {
            width = display.getWidth(); // deprecated
            height = display.getHeight(); // deprecated
        }

        return new int[] { width, height };
    }
    
    private static final Pattern NAME_PATTERN = Pattern
            .compile("(.*[\"\\*\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)");
    
    
    public static boolean hasValideName(String name){
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }
    
    /**
     * Display PDF User Guide.
     * @param activity
     */
    public static void displayHelp(Activity activity)
    {
        String pathHelpGuideFile = null;
        try
        {
            long lastUpdate = activity.getPackageManager().getPackageInfo(activity.getApplicationContext().getPackageName(), 0).lastUpdateTime;
            // Check last update time of the app and compare to an
            // existing (or not) help guide.
            File assetFolder = StorageManager.getAssetFolder(activity);
            String helpGuideName = activity.getString(R.string.asset_folder_prefix) + "_" + activity.getString(R.string.help_user_guide);
            File helpGuideFile = new File(assetFolder, helpGuideName);

            if (!helpGuideFile.exists() || helpGuideFile.lastModified() < lastUpdate)
            {
                String assetfilePath = activity.getString(R.string.help_path) + helpGuideName;
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(activity.getAssets().open(assetfilePath), helpGuideFile);
            }

            pathHelpGuideFile = helpGuideFile.getPath();

            if (!ActionManager.launchPDF(activity, pathHelpGuideFile))
            {
                new HelpDialogFragment().show(activity.getFragmentManager(), HelpDialogFragment.TAG);
            }
        }
        catch (Exception e)
        {
            Log.e("HelpGuide", "Unable to open help guide.");
        }
    }

}

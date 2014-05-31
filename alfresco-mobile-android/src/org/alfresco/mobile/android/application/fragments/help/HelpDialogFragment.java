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
package org.alfresco.mobile.android.application.fragments.help;

import java.io.File;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class HelpDialogFragment extends DialogFragment
{
    public static final String TAG = HelpDialogFragment.class.getName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_alfresco)
                .setTitle(R.string.app_name).setMessage(R.string.get_pdf_viewer)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        ActionUtils.getAdobeReader(getActivity());
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            TextView messageText = (TextView) getDialog().findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            getDialog().show();
        }
        super.onResume();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Display PDF User Guide.
     * 
     * @param activity
     */
    public static void displayHelp(Activity activity)
    {
        String pathHelpGuideFile = null;
        try
        {
            long lastUpdate = activity.getPackageManager().getPackageInfo(
                    activity.getApplicationContext().getPackageName(), 0).lastUpdateTime;
            // Check last update time of the app and compare to an
            // existing (or not) help guide.
            File assetFolder = AlfrescoStorageManager.getInstance(activity).getAssetFolder();
            String helpGuideName = activity.getString(R.string.asset_folder_prefix) + "_"
                    + activity.getString(R.string.help_user_guide);
            File helpGuideFile = new File(assetFolder, helpGuideName);

            if (!helpGuideFile.exists() || helpGuideFile.lastModified() < lastUpdate)
            {
                String assetfilePath = activity.getString(R.string.help_path) + helpGuideName;
                org.alfresco.mobile.android.api.utils.IOUtils.copyFile(activity.getAssets().open(assetfilePath),
                        helpGuideFile);
            }

            pathHelpGuideFile = helpGuideFile.getPath();

            if (!ActionUtils.launchPDF(activity, pathHelpGuideFile))
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
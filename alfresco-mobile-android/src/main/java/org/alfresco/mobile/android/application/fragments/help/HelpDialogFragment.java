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

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.managers.ActionUtils;

import android.app.Activity;
import android.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;

public class HelpDialogFragment extends DialogFragment
{
    public static final String TAG = HelpDialogFragment.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static void displayHelp(Activity activity)
    {
        try
        {
            String prefix = activity.getString(R.string.docs_prefix);
            String urlValue = null;
            if (TextUtils.isEmpty(prefix))
            {
                urlValue = activity.getString(R.string.help_user_guide_default_url);
            }
            else
            {
                urlValue = String.format(activity.getString(R.string.help_user_guide_url), prefix, prefix);
            }
            ActionUtils.openURL(activity, urlValue);
        }
        catch (Exception e)
        {
            Log.e("HelpGuide", "Unable to open help guide.");
        }
    }
}
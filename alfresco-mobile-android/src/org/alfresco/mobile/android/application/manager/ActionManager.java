/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.manager;

import java.io.File;
import java.util.List;

import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

public class ActionManager extends org.alfresco.mobile.android.ui.manager.ActionManager
{

    public static final String REFRESH_EXTRA = "refreshExtra";

    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionRefresh(Fragment f, String refreshCategory, String type, Bundle bundle)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_REFRESH);
        i.addCategory(refreshCategory);
        if (type != null && type.length() > 0)
        {
            i.setType(type);
        }
        if (bundle != null)
        {
            i.putExtra(REFRESH_EXTRA, bundle);
        }
        f.startActivity(i);
    }

    public static void actionRefresh(Fragment f, String refreshCategory, String type)
    {
        actionRefresh(f, refreshCategory, type, null);
    }

    public static void actionDisplayError(Fragment f, Exception e)
    {
        String intentId = IntentIntegrator.ACTION_DISPLAY_ERROR;
        if (f.getActivity() instanceof HomeScreenActivity)
        {
            intentId = IntentIntegrator.ACTION_DISPLAY_ERROR_HOMESCREEN;
        }
        Intent i = new Intent(intentId);
        if (e != null)
        {
            i.putExtra(IntentIntegrator.DISPLAY_ERROR_DATA, e);
        }
        f.startActivity(i);
    }

    public static void actionRequestUserAuthentication(Activity activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH);
        i.setType(IntentIntegrator.ACCOUNT_TYPE);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.ACCOUNT_TYPE, account.getId());
        }
        activity.startActivity(i);
    }

    public static void actionRequestAuthentication(Activity activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH_REFRESH);
        i.setType(IntentIntegrator.ACCOUNT_TYPE);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.ACCOUNT_TYPE, account.getId());
        }
        activity.startActivity(i);
    }

    public static boolean launchPDF(Context c, String pdfFile)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(pdfFile)), "application/pdf");

        PackageManager pm = c.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() > 0)
        {
            c.startActivity(intent);
        }
        else
        {
            return false;
        }

        return true;
    }

    public static void getAdobeReader(Context c)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.adobe.reader"));
        c.startActivity(intent);
    }
}

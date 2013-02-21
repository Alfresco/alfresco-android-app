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
import java.io.IOException;
import java.util.List;

import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.integration.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ActionManager extends org.alfresco.mobile.android.ui.manager.ActionManager
{

    public static final String REFRESH_EXTRA = "refreshExtra";

    /**
     * Allow user to share a file with other applications.
     * 
     * @param fr
     * @param contentFile
     */
    public static void actionShareContent(Fragment fr, File contentFile, int requestCode)
    {
        try
        {
            if (CipherUtils.isEncrypted(fr.getActivity(), contentFile.getPath(), true))
            {
                if (CipherUtils.decryptFile(fr.getActivity(), contentFile.getPath()))
                    PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit().putString("RequiresEncrypt", contentFile.getPath()).commit();
            }
            
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
            fr.startActivityForResult(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)), requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
        catch (IOException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.decryption_failed);
            e.printStackTrace();
        }
        catch (Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.decryption_failed);
            e.printStackTrace();
        }
    }
    
    public static void openIn(Fragment fr, File myFile, String mimeType, int requestCode)
    {
        try
        {
            if (CipherUtils.isEncrypted(fr.getActivity(), myFile.getPath(), true))
            {
                if (CipherUtils.decryptFile(fr.getActivity(), myFile.getPath()))
                    PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit().putString("RequiresEncrypt", myFile.getPath()).commit();
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.fromFile(myFile);
            intent.setDataAndType(data, mimeType.toLowerCase());

            fr.startActivityForResult(intent, requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
        catch (Exception e)
        {
            MessengerManager.showLongToast(fr.getActivity(), fr.getString(R.string.decryption_failed));
            e.printStackTrace();
        }
    }
    
    public static void actionView(Fragment fr, File myFile, String mimeType, ActionManagerListener listener, int requestCode)
    {
        try
        {
            if (CipherUtils.isEncrypted(fr.getActivity(), myFile.getPath(), true))
            {
                if (CipherUtils.decryptFile(fr.getActivity(), myFile.getPath()))
                    PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit().putString("RequiresEncrypt", myFile.getPath()).commit();
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.fromFile(myFile);
            intent.setDataAndType(data, mimeType.toLowerCase());
            
            try
            {
                fr.startActivityForResult(intent, requestCode);
            }
            catch (ActivityNotFoundException e)
            {
                if (listener != null)
                {
                    listener.onActivityNotFoundException(e);
                }
            }
        }
        catch (Exception e)
        {
            MessengerManager.showLongToast(fr.getActivity(), fr.getString(R.string.decryption_failed));
            e.printStackTrace();
        }
    }
    
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

    public static void actionDisplayDialog(Activity activity, Bundle bundle)
    {
        String IntentId = IntentIntegrator.ACTION_DISPLAY_DIALOG;
        if (activity instanceof HomeScreenActivity)
        {
            IntentId = IntentIntegrator.ACTION_DISPLAY_DIALOG_HOMESCREEN;
        }

        Intent i = new Intent(IntentId);
        if (bundle != null)
        {
            i.putExtras(bundle);
        }
        activity.startActivity(i);
    }

    public static void actionDisplayDialog(Fragment f, Bundle bundle)
    {
        String IntentId = IntentIntegrator.ACTION_DISPLAY_DIALOG;
        if (f.getActivity() instanceof HomeScreenActivity)
        {
            IntentId = IntentIntegrator.ACTION_DISPLAY_DIALOG_HOMESCREEN;
        }

        Intent i = new Intent(IntentId);
        if (bundle != null)
        {
            i.putExtras(bundle);
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
        if (f.getActivity() instanceof PublicDispatcherActivity)
        {
            intentId = IntentIntegrator.ACTION_DISPLAY_ERROR_IMPORT;
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

    /**
     * Open Play Store application or its web version if no play store available.
     * @param c : Android Context
     */
    public static void actionDisplayPlayStore(Context c)
    {
        // Retrieve list of application that understand market Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=org.alfresco.mobile.android.application"));
        final PackageManager mgr = c.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, 0);

        // By default we redirect to the webbrowser version of play store.
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/"));

        for (ResolveInfo resolveInfo : list)
        {
            // If we find something related to android we open the application
            // version of play store.
            if (resolveInfo.activityInfo.applicationInfo.packageName.contains("android"))
            {
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                        resolveInfo.activityInfo.name));
                intent.setData(Uri.parse("market://"));
                break;
            }
        }
        c.startActivity(intent);
    }
    
    public static void actionSendContent(Activity activity, File contentFile)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
            activity.startActivity(Intent.createChooser(i, activity.getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_share_content);
        }
    }
}

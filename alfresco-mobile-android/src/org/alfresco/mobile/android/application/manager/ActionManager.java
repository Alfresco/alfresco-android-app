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
package org.alfresco.mobile.android.application.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.encryption.EncryptionDialogFragment;
import org.alfresco.mobile.android.application.integration.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.security.CipherUtils;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public class ActionManager extends org.alfresco.mobile.android.ui.manager.ActionManager
{

    public static final String TAG = ActionManager.class.getName();

    public static final String REFRESH_EXTRA = "refreshExtra";

    public static void actionShareContent(Fragment fr, File myFile)
    {
        actionSendContent(fr.getActivity(), myFile);
    }

    public static void actionShareContent(Fragment fr, List<File> files)
    {
        if (files.size() == 1)
        {
            actionShareContent(fr, files.get(0));
            return;
        }

        try
        {
            Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<Uri>();
            // convert from paths to Android friendly Parcelable Uri's
            for (File file : files)
            {
                Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            i.setType(MimeTypeManager.getMIMEType("text/plain"));
            fr.getActivity().startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    public static void actionShareContent(Activity activity, File myFile)
    {
        try
        {
            String mimeType = MimeTypeManager.getMIMEType(myFile.getName());
            if (CipherUtils.isEncryptionActive(activity))
            {
                FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                EncryptionDialogFragment fragment = EncryptionDialogFragment.decrypt(myFile, mimeType, null,
                        Intent.ACTION_SEND);
                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                fragmentTransaction.commit();
            }
            else
            {
                actionSendContent(activity, myFile);
            }
        }
        catch (Exception e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_open_file);
        }
    }

    public static void actionView(Fragment fr, File myFile, ActionManagerListener listener)
    {
        try
        {
            String mimeType = MimeTypeManager.getMIMEType(myFile.getName());
            if (CipherUtils.isEncryptionActive(fr.getActivity()))
            {
                myFile = IOUtils.makeTempFile(myFile);

                FragmentTransaction fragmentTransaction = fr.getActivity().getFragmentManager().beginTransaction();
                EncryptionDialogFragment fragment = EncryptionDialogFragment.decrypt(myFile, mimeType, listener,
                        Intent.ACTION_VIEW);
                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                fragmentTransaction.commit();
            }
            else
            {
                actionView(fr.getActivity(), myFile, mimeType);
            }
        }
        catch (Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    public static void actionDisplayDialog(Context context, Bundle bundle)
    {
        String intentId = IntentIntegrator.ACTION_DISPLAY_DIALOG;
        Intent i = new Intent(intentId);
        if (bundle != null)
        {
            i.putExtras(bundle);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void actionDisplayDialog(Fragment f, Bundle bundle)
    {
        String intentId = IntentIntegrator.ACTION_DISPLAY_DIALOG;
        if (f.getActivity() instanceof HomeScreenActivity)
        {
            intentId = IntentIntegrator.ACTION_DISPLAY_DIALOG_HOMESCREEN;
        }

        Intent i = new Intent(intentId);
        if (bundle != null)
        {
            i.putExtras(bundle);
        }
        f.startActivity(i);
    }

    public static void actionDisplayError(Fragment f, Exception e)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_ERROR);
        if (e != null)
        {
            i.putExtra(IntentIntegrator.DISPLAY_ERROR_DATA, e);
        }
        LocalBroadcastManager.getInstance(f.getActivity()).sendBroadcast(i);
    }

    public static void actionRequestUserAuthentication(Context activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void actionRequestAuthentication(Context activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH_REFRESH);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
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
     * 
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

    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionPickFile(Fragment f, int requestCode)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            // i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            f.startActivityForResult(Intent.createChooser(i, f.getText(R.string.content_app_pick_file)), requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(f.getActivity(), R.string.error_unable_open_file);
        }
    }

    public static void actionDisplayOperations(Activity activity)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS);
        activity.startActivity(i);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public static void reloadAccount(Activity activity, Account account, String networkId)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
            i.putExtra(IntentIntegrator.EXTRA_NETWORK_ID, networkId);
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void reloadAccount(Activity activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void loadAccount(Activity activity, Account account)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void loadAccount(Activity activity, Account account, OAuthData data)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
            i.putExtra(IntentIntegrator.EXTRA_OAUTH_DATA, data);
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void createAccount(Activity activity, CreateAccountRequest request)
    {
        Intent i = new Intent(IntentIntegrator.ACTION_CREATE_ACCOUNT);
        i.putExtra(IntentIntegrator.EXTRA_CREATE_REQUEST, request);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static boolean createMailWithAttachment(Fragment fr, String subject, String content, Uri attachment,
            int requestCode)
    {
        try
        {
            if (CipherUtils.isEncrypted(fr.getActivity(), attachment.getPath(), true)
                    && CipherUtils.decryptFile(fr.getActivity(), attachment.getPath()))
            {
                PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit()
                        .putString(GeneralPreferences.REQUIRES_ENCRYPT, attachment.getPath()).commit();
            }

            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content));
            i.putExtra(Intent.EXTRA_STREAM, attachment);
            i.setType("text/plain");
            fr.startActivityForResult(Intent.createChooser(i, fr.getString(R.string.send_email)), requestCode);

            return true;
        }
        catch (IOException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.decryption_failed);
            Log.d(TAG, Log.getStackTraceString(e));
        }
        catch (Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.decryption_failed);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    public static boolean createMailWithLink(Context c, String subject, String content, Uri link)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, content);
        i.setType("text/plain");
        c.startActivity(Intent.createChooser(i, String.format(c.getString(R.string.send_email), link.toString())));

        return true;
    }

    public static void actionUpload(Activity activity, File file)
    {
        try
        {
            String mimeType = MimeTypeManager.getMIMEType(file.getName());
            if (CipherUtils.isEncryptionActive(activity))
            {
                FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                EncryptionDialogFragment fragment = EncryptionDialogFragment.decrypt(file, mimeType, null,
                        Intent.ACTION_SEND);
                fragmentTransaction.add(fragment, fragment.getFragmentTransactionTag());
                fragmentTransaction.commit();
            }
            else
            {
                actionUploadDocument(activity, file);
            }
        }
        catch (Exception e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_open_file);
        }
    }
    
    public static void actionUploadDocument(Activity activity, File contentFile)
    {
        try
        {
            Intent i = new Intent(activity, PublicDispatcherActivity.class);
            i.setAction(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
            activity.startActivity(i);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_share_content);
        }
    }
    

}

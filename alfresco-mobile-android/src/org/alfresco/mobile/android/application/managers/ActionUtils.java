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
package org.alfresco.mobile.android.application.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.extensions.SamsungManager;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.MessengerUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

/**
 * @author Jean Marie Pascal
 */
public class ActionUtils extends BaseActionUtils
{
    public static final String TAG = ActionUtils.class.getName();

    public static void actionOpenIn(Fragment fr, File myFile)
    {
        try
        {
            String mimeType = MimeTypeManager.getInstance(fr.getActivity()).getMIMEType(myFile.getName());
            if (DataProtectionManager.getInstance(fr.getActivity()).isEncrypted(myFile.getPath()))
            {
                WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        R.string.decryption_title, true);
                dialog.show(fr.getActivity().getFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(fr.getActivity()).decrypt(SessionUtils.getAccount(fr.getActivity()),
                        myFile, DataProtectionManager.ACTION_COPY);
            }
            else
            {
                actionView(fr.getActivity(), myFile, mimeType, null);
            }
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Open a local file with a 3rd party application. Manage automatically with
     * Data Protection.
     * 
     * @param fr
     * @param myFile
     * @param listener
     */
    public static void actionView(Fragment fr, File myFile, ActionManagerListener listener)
    {
        try
        {
            String mimeType = MimeTypeManager.getInstance(fr.getActivity()).getMIMEType(myFile.getName());
            if (SamsungManager.getInstance(fr.getActivity()) != null
                    && SamsungManager.getInstance(fr.getActivity()).hasPenEnable()
                    && (mimeType == null || mimeType.equals("application/octet-stream"))
                    && MimeTypeManager.getExtension(myFile.getName()).equals(SamsungManager.SAMSUNG_NOTE_EXTENSION_SPD))
            {
                mimeType = SamsungManager.SAMSUNG_NOTE_MIMETYPE;
            }
            if (DataProtectionManager.getInstance(fr.getActivity()).isEncrypted(myFile.getPath()))
            {
                WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        R.string.decryption_title, true);
                dialog.show(fr.getActivity().getFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(fr.getActivity()).decrypt(SessionUtils.getAccount(fr.getActivity()),
                        myFile, DataProtectionManager.ACTION_VIEW);
            }
            else
            {
                actionView(fr.getActivity(), myFile, mimeType, listener);
            }
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    public static Intent createViewIntent(Activity activity, File contentFile)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(contentFile);
        intent.setDataAndType(data, MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName())
                .toLowerCase());
        return intent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ERRORS & DIALOG
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionDisplayDialog(Context context, Bundle bundle)
    {
        String intentId = PrivateIntent.ACTION_DISPLAY_DIALOG;
        Intent i = new Intent(intentId);
        if (bundle != null)
        {
            i.putExtras(bundle);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void actionDisplayError(Fragment f, Exception e)
    {
        Intent i = new Intent(PrivateIntent.ACTION_DISPLAY_ERROR);
        if (e != null)
        {
            i.putExtra(PrivateIntent.EXTRA_ERROR_DATA, e);
        }
        LocalBroadcastManager.getInstance(f.getActivity()).sendBroadcast(i);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PDF
    // ///////////////////////////////////////////////////////////////////////////
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

    // ///////////////////////////////////////////////////////////////////////////
    // PLAY STORE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Open Play Store application or its web version if no play store
     * available.
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

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION SEND / SHARE
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionSend(Activity activity, File myFile, ActionManagerListener listener)
    {
        try
        {
            String mimeType = MimeTypeManager.getInstance(activity).getMIMEType(myFile.getName());
            if (SamsungManager.getInstance(activity) != null && SamsungManager.getInstance(activity).hasPenEnable()
                    && (mimeType == null || mimeType.equals("application/octet-stream"))
                    && MimeTypeManager.getExtension(myFile.getName()).equals(SamsungManager.SAMSUNG_NOTE_EXTENSION_SPD))
            {
                mimeType = SamsungManager.SAMSUNG_NOTE_MIMETYPE;
            }
            if (DataProtectionManager.getInstance(activity).isEncrypted(myFile.getPath()))
            {
                WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        R.string.decryption_title, true);
                dialog.show(activity.getFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), myFile,
                        DataProtectionManager.ACTION_SEND);
            }
            else
            {
                actionSend(activity, myFile);
            }
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(activity, R.string.error_unable_open_file);
        }
    }

    public static void actionSendDocument(Fragment fr, File myFile)
    {
        actionSend(fr.getActivity(), myFile);
    }

    public static void actionSend(Activity activity, File contentFile)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()));
            activity.startActivity(Intent.createChooser(i, activity.getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerUtils.showToast(activity, R.string.error_unable_share_content);
        }
    }

    public static Intent createSendIntent(Activity activity, File contentFile)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
        i.setType(MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()));
        return i;
    }

    public static boolean actionSendMailWithAttachment(Fragment fr, String subject, String content, Uri attachment,
            int requestCode)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content));
            i.putExtra(Intent.EXTRA_STREAM, attachment);
            i.setType("text/plain");
            fr.startActivityForResult(Intent.createChooser(i, fr.getString(R.string.send_email)), requestCode);

            return true;
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(fr.getActivity(), R.string.decryption_failed);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    public static boolean actionSendMailWithLink(Context c, String subject, String content, Uri link)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, content);
        i.setType("text/plain");
        c.startActivity(Intent.createChooser(i, String.format(c.getString(R.string.send_email), link.toString())));

        return true;
    }

    public static void actionSendDocumentToAlfresco(Activity activity, File file)
    {
        try
        {
            if (DataProtectionManager.getInstance(activity).isEncryptable(SessionUtils.getAccount(activity), file))
            {
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), file,
                        DataProtectionManager.ACTION_SEND_ALFRESCO);
            }
            else
            {
                actionSendFileToAlfresco(activity, file);
            }
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(activity, R.string.error_unable_open_file);
        }
    }

    public static void actionSendFileToAlfresco(Activity activity, File contentFile)
    {
        try
        {
            activity.startActivity(createSendFileToAlfrescoIntent(activity, contentFile));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerUtils.showToast(activity, R.string.error_unable_share_content);
        }
    }

    public static Intent createSendFileToAlfrescoIntent(Activity activity, File contentFile)
    {
        Intent i = new Intent(activity, PublicDispatcherActivity.class);
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
        i.setType(MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()));
        return i;
    }

    public static void actionShareContent(Activity activity, File myFile)
    {
        try
        {
            if (DataProtectionManager.getInstance(activity).isEncrypted(myFile.getPath()))
            {
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), myFile,
                        DataProtectionManager.ACTION_SEND);
            }
            else
            {
                actionSend(activity, myFile);
            }
        }
        catch (Exception e)
        {
            MessengerUtils.showToast(activity, R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION SEND MULTIPLE
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionSendDocumentsToAlfresco(Fragment fr, List<File> files)
    {
        if (files.size() == 1)
        {
            actionSendDocumentToAlfresco(fr.getActivity(), files.get(0));
            return;
        }

        try
        {
            Intent i = new Intent(fr.getActivity(), PublicDispatcherActivity.class);
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<Uri>();
            // convert from paths to Android friendly Parcelable Uri's
            for (File file : files)
            {
                Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            i.setType(MimeTypeManager.getInstance(fr.getActivity()).getMIMEType("text/plain"));
            fr.getActivity().startActivity(i);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerUtils.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    public static void actionSendDocuments(Fragment fr, List<File> files)
    {
        if (files.size() == 1)
        {
            actionSendDocument(fr, files.get(0));
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
            i.setType(MimeTypeManager.getInstance(fr.getActivity()).getMIMEType("text/plain"));
            fr.getActivity().startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerUtils.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICK FILE
    // ///////////////////////////////////////////////////////////////////////////
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
            f.startActivityForResult(Intent.createChooser(i, f.getText(R.string.content_app_pick_file)), requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerUtils.showToast(f.getActivity(), R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // AUTHENTICATION
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionRequestUserAuthentication(Context context, AlfrescoAccount account)
    {
        Intent i = new Intent(PrivateIntent.ACTION_USER_AUTHENTICATION);
        i.addCategory(PrivateIntent.CATEGORY_OAUTH);
        if (account != null)
        {
            i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void actionRequestAuthentication(Context context, AlfrescoAccount account)
    {
        Intent i = new Intent(PrivateIntent.ACTION_USER_AUTHENTICATION);
        i.addCategory(PrivateIntent.CATEGORY_OAUTH_REFRESH);
        if (account != null)
        {
            i.putExtra(PrivateIntent.EXTRA_ACCOUNT_ID, account.getId());
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SPEECH TO TEXT
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean hasSpeechToText(Context context)
    {
        try
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
            return !list.isEmpty();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}

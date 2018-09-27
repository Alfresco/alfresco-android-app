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
package org.alfresco.mobile.android.application.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.editors.text.TextEditorActivity;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.extensions.SamsungManager;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.WaitingDialogFragment;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
                dialog.show(fr.getActivity().getSupportFragmentManager(), WaitingDialogFragment.TAG);
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
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_file);
        }
    }

    public static void openWithAlfrescoTextEditor(Fragment fr, File myFile, String mimeType, int requestCode)
    {
        Intent intent = new Intent(fr.getActivity(), TextEditorActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        Uri data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                data = FileProvider.getUriForFile(fr.getActivity(), fr.getActivity().getApplicationContext().getPackageName() + ".provider", myFile);
        } else {
            data = Uri.fromFile(myFile);
        }

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
                    org.alfresco.mobile.android.foundation.R.string.error_unable_open_file);
        }
    }

    private static boolean isLocalFile(File file) {
        return file.getPath().startsWith("/storage/emulated");
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionView(FragmentActivity context, File myFile, String mimeType,
            ActionManagerListener listener)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                data = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", myFile);
        } else {
            data = Uri.fromFile(myFile);
        }

        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            if (intent.resolveActivity(context.getPackageManager()) == null)
            {
                if (listener != null) { throw new ActivityNotFoundException(); }
                AlfrescoNotificationManager.getInstance(context).showAlertCrouton(context,
                        context.getString(R.string.feature_disable));
                return;
            }
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            if (listener != null)
            {
                listener.onActivityNotFoundException(e);
            }
        }
    }

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
                dialog.show(fr.getActivity().getSupportFragmentManager(), WaitingDialogFragment.TAG);
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
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_file);
        }
    }

    public static Intent createViewIntent(FragmentActivity activity, File contentFile)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            data = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", contentFile);
        } else {
            data = Uri.fromFile(contentFile);
        }
        intent.setDataAndType(data,
                MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()).toLowerCase());
        return intent;
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
        intent.setData(Uri.parse("market://details?type=org.alfresco.mobile.android.application"));
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
    public static void actionSend(FragmentActivity activity, File myFile, ActionManagerListener listener)
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
                dialog.show(activity.getSupportFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), myFile,
                        DataProtectionManager.ACTION_SEND);
            }
            else
            {
                actionSend(activity, myFile, mimeType);
            }
        }
        catch (Exception e)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_open_file);
        }
    }

    public static void actionSendDocument(Fragment fr, File myFile)
    {
        actionSend(fr.getActivity(), myFile, (String) null);
    }

    public static void actionSend(FragmentActivity activity, File contentFile, String mimetype)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType((TextUtils.isEmpty(mimetype))
                    ? MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()) : mimetype);

            if (i.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                        activity.getString(R.string.feature_disable));
                return;
            }

            activity.startActivity(Intent.createChooser(i, activity.getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_share_content);
        }
    }

    public static Intent createSendIntent(FragmentActivity activity, File contentFile)
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

            if (i.resolveActivity(fr.getActivity().getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                        fr.getString(R.string.feature_disable));
                return false;
            }

            fr.startActivityForResult(Intent.createChooser(i, fr.getString(R.string.send_email)), requestCode);

            return true;
        }
        catch (Exception e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.decryption_failed);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    public static void actionSendDocumentToAlfresco(FragmentActivity activity, File file)
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
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_open_file);
        }
    }

    public static void actionSendFileToAlfresco(FragmentActivity activity, File contentFile)
    {
        try
        {
            Intent i = createSendFileToAlfrescoIntent(activity, contentFile);
            if (i.resolveActivity(activity.getPackageManager()) == null)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                        activity.getString(R.string.feature_disable));
            }
            else
            {
                activity.startActivity(i);
            }
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_share_content);
        }
    }

    public static Intent createSendFileToAlfrescoIntent(FragmentActivity activity, File contentFile)
    {
        Intent i = new Intent(activity, PublicDispatcherActivity.class);
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
        i.setType(MimeTypeManager.getInstance(activity).getMIMEType(contentFile.getName()));
        return i;
    }

    public static void actionShareContent(FragmentActivity activity, File myFile)
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
                actionSend(activity, myFile, (String) null);
            }
        }
        catch (Exception e)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION SEND MULTIPLE
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionSendDocumentsToAlfresco(Fragment fr, List<File> files)
    {
        actionSendDocumentsToAlfresco(fr.getActivity(), files);
    }

    public static void actionSendDocumentsToAlfresco(FragmentActivity activity, List<File> files)
    {
        if (files.size() == 1)
        {
            actionSendDocumentToAlfresco(activity, files.get(0));
            return;
        }

        try
        {
            Intent i = new Intent(activity, PublicDispatcherActivity.class);
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<>();
            // convert from paths to Android friendly Parcelable Uri's
            for (File file : files)
            {
                Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            i.setType(MimeTypeManager.getInstance(activity).getMIMEType("text/plain"));
            activity.startActivity(i);
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity,
                    R.string.error_unable_share_content);
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
            ArrayList<Uri> uris = new ArrayList<>();
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
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_share_content);
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
    public static void actionPickFile(Fragment fr, int requestCode)
    {
        if (fr == null) { return; }
        try
        {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            fr.startActivityForResult(Intent.createChooser(i, fr.getText(R.string.content_app_pick_file)), requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_unable_open_file);
        }
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
            return intent.resolveActivity(context.getPackageManager()) != null;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean hasCameraAvailable(Context context)
    {
        try
        {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static void startPlayStore(Context context, String appPackage)
    {
        final String appPackageName = appPackage;
        try
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?type=" + appPackageName)));
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?type=" + appPackageName)));
        }
    }

    public static void startWebBrowser(Context context, String url)
    {
        final String webUrl = url;
        try
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            // Display error ?
        }
    }

    public static boolean actionSendFeedbackEmail(Fragment fr)
    {
        try
        {
            ShareCompat.IntentBuilder iBuilder = ShareCompat.IntentBuilder.from(fr.getActivity());
            Context context = fr.getContext();
            // Email
            iBuilder.addEmailTo(context.getResources()
                    .getStringArray(org.alfresco.mobile.android.foundation.R.array.bugreport_email));

            // Prepare Subject
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;

            String subject = "Alfresco Android Mobile Feedback";
            iBuilder.setSubject(subject);

            // Content
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            String densityBucket = getDensityString(dm);

            Map<String, String> info = new LinkedHashMap<>();
            info.put("Version", versionName);
            info.put("Version code", Integer.toString(versionCode));
            info.put("Make", Build.MANUFACTURER);
            info.put("Model", Build.MODEL);
            info.put("Resolution", dm.heightPixels + "x" + dm.widthPixels);
            info.put("Density", dm.densityDpi + "dpi (" + densityBucket + ")");
            info.put("Release", Build.VERSION.RELEASE);
            info.put("API", String.valueOf(Build.VERSION.SDK_INT));
            info.put("Language", context.getResources().getConfiguration().locale.getDisplayLanguage());

            StringBuilder builder = new StringBuilder();
            builder.append("\n\n\n\n");
            builder.append("Alfresco Mobile and device details\n");
            builder.append("-------------------\n").toString();
            for (Map.Entry entry : info.entrySet())
            {
                builder.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }

            builder.append("-------------------\n\n").toString();
            iBuilder.setType("message/rfc822");
            iBuilder.setText(builder.toString());
            iBuilder.setChooserTitle(fr.getString(R.string.settings_feedback_email)).startChooser();

            return true;
        }
        catch (Exception e)
        {
            AlfrescoNotificationManager.getInstance(fr.getActivity()).showAlertCrouton(fr.getActivity(),
                    R.string.error_general);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    private static String getDensityString(DisplayMetrics displayMetrics)
    {
        switch (displayMetrics.densityDpi)
        {
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxxhdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            default:
                return "unknown";
        }
    }

}

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
import java.io.IOException;
import java.net.URI;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public final class EmailUtils
{
    private EmailUtils()
    {
    }

    public static final String TAG = "EmailUtils";

    public static boolean createMailWithAttachment(Fragment fr, String subject, String content, Uri attachment,
            int requestCode)
    {
        try
        {
            // If it comes from tmp folder we move it to download folder with timestamping.
            File tmpFile = new File(new URI(attachment.toString()));
            File f = new File(new URI(attachment.toString()));
            if (StorageManager.isTempFile(fr.getActivity(), f))
            {
                tmpFile = IOUtils.renameTimeStampFile(
                        StorageManager.getDownloadFolder(fr.getActivity(), SessionUtils.getAccount(fr.getActivity())
                                .getUrl(), SessionUtils.getAccount(fr.getActivity()).getUsername()), f);
                
                if (CipherUtils.isEncryptionActive(fr.getActivity())){
                    PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit()
                    .putString(GeneralPreferences.REQUIRES_ENCRYPT, tmpFile.getPath()).commit();
                }
            }
            
            if (CipherUtils.isEncrypted(fr.getActivity(), tmpFile.getPath(), true)
                    && CipherUtils.decryptFile(fr.getActivity(), tmpFile.getPath()))
            {
                PreferenceManager.getDefaultSharedPreferences(fr.getActivity()).edit()
                        .putString(GeneralPreferences.REQUIRES_ENCRYPT, tmpFile.getPath()).commit();
            }

            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content));
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile));
            i.setType("text/plain");
            fr.getActivity().startActivityForResult(Intent.createChooser(i, fr.getString(R.string.send_email)),
                    requestCode);

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
}

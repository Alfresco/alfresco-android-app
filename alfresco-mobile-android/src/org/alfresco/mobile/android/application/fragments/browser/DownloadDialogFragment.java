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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.util.Date;

import org.alfresco.mobile.android.api.asynchronous.DownloadTask;
import org.alfresco.mobile.android.api.asynchronous.DownloadTask.DownloadTaskListener;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.EmailUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadDialogFragment extends DialogFragment implements DownloadTaskListener
{
    public static final String ARGUMENT_DOCUMENT = "document";

    public static final String ARGUMENT_ACTION = "action";

    public static final int ACTION_OPEN = 1;

    public static final int ACTION_EMAIL = 2;

    public static final int ACTION_UNDEFINED = 0;

    public static final String TAG = "DownloadDialogFragment";

    public static final String ARGUMENT_TEMPFILE = "TempFile";

    private Dialog dialog;

    private Document doc;

    private long totalSize;

    private DownloadTask dlt;

    private ContentFile contentFile;

    private int action = ACTION_UNDEFINED;

    public static DownloadDialogFragment newInstance()
    {
        return new DownloadDialogFragment();
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (contentFile != null)
        {
            executeAction();
        }

        if (getArguments().containsKey(ARGUMENT_DOCUMENT))
        {
            doc = getArguments().getParcelable(ARGUMENT_DOCUMENT);
        }
        else
        {
            return null;
        }

        if (getArguments().containsKey(ARGUMENT_ACTION))
        {
            action = getArguments().getInt(ARGUMENT_ACTION);
        }

        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle(getString(R.string.download) + " : " + doc.getName());
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMessage(getString(R.string.download_progress));
        progress.setIndeterminate(false);
        progress.setCancelable(true);
        progress.setProgress(0);
        progress.setMax(100);
        dialog = progress;

        if (dlt == null)
        {
            File dlFile = null;
            if (getArguments().containsKey(ARGUMENT_TEMPFILE))
            {
                dlFile = new File (getArguments().getString(ARGUMENT_TEMPFILE));
            }
            else
            {
                dlFile = getDownloadFile();
            }

            if (dlFile != null)
            {
                totalSize = doc.getContentStreamLength();
                dlt = new DownloadTask(SessionUtils.getSession(getActivity()), doc, dlFile);
                dlt.setDl(this);
                dlt.execute();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.sdinaccessible);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                    }
                });
                dialog = builder.create();
            }
        }
        else
        {
            dialog.show();
        }

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            try
            {
                String filename = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                        GeneralPreferences.REQUIRES_ENCRYPT, "");
                if (filename != null && filename.length() > 0)
                {
                    if (!CipherUtils.encryptFile(getActivity(), filename, true))
                    {
                        MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                    }
                    else
                    {
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                .putString(GeneralPreferences.REQUIRES_ENCRYPT, "").commit();
                    }
                }
            }
            catch (Exception e)
            {
                MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                Log.d(TAG, Log.getStackTraceString(e));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private File getDownloadFile()
    {
        if (SessionUtils.getAccount(getActivity()) == null) { return null; }
        File tmpFile = NodeActions.getDownloadFile(getActivity(), doc);
        if (tmpFile != null)
        {
            org.alfresco.mobile.android.api.utils.IOUtils.ensureOrCreatePathAndFile(tmpFile);
        }

        return tmpFile;
    }

    @Override
    public void onPreExecute()
    {
    }

    @Override
    public void onProgressUpdate(Integer... values)
    {
        int percent = Math.round(((float) values[0] / totalSize) * 100);
        ((ProgressDialog) dialog).setProgress(percent);
    }

    @Override
    public void onPostExecute(ContentFile results)
    {
        contentFile = results;
        if (getActivity() != null)
        {
            executeAction();
        }
    }

    private void executeAction()
    {
        if (contentFile != null && contentFile.getFile() != null)
        {
            switch (action)
            {
                case ACTION_OPEN:
                    Log.d(TAG, getActivity().toString());
                    Log.d(TAG, contentFile + "");

                    MessengerManager.showToast(getActivity(), getActivity().getText(R.string.download_complete)
                            + " " + IOUtils.getOriginalFromTempFilename(contentFile.getFileName()));

                    DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentByTag(
                            DetailsFragment.TAG);
                    if (detailsFragment != null)
                    {
                        long datetime = contentFile.getFile().lastModified();
                        detailsFragment.setDownloadDateTime(new Date(datetime));
                        ActionManager.openIn(detailsFragment, contentFile.getFile(), doc.getContentStreamMimeType(),
                                PublicIntent.REQUESTCODE_SAVE_BACK);
                    }
                    break;

                case ACTION_EMAIL:
                    EmailUtils.createMailWithAttachment(this, contentFile.getFileName(), getFragmentManager()
                            .findFragmentByTag(DetailsFragment.TAG).getActivity().getString(R.string.email_content),
                            Uri.fromFile(contentFile.getFile()), PublicIntent.REQUESTCODE_DECRYPTED);
                    break;

                case ACTION_UNDEFINED:
                    break;
            }
        }
        else
        {
            MessengerManager.showToast(getActivity(), getActivity().getText(R.string.download_error).toString());
        }
        dismiss();
    }

    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance())
        {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        if (dlt != null)
        {
            dlt.cancel(false);
        }
    }
}

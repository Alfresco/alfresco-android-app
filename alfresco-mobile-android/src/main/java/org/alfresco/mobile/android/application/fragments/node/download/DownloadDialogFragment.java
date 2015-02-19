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
package org.alfresco.mobile.android.application.fragments.node.download;

import java.io.File;
import java.util.Date;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.actions.NodeActions;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.intent.RequestCode;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.node.download.DownloadTask;
import org.alfresco.mobile.android.async.node.download.DownloadTask.DownloadTaskListener;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

public class DownloadDialogFragment extends DialogFragment implements DownloadTaskListener
{
    public static final String ARGUMENT_DOCUMENT = "document";

    public static final String ARGUMENT_ACTION = "action";

    public static final int ACTION_OPEN = 1;

    public static final int ACTION_EMAIL = 2;

    public static final int ACTION_EDIT = 3;

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
            File dlFile = getDownloadFile();
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

    private File getDownloadFile()
    {
        if (SessionUtils.getAccount(getActivity()) == null) { return null; }
        File tmpFile = NodeActions.getTempFile(getActivity(), doc);
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
                    AlfrescoNotificationManager.getInstance(getActivity()).showToast(
                            getActivity().getText(R.string.download_complete) + " " + contentFile.getFileName());

                    NodeDetailsFragment detailsFragment = (NodeDetailsFragment) getFragmentManager().findFragmentByTag(
                            NodeDetailsFragment.getDetailsTag());
                    if (detailsFragment != null)
                    {
                        long datetime = contentFile.getFile().lastModified();
                        detailsFragment.setDownloadDateTime(new Date(datetime));
                        ActionUtils.openIn(detailsFragment, contentFile.getFile(), doc.getContentStreamMimeType(),
                                RequestCode.SAVE_BACK);
                    }
                    break;

                case ACTION_EMAIL:
                    ActionUtils.actionSendMailWithAttachment(this, contentFile.getFileName(),
                            getString(R.string.email_content), Uri.fromFile(contentFile.getFile()),
                            RequestCode.DECRYPTED);
                    break;

                case ACTION_UNDEFINED:
                    break;
            }
        }
        else
        {
            AlfrescoNotificationManager.getInstance(getActivity()).showToast(
                    getActivity().getText(R.string.download_error).toString());
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

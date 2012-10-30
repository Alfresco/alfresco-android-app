/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.browser;

import org.alfresco.mobile.android.api.asynchronous.DownloadTask.DownloadTaskListener;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.application.utils.EmailUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;

public class DownloadTaskCallback implements DownloadTaskListener
{
    public enum DownloadAction
    {
        ACTION_OPEN, ACTION_EMAIL, ACTION_UNDEFINED
    };

    private Fragment fragment;

    private ProgressDialog progressDialog;

    private Document doc;

    private long totalSize;

    private DownloadAction action = DownloadAction.ACTION_UNDEFINED;

    public DownloadTaskCallback(Fragment fragment, Document doc, DownloadAction action)
    {
        this.action = action;
        this.fragment = fragment;
        this.doc = doc;
        totalSize = doc.getContentStreamLength();
    }

    @Override
    public void onPreExecute()
    {
        createProgressBar();
    }

    @Override
    public void onPostExecute(ContentFile results)
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }

        if (results != null && results.getFile() != null)
        {
            switch (action)
            {
                case ACTION_OPEN:
                    MessengerManager.showToast(fragment.getActivity(),
                            fragment.getActivity().getText(R.string.download_complete) + results.getFileName());
                    ActionManager.openIn(fragment, results.getFile(), doc.getContentStreamMimeType(),
                            PublicIntent.REQUESTCODE_SAVE_BACK);
                    break;

                case ACTION_EMAIL:
                    EmailUtils.createMailWithAttachment(fragment.getActivity(), results.getFileName(), fragment
                            .getActivity().getString(R.string.email_content), Uri.fromFile(results.getFile()));
                    break;

                case ACTION_UNDEFINED:
                    break;
            }

        }
        else
        {
            MessengerManager.showToast(fragment.getActivity(),
                    fragment.getActivity().getText(R.string.download_complete_error).toString());
        }
    }

    @Override
    public void onProgressUpdate(Integer... values)
    {
        int percent = Math.round(((float) values[0] / totalSize) * 100);

        if (progressDialog == null)
        {
            createProgressBar();
        }
        else
        {
            progressDialog.setProgress(percent);
        }
    }

    private void createProgressBar(){
        progressDialog = new ProgressDialog(fragment.getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(fragment.getActivity().getText(R.string.download));
        progressDialog.setOnCancelListener(new OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                dialog.dismiss();
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.setTitle(fragment.getActivity().getText(R.string.download) + " : " + doc.getName());
        progressDialog.setMessage(fragment.getActivity().getText(R.string.download_progress));
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
    }
    
    public ProgressDialog getProgressDialog (){
        return progressDialog;
    }
}

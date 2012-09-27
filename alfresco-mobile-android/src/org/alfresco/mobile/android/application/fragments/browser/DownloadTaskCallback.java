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
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class DownloadTaskCallback implements DownloadTaskListener
{

    private Activity activity;

    private Fragment fragment;

    private ProgressDialog progressDialog;

    private Document doc;

    private long totalSize;

    public DownloadTaskCallback(Fragment fragment, Document doc)
    {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        this.doc = doc;
        totalSize = doc.getContentStreamLength();
    }

    @Override
    public void onPreExecute()
    {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(this.activity.getText(R.string.download));
        progressDialog.setOnCancelListener(new OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                dialog.dismiss();
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.setTitle(this.activity.getText(R.string.download) + " : " + doc.getName());
        progressDialog.setMessage(this.activity.getText(R.string.download_progress));
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
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
            MessengerManager.showToast(activity, activity.getText(R.string.download_complete) + results.getFileName());
            ActionManager.openIn(fragment, results.getFile(), doc.getContentStreamMimeType(),
                    PublicIntent.REQUESTCODE_SAVE_BACK);
        }
        else
        {
            MessengerManager.showToast(activity, activity.getText(R.string.download_complete_error).toString());
        }
    }

    @Override
    public void onProgressUpdate(Integer... values)
    {
        int percent = Math.round(((float) values[0] / totalSize) * 100);
        progressDialog.setProgress(percent);
    }

}

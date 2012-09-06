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
package org.alfresco.mobile.android.application.loaders;

import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.DocumentCreateLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;

public class NodeLoaderCallback implements LoaderCallbacks<LoaderResult<Node>>
{

    private Activity activity;

    private AlfrescoSession session;
    
    private List<Account> accounts;

    private String url;

    private ProgressDialog mProgressDialog;


    public NodeLoaderCallback(Activity activity, List<Account> accounts, String url)
    {
        this.activity = activity;
        this.accounts = accounts;
        this.url = url;
    }
    
    public NodeLoaderCallback(Activity activity, AlfrescoSession session, String url)
    {
        this.activity = activity;
        this.session = session;
        this.url = url;
    }

    @Override
    public Loader<LoaderResult<Node>> onCreateLoader(final int id, Bundle args)
    {
        
        mProgressDialog = ProgressDialog.show(activity, "Please wait", "Searching & Loading information...", true,
                true, new OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        activity.getLoaderManager().destroyLoader(NodeLoader.ID);
                        dialog.dismiss();
                    }
                });
        
        if (session != null)
            return new NodeLoader(activity, session, url);
        else
            return new NodeLoader(activity, accounts, url);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Node>> loader, LoaderResult<Node> results)
    {
        mProgressDialog.dismiss();
        if (!results.hasException() && results.getData() != null){
            SessionUtils.setsession(activity, ((NodeLoader)loader).getSession());
            SessionUtils.setAccount(activity, ((NodeLoader)loader).getAccount());
            ((MainActivity) activity).setCurrentNode(results.getData());
            Intent i = new Intent(activity, MainActivity.class);
            i.setAction(IntentIntegrator.ACTION_DISPLAY_NODE);
            activity.startActivity(i);
        } else {
            AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("Unable to open this url.")
                    .setMessage(results.getException().getMessage()).setCancelable(false)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Node>> arg0)
    {
        mProgressDialog.dismiss();
    }
}

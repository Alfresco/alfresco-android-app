/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.account;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LoadSessionCallBack extends AbstractBatchOperationCallback<AlfrescoSession>
{
    private static final String TAG = LoadSessionCallBack.class.getName();

    public LoadSessionCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.wait_message);
        complete = getBaseContext().getString(R.string.session_loaded);
    }

    @Override
    public void onPostExecute(Operation<AlfrescoSession> task, AlfrescoSession results)
    {
        saveData(task, results);
        super.onPostExecute(task, results);
    }

    @Override
    public void onError(Operation<AlfrescoSession> task, Exception e)
    {
        LoadSessionThread loadingTask = ((LoadSessionThread) task);
        Log.e(TAG, Log.getStackTraceString(e));

        switch (loadingTask.getAccount().getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                saveData(task, null);
                CloudExceptionUtils.handleCloudException(context, loadingTask.getAccount().getId(), e, true);
                break;
            case Account.TYPE_ALFRESCO_TEST_BASIC:
            case Account.TYPE_ALFRESCO_CMIS:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
                broadcastIntent
                        .putExtra(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_MESSAGE,
                        SessionExceptionHelper.getMessageId(context, e));
                if (loadingTask.getAccount() != null)
                {
                    broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, loadingTask.getAccount().getId());
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                break;
            default:
                break;
        }
        super.onError(task, e);
    }

    private void saveData(Operation<AlfrescoSession> task, AlfrescoSession session)
    {
        LoadSessionThread loadingTask = ((LoadSessionThread) task);
        Account acc = loadingTask.getAccount();

        // Save Session for reuse purpose
        if (session != null)
        {
            ApplicationManager.getInstance(getBaseContext()).saveSession(acc, session);
        }

        // For cloud session, try to save the latest version of oauthdata
        if (loadingTask.getOAuthData() == null) return;

        switch (loadingTask.getAccount().getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                acc = AccountManager.update(context, acc.getId(), acc.getDescription(), acc.getUrl(),
                        acc.getUsername(), acc.getPassword(), acc.getRepositoryId(),
                        Integer.valueOf((int) acc.getTypeId()), null, loadingTask.getOAuthData().getAccessToken(),
                        loadingTask.getOAuthData().getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0);

                if (acc == null)
                {
                    Log.e(TAG, "Error during saving oauth data");
                }
                break;
            default:
                // Do nothing
                break;
        }
    }
}

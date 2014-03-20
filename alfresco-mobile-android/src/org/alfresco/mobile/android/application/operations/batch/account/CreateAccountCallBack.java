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

import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CreateAccountCallBack extends AbstractBatchOperationCallback<Account>
{
    private static final String TAG = CreateAccountCallBack.class.getName();

    public CreateAccountCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.account_verify);
        complete = getBaseContext().getString(R.string.account_wizard_alldone_description);
    }

    @Override
    public void onPostExecute(Operation<Account> task, Account account)
    {
        super.onPostExecute(task, account);

        CreateAccountThread createTask = ((CreateAccountThread) task);

        ApplicationManager.getInstance(context).saveSession(account, createTask.getSession());
        ApplicationManager.getInstance(context).saveAccount(account);
    }

    @Override
    public void onError(Operation<Account> task, Exception e)
    {
        Log.d(TAG, Log.getStackTraceString(e));

        Bundle b = new Bundle();
        b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
        b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_title);
        b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
        b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, SessionExceptionHelper.getMessageId(context, e));
        ActionManager.actionDisplayDialog(context, b);

        if (task instanceof CreateAccountThread && ((CreateAccountThread) task).getOauthData() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                    new Intent(IntentIntegrator.ACTION_CREATE_ACCOUNT_CLOUD_ERROR));
        }

        super.onError(task, e);
    }
}

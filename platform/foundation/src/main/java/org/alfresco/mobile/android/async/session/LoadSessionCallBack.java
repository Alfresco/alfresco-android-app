/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.session;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;

import android.content.Context;
import android.util.Log;

public class LoadSessionCallBack implements Operation.OperationCallback<AlfrescoSession>
{
    private static final String TAG = LoadSessionCallBack.class.getName();

    private Context context;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public LoadSessionCallBack(Context context)
    {
        this.context = context.getApplicationContext();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPreExecute(Operation<AlfrescoSession> task)
    {
        EventBusManager.getInstance()
                .post(new LoadAccountStartedEvent(task.getRequestId(), ((LoadSessionOperation) task).getAccount()));
    }

    @Override
    public void onPostExecute(Operation<AlfrescoSession> task, AlfrescoSession result)
    {
        saveData(task, result);

        EventBusManager.getInstance()
                .post(new LoadAccountCompletedEvent(task.getRequestId(), ((LoadSessionOperation) task).getAccount()));
    }

    @Override
    public void onError(Operation<AlfrescoSession> task, Exception e)
    {
        LoadSessionOperation loadingTask = ((LoadSessionOperation) task);
        Log.e(TAG, Log.getStackTraceString(e));

        switch (loadingTask.getAccount().getTypeId())
        {
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                saveData(task, null);
                AlfrescoAccount acc = loadingTask.getAccount();
                if (acc.getActivation() == null)
                {
                    // OAuth Error
                    CloudExceptionUtils.handleCloudException(context, loadingTask.getAccount().getId(), e, true,
                            task.getRequestId());
                }
                else
                {
                    // Cloud Account is not active
                    EventBusManager.getInstance().post(new LoadInactiveAccountEvent(task.getRequestId(),
                            ((LoadSessionOperation) task).getAccount()));
                }
                break;
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_BASIC:
            case AlfrescoAccount.TYPE_ALFRESCO_CMIS:

                // Neeed to define SAML specific

                EventBusManager.getInstance()
                        .post(new LoadAccountErrorEvent(task.getRequestId(), ((LoadSessionOperation) task).getAccount(),
                                e, AlfrescoExceptionHelper.getMessageId(context, e)));
                break;

            case AlfrescoAccount.TYPE_ALFRESCO_CMIS_SAML:

                EventBusManager.getInstance()
                        .post(new LoadAccountErrorEvent(task.getRequestId(), ((LoadSessionOperation) task).getAccount(),
                                e, AlfrescoExceptionHelper.getMessageId(context, e)));
                break;

            default:
                break;
        }
    }

    private void saveData(Operation<AlfrescoSession> task, AlfrescoSession session)
    {
        LoadSessionOperation loadingTask = ((LoadSessionOperation) task);
        AlfrescoAccount acc = loadingTask.getAccount();

        // Save Session for reuse purpose
        if (session != null)
        {
            SessionManager.getInstance(context).saveSession(acc, session);
        }

        AnalyticsHelper.checkServerConfiguration(context, session, acc);

        // For cloud session, try to save the latest version of oauthdata
        if (loadingTask.getOAuthData() == null) return;

        switch (loadingTask.getAccount().getTypeId())
        {
            case AlfrescoAccount.TYPE_ALFRESCO_TEST_OAUTH:
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                if (acc.getActivation() != null && session == null)
                {
                    // Do Nothing
                }
                else
                {
                    acc = AlfrescoAccountManager.getInstance(context).update(acc.getId(), acc.getTitle(), acc.getUrl(),
                            acc.getUsername(), acc.getPassword(), acc.getRepositoryId(), acc.getTypeId(), null,
                            loadingTask.getOAuthData().getAccessToken(), loadingTask.getOAuthData().getRefreshToken(),
                            acc.getIsPaidAccount() ? 1 : 0);
                }

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

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS RESPONSE
    // ///////////////////////////////////////////////////////////////////////////
    public static class LoadAccountCompletedEvent extends OperationEvent<AlfrescoAccount>
    {
        public static final String SWAP = "reloaded";

        public AlfrescoAccount account;

        public LoadAccountCompletedEvent(String requestId, AlfrescoAccount account)
        {
            super(requestId, account);
            this.account = account;
        }
    }

    public static class LoadAccountStartedEvent extends OperationEvent<AlfrescoAccount>
    {
        public AlfrescoAccount account;

        public LoadAccountStartedEvent(String requestId, AlfrescoAccount account)
        {
            super(requestId, account);
            this.account = account;
        }
    }

    public static class LoadInactiveAccountEvent extends OperationEvent<AlfrescoAccount>
    {
        public AlfrescoAccount account;

        public LoadInactiveAccountEvent(String requestId, AlfrescoAccount account)
        {
            super(requestId, account);
            this.account = account;
        }
    }

    public static class LoadAccountErrorEvent extends OperationEvent<Long>
    {
        public final AlfrescoAccount account;

        public final int messageId;

        public LoadAccountErrorEvent(String requestId, AlfrescoAccount account, Exception e, int messageId)
        {
            super(requestId, account.getId(), e);
            this.account = account;
            this.messageId = messageId;
        }

        public LoadAccountErrorEvent(String requestId, Long accountId, Exception e, int messageId)
        {
            super(requestId, accountId, e);
            this.account = null;
            this.messageId = messageId;
        }
    }

    public static class CloudAccountErrorEvent extends OperationEvent<Long>
    {
        public final AlfrescoAccount account;

        public final int messageId;

        public CloudAccountErrorEvent(String requestId, AlfrescoAccount account, Exception e, int messageId)
        {
            super(requestId, account.getId(), e);
            this.account = account;
            this.messageId = messageId;
        }

        public CloudAccountErrorEvent(String requestId, Long accountId, Exception e, int messageId)
        {
            super(requestId, accountId, e);
            this.account = null;
            this.messageId = messageId;
        }
    }

}

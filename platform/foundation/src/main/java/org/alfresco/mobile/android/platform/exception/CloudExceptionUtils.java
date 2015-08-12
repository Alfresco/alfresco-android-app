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
package org.alfresco.mobile.android.platform.exception;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack.LoadAccountErrorEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.http.HttpStatus;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

public final class CloudExceptionUtils
{

    private static final String TAG = CloudExceptionUtils.class.getName();

    private CloudExceptionUtils()
    {
    }

    public static void handleCloudException(Context context, Long accountId, Exception exception, boolean forceRefresh,
            String taskId)
    {
        Log.w(TAG, Log.getStackTraceString(exception));
        if (exception instanceof AlfrescoSessionException)
        {
            AlfrescoSessionException ex = ((AlfrescoSessionException) exception);
            switch (ex.getErrorCode())
            {
                case ErrorCodeRegistry.SESSION_API_KEYS_INVALID:
                case ErrorCodeRegistry.SESSION_REFRESH_TOKEN_EXPIRED:
                    requestOAuthAuthentication(context, accountId, taskId, forceRefresh);
                    return;
                default:
                    if (ex.getMessage().contains("No authentication challenges found") || ex.getErrorCode() == 100)
                    {
                        requestOAuthAuthentication(context, accountId, taskId, forceRefresh);
                        return;
                    }
                    break;
            }
        }

        if (exception instanceof AlfrescoServiceException)
        {
            AlfrescoServiceException ex = ((AlfrescoServiceException) exception);
            if ((ex.getErrorCode() == 104 || (ex.getMessage() != null && ex.getMessage().contains(
                    "No authentication challenges found"))))
            {
                requestOAuthAuthentication(context, accountId, taskId, forceRefresh);
                return;
            }
            else
            {
                new MaterialDialog.Builder(context).iconRes(R.drawable.ic_application_logo)
                        .title(R.string.error_general_title)
                        .content(Html
                                .fromHtml(context.getString(AlfrescoExceptionHelper.getMessageId(context, exception))))
                        .positiveText(android.R.string.ok).show();
                return;
            }
        }

        if (exception instanceof CmisConnectionException)
        {
            CmisConnectionException ex = ((CmisConnectionException) exception);
            if (ex.getMessage().contains("No authentication challenges found"))
            {
                requestOAuthAuthentication(context, accountId, taskId, forceRefresh);
                return;
            }
        }

        if (exception instanceof AlfrescoSessionException)
        {
            int messageId = R.string.error_session_notfound;
            AlfrescoSessionException se = ((AlfrescoSessionException) exception);
            if (se.getErrorCode() == ErrorCodeRegistry.GENERAL_HTTP_RESP && se.getMessage() != null
                    && se.getMessage().contains(HttpStatus.SC_SERVICE_UNAVAILABLE + ""))
            {
                messageId = R.string.error_session_cloud_unavailable;
            }

            EventBusManager.getInstance().post(new LoadAccountErrorEvent(null, accountId, exception, messageId));
        }
    }

    public static void handleCloudException(FragmentActivity activity, Exception exception, boolean forceRefresh)
    {
        Long accountId = null;
        handleCloudException(activity, accountId, exception, forceRefresh, "");
    }

    private static void requestOAuthAuthentication(Context context, long accountId, String taskId, boolean forceRefresh)
    {
        if (forceRefresh)
        {
            EventBusManager.getInstance().post(
                    new LoadSessionCallBack.CloudAccountErrorEvent(taskId, AlfrescoAccountManager.getInstance(context)
                            .retrieveAccount(accountId), null, 0));
        }
        else
        {
            Operator.with(context).load(
                    new RetrieveOAuthDataRequest.Builder((CloudSession) SessionUtils.getSession(context)));
        }
    }
}

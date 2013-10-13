/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.exception;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class CloudExceptionUtils
{

    private static final String TAG = CloudExceptionUtils.class.getName();

    private CloudExceptionUtils()
    {
    };

    public static void handleCloudException(Context context, Long accountId, Exception exception, boolean forceRefresh)
    {
        Log.w(TAG, Log.getStackTraceString(exception));
        if (exception instanceof AlfrescoSessionException)
        {
            AlfrescoSessionException ex = ((AlfrescoSessionException) exception);
            switch (ex.getErrorCode())
            {
                case ErrorCodeRegistry.SESSION_API_KEYS_INVALID:
                case ErrorCodeRegistry.SESSION_REFRESH_TOKEN_EXPIRED:
                    manageException(context, forceRefresh);
                    return;
                default:
                    if (ex.getMessage().contains("No authentication challenges found") || ex.getErrorCode() == 100)
                    {
                        manageException(context, forceRefresh);
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
                manageException(context, forceRefresh);
                return;
            }
            else
            {
                Bundle b = new Bundle();
                b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
                b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_general_title);
                b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE,
                        SessionExceptionHelper.getMessageId(context, exception));
                ActionManager.actionDisplayDialog(context, b);
                return;
            }
        }

        if (exception instanceof CmisConnectionException)
        {
            CmisConnectionException ex = ((CmisConnectionException) exception);
            if (ex.getMessage().contains("No authentication challenges found"))
            {
                manageException(context, forceRefresh);
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

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_MESSAGE, messageId);
            if (accountId != null)
            {
                broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
            return;
        }
    }

    public static void handleCloudException(Activity activity, Exception exception, boolean forceRefresh)
    {
        Long accountId = null;
        if (activity instanceof BaseActivity)
        {
            accountId = ((BaseActivity) activity).getCurrentAccount().getId();
        }
        handleCloudException(activity, accountId, exception, forceRefresh);
    }

    private static void manageException(Context activity, boolean forceRefresh)
    {
        if (forceRefresh)
        {
            // MessengerManager.showLongToast(activity, (String)
            // activity.getText(R.string.error_session_expired));
            ActionManager.actionRequestUserAuthentication(activity, SessionUtils.getAccount(activity));
        }
        else
        {
            // MessengerManager.showLongToast(activity, (String)
            // activity.getText(R.string.error_session_refresh));
            ActionManager.actionRequestAuthentication(activity, SessionUtils.getAccount(activity));
        }
    }

}

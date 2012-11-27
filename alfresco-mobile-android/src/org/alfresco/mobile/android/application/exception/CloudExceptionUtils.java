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
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;

import android.app.Activity;
import android.util.Log;

public class CloudExceptionUtils
{

    public static void handleCloudException(Activity activity, Exception exception, boolean forceRefresh)
    {
        Log.d("CloudExceptionUtils", Log.getStackTraceString(exception));
        if (exception instanceof AlfrescoSessionException)
        {
            // Case CmisConnexionException ==> Token expired
            AlfrescoSessionException ex = ((AlfrescoSessionException) exception);
            if (ex.getMessage().contains("No authentication challenges found") || ex.getErrorCode() == 100)
            {
                manageException(activity, forceRefresh);
            }
        }

        if (exception instanceof AlfrescoServiceException)
        {
            AlfrescoServiceException ex = ((AlfrescoServiceException) exception);
            if (ex != null
                    && (ex.getErrorCode() == 104 || (ex.getMessage() != null && ex.getMessage().contains(
                            "No authentication challenges found"))))
            {
                manageException(activity, forceRefresh);
            }
        }

        if (exception instanceof CmisConnectionException)
        {
            CmisConnectionException ex = ((CmisConnectionException) exception);
            if (ex.getMessage().contains("No authentication challenges found"))
            {
                manageException(activity, forceRefresh);
            }
        }
    }

    private static void manageException(Activity activity, boolean forceRefresh)
    {
        if (forceRefresh)
        {
            MessengerManager.showLongToast(activity, (String) activity.getText(R.string.error_session_expired));
            ActionManager.actionRequestUserAuthentication(activity, SessionUtils.getAccount(activity));
        }
        else
        {
            MessengerManager.showLongToast(activity, (String) activity.getText(R.string.error_session_refresh));
            ActionManager.actionRequestAuthentication(activity, SessionUtils.getAccount(activity));
        }
    }

}

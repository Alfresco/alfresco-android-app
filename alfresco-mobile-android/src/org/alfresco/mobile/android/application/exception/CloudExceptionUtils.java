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
            if (ex != null && (ex.getErrorCode() == 104 || (ex.getMessage() != null && ex.getMessage().contains("No authentication challenges found"))))
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

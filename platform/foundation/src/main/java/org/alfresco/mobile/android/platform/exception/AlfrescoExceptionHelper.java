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
package org.alfresco.mobile.android.platform.exception;

import java.net.UnknownHostException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import javax.net.ssl.SSLHandshakeException;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

import com.afollestad.materialdialogs.MaterialDialog;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Helper class to find the right user message to display when an exception has
 * occured.
 * 
 * @author Jean Marie Pascal
 */
public final class AlfrescoExceptionHelper
{

    private AlfrescoExceptionHelper()
    {
    }

    public static int getMessageErrorId(Context context, Exception e, boolean longMessage)
    {
        int messageId = R.string.error_session_creation;

        try
        {
            if (e.getCause() != null)
            {
                throw e.getCause();
            }
            else
            {
                throw e;
            }
        }
        catch (UnknownHostException ue)
        {
            if (ConnectivityUtils.hasInternetAvailable(context))
            {
                messageId = longMessage ? R.string.error_session_hostname_short : R.string.error_session_hostname;
            }
            else
            {
                messageId = longMessage ? R.string.error_session_nodata_short : R.string.error_session_nodata;
            }
        }
        catch (Throwable er)
        {
            messageId = R.string.error_unknown;
        }

        return messageId;
    }

    /**
     * Return user friendly message Id for a specific exception.
     * 
     * @param context :
     * @param e : exception occured
     * @return message Id
     */
    public static int getMessageId(Context context, Exception e)
    {
        int messageId = R.string.error_session_creation;

        if (e instanceof CmisConnectionException)
        {
            messageId = R.string.error_session_nodata;
        }
        else if (e instanceof CmisUnauthorizedException)
        {
            messageId = R.string.error_session_unauthorized;
        }
        // SAML Token not provided as Saml Disabled or enabled.
        else if (e instanceof IllegalArgumentException)
        {
            if (e.getMessage().contains("saml token"))
            {
                messageId = R.string.error_session_unauthorized;
            }
        }
        // USername error during session creation
        else if (e.getCause() instanceof AlfrescoSessionException)
        {
            if (e.getCause().getCause() instanceof CmisUnauthorizedException)
            {
                messageId = R.string.error_session_unauthorized;
            }
            else if (e.getCause().getCause() instanceof CmisObjectNotFoundException)
            {
                messageId = R.string.error_session_unauthorized;
            }
        }
        // Case where the user has no right (server configuration or wrong
        // username/password)
        else if (e.getCause() instanceof CmisUnauthorizedException)
        {
            messageId = R.string.error_session_unauthorized;
        }
        // Case where the ALL url seems to be wrong.
        else if (e.getCause() instanceof CmisObjectNotFoundException)
        {
            messageId = R.string.error_session_service_url;
        }
        // Case where the port seems to be wrong.
        else if (e.getCause() instanceof CmisRuntimeException
                && e.getCause().getMessage().contains("Service Temporarily Unavailable"))
        {
            messageId = R.string.error_session_service_unavailable;
        }
        // Case where the port seems to be wrong.
        else if (e.getCause() instanceof CmisRuntimeException && e.getCause().getMessage().contains("Found"))
        {
            messageId = R.string.error_session_port;
        }
        // Case where the hostname is wrong or no data connection.
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof UnknownHostException)
        {
            if (ConnectivityUtils.hasInternetAvailable(context))
            {
                messageId = R.string.error_session_hostname;
            }
            else
            {
                messageId = R.string.error_session_nodata;
            }
        }
        // Case where missing certificate / untrusted certificate
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof SSLHandshakeException
                && (e.getCause().getCause().getCause() instanceof CertPathValidatorException
                        || e.getCause().getCause().getCause() instanceof CertificateException)
                && e.getCause().getCause().getCause().getMessage()
                        .contains("Trust anchor for certification path not found."))
        {
            messageId = R.string.error_session_certificate;
        }
        // Case where the certificate has expired or is not yet valid.
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof SSLHandshakeException
                && e.getCause().getCause().getCause() instanceof CertificateException && e.getCause().getCause()
                        .getCause().getMessage().contains("Could not validate certificate: current time:"))
        {
            messageId = R.string.error_session_certificate_expired;
        }
        // Case where the certificate has expired or is not yet valid.
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof SSLHandshakeException
                && (e.getCause().getCause().getCause() instanceof CertificateExpiredException
                        || e.getCause().getCause().getCause() instanceof CertificateNotYetValidException))
        {
            messageId = R.string.error_session_certificate_expired;
        }
        // Generic Certificate error
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof SSLHandshakeException
                && e.getCause().getCause().getCause() instanceof CertificateException)
        {
            messageId = R.string.error_session_certificate;
        }
        // Generic SSL error
        else if (e.getCause() instanceof CmisConnectionException
                && e.getCause().getCause() instanceof SSLHandshakeException)
        {
            messageId = R.string.error_session_ssl;
        }
        // Case where the service url seems to be wrong.
        else if (e.getCause() instanceof CmisConnectionException && e.getCause().getMessage().contains("Cannot access"))
        {
            messageId = R.string.error_session_notfound;
        }
        else if (e.getCause() instanceof CmisConnectionException)
        {
            messageId = R.string.error_session_notfound;
        }
        else if (e instanceof AlfrescoServiceException && e.getMessage() != null
                && e.getMessage().contains("API plan limit exceeded"))
        {
            messageId = R.string.error_general;
        }
        else if (e instanceof NetworkErrorException)
        {
            messageId = R.string.error_session_nodata;
        }
        else if (e.getCause() instanceof CmisContentAlreadyExistsException)
        {
            messageId = R.string.error_name_already_exist;
        }
        else
        // Default case. We don't know what's wrong...
        {
            if (context instanceof FragmentActivity)
            {
                AlfrescoNotificationManager.getInstance(context).showAlertCrouton((FragmentActivity) context,
                        String.format(context.getString(R.string.error_unknown_exception), e.getCause()));
                messageId = R.string.error_unknown;
            }
            else
            {
                messageId = R.string.error_unknown;
            }
        }

        return messageId;
    }

    public static boolean checkEventException(FragmentActivity activity, OperationEvent event)
    {
        if (event.hasException)
        {
            int messageId = getMessageId(activity, event.exception);
            if (messageId != -1)
            {
                AlfrescoNotificationManager.getInstance(activity).showAlertCrouton(activity, messageId);
            }
            Log.w("[ERROR]", Log.getStackTraceString(event.exception));
            return true;
        }
        return false;
    }

    public static void displayErrorStackTrace(Activity activity, Exception e)
    {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity).title("Error Informations")
                .cancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        dialog.dismiss();
                    }
                }).content(Log.getStackTraceString(e)).negativeText(R.string.share).positiveText(R.string.cancel);
        builder.show();
        return;
    }

}

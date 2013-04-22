package org.alfresco.mobile.android.application.integration;

import java.io.Serializable;

public interface OperationRequest extends Serializable
{
    /** Display nothing. */
    int VISIBILITY_HIDDEN = 1;
    
    /** Notification status + inline. */
    int VISIBILITY_NOTIFICATIONS = 2;

    /** Display a central modal dialog. */
    int VISIBILITY_DIALOG = 4;

    /** Display a simple toast notification. */
    int VISIBILITY_TOAST = 8;
    
    OperationRequest setAccountId(long accountIdentifier);

    OperationRequest setNetworkId(String networkId);
    
    OperationRequest setNotificationVisibility(int visibility);
    
    OperationRequest setNotificationTitle(String title);

    OperationRequest setNotificationDescription(String description);
    
    OperationRequest setMimeType(String mimeType);
    
    
    String getRequestIdentifier();
    
    int getNotificationVisibility();
    
    String getNotificationTitle();
    
    String getNotificationDescription();
    
    String getMimeType();
}

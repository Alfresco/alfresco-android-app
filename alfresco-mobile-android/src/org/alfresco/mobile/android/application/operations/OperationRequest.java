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
package org.alfresco.mobile.android.application.operations;

import java.io.Serializable;

import android.net.Uri;

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
    
    Uri getNotificationUri();
}

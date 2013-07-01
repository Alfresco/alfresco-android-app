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
package org.alfresco.mobile.android.application.exception;

import org.alfresco.mobile.android.api.exceptions.AlfrescoException;

/**
 * High Level Exception that occurs during Application context.
 * 
 * @author Jean Marie Pascal
 */
public class AlfrescoAppException extends AlfrescoException
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private boolean displayMessage = false;

    /**
     * Default constructor.
     * 
     * @param detailsMessage the details message of the exception
     */
    public AlfrescoAppException(String detailsMessage)
    {
        super(detailsMessage);
    }

    /**
     * Instantiates a new Alfresco Application exception.
     * 
     * @param message Exception message
     * @param errorContent the error content (raw value from the server)
     */
    public AlfrescoAppException(String message, boolean displayMessage)
    {
        super(message);
        this.displayMessage = displayMessage;
    }

    /**
     * Instantiates a new alfresco service exception.
     * 
     * @param errorCode the error code
     * @param e Exception encapsulate by this new exception
     */
    public AlfrescoAppException(int errorCode, Throwable e)
    {
        super(errorCode, e);
    }
    
    public boolean isDisplayMessage()
    {
        return displayMessage;
    }
}

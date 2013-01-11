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

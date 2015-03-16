package org.alfresco.mobile.android.platform.mdm;

/**
 * Created by jpascal on 11/02/2015.
 */
public class MDMEvent
{

    public final Exception exception;

    public MDMEvent()
    {
        this.exception = null;
    }

    public MDMEvent(Exception exception)
    {
        this.exception = exception;
    }

}

package org.alfresco.mobile.android.application.intent;

/**
 * @since 1.5
 * @author Jean Marie Pascal
 */
public interface AlfrescoIntentAPI
{
    String SCHEME = "alfresco";

    // ///////////////////////////////////////////////////////////////////////////
    // PREFIX
    // ///////////////////////////////////////////////////////////////////////////
    String PREFIX_ACTION = "com.alfresco.android.intent.action";

    String PREFIX_EXTRA = "com.alfresco.android.intent.extra";

    // TYPE
    // ///////////////////////////////////////////////////////////////////////////
    String AUTHORITY_FOLDER = "folder";

    String AUTHORITY_DOCUMENT = "document";
    
    String AUTHORITY_FILE = "file";

    // EXTRA
    // ///////////////////////////////////////////////////////////////////////////
    String EXTRA_ACCOUNT_ID = PREFIX_EXTRA.concat(".ACCOUNT_ID");

    String EXTRA_FOLDER_ID = PREFIX_EXTRA.concat(".FOLDER_ID");

    String EXTRA_DOCUMENT_ID = PREFIX_EXTRA.concat(".DOCUMENT_ID");

    String EXTRA_PATH = PREFIX_EXTRA.concat(".PATH");


    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * <h3>Text Editor</h3>
     * <ul>
     * <li>text/plain : Start Alfresco Text Editor
     * </ul>
     */
    String ACTION_CREATE = PREFIX_ACTION.concat(".CREATE");

    String EXTRA_SPEECH2TEXT = PREFIX_EXTRA.concat(".SPEECH2TEXT");

    String ACTION_SEND = PREFIX_ACTION.concat(".SEND");

    // READ
    // ///////////////////////////////////////////////////////////////////////////
    String ACTION_VIEW = PREFIX_ACTION.concat(".VIEW");

    // EDIT/UPDATE
    // ///////////////////////////////////////////////////////////////////////////
    String ACTION_RENAME = PREFIX_ACTION.concat(".RENAME");

}

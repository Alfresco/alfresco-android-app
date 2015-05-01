package org.alfresco.mobile.android.application.providers.storage;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.DocumentsContract;

/**
 * Created by jpascal on 23/04/2015.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public final class AlfrescoContract {
    private static final String TAG = "AlfrescoContract";

    public static final String[] DEFAULT_ROOT_PROJECTION = new String[]{DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES, DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON, DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY, DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES};

    public static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.COLUMN_SIZE,
            AlfrescoContract.Document.COLUMN_PATH, AlfrescoContract.Document.COLUMN_TYPE, AlfrescoContract.Document.COLUMN_ACCOUNT_ID};

    private AlfrescoContract() {
    }

    public final static class Document {
        private Document() {
        }

        public static final String COLUMN_PATH = "alf_path";

        public static final String COLUMN_ACCOUNT_ID = "alf_account_id";

        public static final String COLUMN_TYPE = "alf_type";
    }
}

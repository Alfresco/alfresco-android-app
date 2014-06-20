package org.alfresco.mobile.android.application.commons.extensions;

import org.alfresco.mobile.android.application.commons.data.DocumentTypeRecord;

public abstract class SamsungManager
{

    public static final String SAMSUNG_NOTE_EXTENSION_SPD = "spd";

    public static final String SAMSUNG_NOTE_MIMETYPE = "application/samsung_note";

    public abstract DocumentTypeRecord addDocumentTypeRecord();

    public abstract boolean hasPenEnable();

}

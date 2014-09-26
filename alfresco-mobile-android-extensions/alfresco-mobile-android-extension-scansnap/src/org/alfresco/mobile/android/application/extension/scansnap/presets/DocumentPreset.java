package org.alfresco.mobile.android.application.extension.scansnap.presets;

import org.alfresco.mobile.android.application.extension.scansnap.R;

/**
 * All scanned pages are merged into a single PDF Image File.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentPreset extends DefaultPreset
{
    public static final int ID = 1;

    public DocumentPreset()
    {
        // UI parameters
        titleId = R.string.scan_preset_document;
        iconId = R.drawable.mime_pdf;

        // Scan parameters
        paperSize = PAPERSIZE_AUTO;
        fileNameFormat = FILENAMEFORMAT_ATTACHED;
        outMode = OUTMODE_OPEN;
    }

    @Override
    public int getIdentifier()
    {
        return ID;
    }
}

package org.alfresco.mobile.android.application.extension.scansnap.presets;

import org.alfresco.mobile.android.application.extension.scansnap.R;
import org.alfresco.mobile.android.application.extension.scansnap.ScanSnapDispatcherActivity;

/**
 * All scanned pages are merged into a single PDF Image File.
 * 
 * @author Jean Marie Pascal
 */
public class DefaultPreset extends ScanSnapPreset
{
    public static final int ID = 0;

    public DefaultPreset()
    {
        // UI parameters
        titleId = R.string.scan_preset_default;
        iconId = org.alfresco.mobile.android.foundation.R.drawable.mime_generic;

        // Scan parameters
        paperSize = PAPERSIZE_AUTO;
        fileNameFormat = FILENAMEFORMAT_ATTACHED;
        outMode = OUTMODE_OPEN;
        reduceBleedThrough = REDUCEBLEEDTHROUGH_ENABLE;
        blankPageSkip = BLANKPAGESKIP_ENABLE;
        scanMode = SCANMODE_AUTO;
        callBack = "org.alfresco.mobile.android.application,".concat(ScanSnapDispatcherActivity.class.getName());
    }

    @Override
    public int getIdentifier()
    {
        return ID;
    }
}

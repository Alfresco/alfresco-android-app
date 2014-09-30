package org.alfresco.mobile.android.application.extension.scansnap.presets;

import org.alfresco.mobile.android.application.extension.scansnap.R;

/**
 * Each photograph is stored as a high-resolution scanned image.
 * 
 * @author Jean Marie Pascal
 */
public class PhotoPreset extends DefaultPreset
{
    public static final int ID = 2;

    public PhotoPreset()
    {
        super();
        // UI parameters
        titleId = R.string.scan_preset_photo;
        iconId = org.alfresco.mobile.android.foundation.R.drawable.mime_img;

        // Scan parameters
        savetogether = SAVETOGETHER_DISABLE;
        format = FORMAT_JPEG;
        scanMode = SCANMODE_BETTER;
    }

    @Override
    public int getIdentifier()
    {
        return ID;
    }
}

package org.alfresco.mobile.android.application.extension.scansnap.presets;

import org.alfresco.mobile.android.application.extension.scansnap.R;

/**
 * Each business card is saved as a picture.
 * 
 * @author Jean Marie Pascal
 */
public class BusinessCardPreset extends DefaultPreset
{
    public static final int ID = 3;
    
    public BusinessCardPreset()
    {
        //UI parameters
        titleId = R.string.scan_preset_business_card;
        iconId = R.drawable.ic_person;

        
        //Scan parameters
        paperSize = PAPERSIZE_BUSINESSCARDS;
        savetogether = SAVETOGETHER_DISABLE;
        format = FORMAT_JPEG;
        fileNameFormat = FILENAMEFORMAT_ATTACHED;
        outMode = OUTMODE_OPEN;
        scanMode = SCANMODE_NORMAL;
    }
    
    @Override
    public int getIdentifier()
    {
        return ID;
    }
}

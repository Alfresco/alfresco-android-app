/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.platform.extensions;

import org.alfresco.mobile.android.platform.Manager;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;

public abstract class ScanSnapManager extends Manager
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    public static final String ACTION_START_SCANSNAP = "com.alfresco.intent.action.START_SCANSNAP";

    /**
     * Keys for url
     */
    public static final String CARRY_ERROR = "Error";

    public static final String CARRY_OUTMODE = "OutMode";

    public static final String CARRY_FORMAT = "Format";

    public static final String CARRY_FILE = "File";

    public static final String CARRY_FILE_COUNT = "FileCount";

    public static final String PFUFILELISTFORMAT = "PFUFILELISTFORMAT";

    /**
     * File type
     */
    public static final int CARRY_FILE_TYPE_JPG = 2;

    public static final int CARRY_FILE_TYPE_PDF_MULTI_MODE = 1;

    /**
     * Data linkage mode
     */
    public static final int CARRY_OUTMODE_URL_PATH = 2;

    /**
     * Error type
     */
    public static final int CARRY_RESULT_SUCCESS = 0;

    public static final int CARRY_SCAN_ERROR = -1;

    public static final int CARRY_PARSE_ERROR = -2;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static ScanSnapManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, ScanSnapManager.class.getSimpleName());
            }

            return (ScanSnapManager) mInstance;
        }
    }

    protected ScanSnapManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract boolean hasScanSnapApplication();

    public abstract void startPresetChooser(FragmentActivity activity);

    public abstract void scan(FragmentActivity activity, Integer preset);
}

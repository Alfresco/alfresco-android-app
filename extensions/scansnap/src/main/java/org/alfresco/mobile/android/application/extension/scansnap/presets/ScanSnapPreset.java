/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.extension.scansnap.presets;

import android.net.Uri;

public abstract class ScanSnapPreset
{
    protected static final int EMPTY_VALUE = -1;

    public static final String URL_PROTOCOL = "scansnap:///";

    public static final String URL_ACTION_SCAN = "scan";

    /** Specify the file format of the scanned images to be saved. */
    protected int format = EMPTY_VALUE;

    public static final String FORMAT = "Format";

    public static final int FORMAT_PDF = 1;

    public static final int FORMAT_JPEG = 2;

    /** Specify whether to enable batch saving. Enabled for Format=2 (JPEG). */
    protected int savetogether = EMPTY_VALUE;

    public static final String SAVETOGETHER = "SaveTogether";

    public static final int SAVETOGETHER_ENABLE = 0;

    public static final int SAVETOGETHER_DISABLE = 1;

    /**
     * When iX100 is connected, this setting scanning is ignored and simplex
     * scanning is set.
     */
    protected int scanningside = EMPTY_VALUE;

    public static final String SCANNINGSIDE = "ScanningSide";

    public static final int SCANNINGSIDE_DUPLEX = 0;

    public static final int SCANNINGSIDE_SIMPLEX = 1;

    /**
     * Specify the color mode. Black & white is enabled for Format=1 (file
     * format: PDF).
     */
    protected int colorMode = EMPTY_VALUE;

    public static final String COLORMODE = "ColorMode";

    public static final int COLORMODE_AUTO = 1;

    public static final int COLORMODE_COLOR = 2;

    public static final int COLORMODE_BW = 3;

    public static final int COLORMODE_GRAY = 5;

    /**
     * If B&W is selected for color mode, specify the black & white density.
     * Enabled for ColorMode=3 (color mode: B&W). The initial value is "5".
     */
    protected int concentration = EMPTY_VALUE;

    public static final String CONCENTRATION = "Concentration";

    public static final int CONCENTRATION_MINUS_5 = 0;

    public static final int CONCENTRATION_MINUS_4 = 1;

    public static final int CONCENTRATION_MINUS_3 = 2;

    public static final int CONCENTRATION_MINUS_2 = 3;

    public static final int CONCENTRATION_MINUS_1 = 4;

    public static final int CONCENTRATION_0 = 5;

    public static final int CONCENTRATION_1 = 6;

    public static final int CONCENTRATION_2 = 7;

    public static final int CONCENTRATION_3 = 8;

    public static final int CONCENTRATION_4 = 9;

    public static final int CONCENTRATION_5 = 10;

    /**
     * Specify the image quality (resolution).
     */
    protected int scanMode = EMPTY_VALUE;

    public static final String SCANMODE = "ScanMode";

    public static final int SCANMODE_NORMAL = 1;

    public static final int SCANMODE_BETTER = 2;

    public static final int SCANMODE_BEST = 3;

    public static final int SCANMODE_AUTO = 99;

    /**
     * Specify whether to enable continuous scanning.
     */
    protected int continueScan = EMPTY_VALUE;

    public static final String CONTINUESCAN = "ContinueScan";

    public static final int CONTINUESCAN_ENABLE = 0;

    public static final int CONTINUESCAN_DISABLE = 1;

    /**
     * Specify whether to delete blank pages.
     */
    protected int blankPageSkip = EMPTY_VALUE;

    public static final String BLANKPAGESKIP = "BlankPageSkip";

    public static final int BLANKPAGESKIP_ENABLE = 1;

    public static final int BLANKPAGESKIP_DISABLE = 0;

    /**
     * Specify whether to reduce bleedthrough.
     */
    protected int reduceBleedThrough = EMPTY_VALUE;

    public static final String REDUCEBLEEDTHROUGH = "ReduceBleedThrough";

    public static final int REDUCEBLEEDTHROUGH_ENABLE = 1;

    public static final int REDUCEBLEEDTHROUGH_DISABLE = 0;

    /**
     * Specify the format of the file name to be added automatically to scanned
     * images.
     */
    protected int fileNameFormat = EMPTY_VALUE;

    public static final String FILENAMEFORMAT = "FileNameFormat";

    /** yyyy_MM_dd_HH_mm_ss */
    public static final int FILENAMEFORMAT_UNDERSCORE = 0;

    /** yyyyMMddHHmmss */
    public static final int FILENAMEFORMAT_ATTACHED = 1;

    /** Direct input */
    public static final int FILENAMEFORMAT_DIRECTINPUT = 2;

    /** yyyy-MM-dd-HHmm-ss */
    public static final int FILENAMEFORMAT_MINUS = 3;

    /**
     * Specify the file name when direct input is selected for file name format.
     * Enabled for FileNameFormat=2 (direct input). The initial value is
     * "untitled". (This depends on the language environment.)
     */
    protected String saveNameDirectInput = null;

    public static final String SAVENAMEDIRECTINPUT = "SaveNameDirectInput";

    /** Specify the paper size. */
    protected int paperSize = EMPTY_VALUE;

    public static final String PAPERSIZE = "PaperSize";

    public static final int PAPERSIZE_AUTO = 0;

    public static final int PAPERSIZE_A4 = 1;

    public static final int PAPERSIZE_A5 = 2;

    public static final int PAPERSIZE_A6 = 3;

    public static final int PAPERSIZE_B5 = 4;

    public static final int PAPERSIZE_B6 = 5;

    public static final int PAPERSIZE_POSTCARDS = 6;

    public static final int PAPERSIZE_BUSINESSCARDS = 7;

    public static final int PAPERSIZE_LETTER = 8;

    public static final int PAPERSIZE_LEGAL = 9;

    /**
     * When iX100 is connected, this setting is ignored and "do not detect" is
     * set.
     */
    protected int multiFeedControl = EMPTY_VALUE;

    public static final String MULTIFEEDCONTROL = "MultiFeedControl";

    public static final int MULTIFEEDCONTROL_ENABLE = 1;

    public static final int MULTIFEEDCONTROL_DISABLE = 0;

    /** Specify the compression ratio. */
    protected int compression = EMPTY_VALUE;

    public static final String COMPRESSION = "Compression";

    public static final int COMPRESSION_LOW = 1;

    public static final int COMPRESSION_MODERATELY_LOW = 2;

    public static final int COMPRESSION_STANDARD = 3;

    public static final int COMPRESSION_MODERATELY_HIGH = 4;

    public static final int COMPRESSION_HIGH = 5;

    /** Output Mode after the scan process. */
    protected int outMode = EMPTY_VALUE;

    public static final String OUTMODE = "OutMode";

    /**
     * Start user applications by using URL Scheme File path specification
     */
    public static final int OUTMODE_URL = 2;

    /**
     * Start user applications when explicitly specified (by selecting Open)
     */
    public static final int OUTMODE_OPEN = 3;

    /**
     * If OutMode=2 is specified and OutPath is not specified for the user
     * application in Android, files are saved at the default SSCA save
     * destination.
     */
    protected String outPath = null;

    public static final String OUTPATH = "OutPath";

    /**
     * 
     */
    protected String callBack = null;

    public static final String CALLBACK = "CallBack";

    protected int titleId;

    protected int iconId;

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract int getIdentifier();

    public int getTitleId()
    {
        return titleId;
    }

    public int getIconId()
    {
        return iconId;
    }

    /**
     * @return the format
     */
    public int getFormat()
    {
        return format;
    }

    /**
     * @return the savetogether
     */
    public int getSavetogether()
    {
        return savetogether;
    }

    /**
     * @return the scanningside
     */
    public int getScanningside()
    {
        return scanningside;
    }

    /**
     * @return the colorMode
     */
    public int getColorMode()
    {
        return colorMode;
    }

    /**
     * @return the concentration
     */
    public int getConcentration()
    {
        return concentration;
    }

    /**
     * @return the scanMode
     */
    public int getScanMode()
    {
        return scanMode;
    }

    /**
     * @return the continueScan
     */
    public int getContinueScan()
    {
        return continueScan;
    }

    /**
     * @return the blankPageSkip
     */
    public int getBlankPageSkip()
    {
        return blankPageSkip;
    }

    /**
     * @return the reduceBleedThrough
     */
    public int getReduceBleedThrough()
    {
        return reduceBleedThrough;
    }

    /**
     * @return the fileNameFormat
     */
    public int getFileNameFormat()
    {
        return fileNameFormat;
    }

    /**
     * @return the saveNameDirectInput
     */
    public String getSaveNameDirectInput()
    {
        return saveNameDirectInput;
    }

    /**
     * @return the paperSize
     */
    public int getPaperSize()
    {
        return paperSize;
    }

    /**
     * @return the multiFeedControl
     */
    public int getMultiFeedControl()
    {
        return multiFeedControl;
    }

    /**
     * @return the compression
     */
    public int getCompression()
    {
        return compression;
    }

    /**
     * @return the outMode
     */
    public int getOutMode()
    {
        return outMode;
    }

    /**
     * @return the outPath
     */
    public String getOutPath()
    {
        return outPath;
    }

    /**
     * @return the callBack
     */
    public String getCallBack()
    {
        return callBack;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public Uri generateURI()
    {
        StringBuilder builder = new StringBuilder(URL_PROTOCOL);
        builder.append(URL_ACTION_SCAN);
        addParameter(builder, FORMAT, format);
        addParameter(builder, SAVETOGETHER, savetogether);
        addParameter(builder, SCANNINGSIDE, scanningside);
        addParameter(builder, COLORMODE, colorMode);
        addParameter(builder, CONCENTRATION, concentration);
        addParameter(builder, SCANMODE, scanMode);
        addParameter(builder, CONTINUESCAN, continueScan);
        addParameter(builder, BLANKPAGESKIP, blankPageSkip);
        addParameter(builder, REDUCEBLEEDTHROUGH, reduceBleedThrough);
        addParameter(builder, FILENAMEFORMAT, fileNameFormat);
        addParameter(builder, SAVENAMEDIRECTINPUT, saveNameDirectInput);
        addParameter(builder, PAPERSIZE, paperSize);
        addParameter(builder, MULTIFEEDCONTROL, multiFeedControl);
        addParameter(builder, COMPRESSION, compression);
        addParameter(builder, OUTMODE, outMode);
        addParameter(builder, OUTPATH, outPath);
        addParameter(builder, CALLBACK, callBack);
        return Uri.parse(builder.toString());
    }

    protected void addParameter(StringBuilder builder, String key, int value)
    {
        if (value != EMPTY_VALUE)
        {
            builder.append("&").append(key).append("=").append(value);
        }
    }

    protected void addParameter(StringBuilder builder, String key, String value)
    {
        if (value != null)
        {
            builder.append("&").append(key).append("=").append(value);
        }
    }
}

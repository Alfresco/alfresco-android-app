//--------------------------------------------------------------------
//	CarryResult.java
//	ScanSnap Connect Application Sample
//	All rights reserved, Copyright (c) 2013 PFU LIMITED
//-------------------------------------------------------------------
package org.alfresco.mobile.android.application.capture;

import java.io.Serializable;

public class CarryResult implements Serializable
{
	public static final String TAG = "CarryResult";

    private static final long serialVersionUID = 1L;

	private static CarryResult mInstance = null;
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


	public int m_OutMode;

	public boolean m_scanResult;

	public int m_nScanFileType;

	public int m_nFileCnt;



	public static CarryResult getInstance() 
	{
		if (mInstance == null) 
		{
			mInstance = new CarryResult();
		}
		return mInstance;
	}

	public CarryResult() 
	{

	}


	public int getOutMode() 
	{
		return m_OutMode;
	}

	public void setOutMode(int OutMode) 
	{
		this.m_OutMode = OutMode;
	}

	public boolean getScanResult() 
	{
		return m_scanResult;
	}

	public void setScanResult(boolean scanResult) 
	{
		this.m_scanResult = scanResult;
	}

	public int getScanFileType() 
	{
		return m_nScanFileType;
	}

	public void setScanFileType(int scanFileType) 
	{
		this.m_nScanFileType = scanFileType;
	}

	public int getFileCnt() 
	{
		return m_nFileCnt;
	}

	public void setFileCnt(int FileCnt) 
	{
		this.m_nFileCnt = FileCnt;
	}

	public void clear()
	{
		m_OutMode = 0;
		m_scanResult = false;
		m_nScanFileType = 0;
		m_nFileCnt = 0;
	}
}	
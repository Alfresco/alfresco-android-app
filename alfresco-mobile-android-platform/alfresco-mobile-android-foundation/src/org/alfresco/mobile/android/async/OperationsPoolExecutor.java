/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class OperationsPoolExecutor extends ThreadPoolExecutor
{
    private static final int DEFAULT_THREAD_COUNT = 2;

    OperationsPoolExecutor()
    {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new OperationsThreadFactory());
    }

    void adjustThreadCount(NetworkInfo info)
    {
        if (info == null || !info.isConnectedOrConnecting())
        {
            setThreadCount(DEFAULT_THREAD_COUNT);
            return;
        }
        switch (info.getType())
        {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                setThreadCount(DEFAULT_THREAD_COUNT);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype())
                {
                    case TelephonyManager.NETWORK_TYPE_LTE: // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        setThreadCount(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        setThreadCount(1);
                        break;
                    default:
                        setThreadCount(DEFAULT_THREAD_COUNT);
                }
                break;
            default:
                setThreadCount(DEFAULT_THREAD_COUNT);
        }
    }

    private void setThreadCount(int threadCount)
    {
        setCorePoolSize(threadCount);
        setMaximumPoolSize(threadCount);
    }
}

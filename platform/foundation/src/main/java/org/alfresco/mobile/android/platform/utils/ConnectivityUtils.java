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
package org.alfresco.mobile.android.platform.utils;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Utility class to manage connectivity.
 * 
 * @author Jean Marie Pascal
 */
public final class ConnectivityUtils
{
    private ConnectivityUtils()
    {
    }

    public static boolean isAirplaneModeOn(Context context)
    {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean hasPermission(Context context, String permission)
    {
        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasInternetAvailable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) { return true; }
        return false;
    }

    public static boolean isWifiAvailable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) { return true; }
        return false;
    }

    public static boolean isMobileNetworkAvailable(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileInfo != null && mobileInfo.isConnected()) { return true; }
        return false;
    }

    public static boolean hasNetwork(Context context)
    {
        if (!ConnectivityUtils.hasInternetAvailable(context))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean hasMobileConnectivity(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileInfo != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static long getAverageSpeed(Context context)
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobileInfo = cm.getActiveNetworkInfo();
            int type = mobileInfo.getType();
            int subType = mobileInfo.getSubtype();
            if (mobileInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                return 500 * 1024;
            }
            else if (type == ConnectivityManager.TYPE_MOBILE)
            {
                switch (subType)
                {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return 50 * 1024; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return 14 * 1024; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return 50 * 1024; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return 200 * 1024; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return 300 * 1024; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return 80 * 1024; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return 1000 * 1024; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return 600 * 1024; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return 1000 * 1024; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return 350 * 1024; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return 800 * 1024; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return 1000 * 1024; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return 1000 * 1024; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return 25 * 1024; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return 1000 * 1024; // ~ 10+ Mbps
                        // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return 125 * 1024;
                }
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
        return 125 * 1024;
    }

}

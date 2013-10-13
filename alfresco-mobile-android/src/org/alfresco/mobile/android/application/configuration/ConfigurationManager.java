/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.configuration.ConfigurationOperationRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * @author Jean Marie Pascal
 */
public final class ConfigurationManager
{
    // private static final String TAG = ApplicationManager.class.getName();

    private static ConfigurationManager mInstance;

    private final Context appContext;

    private int state = STATE_UNKNOWN;

    private Map<Long, ConfigurationContext> configContextMap = new HashMap<Long, ConfigurationContext>();

    private static final Object LOCK = new Object();

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////

    public static final String DATA_DICTIONNARY = "Data Dictionary";

    public static final List<String> DATA_DICTIONNARY_LIST = new ArrayList<String>(8)
    {
        {
            add("Data Dictionary");// UK,JA
            add("Dictionnaire de données");// FR
            add("Datenverzeichnis");// DE
            add("Diccionario de datos");// ES
            add("Dizionario dei dati");// IT
            add("Dataordbok");// Nb NO
            add("Gegevenswoordenboek");// NL
            add("Dicionário de dados");// PT
        }
    };

    public static final String DATA_DICTIONNARY_MOBILE_PATH = "Mobile/configuration.json";

    public static final int STATE_UNKNOWN = 0;

    public static final int STATE_NO_CONFIGURATION = 1;

    public static final int STATE_HAS_CONFIGURATION = 2;

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC MODEL
    // ///////////////////////////////////////////////////////////////////////////
    public static final String CATEGORY_ROOTMENU = "rootMenu";

    public static final String MENU_ACTIVITIES = "com.alfresco.activities";

    public static final String MENU_REPOSITORY = "com.alfresco.repository";

    public static final String MENU_SITES = "com.alfresco.sites";

    public static final String MENU_TASKS = "com.alfresco.tasks";

    public static final String MENU_FAVORITES = "com.alfresco.favorites";

    public static final String MENU_SEARCH = "com.alfresco.search";

    public static final String MENU_LOCAL_FILES = "com.alfresco.localFiles";

    public static final String MENU_NOTIFICATIONS = "com.alfresco.notifications";

    public static final String MENU_SHARED = "com.alfresco.repository.shared";

    public static final String MENU_MYFILES = "com.alfresco.repository.userhome";

    public static final String PROP_VISIBILE = "visible";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    private ConfigurationManager(Context applicationContext)
    {
        appContext = applicationContext;
        LocalBroadcastManager.getInstance(appContext).registerReceiver(new ConfigurationReceiver(),
                new IntentFilter(IntentIntegrator.ACTION_CONFIGURATION_COMPLETED));
    }

    public static ConfigurationManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new ConfigurationManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public void retrieveConfiguration(Activity activity, Account acc)
    {
        if (hasConfig(acc))
        {
            state = STATE_HAS_CONFIGURATION;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IntentIntegrator.ACTION_CONFIGURATION_MENU);
            broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, acc.getId());
            LocalBroadcastManager.getInstance(activity).sendBroadcast(broadcastIntent);
            return;
        }

        // Retrieve data dictionaryRef
        String dataDictionaryRef = getDataDictionaryRef(appContext, acc.getId());
        String configurationRef = getConfigurationRef(appContext, acc.getId());
        Long lastModificationTime = getLastModificationTime(appContext, acc.getId());
        OperationsRequestGroup group = new OperationsRequestGroup(activity, SessionUtils.getAccount(activity));
        group.enqueue(new ConfigurationOperationRequest(dataDictionaryRef, configurationRef, lastModificationTime)
                .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        BatchOperationManager.getInstance(activity).enqueue(group);
    }

    public int getConfigurationState()
    {
        return state;
    }

    public ConfigurationContext getConfig(Account account)
    {
        if (configContextMap != null && account != null)
        {
            return configContextMap.get(account.getId());
        }
        else
        {
            return null;
        }
    }
    
    public boolean hasConfig(Account account)
    {
        if (configContextMap != null && account != null)
        {
            return configContextMap.containsKey(account.getId());
        }
        else
        {
            return false;
        }
    }

    private static final String CONFIGURATION_PREFIX = "Configuration-";

    private static final String DATA_DICTIONARY_PREFIX = "DataDictionary-";

    private static final String LASTMODIFICATION_PREFIX = "LastModification-";

    private static void setDataDictionaryRef(Context activity, long accountId, String dataDictionaryRef,
            String configId, long lastModificationTime)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPref.edit().putString(CONFIGURATION_PREFIX + accountId, NodeRefUtils.getCleanIdentifier(configId))
                .putString(DATA_DICTIONARY_PREFIX + accountId, NodeRefUtils.getCleanIdentifier(dataDictionaryRef))
                .putLong(LASTMODIFICATION_PREFIX + accountId, lastModificationTime).commit();
    }

    private static String getDataDictionaryRef(Context activity, long accountId)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPref.getString(DATA_DICTIONARY_PREFIX + accountId, null);
    }

    private static String getConfigurationRef(Context activity, long accountId)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPref.getString(CONFIGURATION_PREFIX + accountId, null);
    }

    private static Long getLastModificationTime(Context activity, long accountId)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPref.getLong(LASTMODIFICATION_PREFIX + accountId, -1);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    public class ConfigurationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                if (intent != null && intent.getExtras() != null
                        && intent.getExtras().containsKey(IntentIntegrator.EXTRA_DATA))
                {
                    Bundle b = intent.getExtras().getBundle(IntentIntegrator.EXTRA_DATA);
                    if (b != null && b.containsKey(IntentIntegrator.EXTRA_CONFIGURATION))
                    {
                        state = STATE_HAS_CONFIGURATION;
                        ConfigurationContext configContext = (ConfigurationContext) b
                                .getSerializable(IntentIntegrator.EXTRA_CONFIGURATION);
                        configContextMap.put(b.getLong(IntentIntegrator.EXTRA_ACCOUNT_ID), configContext);

                        // Save dictionaryRef
                        setDataDictionaryRef(appContext, b.getLong(IntentIntegrator.EXTRA_ACCOUNT_ID),
                                b.getString(IntentIntegrator.EXTRA_DATA_DICTIONARY_ID),
                                b.getString(IntentIntegrator.EXTRA_CONFIGURATION_ID),
                                b.getLong(IntentIntegrator.EXTRA_LASTMODIFICATION));

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(IntentIntegrator.ACTION_CONFIGURATION_MENU);
                        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID,
                                b.getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                    }
                    else
                    {
                        state = STATE_NO_CONFIGURATION;
                    }
                }
                else
                {
                    state = STATE_NO_CONFIGURATION;
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }
        }
    }
}

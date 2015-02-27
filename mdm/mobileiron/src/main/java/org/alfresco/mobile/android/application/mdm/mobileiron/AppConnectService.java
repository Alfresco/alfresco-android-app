/*
 * Copyright 2013 Mobile Iron, Inc.
 * All rights reserved.
 */

package org.alfresco.mobile.android.application.mdm.mobileiron;

import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.extensions.MobileIronManager;
import org.alfresco.mobile.android.platform.mdm.MDMEvent;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class AppConnectService extends IntentService
{
    public static final String TAG = "AppConnectConfigService";

    public static final String ACTION_HANDLE_CONFIG = "com.mobileiron.HANDLE_CONFIG";

    public static final String CONFIG_APPLIED_INTENT = "configAppliedIntent";

    public static final String CONFIG_ERROR_INTENT = "configErrorIntent";

    public static final String ERROR_STRING = "errorString";

    public static final String CONFIG = "config";

    public AppConnectService()
    {
        super("ConfigService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (ACTION_HANDLE_CONFIG.equals(intent.getAction()))
        {
            Intent i;
            try
            {
                Bundle config = intent.getBundleExtra(CONFIG);

                if (config == null) { throw new AlfrescoAppException("No Config."); }

                MobileIronManager.getInstance(getApplicationContext()).setConfig(config);
                i = intent.getParcelableExtra(CONFIG_APPLIED_INTENT);

                EventBusManager.getInstance().post(new MDMEvent());
            }
            catch (Exception e)
            {
                i = intent.getParcelableExtra(CONFIG_ERROR_INTENT);
                i.putExtra(ERROR_STRING, e.getMessage());

                EventBusManager.getInstance().post(new MDMEvent(e));
            }
            startService(i);
        }
    }
}

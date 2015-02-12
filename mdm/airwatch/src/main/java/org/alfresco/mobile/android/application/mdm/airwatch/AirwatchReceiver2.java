package org.alfresco.mobile.android.application.mdm.airwatch;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.util.Log;

/**
 * Created by jpascal on 12/02/2015.
 */
@TargetApi(21)
public class AirwatchReceiver2 extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.e("[AIRWATCH]", "Received Request Permission : " + intent);
        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        Log.e("[AIRWATCH]", manager.getApplicationRestrictions("org.alfresco.mobile.android.application.debug").toString());
    }
}

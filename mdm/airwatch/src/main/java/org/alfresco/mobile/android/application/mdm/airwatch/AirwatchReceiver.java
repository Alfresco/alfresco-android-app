package org.alfresco.mobile.android.application.mdm.airwatch;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.service.restrictions.RestrictionsReceiver;
import android.util.Log;

/**
 * Created by jpascal on 12/02/2015.
 */
@TargetApi(21)
public class AirwatchReceiver extends RestrictionsReceiver
{
    @Override
    public void onRequestPermission(Context context, String packageName, String requestType, String requestId,
            PersistableBundle request)
    {
        Log.e("[AIRWATCH]", "Received Request Permission" + requestType);
        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        Log.e("[AIRWATCH]", manager.getApplicationRestrictions("org.alfresco.mobile.android.application.debug").toString());
    }
}

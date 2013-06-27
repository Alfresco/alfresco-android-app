package org.alfresco.mobile.android.application.security;

import java.io.File;
import java.util.Stack;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DataCleanerService extends Service
{
    private static final String TAG = DataCleanerService.class.getName();

    private Stack<Intent> intents = new Stack<Intent>();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getExtras() != null)
        {
            intents.push(intent);
            startService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // ////////////////////////////////////////////////////
    // Service lifecycle
    // ////////////////////////////////////////////////////
    private void startService()
    {
        try
        {
            Intent intent = intents.pop();
            if (intent.getAction() == IntentIntegrator.ACTION_CLEAN_SHARE_FILE)
            {
                deleteShareFile(intent.getExtras().getString(IntentIntegrator.EXTRA_FILE_PATH));
            }
            else
            {
                stopSelf();
            }
        }
        catch (Exception e)
        {
            stopSelf();
        }
    }


    // ////////////////////////////////////////////////////
    // ACTIONS
    // ////////////////////////////////////////////////////
    private void deleteShareFile(String filePath)
    {
        Log.d(TAG, "Clean");
        if (filePath != null)
        {
            File removeFile = new File(filePath);
            if (removeFile.exists())
            {
                removeFile.delete();
            }
        }
        stopSelf();
        return;
    }
}

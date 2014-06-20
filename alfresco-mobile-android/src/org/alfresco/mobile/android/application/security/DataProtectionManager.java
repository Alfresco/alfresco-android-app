package org.alfresco.mobile.android.application.security;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.alfresco.mobile.android.application.VersionNumber;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.file.encryption.DataProtectionRequest;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.IOUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DataProtectionManager implements VersionNumber
{
    //private static final String TAG = DataProtectionManager.class.getName();

    public static final int ACTION_NONE = -1;

    public static final int ACTION_VIEW = 1;

    public static final int ACTION_OPEN_IN = 2;

    public static final int ACTION_SEND = 4;

    public static final int ACTION_SEND_ALFRESCO = 8;

    public static final int ACTION_COPY = 16;

    private static DataProtectionManager mInstance;

    protected final Context mAppContext;

    protected static final Object LOCK = new Object();

    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    public static DataProtectionManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new DataProtectionManager(context.getApplicationContext());
            }

            return (DataProtectionManager) mInstance;
        }
    }

    private DataProtectionManager(Context applicationContext)
    {
        mAppContext = applicationContext;
    }

    // ////////////////////////////////////////////////////
    // PUBLIC ACTIONS
    // ////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////
    // ENCRYPTION
    // ////////////////////////////////////////////////////
    public void copyAndEncrypt(Account account, List<File> sourceFiles, File folderStorage)
    {
        if (account == null) { return; }
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);

        File destinationFile = null;
        for (File sourceFile : sourceFiles)
        {
            destinationFile = new File(folderStorage, sourceFile.getName());
            destinationFile = IOUtils.createFile(destinationFile);
            if (isEncryptionEnable())
            {
                group.enqueue(new DataProtectionRequest(sourceFile, destinationFile, true, ACTION_NONE)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
            }
            else
            {
                group.enqueue(new DataProtectionRequest(sourceFile, destinationFile, true, ACTION_COPY)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
            }
        }

        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void copyAndEncrypt(Account account, File sourceFile, File destinationFile)
    {
        if (account == null) { return; }
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        if (isEncryptionEnable())
        {
            group.enqueue(new DataProtectionRequest(sourceFile, destinationFile, true, ACTION_NONE)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        else
        {
            group.enqueue(new DataProtectionRequest(sourceFile, destinationFile, true, ACTION_COPY)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_NOTIFICATIONS));
        }
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void encrypt(Account account)
    {
        File folder = StorageManager.getPrivateFolder(mAppContext, "", null);
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new DataProtectionRequest(folder, true)
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void encrypt(Account account, File file)
    {
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new DataProtectionRequest(file, true)
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void checkEncrypt(Account account, File file)
    {
        if (account == null) { return; }
        if (isEncryptable(account, file) && !isEncrypted(file.getPath()))
        {
            encrypt(account, file);
        }
    }

    // ////////////////////////////////////////////////////
    // DECRYPTION
    // ////////////////////////////////////////////////////
    public void decrypt(Account account)
    {
        File folder = StorageManager.getPrivateFolder(mAppContext, "", null);
        OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
        group.enqueue(new DataProtectionRequest(folder, false)
                .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
        BatchOperationManager.getInstance(mAppContext).enqueue(group);
    }

    public void checkDecrypt(Account account, File file)
    {
        decrypt(account, file, ACTION_NONE);
    }

    public void decrypt(Account account, File file, int intentAction)
    {
        if (account == null) { return; }
        if (isEncryptable(account, file) && isEncrypted(file.getPath()))
        {
            OperationsRequestGroup group = new OperationsRequestGroup(mAppContext, account);
            group.enqueue(new DataProtectionRequest(file, false, intentAction)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
            BatchOperationManager.getInstance(mAppContext).enqueue(group);
        }
    }

    public boolean isEncrypted(String filePath)
    {
        try
        {
            return EncryptionUtils.isEncrypted(mAppContext, filePath);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public boolean isEncryptionEnable()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        return prefs.getBoolean(GeneralPreferences.PRIVATE_FOLDERS, false);
    }

    public boolean isEncryptable(Account account, File file)
    {
        return isEncryptionEnable() && isFileInProtectedFolder(account, file);
    }

    // ////////////////////////////////////////////////////
    // Internal Utils
    // ////////////////////////////////////////////////////
    private boolean isFileInProtectedFolder(Account account, File f)
    {
        return (f.getPath().startsWith(StorageManager.getDownloadFolder(mAppContext, account).getPath()) || f.getPath()
                .startsWith(StorageManager.getSynchroFolder(mAppContext, account).getPath()));
    }

    // ////////////////////////////////////////////////////
    // Broadcast Receiver
    // ////////////////////////////////////////////////////
    private static Intent createActionIntent(Activity activity, int intentAction, File f)
    {
        Intent intentI = null;
        switch (intentAction)
        {
            case DataProtectionManager.ACTION_SEND_ALFRESCO:
                intentI = ActionManager.createSendFileToAlfrescoIntent(activity, f);
                break;
            case DataProtectionManager.ACTION_SEND:
                intentI = ActionManager.createSendIntent(activity, f);
                break;
            case DataProtectionManager.ACTION_VIEW:
                intentI = ActionManager.createViewIntent(activity, f);
                break;
            default:
                break;
        }

        if (intentI != null)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            prefs.edit().putString(GeneralPreferences.REQUIRES_ENCRYPT, f.getPath()).commit();
        }
        return intentI;
    }

    public static void executeAction(Activity activity, int intentAction, File f)
    {
        try
        {
            if (intentAction == DataProtectionManager.ACTION_NONE || intentAction == 0) { return; }
            activity.startActivityForResult(createActionIntent(activity, intentAction, f),
                    PublicIntent.REQUESTCODE_DECRYPTED);
        }
        catch (ActivityNotFoundException e)
        {

        }
    }

    public static void executeAction(Fragment fragment, int intentAction, File f)
    {
        try
        {
            if (intentAction == DataProtectionManager.ACTION_NONE || intentAction == 0) { return; }
            fragment.startActivityForResult(createActionIntent(fragment.getActivity(), intentAction, f),
                    PublicIntent.REQUESTCODE_DECRYPTED);
        }
        catch (ActivityNotFoundException e)
        {

        }
    }
}

package org.alfresco.mobile.android.application.integration.account;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountProvider;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.thirdparty.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LoadSessionCallBack extends AbstractOperationCallback<AlfrescoSession>
{
    private static final String TAG = LoadSessionCallBack.class.getName();

    public LoadSessionCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.wait_message);
        complete = getBaseContext().getString(R.string.session_loaded);
    }

    @Override
    public void onPostExecute(Operation<AlfrescoSession> task, AlfrescoSession results)
    {
        saveData(task, results);
        super.onPostExecute(task, results);
    }

    @Override
    public void onError(Operation<AlfrescoSession> task, Exception e)
    {
        LoadSessionTask loadingTask = ((LoadSessionTask) task);
        Log.e(TAG, Log.getStackTraceString(e));

        switch (loadingTask.getAccount().getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                saveData(task, null);
                CloudExceptionUtils.handleCloudException(context, loadingTask.getAccount().getId(), e, true);
                break;
            case Account.TYPE_ALFRESCO_TEST_BASIC:
            case Account.TYPE_ALFRESCO_CMIS:
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
                broadcastIntent
                        .putExtra(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                broadcastIntent.putExtra(SimpleAlertDialogFragment.PARAM_MESSAGE,
                        SessionExceptionHelper.getMessageId(context, e));
                if (loadingTask.getAccount() != null)
                {
                    broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, loadingTask.getAccount().getId());
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                break;
            default:
                break;
        }
        super.onError(task, e);
    }

    private void saveData(Operation<AlfrescoSession> task, AlfrescoSession session)
    {
        LoadSessionTask loadingTask = ((LoadSessionTask) task);
        Account acc = loadingTask.getAccount();
        
        //Save Session for reuse purpose
        if (session != null)
        {
            ApplicationManager.getInstance(getBaseContext()).saveSession(acc, session);
        }
        
        //For cloud session, try to save the latest version of oauthdata
        if (loadingTask.getOAuthData() == null) return;
        
        switch (loadingTask.getAccount().getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                int updated = context.getContentResolver().update(
                        AccountProvider.getUri(acc.getId()),
                        AccountProvider.createContentValues(acc.getDescription(), acc.getUrl(), acc.getUsername(), acc
                                .getPassword(), acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null,
                                loadingTask.getOAuthData().getAccessToken(), loadingTask.getOAuthData()
                                        .getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0), null, null);

                acc = AccountProvider.retrieveAccount(context, acc.getId());

                if (updated != 1)
                {
                    Log.e(TAG, "Error during saving oauth data");
                }
                break;
            default:
                // Do nothing
                break;
        }
    }
}

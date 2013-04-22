package org.alfresco.mobile.android.application.integration.account;

import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.integration.Operation;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationCallback;
import org.alfresco.mobile.android.application.manager.ActionManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class CreateAccountCallBack extends AbstractOperationCallback<Account>
{
    private static final String TAG = CreateAccountCallBack.class.getName();

    public CreateAccountCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.account_verify);
        complete = getBaseContext().getString(R.string.account_wizard_alldone_description);
    }

    @Override
    public void onPostExecute(Operation<Account> task, Account account)
    {
        super.onPostExecute(task, account);

        CreateAccountTask createTask = ((CreateAccountTask) task);

        ApplicationManager.getInstance(context).saveSession(account, createTask.getSession());

    }

    @Override
    public void onError(Operation<Account> task, Exception e)
    {
        Log.d(TAG, Log.getStackTraceString(e));
        
        Bundle b = new Bundle();
        b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
        b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_title);
        b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
        b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, SessionExceptionHelper.getMessageId(context, e));
        ActionManager.actionDisplayDialog(context, b);
        
        super.onError(task, e);
    }
}

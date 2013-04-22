package org.alfresco.mobile.android.application.integration.account;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountProvider;
import org.alfresco.mobile.android.application.integration.OperationRequest;
import org.alfresco.mobile.android.application.integration.impl.AbstractOperationTask;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;

import android.content.Context;
import android.content.Intent;

public class LoadSessionTask extends AbstractOperationTask<AlfrescoSession>
{
    private Account account;

    private OAuthData oauthData;

    public LoadSessionTask(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof LoadSessionRequest)
        {
            oauthData = ((LoadSessionRequest) request).getData();
        }
    }

    @Override
    protected LoaderResult<AlfrescoSession> doInBackground(Void... params)
    {
        LoaderResult<AlfrescoSession> result = new LoaderResult<AlfrescoSession>();
        AlfrescoSession repoSession = null;
        LoadSessionHelper sHelper = null;
        try
        {
            // Retrieve informations about the account.
            account = AccountProvider.retrieveAccount(context, accountId);
            if (listener != null)
            {
                listener.onPreExecute(this);
            }
            
            sHelper = new LoadSessionHelper(context, account, oauthData);
            repoSession = sHelper.requestSession();
            oauthData = sHelper.getOAuthData();
        }
        catch (Exception e)
        {
            if (sHelper != null){
                oauthData = sHelper.getOAuthData();
            }
            result.setException(e);
        }

        result.setData(repoSession);

        return result;
    }

    public OAuthData getOAuthData()
    {
        return oauthData;
    }

    public Account getAccount()
    {
        return account;
    }
    
    
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        return broadcastIntent;
    }
    
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        return broadcastIntent;
    }

}

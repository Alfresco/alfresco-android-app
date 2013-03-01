/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.integration.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

@TargetApi(11)
/**
 * AccountLoginLoaderCallback is responsible to update the UI after session has been created.
 * @author Jean Marie Pascal
 *
 */
public class AccountLoginLoaderCallback extends AbstractSessionCallback
{

    private static final String TAG = "AccountLoginLoaderCallback";

    private Account acc;

    private ProgressDialog mProgressDialog;

    public AccountLoginLoaderCallback(Activity activity, Account acc)
    {
        this.activity = activity;
        this.acc = acc;
    }

    public AccountLoginLoaderCallback(Activity activity, Account acc, OAuthData data)
    {
        this.activity = activity;
        this.acc = acc;
        this.data = data;
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {

        Loader<LoaderResult<AlfrescoSession>> loader = null;
        if (data != null)
        {
            mProgressDialog = ProgressDialog.show(activity, getText(R.string.wait_title),
                    getText(R.string.wait_message), true, true, new OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            activity.getLoaderManager().destroyLoader(id);
                        }
                    });
            loader = getSessionLoader(new AccountSettingsHelper(activity, acc, data));
        }
        else
        {
            loader = getSessionLoader(new AccountSettingsHelper(activity, acc));
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> loader, LoaderResult<AlfrescoSession> results)
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        if (!results.hasException())
        {
            saveNewOauthData(loader);

            // Save latest position as default future one
            final SharedPreferences settings = activity.getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            if (settings != null)
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong(AccountsPreferences.ACCOUNT_DEFAULT, acc.getId());
                editor.commit();
            }
            
            activity.getLoaderManager().destroyLoader(loader.getId());
            SessionUtils.setsession(activity, results.getData());
            SessionUtils.setRenditionManager(activity, null);
            Intent i = new Intent(activity, getActivityClass());
            i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
            activity.startActivity(i);
        }
        else
        {
            // In case of import. If something goes wrong We display an error
            // message to alert the user.
            // The user must open the main application to resolve the problem.
            if (activity instanceof PublicDispatcherActivity)
            {
                SessionUtils.setsession(activity, null);
                Intent i = new Intent(activity, getActivityClass());
                i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
                activity.startActivity(i);
                MessengerManager.showLongToast(activity, activity.getString(R.string.error_import));
                return;
            }

            switch ((int) acc.getTypeId())
            {
                case Account.TYPE_ALFRESCO_TEST_OAUTH:
                case Account.TYPE_ALFRESCO_CLOUD:

                    CloudExceptionUtils.handleCloudException(activity, results.getException(), true);

                    break;
                case Account.TYPE_ALFRESCO_TEST_BASIC:
                case Account.TYPE_ALFRESCO_CMIS:
                    Exception e = results.getException();
                    Bundle b = new Bundle();
                    b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
                    b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
                    b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
                    b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, SessionExceptionHelper.getMessageId(activity, e));
                    ActionManager.actionDisplayDialog(activity, b);
                    if (activity instanceof MainActivity)
                    {
                        ((MainActivity) activity).setSessionErrorMessageId(SessionExceptionHelper.getMessageId(activity, e));
                    }
                    break;
                default:
                    MessengerManager.showLongToast(activity, getText(R.string.error_session_creation));
                    Intent i = new Intent(activity, getActivityClass());
                    i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
                    activity.startActivity(i);
                    break;
            }
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        }
        activity.setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<AlfrescoSession>> loader)
    {
        saveNewOauthData(loader);

        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
    }

    private void saveNewOauthData(Loader<LoaderResult<AlfrescoSession>> loader)
    {
        Log.d(TAG, loader.toString());
        switch ((int) acc.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                AccountDAO accountDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
                if (accountDao.update(acc.getId(), acc.getDescription(), acc.getUrl(), acc.getUsername(), acc
                        .getPassword(), acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null,
                        ((CloudSessionLoader) loader).getOAuthData().getAccessToken(), ((CloudSessionLoader) loader)
                                .getOAuthData().getRefreshToken(), acc.getIsPaidAccount() ? 1 : 0))
                {
                    SessionUtils.setAccount(activity, accountDao.findById(acc.getId()));
                }
                else
                {
                    MessengerManager.showLongToast(activity, activity.getString(R.string.error_refresh_token));
                }
                break;
        }
    }

    /**
     * Helper method to retrieve the class name associated to the parent
     * activity.
     * 
     * @return Class object of the parent activity.
     */
    @SuppressWarnings("rawtypes")
    private Class getActivityClass()
    {
        if (activity instanceof MainActivity)
        {
            return MainActivity.class;
        }
        else if (activity instanceof PublicDispatcherActivity) { return PublicDispatcherActivity.class; }
        return null;
    }

}

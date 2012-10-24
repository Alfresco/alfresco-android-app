/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import org.alfresco.mobile.android.api.asynchronous.OAuthAccessTokenLoader;
import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.oauth.OAuthFragment;
import org.alfresco.mobile.android.ui.oauth.listener.OnOAuthAccessTokenListener;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WizardOAuthAppFragment extends OAuthFragment
{
    public static final String TAG = "OAuthAppFragment";

    private ProgressDialog mProgressDialog;

    public static WizardOAuthAppFragment newInstance()
    {
        WizardOAuthAppFragment bf = getOAuthFragment();
        Bundle b = createBundleArgs(R.layout.sdkapp_wizard_account_step2_cloud);
        bf.setArguments(b);
        return bf;
    }

    public WizardOAuthAppFragment()
    {
    }

    public WizardOAuthAppFragment(String oauthUrl, String apikey, String apiSecret)
    {
        super(oauthUrl, apikey, apiSecret);
    }

    public static WizardOAuthAppFragment getOAuthFragment()
    {
        String oauthUrl = null, apikey = null, apisecret = null;
        Bundle b = SessionSettingsHelper.getOAuthSettings();
        if (b != null)
        {
            oauthUrl = b.getString(SessionSettingsHelper.OAUTH_URL);
            apikey = b.getString(SessionSettingsHelper.OAUTH_API_KEY);
            apisecret = b.getString(SessionSettingsHelper.OAUTH_API_SECRET);
        }

        WizardOAuthAppFragment oauthFragment = null;
        if (oauthUrl == null || oauthUrl.isEmpty())
        {
            oauthFragment = new WizardOAuthAppFragment();
        }
        else
        {
            oauthFragment = new WizardOAuthAppFragment(oauthUrl, apikey, apisecret);
        }
        return oauthFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        setOnOAuthAccessTokenListener(new OnOAuthAccessTokenListener()
        {

            @Override
            public void failedRequestAccessToken(Exception e)
            {
                mProgressDialog.dismiss();
                MessengerManager.showLongToast(getActivity(), e.getMessage());
            }

            @Override
            public void beforeRequestAccessToken(Bundle b)
            {
                mProgressDialog = ProgressDialog.show(getActivity(), getText(R.string.dialog_wait),
                        getText(R.string.validation_creadentials), true, true, new OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                getLoaderManager().destroyLoader(OAuthAccessTokenLoader.ID);
                            }
                        });
            }

            @Override
            public void afterRequestAccessToken(OAuthData result)
            {
                mProgressDialog.dismiss();
                load(result);
            }
        });

        return v;
    }

    public void load(OAuthData oauthData)
    {
        AccountLoaderCallback call = new AccountLoaderCallback(getActivity(), this, oauthData);
        LoaderManager lm = getLoaderManager();
        lm.restartLoader(SessionLoader.ID, null, call);
        lm.getLoader(SessionLoader.ID).forceLoad();
    }
}

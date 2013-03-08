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

import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.oauth.OAuthFragment;
import org.alfresco.mobile.android.ui.oauth.listener.OnOAuthAccessTokenListener;

import android.app.LoaderManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

public class AccountOAuthFragment extends OAuthFragment
{
    public static final String TAG = "AccountOAuthFragment";

    private static final String PARAM_ACCOUNT = "account";

    public static AccountOAuthFragment newInstance()
    {
        AccountOAuthFragment bf = getOAuthFragment(null);
        Bundle b = createBundleArgs(R.layout.app_wizard_account_step2_cloud);
        bf.setArguments(b);
        return bf;
    }

    public static AccountOAuthFragment newInstance(Account account)
    {
        AccountOAuthFragment bf = getOAuthFragment(account);
        Bundle b = createBundleArgs(R.layout.app_account_authentication);
        b.putSerializable(PARAM_ACCOUNT, account);
        bf.setArguments(b);
        return bf;
    }

    public AccountOAuthFragment()
    {
    }

    public AccountOAuthFragment(String oauthUrl, String apikey, String apiSecret)
    {
        super(oauthUrl, apikey, apiSecret);
    }

    public static AccountOAuthFragment getOAuthFragment(Account account)
    {
        String oauthUrl = null, apikey = null, apisecret = null;
        Bundle b = AccountSettingsHelper.getOAuthSettings();
        if (b != null)
        {
            oauthUrl = b.getString(AccountSettingsHelper.OAUTH_URL);
            apikey = b.getString(AccountSettingsHelper.OAUTH_API_KEY);
            apisecret = b.getString(AccountSettingsHelper.OAUTH_API_SECRET);
        }

        if (account != null)
        {
            String tmpOauthUrl = account.getUrl();
            if (!tmpOauthUrl.equals(oauthUrl))
            {
                oauthUrl = tmpOauthUrl;
                apikey = null;
                apisecret = null;
            }
        }

        AccountOAuthFragment oauthFragment = null;
        if (oauthUrl == null || oauthUrl.isEmpty())
        {
            oauthFragment = new AccountOAuthFragment();
        }
        else
        {
            oauthFragment = new AccountOAuthFragment(oauthUrl, apikey, apisecret);
        }
        return oauthFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.account_wizard_step2_title);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            getActivity().getActionBar().show();
            getActivity().setTitle(R.string.account_wizard_step2_title);
        }

        final View v = super.onCreateView(inflater, container, savedInstanceState);

        setOnOAuthAccessTokenListener(new OnOAuthAccessTokenListener()
        {

            @Override
            public void failedRequestAccessToken(Exception e)
            {
                if (DisplayUtils.hasCentralPane(getActivity()))
                {
                    ((MainActivity) getActivity()).clearScreen();
                }
                else
                {
                    getActivity().getFragmentManager().popBackStack();
                }

                Log.e(TAG, Log.getStackTraceString(e));
                MessengerManager.showLongToast(getActivity(), getActivity().getString(R.string.error_general));
            }

            @Override
            public void beforeRequestAccessToken(Bundle b)
            {
                if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
                {
                    new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
                }
            }

            @Override
            public void afterRequestAccessToken(OAuthData result)
            {
                load(result);
            }
        });

        final View waiting = v.findViewById(R.id.waiting);

        setOnOAuthWebViewListener(new OnOAuthWebViewListener()
        {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                if (waiting != null)
                {
                    waiting.setVisibility(View.GONE);
                }
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                 waiting.setVisibility(View.VISIBLE);
            }

        });

        return v;
    }

    public void load(OAuthData oauthData)
    {
        if (oauthData == null)
        {
            ActionManager.actionDisplayError(this, null);
            return;
        }

        AbstractSessionCallback call = null;
        if (getArguments().containsKey(PARAM_ACCOUNT))
        {
            call = new AccountLoginLoaderCallback(getActivity(), (Account) getArguments()
                    .getSerializable(PARAM_ACCOUNT), oauthData);
        }
        else
        {
            call = new AccountCreationLoaderCallback(getActivity(), this, oauthData);
        }
        LoaderManager lm = getLoaderManager();
        lm.restartLoader(SessionLoader.ID, null, call);
    }

    @Override
    public void onStart()
    {
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

}

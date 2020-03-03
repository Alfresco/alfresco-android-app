/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.alfresco.mobile.android.application.fragments.signin;

import java.util.Map;

import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.CreateAccountRequest;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.oauth.OAuthFragment;
import org.alfresco.mobile.android.ui.oauth.OnOAuthAccessTokenListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.squareup.otto.Subscribe;

public class AccountOAuthFragment extends OAuthFragment implements AnalyticsManager.FragmentAnalyzed
{
    public static final String TAG = "AccountOAuthFragment";

    private static final String ARGUMENT_ACCOUNT = "account";

    public static final String OAUTH_URL = "oauth_url";

    public static final String OAUTH_API_KEY = "apikey";

    public static final String OAUTH_API_SECRET = "apisecret";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountOAuthFragment()
    {
    }

    public static AccountOAuthFragment newInstance(Context context)
    {
        AccountOAuthFragment bf = getOAuthFragment(context, null);
        Bundle b = createBundleArgs(R.layout.fr_oauth_cloud);
        bf.setArguments(b);
        return bf;
    }

    public static AccountOAuthFragment newInstance(Context context, AlfrescoAccount account)
    {
        AccountOAuthFragment bf = getOAuthFragment(context, account);
        Bundle b = createBundleArgs(R.layout.fr_oauth_refresh);
        b.putSerializable(ARGUMENT_ACCOUNT, account);
        bf.setArguments(b);
        return bf;
    }

    public AccountOAuthFragment(String oauthUrl, String apikey, String apiSecret)
    {
        super(oauthUrl, apikey, apiSecret);
    }

    public static AccountOAuthFragment getOAuthFragment(Context context, AlfrescoAccount account)
    {
        String oauthUrl = null, apikey = null, apisecret = null;
        Bundle b = SessionManager.getInstance(context).getOAuthSettings();
        if (b != null && !b.isEmpty())
        {
            oauthUrl = b.getString(OAUTH_URL);
            apikey = b.getString(OAUTH_API_KEY);
            apisecret = b.getString(OAUTH_API_SECRET);
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

        AccountOAuthFragment oauthFragment;
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

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = super.onCreateView(inflater, container, savedInstanceState);
        setOnOAuthAccessTokenListener(new OnOAuthAccessTokenListener()
        {

            @Override
            public void failedRequestAccessToken(Exception e)
            {
                retryOAuthAuthentication();
                Log.e(TAG, Log.getStackTraceString(e));
            }

            @Override
            public void beforeRequestAccessToken(Bundle b)
            {
                if (getActivity() instanceof BaseActivity)
                {
                    ((BaseActivity) getActivity()).displayWaitingDialog();
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
                if (url.startsWith(callback.replace("http", "https")))
                {
                    if (waiting != null)
                    {
                        waiting.setVisibility(View.VISIBLE);
                    }
                    view.setVisibility(View.GONE);
                }
                else if (OAuthConstant.PUBLIC_API_OAUTH_AUTHORIZE_URL.equals(url))
                {
                    retryOAuthAuthentication();
                    if (waiting != null)
                    {
                        waiting.setVisibility(View.VISIBLE);
                    }
                    view.setVisibility(View.GONE);
                }
                else
                {
                    if (waiting != null)
                    {
                        waiting.setVisibility(View.GONE);
                    }
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                waiting.setVisibility(View.VISIBLE);
            }

        });

        return v;
    }

    @Override
    public void onStart()
    {
        getActivity().invalidateOptionsMenu();
        super.onStart();
    }

    @Subscribe
    public void onRetrieveOAuthDataEvent(RetrieveOAuthDataEvent event)
    {
        super.onRetrieveOAuthDataEvent(event);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////////////////////////
    public void load(OAuthData oauthData)
    {
        if (oauthData == null)
        {
            ActionUtils.actionDisplayError(this, null);
            return;
        }

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_ACCOUNT))
        {
            // TODO Replace by SessionMAnager
            EventBusManager.getInstance().post(new RequestSessionEvent(
                    (AlfrescoAccount) getArguments().getSerializable(ARGUMENT_ACCOUNT), oauthData));
        }
        else
        {
            Operator.with(getActivity()).load(new CreateAccountRequest.Builder(oauthData));
        }

        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).displayWaitingDialog();
        }

    }

    private void retryOAuthAuthentication()
    {
        reload();
        AlfrescoNotificationManager.getInstance(getActivity())
                .showLongToast(getActivity().getString(R.string.error_general));
    }

    private void retryOAuthAuthentication(Exception e)
    {
        reload();
        AlfrescoNotificationManager.getInstance(getActivity())
                .showLongToast(getString(AlfrescoExceptionHelper.getMessageId(getActivity(), e)));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onAccountCreated(CreateAccountEvent event)
    {
        ((BaseActivity) getActivity()).removeWaitingDialog();

        if (event.hasException)
        {
            getActivity().getSupportFragmentManager().popBackStack();
            retryOAuthAuthentication(event.exception);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(GeneralPreferences.HAS_SHOWN_SHUTTING_DOWN_ALERT, 0).apply();

        if (getActivity() instanceof WelcomeActivity)
        {
            AlfrescoAccount acc = event.data;

            if (acc.getIsPaidAccount() && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
            }
            AccountNameFragment.with(getActivity()).accountId(acc.getId()).back(false).display();
            return;
        }
    }

    @Override
    public String getScreenName()
    {
        return AnalyticsManager.SCREEN_ACCOUNT_OAUTH;
    }

    @Override
    public boolean reportAtCreationEnable()
    {
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        private AlfrescoAccount account;

        private boolean isCreation = false;

        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(FragmentActivity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(FragmentActivity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder account(AlfrescoAccount account)
        {
            this.account = account;
            return this;
        }

        public Builder isCreation(boolean isCreation)
        {
            this.isCreation = isCreation;
            return this;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            if (isCreation)
            {
                return newInstance(getActivity());
            }
            else
            {
                return newInstance(getActivity(), account);
            }
        }
    }
}

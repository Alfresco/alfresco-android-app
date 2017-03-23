/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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

import org.alfresco.mobile.android.api.constants.SAMLConstant;
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.api.session.authentication.impl.Saml2AuthHelper;
import org.alfresco.mobile.android.api.session.authentication.impl.SamlDataImpl;
import org.alfresco.mobile.android.api.utils.JsonUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PrivateDialogActivity;
import org.alfresco.mobile.android.application.activity.WelcomeActivity;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.NotificationManager;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CreateAccountEvent;
import org.alfresco.mobile.android.async.account.CreateAccountRequest;
import org.alfresco.mobile.android.async.account.URLInfo;
import org.alfresco.mobile.android.async.session.RequestSessionEvent;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.activity.AlfrescoAppCompatActivity;

import com.squareup.otto.Subscribe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AccountSigninSamlFragment extends DialogFragment implements AnalyticsManager.FragmentAnalyzed
{
    public static final String TAG = AccountSigninSamlFragment.class.getName();

    public static final String LAYOUT_ID = "OAuthLayoutId";

    protected static final String ARGUMENT_ACCOUNT = "account";

    protected static final String ARGUMENT_BASE_URL = "urlInfo";

    protected int layout_id = org.alfresco.mobile.android.foundation.R.layout.app_webview;

    protected URLInfo urlInfo;

    protected WebView webview;

    protected Saml2AuthHelper helper;

    private AlfrescoAccount account;

    private String lastUrl;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountSigninSamlFragment()
    {
    }

    public static AccountSigninSamlFragment newInstance(Context context, Bundle bundle)
    {
        AccountSigninSamlFragment bf = getSamlFragment(context, null, null);
        Bundle b = createBundleArgs(R.layout.fr_oauth_cloud);
        b.putAll(bundle);
        bf.setArguments(b);
        return bf;
    }

    public static AccountSigninSamlFragment newInstance(Context context, AlfrescoAccount account)
    {
        AccountSigninSamlFragment bf = getSamlFragment(context, account, null);
        Bundle b = createBundleArgs(R.layout.fr_oauth_cloud);
        b.putSerializable(ARGUMENT_ACCOUNT, account);
        bf.setArguments(b);
        return bf;
    }

    public static AccountSigninSamlFragment newInstance(Context context, String baseUrl)
    {
        AccountSigninSamlFragment bf = getSamlFragment(context, null, baseUrl);
        Bundle b = createBundleArgs(R.layout.fr_oauth_refresh);
        b.putSerializable(ARGUMENT_BASE_URL, baseUrl);
        bf.setArguments(b);
        return bf;
    }

    public static Bundle createBundleArgs(int layoutId)
    {
        Bundle args = new Bundle();
        args.putInt(LAYOUT_ID, layoutId);
        return args;
    }

    public static AccountSigninSamlFragment getSamlFragment(Context context, AlfrescoAccount account, String baseAlfUrl)
    {
        String baseUrl = baseAlfUrl;
        if (account != null)
        {
            String tmpOauthUrl = account.getUrl();
            if (!tmpOauthUrl.equals(baseUrl))
            {
                baseUrl = tmpOauthUrl;
            }
        }

        AccountSigninSamlFragment samlFragment;
        if (baseUrl == null || baseUrl.isEmpty())
        {
            samlFragment = new AccountSigninSamlFragment();
        }
        else
        {
            samlFragment = new AccountSigninSamlFragment();
        }
        return samlFragment;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }

        EventBusManager.getInstance().register(this);

        if (getArguments() != null)
        {
            layout_id = BundleUtils.getInt(getArguments(), LAYOUT_ID);
            if (getArguments().containsKey(ARGUMENT_BASE_URL))
            {
                urlInfo = (URLInfo) BundleUtils.getSerializable(getArguments(), ARGUMENT_BASE_URL);
            }
            if (getArguments().containsKey(ARGUMENT_ACCOUNT))
            {
                account = (AlfrescoAccount) BundleUtils.getSerializable(getArguments(), ARGUMENT_ACCOUNT);
                NotificationManager.getInstance(getActivity()).showInfoCrouton(getActivity(),
                        R.string.error_session_expired);
            }

            if (urlInfo != null)
            {
                helper = new Saml2AuthHelper(urlInfo.baseUrl);
            }
            else if (account != null)
            {
                helper = new Saml2AuthHelper(account.getUrl());
            }
        }

        View v = inflater.inflate(layout_id, container, false);

        webview = (WebView) v.findViewById(org.alfresco.mobile.android.foundation.R.id.webview);
        webview.clearCache(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        // webview.getSettings().setUseWideViewPort(true);

        final View waiting = v.findViewById(R.id.waiting);

        if (AndroidVersion.isLollipopOrAbove())
        {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webview.setWebChromeClient(new WebChromeClient()
        {
            public boolean onConsoleMessage(ConsoleMessage cmsg)
            {
                String message = cmsg.message().trim();

                if (checkAuthResponseURLResponse(lastUrl))
                {
                    if (message.startsWith("{"))
                    {
                        Map<String, Object> json = JsonUtils.parseObject(message);
                        SamlData data = new SamlDataImpl(null, urlInfo != null ? urlInfo.samlData : null);
                        ((SamlDataImpl) data).setSamlTicket(json);
                        load(data);
                    }
                }
                return true;
            }
        });

        webview.setWebViewClient(new WebViewClient()
        {

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
            {
                Log.d("SAML", "onReceivedHttpError: " + lastUrl);
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
                Log.d("SAML", "onReceivedError: " + lastUrl);
                super.onReceivedError(view, request, error);
                if (checkAuthResponseURLResponse(lastUrl))
                {
                    view.loadUrl(helper.getAuthenticateUrl());
                    return;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                lastUrl = url;
                if (url.endsWith(SAMLConstant.SMALV2_RESTAPI_AUTHENTICATE_PATH))
                {
                    waiting.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                    super.onPageFinished(view, url);
                }
                else if (checkAuthResponseURLResponse(url))
                {
                    view.setVisibility(View.GONE);
                    waiting.setVisibility(View.VISIBLE);
                    Log.d("SAML", "onPageFinished: Get Info - " + url);
                    view.loadUrl("javascript:console.log(document.body.getElementsByTagName('pre')[0].innerHTML);");
                }
                else
                {
                    waiting.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                    super.onPageFinished(view, url);
                }

                if (getActivity() != null)
                {
                    if (getActivity() instanceof AlfrescoActivity)
                    {
                        ((AlfrescoActivity) getActivity()).removeWaitingDialog();
                    }
                    else if (getActivity() instanceof AlfrescoAppCompatActivity)
                    {
                        ((AlfrescoAppCompatActivity) getActivity()).removeWaitingDialog();
                    }
                }
            }
        });

        webview.loadUrl(helper.getAuthenticateUrl());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop()
    {
        EventBusManager.getInstance().unregister(this);
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    public void load(SamlData samlData)
    {
        if (samlData == null)
        {
            ActionUtils.actionDisplayError(this, null);
            return;
        }

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_ACCOUNT))
        {
            // Update account across managers
            AlfrescoAccountManager.getInstance(getActivity()).setSamlToken(account.getId(), samlData.getTicket());
            AlfrescoAccountManager.getInstance(getActivity()).update(account.getId(), AlfrescoAccount.ACCOUNT_USERNAME,
                    samlData.getUserId());
            SessionManager.getInstance(getActivity())
                    .saveAccount(AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(account.getId()));
            SessionManager.getInstance(getActivity()).saveSession(null);


            // TODO Replace by SessionMAnager
            EventBusManager.getInstance().post(new RequestSessionEvent(
                    AlfrescoAccountManager.getInstance(getActivity()).retrieveAccount(account.getId()), samlData,
                    getActivity() instanceof MainActivity));

            if (getActivity() instanceof PrivateDialogActivity)
            {
                getActivity().finish();
            }
        }
        else
        {
            Operator.with(getActivity()).load(new CreateAccountRequest.Builder(urlInfo.baseUrl, samlData));
        }

        if (getActivity() instanceof BaseActivity)
        {
            ((BaseActivity) getActivity()).displayWaitingDialog();
        }
    }

    private void retryAuthentication(Exception e)
    {
        reload();
        AlfrescoNotificationManager.getInstance(getActivity())
                .showLongToast(getString(AlfrescoExceptionHelper.getMessageId(getActivity(), e)));
    }

    protected void reload()
    {
        helper = new Saml2AuthHelper(urlInfo.baseUrl);
        webview.loadUrl(helper.getAuthenticateUrl());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    protected boolean checkAuthResponseURLResponse(String receivedUrl)
    {
        if (receivedUrl == null) { return false; }
        String authResponseURL = helper.getAuthenticateUrl();

        // If HTTP we check the response can be HTTPS
        if (authResponseURL.startsWith("http") && receivedUrl.startsWith("https"))
        {
            return receivedUrl.contains(helper.getHostBaseUrl())
                    && receivedUrl.contains(SAMLConstant.SMALV2_RESTAPI_AUTHENTICATE_RESPONSE_PATH);
        }
        else if (receivedUrl.startsWith(authResponseURL)) { return true; }

        return false;
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
            retryAuthentication(event.exception);
            return;
        }

        if (getActivity() instanceof WelcomeActivity)
        {
            AlfrescoAccount acc = event.data;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (acc.getIsPaidAccount() && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).apply();
            }
            // Display Account Name
            AccountNameFragment.with(getActivity()).accountId(acc.getId()).back(false).display();
            return;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ANALYTICS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public String getScreenName()
    {
        return AnalyticsManager.SCREEN_ACCOUNT_SAML;
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

        private URLInfo urlInfo;

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
            extraConfiguration.putSerializable(ARGUMENT_ACCOUNT, account);
            return this;
        }

        public Builder urlInfo(URLInfo urlInfo)
        {
            extraConfiguration.putSerializable(ARGUMENT_BASE_URL, urlInfo);
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
                return newInstance(getActivity(), b);
            }
            else
            {
                return newInstance(getActivity(), account);
            }
        }
    }
}

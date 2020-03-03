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
package org.alfresco.mobile.android.ui.oauth;

import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataEvent;
import org.alfresco.mobile.android.async.session.oauth.RetrieveOAuthDataRequest;
import org.alfresco.mobile.android.foundation.R;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.otto.Subscribe;

public abstract class OAuthFragment extends DialogFragment
{

    public static final String TAG = "OAuthFragment";

    public static final String LAYOUT_ID = "OAuthLayoutId";

    private String apiKey;

    private String apiSecret;

    protected String callback;

    private String scope;

    protected int layout_id = R.layout.app_webview;

    private String baseOAuthUrl = OAuthConstant.PUBLIC_API_HOSTNAME;

    private OnOAuthAccessTokenListener onOAuthAccessTokenListener;

    private OnOAuthWebViewListener onOAuthWebViewListener;

    private boolean isLoaded;

    protected WebView webview;

    public OAuthFragment()
    {
    }

    public OAuthFragment(String baseOAuthUrl, String oauth_api_key, String oauth_api_secret)
    {
        this.baseOAuthUrl = baseOAuthUrl;
        this.apiKey = oauth_api_key;
        this.apiSecret = oauth_api_secret;
    }

    public static Bundle createBundleArgs(int layoutId)
    {
        Bundle args = new Bundle();
        args.putInt(LAYOUT_ID, layoutId);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null) { return null; }

        EventBusManager.getInstance().register(this);

        if (getArguments() != null && getArguments().containsKey(LAYOUT_ID))
        {
            layout_id = getArguments().getInt(LAYOUT_ID);
        }

        View v = inflater.inflate(layout_id, container, false);

        if (this.apiKey == null)
        {
            this.apiKey = getText(R.string.oauth_api_key).toString();
        }
        if (this.apiSecret == null)
        {
            this.apiSecret = getText(R.string.oauth_api_secret).toString();
        }
        if (this.callback == null)
        {
            this.callback = getText(R.string.oauth_callback).toString();
        }
        if (this.scope == null)
        {
            this.scope = getText(R.string.oauth_scope).toString();
        }

        webview = (WebView) v.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        if (AndroidVersion.isLollipopOrAbove())
        {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        final FragmentActivity activity = getActivity();
        webview.setWebChromeClient(new WebChromeClient()
        {
            public void onProgressChanged(WebView view, int progress)
            {
                // Activities and WebViews measure progress with different
                // scales.The progress meter will automatically disappear when
                // we reach 100%
                activity.setProgress(progress * 100);
            }
        });

        // attach WebViewClient to intercept the callback url
        webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                // check for our custom callback protocol
                if (!isLoaded)
                {
                    onCodeUrl(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                if (!isLoaded)
                {
                    onCodeUrl(url);
                }
                if (onOAuthWebViewListener != null)
                {
                    onOAuthWebViewListener.onPageStarted(webview, url, favicon);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                if (onOAuthWebViewListener != null)
                {
                    onOAuthWebViewListener.onPageFinished(webview, url);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (onOAuthWebViewListener != null)
                {
                    onOAuthWebViewListener.onReceivedError(webview, errorCode, description, failingUrl);
                }
            }

        });

        OAuthHelper helper = new OAuthHelper(baseOAuthUrl);
        // Log.d("OAUTH URL", helper.getAuthorizationUrl(apiKey, callback,
        // scope));
        // send user to authorization page
        webview.loadUrl(helper.getAuthorizationUrl(apiKey, callback, scope));

        return v;
    }

    private void onCodeUrl(String url)
    {
        // check for our custom callback protocol
        if (url.startsWith(getText(R.string.oauth_callback).toString()))
        {
            isLoaded = true;

            // authorization complete hide webview for now & retrieve
            // the acces token
            String code = OAuthHelper.retrieveCode(url);
            if (!TextUtils.isEmpty(code))
            {
                retrieveAccessToken(code);
            }
            else
            {
                if (onOAuthAccessTokenListener != null)
                {
                    onOAuthAccessTokenListener.failedRequestAccessToken(
                            new AlfrescoSessionException(ErrorCodeRegistry.SESSION_AUTH_CODE_INVALID,
                                    Messagesl18n.getString("ErrorCodeRegistry.SESSION_AUTH_CODE_INVALID")));
                }
            }
        }
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

    public void retrieveAccessToken(String code)
    {
        if (onOAuthAccessTokenListener != null)
        {
            onOAuthAccessTokenListener.beforeRequestAccessToken(new Bundle());
        }

        Operator.with(getActivity()).load(new RetrieveOAuthDataRequest.Builder(
                RetrieveOAuthDataRequest.OPERATION_ACCESS_TOKEN, baseOAuthUrl, code, apiKey, apiSecret, callback));
    }

    @Subscribe
    public void onRetrieveOAuthDataEvent(RetrieveOAuthDataEvent event)
    {

        if (onOAuthAccessTokenListener != null)
        {
            if (event.hasException || event.data == null)
            {
                onOAuthAccessTokenListener.failedRequestAccessToken(event.exception);
            }
            else
            {
                onOAuthAccessTokenListener.afterRequestAccessToken(event.data);
            }
        }
        else
        {
            if (event.hasException)
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(event.exception.getMessage());
            }
            else
            {
                AlfrescoNotificationManager.getInstance(getActivity()).showLongToast(event.data.toString());
            }
        }
    }

    public void setOnOAuthAccessTokenListener(OnOAuthAccessTokenListener onOAuthAccessTokenListener)
    {
        this.onOAuthAccessTokenListener = onOAuthAccessTokenListener;
    }

    public void setOnOAuthWebViewListener(OnOAuthWebViewListener onOAuthWebViewListener)
    {
        this.onOAuthWebViewListener = onOAuthWebViewListener;
    }

    public interface OnOAuthWebViewListener
    {
        void onPageStarted(WebView view, String url, Bitmap favicon);

        void onPageFinished(WebView view, String url);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    }

    protected void reload()
    {
        OAuthHelper helper = new OAuthHelper(baseOAuthUrl);
        webview.loadUrl(helper.getAuthorizationUrl(apiKey, callback, scope));
    }
}

/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.asynchronous.OAuthAccessTokenLoader;
import org.alfresco.mobile.android.api.constants.OAuthConstant;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuthHelper;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.oauth.listener.OnOAuthAccessTokenListener;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public abstract class OAuthFragment extends DialogFragment implements LoaderCallbacks<LoaderResult<OAuthData>>
{

    public static final String TAG = "AppOAuthFragment";

    public static final String LAYOUT_ID = "OAuthLayoutId";

    private String apiKey;

    private String apiSecret;

    private String callback;

    private String scope;

    private String code;

    private int layout_id = R.layout.sdk_oauth;

    private String baseOAuthUrl = OAuthConstant.PUBLIC_API_HOSTNAME;

    private OnOAuthAccessTokenListener onOAuthAccessTokenListener;

    private OnOAuthWebViewListener onOAuthWebViewListener;

    private boolean isLoaded;

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

        final WebView webview = (WebView) v.findViewById(R.id.webview);

        final Activity activity = getActivity();
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
        Log.d("OAUTH URL", helper.getAuthorizationUrl(apiKey, callback, scope));
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
            code = OAuthHelper.retrieveCode(url);
            if (code != null)
            {
                retrieveAccessToken(code);
            }
            else
            {
                if (onOAuthAccessTokenListener != null)
                {
                    onOAuthAccessTokenListener.failedRequestAccessToken(new AlfrescoSessionException(
                            ErrorCodeRegistry.SESSION_AUTH_CODE_INVALID, Messagesl18n
                                    .getString("ErrorCodeRegistry.SESSION_AUTH_CODE_INVALID")));
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

    public void retrieveAccessToken(String code)
    {
        LoaderManager lm = getLoaderManager();
        Bundle b = new Bundle();
        b.putString(OAuthAccessTokenLoader.PARAM_CODE, code);
        b.putString(OAuthAccessTokenLoader.PARAM_APIKEY, apiKey);
        b.putString(OAuthAccessTokenLoader.PARAM_APISECRET, apiSecret);
        b.putString(OAuthAccessTokenLoader.PARAM_CALLBACK_URL, callback);
        b.putString(OAuthAccessTokenLoader.PARAM_BASEURL, baseOAuthUrl);
        b.putInt(OAuthAccessTokenLoader.PARAM_OPERATION, OAuthAccessTokenLoader.OPERATION_ACCESS_TOKEN);
        b.putString(OAuthAccessTokenLoader.PARAM_CODE, code);
        lm.restartLoader(OAuthAccessTokenLoader.ID, b, this);
    }

    @Override
    public Loader<LoaderResult<OAuthData>> onCreateLoader(final int id, Bundle bundle)
    {
        if (onOAuthAccessTokenListener != null)
        {
            onOAuthAccessTokenListener.beforeRequestAccessToken(bundle);
        }
        return new OAuthAccessTokenLoader(getActivity(), bundle);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<OAuthData>> arg0, LoaderResult<OAuthData> result)
    {

        if (onOAuthAccessTokenListener != null)
        {
            if (result.hasException())
            {
                onOAuthAccessTokenListener.failedRequestAccessToken(result.getException());
            }
            else
            {
                onOAuthAccessTokenListener.afterRequestAccessToken(result.getData());
            }
        }
        else
        {
            if (result.hasException())
            {
                MessengerManager.showLongToast(getActivity(), result.getException().getMessage());
            }
            else
            {
                MessengerManager.showLongToast(getActivity(), result.getData().toString());
            }
        }
    }

    @Override
    public void onLoaderReset(
            Loader<LoaderResult<org.alfresco.mobile.android.api.session.authentication.OAuthData>> arg0)
    {

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
}

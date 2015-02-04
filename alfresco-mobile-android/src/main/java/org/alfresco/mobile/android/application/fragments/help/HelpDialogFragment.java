/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.application.fragments.help;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelpDialogFragment extends DialogFragment
{
    public static final String TAG = HelpDialogFragment.class.getName();

    private boolean isDefault = false;

    private WebView webView;

    private String defaultUrl = null;

    private String rootUrl = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public static HelpDialogFragment newInstanceByTemplate(Bundle b)
    {
        HelpDialogFragment cbf = new HelpDialogFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.sdk_webview, container, false);

        webView = (WebView) v.findViewById(org.alfresco.mobile.android.foundation.R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = getActivity();

        defaultUrl = activity.getString(R.string.help_user_guide_default_url);

        webView.setWebViewClient(new WebViewClient() {
            boolean hasError = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                hasError = false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                // We redirect to default EN documentation if locale docs are
                // not available.
                if ((errorCode == ERROR_FILE_NOT_FOUND || errorCode == ERROR_HOST_LOOKUP) && !isDefault
                        && failingUrl.equals(rootUrl)) {
                    hasError = true;
                    view.loadUrl(defaultUrl);
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (hasError) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }

            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                resend.sendToTarget();
            }

        });

        webView.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });

        rootUrl = getUrl(activity);
        webView.loadUrl(rootUrl);

        return v;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private String getUrl(Activity activity)
    {
        String prefix = activity.getString(R.string.docs_prefix);
        String urlValue = null;
        if (TextUtils.isEmpty(prefix))
        {
            isDefault = true;
            urlValue = activity.getString(R.string.help_user_guide_default_url);
        }
        else
        {
            isDefault = false;
            urlValue = String.format(activity.getString(R.string.help_user_guide_url), prefix);
        }
        return urlValue;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(Activity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends LeafFragmentBuilder
    {
        // ///////////////////////////////////////////////////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////////////////////////////////////////////////
        public Builder(Activity activity)
        {
            super(activity);
            this.extraConfiguration = new Bundle();
        }

        public Builder(Activity appActivity, Map<String, Object> configuration)
        {
            super(appActivity, configuration);
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}
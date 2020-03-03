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
package org.alfresco.mobile.android.application.fragments.help;

import java.util.Map;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.MenuFragmentHelper;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.ui.RefreshFragment;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.activity.AlfrescoAppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class HelpDialogFragment extends DialogFragment implements RefreshFragment
{
    public static final String TAG = HelpDialogFragment.class.getName();

    private boolean isDefault = false;

    private WebView webView;

    private View emptyView;

    private TextView emptyTextView;

    private String defaultUrl = null;

    private String rootUrl = null;

    private MenuItem refreshIcon;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public HelpDialogFragment()
    {
        setHasOptionsMenu(true);
    }

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
        View v = inflater.inflate(R.layout.app_webview, container, false);

        webView = (WebView) v.findViewById(org.alfresco.mobile.android.foundation.R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        emptyView = v.findViewById(org.alfresco.mobile.android.foundation.R.id.empty);
        emptyTextView = (TextView) v.findViewById(org.alfresco.mobile.android.foundation.R.id.empty_text);
        emptyTextView.setText(Html.fromHtml(getString(R.string.error_offline)));

        final FragmentActivity activity = getActivity();

        defaultUrl = activity.getString(R.string.help_user_guide_default_url);

        webView.setWebViewClient(new WebViewClient()
        {
            boolean hasError = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                hasError = false;
                displayProgress(true);
                if (refreshIcon != null)
                {
                    refreshIcon.setVisible(false);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);

                // We redirect to default EN documentation if locale docs are
                // not available.
                if ((errorCode == ERROR_FILE_NOT_FOUND || errorCode == ERROR_HOST_LOOKUP) && !isDefault
                        && failingUrl.equals(rootUrl))
                {
                    hasError = true;
                    view.loadUrl(defaultUrl);
                    view.setVisibility(View.GONE);
                }
                else if (!ConnectivityUtils.hasInternetAvailable(getActivity()))
                {
                    view.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    hasError = true;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                if (getActivity() == null) { return; }
                super.onPageFinished(view, url);
                if (hasError)
                {
                    view.setVisibility(View.GONE);
                }
                else
                {
                    view.setVisibility(View.VISIBLE);
                }
                displayProgress(false);
                if (refreshIcon != null)
                {
                    refreshIcon.setVisible(true);
                }
            }

            public void onFormResubmission(WebView view, Message dontResend, Message resend)
            {
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

    @Override
    public void onStop()
    {
        super.onStop();
        if (webView != null)
        {
            webView.stopLoading();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private String getUrl(FragmentActivity activity)
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

    @Override
    public void refresh()
    {
        webView.reload();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        if (!MenuFragmentHelper.canDisplayFragmentMenu(getActivity())) { return; }
        menu.clear();

        refreshIcon = menu.add(Menu.NONE, R.id.menu_refresh, Menu.FIRST, R.string.refresh);
        refreshIcon.setIcon(R.drawable.ic_refresh);
        refreshIcon.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return false;
    }

    private void displayProgress(boolean show)
    {
        if (getActivity() instanceof AppCompatActivity)
        {
            ((AlfrescoAppCompatActivity) getActivity()).setSupportProgressBarIndeterminate(show);
        }
        else if (getActivity() instanceof AlfrescoActivity)
        {
            ((AlfrescoActivity) getActivity()).setSupportProgressBarIndeterminate(show);
        }
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
        // CREATE
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            AnalyticsHelper.reportScreen(getActivity(), AnalyticsManager.SCREEN_HELP);
            return newInstanceByTemplate(b);
        }
    }
}
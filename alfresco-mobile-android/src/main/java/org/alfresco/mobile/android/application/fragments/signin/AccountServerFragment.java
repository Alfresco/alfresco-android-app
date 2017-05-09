/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco Activiti Mobile for Android.
 *
 * Alfresco Activiti Mobile for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco Activiti Mobile for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.alfresco.mobile.android.application.fragments.signin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.api.utils.PublicAPIUrlRegistry;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.account.CheckServerEvent;
import org.alfresco.mobile.android.async.account.CheckServerRequest;
import org.alfresco.mobile.android.platform.exception.AlfrescoExceptionHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class AccountServerFragment extends AlfrescoFragment
{
    public static final String TAG = AccountServerFragment.class.getName();

    private MaterialEditText hostname;

    private CheckBox https;

    private boolean hideHTTPS = true;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public AccountServerFragment()
    {
        super();
        requiredSession = false;
        eventBusRequired = true;
        screenName = AnalyticsManager.SCREEN_ACCOUNT_SERVER;
    }

    public static AccountServerFragment newInstanceByTemplate(Bundle b)
    {
        AccountServerFragment cbf = new AccountServerFragment();
        cbf.setArguments(b);
        return cbf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRootView(inflater.inflate(R.layout.fr_account_server, container, false));
        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final LinearLayout backField = (LinearLayout) viewById(R.id.account_action_server_container);

        final Button actionContinue = (Button) viewById(R.id.account_action_server);
        actionContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String value = createHostnameURL(hostname.getText().toString(), https.isChecked());
                if (value == null)
                {
                    hostname.setError("Your hostname seems invalid");
                }
                else
                {
                    Operator.with(getActivity())
                            .load(new CheckServerRequest.Builder(https.isChecked(), hostname.getText().toString()));
                }
            }
        });

        https = (CheckBox) viewById(R.id.signing_https);
        https.setVisibility(View.INVISIBLE);

        hostname = (MaterialEditText) viewById(R.id.signing_hostname);
        hostname.requestFocus();
        UIUtils.showKeyboard(getActivity(), hostname);

        if (hostname.getText().length() == 0)
        {
            actionContinue.setEnabled(false);
            backField.setBackgroundColor(getResources().getColor(R.color.accent_disable));
        }

        hostname.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() == 0)
                {
                    actionContinue.setEnabled(false);
                    backField.setBackgroundColor(getResources().getColor(R.color.accent_disable));
                }
                else
                {
                    actionContinue.setEnabled(true);
                    backField.setBackgroundColor(getResources().getColor(R.color.accent));
                }

                hideHTTPS = true;
                switch (s.length())
                {
                    case 0:
                        hideHTTPS = true;
                        break;
                    case 1:
                        hideHTTPS = s.toString().equals("h");
                        break;
                    case 2:
                        hideHTTPS = s.toString().equals("ht");
                        break;
                    case 3:
                        hideHTTPS = s.toString().equals("htt");
                        break;
                    case 4:
                        hideHTTPS = s.toString().equals("http");
                        break;
                    case 5:
                        hideHTTPS = s.toString().equals("http:") || s.toString().equals("https");
                        break;
                    case 6:
                        hideHTTPS = s.toString().equals("http:/") || s.toString().equals("https:");
                        break;
                    case 7:
                        hideHTTPS = s.toString().equals("http://") || s.toString().equals("https:/");
                        break;
                    case 8:
                        hideHTTPS = s.toString().startsWith("http://") || s.toString().equals("https://");
                        break;
                    default:
                        hideHTTPS = s.toString().startsWith("http://") || s.toString().startsWith("https://");
                }

                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                if (https.getVisibility() == View.VISIBLE && hideHTTPS
                        || https.getVisibility() == View.INVISIBLE && !hideHTTPS)
                {
                    https.setVisibility(hideHTTPS ? View.INVISIBLE : View.VISIBLE);
                    https.animate().setDuration(shortAnimTime).alpha(hideHTTPS ? 0 : 1)
                            .setListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    https.setVisibility(hideHTTPS ? View.INVISIBLE : View.VISIBLE);
                                }
                            });
                }
                else
                {
                    https.setVisibility(hideHTTPS ? View.INVISIBLE : View.VISIBLE);
                }
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public static String createHostnameURL(String hostname, boolean https)
    {
        String value = hostname.trim().toLowerCase();

        StringBuilder builder = new StringBuilder();

        // Check if starts with http ?
        if (value.startsWith("http://") || value.startsWith("https://"))
        {
            // Do Nothing. We consider it's a plain url
            builder.append(value);
        }
        else
        {
            builder.append((https) ? "https://" : "http://");
            builder.append(value);
            if (value.endsWith("/alfresco") || value.endsWith("/alfresco/"))
            {
                // Do nothing
            }
            else if (value.endsWith(PublicAPIUrlRegistry.BINDING_NETWORK_CMISATOM)
                    || value.endsWith(OnPremiseUrlRegistry.BINDING_CMISATOM)
                    || value.endsWith(OnPremiseUrlRegistry.BINDING_CMIS))
            {
                // Do nothing
            }
            else
            {
                builder.append(value.endsWith("/") ? "alfresco" : "/alfresco");
            }
        }

        // Check it's a valid URL
        try
        {
            URL url = new URL(builder.toString());
        }
        catch (MalformedURLException e)
        {
            // Display Error !
            return null;
        }

        return builder.toString();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTs
    // ///////////////////////////////////////////////////////////////////////////
    @Subscribe
    public void onServerCheck(final CheckServerEvent event)
    {
        if (event.hasException)
        {
            int fieldMessageId = AlfrescoExceptionHelper.getMessageErrorId(getActivity(), event.exception, true);
            int messageId = AlfrescoExceptionHelper.getMessageErrorId(getActivity(), event.exception, false);

            hostname.setError(Html.fromHtml(fieldMessageId == R.string.error_unknown
                    ? String.format(getString(fieldMessageId), event.exception.getCause())
                    : getString(fieldMessageId)));
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                    .title(R.string.general_login_check_server).neutralText("Ignore and Continue")
                    .callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onNeutral(MaterialDialog dialog)
                        {
                            AccountCredentialsFragment.with(getActivity())
                                    .hostname(createHostnameURL(hostname.getText().toString(), https.isChecked()))
                                    .display();
                        }
                    })
                    .content(Html.fromHtml(messageId == R.string.error_unknown
                            ? String.format(getString(messageId), event.exception.getCause()) : getString(messageId)))
                    .positiveText(R.string.ok);
            builder.show();
            return;
        }
        else
        {
            // SAML enabled so
            // SAML is enforced by default (need to check server side config)
            if (event.data.samlData != null && event.data.samlData.isSamlEnabled())
            {
                AccountSigninSamlFragment.with(getActivity()).urlInfo(event.data).isCreation(true).display();
            }
            else
            {
                // Default case: we request credentials
                AccountCredentialsFragment.with(getActivity())
                        .hostname(event.data.enforceCMIS ? event.data.baseUrl : event.data.testUrl).display();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity activity)
    {
        return new Builder(activity);
    }

    public static class Builder extends AlfrescoFragmentBuilder
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
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        };
    }
}

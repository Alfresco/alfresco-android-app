/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.mobile.android.application.activity;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.signin.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.signin.AccountServerFragment;
import org.alfresco.mobile.android.platform.intent.PrivateIntent;
import org.alfresco.mobile.android.platform.utils.BundleUtils;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.PopupMenu;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * A login screen that offers login via email/password.
 */
public class WelcomeActivity extends BaseActivity
{
    public static final String EXTRA_ADD_ACCOUNT = "addAccount";

    protected boolean isCreation = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (getIntent().getExtras() != null)
        {
            isCreation = BundleUtils.getBoolean(getIntent().getExtras(), EXTRA_ADD_ACCOUNT);
        }

        findViewById(R.id.signin_more_information).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(WelcomeActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.signin_information, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.menu_about:
                                AboutFragment.with(WelcomeActivity.this).displayAsDialog();
                                break;
                            case R.id.menu_help:
                                Intent i = new Intent(WelcomeActivity.this, PrivateDialogActivity.class);
                                i.setAction(PrivateIntent.ACTION_DISPLAY_HELP);
                                if (isCreation())
                                {
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                }
                                startActivity(i);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        if (getSupportFragmentManager().findFragmentByTag(AccountServerFragment.TAG) == null) {
            FragmentDisplayer.with(this)
                    .load(AccountServerFragment.with(this).addExtra(getIntent().getExtras()).createFragment()).animate(null)
                    .back(false).into(FragmentDisplayer.PANEL_LEFT);
        }
    }

    public void signIn(View v)
    {
        AccountServerFragment.with(this).display();
    }

    public void signInOnline(View v)
    {
        if (ConnectivityUtils.hasNetwork(this))
        {
            AccountOAuthFragment.with(this).isCreation(true).display();
        }
        else
        {
            Crouton.cancelAllCroutons();
            Crouton.showText(this,
                    Html.fromHtml(getString(org.alfresco.mobile.android.foundation.R.string.error_session_nodata)),
                    Style.ALERT, (ViewGroup) (findViewById(R.id.content_frame)));
        }
    }

    public boolean isCreation()
    {
        return isCreation;
    }
}

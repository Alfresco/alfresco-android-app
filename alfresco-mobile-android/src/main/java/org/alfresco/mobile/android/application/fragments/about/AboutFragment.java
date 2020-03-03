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
package org.alfresco.mobile.android.application.fragments.about;

import java.util.Map;

import org.alfresco.mobile.android.api.Version;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.fragments.builder.LeafFragmentBuilder;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class AboutFragment extends AlfrescoFragment
{

    public static final String TAG = AboutFragment.class.getName();

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public AboutFragment()
    {
        requiredSession = false;
        checkSession = false;
        screenName = AnalyticsManager.SCREEN_SETTINGS_ABOUT;
    }

    protected static AboutFragment newInstanceByTemplate(Bundle b)
    {
        AboutFragment bf = new AboutFragment();
        bf.setArguments(b);
        return bf;
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return new MaterialDialog.Builder(getActivity()).backgroundColorRes(R.color.secondary_background)
                .customView(createView(inflater, null), true).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);
        if (container == null) { return null; }
        return createView(inflater, container);
    }

    private View createView(LayoutInflater inflater, ViewGroup container)
    {
        View v = inflater.inflate(R.layout.app_about, container, false);

        TextView foo = (TextView) v.findViewById(R.id.about_description);
        foo.setText(Html.fromHtml(getString(R.string.about_description)));

        // Version Number
        TextView tv = (TextView) v.findViewById(R.id.about_buildnumber);
        tv.setText(getVersionNumber(getActivity()));

        // SDK Version Number
        tv = (TextView) v.findViewById(R.id.about_sdknumber);
        StringBuilder sb = new StringBuilder(getText(R.string.sdknumber_version)).append(" ").append(Version.SDK);
        tv.setText(sb.toString());

        return v;
    }

    public static String getVersionNumber(Context context)
    {
        String versionNumber;
        try
        {
            StringBuilder sb = new StringBuilder(context.getString(R.string.buildnumber_version)).append(" ")
                    .append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)
                    .append(".").append(context.getString(R.string.bamboo_buildnumber));
            versionNumber = sb.toString();
        }
        catch (NameNotFoundException e)
        {
            versionNumber = "X.x.x.x";
        }
        return versionNumber;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity appActivity)
    {
        return new Builder(appActivity);
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
            this.extraConfiguration = new Bundle();
            this.menuIconId = R.drawable.ic_repository_dark;
            this.menuTitleId = R.string.menu_browse_root;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // CREATE FRAGMENT
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

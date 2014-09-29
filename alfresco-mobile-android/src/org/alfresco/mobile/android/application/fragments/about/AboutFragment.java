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
package org.alfresco.mobile.android.application.fragments.about;

import org.alfresco.mobile.android.api.Version;
import org.alfresco.mobile.android.application.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends DialogFragment
{

    public static final String TAG = AboutFragment.class.getName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return new AlertDialog.Builder(getActivity()).setView(createView(inflater, null)).create();
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

        TextView tv = (TextView) v.findViewById(R.id.about_versionLabel);
        String versionLabel = getString(R.string.version_label);
        if (!TextUtils.isEmpty(versionLabel)){
            tv.setText(versionLabel);
        } else {
            tv.setVisibility(View.GONE);
        }
        
        // Version Number
        tv = (TextView) v.findViewById(R.id.about_buildnumber);
        String versionNumber = "";
        try
        {
            StringBuilder sb = new StringBuilder(getText(R.string.buildnumber_version));
            sb.append(" ");
            sb.append(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
            sb.append(".");
            sb.append(getText(R.string.bamboo_buildnumber));
            versionNumber = sb.toString();
        }
        catch (NameNotFoundException e)
        {
            versionNumber = "X.x.x.x";
        }
        tv.setText(versionNumber);

        // SDK Version Number
        tv = (TextView) v.findViewById(R.id.about_sdknumber);
        StringBuilder sb = new StringBuilder(getText(R.string.sdknumber_version));
        sb.append(" ");
        sb.append(Version.SDK);
        tv.setText(sb.toString());

        return v;
    }

}

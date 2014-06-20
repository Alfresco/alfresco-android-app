/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.workflow;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class CreateTaskDocumentPickerFragment extends BaseFragment
{
    public static final String TAG = CreateTaskDocumentPickerFragment.class.getName();

    private View vRoot;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS & HELPERS
    // ///////////////////////////////////////////////////////////////////////////
    public CreateTaskDocumentPickerFragment()
    {
    }

    public static CreateTaskDocumentPickerFragment newInstance()
    {
        CreateTaskDocumentPickerFragment bf = new CreateTaskDocumentPickerFragment();
        Bundle b = new Bundle();
        bf.setArguments(b);
        return bf;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(true);

        container.setVisibility(View.VISIBLE);
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        vRoot = inflater.inflate(R.layout.app_document_picker, container, false);

        if (alfSession == null) { return vRoot; }

        // BUTTONS
        Button b = (Button) vRoot.findViewById(R.id.picker_root);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((BaseActivity) getActivity()).addNavigationFragment(alfSession.getRootFolder());
            }
        });

        b = (Button) vRoot.findViewById(R.id.picker_sites);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BrowserSitesFragment frag = BrowserSitesFragment.newInstance();
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        BrowserSitesFragment.TAG, true);
            }
        });

        b = (Button) vRoot.findViewById(R.id.picker_favorites);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FavoritesFragment frag = FavoritesFragment.newInstance(FavoritesFragment.MODE_FOLDERS);
                FragmentDisplayer.replaceFragment(getActivity(), frag, DisplayUtils.getLeftFragmentId(getActivity()),
                        FavoritesFragment.TAG, true);
            }
        });

        return vRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        if (getArguments() != null && getArguments().containsKey(IntentIntegrator.EXTRA_DOCUMENTS))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_attachments));
        }
        else
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.process_choose_attachments));
        }
        getActivity().invalidateOptionsMenu();

        super.onResume();
    }
}

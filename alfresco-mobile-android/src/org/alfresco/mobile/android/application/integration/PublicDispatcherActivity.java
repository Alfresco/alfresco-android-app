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
package org.alfresco.mobile.android.application.integration;

import java.io.File;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.fragment.AccountOAuthFragment;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

/**
 * Activity responsible to manage public intent from 3rd party application.
 * 
 * @author Jean Marie Pascal
 */
public class PublicDispatcherActivity extends Activity
{
    /** Define the type of importFolder. */
    private int uploadFolder;

    /** Define the local file to upload */
    private File uploadFile;

    // ///////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.app_left_panel);
        getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco_logo);

        Fragment f = new ImportFormFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), ImportFormFragment.TAG, false,
                false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE && resultCode == RESULT_CANCELED)
        {
            finish();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        PassCodeActivity.requestUserPasscode(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        PasscodePreferences.updateLastActivityDisplay(this);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if (PasscodePreferences.hasPasscodeEnable(this)) { return; }

        try
        {
            // Intent after session loading
            if (IntentIntegrator.ACTION_LOAD_SESSION_FINISH.equals(intent.getAction()))
            {
                setProgressBarIndeterminateVisibility(false);

                if (getSession() instanceof RepositorySession)
                {
                    DisplayUtils.switchSingleOrTwo(this, false);
                }
                else if (getSession() instanceof CloudSession)
                {
                    DisplayUtils.switchSingleOrTwo(this, true);
                }

                // Remove OAuthFragment if one
                if (getFragment(AccountOAuthFragment.TAG) != null)
                {
                    getFragmentManager().popBackStack(AccountOAuthFragment.TAG,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }

                BaseFragment frag = null;
                if (getSession() != null && uploadFolder == R.string.menu_browse_sites)
                {
                    frag = BrowserSitesFragment.newInstance();
                    FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                            BrowserSitesFragment.TAG, true);
                }
                else if (getSession() != null && uploadFolder == R.string.menu_browse_root)
                {
                    addNavigationFragment(getSession().getRootFolder());
                }
                return;
            }

            // Intent for Display Errors
            if (IntentIntegrator.ACTION_DISPLAY_ERROR_IMPORT.equals(intent.getAction()))
            {
                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }
                Exception e = (Exception) intent.getExtras().getSerializable(IntentIntegrator.DISPLAY_ERROR_DATA);

                String errorMessage = getString(R.string.error_general);
                if (e instanceof AlfrescoAppException && ((AlfrescoAppException) e).isDisplayMessage())
                {
                    errorMessage = e.getMessage();
                }

                MessengerManager.showLongToast(this, errorMessage);

                CloudExceptionUtils.handleCloudException(this, e, false);

                return;
            }
        }
        catch (Exception e)
        {
            MessengerManager.showLongToast(this, e.getMessage());
        }
    }

    // ///////////////////////////////////////////
    // FRAGMENT MANAGEMENT
    // ///////////////////////////////////////////

    public void addNavigationFragment(Folder f)
    {
        BaseFragment frag = ChildrenBrowserFragment.newInstance(f);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(Site s)
    {
        BaseFragment frag = ChildrenBrowserFragment.newInstance(s);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    // ///////////////////////////////////////////
    // UI Public Method
    // ///////////////////////////////////////////

    public void doCancel(View v)
    {
        finish();
    }

    public void doImport(View v)
    {
        ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFile(uploadFile);
    }

    // ///////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////

    public Fragment getFragment(String tag)
    {
        return getFragmentManager().findFragmentByTag(tag);
    }

    public AlfrescoSession getSession()
    {
        return SessionUtils.getSession(this);
    }

    public void setUploadFolder(int uploadFolderType)
    {
        this.uploadFolder = uploadFolderType;
    }

    public void setUploadFile(File localFile)
    {
        this.uploadFile = localFile;
    }
}

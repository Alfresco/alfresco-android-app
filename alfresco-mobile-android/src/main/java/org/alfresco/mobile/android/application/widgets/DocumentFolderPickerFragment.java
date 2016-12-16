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
package org.alfresco.mobile.android.application.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.builder.AlfrescoFragmentBuilder;
import org.alfresco.mobile.android.application.fragments.node.upload.UploadFolderAdapter;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * Display the form to choose AlfrescoAccount and import folder.
 * 
 * @author Jean Marie Pascal
 */
public class DocumentFolderPickerFragment extends Fragment
{
    public static final String TAG = "ImportFormFragment";

    @SuppressWarnings("serial")
    private static final List<Integer> FOLDER_ACTIONS_LIST = new ArrayList<Integer>(3)
    {
        {
            add(R.string.menu_browse_root);
            add(R.string.menu_browse_userhome);
            add(R.string.menu_browse_sites);
            add(R.string.menu_favorites_folder);

        }
    };

    private static final List<Integer> FOLDER_SHORTCUT_LIST = new ArrayList<Integer>(4)
    {
        {
            add(R.string.menu_browse_root);
            add(R.string.menu_browse_userhome);
            add(R.string.menu_browse_sites);
            add(R.string.menu_favorites_folder);
            add(R.string.menu_downloads);
        }
    };

    @SuppressWarnings("serial")
    private static final List<Integer> ACTIONS_LIST = new ArrayList<Integer>(3)
    {
        {
            add(R.string.action_text);
            add(R.string.action_speech2text);
            add(R.string.action_photo);
        }
    };

    private AlfrescoAccount selectedAccount;

    private View rootView;

    private Integer rootFolderTypeId;

    private int rootFolderTypeIndex;

    private Integer actionId;

    private int actionIdIndex;

    /** Principal ListView of the fragment */
    protected ListView lv;

    protected ArrayAdapter<?> adapter;

    protected int selectedPosition;

    private Spinner spinnerAccount;

    // //////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public static DocumentFolderPickerFragment newInstance(Bundle b)
    {
        DocumentFolderPickerFragment fr = new DocumentFolderPickerFragment();
        fr.setArguments(b);
        return fr;
    }

    public static DocumentFolderPickerFragment newInstanceByTemplate(Bundle b)
    {
        return new DocumentFolderPickerFragment();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        int titleId = R.string.shortcut_action_create;
        if (getActivity() instanceof FolderShortcutActivity)
        {
            titleId = R.string.shortcut_create;
        }
        UIUtils.displayTitle(getActivity(), titleId);

        rootView = inflater.inflate(R.layout.app_docfolder_picker, container, false);
        spinnerAccount = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                selectedAccount = (AlfrescoAccount) parent.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // Do nothing
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        List<AlfrescoAccount> list = AlfrescoAccountManager.retrieveAccounts(getActivity());
        spinnerAccount.setAdapter(new AccountsAdapter(getActivity(), list, R.layout.row_two_lines, null));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Button b = UIUtils.initCancel(rootView, R.string.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        b = UIUtils.initValidation(rootView, R.string.next);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        refreshSpinners();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void refreshSpinners()
    {
        // UPLOAD FOLDER
        Spinner spinner = (Spinner) rootView.findViewById(R.id.import_folder_spinner);
        UploadFolderAdapter upLoadadapter = null;
        if (getActivity() instanceof FolderShortcutActivity)
        {
            upLoadadapter = new UploadFolderAdapter(getActivity(), R.layout.row_single_line, FOLDER_SHORTCUT_LIST);
        }
        else
        {
            upLoadadapter = new UploadFolderAdapter(getActivity(), R.layout.row_single_line, FOLDER_ACTIONS_LIST);
        }
        spinner.setAdapter(upLoadadapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                rootFolderTypeId = (Integer) parent.getItemAtPosition(pos);
                rootFolderTypeIndex = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // DO Nothing
            }
        });
        if (rootFolderTypeId == null)
        {
            rootFolderTypeIndex = 0;
        }
        spinner.setSelection(rootFolderTypeIndex);

        // ACTIONS ASSOCIATED
        if (getActivity() instanceof FolderShortcutActivity)
        {
            rootView.findViewById(R.id.actions_spinner_title).setVisibility(View.GONE);
            rootView.findViewById(R.id.actions_spinner).setVisibility(View.GONE);
            return;
        }
        spinner = (Spinner) rootView.findViewById(R.id.actions_spinner);
        ActionShortcutAdapter actionAdapter = new ActionShortcutAdapter(getActivity(), R.layout.row_single_line,
                ACTIONS_LIST);
        spinner.setAdapter(actionAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                actionId = (Integer) parent.getItemAtPosition(pos);
                actionIdIndex = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // DO Nothing
            }
        });
        if (actionId == null)
        {
            actionIdIndex = 0;
        }
        spinner.setSelection(actionIdIndex);

    }

    private void next()
    {
        AlfrescoAccount tmpAccount = selectedAccount;
        switch (rootFolderTypeId)
        {
            case R.string.menu_browse_sites:
            case R.string.menu_browse_root:
            case R.string.menu_favorites_folder:
            case R.string.menu_browse_userhome:

                if (getActivity() instanceof BaseShortcutActivity)
                {
                    ((BaseShortcutActivity) getActivity()).setUploadFolder(rootFolderTypeId);
                    ((BaseShortcutActivity) getActivity()).setUploadAccount(tmpAccount);
                }

                if (getActivity() instanceof ActionShortcutActivity)
                {
                    ((ActionShortcutActivity) getActivity()).setActionId(actionId);
                }

                AlfrescoSession session = SessionManager.getInstance(getActivity()).getSession(tmpAccount.getId());

                // Try to use Session used by the application
                if (session != null)
                {
                    ((BaseActivity) getActivity()).setCurrentAccount(tmpAccount);
                    ((BaseActivity) getActivity()).setRenditionManager(null);
                    EventBusManager.getInstance().post(
                            new LoadSessionCallBack.LoadAccountCompletedEvent(null, tmpAccount));
                    return;
                }

                // Session is not used by the application so create one.
                SessionManager.getInstance(getActivity()).loadSession(tmpAccount);
                if (getActivity() instanceof AlfrescoActivity)
                {
                    ((AlfrescoActivity) getActivity()).displayWaitingDialog();
                }

                break;
            case R.string.menu_downloads:
                if (getActivity() instanceof FolderShortcutActivity)
                {
                    ((FolderShortcutActivity) getActivity()).createShortcut(tmpAccount, AlfrescoStorageManager
                            .getInstance(getActivity()).getDownloadFolder(tmpAccount));
                }

                break;
            default:
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BUILDER
    // ///////////////////////////////////////////////////////////////////////////
    public static Builder with(FragmentActivity appActivity)
    {
        return new Builder(appActivity);
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
            this.extraConfiguration = new Bundle();

            this.menuIconId = R.drawable.ic_repository_dark;
            this.menuTitleId = R.string.menu_browse_root;
        }

        // ///////////////////////////////////////////////////////////////////////////
        // SETTERS
        // ///////////////////////////////////////////////////////////////////////////

        // ///////////////////////////////////////////////////////////////////////////
        // CLICK
        // ///////////////////////////////////////////////////////////////////////////
        protected Fragment createFragment(Bundle b)
        {
            return newInstanceByTemplate(b);
        }
    }
}

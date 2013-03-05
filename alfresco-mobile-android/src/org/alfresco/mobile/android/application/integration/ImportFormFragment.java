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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.SessionLoader;
import org.alfresco.mobile.android.application.HomeScreenActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.fragment.AccountLoginLoaderCallback;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsLoader;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.ui.manager.ActionManager;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Display the form to choose account and import folder.
 * 
 * @author Jean Marie Pascal
 */
public class ImportFormFragment extends Fragment implements LoaderCallbacks<List<Account>>
{

    public static final String TAG = "ImportFormFragment";

    private List<Account> accounts;

    private Account selectedAccount;

    private int accountIndex;

    private String fileName;

    private File file;

    private View rootView;

    private Integer folderImportId;

    private int importFolderIndex;

    public static ImportFormFragment newInstance(Bundle b)
    {
        ImportFormFragment fr = new ImportFormFragment();
        fr.setArguments(b);
        return fr;
    }

    // ///////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.app_import, container, false);
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        getLoaderManager().restartLoader(AccountsLoader.ID, null, this);

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        try
        {
            // Manage only one clip data. If multiple we ignore.
            if (AndroidVersion.isJBOrAbove() && (!Intent.ACTION_SEND.equals(action) || type == null))
            {
                ClipData clipdata = intent.getClipData();
                if (clipdata != null && clipdata.getItemCount() == 1 && clipdata.getItemAt(0) != null
                        && (clipdata.getItemAt(0).getText() != null || clipdata.getItemAt(0).getUri() != null))
                {
                    Item item = clipdata.getItemAt(0);
                    Uri uri = item.getUri();
                    if (uri != null)
                    {
                        retrieveIntentInfo(uri);
                    }
                    else
                    {
                        String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss").format(new Date());
                        File localParentFolder = StorageManager.getCacheDir(getActivity(), "AlfrescoMobile/import");
                        File f = createFile(localParentFolder, timeStamp + ".txt", item.getText().toString());
                        if (f.exists())
                        {
                            retrieveIntentInfo(Uri.fromFile(f));
                        }
                    }
                }
            }

            if (file == null && Intent.ACTION_SEND.equals(action) && type != null)
            {
                Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                retrieveIntentInfo(uri);
            }
            else if (action == null && intent.getData() != null)
            {
                retrieveIntentInfo(intent.getData());
            }
            else if (file == null || fileName == null)
            {
                MessengerManager.showLongToast(getActivity(), getString(R.string.import_unsupported_intent));
                getActivity().finish();
                return;
            }
        }
        catch (AlfrescoAppException e)
        {
            org.alfresco.mobile.android.application.manager.ActionManager.actionDisplayError(this, e);
            getActivity().finish();
            return;
        }

        TextView tv = (TextView) rootView.findViewById(R.id.toptext);
        tv.setText(fileName);
        tv = (TextView) rootView.findViewById(R.id.bottomtext);
        tv.setText(Formatter.formatFileSize(getActivity(), file.length()));
        ImageView iv = (ImageView) rootView.findViewById(R.id.icon);
        iv.setImageDrawable(getResources().getDrawable(MimeTypeManager.getIcon(fileName)));

        Button b = (Button) rootView.findViewById(R.id.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        b = (Button) rootView.findViewById(R.id.ok);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        refreshImportFolder();
    }

    private void retrieveIntentInfo(Uri uri)
    {
        if (uri == null) { throw new AlfrescoAppException(getString(R.string.import_unsupported_intent), true); }

        String tmpPath = ActionManager.getPath(getActivity(), uri);
        if (tmpPath != null)
        {
            file = new File(tmpPath);

            if (file == null || !file.exists()) { throw new AlfrescoAppException(
                    getString(R.string.error_unknown_filepath), true); }
            fileName = file.getName();

            if (getActivity() instanceof PublicDispatcherActivity)
            {
                ((PublicDispatcherActivity) getActivity()).setUploadFile(file);
            }
        }
        else
        {
            // Error case : Unable to find the file path associated
            // to user pick.
            // Sample : Picasa image case
            throw new AlfrescoAppException(getString(R.string.error_unknown_filepath), true);
        }
    }

    // ///////////////////////////////////////////
    // ACCOUNT LOADER
    // ///////////////////////////////////////////

    @Override
    public Loader<List<Account>> onCreateLoader(int id, Bundle args)
    {
        return new AccountsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Account>> arg0, List<Account> results)
    {
        if (results == null || results.isEmpty())
        {
            startActivityForResult(new Intent(getActivity(), HomeScreenActivity.class), 1);
            return;
        }

        accounts = new ArrayList<Account>(results.size());
        for (Account item : results)
        {
            if (item.getActivation() == null)
            {
                accounts.add(item);
            }
        }
        refreshAccounts();
    }

    @Override
    public void onLoaderReset(Loader<List<Account>> arg0)
    {
        // TODO Auto-generated method stub

    }

    public void refreshAccounts()
    {
        if (accounts == null) { return; }

        SimpleAccountAdapter adapter = new SimpleAccountAdapter(getActivity(), R.layout.app_list_row, accounts);
        Spinner s = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                selectedAccount = (Account) parent.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // Do nothing
            }
        });

        if (selectedAccount == null)
        {
            selectedAccount = AccountsPreferences.getDefaultAccount(getActivity(), accounts);
        }
        if (selectedAccount == null)
        {
            accountIndex = 0;
        }
        else
        {
            for (int i = 0; i < accounts.size(); i++)
            {
                if (selectedAccount != null && accounts.get(i).getId() == selectedAccount.getId())
                {
                    accountIndex = i;
                    break;
                }
            }
        }

        s.setSelection(accountIndex);
        selectedAccount = accounts.get(accountIndex);
    }

    // ///////////////////////////////////////////
    // IMPORT FOLDER
    // ///////////////////////////////////////////

    public void refreshImportFolder()
    {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.import_folder_spinner);
        SimpleImportFolderAdapter adapter = new SimpleImportFolderAdapter(getActivity(), R.layout.app_list_row,
                importFolderList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                folderImportId = (Integer) parent.getItemAtPosition(pos);
                importFolderIndex = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub

            }
        });
        if (folderImportId == null)
        {
            importFolderIndex = 0;
        }
        spinner.setSelection(importFolderIndex);
    }

    @SuppressWarnings("serial")
    private static final List<Integer> importFolderList = new ArrayList<Integer>(3)
    {
        {
            add(R.string.menu_downloads);
            add(R.string.menu_browse_all_sites);
            add(R.string.menu_browse_root);
        }
    };

    private void next()
    {
        switch (folderImportId)
        {
            case R.string.menu_browse_all_sites:
            case R.string.menu_browse_root:
                AccountLoginLoaderCallback call = new AccountLoginLoaderCallback(getActivity(), selectedAccount);
                getActivity().getLoaderManager().restartLoader(SessionLoader.ID, null, call);
                if (getActivity() instanceof PublicDispatcherActivity)
                {
                    ((PublicDispatcherActivity) getActivity()).setUploadFolder(folderImportId);
                }
                if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
                {
                    new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
                }
                break;
            case R.string.menu_downloads:
                ImportLocalDialogFragment fr = ImportLocalDialogFragment.newInstance(selectedAccount, file);
                fr.show(getActivity().getFragmentManager(), ImportLocalDialogFragment.TAG);
                break;
            default:
                break;
        }
    }

    // TODO Move to IOUtils
    private File createFile(File localParentFolder, String filename, String data)
    {
        File outputFile = null;
        Writer writer = null;
        try
        {
            if (!localParentFolder.isDirectory())
            {
                localParentFolder.mkdir();
            }
            outputFile = new File(localParentFolder, filename);
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(data);
            writer.close();
        }
        catch (IOException e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return outputFile;
    }
}

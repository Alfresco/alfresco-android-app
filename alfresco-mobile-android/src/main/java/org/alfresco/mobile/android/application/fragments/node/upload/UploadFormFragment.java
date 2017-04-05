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
package org.alfresco.mobile.android.application.fragments.node.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.capture.DeviceCapture;
import org.alfresco.mobile.android.application.fragments.account.AccountsAdapter;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerAdapter;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.async.session.LoadSessionCallBack;
import org.alfresco.mobile.android.async.session.oauth.AccountOAuthHelper;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.EventBusManager;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.exception.AlfrescoAppException;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;
import org.alfresco.mobile.android.ui.fragments.AlfrescoFragment;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
public class UploadFormFragment extends AlfrescoFragment
{
    public static final String TAG = "ImportFormFragment";

    private String fileName;

    private File file;

    private AlfrescoAccount selectedAccount;

    private Integer folderImportId;

    private int importFolderIndex;

    /** Principal ListView of the fragment */
    protected ListView lv;

    protected ArrayAdapter<?> adapter;

    protected int selectedPosition;

    protected List<File> files = new ArrayList<>();

    private Spinner spinnerDoc;

    private Spinner spinnerAccount;

    private Spinner spinnerFolder;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UploadFormFragment()
    {
        requiredSession = false;
        checkSession = false;
    }

    public static UploadFormFragment newInstance(Bundle b)
    {
        UploadFormFragment fr = new UploadFormFragment();
        fr.setArguments(b);
        return fr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        UIUtils.displayTitle(getActivity(), R.string.import_document_title);

        setRootView(inflater.inflate(R.layout.app_import, container, false));
        if (viewById(R.id.listView) != null)
        {
            initDocumentList(getRootView());
        }
        else
        {
            initiDocumentSpinner(getRootView());
        }

        spinnerAccount = (Spinner) viewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                selectedAccount = (AlfrescoAccount) parent.getItemAtPosition(pos);
                folderImportId = null;
                if (spinnerFolder != null)
                {
                    spinnerFolder.setAdapter(null);
                    spinnerFolder.setSelection(0);
                }
                refreshImportFolder();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // Do nothing
            }
        });
        return getRootView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        List<AlfrescoAccount> list = AlfrescoAccountManager.retrieveAccounts(getActivity());
        spinnerAccount.setAdapter(new AccountsAdapter(getActivity(), list, R.layout.row_two_lines, null));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart()
    {
        super.onStart();

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (files != null)
        {
            files.clear();
        }

        try
        {
            if (Intent.ACTION_SEND_MULTIPLE.equals(action))
            {
                if (AndroidVersion.isJBOrAbove())
                {
                    ClipData clipdata = intent.getClipData();
                    if (clipdata != null && clipdata.getItemCount() > 0)
                    {
                        Item item = null;
                        for (int i = 0; i < clipdata.getItemCount(); i++)
                        {
                            item = clipdata.getItemAt(i);
                            Uri uri = item.getUri();
                            if (uri != null)
                            {
                                retrieveIntentInfo(uri);
                            }
                            else
                            {
                                String timeStamp = new SimpleDateFormat(DeviceCapture.TIMESTAMP_PATTERN)
                                        .format(new Date());
                                File localParentFolder = AlfrescoStorageManager.getInstance(getActivity())
                                        .getCacheDir("AlfrescoMobile/import");
                                File f = createFile(localParentFolder, timeStamp + ".txt", item.getText().toString());
                                if (f.exists())
                                {
                                    retrieveIntentInfo(Uri.fromFile(f));
                                }
                            }
                            if (!files.contains(file))
                            {
                                files.add(file);
                            }
                        }
                    }
                }
                else
                {
                    if (intent.getExtras() != null && intent.getExtras().get(Intent.EXTRA_STREAM) instanceof ArrayList)
                    {
                        @SuppressWarnings("unchecked")
                        List<Object> attachments = (ArrayList<Object>) intent.getExtras().get(Intent.EXTRA_STREAM);
                        for (Object object : attachments)
                        {
                            if (object instanceof Uri)
                            {
                                Uri uri = (Uri) object;
                                if (uri != null)
                                {
                                    retrieveIntentInfo(uri);
                                }
                                if (!files.contains(file))
                                {
                                    files.add(file);
                                }
                            }
                        }
                    }
                    else if (file == null || fileName == null)
                    {
                        AlfrescoNotificationManager.getInstance(getActivity())
                                .showLongToast(getString(R.string.import_unsupported_intent));
                        getActivity().finish();
                        return;
                    }
                }
            }
            else
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
                            String timeStamp = new SimpleDateFormat(DeviceCapture.TIMESTAMP_PATTERN).format(new Date());
                            File localParentFolder = AlfrescoStorageManager.getInstance(getActivity())
                                    .getCacheDir("AlfrescoMobile/import");
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
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    retrieveIntentInfo(uri);
                }
                else if (action == null && intent.getData() != null)
                {
                    retrieveIntentInfo(intent.getData());
                }
                else if (file == null || fileName == null)
                {
                    AlfrescoNotificationManager.getInstance(getActivity())
                            .showLongToast(getString(R.string.import_unsupported_intent));
                    getActivity().finish();
                    return;
                }
                if (!files.contains(file))
                {
                    files.add(file);
                }
            }
        }
        catch (AlfrescoAppException e)
        {
            ActionUtils.actionDisplayError(this, e);
            getActivity().finish();
            return;
        }

        if (adapter == null && files != null)
        {
            adapter = new FileExplorerAdapter(this, R.layout.row_two_lines_progress, files);
            if (lv != null)
            {
                lv.setAdapter(adapter);
            }
            else if (spinnerDoc != null)
            {
                spinnerDoc.setAdapter(adapter);
            }
        }

        Button b = UIUtils.initCancel(getRootView(), R.string.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        b = UIUtils.initValidation(getRootView(), R.string.next);
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

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void initiDocumentSpinner(View v)
    {
        spinnerDoc = (Spinner) v.findViewById(R.id.import_documents_spinner);
        if (adapter != null)
        {
            spinnerDoc.setAdapter(adapter);
        }
    }

    private void initDocumentList(View v)
    {
        lv = (ListView) v.findViewById(R.id.listView);

        if (adapter != null)
        {
            lv.setAdapter(adapter);
            lv.setSelection(selectedPosition);
        }
    }

    private void retrieveIntentInfo(Uri uri)
    {
        if (uri == null) { throw new AlfrescoAppException(getString(R.string.import_unsupported_intent), true); }

        String tmpPath = ActionUtils.getPath(getActivity(), uri);
        if (tmpPath != null)
        {
            file = new File(tmpPath);

            if (!file.exists()) { throw new AlfrescoAppException(getString(R.string.error_unknown_filepath), true); }

            if (!file.getPath()
                    .startsWith(AlfrescoStorageManager.getInstance(getContext()).getRootPrivateFolder().getPath()))
            {
                int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            BaseActivity.REQUEST_PERMISSION_IMPORT_SD);
                }
            }

            fileName = file.getName();

            if (getActivity() instanceof PublicDispatcherActivity)
            {
                files.add(file);
                ((PublicDispatcherActivity) getActivity()).setUploadFile(files);
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

    private void refreshImportFolder()
    {
        List<Integer> importList = IMPORT_FOLDER_LIST;
        if (selectedAccount == null || selectedAccount.getTypeId() == AlfrescoAccount.TYPE_ALFRESCO_CLOUD)
        {
            importList = IMPORT_CLOUD_FOLDER_LIST;
        }

        spinnerFolder = (Spinner) viewById(R.id.import_folder_spinner);
        UploadFolderAdapter upLoadadapter = new UploadFolderAdapter(getActivity(), R.layout.row_single_line,
                importList);
        spinnerFolder.setAdapter(upLoadadapter);
        spinnerFolder.setOnItemSelectedListener(new OnItemSelectedListener()
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
                // DO Nothing
            }
        });
        if (folderImportId == null)
        {
            importFolderIndex = 0;
        }
        spinnerFolder.setSelection(importFolderIndex);
    }

    @SuppressWarnings("serial")
    private static final List<Integer> IMPORT_FOLDER_LIST = new ArrayList<Integer>(5)
    {
        {
            add(R.string.menu_downloads);
            add(R.string.menu_browse_sites);
            add(R.string.menu_favorites_folder);
            add(R.string.menu_browse_root);
            add(R.string.menu_browse_userhome);
        }
    };

    @SuppressWarnings("serial")
    private static final List<Integer> IMPORT_CLOUD_FOLDER_LIST = new ArrayList<Integer>(5)
    {
        {
            add(R.string.menu_downloads);
            add(R.string.menu_browse_sites);
            add(R.string.menu_favorites_folder);
            add(R.string.menu_browse_root);
        }
    };

    private void next()
    {
        AlfrescoAccount tmpAccount = selectedAccount;
        if (tmpAccount == null) { return; }
        switch (folderImportId)
        {
            case R.string.menu_browse_sites:
            case R.string.menu_browse_root:
            case R.string.menu_favorites_folder:
            case R.string.menu_browse_userhome:

                if (getActivity() instanceof PublicDispatcherActivity)
                {
                    ((PublicDispatcherActivity) getActivity()).setUploadFolder(folderImportId);
                }

                AlfrescoSession session = SessionManager.getInstance(getActivity()).getSession(tmpAccount.getId());

                // Try to use Session used by the application
                if (session != null && session.getServiceRegistry() != null)
                {
                    if (session instanceof CloudSession && AccountOAuthHelper.doesRequireRefreshToken(getActivity()))
                    {
                        // Enforce session creation if oauth token is obsolete
                        SessionManager.getInstance(getActivity()).removeAccount(tmpAccount.getId());
                        ((BaseActivity) getActivity()).setCurrentAccount(tmpAccount);
                        SessionManager.getInstance(getActivity()).loadSession(tmpAccount);
                        if (getActivity() instanceof AlfrescoActivity)
                        {
                            ((AlfrescoActivity) getActivity()).displayWaitingDialog();
                        }
                        return;
                    }
                    else
                    {
                        // elsewhere just use the current session
                        ((BaseActivity) getActivity()).setCurrentAccount(tmpAccount);
                        ((BaseActivity) getActivity()).setRenditionManager(null);
                        EventBusManager.getInstance()
                                .post(new LoadSessionCallBack.LoadAccountCompletedEvent(null, tmpAccount));
                        return;
                    }
                }

                // Session is not used by the application so create one.
                SessionManager.getInstance(getActivity()).loadSession(tmpAccount);
                if (getActivity() instanceof AlfrescoActivity)
                {
                    ((AlfrescoActivity) getActivity()).displayWaitingDialog();
                }

                break;
            case R.string.menu_downloads:
                if (files.size() == 1)
                {
                    UploadLocalDialogFragment fr = UploadLocalDialogFragment.newInstance(tmpAccount, file);
                    fr.show(getActivity().getSupportFragmentManager(), UploadLocalDialogFragment.TAG);
                }
                else
                {
                    File folderStorage = AlfrescoStorageManager.getInstance(getActivity())
                            .getDownloadFolder(tmpAccount);
                    DataProtectionManager.getInstance(getActivity()).copyAndEncrypt(tmpAccount, files, folderStorage);
                    getActivity().finish();
                }
                break;
            default:
                break;
        }
    }

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

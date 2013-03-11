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
package org.alfresco.mobile.android.application.fragments.encryption;

import java.io.File;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.WaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.manager.ActionManager.ActionManagerListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Responsible to display a waiting dialog during encryption/decryption process.
 * 
 * @author Jean Marie Pascal
 */
public class EncryptionDialogFragment extends Fragment implements LoaderCallbacks<LoaderResult<File>>
{
    private static final String TAG = "EncryptionDialogFragment";

    private static final String PARAM_FILE = "file";

    private static final String PARAM_FILE_MIMETYPE = "fileMimetype";

    private static final String PARAM_FILENAME = "fileName";

    private static final String PARAM_ENCRYPT = "encrypt";

    private static final String PARAM_ACCOUNT = "account";

    private static final String PARAM_INTENT_ACTION = "intentAction";

    private static final String PARAM_COPIED_FILE = "copiedFile";

    private static final String PARAM_FOLDER = "folder";

    private static Runnable finishedRunnable = null;

    private ActionManagerListener listener;

    private int loaderId;

    private String fragmentTransactionTag;

    public EncryptionDialogFragment()
    {
    }

    public EncryptionDialogFragment(String id)
    {
        this.fragmentTransactionTag = id;
    }

    public static EncryptionDialogFragment copyAndEncrypt(String copiedPath, String filePath, Account account)
    {
        File myFile = new File(filePath);
        File copiedFile = new File(copiedPath);
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putSerializable(PARAM_COPIED_FILE, copiedFile);
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putSerializable(PARAM_FILE, myFile);
        b.putBoolean(PARAM_ENCRYPT, true);
        b.putSerializable(PARAM_ACCOUNT, account);
        fragment.setArguments(b);
        return fragment;
    }

    public static EncryptionDialogFragment copy(String copiedPath, String filePath, Account account)
    {
        File myFile = new File(filePath);
        File copiedFile = new File(copiedPath);
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putSerializable(PARAM_COPIED_FILE, copiedFile);
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putSerializable(PARAM_FILE, myFile);
        b.putBoolean(PARAM_ENCRYPT, false);
        b.putSerializable(PARAM_ACCOUNT, account);
        fragment.setArguments(b);
        return fragment;
    }

    public static EncryptionDialogFragment encrypt(String filePath)
    {
        File myFile = new File(filePath);
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putSerializable(PARAM_FILE, myFile);
        b.putBoolean(PARAM_ENCRYPT, true);
        fragment.setArguments(b);
        return fragment;
    }

    public static EncryptionDialogFragment encryptAll(String filePath)
    {
        File myFile = new File(filePath);
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putSerializable(PARAM_FILE, myFile);
        b.putBoolean(PARAM_ENCRYPT, true);
        b.putBoolean(PARAM_FOLDER, true);
        fragment.setArguments(b);
        return fragment;
    }

    public static EncryptionDialogFragment decrypt(File myFile, String mimeType, ActionManagerListener listener,
            String intentAction)
    {
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putSerializable(PARAM_FILE, myFile);
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putString(PARAM_FILE_MIMETYPE, mimeType);
        b.putBoolean(PARAM_ENCRYPT, false);
        b.putString(PARAM_INTENT_ACTION, intentAction);
        fragment.setArguments(b);
        fragment.setActionManagerListener(listener);
        return fragment;
    }

    public static EncryptionDialogFragment decryptAll(File myFile)
    {
        return decryptAll(myFile, null);
    }
    
    public static EncryptionDialogFragment decryptAll(File myFile, Runnable r)
    {
        finishedRunnable = r;
        
        EncryptionDialogFragment fragment = new EncryptionDialogFragment(myFile.getName());
        Bundle b = new Bundle();
        b.putSerializable(PARAM_FILE, myFile);
        b.putString(PARAM_FILENAME, myFile.getName());
        b.putBoolean(PARAM_ENCRYPT, false);
        b.putBoolean(PARAM_FOLDER, true);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        Boolean doEncrypt = getArguments().getBoolean(PARAM_ENCRYPT);

        int messageId = R.string.decryption_title;
        int titleId = R.string.data_protection;
        if (getArguments().containsKey(PARAM_COPIED_FILE) && !doEncrypt)
        {
            messageId = R.string.copy_file_title;
            titleId = R.string.import_document_title;
        }
        else if (doEncrypt)
        {
            messageId = R.string.encryption_title;
        }

        if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
        {
            WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(titleId, messageId, true);
            dialog.show(getFragmentManager(), WaitingDialogFragment.TAG);
        }

        loaderId = EncryptionLoader.ID + getArguments().getSerializable(PARAM_FILENAME).hashCode();
        Loader<Object> loader = getActivity().getLoaderManager().getLoader(loaderId);
        if (loader == null)
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), EncryptionDialogFragment.this);
            getActivity().getLoaderManager().getLoader(loaderId).forceLoad();
        }
        else
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), EncryptionDialogFragment.this);
        }
        super.onStart();
    }

    @Override
    public Loader<LoaderResult<File>> onCreateLoader(int id, Bundle args)
    {
        File myFile = (File) getArguments().getSerializable(PARAM_FILE);
        Boolean doEncrypt = getArguments().getBoolean(PARAM_ENCRYPT);

        if (getArguments().containsKey(PARAM_COPIED_FILE))
        {
            return new EncryptionLoader(this, SessionUtils.getSession(getActivity()), myFile, doEncrypt,
                    (File) getArguments().getSerializable(PARAM_COPIED_FILE));
        }
        else
        {
            return new EncryptionLoader(this, SessionUtils.getSession(getActivity()), myFile, doEncrypt, getArguments()
                    .containsKey(PARAM_FOLDER));
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<File>> loader, LoaderResult<File> results)
    {
        if (finishedRunnable != null)
        {
            finishedRunnable.run();
            finishedRunnable = null;
        }
        
        if (results.hasException())
        {
            Log.e(TAG, Log.getStackTraceString(results.getException()));
            MessengerManager.showToast(getActivity(), R.string.error_general);
        }
        else
        {
            if (loader instanceof EncryptionLoader && ((EncryptionLoader) loader).getCopiedFile() != null)
            {
                MessengerManager.showLongToast(
                        getActivity(),
                        String.format(getString(R.string.import_send_download),
                                ((Account) getArguments().get(PARAM_ACCOUNT)).getDescription()));
                getActivity().finish();
            }
            else
            {
                Intent i = new Intent(getActivity(), MainActivity.class);
                i.setAction(IntentIntegrator.ACTION_REMOVE_FRAGMENT);
                i.putExtra(IntentIntegrator.REMOVE_FRAGMENT_TAG, fragmentTransactionTag);
                i.putExtra(IntentIntegrator.REMOVE_LOADER_ID, loaderId);
                i.putExtra(IntentIntegrator.REMOVE_FRAGMENT_WAITING, true);
                i.putExtras(getArguments());
                getActivity().startActivity(i);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<File>> arg0)
    {
    }

    /**
     * Create and start the remove Fragment Intent. It informs the activity that
     * it can remove this fragment and this loader.
     * 
     * @param f : Fragment to remove.
     * @param fragmentTransactionTag : Fragment transaction tag name.
     * @param loaderId : loader unique identifier.
     */
    public static void actionRemoveUploadFragment(Fragment f, String fragmentTransactionTag, int loaderId)
    {
        Intent i = new Intent(f.getActivity(), MainActivity.class);
        i.setAction(IntentIntegrator.ACTION_REMOVE_FRAGMENT);
        i.putExtra(IntentIntegrator.REMOVE_FRAGMENT_TAG, fragmentTransactionTag);
        i.putExtra(IntentIntegrator.REMOVE_LOADER_ID, loaderId);
        i.putExtra(IntentIntegrator.REMOVE_FRAGMENT_WAITING, true);
        f.getActivity().startActivity(i);
    }

    public static void removeFragment(Activity activity, Intent intent)
    {
        // Intent for Removing Fragment + eventual associated loader.
        if (IntentIntegrator.ACTION_REMOVE_FRAGMENT.equals(intent.getAction()))
        {
            ActionManagerListener listener = null;
            Fragment fr = getFragment(activity, intent.getExtras().getString(IntentIntegrator.REMOVE_FRAGMENT_TAG));
            if (fr != null)
            {
                if (fr instanceof EncryptionDialogFragment)
                {
                    listener = ((EncryptionDialogFragment) fr).getActionManagerListener();
                }
                FragmentDisplayer.remove(activity, fr, false);
            }

            if (intent.getExtras().getBoolean(IntentIntegrator.REMOVE_FRAGMENT_WAITING)
                    && getFragment(activity, WaitingDialogFragment.TAG) != null)
            {
                ((DialogFragment) getFragment(activity, WaitingDialogFragment.TAG)).dismiss();
            }

            int loaderId = intent.getExtras().getInt(IntentIntegrator.REMOVE_LOADER_ID);
            if (loaderId != 0)
            {
                activity.getLoaderManager().destroyLoader(loaderId);
            }

            String intentAction = intent.getExtras().getString(PARAM_INTENT_ACTION);
            if (intentAction != null && !intentAction.isEmpty())
            {
                Intent intentI = new Intent(intentAction);
                File f = (File) intent.getExtras().getSerializable(PARAM_FILE);
                Uri data = Uri.fromFile(f);

                if (Intent.ACTION_SEND.equals(intentAction))
                {
                    intentI.putExtra(Intent.EXTRA_SUBJECT, f.getName());
                    intentI.putExtra(Intent.EXTRA_STREAM, data);
                    intentI.setType(MimeTypeManager.getMIMEType(f.getName()));
                }
                else if (Intent.ACTION_VIEW.equals(intentAction))
                {
                    intentI.setDataAndType(data, intent.getExtras().getString(PARAM_FILE_MIMETYPE).toLowerCase());
                }

                try
                {
                    activity.startActivityForResult(intentI, PublicIntent.REQUESTCODE_DECRYPTED);
                }
                catch (ActivityNotFoundException e)
                {
                    if (listener != null)
                    {
                        listener.onActivityNotFoundException(e);
                    }
                }
            }
        }
    }

    private static Fragment getFragment(Activity activity, String tag)
    {
        return activity.getFragmentManager().findFragmentByTag(tag);
    }

    private void setActionManagerListener(ActionManagerListener listener)
    {
        this.listener = listener;
    }

    public ActionManagerListener getActionManagerListener()
    {
        return listener;
    }

    public String getFragmentTransactionTag()
    {
        return fragmentTransactionTag;
    }

}

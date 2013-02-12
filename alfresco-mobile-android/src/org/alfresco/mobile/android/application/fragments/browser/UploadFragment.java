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
package org.alfresco.mobile.android.application.fragments.browser;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.DocumentCreateLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ProgressNotification;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Responsible for uploading process. Fragment with no UI. This component is
 * temporary and may be replaced by an upload service manager.
 * 
 * @author Jean Marie Pascal
 */
@Deprecated
public class UploadFragment extends Fragment implements LoaderCallbacks<LoaderResult<Document>>
{
    public static final String TAG = "UploadFragment";

    public static final String ARGUMENT_FOLDER = "folder";

    public static final String ARGUMENT_CONTENT_FILE = "contentFileURI";

    public static final String ARGUMENT_CONTENT_NAME = "contentName";

    public static final String ARGUMENT_CONTENT_DESCRIPTION = "contentDescription";

    public static final String ARGUMENT_CONTENT_TAGS = "contentTags";

    /** RepositorySession */
    protected AlfrescoSession alfSession;

    Account currentAccount = null;

    private int loaderId;

    private String fragmentTransactionTag;

    private Folder parentFolder;

    public String getFragmentTransactionTag()
    {
        return fragmentTransactionTag;
    }

    public void setSession(AlfrescoSession session)
    {
        this.alfSession = session;
    }

    public UploadFragment(String id)
    {
        this.fragmentTransactionTag = id;
    }

    public static UploadFragment newInstance(Bundle b)
    {
        UploadFragment adf = new UploadFragment(b.getString(ARGUMENT_CONTENT_NAME));
        adf.setArguments(b);
        return adf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setRetainInstance(true);
        alfSession = SessionUtils.getSession(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        loaderId = DocumentCreateLoader.ID + getArguments().getString(ARGUMENT_CONTENT_NAME).hashCode();
        Loader<Object> loader = getActivity().getLoaderManager().getLoader(loaderId);
        if (loader == null)
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), UploadFragment.this);
            getActivity().getLoaderManager().getLoader(loaderId).forceLoad();
        }
        else
        {
            getActivity().getLoaderManager().initLoader(loaderId, getArguments(), UploadFragment.this);
        }
        super.onStart();
    }

    @Override
    public Loader<LoaderResult<Document>> onCreateLoader(int id, Bundle args)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(3);
        props.put(ContentModel.PROP_DESCRIPTION, args.getString(ARGUMENT_CONTENT_DESCRIPTION));
        props.put(ContentModel.PROP_TAGS, args.getStringArrayList(ARGUMENT_CONTENT_TAGS));
        props.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);

        String name = args.getString(ARGUMENT_CONTENT_NAME);
        ContentFile contentFile = (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE);
        if (contentFile != null && name != null)
        {
            currentAccount = SessionUtils.getAccount(getActivity());

            Bundle progressBundle = new Bundle();
            ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);

            // Create the first Creation Notification.
            if (f.getClass() == ContentFileProgressImpl.class)
            {
                ((ContentFileProgressImpl) f).setFilename(name);
                progressBundle.putString(ProgressNotification.PARAM_DATA_NAME, name);
            }
            else
            {
                progressBundle.putString(ProgressNotification.PARAM_DATA_NAME, f.getFile().getName());
            }

            progressBundle.putInt(ProgressNotification.PARAM_DATA_SIZE, (int) f.getFile().length());
            progressBundle.putInt(ProgressNotification.PARAM_DATA_INCREMENT, (int) (f.getFile().length() / 10));

            ProgressNotification.createProgressNotification(getActivity(), progressBundle, getActivity().getClass());

            parentFolder = (Folder) getArguments().get(ARGUMENT_FOLDER);
        }

        return new DocumentCreateLoader(getActivity(), alfSession, (Folder) getArguments().get(ARGUMENT_FOLDER),
                args.getString(ARGUMENT_CONTENT_NAME), props, (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Document>> loader, LoaderResult<Document> results)
    {
        if (results.hasException())
        {
            Log.e(TAG, Log.getStackTraceString(results.getException()));
            DocumentCreateLoader loaderD = (DocumentCreateLoader) loader;
            ContentFile contentFile = loaderD.getContentFile();
            if (contentFile != null)
            {
                // An error occurs, notify the user.
                ProgressNotification.updateProgress(contentFile.getFile().getName(),
                        ProgressNotification.FLAG_UPLOAD_ERROR);

                // During creation process, the content must be available on
                // Download area.
                // The file is move from capture to download.
                if ((Boolean) getArguments().getSerializable(CreateDocumentDialogFragment.ARGUMENT_IS_CREATION))
                {
                    final File folderStorage = StorageManager.getDownloadFolder(getActivity(), currentAccount.getUrl(),
                            currentAccount.getUsername());

                    File dlFile = new File(folderStorage, contentFile.getFileName());
                    if (dlFile.exists())
                    {
                        String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss-").format(new Date());
                        dlFile = new File(folderStorage, timeStamp + contentFile.getFileName() );
                    }

                    if (contentFile.getFile().renameTo(dlFile))
                    {
                        MessengerManager.showLongToast(getActivity(), getString(R.string.create_document_save));
                    }
                    else
                    {
                        MessengerManager.showToast(getActivity(), R.string.error_general);
                    }
                }
            }

            // The upload is done even if it's an error.
            // Remove the fragment + the loader associated.
            actionRemoveUploadFragment(UploadFragment.this, fragmentTransactionTag, loaderId);
        }
        else
        {
            Bundle args = getArguments();
            if (args != null)
            {
                // Ensure UI is updated with status.
                if (!AndroidVersion.isICSOrAbove())
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.upload_complete));
                }

                ContentFile f = (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE);
                if (f != null)
                {
                    // Notify the upload is complete.
                    ProgressNotification.updateProgress(f.getFile().getName(),
                            ProgressNotification.FLAG_UPLOAD_COMPLETED);

                    // During creation process, we remove the file from the temp
                    // capture folder.
                    if ((Boolean) getArguments().getSerializable(CreateDocumentDialogFragment.ARGUMENT_IS_CREATION))
                    {
                        f.getFile().delete();
                    }
                }

                // If we can/need to refresh the panels, do that now...
                Fragment lf = getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(getActivity()));
                if (lf != null && lf instanceof ChildrenBrowserFragment)
                {
                    Folder parentFolder = ((ChildrenBrowserFragment) lf).getParent();
                    if (parentFolder == this.parentFolder)
                    {
                        ((ChildrenBrowserFragment) lf).refresh();
                    }
                }

                // The upload is done. Remove the fragment + the loader
                // associated.
                actionRemoveUploadFragment(UploadFragment.this, fragmentTransactionTag, loaderId);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Document>> arg0)
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
        String intentId = IntentIntegrator.ACTION_REMOVE_FRAGMENT;
        Intent i = new Intent(intentId);
        i.putExtra(IntentIntegrator.REMOVE_FRAGMENT_TAG, fragmentTransactionTag);
        i.putExtra(IntentIntegrator.REMOVE_LOADER_ID, loaderId);
        f.startActivity(i);
    }
}

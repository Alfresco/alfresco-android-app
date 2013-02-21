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

import java.io.Serializable;
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
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.CipherUtils;
import org.alfresco.mobile.android.application.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.application.utils.ProgressNotification;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.intent.PublicIntent;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeCreateListener;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    private OnNodeCreateListener onCreateListener;

    /** RepositorySession */
    protected AlfrescoSession alfSession;

    public void setSession(AlfrescoSession session)
    {
        this.alfSession = session;
    }

    public static UploadFragment newInstance(Bundle b)
    {
        UploadFragment adf = new UploadFragment();
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
        if (getLoaderManager().getLoader(DocumentCreateLoader.ID) == null)
        {
            onCreateListener = nodeCreateListener;
            getLoaderManager().initLoader(DocumentCreateLoader.ID, getArguments(), UploadFragment.this);
            getLoaderManager().getLoader(DocumentCreateLoader.ID).forceLoad();
        }
        else
        {
            getLoaderManager().initLoader(DocumentCreateLoader.ID, getArguments(), UploadFragment.this);
        }
        super.onStart();
    }

    @Override
    public Loader<LoaderResult<Document>> onCreateLoader(int id, Bundle args)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(3);
        props.put(ContentModel.PROP_DESCRIPTION, args.getString(ARGUMENT_CONTENT_DESCRIPTION));
        props.put(ContentModel.PROP_TAGS, args.getStringArrayList(ARGUMENT_CONTENT_TAGS));
        props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        if (onCreateListener != null)
        {
            onCreateListener.beforeContentCreation((Folder) getArguments().get(ARGUMENT_FOLDER),
                    args.getString(ARGUMENT_CONTENT_NAME), props,
                    (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE));
        }

        return new DocumentCreateLoader(getActivity(), alfSession, (Folder) getArguments().get(ARGUMENT_FOLDER),
                args.getString(ARGUMENT_CONTENT_NAME), props, (ContentFile) args.getSerializable(ARGUMENT_CONTENT_FILE));
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Document>> loader, LoaderResult<Document> results)
    {
        if (results.hasException())
        {
            MessengerManager.showLongToast(getActivity(), results.getException().getMessage());
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        }

        if (onCreateListener != null)
        {
            if (results.hasException())
            {
                onCreateListener.onExeceptionDuringCreation(results.getException());
            }
            else
            {
                onCreateListener.afterContentCreation(results.getData());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Document>> arg0)
    {
    }

    OnNodeCreateListener nodeCreateListener = new OnNodeCreateListener()
    {
        Folder parentFolder = null;

        @Override
        public void beforeContentCreation(Folder parentFolder, String name, Map<String, Serializable> props,
                ContentFile contentFile)
        {
            if (contentFile != null)
            {
                Bundle progressBundle = new Bundle();
                ContentFile f = (ContentFile) getArguments().getSerializable(ARGUMENT_CONTENT_FILE);

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

                ProgressNotification
                        .createProgressNotification(getActivity(), progressBundle, getActivity().getClass());

                this.parentFolder = parentFolder;
            }
        }

        @Override
        public void afterContentCreation(Node node)
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
                    ProgressNotification.updateProgress(f.getFile().getName(),
                            ProgressNotification.FLAG_UPLOAD_COMPLETED);
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
                
                String filename = f.getFile().getPath();
                if (StorageManager.shouldEncryptDecrypt(getActivity(), filename))
                {
                    try
                    {
                        CipherUtils.encryptFile(getActivity(), filename, true);
                    }
                    catch (Exception e)
                    {
                        MessengerManager.showToast(getActivity(), getString(R.string.encryption_failed));
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onExeceptionDuringCreation(Exception e)
        {
            CloudExceptionUtils.handleCloudException(getActivity(), e, false);
        }
    };
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            try
            {
                String filename = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("RequiresEncrypt", "");
                if (filename != null && filename.length() > 0)
                {
                    if (!CipherUtils.encryptFile(getActivity(), filename, true) == false)
                        MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                    else
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("RequiresEncrypt", "").commit();
                }
            }
            catch (Exception e)
            {
                MessengerManager.showLongToast(getActivity(), getString(R.string.encryption_failed));
                e.printStackTrace();
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

}

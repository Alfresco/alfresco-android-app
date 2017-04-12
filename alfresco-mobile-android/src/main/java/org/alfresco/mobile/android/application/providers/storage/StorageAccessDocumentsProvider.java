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
package org.alfresco.mobile.android.application.providers.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.mobile.android.api.exceptions.AlfrescoException;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.KeywordSearchOptions;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.impl.OAuth2DataImpl;
import org.alfresco.mobile.android.api.utils.DateUtils;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.configuration.ConfigurableActionHelper;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.update.UpdateContentRequest;
import org.alfresco.mobile.android.async.utils.ContentFileProgressImpl;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccountManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.provider.AlfrescoContentProvider;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.security.EncryptionUtils;
import org.alfresco.mobile.android.platform.utils.ConnectivityUtils;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.FileObserver;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class StorageAccessDocumentsProvider extends DocumentsProvider implements AlfrescoContentProvider
{
    // //////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // //////////////////////////////////////////////////////////////////////
    private static final String TAG = StorageAccessDocumentsProvider.class.getSimpleName();

    private static final int PREFIX_ACCOUNT = 1;

    private static final int PREFIX_ROOT_MENU = 2;

    private static final int PREFIX_SITE = 4;

    private static final int PREFIX_DOC = 8;

    private static final int PREFIX_SYNC = 16;

    private static final String QUERY_RECENT = "SELECT * FROM cmis:document WHERE cmis:lastModificationDate > TIMESTAMP '%s' ORDER BY cmis:lastModificationDate DESC";

    private static final String[] DEFAULT_ROOT_PROJECTION = AlfrescoContract.DEFAULT_ROOT_PROJECTION;

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = AlfrescoContract.DEFAULT_DOCUMENT_PROJECTION;

    @SuppressWarnings("serial")
    private static final List<String> IMPORT_FOLDER_LIST = new ArrayList<String>(3)
    {
        {
            add(String.valueOf(R.string.menu_browse_sites));
            add(String.valueOf(R.string.menu_browse_favorites_folder));
            add(String.valueOf(R.string.menu_browse_sync_content_folder));
        }
    };

    // //////////////////////////////////////////////////////////////////////
    // MEMBERS
    // //////////////////////////////////////////////////////////////////////
    private AlfrescoSession session;

    private String mAuthority;

    private final ConcurrentHashMap<Uri, Boolean> mLoadingUris = new ConcurrentHashMap<Uri, Boolean>();

    protected Map<String, Node> nodesIndex = new HashMap<String, Node>();

    protected Map<String, Node> pathIndex = new HashMap<String, Node>();

    protected Map<String, Site> siteIndex = new HashMap<String, Site>();

    protected Map<String, Long> syncIndex = new HashMap<String, Long>();

    protected Folder parentFolder;

    private LongSparseArray<AlfrescoAccount> accountsIndex;

    private Map<Long, AlfrescoSession> sessionIndex;

    private AlfrescoAccount selectedAccount;

    private Long selectedAccountId;

    private String selectedUrl;

    private Folder currentFolder;

    protected org.alfresco.mobile.android.api.model.Document createdNode;

    protected AlfrescoException exception;

    private int accountType;

    private OAuthData oauthdata;

    private SessionManager sessionManager;

    // //////////////////////////////////////////////////////////////////////
    // INIT
    // //////////////////////////////////////////////////////////////////////
    @Override
    public void attachInfo(Context context, ProviderInfo info)
    {
        mAuthority = info.authority;
        super.attachInfo(context, info);
    }

    @Override
    public boolean onCreate()
    {
        initAccounts();
        return true;
    }

    // //////////////////////////////////////////////////////////////////////
    // PROVIDER METHODS
    // //////////////////////////////////////////////////////////////////////
    // Roots == Alfresco Accounts
    // Can be Alfresco Cloud or Alfresco On Premise
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException
    {
        final DocumentFolderCursor rootCursor = new DocumentFolderCursor(resolveRootProjection(projection));
        final Uri uri = DocumentsContract.buildRootsUri(mAuthority);
        try
        {
            for (int i = 0; i < accountsIndex.size(); i++)
            {
                addRootRow(rootCursor, accountsIndex.get(accountsIndex.keyAt(i)));
            }
        }
        catch (Exception e)
        {
            rootCursor.setErrorInformation("Error : " + e.getMessage());
            rootCursor.setNotificationUri(getContext().getContentResolver(), uri);
            getContext().getContentResolver().notifyChange(uri, null);
            Log.w(TAG, Log.getStackTraceString(e));
        }

        return rootCursor;
    }

    @Override
    public Cursor queryChildDocuments(final String parentDocumentId, String[] projection, String sortOrder)
            throws FileNotFoundException
    {
        // Log.d(TAG, "Query Children : " + parentDocumentId);
        final DocumentFolderCursor docsCursor = new DocumentFolderCursor(resolveDocumentProjection(projection));
        Uri uri = DocumentsContract.buildChildDocumentsUri(mAuthority, parentDocumentId);
        // Log.d(TAG, "Query Children : " + uri);
        EncodedQueryUri cUri = new EncodedQueryUri(parentDocumentId);

        // Dispatch value
        try
        {
            // Flag to detect loading in progress
            Boolean active = mLoadingUris.get(uri);

            switch (cUri.type)
            {

                case PREFIX_ACCOUNT:
                    // First Rows after AlfrescoAccount selection
                    // Display Top level Entry Points
                    retrieveRootMenuChildren(uri, cUri, docsCursor);
                    break;

                case PREFIX_ROOT_MENU:
                    int id = Integer.parseInt(cUri.id);
                    switch (id)
                    {
                        case R.string.menu_browse_sites:
                            // List of Sites
                            if (active != null && !active)
                            {
                                fillSitesChildren(uri, active, docsCursor);
                            }
                            else
                            {
                                retrieveSitesChildren(uri, cUri, docsCursor);
                            }
                            break;

                        case R.string.menu_browse_favorites_folder:
                            // List favorite folders
                            if (active != null && !active)
                            {
                                fillNodeChildren(uri, active, docsCursor);
                            }
                            else
                            {
                                retrieveFavoriteFoldersChildren(uri, cUri, docsCursor);
                            }
                            break;

                        case R.string.menu_browse_sync_content_folder:
                            // List sync content folders
                            if (active != null && !active)
                            {
                                fillSyncContentChildren(uri, active, docsCursor);
                            }
                            else
                            {
                                retrieveSyncContentFoldersChildren(uri, cUri, docsCursor);
                            }
                            break;

                        default:
                            break;
                    }
                    break;
                case PREFIX_SITE:
                    // List children for a specific site
                    // i.e Document Library Children
                    if (active != null && !active)
                    {
                        fillNodeChildren(uri, active, docsCursor);
                    }
                    else
                    {
                        retrieveSiteDocumentLibraryChildren(uri, cUri, docsCursor);
                    }
                    break;

                case PREFIX_DOC:
                    // Children browsing
                    if (parentDocumentId == null) { return docsCursor; }

                    if (active != null && !active)
                    {
                        fillNodeChildren(uri, active, docsCursor);
                    }
                    else
                    {
                        retrieveFolderChildren(uri, cUri, docsCursor);
                    }
                    break;
                case PREFIX_SYNC:
                    // Children browsing
                    if (active != null && !active)
                    {
                        fillSyncContentChildren(uri, active, docsCursor);
                    }
                    else
                    {
                        retrieveSyncContentFoldersChildren(uri, cUri, docsCursor);
                    }
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            docsCursor.setErrorInformation("Error : " + e.getMessage());
            docsCursor.setNotificationUri(getContext().getContentResolver(), uri);
            getContext().getContentResolver().notifyChange(uri, null);
            Log.w(TAG, Log.getStackTraceString(e));
        }

        return docsCursor;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException
    {
        // Log.d(TAG, "Query Document : " + documentId);
        final DocumentFolderCursor docsCursor = new DocumentFolderCursor(resolveDocumentProjection(projection));
        Uri uri = DocumentsContract.buildDocumentUri(mAuthority, documentId);

        try
        {
            EncodedQueryUri cUri = new EncodedQueryUri(documentId);

            if (cUri.id != null)
            {
                if (nodesIndex.containsKey(cUri.id))
                {
                    addNodeRow(docsCursor, nodesIndex.get(cUri.id));
                }
                else if (pathIndex.containsKey(cUri.id))
                {
                    addNodeRow(docsCursor, pathIndex.get(cUri.id));
                }
                else if (siteIndex.containsKey(cUri.id))
                {
                    addSiteRow(docsCursor, siteIndex.get(cUri.id));
                }
                else if (IMPORT_FOLDER_LIST.contains(cUri.id))
                {
                    addRootMenuRow(docsCursor, Integer.parseInt(cUri.id));
                }
                else if (syncIndex.containsKey(cUri.id))
                {
                    addSyncContentRow(docsCursor, syncIndex.get(cUri.id));
                }
            }
            else
            {
                // Is it synced ?
                if (cUri.type == PREFIX_SYNC)
                {
                    addSyncContentRow(docsCursor, cUri.cid);
                    return docsCursor;
                }

                // Log.d(TAG, "Default Row " + documentId);
                DocumentFolderCursor.RowBuilder row = docsCursor.newRow();
                row.add(Document.COLUMN_DOCUMENT_ID,
                        EncodedQueryUri.encodeItem(PREFIX_ACCOUNT, cUri.accountId, cUri.id));
                row.add(Document.COLUMN_DISPLAY_NAME, cUri.id);
                row.add(Document.COLUMN_SIZE, null);
                row.add(Document.COLUMN_LAST_MODIFIED, null);
                row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
                row.add(Document.COLUMN_ICON, null);
            }
        }
        catch (Exception e)
        {
            docsCursor.setErrorInformation("Error : " + e.getMessage());
            docsCursor.setNotificationUri(getContext().getContentResolver(), uri);
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return docsCursor;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal)
            throws FileNotFoundException
    {
        Log.d(TAG, "Open Document : " + documentId + " - Mode " + mode);
        try
        {
            // Check the mode
            EncodedQueryUri cUri = new EncodedQueryUri(documentId);
            final int accessMode = ParcelFileDescriptor.parseMode(mode);
            final boolean isWrite = (mode.indexOf('w') != -1);

            if (cUri.type == PREFIX_SYNC)
            {
                Cursor itemCursor = getContext().getContentResolver().query(SyncContentManager.getUri(cUri.cid),
                        SyncContentSchema.COLUMN_ALL, null, null, null);
                if (itemCursor.getCount() == 1 && itemCursor.moveToNext())
                {
                    String nodeRefId = itemCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);
                    String nodeId = NodeRefUtils
                            .getNodeIdentifier(itemCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));

                    // Offline creation?
                    if (nodeId == null)
                    {
                        nodeId = Long.toString(cUri.cid);
                    }

                    String nodeName = itemCursor.getString(SyncContentSchema.COLUMN_TITLE_ID);

                    File downloadedFile = SyncContentManager.getInstance(getContext()).getSyncFile(selectedAccount,
                            nodeName, nodeId);

                    // Is Synced doc ?
                    if (downloadedFile != null && downloadedFile.exists())
                    {
                        if (DataProtectionManager.getInstance(getContext()).isEncryptionEnable()
                                && DataProtectionManager.getInstance(getContext())
                                        .isEncrypted(downloadedFile.getPath()))
                        {
                            Log.d(TAG, "Decrypt : " + downloadedFile.getPath());
                            // Decrypt now !
                            EncryptionUtils.decryptFile(getContext(), downloadedFile.getPath());
                        }

                        Log.d(TAG, "Create Sync File Descriptor : " + downloadedFile.getPath());

                        // Analytics
                        AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                                AnalyticsManager.ACTION_OPEN,
                                MimeTypeManager.getInstance(getContext()).getMIMEType(downloadedFile.getName()), 1,
                                false);

                        // Document available locally
                        return createSyncFileDescriptor(nodeRefId, isWrite, downloadedFile, accessMode);
                    }
                }

                return null;
            }

            checkSession(cUri);

            Node currentNode = retrieveNode(cUri.id);
            try
            {
                // DocumentId can be an old one stored as "recent doc"
                // This id might have been updated/changed until the last access
                // That's why We ALWAYS request the latest version
                // Log.d(TAG, "retrieve latest version");
                currentNode = session.getServiceRegistry().getVersionService()
                        .getLatestVersion((org.alfresco.mobile.android.api.model.Document) currentNode);
            }
            catch (AlfrescoServiceException e)
            {
                // Specific edge case on old version of Alfresco
                // the first version number can be 0.1 instead of 1.0
                // So we try to find this version instead the default 1.0
                String id = NodeRefUtils.getCleanIdentifier(cUri.id);
                if (session instanceof RepositorySession)
                {
                    id = NodeRefUtils.createNodeRefByIdentifier(id) + ";0.1";
                }
                currentNode = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(id);
                currentNode = session.getServiceRegistry().getVersionService()
                        .getLatestVersion((org.alfresco.mobile.android.api.model.Document) currentNode);
            }
            nodesIndex.put(NodeRefUtils.getVersionIdentifier(cUri.id), currentNode);
            nodesIndex.put(NodeRefUtils.getVersionIdentifier(currentNode.getIdentifier()), currentNode);

            // Check Document has Content
            if (currentNode.isFolder()) { return null; }

            // It's a document so let's write the local file!
            // Store the document inside a temporary folder per account
            File downloadedFile = null;
            if (getContext() != null && currentNode != null && session != null)
            {
                File folder;
                if (cUri.type != PREFIX_SYNC)
                {
                    folder = AlfrescoStorageManager.getInstance(getContext()).getShareFolder(selectedAccount);
                    if (folder != null)
                    {
                        String extension = MimeTypeManager.getExtension(currentNode.getName());
                        String name = NodeRefUtils.getVersionIdentifier(currentNode.getIdentifier());
                        if (!TextUtils.isEmpty(extension))
                        {
                            name = name.concat(".").concat(extension);
                        }
                        downloadedFile = new File(folder, name);
                    }
                }
                else
                {
                    downloadedFile = SyncContentManager.getInstance(getContext()).getSyncFile(selectedAccount,
                            currentNode);
                }
            }

            Log.d(TAG, "Doc in cache : " + (downloadedFile != null) + " - Mode "
                    + currentNode.getModifiedAt().getTimeInMillis() + " < " + downloadedFile.lastModified());

            // Is Synced doc ?
            if (cUri.type == PREFIX_SYNC && downloadedFile != null && downloadedFile.exists())
            {
                Log.d(TAG, "Doc synced : " + downloadedFile.getPath() + " - Mode " + mode);

                // Analytics
                AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                        AnalyticsManager.ACTION_OPEN,
                        currentNode.isDocument() ? ((org.alfresco.mobile.android.api.model.Document) currentNode)
                                .getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER,
                        1, false);

                // Document available locally
                return createFileDescriptor((org.alfresco.mobile.android.api.model.Document) currentNode, isWrite,
                        downloadedFile, accessMode);
            }

            // Is Document in cache ?
            if (downloadedFile != null && downloadedFile.exists())
            {
                Log.d(TAG, "Doc in cache : " + downloadedFile.getPath() + " - Mode " + mode);

                // Analytics
                AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                        AnalyticsManager.ACTION_OPEN,
                        currentNode.isDocument() ? ((org.alfresco.mobile.android.api.model.Document) currentNode)
                                .getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER,
                        1, false);

                // Document available locally
                return createFileDescriptor((org.alfresco.mobile.android.api.model.Document) currentNode, isWrite,
                        downloadedFile, accessMode);
            }

            // Not in cache so let's download the content if it has content !
            if (((org.alfresco.mobile.android.api.model.Document) currentNode).getContentStreamLength() != 0)
            {
                Log.d(TAG, "Checking Doc : " + downloadedFile.getPath() + " - Mode " + mode);
                ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService()
                        .getContentStream((org.alfresco.mobile.android.api.model.Document) currentNode);

                // Check Stream
                if (contentStream == null || contentStream.getLength() == 0)
                {
                    Log.d(TAG, "Create Doc : " + downloadedFile.getPath());
                    downloadedFile.createNewFile();
                }
                else
                {
                    Log.d(TAG, "Retrieve Doc : " + downloadedFile.getPath());
                    // Copy the content locally.
                    copyFile(contentStream.getInputStream(), contentStream.getLength(), downloadedFile, signal);
                }

            }
            else
            {
                Log.d(TAG, "Create Doc : " + downloadedFile.getPath() + " - Mode " + mode);

                downloadedFile.createNewFile();
            }

            if (downloadedFile.exists())
            {
                // Analytics
                AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                        AnalyticsManager.ACTION_OPEN,
                        currentNode.isDocument() ? ((org.alfresco.mobile.android.api.model.Document) currentNode)
                                .getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER,
                        1, false);

                // Document available locally
                return createFileDescriptor((org.alfresco.mobile.android.api.model.Document) currentNode, isWrite,
                        downloadedFile, accessMode);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            throw new FileNotFoundException("Unable to find this document");
        }
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal)
            throws FileNotFoundException
    {
        // Log.v(TAG, "openDocumentThumbnail");
        try
        {
            Node currentNode = null;
            EncodedQueryUri cUri = new EncodedQueryUri(documentId);
            if (cUri.type != PREFIX_DOC) { return null; }
            checkSession(cUri);

            currentNode = retrieveNode(cUri.id);

            // Let's retrieve the thumbnail
            // Store the document inside a temporary folder per account
            File downloadedFile = null;
            if (getContext() != null && currentNode != null && session != null)
            {
                File folder = AlfrescoStorageManager.getInstance(getContext()).getTempFolder(selectedAccount);
                if (folder != null)
                {
                    downloadedFile = new File(folder, currentNode.getName());
                }
            }
            else
            {
                return null;
            }

            // Is Document in cache ?
            if (downloadedFile.exists()
                    && currentNode.getModifiedAt().getTimeInMillis() < downloadedFile.lastModified())
            {
                // Document available locally
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(downloadedFile,
                        ParcelFileDescriptor.MODE_READ_ONLY);
                return new AssetFileDescriptor(pfd, 0, downloadedFile.length());
            }

            // Not in cache so let's download the content !
            ContentStream contentStream = session.getServiceRegistry().getDocumentFolderService()
                    .getRenditionStream(currentNode, DocumentFolderService.RENDITION_THUMBNAIL);

            // Check ContentStream
            if (contentStream == null || contentStream.getLength() == 0) { return null; }

            // Store the thumbnail locally
            copyFile(contentStream.getInputStream(), contentStream.getLength(), downloadedFile, signal);

            // Return the fileDescriptor
            if (downloadedFile.exists())
            {
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(downloadedFile,
                        ParcelFileDescriptor.MODE_READ_ONLY);
                return new AssetFileDescriptor(pfd, 0, downloadedFile.length());
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    @Override
    public Cursor querySearchDocuments(String rootId, final String query, String[] projection)
            throws FileNotFoundException
    {
        final DocumentFolderCursor documentFolderCursor = new DocumentFolderCursor(
                resolveDocumentProjection(projection));
        Uri uri = DocumentsContract.buildSearchDocumentsUri(mAuthority, rootId, query);
        final EncodedQueryUri cUri = new EncodedQueryUri(rootId);

        Boolean active = mLoadingUris.get(uri);

        if (active != null)
        {
            for (Entry<String, Node> nodeEntry : nodesIndex.entrySet())
            {
                addNodeRow(documentFolderCursor, nodeEntry.getValue());
            }
            if (!active)
            {
                // loading request is finished and refreshed
                mLoadingUris.remove(uri);
            }
        }

        if (active == null)
        {
            new StorageProviderAsyncTask(uri, documentFolderCursor, true)
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    checkSession(cUri);

                    List<Node> nodes = session.getServiceRegistry().getSearchService().keywordSearch(query,
                            new KeywordSearchOptions());

                    for (Node node : nodes)
                    {
                        nodesIndex.put(NodeRefUtils.getVersionIdentifier(node.getIdentifier()), node);
                    }

                    return null;
                }
            }.execute();
        }
        return documentFolderCursor;
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException
    {
        // Log.v(TAG, "queryRecentDocuments" + rootId);

        final DocumentFolderCursor recentDocumentsCursor = new DocumentFolderCursor(
                resolveDocumentProjection(projection));
        Uri uri = DocumentsContract.buildRecentDocumentsUri(mAuthority, rootId);
        final EncodedQueryUri cUri = new EncodedQueryUri(rootId);

        Boolean active = mLoadingUris.get(uri);

        if (active != null)
        {
            for (Entry<String, Node> nodeEntry : nodesIndex.entrySet())
            {
                addNodeRow(recentDocumentsCursor, nodeEntry.getValue());
            }
            if (!active)
            {
                // loading request is finished and refreshed
                mLoadingUris.remove(uri);
            }
        }

        if (active == null)
        {
            new StorageProviderAsyncTask(uri, recentDocumentsCursor, true)
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        checkSession(cUri);
                        GregorianCalendar calendar = new GregorianCalendar();
                        calendar.add(Calendar.DAY_OF_YEAR, -7);
                        String formatedDate = DateUtils.format(calendar);
                        List<Node> nodes = session.getServiceRegistry().getSearchService()
                                .search(String.format(QUERY_RECENT, formatedDate), SearchLanguage.CMIS);

                        for (Node node : nodes)
                        {
                            nodesIndex.put(NodeRefUtils.getVersionIdentifier(node.getIdentifier()), node);
                        }

                    }
                    catch (Exception e)
                    {
                        exception = null;
                        Log.w(TAG, Log.getStackTraceString(e));
                    }
                    return null;
                }
            }.execute();
        }
        return recentDocumentsCursor;
    }

    @Override
    public String createDocument(final String parentDocumentId, String mimeType, final String displayName)
            throws FileNotFoundException
    {
        // Log.v(TAG, "createDocument " + parentDocumentId);

        EncodedQueryUri cUri = new EncodedQueryUri(parentDocumentId);

        Node parentFolder = null;
        if (cUri.type != PREFIX_SYNC)
        {
            if (nodesIndex.containsKey(cUri.id))
            {
                parentFolder = nodesIndex.get(cUri.id);
            }
            else if (pathIndex.containsKey(cUri.id))
            {
                parentFolder = nodesIndex.get(cUri.id);
            }

            if (parentFolder == null)
            {
                parentFolder = session.getServiceRegistry().getDocumentFolderService()
                        .getNodeByIdentifier(getIdentifier(cUri.id));
            }
        }

        try
        {
            // Analytics
            AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                    AnalyticsManager.ACTION_CREATE, mimeType, 1, false);

            // Flag to detect loading in progress
            Boolean active = mLoadingUris.get(cUri);

            switch (cUri.type)
            {
                case PREFIX_SYNC:
                    // Create URI
                    Uri uri = SyncContentManager.getInstance(getContext()).createTmpSyncFile(selectedAccount,
                            displayName, cUri.id, mimeType);

                    Log.v(TAG, "createTmpSyncFile " + uri);

                    // Update with file information
                    File file = SyncContentManager.getInstance(getContext()).getSyncFile(selectedAccount, displayName,
                            uri.getLastPathSegment());
                    file.createNewFile();

                    ContentValues cValues = new ContentValues();
                    cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, file.length());
                    cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, file.length());
                    cValues.put(SyncContentSchema.COLUMN_LOCAL_URI, Uri.fromFile(file).toString());
                    SyncContentManager.getInstance(getContext()).update(uri, cValues);

                    Log.v(TAG, "return createTmpSyncFile " + EncodedQueryUri.encodeItem(PREFIX_SYNC, cUri.accountId,
                            null, Long.parseLong(uri.getLastPathSegment())));

                    syncIndex.put(uri.getLastPathSegment(), Long.parseLong(uri.getLastPathSegment()));

                    return EncodedQueryUri.encodeItem(PREFIX_SYNC, cUri.accountId, null,
                            Long.parseLong(uri.getLastPathSegment()));
                default:
                    createdNode = session.getServiceRegistry().getDocumentFolderService()
                            .createDocument((Folder) parentFolder, displayName, null, null);

                    nodesIndex.put(NodeRefUtils.getVersionIdentifier(createdNode.getIdentifier()), createdNode);
                    return EncodedQueryUri.encodeItem(PREFIX_DOC, cUri.accountId,
                            NodeRefUtils.getVersionIdentifier(createdNode.getIdentifier()));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public void deleteDocument(final String documentId) throws FileNotFoundException
    {
        // Log.v(TAG, "deleteDocument");
        final Uri uri = DocumentsContract.buildDocumentUri(mAuthority, documentId);

        final EncodedQueryUri cUri = new EncodedQueryUri(documentId);

        Boolean active = mLoadingUris.get(uri);

        if (active != null && !active)
        {
            // loading request is finished and refreshed
            mLoadingUris.remove(uri);
        }

        if (active == null && session != null)
        {
            mLoadingUris.put(uri, Boolean.TRUE);

            new AsyncTask<Void, Void, Void>()
            {

                @Override
                protected Void doInBackground(Void... params)
                {
                    checkSession(cUri);
                    Node currentNode = retrieveNode(cUri.id);
                    session.getServiceRegistry().getDocumentFolderService().deleteNode(currentNode);

                    // Analytics
                    AnalyticsHelper.reportOperationEvent(getContext(), AnalyticsManager.CATEGORY_DOC_PROVIDER,
                            AnalyticsManager.ACTION_DELETE,
                            currentNode.isDocument() ? ((org.alfresco.mobile.android.api.model.Document) currentNode)
                                    .getContentStreamMimeType() : AnalyticsManager.TYPE_FOLDER,
                            1, false);

                    return null;
                }

                protected void onPostExecute(Void noResult)
                {
                    mLoadingUris.put(uri, Boolean.FALSE);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }.execute();
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // PROJECTION
    // //////////////////////////////////////////////////////////////////////

    /**
     * @param projection the requested root column projection
     * @return either the requested root column projection, or the default
     *         projection if the requested projection is null.
     */
    private static String[] resolveRootProjection(String[] projection)
    {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection)
    {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    // //////////////////////////////////////////////////////////////////////
    // CHECK SESSION
    // //////////////////////////////////////////////////////////////////////
    private void initAccounts()
    {
        // Refresh in case of crash
        if (accountsIndex == null || accountsIndex.size() == 0)
        {
            List<AlfrescoAccount> accounts = AlfrescoAccountManager.retrieveAccounts(getContext());
            accountsIndex = new LongSparseArray<AlfrescoAccount>(accounts.size());

            sessionIndex = new HashMap<Long, AlfrescoSession>(accounts.size());
            sessionManager = SessionManager.getInstance(getContext());
            for (AlfrescoAccount account : accounts)
            {
                accountsIndex.put(account.getId(), account);
                if (sessionManager != null && sessionManager.getSession(account.getId()) != null)
                {
                    sessionIndex.put(account.getId(),
                            SessionManager.getInstance(getContext()).getSession(account.getId()));
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // ROOTS
    // //////////////////////////////////////////////////////////////////////
    private void addRootRow(DocumentFolderCursor result, AlfrescoAccount account)
    {
        DocumentFolderCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, EncodedQueryUri.encodeItem(PREFIX_ACCOUNT, account.getId(), null));
        row.add(Root.COLUMN_SUMMARY, account.getUsername());
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH | Root.FLAG_SUPPORTS_RECENTS);
        row.add(Root.COLUMN_TITLE, account.getTitle());
        row.add(Root.COLUMN_DOCUMENT_ID, EncodedQueryUri.encodeItem(PREFIX_ACCOUNT, account.getId(), null));

        accountType = account.getTypeId();
        switch (account.getTypeId())
        {
            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                row.add(Root.COLUMN_TITLE, getContext().getString(R.string.account_alfresco_cloud));
                break;
            case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                row.add(Root.COLUMN_TITLE, getContext().getString(R.string.account_alfresco));
                break;
            default:
                break;
        }
        row.add(Root.COLUMN_ICON, R.drawable.ic_application_logo);
    }

    // //////////////////////////////////////////////////////////////////////
    // ROOT MENU
    // //////////////////////////////////////////////////////////////////////
    private void retrieveRootMenuChildren(Uri uri, final EncodedQueryUri row, DocumentFolderCursor rootMenuCursor)
    {
        // Retrieve and init accounts
        selectedAccount = accountsIndex.get(row.accountId);
        selectedAccountId = selectedAccount.getId();
        accountType = selectedAccount.getTypeId();
        selectedUrl = selectedAccount.getUrl();

        Boolean isLoading = mLoadingUris.get(uri);
        Boolean available = (sessionIndex.containsKey(selectedAccountId)
                && sessionIndex.get(selectedAccountId) != null);

        // Log.v(TAG, "isLoading " + isLoading + " available " + available);

        if (isLoading != null && !isLoading && !available)
        {
            session = sessionIndex.get(selectedAccountId);
            isLoading = null;
        }

        if (isLoading != null || available)
        {
            fillRootMenuCursor(uri, isLoading, rootMenuCursor);
            return;
        }

        if (!ConnectivityUtils.hasInternetAvailable(getContext()))
        {
            fillRootMenuCursor(uri, isLoading, rootMenuCursor);
            return;
        }

        if (isLoading == null)
        {
            new StorageProviderAsyncTask(uri, rootMenuCursor)
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    if (!ConnectivityUtils.hasInternetAvailable(getContext())) { return null; }
                    try
                    {
                        switch (accountType)
                        {
                            case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                                oauthdata = new OAuth2DataImpl(getContext().getString(R.string.oauth_api_key),
                                        getContext().getString(R.string.oauth_api_secret),
                                        selectedAccount.getAccessToken(), selectedAccount.getRefreshToken());
                                session = CloudSession.connect(oauthdata);
                                break;
                            case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                                session = RepositorySession.connect(selectedUrl, selectedAccount.getUsername(),
                                        selectedAccount.getPassword());
                                break;
                            default:
                                break;
                        }
                        sessionIndex.put(selectedAccountId, session);
                    }
                    catch (AlfrescoException e)
                    {
                        exception = e;
                        Log.w(TAG, Log.getStackTraceString(e));
                    }
                    return null;
                }
            }.execute();
        }
    }

    private void fillRootMenuCursor(Uri uri, Boolean active, DocumentFolderCursor rootMenuCursor)
    {
        if (hasError(uri, active, rootMenuCursor)) { return; }

        int id = -1;

        if (ConnectivityUtils.hasInternetAvailable(getContext()))
        {
            // ONLINE
            for (String idValue : IMPORT_FOLDER_LIST)
            {
                id = Integer.parseInt(idValue);
                addRootMenuRow(rootMenuCursor, id);
            }
            if (session.getRootFolder() != null)
            {
                currentFolder = session.getRootFolder();
                addNodeRow(rootMenuCursor, session.getRootFolder(), true);
            }
        }
        else
        {
            // OFFLINE
            addRootMenuRow(rootMenuCursor, R.string.menu_browse_sync_content_folder);
        }
        removeUri(uri, active);
    }

    private void addRootMenuRow(DocumentFolderCursor rootMenuCursor, int id)
    {
        DocumentFolderCursor.RowBuilder row = rootMenuCursor.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID,
                EncodedQueryUri.encodeItem(PREFIX_ROOT_MENU, selectedAccountId, Integer.toString(id)));
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(id));
        row.add(Document.COLUMN_SIZE, 0);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_LAST_MODIFIED, null);
        row.add(Document.COLUMN_FLAGS, 0);
    }

    // //////////////////////////////////////////////////////////////////////
    // SITES
    // //////////////////////////////////////////////////////////////////////
    private void retrieveSitesChildren(Uri uri, final EncodedQueryUri row, DocumentFolderCursor sitesCursor)
    {
        new StorageProviderAsyncTask(uri, sitesCursor)
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                checkSession(row);
                List<Site> sites = session.getServiceRegistry().getSiteService().getSites();
                for (Site site : sites)
                {
                    siteIndex.put(site.getIdentifier(), site);
                }
                return null;
            }
        }.execute();
    }

    private void fillSitesChildren(Uri uri, Boolean active, DocumentFolderCursor sitesCursor)
    {
        if (hasError(uri, active, sitesCursor)) { return; }
        for (Entry<String, Site> siteEntry : siteIndex.entrySet())
        {
            addSiteRow(sitesCursor, siteEntry.getValue());
        }
        removeUri(uri, active);
    }

    private void addSiteRow(DocumentFolderCursor sitesCursor, Site site)
    {
        DocumentFolderCursor.RowBuilder row = sitesCursor.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID,
                EncodedQueryUri.encodeItem(PREFIX_SITE, selectedAccountId, site.getIdentifier()));
        row.add(Document.COLUMN_DISPLAY_NAME, site.getTitle());
        row.add(Document.COLUMN_SIZE, null);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_LAST_MODIFIED, null);
        row.add(Document.COLUMN_FLAGS, 0);
    }

    private void retrieveSiteDocumentLibraryChildren(final Uri uri, EncodedQueryUri row,
            DocumentFolderCursor sitesCursor)
    {
        checkSession(row);
        Site currentSite = null;
        if (siteIndex != null && siteIndex.containsKey(row.id))
        {
            currentSite = siteIndex.get(row.id);
        }
        else
        {
            currentSite = session.getServiceRegistry().getSiteService().getSite(row.id);
        }

        Folder documentLibraryFolder = session.getServiceRegistry().getSiteService().getDocumentLibrary(currentSite);

        retrieveFolderChildren(uri, new EncodedQueryUri(PREFIX_DOC, selectedAccountId,
                NodeRefUtils.getVersionIdentifier(documentLibraryFolder.getIdentifier())), sitesCursor);
    }

    // //////////////////////////////////////////////////////////////////////
    // FAVORITES FOLDER
    // //////////////////////////////////////////////////////////////////////
    private void retrieveFavoriteFoldersChildren(Uri uri, final EncodedQueryUri row,
            DocumentFolderCursor documentFolderCursor)
    {
        new StorageProviderAsyncTask(uri, documentFolderCursor, true)
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                checkSession(row);
                List<Folder> folders = session.getServiceRegistry().getDocumentFolderService().getFavoriteFolders();
                for (Node node : folders)
                {
                    nodesIndex.put(NodeRefUtils.getVersionIdentifier(node.getIdentifier()), node);
                }
                return null;
            }
        }.execute();
    }

    // //////////////////////////////////////////////////////////////////////
    // Sync Content FOLDER
    // //////////////////////////////////////////////////////////////////////
    private void retrieveSyncContentFoldersChildren(Uri uri, final EncodedQueryUri row,
            final DocumentFolderCursor syncContentCursor)
    {
        Log.v(TAG, "retrieveSyncContentFoldersChildren :" + row.toString());

        new StorageProviderAsyncTask(uri, syncContentCursor, true)
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                StringBuilder selection = new StringBuilder();
                if (selectedAccount != null)
                {
                    selection.append(SyncContentProvider.getAccountFilter(selectedAccount));
                }

                if (selection.length() > 0)
                {
                    selection.append(" AND ");
                }

                if (row.type == PREFIX_SYNC && !TextUtils.isEmpty(row.id))
                {
                    selection.append(SyncContentSchema.COLUMN_PARENT_ID).append(" == '").append(row.id).append("'");
                }
                else
                {
                    selection.append(
                            SyncContentSchema.COLUMN_IS_SYNC_ROOT + " == '" + SyncContentProvider.FLAG_SYNC_SET + "'");
                    selection.append(" OR ");
                    selection.append(
                            SyncContentSchema.COLUMN_STATUS + " == '" + SyncContentStatus.STATUS_REQUEST_USER + "'");
                }

                if (selection.length() > 0)
                {
                    selection.append(" AND ");
                }

                selection.append(SyncContentSchema.COLUMN_STATUS + " NOT IN (" + SyncContentStatus.STATUS_HIDDEN + ")");

                Log.v(TAG, "retrieveSyncContentFoldersChildren :" + selection);

                Cursor syncCursor = null;
                try
                {
                    syncCursor = getContext().getContentResolver().query(SyncContentProvider.CONTENT_URI,
                            SyncContentSchema.COLUMN_ALL, selection.toString(), null, null);

                    Log.v(TAG, "retrieveSyncContentFoldersChildren Count:" + syncCursor.getCount());

                    while (syncCursor.moveToNext())
                    {
                        syncIndex.put(
                                NodeRefUtils.getVersionIdentifier(
                                        syncCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID)),
                                syncCursor.getLong(SyncContentSchema.COLUMN_ID_ID));
                    }
                }
                catch (Exception e)
                {

                }
                finally
                {
                    CursorUtils.closeCursor(syncCursor);
                }
                return null;
            }
        }.execute();
    }

    private void fillSyncContentChildren(Uri uri, Boolean active, DocumentFolderCursor syncContentCursor)
    {
        if (hasError(uri, active, syncContentCursor)) { return; }
        for (Entry<String, Long> syncEntry : syncIndex.entrySet())
        {
            addSyncContentRow(syncContentCursor, syncEntry.getValue());
        }
        removeUri(uri, active);
    }

    private void addSyncContentRow(DocumentFolderCursor syncContentCursor, Long cid)
    {
        Cursor syncCursor = null;
        try
        {
            // Log.v(TAG, "addSyncContentRow " +
            // SyncContentManager.getUri(cid));
            Cursor itemCursor = getContext().getContentResolver().query(SyncContentManager.getUri(cid),
                    SyncContentSchema.COLUMN_ALL, null, null, null);

            int flags = 0;
            if (itemCursor.getCount() == 1 && itemCursor.moveToNext())
            {
                String mime = itemCursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID);
                if ("cm:folder".equals(mime))
                {
                    mime = null;
                    flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
                }
                else
                {
                    flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
                }

                DocumentFolderCursor.RowBuilder row = syncContentCursor.newRow();

                String nodeId = itemCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);
                nodeId = TextUtils.isEmpty(nodeId) ? null
                        : NodeRefUtils.getVersionIdentifier(itemCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                row.add(Document.COLUMN_DOCUMENT_ID,
                        EncodedQueryUri.encodeItem(PREFIX_SYNC, selectedAccountId, nodeId, cid));
                row.add(Document.COLUMN_DISPLAY_NAME, itemCursor.getString(SyncContentSchema.COLUMN_TITLE_ID));
                row.add(Document.COLUMN_SIZE, itemCursor.getLong(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES_ID));
                row.add(Document.COLUMN_MIME_TYPE, mime == null ? Document.MIME_TYPE_DIR : mime);
                row.add(Document.COLUMN_LAST_MODIFIED,
                        itemCursor.getLong(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID));

                flags |= Document.FLAG_SUPPORTS_WRITE;
                // flags |= Document.FLAG_SUPPORTS_DELETE;
                row.add(Document.COLUMN_FLAGS, flags);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            CursorUtils.closeCursor(syncCursor);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // DOCUMENTS & FOLDERS
    // //////////////////////////////////////////////////////////////////////
    private void retrieveFolderChildren(final Uri uri, final EncodedQueryUri row,
            DocumentFolderCursor documentFolderCursor)
    {
        new StorageProviderAsyncTask(uri, documentFolderCursor, true)
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    checkSession(row);

                    List<Node> nodes = new ArrayList<Node>();
                    if (row.id == null)
                    {
                        nodes = session.getServiceRegistry().getDocumentFolderService()
                                .getChildren(session.getRootFolder());
                    }
                    else
                    {
                        currentFolder = (Folder) session.getServiceRegistry().getDocumentFolderService()
                                .getNodeByIdentifier(NodeRefUtils.createNodeRefByIdentifier(row.id));
                        pathIndex.put(currentFolder.getIdentifier(), currentFolder);
                        nodes = session.getServiceRegistry().getDocumentFolderService().getChildren(currentFolder);
                    }

                    for (Node node : nodes)
                    {
                        nodesIndex.put(NodeRefUtils.getVersionIdentifier(node.getIdentifier()), node);
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

                return null;
            }
        }.execute();
    }

    private void fillNodeChildren(Uri uri, Boolean active, DocumentFolderCursor documentFolderCursor)
    {
        if (hasError(uri, active, documentFolderCursor)) { return; }

        for (Entry<String, Node> nodeEntry : nodesIndex.entrySet())
        {
            addNodeRow(documentFolderCursor, nodeEntry.getValue());
        }
        removeUri(uri, active);
    }

    private void addNodeRow(DocumentFolderCursor result, Node node)
    {
        addNodeRow(result, node, false);
    }

    private void addNodeRow(DocumentFolderCursor result, Node node, boolean isRoot)
    {
        int flags = 0;

        DocumentFolderCursor.RowBuilder row = result.newRow();

        row.add(Document.COLUMN_DOCUMENT_ID, EncodedQueryUri.encodeItem(PREFIX_DOC, selectedAccountId,
                NodeRefUtils.getVersionIdentifier(node.getIdentifier())));
        row.add(Document.COLUMN_DISPLAY_NAME,
                isRoot ? getContext().getString(R.string.menu_browse_root) : node.getName());
        if (node.isFolder())
        {
            row.add(Document.COLUMN_SIZE, null);
            row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
            // if (permission != null && permission.canAddChildren())
            if (ConfigurableActionHelper.isVisible(getContext(), selectedAccount, session, parentFolder,
                    ConfigurableActionHelper.ACTION_NODE_UPLOAD))
            {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            }
        }
        else
        {
            row.add(Document.COLUMN_SIZE,
                    ((org.alfresco.mobile.android.api.model.Document) node).getContentStreamLength());
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
            row.add(Document.COLUMN_MIME_TYPE,
                    ((org.alfresco.mobile.android.api.model.Document) node).getContentStreamMimeType());
            if (ConfigurableActionHelper.isVisible(getContext(), selectedAccount, session, parentFolder,
                    ConfigurableActionHelper.ACTION_NODE_EDIT))
            {
                flags |= Document.FLAG_SUPPORTS_WRITE;
            }

            if (ConfigurableActionHelper.isVisible(getContext(), selectedAccount, session, parentFolder,
                    ConfigurableActionHelper.ACTION_NODE_DELETE))
            {
                flags |= Document.FLAG_SUPPORTS_DELETE;
            }
        }

        row.add(Document.COLUMN_LAST_MODIFIED, isRoot ? null : node.getModifiedAt().getTimeInMillis());
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_ICON, R.drawable.ic_person_light);
        row.add(AlfrescoContract.Document.COLUMN_TYPE, node.getType());
        row.add(AlfrescoContract.Document.COLUMN_ACCOUNT_ID, selectedAccountId);
        row.add(AlfrescoContract.Document.COLUMN_PATH, currentFolder.getPropertyValue(PropertyIds.PATH) != null
                ? currentFolder.getPropertyValue(PropertyIds.PATH) != null : "/");
    }

    // //////////////////////////////////////////////////////////////////////
    // FILE DESCRIPTOR
    // //////////////////////////////////////////////////////////////////////
    private ParcelFileDescriptor createSyncFileDescriptor(final String nodeId, boolean isWrite, final File file,
            final int accessMode) throws FileNotFoundException
    {
        Log.d(TAG, "Create File Descriptor " + nodeId + " : " + file.getPath() + " - Is Write " + isWrite);

        if (isWrite)
        {
            // Attach a close listener if the document is opened in write mode.
            try
            {
                NodeFileObserver observer = new NodeFileObserver(file.getPath(), nodeId, true);
                observer.startWatching();
                return ParcelFileDescriptor.open(file, accessMode);
            }
            catch (IOException e)
            {
                throw new FileNotFoundException("Failed to open document");
            }
        }
        else
        {
            return ParcelFileDescriptor.open(file, accessMode);
        }
    }

    private ParcelFileDescriptor createFileDescriptor(final org.alfresco.mobile.android.api.model.Document currentNode,
            boolean isWrite, final File file, final int accessMode) throws FileNotFoundException
    {
        Log.d(TAG,
                "Create File Descriptor " + currentNode.getName() + " : " + file.getPath() + " - Is Write " + isWrite);

        if (isWrite)
        {
            // Attach a close listener if the document is opened in write mode.
            try
            {
                NodeFileObserver observer = new NodeFileObserver(file.getPath(), currentNode, parentFolder);
                observer.startWatching();
                return ParcelFileDescriptor.open(file, accessMode);
            }
            catch (IOException e)
            {
                throw new FileNotFoundException("Failed to open document");
            }
        }
        else
        {
            return ParcelFileDescriptor.open(file, accessMode);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // INDEX
    // //////////////////////////////////////////////////////////////////////
    public void removeUri(Uri uri, Boolean active)
    {
        if (active != null && !active)
        {
            mLoadingUris.remove(uri);
        }
    }

    private boolean hasError(Uri uri, Boolean active, DocumentFolderCursor cursor)
    {
        if (exception != null)
        {
            cursor.setErrorInformation("Error : " + exception.getMessage());
            removeUri(uri, active);
            exception = null;
            return true;
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // IOUtils
    // //////////////////////////////////////////////////////////////////////
    private static final int MAX_BUFFER_SIZE = 1024;

    public static boolean copyFile(InputStream src, long size, File dest, CancellationSignal signal)
    {
        IOUtils.ensureOrCreatePathAndFile(dest);
        OutputStream os = null;
        boolean copied = true;
        int downloaded = 0;

        try
        {
            os = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            while (size - downloaded > 0)
            {
                if (size - downloaded < MAX_BUFFER_SIZE)
                {
                    buffer = new byte[(int) (size - downloaded)];
                }

                int read = src.read(buffer);
                if (read == -1)
                {
                    break;
                }

                os.write(buffer, 0, read);
                downloaded += read;
                if (signal != null && signal.isCanceled())
                {
                    signal.throwIfCanceled();
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            copied = false;
        }
        finally
        {
            IOUtils.closeStream(src);
            IOUtils.closeStream(os);
        }
        return copied;
    }

    // //////////////////////////////////////////////////////////////////////
    // SESSION UTILITY
    // //////////////////////////////////////////////////////////////////////
    private void checkSession(EncodedQueryUri row)
    {
        // Check Session
        if (session == null)
        {
            // If no session available, try to retrieve it from
            // ApplicationManager
            sessionManager = SessionManager.getInstance(getContext());
            if (sessionManager != null && sessionManager.getSession(row.accountId) != null)
            {
                session = sessionManager.getSession(row.accountId);
                sessionIndex.put(row.accountId, session);
            }

            // If no session available, try to create a new one
            selectedAccount = accountsIndex.get(row.accountId);
            accountType = selectedAccount.getTypeId();
            selectedUrl = selectedAccount.getUrl();
            try
            {
                switch (accountType)
                {
                    case AlfrescoAccount.TYPE_ALFRESCO_CLOUD:
                        oauthdata = new OAuth2DataImpl(getContext().getString(R.string.oauth_api_key),
                                getContext().getString(R.string.oauth_api_secret), selectedAccount.getAccessToken(),
                                selectedAccount.getRefreshToken());
                        session = CloudSession.connect(oauthdata);
                        break;
                    case AlfrescoAccount.TYPE_ALFRESCO_CMIS:
                        session = RepositorySession.connect(selectedUrl, selectedAccount.getUsername(),
                                selectedAccount.getPassword());
                        break;
                    default:
                        break;
                }
                sessionIndex.put(selectedAccountId, session);
            }
            catch (AlfrescoException e)
            {
                Log.e(TAG, Log.getStackTraceString(e));
                exception = e;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Base AsyncTask
    // //////////////////////////////////////////////////////////////////////
    public abstract class StorageProviderAsyncTask extends AsyncTask<Void, Void, Void>
    {
        protected Uri uri;

        protected DocumentFolderCursor docsCursor;

        private boolean clearNodes = false;

        public StorageProviderAsyncTask(Uri uri, DocumentFolderCursor docsCursor)
        {
            this.uri = uri;
            this.docsCursor = docsCursor;
        }

        public StorageProviderAsyncTask(Uri uri, DocumentFolderCursor documentFolderCursor, boolean clearNodes)
        {
            this.uri = uri;
            this.docsCursor = documentFolderCursor;
            this.clearNodes = clearNodes;
        }

        @Override
        protected void onPreExecute()
        {
            if (clearNodes && nodesIndex != null)
            {
                nodesIndex.clear();
            }
            if (clearNodes && syncIndex != null)
            {
                syncIndex.clear();
            }
            startLoadingUri(uri, docsCursor);
        }

        protected void onPostExecute(Void noResult)
        {
            stopLoadingUri(uri);
        }

        @Override
        protected void onCancelled()
        {
            uri = null;
            docsCursor = null;
        }

        public void startLoadingUri(Uri uri, DocumentFolderCursor documentFolderCursor)
        {
            documentFolderCursor.setIsLoading(true);
            documentFolderCursor.setNotificationUri(getContext().getContentResolver(), uri);
            mLoadingUris.put(uri, Boolean.TRUE);
        }

        public void stopLoadingUri(Uri uri)
        {
            mLoadingUris.put(uri, Boolean.FALSE);
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private String getIdentifier(String cUri)
    {
        String id = cUri;
        if (session instanceof RepositorySession)
        {
            id = NodeRefUtils.createNodeRefByIdentifier(cUri);
        }
        return id;
    }

    private Node retrieveNode(String docId)
    {
        Node currentNode = null;
        // Retrieve node by its id
        if (nodesIndex.containsKey(docId))
        {
            currentNode = nodesIndex.get(docId);
        }
        else
        {
            String id = getIdentifier(docId);
            try
            {
                currentNode = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(id);
            }
            catch (AlfrescoServiceException e)
            {
                // Specific edge case on old version of Alfresco
                // the first version number can be 0.1 instead of 1.0
                // So we try to find this version instead the default 1.0
                id = NodeRefUtils.getCleanIdentifier(docId);
                if (session instanceof RepositorySession)
                {
                    id = NodeRefUtils.createNodeRefByIdentifier(id) + ";0.1";
                }
                currentNode = session.getServiceRegistry().getDocumentFolderService().getNodeByIdentifier(id);
            }
        }
        return currentNode;
    }

    private class NodeFileObserver extends FileObserver
    {
        private final File file;

        private final org.alfresco.mobile.android.api.model.Document currentNode;

        private final String nodeId;

        private final Folder parentFolder;

        private boolean modified;

        private boolean isSynced;

        public NodeFileObserver(String path, Node currentNode, Folder parentFolder)
        {
            super(path);
            this.modified = false;
            this.file = new File(path);
            this.currentNode = (org.alfresco.mobile.android.api.model.Document) currentNode;
            this.parentFolder = parentFolder;
            this.isSynced = false;
            this.nodeId = currentNode.getIdentifier();
        }

        public NodeFileObserver(String path, String nodeId, boolean isSynced)
        {
            super(path);
            this.modified = false;
            this.file = new File(path);
            this.currentNode = null;
            this.parentFolder = null;
            this.isSynced = isSynced;
            this.nodeId = nodeId;
        }

        private void onCloseEvent()
        {
            try
            {
                if (!modified) { return; }

                if (isSynced)
                {
                    // Update statut of the sync reference
                    ContentValues cValues = new ContentValues();
                    Uri localUri;
                    if (NodeRefUtils.isIdentifier(nodeId) || NodeRefUtils.isNodeRef(nodeId))
                    {
                        localUri = SyncContentManager.getInstance(getContext())
                                .getUri(SessionUtils.getAccount(getContext()), nodeId);
                        cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_MODIFIED);
                    }
                    else
                    {
                        localUri = android.net.Uri.parse(SyncContentProvider.CONTENT_URI + "/" + nodeId);
                        cValues.put(SyncContentSchema.COLUMN_STATUS, SyncContentStatus.STATUS_PENDING);
                    }

                    if (!TextUtils.isEmpty(nodeId))
                    {
                        getContext().getContentResolver().update(localUri, cValues, null, null);
                    }

                    // Sync if it's possible.
                    if ((NodeRefUtils.isIdentifier(nodeId) || NodeRefUtils.isNodeRef(nodeId)) && SyncContentManager
                            .getInstance(getContext()).canSync(SessionUtils.getAccount(getContext())))
                    {
                        SyncContentManager.getInstance(getContext()).sync(SessionUtils.getAccount(getContext()),
                                nodeId);
                    }
                    else
                    {
                        SyncContentManager.getInstance(getContext()).sync(AnalyticsManager.LABEL_SYNC_DOC_PROVIDER,
                                SessionUtils.getAccount(getContext()));
                    }
                    return;
                }

                if (file.length() == 0)
                {
                    Log.e(TAG, "Save File incorrect for " + currentNode.getName() + " : " + file.getPath());
                    return;
                }
                Log.d(TAG, "Save File " + currentNode.getName() + " : " + file.getPath());

                String mimetype = currentNode.getContentStreamMimeType();
                if (TextUtils.isEmpty(mimetype))
                {
                    mimetype = MimeTypeManager.getInstance(getContext()).getMIMEType(currentNode.getName());
                }

                if (!isSynced)
                {
                    Operator.with(getContext(), selectedAccount).load(new UpdateContentRequest.Builder(parentFolder,
                            currentNode, new ContentFileProgressImpl(file, currentNode.getName(), mimetype)));
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, Log.getStackTraceString(e));
                stopWatching();
            }
            finally
            {
                stopWatching();
            }
        }

        @Override
        public void onEvent(int event, String path)
        {
            if (event == FileObserver.MODIFY)
            {
                this.modified = true;
            }
            else if (event == FileObserver.CLOSE_WRITE)
            {
                this.onCloseEvent();
            }
        }
    }
}

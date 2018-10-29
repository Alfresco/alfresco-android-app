/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.sync.prepare;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.async.Operation;
import org.alfresco.mobile.android.async.OperationSchema;
import org.alfresco.mobile.android.async.OperationStatus;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentProvider;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContent;
import org.alfresco.mobile.android.sync.operations.SyncContentDownload;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class PrepareBaseHelper
{
    private static final String TAG = PrepareBaseHelper.class.getName();

    protected Context context;

    protected int mode;

    protected Cursor localSyncCursor;

    protected AlfrescoSession session;

    protected AlfrescoAccount acc;

    protected ListingContext listingContext;

    protected PagingResult<Document> nodeDocumentSynced;

    protected PagingResult<Folder> nodeFolderSynced;

    protected long syncScanningTimeStamp;

    protected SyncContentManager syncManager;

    protected DataProtectionManager dataProtectionManager;

    // UPDATE
    private ArrayList<String> repoFavoriteIds;

    private ArrayList<String> repoSyncIds;

    // SCAN NODE
    private Node node;

    private String nodeIdentifier;

    protected SyncResult syncResult;

    // Mode, Context
    public PrepareBaseHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result)
    {
        this.syncResult = result;
        this.context = context;
        this.session = session;
        this.acc = acc;
        this.listingContext = session.getDefaultListingContext();
        this.mode = mode;
        this.syncScanningTimeStamp = syncScanningTimeStamp;
        this.syncManager = SyncContentManager.getInstance(context);
        this.dataProtectionManager = DataProtectionManager.getInstance(context);
    }

    // Mode, Context
    public PrepareBaseHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result, Node node)
    {
        this(context, acc, session, mode, syncScanningTimeStamp, result);
        if (node != null)
        {
            this.node = node;
            this.nodeIdentifier = node.getIdentifier();
        }
    }

    // nodeIdentifier
    public PrepareBaseHelper(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result, String nodeIdentifier)
    {
        this(context, acc, session, mode, syncScanningTimeStamp, result);
        this.nodeIdentifier = nodeIdentifier;
    }

    public abstract List<SyncContent> prepare();

    protected void scan()
    {
        // Retrieve List of remote favorite
        switch (mode)
        {
            case SyncContentManager.MODE_BOTH:
                retrieveSyncedDocument();
                retrieveSyncedFolder();
                break;
            case SyncContentManager.MODE_FOLDERS:
                retrieveSyncedFolder();
                break;
            case SyncContentManager.MODE_DOCUMENTS:
                retrieveSyncedDocument();
                break;
            case SyncContentManager.MODE_NODE:
                break;
            default:
                break;
        }

        // Retrieve list of local Favorites
        localSyncCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                SyncContentSchema.COLUMN_ALL, SyncContentProvider.getAccountFilter(acc), null, null);

        // We have our favorites
        // Update the Local referential
        if (isFirstSync())
        {
            // First Sync
            prepareFirstSync();
        }
        else
        {
            // Check updated favorites
            prepareRemoteCreation();

            // Check updated favorites
            prepareUpdate();

            // Check deleted favorites
            prepareDelete();
        }

        CursorUtils.closeCursor(localSyncCursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SCAN : RETRIEVE NODES INFO
    // ///////////////////////////////////////////////////////////////////////////
    private static final String QUERY_RESTRICTABLE_DOCS = "SELECT d.cmis:objectId, d.cmis:objectTypeId, d.cmis:baseTypeId, d.cmis:name, d.cmis:createdBy, d.cmis:lastModificationDate,d.cmis:versionSeriesCheckedOutId,d.cmis:contentStreamLength,d.cmis:contentStreamMimeType,d.cmis:isVersionSeriesCheckedOut,d.cmis:versionLabel, m.dp:offlineExpiresAfter FROM cmis:document AS d JOIN dp:restrictable AS m ON d.cmis:objectId = m.cmis:objectId WHERE (d.cmis:objectId=";

    private static final String QUERY_RESTRICTABLE_FOLDER = "SELECT d.cmis:objectId, d.cmis:objectTypeId, d.cmis:baseTypeId, d.cmis:name, d.cmis:createdBy, d.cmis:lastModificationDate, m.dp:offlineExpiresAfter FROM cmis:folder AS d JOIN dp:restrictable AS m ON d.cmis:objectId = m.cmis:objectId WHERE (d.cmis:objectId=";

    private static final String QUERY_OR = " OR d.cmis:objectId=";

    private void retrieveSyncedDocument()
    {
        // Retrieve list of synced documents
        Cursor cursorNodesIds = null;
        List<String> syncedDocumentIds = null;
        try
        {
            cursorNodesIds = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL,
                    SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_IS_SYNC_ROOT
                            + " == '" + SyncContentProvider.FLAG_SYNC_SET + "' AND " + SyncContentSchema.COLUMN_MIMETYPE
                            + " != '" + ContentModel.TYPE_FOLDER + "'",
                    null, null);

            syncedDocumentIds = new ArrayList<String>(cursorNodesIds.getCount());
            while (cursorNodesIds.moveToNext())
            {
                syncedDocumentIds.add(cursorNodesIds.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            CursorUtils.closeCursor(cursorNodesIds);
        }

        // Check if restrictable is available on repo
        List<String> restrictableIds = new ArrayList<>(syncedDocumentIds.size());
        try
        {
            if (syncedDocumentIds.size() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(QUERY_RESTRICTABLE_DOCS);
                join(builder, QUERY_OR, syncedDocumentIds);
                builder.append(")");

                List<Node> restrictableNodes = session.getServiceRegistry().getSearchService()
                        .search(builder.toString(), SearchLanguage.CMIS);
                for (Node node : restrictableNodes)
                {
                    restrictableIds.add(node.getIdentifier());
                }
            }
        }
        catch (Exception e)
        {
        }

        // Retrieve list of nodes from server
        // Objects don't contain enough information
        // We request all node object with a search query
        // to retrieve ContentStreamId and permissions.
        List<Document> favoriteDocumentsList = new ArrayList<>(syncedDocumentIds.size());
        if (syncedDocumentIds.size() > 0)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM cmis:document WHERE ( cmis:objectId=");
            join(builder, " OR cmis:objectId=", syncedDocumentIds);
            builder.append(")");

            List<Node> nodes = session.getServiceRegistry().getSearchService().search(builder.toString(),
                    SearchLanguage.CMIS);

            for (Node node : nodes)
            {
                favoriteDocumentsList.add((Document) node);
            }
        }
        nodeDocumentSynced = new PagingResultImpl<>(favoriteDocumentsList, false, favoriteDocumentsList.size());

        // Check Restrictable
        if (restrictableIds != null && !restrictableIds.isEmpty())
        {
            List<Document> tmpNodes = new ArrayList<>(nodeDocumentSynced.getTotalItems());
            for (Node node : nodeDocumentSynced.getList())
            {
                if (!restrictableIds.contains(node.getIdentifier()))
                {
                    tmpNodes.add((Document) node);
                }
            }
            nodeDocumentSynced = new PagingResultImpl<>(tmpNodes, false, nodeDocumentSynced.getTotalItems());
        }
    }

    private void retrieveSyncedFolder()
    {
        // Retrieve list of synced documents
        Cursor cursorNodesIds = null;
        List<String> syncedFolderIds = null;
        try
        {
            cursorNodesIds = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL,
                    SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_IS_SYNC_ROOT
                            + " == '" + SyncContentProvider.FLAG_SYNC_SET + "' AND " + SyncContentSchema.COLUMN_MIMETYPE
                            + " == '" + ContentModel.TYPE_FOLDER + "'",
                    null, null);

            syncedFolderIds = new ArrayList<>(cursorNodesIds.getCount());
            Uri localUri = null;
            while (cursorNodesIds.moveToNext())
            {
                syncedFolderIds.add(cursorNodesIds.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            CursorUtils.closeCursor(cursorNodesIds);
        }

        // Check if restrictable is available on repo
        List<String> restrictableIds = new ArrayList<>(syncedFolderIds.size());
        try
        {
            if (syncedFolderIds.size() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(QUERY_RESTRICTABLE_FOLDER);
                join(builder, QUERY_OR, syncedFolderIds);
                builder.append(")");

                List<Node> restrictableNodes = session.getServiceRegistry().getSearchService()
                        .search(builder.toString(), SearchLanguage.CMIS);
                for (Node node : restrictableNodes)
                {
                    restrictableIds.add(node.getIdentifier());
                }
            }
        }
        catch (Exception e)
        {
        }

        // Objects don't contain enough information
        // We request all node object with a search query
        // to retrieve ContentStreamId and permissions.
        List<Folder> favoriteFoldersList = new ArrayList<>(syncedFolderIds.size());
        if (syncedFolderIds.size() > 0)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * FROM cmis:folder WHERE ( cmis:objectId=");
            join(builder, " OR cmis:objectId=", syncedFolderIds);
            builder.append(")");

            List<Node> nodes = session.getServiceRegistry().getSearchService().search(builder.toString(),
                    SearchLanguage.CMIS);

            for (Node node : nodes)
            {
                favoriteFoldersList.add((Folder) node);
            }
        }
        nodeFolderSynced = new PagingResultImpl<>(favoriteFoldersList, false, favoriteFoldersList.size());

        // Check Restrictable
        if (restrictableIds != null && !restrictableIds.isEmpty())
        {
            List<Folder> tmpNodes = new ArrayList<>(nodeFolderSynced.getTotalItems());
            for (Node node : nodeFolderSynced.getList())
            {
                if (!restrictableIds.contains(node.getIdentifier()))
                {
                    tmpNodes.add((Folder) node);
                }
            }
            nodeFolderSynced = new PagingResultImpl<>(tmpNodes, false, nodeFolderSynced.getTotalItems());
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FIRST SYNC
    // ///////////////////////////////////////////////////////////////////////////
    private boolean isFirstSync()
    {
        return (localSyncCursor == null || localSyncCursor.getCount() == 0);
    }

    private void prepareFirstSync()
    {
        // USE CASE : FIRST
        // If 0 ==> Bulk Insert
        switch (mode)
        {
            case SyncContentManager.MODE_DOCUMENTS:
                syncResult.stats.numEntries = nodeDocumentSynced.getList().size();
                for (Document doc : nodeDocumentSynced.getList())
                {
                    prepareLocalCreation(doc);
                }
                break;
            case SyncContentManager.MODE_FOLDERS:
                for (Folder folder : nodeFolderSynced.getList())
                {
                    prepareLocalCreation(folder);
                }
                break;
            case SyncContentManager.MODE_BOTH:
                for (Document doc : nodeDocumentSynced.getList())
                {
                    prepareLocalCreation(doc);
                }
                for (Folder folder : nodeFolderSynced.getList())
                {
                    prepareLocalCreation(folder);
                }
                break;
            case SyncContentManager.MODE_NODE:
                if (node.isDocument())
                {
                    syncResult.stats.numEntries = 1;
                    prepareLocalCreation((Document) node);
                }
                else
                {
                    prepareLocalCreation((Folder) node);
                }
                break;
            default:
                break;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION
    // ///////////////////////////////////////////////////////////////////////////
    private void prepareRemoteCreation()
    {
        Cursor cursorCreation = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                SyncContentSchema.COLUMN_ALL,
                SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_STATUS + " == '"
                        + OperationStatus.STATUS_PENDING + "' AND " + SyncContentSchema.COLUMN_NODE_ID + " == ''",
                null, null);

        while (cursorCreation.moveToNext())
        {
            prepareRemoteCreation(SyncContentManager.getUri(cursorCreation.getLong(SyncContentSchema.COLUMN_ID_ID)),
                    cursorCreation.getLong(SyncContentSchema.COLUMN_ID_ID));
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UPDATE
    // ///////////////////////////////////////////////////////////////////////////
    private void prepareUpdate()
    {
        repoFavoriteIds = new ArrayList<String>();

        switch (mode)
        {
            case SyncContentManager.MODE_NODE:
                List<Node> tmpNode = new ArrayList<Node>(1);
                if (node != null)
                {
                    if (node.isFolder())
                    {
                        repoSyncIds = new ArrayList<String>();
                    }
                    try
                    {
                        ((AbstractAlfrescoSessionImpl) session).getCmisSession()
                                .removeObjectFromCache(node.getIdentifier());
                        if (session.getServiceRegistry().getDocumentFolderService()
                                .getNodeByIdentifier(node.getIdentifier()) != null)
                        {
                            tmpNode.add(node);
                        }
                        syncResult.stats.numEntries = tmpNode.size();
                        prepareUpdate(tmpNode);
                    }
                    catch (Exception e)
                    {
                        // Do nothing
                        Log.d("Sync", "Sync Scan Deleted ?");
                    }
                }
                break;
            case SyncContentManager.MODE_DOCUMENTS:
                List<Node> tmpNodes = new ArrayList<Node>(nodeDocumentSynced.getList());
                syncResult.stats.numEntries = tmpNodes.size();
                prepareUpdate(tmpNodes);
                break;
            case SyncContentManager.MODE_BOTH:
                List<Node> tmpNodes2 = new ArrayList<Node>(nodeDocumentSynced.getList());
                syncResult.stats.numEntries = tmpNodes2.size();
                prepareUpdate(tmpNodes2);
            case SyncContentManager.MODE_FOLDERS:
                repoSyncIds = new ArrayList<String>();
                Cursor cursorId = null;
                try
                {
                    for (Folder favoriteFolder : nodeFolderSynced.getList())
                    {
                        repoFavoriteIds.add(NodeRefUtils.getCleanIdentifier(favoriteFolder.getIdentifier()));

                        // Check if it exists first
                        // Try to retrieve local info
                        cursorId = SyncContentManager.getCursorForId(context, acc, favoriteFolder.getIdentifier());

                        if (cursorId.moveToFirst())
                        {
                            // Is it a new Favorite ?
                            Boolean favorited = cursorId.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID) > 0;
                            if (favorited)
                            {
                                // If exist so update
                                recursiveUpdate(favoriteFolder, favoriteFolder);
                            }
                            else
                            {
                                // New favorite
                                String parent = cursorId.getString(SyncContentSchema.COLUMN_PARENT_ID_ID);

                                recursiveUpdate(favoriteFolder, favoriteFolder);

                                ContentValues cValues = new ContentValues();
                                cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, SyncContentProvider.FLAG_SYNC_SET);
                                cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parent);
                                context.getContentResolver().update(
                                        SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID)),
                                        cValues, null, null);
                            }
                        }
                        else
                        {
                            // If not create
                            prepareLocalCreation(favoriteFolder);
                        }
                        cursorId.close();
                    }
                }
                catch (Exception e)
                {

                }
                finally
                {
                    if (cursorId != null)
                    {
                        cursorId.close();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void prepareUpdate(List<Node> favoritedNode)
    {
        prepareUpdate(null, null, favoritedNode);
    }

    private void prepareUpdate(Folder favoriteRootFolder, Folder parentFolder, List<Node> childrens)
    {
        // Favorites are present.
        // Check if new, update or delete action
        Cursor cursorId = null;
        long localServerTimeStamp = -1;
        long remoteServerTimeStamp = -1;
        boolean isDocumentFavorite = (favoriteRootFolder == null && parentFolder == null);
        File localFile = null;
        boolean hasLocalModification = false;
        Uri localUri = null;
        Uri localFileUri = null;
        String localContentUri = "";
        String docContentUri = "";
        Permissions permissions = null;

        // Browse the results
        for (Node childrenNode : childrens)
        {
            if (cursorId != null)
            {
                cursorId.close();
            }

            if (isDocumentFavorite)
            {
                // Documents Favorited
                repoFavoriteIds.add(NodeRefUtils.getCleanIdentifier(childrenNode.getIdentifier()));
            }
            else
            {
                // Documents & Folders Non Favorited
                repoSyncIds.add(NodeRefUtils.getCleanIdentifier(childrenNode.getIdentifier()));
            }

            // Try to retrieve local info
            cursorId = SyncContentManager.getCursorForId(context, acc, childrenNode.getIdentifier());

            if (cursorId.moveToFirst())
            {
                localUri = SyncContentManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID));

                // NODE HAS MOVED
                // Check Parent ID if possible
                String parentId = cursorId.getString(SyncContentSchema.COLUMN_PARENT_ID_ID);
                if (parentFolder != null && !parentFolder.getIdentifier().equals(parentId))
                {
                    ContentValues cValues = new ContentValues();
                    cValues.put(OperationSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
                    context.getContentResolver().update(localUri, cValues, null, null);
                }

                // Folder
                if (childrenNode.isFolder())
                {

                    // Renamed ?
                    ContentValues cValues = new ContentValues();
                    String folderName = cursorId.getString(SyncContentSchema.COLUMN_TITLE_ID);
                    if (!childrenNode.getName().equals(folderName))
                    {
                        // FOLDER HAS BEEN RENAMED

                        cValues.put(OperationSchema.COLUMN_TITLE, childrenNode.getName());
                        context.getContentResolver().update(localUri, cValues, null, null);
                    }

                    // First creation ?
                    long folderSize = cursorId.getLong(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                    if (folderSize == -1)
                    {
                        // Retrieve folder size && Flag children
                        long size = prepareChildrenFolderCreation((Folder) childrenNode, (Folder) childrenNode);

                        // Flag the favorite
                        Uri uri = syncManager.getUri(acc, childrenNode.getIdentifier());
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, size);
                        if (size == 0)
                        {
                            contentValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
                        }
                        context.getContentResolver().update(uri, contentValues, null, null);
                    }
                    else
                    {
                        CursorUtils.closeCursor(cursorId);
                        // Browse Children nodes
                        recursiveUpdate(favoriteRootFolder, (Folder) childrenNode);
                    }
                    continue;
                }

                // Info available locally
                hasLocalModification = hasLocalModification(cursorId);
                permissions = session.getServiceRegistry().getDocumentFolderService().getPermissions(childrenNode);

                // Check ContentStream modification
                localContentUri = cursorId.getString(SyncContentSchema.COLUMN_CONTENT_URI_ID);
                docContentUri = childrenNode.getProperty(PropertyIds.CONTENT_STREAM_ID).getValue();
                if (localContentUri != null && docContentUri != null && !localContentUri.equals(docContentUri))
                {
                    // Content has changed in server side
                    // Check local modification
                    if (hasLocalModification)
                    {
                        if (permissions.canEdit())
                        {
                            // Local Change present
                            // User has the right to upload
                            requestUserInteraction(localUri, SyncContentStatus.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncContentStatus.REASON_NO_PERMISSION);
                        }
                    }
                    else
                    {
                        // No local change
                        // We download the new content
                        prepareLocalCreation(localUri, (Document) childrenNode);
                    }
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }

                // Check if Local file exist
                // Content might been deleted with a file explorer
                localFileUri = Uri.parse(cursorId.getString(SyncContentSchema.COLUMN_LOCAL_URI_ID));
                localFile = new File(localFileUri.getPath());
                if (!localFile.exists())
                {
                    // Content is not present, we download content
                    prepareLocalCreation(localUri, (Document) childrenNode);
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }

                // Check modification Date and local modification
                remoteServerTimeStamp = cursorId.getLong(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID);
                localServerTimeStamp = childrenNode.getModifiedAt().getTimeInMillis();
                hasLocalModification = hasLocalModification(cursorId, localFile);

                // Check if there's a modification in server side
                if (remoteServerTimeStamp > localServerTimeStamp)
                {
                    // Server side modification
                    // Check if local modification
                    if (hasLocalModification)
                    {
                        // Request User Decision
                        if (permissions.canEdit())
                        {
                            // Local Change present
                            // User has the right to upload
                            requestUserInteraction(localUri, SyncContentStatus.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncContentStatus.REASON_NO_PERMISSION);
                        }
                    }
                    else
                    {
                        // Document metadata has changed
                        // Content is still the same
                        // We rename the document.
                        rename((Document) childrenNode, localFile, localUri);
                    }
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }

                // Check if it's a local modification
                if (hasLocalModification && remoteServerTimeStamp == localServerTimeStamp)
                {
                    // Local change available
                    // Check permission
                    if (permissions.canEdit())
                    {
                        // Let's Update!
                        prepareUpdate((Document) childrenNode, cursorId, localFile, localUri);
                    }
                    else
                    {
                        // Local Change present
                        // User has read right
                        requestUserInteraction(localUri, SyncContentStatus.REASON_NO_PERMISSION);
                    }
                } else if (localServerTimeStamp > remoteServerTimeStamp) {

                    // Check if local modification
                    if (hasLocalModification) {
                        // Local change available
                        // Check permission
                        if (permissions.canEdit()) {
                            // Let's Update!
                            prepareUpdate((Document) childrenNode, cursorId, localFile, localUri);
                        } else {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncContentStatus.REASON_NO_PERMISSION);
                        }
                    }
                }
            }
            else
            {
                // Info unavailable
                // USE CASE : NEW
                // Folder : we create it
                if (childrenNode.isFolder())
                {
                    if (parentFolder != null && favoriteRootFolder != null)
                    {
                        prepareLocalCreation((Folder) childrenNode, parentFolder, favoriteRootFolder);
                    }
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }
                else
                {
                    // Document : We add it
                    if (parentFolder != null && favoriteRootFolder != null)
                    {
                        prepareChildCreation((Document) childrenNode, parentFolder.getIdentifier());
                    }
                    else
                    {
                        prepareLocalCreation((Document) childrenNode);
                    }
                }
            }
            CursorUtils.closeCursor(cursorId);
        }
    }

    private void recursiveUpdate(Folder favoriteFolder, Folder currentFolder)
    {
        boolean hasMoreItems = true;
        PagingResult<Node> childrenResult = null;
        ListingContext lc = new ListingContext();
        lc.setMaxItems(100);
        lc.setSkipCount(0);

        while (hasMoreItems)
        {
            childrenResult = session.getServiceRegistry().getDocumentFolderService().getChildren(currentFolder, lc);
            if (hasMoreItems)
            {
                lc.setSkipCount(childrenResult.getTotalItems());
            }
            hasMoreItems = childrenResult.hasMoreItems();
            prepareUpdate(favoriteFolder, currentFolder, childrenResult.getList());
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DELETE OPERATIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void prepareDelete()
    {
        if (localSyncCursor == null) return;

        switch (mode)
        {
            case SyncContentManager.MODE_BOTH:
            case SyncContentManager.MODE_FOLDERS:
            case SyncContentManager.MODE_DOCUMENTS:
                prepareSyncDelete();
                break;
            case SyncContentManager.MODE_NODE:
                prepareNodeDelete();
                return;
            default:
                break;
        }
    }

    private void prepareChildrenFolderDelete(String folderIdentifier)
    {
        Cursor childrenCursor = null;
        try
        {
            childrenCursor = context.getContentResolver().query(SyncContentProvider.CONTENT_URI,
                    SyncContentSchema.COLUMN_ALL,
                    SyncContentProvider.getAccountFilter(acc) + " AND " + SyncContentSchema.COLUMN_PARENT_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(folderIdentifier) + "'",
                    null, null);

            while (childrenCursor.moveToNext())
            {
                if (childrenCursor.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID) == 0)
                {
                    prepareDelete(childrenCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID), childrenCursor);
                    if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID)))
                    {
                        prepareChildrenFolderDelete(childrenCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                    }
                }
            }

        }
        catch (Exception e)
        {
            // DO Nothing
        }
        finally
        {
            CursorUtils.closeCursor(childrenCursor);
        }
    }

    private void prepareNodeDelete()
    {
        Cursor nodeCursor = null, parentCursor = null;
        try
        {
            String nodeId = nodeIdentifier;
            // Case favorited node has been deleted from the server
            if (node != null)
            {
                nodeId = node.getIdentifier();
            }

            // Retrieve local Cursor
            nodeCursor = SyncContentManager.getCursorForId(context, acc, nodeId);
            nodeCursor.moveToNext();

            if (syncResult.stats.numEntries == 1) { return; }

            if (node != null)
            {
                try
                {
                    // Node is unfavorited, Check if the parent is
                    // Sync/favorited
                    Folder parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
                    parentCursor = SyncContentManager.getCursorForId(context, acc, parentFolder.getIdentifier());
                    if (parentCursor != null && parentCursor.getCount() > 0)
                    {
                        // Parent is present. We just unfavorite the node
                        ContentValues cValues = new ContentValues();
                        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, 0);
                        context.getContentResolver().update(
                                SyncContentManager.getUri(nodeCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues,
                                null, null);
                    }
                    else
                    {
                        prepareDelete(nodeCursor, nodeId);
                    }
                }
                catch (Exception e)
                {
                    prepareDelete(nodeCursor, nodeId);
                }
            }
            else
            {
                prepareDelete(nodeCursor, nodeId);
            }
        }
        catch (Exception e)
        {
            // Do Nothing
            Log.e(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            CursorUtils.closeCursor(nodeCursor);
            CursorUtils.closeCursor(parentCursor);
        }
    }

    private void prepareDelete(Cursor nodeCursor, String nodeId)
    {
        // No Parent, we remove everything
        if (nodeCursor.getCount() > 1)
        {
            while (nodeCursor.moveToNext())
            {
                // Check status
                switch (nodeCursor.getInt(SyncContentSchema.COLUMN_STATUS_ID))
                {
                    case SyncContentStatus.STATUS_HIDDEN:
                        prepareDelete(nodeId, nodeCursor);
                        break;
                    default:
                        break;
                }
            }
        }
        else if (nodeCursor.getCount() == 1 && nodeCursor.moveToFirst())
        {
            if (node == null)
            {
                prepareDelete(nodeId, nodeCursor);
            }
            else if (node.isFolder())
            {
                prepareChildrenFolderDelete(nodeId);
                prepareDelete(nodeId, nodeCursor);
            }
            else
            {
                prepareDelete(nodeId, nodeCursor);
            }
        }
    }

    private void unFavorite(Cursor nodeCursor)
    {
        // Unflag the folder favorite
        ContentValues cValues = new ContentValues();
        cValues.put(SyncContentSchema.COLUMN_IS_SYNC_ROOT, 0);
        cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES, 0);
        if (!ContentModel.TYPE_FOLDER.equals(nodeCursor.getString(SyncContentSchema.COLUMN_MIMETYPE_ID)))
        {
            cValues.put(SyncContentSchema.COLUMN_DOC_SIZE_BYTES,
                    -nodeCursor.getLong(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID));
        }
        context.getContentResolver().update(
                SyncContentManager.getUri(nodeCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), cValues, null, null);
    }

    private void prepareSyncDelete()
    {
        // USE CASE : DELETE
        // Compare referential and list of favorite Ids
        if (!localSyncCursor.isBeforeFirst())
        {
            localSyncCursor.moveToFirst();
            localSyncCursor.moveToPrevious();
        }

        List<String> nodeUnFavorited = new ArrayList<String>();
        List<String> nodeNonFavoritedToDelete = new ArrayList<String>();

        while (localSyncCursor.moveToNext())
        {
            boolean favorited = localSyncCursor.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID) > 0;
            String nodeId = NodeRefUtils
                    .getCleanIdentifier(localSyncCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
            if (favorited)
            {
                // Clean Id because of version number after update
                // (1.4, 1.5...)
                if (repoFavoriteIds.contains(nodeId))
                {
                    repoFavoriteIds.remove(nodeId);
                }
                else
                {
                    nodeUnFavorited.add(localSyncCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                }
            }
            else
            {
                if (repoSyncIds != null && repoSyncIds.contains(nodeId))
                {
                    repoSyncIds.remove(nodeId);
                }
                else
                {
                    nodeNonFavoritedToDelete.add(localSyncCursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID));
                }
            }
        }

        // Node unfavorited but always present in a folder favorite
        Cursor cursorId = null;
        List<String> nodeToDelete = new ArrayList<String>();
        for (String nodeId : nodeUnFavorited)
        {
            if (repoSyncIds.contains(NodeRefUtils.getCleanIdentifier(nodeId)))
            {
                try
                {
                    // Try to retrieve local info
                    cursorId = SyncContentManager.getCursorForId(context, acc, nodeId);
                    if (cursorId.moveToFirst())
                    {
                        // Unflag the folder favorite
                        unFavorite(cursorId);
                    }
                }
                catch (Exception e)
                {
                    // DO Nothing
                }
                finally
                {
                    CursorUtils.closeCursor(cursorId);
                }
            }
            else
            {
                // To delete
                nodeToDelete.add(nodeId);
            }
        }

        // Merge 2 list
        nodeToDelete.addAll(nodeNonFavoritedToDelete);

        Cursor localFavoriteCursor = null;
        try
        {
            // If nodeIds present, favorite are no longer in repo
            for (String id : nodeToDelete)
            {
                localFavoriteCursor = SyncContentManager.getCursorForId(context, acc, id);

                if (localFavoriteCursor.getCount() > 1)
                {
                    while (localFavoriteCursor.moveToNext())
                    {
                        // Check status
                        switch (localFavoriteCursor.getInt(SyncContentSchema.COLUMN_STATUS_ID))
                        {
                            case SyncContentStatus.STATUS_HIDDEN:
                                prepareDelete(id, localFavoriteCursor);
                                break;
                            default:
                                break;
                        }
                    }
                }
                else if (localFavoriteCursor.getCount() == 1 && localFavoriteCursor.moveToFirst())
                {
                    prepareDelete(id, localFavoriteCursor);
                }
                localFavoriteCursor.close();
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
        finally
        {
            CursorUtils.closeCursor(localFavoriteCursor);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION OPERATIONS
    // ///////////////////////////////////////////////////////////////////////////
    protected abstract void prepareRemoteCreation(Uri localUri, Long id);

    private void prepareLocalCreation(Document doc)
    {
        Uri uri = syncManager.getUri(acc, doc.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(SyncContentProvider.CONTENT_URI,
                    SyncContentManager.createRemoteRootContentValues(context, acc, SyncContentDownload.TYPE_ID, doc,
                            syncScanningTimeStamp));
        }
        // Execution
        prepareLocalCreation(uri, doc);
    }

    protected abstract void prepareLocalCreation(Uri localUri, Document doc);

    private void prepareLocalCreation(Folder folder)
    {
        Uri uri = syncManager.getUri(acc, folder.getIdentifier());
        if (uri == null)
        {
            Folder parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(folder);

            uri = context.getContentResolver().insert(SyncContentProvider.CONTENT_URI,
                    SyncContentManager.createSyncRootContentValues(context, acc, SyncContentDownload.TYPE_ID,
                            parentFolder.getIdentifier(), folder, syncScanningTimeStamp, -1));
        }

        // Retrieve folder size && Flag children
        long size = prepareChildrenFolderCreation(folder, folder);

        // Flag the favorite
        uri = syncManager.getUri(acc, folder.getIdentifier());
        ContentValues cValues = new ContentValues();
        cValues.put(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES, size);
        if (size == 0)
        {
            cValues.put(SyncContentSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
        }
        context.getContentResolver().update(uri, cValues, null, null);
    }

    private void prepareLocalCreation(Folder currentFolder, Folder parentFolder, Folder rootFavoriteFolder)
    {
        // Flag children
        long size = prepareChildrenFolderCreation(currentFolder, rootFavoriteFolder);

        // Flag the favorite
        Uri uri = syncManager.getUri(acc, currentFolder.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(SyncContentProvider.CONTENT_URI,
                    SyncContentManager.createContentValues(context, acc, SyncContentDownload.TYPE_ID,
                            NodeRefUtils.getNodeIdentifier(parentFolder.getIdentifier()), currentFolder,
                            syncScanningTimeStamp, size));
        }
    }

    private long prepareChildrenFolderCreation(Folder folder, Folder rootFavoriteFolder)
    {
        long length = 0;
        boolean hasMoreItems = true;
        PagingResult<Node> nodes = null;
        ListingContext lc = new ListingContext();
        lc.setMaxItems(100);
        lc.setSkipCount(0);

        while (hasMoreItems)
        {
            nodes = session.getServiceRegistry().getDocumentFolderService().getChildren(folder, lc);
            if (hasMoreItems)
            {
                lc.setSkipCount(nodes.getTotalItems());
            }
            hasMoreItems = nodes.hasMoreItems();

            for (Node node : nodes.getList())
            {
                if (node.isDocument())
                {
                    prepareChildCreation((Document) node, folder.getIdentifier());
                    length += ((Document) node).getContentStreamLength();
                }
                else if (node.isFolder())
                {
                    long folderSize = prepareChildrenFolderCreation((Folder) node, rootFavoriteFolder);
                    prepareChildCreation((Folder) node, folder.getIdentifier(), rootFavoriteFolder.getIdentifier(),
                            folderSize);
                    length += folderSize;
                }
            }
        }
        return length;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CREATION CHILDREN
    // ///////////////////////////////////////////////////////////////////////////
    private void prepareChildCreation(Folder folder, String parentFolderId, String favoriteFolderId, long folderSize)
    {
        Uri uri = syncManager.getUri(acc, folder.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(SyncContentProvider.CONTENT_URI,
                    SyncContentManager.createContentValues(context, acc, SyncContentDownload.TYPE_ID, parentFolderId,
                            folder, syncScanningTimeStamp, folderSize));
        }
        else
        {
            // Already present == Favorite Folder
            ContentValues cValues = new ContentValues();
            cValues.put(SyncContentSchema.COLUMN_PARENT_ID, parentFolderId);
            context.getContentResolver().update(uri, cValues, null, null);
        }
    }

    private void prepareChildCreation(Document doc, String parentFolderId)
    {
        Uri uri = syncManager.getUri(acc, doc.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(SyncContentProvider.CONTENT_URI,
                    SyncContentManager.createContentValues(context, acc, SyncContentDownload.TYPE_ID, parentFolderId,
                            doc,
                            syncScanningTimeStamp, 0));
        }

        // Execution
        prepareLocalCreation(uri, doc);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UPDATE : RENAME
    // ///////////////////////////////////////////////////////////////////////////
    protected abstract void rename(Document doc, File localFile, Uri localUri);

    protected abstract void prepareUpdate(Document doc, Cursor cursorId, File localFile, Uri localUri);

    // ///////////////////////////////////////////////////////////////////////////
    // DELETE
    // ///////////////////////////////////////////////////////////////////////////
    protected abstract void prepareDelete(String id, Cursor cursorId);

    // ///////////////////////////////////////////////////////////////////////////
    // USER INTERACTION
    // ///////////////////////////////////////////////////////////////////////////
    protected void requestUserInteraction(Uri localUri, int reasonId)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(OperationSchema.COLUMN_STATUS, SyncContentStatus.STATUS_REQUEST_USER);
        cValues.put(OperationSchema.COLUMN_REASON, reasonId);
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private static void join(StringBuilder sb, CharSequence delimiter, List<String> tokens)
    {
        boolean firstTime = true;
        for (String token : tokens)
        {
            if (firstTime)
            {
                firstTime = false;
            }
            else
            {
                sb.append(delimiter);
            }
            sb.append("'").append(token).append("'");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOCAL MODIFICATION
    // ///////////////////////////////////////////////////////////////////////////
    private boolean hasLocalModification(Cursor cursor, File localFile)
    {
        if (dataProtectionManager.isEncryptionEnable())
        {
            if (SyncContentStatus.STATUS_MODIFIED == cursor.getInt(SyncContentSchema.COLUMN_STATUS_ID)) { return true; }
            return false;
        }

        // Check modification Date and local modification
        long localSyncTimeStamp = cursor.getLong(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
        return (localSyncTimeStamp != -1 && localFile != null && localFile.lastModified() > localSyncTimeStamp);
    }

    private boolean hasLocalModification(Cursor cursor)
    {
        if (dataProtectionManager.isEncryptionEnable())
        {
            if (SyncContentStatus.STATUS_MODIFIED == cursor.getInt(SyncContentSchema.COLUMN_STATUS_ID)) { return true; }
            return false;
        }
        // Check modification Date and local modification
        Uri localFileUri = Uri.parse(cursor.getString(SyncContentSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        return hasLocalModification(cursor, localFile);
    }
}

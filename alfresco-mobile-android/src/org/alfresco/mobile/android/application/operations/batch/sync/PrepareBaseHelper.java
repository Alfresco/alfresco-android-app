/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.operations.batch.sync;

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
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.impl.RepositorySessionImpl;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationSchema;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.operations.sync.node.download.SyncDownloadRequest;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.CursorUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class PrepareBaseHelper
{
    private static final String TAG = SyncPrepareThread.class.getName();

    protected Context context;

    protected int mode;

    protected Cursor localSyncCursor;

    protected AlfrescoSession session;

    protected Account acc;

    protected ListingContext listingContext;

    protected PagingResult<Document> repoDocumentFavorites;

    protected PagingResult<Folder> repoFolderFavorites;

    protected long syncScanningTimeStamp;

    protected SynchroManager syncManager;

    protected DataProtectionManager dataProtectionManager;

    // UPDATE
    private ArrayList<String> repoFavoriteIds;

    private ArrayList<String> repoSyncIds;

    // SCAN NODE
    private Node node;

    private String nodeIdentifier;

    // Mode, Context
    public PrepareBaseHelper(Context context, SyncPrepareThread syncScanThread, long syncScanningTimeStamp)
    {
        this.context = context;
        this.session = syncScanThread.getSession();
        this.acc = syncScanThread.getAccount();
        this.listingContext = session.getDefaultListingContext();
        this.mode = syncScanThread.getMode();
        this.syncScanningTimeStamp = syncScanningTimeStamp;
        this.syncManager = SynchroManager.getInstance(context);
        this.dataProtectionManager = DataProtectionManager.getInstance(context);
        this.node = syncScanThread.getNode();
        this.nodeIdentifier = syncScanThread.getNodeIdentifier();
    }

    public abstract OperationsRequestGroup prepare();

    protected void scan()
    {
        // Retrieve List of remote favorite
        switch (mode)
        {
            case SyncPrepareRequest.MODE_BOTH:
                retrieveDocumentFavorites();
                retrieveFolderFavorites();
                break;
            case SyncPrepareRequest.MODE_FOLDERS:
                retrieveFolderFavorites();
                break;
            case SyncPrepareRequest.MODE_DOCUMENTS:
                retrieveDocumentFavorites();
                break;
            case SyncPrepareRequest.MODE_NODE:
                break;
            default:
                break;
        }

        // Retrieve list of local Favorites
        localSyncCursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI, SynchroSchema.COLUMN_ALL,
                SynchroProvider.getAccountFilter(acc), null, null);

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
            prepareUpdate();

            // Check deleted favorites
            prepareDelete();
        }

        CursorUtils.closeCursor(localSyncCursor);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SCAN : RESTRICTABLE
    // ///////////////////////////////////////////////////////////////////////////
    private static final String QUERY_RESTRICTABLE_DOCS = "SELECT d.cmis:objectId, d.cmis:objectTypeId, d.cmis:baseTypeId, d.cmis:name, d.cmis:createdBy, d.cmis:lastModificationDate,d.cmis:versionSeriesCheckedOutId,d.cmis:contentStreamLength,d.cmis:contentStreamMimeType,d.cmis:isVersionSeriesCheckedOut,d.cmis:versionLabel, m.dp:offlineExpiresAfter FROM cmis:document AS d JOIN dp:restrictable AS m ON d.cmis:objectId = m.cmis:objectId WHERE (d.cmis:objectId=";

    private static final String QUERY_RESTRICTABLE_FOLDER = "SELECT d.cmis:objectId, d.cmis:objectTypeId, d.cmis:baseTypeId, d.cmis:name, d.cmis:createdBy, d.cmis:lastModificationDate, m.dp:offlineExpiresAfter FROM cmis:folder AS d JOIN dp:restrictable AS m ON d.cmis:objectId = m.cmis:objectId WHERE (d.cmis:objectId=";

    private static final String QUERY_OR = " OR d.cmis:objectId=";

    private void retrieveDocumentFavorites()
    {
        repoDocumentFavorites = session.getServiceRegistry().getDocumentFolderService()
                .getFavoriteDocuments(listingContext);

        // Check if restrictable is available on repo
        List<String> restrictableIds = new ArrayList<String>(repoDocumentFavorites.getTotalItems());
        try
        {
            if (repoDocumentFavorites.getTotalItems() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(QUERY_RESTRICTABLE_DOCS);
                join(builder, QUERY_OR, repoDocumentFavorites.getList());
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

        if (session instanceof CloudSession
                || (session instanceof RepositorySessionImpl && ((RepositorySessionImpl) session).hasPublicAPI()))
        {
            // Objects don't contain enough information
            // We request all node object with a search query
            // to retrieve ContentStreamId and permissions.
            List<Document> favoriteDocumentsList = new ArrayList<Document>(repoDocumentFavorites.getTotalItems());
            if (repoDocumentFavorites.getTotalItems() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT * FROM cmis:document WHERE ( cmis:objectId=");
                join(builder, " OR cmis:objectId=", repoDocumentFavorites.getList());
                builder.append(")");

                List<Node> nodes = session.getServiceRegistry().getSearchService()
                        .search(builder.toString(), SearchLanguage.CMIS);

                for (Node node : nodes)
                {
                    favoriteDocumentsList.add((Document) node);
                }
            }
            repoDocumentFavorites = new PagingResultImpl<Document>(favoriteDocumentsList,
                    repoDocumentFavorites.hasMoreItems(), repoDocumentFavorites.getTotalItems());
        }

        // Check Restrictable
        if (restrictableIds != null && !restrictableIds.isEmpty())
        {
            List<Document> tmpNodes = new ArrayList<Document>(repoDocumentFavorites.getTotalItems());
            for (Node node : repoDocumentFavorites.getList())
            {
                if (!restrictableIds.contains(node.getIdentifier()))
                {
                    tmpNodes.add((Document) node);
                }
            }
            repoDocumentFavorites = new PagingResultImpl<Document>(tmpNodes, repoDocumentFavorites.hasMoreItems(),
                    repoDocumentFavorites.getTotalItems());
        }
    }

    private void retrieveFolderFavorites()
    {
        repoFolderFavorites = session.getServiceRegistry().getDocumentFolderService()
                .getFavoriteFolders(listingContext);

        // Check if restrictable is available on repo
        List<String> restrictableIds = new ArrayList<String>(repoFolderFavorites.getTotalItems());
        try
        {
            if (repoFolderFavorites.getTotalItems() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(QUERY_RESTRICTABLE_FOLDER);
                joinF(builder, QUERY_OR, repoFolderFavorites.getList());
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

        if (session instanceof CloudSession
                || (session instanceof RepositorySessionImpl && ((RepositorySessionImpl) session).hasPublicAPI()))
        {
            // Objects don't contain enough information
            // We request all node object with a search query
            // to retrieve ContentStreamId and permissions.
            List<Folder> favoriteFoldersList = new ArrayList<Folder>(repoFolderFavorites.getTotalItems());
            if (repoFolderFavorites.getTotalItems() > 0)
            {
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT * FROM cmis:folder WHERE ( cmis:objectId=");
                joinF(builder, " OR cmis:objectId=", repoFolderFavorites.getList());
                builder.append(")");

                List<Node> nodes = session.getServiceRegistry().getSearchService()
                        .search(builder.toString(), SearchLanguage.CMIS);

                for (Node node : nodes)
                {
                    favoriteFoldersList.add((Folder) node);
                }
            }
            repoFolderFavorites = new PagingResultImpl<Folder>(favoriteFoldersList, repoFolderFavorites.hasMoreItems(),
                    repoFolderFavorites.getTotalItems());
        }

        // Check Restrictable
        if (restrictableIds != null && !restrictableIds.isEmpty())
        {
            List<Folder> tmpNodes = new ArrayList<Folder>(repoFolderFavorites.getTotalItems());
            for (Node node : repoFolderFavorites.getList())
            {
                if (!restrictableIds.contains(node.getIdentifier()))
                {
                    tmpNodes.add((Folder) node);
                }
            }
            repoFolderFavorites = new PagingResultImpl<Folder>(tmpNodes, repoFolderFavorites.hasMoreItems(),
                    repoFolderFavorites.getTotalItems());
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
            case SyncPrepareRequest.MODE_DOCUMENTS:
                for (Document doc : repoDocumentFavorites.getList())
                {
                    prepareCreation(doc);
                }
                break;
            case SyncPrepareRequest.MODE_FOLDERS:
                for (Folder folder : repoFolderFavorites.getList())
                {
                    prepareCreation(folder);
                }
                break;
            case SyncPrepareRequest.MODE_BOTH:
                for (Document doc : repoDocumentFavorites.getList())
                {
                    prepareCreation(doc);
                }
                for (Folder folder : repoFolderFavorites.getList())
                {
                    prepareCreation(folder);
                }
                break;
            case SyncPrepareRequest.MODE_NODE:
                if (node.isDocument())
                {
                    prepareCreation((Document) node);
                }
                else
                {
                    prepareCreation((Folder) node);
                }
                break;
            default:
                break;
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
            case SyncPrepareRequest.MODE_NODE:
                List<Node> tmpNode = new ArrayList<Node>(1);
                if (node != null)
                {
                    if (node.isFolder())
                    {
                        repoSyncIds = new ArrayList<String>();
                    }
                    if (session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(node.getIdentifier()) != null
                            && session.getServiceRegistry().getDocumentFolderService().isFavorite(node))
                    {
                        tmpNode.add(node);
                    }
                }
                prepareUpdate(tmpNode);
                break;
            case SyncPrepareRequest.MODE_DOCUMENTS:
                List<Node> tmpNodes = new ArrayList<Node>(repoDocumentFavorites.getList());
                prepareUpdate(tmpNodes);
                break;
            case SyncPrepareRequest.MODE_BOTH:
                List<Node> tmpNodes2 = new ArrayList<Node>(repoDocumentFavorites.getList());
                prepareUpdate(tmpNodes2);
            case SyncPrepareRequest.MODE_FOLDERS:
                repoSyncIds = new ArrayList<String>();
                Cursor cursorId = null;
                try
                {
                    for (Folder favoriteFolder : repoFolderFavorites.getList())
                    {
                        repoFavoriteIds.add(NodeRefUtils.getCleanIdentifier(favoriteFolder.getIdentifier()));

                        // Check if it exists first
                        // Try to retrieve local info
                        cursorId = SynchroManager.getCursorForId(context, acc, favoriteFolder.getIdentifier());

                        if (cursorId.moveToFirst())
                        {
                            // Is it a new Favorite ?
                            Boolean favorited = cursorId.getInt(SynchroSchema.COLUMN_IS_FAVORITE_ID) > 0;
                            if (favorited)
                            {
                                // If exist so update
                                recursiveUpdate(favoriteFolder, favoriteFolder);
                            }
                            else
                            {
                                // New favorite
                                String parent = cursorId.getString(SynchroSchema.COLUMN_PARENT_ID_ID);

                                recursiveUpdate(favoriteFolder, favoriteFolder);

                                ContentValues cValues = new ContentValues();
                                cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, SynchroProvider.FLAG_FAVORITE);
                                cValues.put(SynchroSchema.COLUMN_PARENT_ID, parent);
                                context.getContentResolver().update(
                                        SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID)), cValues,
                                        null, null);
                            }
                        }
                        else
                        {
                            // If not create
                            prepareCreation(favoriteFolder);
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
            cursorId = SynchroManager.getCursorForId(context, acc, childrenNode.getIdentifier());

            if (cursorId.moveToFirst())
            {
                localUri = SynchroManager.getUri(cursorId.getLong(SynchroSchema.COLUMN_ID_ID));

                // NODE HAS MOVED
                // Check Parent ID if possible
                String parentId = cursorId.getString(SynchroSchema.COLUMN_PARENT_ID_ID);
                if (parentFolder != null && !parentFolder.getIdentifier().equals(parentId))
                {
                    ContentValues cValues = new ContentValues();
                    cValues.put(BatchOperationSchema.COLUMN_PARENT_ID, parentFolder.getIdentifier());
                    context.getContentResolver().update(localUri, cValues, null, null);
                }

                // Folder
                if (childrenNode.isFolder())
                {

                    // Renamed ?
                    ContentValues cValues = new ContentValues();
                    String folderName = cursorId.getString(SynchroSchema.COLUMN_TITLE_ID);
                    if (!childrenNode.getName().equals(folderName))
                    {
                        // FOLDER HAS BEEN RENAMED

                        cValues.put(BatchOperationSchema.COLUMN_TITLE, childrenNode.getName());
                        context.getContentResolver().update(localUri, cValues, null, null);
                    }

                    // First creation ?
                    long folderSize = cursorId.getLong(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                    if (folderSize == -1)
                    {
                        // Retrieve folder size && Flag children
                        long size = prepareChildrenFolderCreation((Folder) childrenNode, (Folder) childrenNode);

                        // Flag the favorite
                        Uri uri = syncManager.getUri(acc, childrenNode.getIdentifier());
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, size);
                        if (size == 0)
                        {
                            contentValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
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
                localContentUri = cursorId.getString(SynchroSchema.COLUMN_CONTENT_URI_ID);
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
                            requestUserInteraction(localUri, SyncOperation.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
                        }
                    }
                    else
                    {
                        // No local change
                        // We download the new content
                        prepareCreation(localUri, (Document) childrenNode);
                    }
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }

                // Check if Local file exist
                // Content might been deleted with a file explorer
                localFileUri = Uri.parse(cursorId.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
                localFile = new File(localFileUri.getPath());
                if (!localFile.exists())
                {
                    // Content is not present, we download content
                    prepareCreation(localUri, (Document) childrenNode);
                    CursorUtils.closeCursor(cursorId);
                    continue;
                }

                // Check modification Date and local modification
                localServerTimeStamp = cursorId.getLong(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID);
                remoteServerTimeStamp = childrenNode.getModifiedAt().getTimeInMillis();
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
                            requestUserInteraction(localUri, SyncOperation.REASON_LOCAL_MODIFICATION);
                        }
                        else
                        {
                            // Local Change present
                            // User has read right
                            requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
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
                        requestUserInteraction(localUri, SyncOperation.REASON_NO_PERMISSION);
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
                        prepareCreation((Folder) childrenNode, parentFolder, favoriteRootFolder);
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
                        prepareCreation((Document) childrenNode);
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
            case SyncPrepareRequest.MODE_BOTH:
            case SyncPrepareRequest.MODE_FOLDERS:
            case SyncPrepareRequest.MODE_DOCUMENTS:
                prepareSyncDelete();
                break;
            case SyncPrepareRequest.MODE_NODE:
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
            childrenCursor = context.getContentResolver().query(
                    SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL,
                    SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_PARENT_ID + " == '"
                            + NodeRefUtils.getCleanIdentifier(folderIdentifier) + "'", null, null);

            while (childrenCursor.moveToNext())
            {
                if (childrenCursor.getInt(SynchroSchema.COLUMN_IS_FAVORITE_ID) == 0)
                {
                    prepareDelete(childrenCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID), childrenCursor);
                    if (ContentModel.TYPE_FOLDER.equals(childrenCursor.getString(SynchroSchema.COLUMN_MIMETYPE_ID)))
                    {
                        prepareChildrenFolderDelete(childrenCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
                    }
                }
            }

        }
        catch (Exception e)
        {
            // TODO: handle exception
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
            nodeCursor = SynchroManager.getCursorForId(context, acc, nodeId);

            if (node != null && !session.getServiceRegistry().getDocumentFolderService().isFavorite(node))
            {
                // Node is unfavorited, Check if the parent is Sync/favorited
                Folder parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);
                parentCursor = SynchroManager.getCursorForId(context, acc, parentFolder.getIdentifier());
                if (parentCursor != null && parentCursor.getCount() > 0)
                {
                    // Parent is present. We just unfavorite the node
                    ContentValues cValues = new ContentValues();
                    cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, 0);
                    context.getContentResolver().update(
                            SynchroManager.getUri(nodeCursor.getLong(SynchroSchema.COLUMN_ID_ID)), cValues, null, null);
                }
                else
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
                switch (nodeCursor.getInt(SynchroSchema.COLUMN_STATUS_ID))
                {
                    case SyncOperation.STATUS_HIDDEN:
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
        cValues.put(SynchroSchema.COLUMN_IS_FAVORITE, 0);
        cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES, 0);
        if (!ContentModel.TYPE_FOLDER.equals(nodeCursor.getString(SynchroSchema.COLUMN_MIMETYPE_ID)))
        {
            cValues.put(SynchroSchema.COLUMN_DOC_SIZE_BYTES,
                    -nodeCursor.getLong(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID));
        }
        context.getContentResolver().update(SynchroManager.getUri(nodeCursor.getLong(SynchroSchema.COLUMN_ID_ID)),
                cValues, null, null);
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
            boolean favorited = localSyncCursor.getInt(SynchroSchema.COLUMN_IS_FAVORITE_ID) > 0;
            String nodeId = NodeRefUtils.getCleanIdentifier(localSyncCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
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
                    nodeUnFavorited.add(localSyncCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
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
                    nodeNonFavoritedToDelete.add(localSyncCursor.getString(SynchroSchema.COLUMN_NODE_ID_ID));
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
                    cursorId = SynchroManager.getCursorForId(context, acc, nodeId);
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
                localFavoriteCursor = SynchroManager.getCursorForId(context, acc, id);

                if (localFavoriteCursor.getCount() > 1)
                {
                    while (localFavoriteCursor.moveToNext())
                    {
                        // Check status
                        switch (localFavoriteCursor.getInt(SynchroSchema.COLUMN_STATUS_ID))
                        {
                            case SyncOperation.STATUS_HIDDEN:
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
    private void prepareCreation(Document doc)
    {
        Uri uri = syncManager.getUri(acc, doc.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(
                    SynchroProvider.CONTENT_URI,
                    SynchroManager.createFavoriteContentValues(context, acc, SyncDownloadRequest.TYPE_ID, doc,
                            syncScanningTimeStamp));
        }
        // Execution
        prepareCreation(uri, doc);
    }

    protected abstract void prepareCreation(Uri localUri, Document doc);

    private void prepareCreation(Folder folder)
    {
        Uri uri = syncManager.getUri(acc, folder.getIdentifier());
        if (uri == null)
        {
            Folder parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(folder);

            uri = context.getContentResolver().insert(
                    SynchroProvider.CONTENT_URI,
                    SynchroManager.createFavoriteContentValues(context, acc, SyncDownloadRequest.TYPE_ID,
                            parentFolder.getIdentifier(), folder, syncScanningTimeStamp, -1));
        }

        // Retrieve folder size && Flag children
        long size = prepareChildrenFolderCreation(folder, folder);

        // Flag the favorite
        uri = syncManager.getUri(acc, folder.getIdentifier());
        ContentValues cValues = new ContentValues();
        cValues.put(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES, size);
        if (size == 0)
        {
            cValues.put(SynchroSchema.COLUMN_STATUS, Operation.STATUS_SUCCESSFUL);
        }
        context.getContentResolver().update(uri, cValues, null, null);
    }

    private void prepareCreation(Folder currentFolder, Folder parentFolder, Folder rootFavoriteFolder)
    {
        // Flag children
        long size = prepareChildrenFolderCreation(currentFolder, rootFavoriteFolder);

        // Flag the favorite
        Uri uri = syncManager.getUri(acc, currentFolder.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(
                    SynchroProvider.CONTENT_URI,
                    SynchroManager.createContentValues(context, acc, SyncDownloadRequest.TYPE_ID,
                            parentFolder.getIdentifier(), currentFolder, syncScanningTimeStamp, size));
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
    private void prepareChildCreation(Folder folder, String parentFolder, String favoriteFolderId, long folderSize)
    {
        Uri uri = syncManager.getUri(acc, folder.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(
                    SynchroProvider.CONTENT_URI,
                    SynchroManager.createContentValues(context, acc, SyncDownloadRequest.TYPE_ID, parentFolder, folder,
                            syncScanningTimeStamp, folderSize));
        }
        else
        {
            // Already present == Favorite Folder
            ContentValues cValues = new ContentValues();
            cValues.put(SynchroSchema.COLUMN_PARENT_ID, parentFolder);
            context.getContentResolver().update(uri, cValues, null, null);
        }
    }

    private void prepareChildCreation(Document doc, String parentFolder)
    {
        Uri uri = syncManager.getUri(acc, doc.getIdentifier());
        if (uri == null)
        {
            uri = context.getContentResolver().insert(
                    SynchroProvider.CONTENT_URI,
                    SynchroManager.createContentValues(context, acc, SyncDownloadRequest.TYPE_ID, parentFolder, doc,
                            syncScanningTimeStamp, 0));
        }

        // Execution
        prepareCreation(uri, doc);
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
        cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_REQUEST_USER);
        cValues.put(BatchOperationSchema.COLUMN_REASON, reasonId);
        context.getContentResolver().update(localUri, cValues, null, null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private static void join(StringBuilder sb, CharSequence delimiter, List<Document> tokens)
    {
        boolean firstTime = true;
        for (Document token : tokens)
        {
            if (firstTime)
            {
                firstTime = false;
            }
            else
            {
                sb.append(delimiter);
            }
            sb.append("'" + token.getIdentifier() + "'");
        }
    }

    private static void joinF(StringBuilder sb, CharSequence delimiter, List<Folder> tokens)
    {
        boolean firstTime = true;
        for (Folder token : tokens)
        {
            if (firstTime)
            {
                firstTime = false;
            }
            else
            {
                sb.append(delimiter);
            }
            sb.append("'" + token.getIdentifier() + "'");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LOCAL MODIFICATION
    // ///////////////////////////////////////////////////////////////////////////
    private boolean hasLocalModification(Cursor cursor, File localFile)
    {
        if (dataProtectionManager.isEncryptionEnable())
        {
            if (SyncOperation.STATUS_MODIFIED == cursor.getInt(SynchroSchema.COLUMN_STATUS_ID)) { return true; }
            return false;
        }

        // Check modification Date and local modification
        long localSyncTimeStamp = cursor.getLong(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
        return (localSyncTimeStamp != -1 && localFile != null && localFile.lastModified() > localSyncTimeStamp);
    }

    private boolean hasLocalModification(Cursor cursor)
    {
        if (dataProtectionManager.isEncryptionEnable())
        {
            if (SyncOperation.STATUS_MODIFIED == cursor.getInt(SynchroSchema.COLUMN_STATUS_ID)) { return true; }
            return false;
        }
        // Check modification Date and local modification
        Uri localFileUri = Uri.parse(cursor.getString(SynchroSchema.COLUMN_LOCAL_URI_ID));
        File localFile = new File(localFileUri.getPath());
        return hasLocalModification(cursor, localFile);
    }
}

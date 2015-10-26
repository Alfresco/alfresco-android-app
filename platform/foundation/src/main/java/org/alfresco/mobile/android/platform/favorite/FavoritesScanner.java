/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform.favorite;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.SearchLanguage;
import org.alfresco.mobile.android.api.model.impl.PagingResultImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.impl.RepositorySessionImpl;
import org.alfresco.mobile.android.api.utils.NodeRefUtils;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.platform.provider.CursorUtils;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;

import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class FavoritesScanner
{
    private static final String TAG = FavoritesScanner.class.getName();

    protected Context context;

    protected int mode;

    protected Cursor localSyncCursor;

    protected AlfrescoSession session;

    protected AlfrescoAccount acc;

    protected ListingContext listingContext;

    protected PagingResult<Document> repoDocumentFavorites;

    protected PagingResult<Folder> repoFolderFavorites;

    protected long syncScanningTimeStamp;

    protected FavoritesManager syncManager;

    protected DataProtectionManager dataProtectionManager;

    private ArrayList<String> repoFavoriteIds;

    // SCAN NODE
    private Node node;

    private String nodeIdentifier;

    protected SyncResult syncResult;

    // Mode, Context
    public FavoritesScanner(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result)
    {
        this.syncResult = result;
        this.context = context;
        this.session = session;
        this.acc = acc;
        this.listingContext = session.getDefaultListingContext();
        this.mode = mode;
        this.syncScanningTimeStamp = syncScanningTimeStamp;
        this.syncManager = FavoritesManager.getInstance(context);
        this.dataProtectionManager = DataProtectionManager.getInstance(context);
    }

    // Mode, Context
    public FavoritesScanner(Context context, AlfrescoAccount acc, AlfrescoSession session, int mode,
            long syncScanningTimeStamp, SyncResult result, Node node)
    {
        this(context, acc, session, mode, syncScanningTimeStamp, result);
        if (node != null)
        {
            this.node = node;
            this.nodeIdentifier = node.getIdentifier();
        }
    }

    public void scan()
    {
        // Retrieve List of remote favorite
        switch (mode)
        {
            case FavoritesManager.MODE_BOTH:
                retrieveDocumentFavorites();
                retrieveFolderFavorites();
                break;
            case FavoritesManager.MODE_FOLDERS:
                retrieveFolderFavorites();
                break;
            case FavoritesManager.MODE_DOCUMENTS:
                retrieveDocumentFavorites();
                break;
            case FavoritesManager.MODE_NODE:
                break;
            default:
                break;
        }

        // Retrieve list of local Favorites
        localSyncCursor = context.getContentResolver().query(FavoritesProvider.CONTENT_URI, FavoritesSchema.COLUMN_ALL,
                FavoritesProvider.getAccountFilter(acc), null, null);

        // Check updated favorites
        prepareUpdate();

        // Check deleted favorites
        prepareDelete();

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
    // UPDATE
    // ///////////////////////////////////////////////////////////////////////////
    private void prepareUpdate()
    {
        repoFavoriteIds = new ArrayList<String>();

        switch (mode)
        {
            case FavoritesManager.MODE_NODE:
                List<Node> tmpNode = new ArrayList<Node>(1);
                if (node != null)
                {
                    if (session.getServiceRegistry().getDocumentFolderService()
                            .getNodeByIdentifier(node.getIdentifier()) != null
                            && session.getServiceRegistry().getDocumentFolderService().isFavorite(node))
                    {
                        tmpNode.add(node);
                    }
                }
                syncResult.stats.numEntries = tmpNode.size();
                prepareUpdate(tmpNode);
                break;
            case FavoritesManager.MODE_DOCUMENTS:
                List<Node> tmpNodes = new ArrayList<Node>(repoDocumentFavorites.getList());
                syncResult.stats.numEntries = tmpNodes.size();
                prepareUpdate(tmpNodes);
                break;
            case FavoritesManager.MODE_BOTH:
                List<Node> tmpNodes2 = new ArrayList<Node>(repoDocumentFavorites.getList());
                syncResult.stats.numEntries = tmpNodes2.size();
                prepareUpdate(tmpNodes2);
            case FavoritesManager.MODE_FOLDERS:
                List<Node> tmpNodes3 = new ArrayList<Node>(repoFolderFavorites.getList());
                syncResult.stats.numEntries = tmpNodes3.size();
                prepareUpdate(tmpNodes3);
                break;
            default:
                break;
        }
    }

    private void prepareUpdate(List<Node> childrens)
    {
        // Favorites are present.
        Cursor cursorId = null;
        Uri localUri = null;

        // Browse the results
        for (Node node : childrens)
        {
            if (cursorId != null)
            {
                cursorId.close();
            }

            // Index of current favorite nodes on repo
            repoFavoriteIds.add(NodeRefUtils.getCleanIdentifier(node.getIdentifier()));

            // Try to retrieve local info
            cursorId = FavoritesManager.getCursorForId(context, acc, node.getIdentifier());

            // Retrieve parent
            Folder parentFolder = session.getServiceRegistry().getDocumentFolderService().getParentFolder(node);

            if (cursorId.moveToFirst())
            {
                // Info available
                // USE CASE : UPDATE OR NOTHING
                localUri = FavoritesManager.getUri(cursorId.getLong(SyncContentSchema.COLUMN_ID_ID));

                // Modification on server side ?
                long localSyncTimeStamp = cursorId.getLong(SyncContentSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP_ID);
                long nodeLastModification = node.getModifiedAt().getTimeInMillis();
                if (localSyncTimeStamp < nodeLastModification)
                {
                    syncResult.stats.numUpdates++;
                    // Repo has changed so update everything
                    context.getContentResolver().update(
                            localUri,
                            FavoritesManager.createFavoriteContentValues(context, acc, parentFolder.getIdentifier(),
                                    node, syncScanningTimeStamp), null, null);
                }
            }
            else
            {
                // Info unavailable
                // USE CASE : NEW
                syncResult.stats.numEntries++;
                context.getContentResolver().insert(
                        FavoritesProvider.CONTENT_URI,
                        FavoritesManager.createFavoriteContentValues(context, acc, parentFolder.getIdentifier(), node,
                                syncScanningTimeStamp));
            }
            CursorUtils.closeCursor(cursorId);
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
            case FavoritesManager.MODE_BOTH:
            case FavoritesManager.MODE_FOLDERS:
            case FavoritesManager.MODE_DOCUMENTS:
                prepareSyncDelete();
                break;
            case FavoritesManager.MODE_NODE:
                prepareNodeDelete();
                return;
            default:
                break;
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
            nodeCursor = FavoritesManager.getCursorForId(context, acc, nodeId);
            nodeCursor.moveToNext();

            if (syncResult.stats.numEntries == 1) { return; }

            if (node != null && !session.getServiceRegistry().getDocumentFolderService().isFavorite(node))
            {
                syncResult.stats.numDeletes++;
                // Node has been removed so we need to delete
                context.getContentResolver().delete(
                        FavoritesManager.getUri(localSyncCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), null, null);
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

    private void prepareSyncDelete()
    {
        // USE CASE : DELETE
        // Compare referential and list of favorite Ids
        if (!localSyncCursor.isBeforeFirst())
        {
            localSyncCursor.moveToFirst();
            localSyncCursor.moveToPrevious();
        }

        while (localSyncCursor.moveToNext())
        {
            String nodeId = NodeRefUtils.getCleanIdentifier(localSyncCursor
                    .getString(FavoritesSchema.COLUMN_NODE_ID_ID));

            if (repoFavoriteIds.contains(nodeId))
            {
                // Node is present so it was an update
                repoFavoriteIds.remove(nodeId);
            }
            else
            {
                // Node has been removed so we need to delete
                context.getContentResolver().delete(
                        FavoritesManager.getUri(localSyncCursor.getLong(SyncContentSchema.COLUMN_ID_ID)), null, null);
            }
        }
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
            sb.append("'").append(token.getIdentifier()).append("'");
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
            sb.append("'").append(token.getIdentifier()).append("'");
        }
    }
}

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
package org.alfresco.mobile.android.async;

import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class OperationRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    // ///////////////////////////////////////////////////////////////////////////
    // REGISTRY
    // ///////////////////////////////////////////////////////////////////////////
    /** Display nothing. */
    public static final int VISIBILITY_HIDDEN = 1;

    /** Notification status + inline. */
    public static final int VISIBILITY_NOTIFICATIONS = 2;

    /** Display a central modal dialog. */
    public static final int VISIBILITY_DIALOG = 4;

    /** Display a simple toast notification. */
    public static final int VISIBILITY_TOAST = 8;

    /** Display a ruban notification. */
    public static final int VISIBILITY_RUBAN = 16;

    /** MimeType Undefined. */
    public static final String MIMETYPE_UNDEFINED = "undefined";

    // ///////////////////////////////////////////////////////////////////////////
    // MEMBERS
    // ///////////////////////////////////////////////////////////////////////////
    protected final long accountId;

    protected final String networkId;

    protected final int notificationVisibility;

    protected String title;

    protected String mimeType;

    protected final int requestTypeId;

    public final Uri notificationUri;

    protected boolean cancelled;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    protected OperationRequest(Context context, long accountId, String networkId, int notificationVisibility,
            String title, String mimeType, int requestTypeId)
    {
        super();
        this.accountId = accountId;
        this.networkId = networkId;
        this.notificationVisibility = notificationVisibility;
        this.title = title;
        this.mimeType = mimeType;
        this.requestTypeId = requestTypeId;
        this.notificationUri = generateNotificationUri(context);
        Log.d("Provider", "[Create] " + notificationUri + " " + getClass().getSimpleName());
    }

    public OperationRequest(Cursor cursor)
    {
        this.accountId = cursor.getLong(OperationsSchema.COLUMN_ACCOUNT_ID_ID);
        this.networkId = cursor.getString(OperationsSchema.COLUMN_TENANT_ID_ID);
        this.notificationVisibility = cursor.getInt(OperationsSchema.COLUMN_NOTIFICATION_VISIBILITY_ID);
        this.title = cursor.getString(OperationsSchema.COLUMN_TITLE_ID);
        this.mimeType = cursor.getString(OperationsSchema.COLUMN_MIMETYPE_ID);
        this.requestTypeId = cursor.getInt(OperationsSchema.COLUMN_REQUEST_TYPE_ID);
        this.notificationUri = cursor.getNotificationUri();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    void cancel()
    {
        cancelled = true;
    }

    boolean isCancelled()
    {
        return cancelled;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public String getCacheKey()
    {
        return getClass().getSimpleName();
    }

    /** Used to define which execution pool to use. */
    public boolean isLongRunning()
    {
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PERSISTENCE
    // ///////////////////////////////////////////////////////////////////////////
    public abstract ContentValues createContentValues(int status);

    protected abstract Uri generateNotificationUri(Context context);

    public void saveStatus(Context context, int status)
    {
        if (notificationUri != null && context != null)
        {
            context.getContentResolver().update(notificationUri, createContentValues(status), null, null);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Builder
    // ///////////////////////////////////////////////////////////////////////////
    public abstract static class OperationBuilder
    {
        protected Uri uri;

        protected long accountId;

        protected String networkId;

        protected int notificationVisibility = VISIBILITY_HIDDEN;

        protected String title;

        protected String mimeType = MIMETYPE_UNDEFINED;

        protected int requestTypeId;

        protected Uri notificationUri;

        protected OperationBuilder()
        {

        }

        public abstract OperationRequest build(Context context);

        public OperationBuilder setNotificationVisibility(int visibility)
        {
            this.notificationVisibility = visibility;
            return this;
        }

        public OperationBuilder setNotificationTitle(String title)
        {
            this.title = title;
            return this;
        }

        public OperationBuilder setAccountId(long accountId)
        {
            this.accountId = accountId;
            return this;
        }

        public OperationBuilder setNetworkId(String networkId)
        {
            this.networkId = networkId;
            return this;
        }

        public OperationBuilder setMimeType(String mimeType)
        {
            this.mimeType = mimeType;
            return this;
        }

        public OperationBuilder setNotificationUri(Uri notificationUri)
        {
            this.notificationUri = notificationUri;
            return this;
        }
    }
}

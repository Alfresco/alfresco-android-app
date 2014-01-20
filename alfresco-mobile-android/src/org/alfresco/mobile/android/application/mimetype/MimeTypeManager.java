/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.mimetype;

import org.alfresco.mobile.android.application.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * @since 1.4
 * @author Jean Marie Pascal
 */
public final class MimeTypeManager
{
    private static final String TAG = MimeTypeManager.class.getName();

    private static MimeTypeManager mInstance;

    private final Context appContext;

    private static final Object LOCK = new Object();

    private Integer mimetypeSize;

    public static final Uri CONTENT_URI = MimeTypeProvider.CONTENT_URI;

    public static final String[] COLUMN_ALL = MimeTypeSchema.COLUMN_ALL;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static MimeTypeManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new MimeTypeManager(context.getApplicationContext());
            }

            return mInstance;
        }
    }

    private MimeTypeManager(Context context)
    {
        // Init/retrieve manager
        this.appContext = context;
        getCount();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // STATIC
    // ///////////////////////////////////////////////////////////////////////////
    public static String getName(String uri)
    {
        if (uri == null) { return null; }

        int dot = uri.lastIndexOf(".".charAt(0));
        if (dot > 0)
        {
            return uri.substring(0, dot).toLowerCase();
        }
        else
        {
            return uri;
        }
    }

    public static String getExtension(String uri)
    {
        if (uri == null) { return null; }

        int dot = uri.lastIndexOf(".".charAt(0));
        if (dot > 0)
        {
            return uri.substring(dot + 1).toLowerCase();
        }
        else
        {
            return "";
        }
    }

    public static String getMIMEType(Context context, String fileName)
    {
        String extension = "";
        if (fileName != null)
        {
            extension = getExtension(fileName);
        }
        MimeType type = findByExtension(context, extension);

        if (type != null)
        {
            return type.getMimeType();
        }
        else
        {
            return "application/octet-stream";
        }
    }

    public static int getIcon(Context context, String fileName)
    {
        String extension = "";
        int iconId = R.drawable.mime_generic;
        if (fileName != null)
        {
            extension = getExtension(fileName);
        }
        MimeType type = findByExtension(context, extension);

        if (type != null)
        {
            return type.getSmallIconId(context);
        }
        else
        {
            return iconId;
        }
    }

    public static int getIcon(Context context, String fileName, boolean isLarge)
    {
        if (!isLarge) { return getIcon(context, fileName); }
        String extension = "";
        int iconId = R.drawable.mime_256_generic;
        if (fileName != null)
        {
            extension = getExtension(fileName);
        }
        MimeType type = findByExtension(context, extension);

        if (type != null)
        {
            return type.getLargeIconId(context);
        }
        else
        {
            return iconId;
        }
    }

    public static MimeType findById(Context context, long id)
    {
        Cursor cursor = context.getContentResolver().query(getUri(id), COLUMN_ALL, null, null, null);
        if (cursor == null) { return null; }
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            return createMimeType(cursor);
        }
        cursor.close();
        return null;
    }

    public static MimeType findByExtension(Context context, String extension)
    {
        if (extension == null) { return null; }
        Cursor cursor = context.getContentResolver().query(MimeTypeProvider.CONTENT_URI, COLUMN_ALL,
                MimeTypeSchema.COLUMN_EXTENSION + " = \"" + extension + "\"", null, null);
        if (cursor == null) { return null; }
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            return createMimeType(cursor);
        }
        cursor.close();
        return null;
    }

    public static MimeType createMimeType(Cursor c)
    {
        MimeType account = new MimeType(c.getLong(MimeTypeSchema.COLUMN_ID_ID),
                c.getString(MimeTypeSchema.COLUMN_EXTENSION_ID), c.getString(MimeTypeSchema.COLUMN_TYPE_ID),
                c.getString(MimeTypeSchema.COLUMN_SUBTYPE_ID), c.getString(MimeTypeSchema.COLUMN_DESCRIPTION_ID),
                c.getString(MimeTypeSchema.COLUMN_SMALL_ICON_ID), c.getString(MimeTypeSchema.COLUMN_LARGE_ICON_ID));
        c.close();
        return account;
    }

    public static MimeType createMimeType(Context context, String extension, String type, String subtype,
            String description, String smallIcon, String largeIcon)
    {
        Uri accountUri = context.getContentResolver().insert(MimeTypeProvider.CONTENT_URI,
                createContentValues(extension, type, subtype, description, smallIcon, largeIcon));

        if (accountUri == null) { return null; }

        return MimeTypeManager.findById(context, Long.parseLong(accountUri.getLastPathSegment()));
    }

    public static MimeType update(Context context, long id, String extension, String type, String subtype,
            String description, String smallIcon, String largeIcon)
    {
        context.getContentResolver().update(getUri(id),
                createContentValues(extension, type, subtype, description, smallIcon, largeIcon), null, null);

        return MimeTypeManager.findById(context, id);
    }

    public static Uri getUri(long id)
    {
        return Uri.parse(MimeTypeProvider.CONTENT_URI + "/" + id);
    }

    public static ContentValues createContentValues(String extension, String type, String subtype, String description,
            String smallIcon, String largeIcon)
    {
        ContentValues updateValues = new ContentValues();

        updateValues.put(MimeTypeSchema.COLUMN_EXTENSION, extension);
        updateValues.put(MimeTypeSchema.COLUMN_TYPE, type);
        updateValues.put(MimeTypeSchema.COLUMN_SUBTYPE, subtype);
        updateValues.put(MimeTypeSchema.COLUMN_DESCRIPTION, description);
        updateValues.put(MimeTypeSchema.COLUMN_SMALL_ICON, smallIcon);
        updateValues.put(MimeTypeSchema.COLUMN_LARGE_ICON, largeIcon);
        return updateValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public boolean hasData()
    {
        getCount();
        return (mimetypeSize != null);
    }

    public boolean hasMimeTypes()
    {
        if (mimetypeSize == null) { return false; }
        return (mimetypeSize > 0);
    }

    public boolean isEmpty()
    {
        getCount();
        if (mimetypeSize == null) { return true; }
        return (mimetypeSize == 0);
    }

    public MimeType update(long id, String extension, String type, String subtype, String description,
            String smallIcon, String largeIcon)
    {
        return update(appContext, id, extension, type, subtype, description, smallIcon, largeIcon);
    }

    private static ContentValues createContentValues(MimeType mimeType)
    {
        ContentValues updateValues = new ContentValues();

        updateValues.put(MimeTypeSchema.COLUMN_EXTENSION, mimeType.getExtension());
        updateValues.put(MimeTypeSchema.COLUMN_TYPE, mimeType.getType());
        updateValues.put(MimeTypeSchema.COLUMN_SUBTYPE, mimeType.getSubType());
        updateValues.put(MimeTypeSchema.COLUMN_DESCRIPTION, mimeType.getDescription());
        updateValues.put(MimeTypeSchema.COLUMN_SMALL_ICON, mimeType.getSmallIcon());
        updateValues.put(MimeTypeSchema.COLUMN_LARGE_ICON, mimeType.getLargeIcon());
        return updateValues;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Private
    // ///////////////////////////////////////////////////////////////////////////
    private void getCount()
    {
        Cursor cursor = appContext.getContentResolver().query(MimeTypeProvider.CONTENT_URI, COLUMN_ALL, null, null,
                null);
        if (cursor != null)
        {
            mimetypeSize = cursor.getCount();
            cursor.close();
        }
        else
        {
            mimetypeSize = 0;
        }
    }
}

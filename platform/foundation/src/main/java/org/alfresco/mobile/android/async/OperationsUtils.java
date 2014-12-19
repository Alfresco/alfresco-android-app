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

import org.alfresco.mobile.android.platform.provider.CursorUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public final class OperationsUtils
{
    private OperationsUtils()
    {
    }

    public static void removeOperationUri(Context context, OperationRequest request)
    {
        Uri operationUri = request.notificationUri;
        context.getContentResolver().delete(operationUri, null, null);
    }

    public static final String[] COLUMN_ALL = { OperationsSchema.COLUMN_ID };

    public static void clean(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(OperationsContentProvider.CONTENT_URI, COLUMN_ALL,
                    OperationsSchema.COLUMN_STATUS + " IN (" + OperationStatus.STATUS_SUCCESSFUL +"," + OperationStatus.STATUS_CANCEL + ")", null, null);
            if (cursor == null) { return; }
            if (!cursor.isFirst())
            {
                cursor.moveToPosition(-1);
            }
            Uri uri = null;
            for (int i = 0; i < cursor.getCount(); i++)
            {
                cursor.moveToPosition(i);
                uri = Uri.parse(OperationsContentProvider.CONTENT_URI + "/" + cursor.getInt(OperationSchema.COLUMN_ID_ID));
                context.getContentResolver().delete(uri, null, null);
            }
        }
        catch (Exception e)
        {
            // DO Nothing
        }
        finally
        {
            CursorUtils.closeCursor(cursor);
        }
    }
}

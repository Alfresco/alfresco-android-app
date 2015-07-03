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
package org.alfresco.mobile.android.application.managers;

import org.alfresco.mobile.android.application.database.DatabaseManagerImpl;
import org.alfresco.mobile.android.platform.favorite.FavoritesManager;

import android.content.Context;
import android.database.Cursor;

public final class FavoritesManagerImpl extends FavoritesManager
{
    // ////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ////////////////////////////////////////////////////
    private FavoritesManagerImpl(Context applicationContext)
    {
        super(applicationContext);
    }

    public static FavoritesManagerImpl getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new FavoritesManagerImpl(context.getApplicationContext());
            }

            return (FavoritesManagerImpl) mInstance;
        }
    }

    // ////////////////////////////////////////////////////
    // OVERRIDE
    // ////////////////////////////////////////////////////
    protected Long retrieveSize(String query)
    {
        Long totalSize = null;

        // Retrieve the TOTAL sum of children
        Cursor cursorTotal = DatabaseManagerImpl.getInstance(appContext).getWriteDb().rawQuery(query, null);
        if (cursorTotal.moveToFirst())
        {
            totalSize = cursorTotal.getLong(0);
        }
        return totalSize;
    }
}

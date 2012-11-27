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
package org.alfresco.mobile.android.application.database;

import java.util.List;

import android.content.Context;

public abstract class DAO<T>
{

    protected Context context;

    public DAO(Context context)
    {
        super();
        this.context = context;
    }

    public abstract T findById(long id);

    public abstract List<T> findAll();

    public abstract boolean delete(long id);
}

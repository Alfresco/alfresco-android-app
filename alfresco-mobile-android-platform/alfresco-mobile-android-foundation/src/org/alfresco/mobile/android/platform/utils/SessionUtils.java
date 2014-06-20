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
package org.alfresco.mobile.android.platform.utils;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.platform.SessionManager;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;

public final class SessionUtils
{
    private SessionUtils()
    {
    }

    public static AlfrescoSession getSession(Context c)
    {
        if (c instanceof AlfrescoActivity)
        {
            return ((AlfrescoActivity) c).getCurrentSession();
        }
        else
        {
            return SessionManager.getInstance(c).getCurrentSession();
        }
    }

    public static AlfrescoAccount getAccount(Context c)
    {
        if (c instanceof AlfrescoActivity)
        {
            return ((AlfrescoActivity) c).getCurrentAccount();
        }
        else
        {
            return SessionManager.getInstance(c).getCurrentAccount();
        }
    }

    public static AlfrescoSession getSession(Context c, long accountId)
    {
        return SessionManager.getInstance(c).getSession(accountId);
    }

    public static void checkSession(Activity activity, AlfrescoSession alfSession)
    {
        if (alfSession == null)
        {
            activity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

}

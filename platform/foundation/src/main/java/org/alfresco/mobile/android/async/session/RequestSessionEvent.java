/*******************************************************************************
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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
package org.alfresco.mobile.android.async.session;

import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.api.session.authentication.SamlData;
import org.alfresco.mobile.android.platform.accounts.AlfrescoAccount;

public class RequestSessionEvent
{
    public final AlfrescoAccount accountToLoad;

    public final OAuthData data;

    public final SamlData samlData;

    public final String networkId;

    public final boolean requestReload;

    public final boolean resetStack;

    public RequestSessionEvent(AlfrescoAccount accountToLoad)
    {
        this.accountToLoad = accountToLoad;
        this.data = null;
        this.requestReload = false;
        this.networkId = null;
        this.samlData = null;
        this.resetStack = true;
    }

    public RequestSessionEvent(AlfrescoAccount accountToLoad, OAuthData data)
    {
        this.accountToLoad = accountToLoad;
        this.data = data;
        this.requestReload = false;
        this.networkId = null;
        this.samlData = null;
        this.resetStack = true;
    }

    public RequestSessionEvent(AlfrescoAccount accountToLoad, SamlData samlData)
    {
        this.accountToLoad = accountToLoad;
        this.samlData = samlData;
        this.requestReload = false;
        this.networkId = null;
        this.data = null;
        this.resetStack = true;
    }

    public RequestSessionEvent(AlfrescoAccount accountToLoad, SamlData samlData, boolean resetStack)
    {
        this.accountToLoad = accountToLoad;
        this.samlData = samlData;
        this.requestReload = false;
        this.networkId = null;
        this.data = null;
        this.resetStack = resetStack;
    }

    public RequestSessionEvent(AlfrescoAccount accountToLoad, boolean reload)
    {
        this.accountToLoad = accountToLoad;
        this.networkId = null;
        this.data = null;
        this.requestReload = reload;
        this.samlData = null;
        this.resetStack = true;
    }

    public RequestSessionEvent(AlfrescoAccount accountToLoad, String networkId, boolean reload)
    {
        this.accountToLoad = accountToLoad;
        this.networkId = networkId;
        this.data = null;
        this.requestReload = reload;
        this.samlData = null;
        this.resetStack = true;
    }
}

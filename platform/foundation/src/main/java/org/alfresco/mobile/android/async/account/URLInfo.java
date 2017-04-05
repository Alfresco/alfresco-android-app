/*
 *  Copyright (C) 2005-2017 Alfresco Software Limited.
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

package org.alfresco.mobile.android.async.account;

import java.io.Serializable;

import org.alfresco.mobile.android.api.session.authentication.SamlInfo;

/**
 * Created by jpascal on 13/02/2017.
 */

public class URLInfo implements Serializable
{
    public final String baseUrl;

    public final String testUrl;

    public final boolean enforceCMIS;

    public final boolean isComplete;

    public final SamlInfo samlData;

    public URLInfo(String baseUrl, String testUrl, boolean enforceCMIS, boolean isComplete, SamlInfo samlData)
    {
        this.baseUrl = baseUrl;
        this.testUrl = testUrl;
        this.enforceCMIS = enforceCMIS;
        this.isComplete = isComplete;
        this.samlData = samlData;
    }

    public URLInfo(String baseUrl, String testUrl, boolean enforceCMIS, boolean isComplete)
    {
        this.baseUrl = baseUrl;
        this.testUrl = testUrl;
        this.enforceCMIS = enforceCMIS;
        this.isComplete = isComplete;
        this.samlData = null;
    }

    public URLInfo(String baseUrl, String testUrl)
    {
        this.baseUrl = baseUrl;
        this.testUrl = testUrl;
        this.enforceCMIS = false;
        this.isComplete = true;
        this.samlData = null;
    }

    public URLInfo(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.testUrl = baseUrl;
        this.enforceCMIS = false;
        this.isComplete = true;
        this.samlData = null;
    }
}

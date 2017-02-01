/*
 *  Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.mobile.android.api.model.config;

/**
 * Base type for View Configuration.
 * 
 * @author Jean Marie Pascal
 */
public interface FeatureConfig extends ItemConfig
{
    // ///////////////////////////////////////////////////////////////////////////
    // DEFAULT FEATURE
    // ///////////////////////////////////////////////////////////////////////////
    String PREFIX_FEATURE = "org.alfresco.client.feature";

    // If ON ==> user can define the value
    // If OFF ==> analytics is OFF
    String FEATURE_ANALYTICS = PREFIX_FEATURE.concat(".analytics");

    // If ON ==> data protection is activated and cant be changed
    // If OFF ==> user can define the value
    // If ON then OFF ==> protection is still active but can be deactivated by
    // the user.
    String FEATURE_DATA_PROTECTION = PREFIX_FEATURE.concat(".data.protection");

    // If ON ==> passcode is activated by default and cant be changed
    // If OFF ==> user can define the value
    // If ON then OFF ==> passcode is still active but can be deactivated by the
    // user.
    String FEATURE_PASSCODE = PREFIX_FEATURE.concat(".passcode");

    // If ON ==> user can define the value
    // If OFF ==> cellular sync is OFF / Sync only on Wifi and cant be changed
    // If OFF then ON ==> cellular sync is still OFF but can be reactivated by
    // the user.
    String FEATURE_CELLULAR_SYNC = PREFIX_FEATURE.concat(".sync.cellular");

    // If ON ==> user can define the value
    // If OFF ==> cellular sync is OFF / Sync only on Wifi and cant be changed
    // If OFF then ON ==> cellular sync is still OFF but can be reactivated by
    // the user.
    String FEATURE_SCHEDULER_SYNC = PREFIX_FEATURE.concat(".sync.scheduler");

    boolean isEnable();
}

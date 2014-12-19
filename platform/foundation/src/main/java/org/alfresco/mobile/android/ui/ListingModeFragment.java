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
package org.alfresco.mobile.android.ui;

public interface ListingModeFragment
{
    String ARGUMENT_MODE = "org.alfresco.mobile.android.application.param.mode";

    String ARGUMENT_FRAGMENT_TAG = "org.alfresco.mobile.android.application.param.fragment.tag";

    String ARGUMENT_SINGLE_CHOICE = "org.alfresco.mobile.android.application.param.fragment.singleChoice";

    /** Normal case where user can interact with everything. */
    int MODE_LISTING = 1;

    /** Select one or multiple document. */
    int MODE_PICK = 2;

    /** Select a folder */
    int MODE_IMPORT = 4;

    /** Display progress. */
    int MODE_PROGRESS = 4;

    int getMode();

}

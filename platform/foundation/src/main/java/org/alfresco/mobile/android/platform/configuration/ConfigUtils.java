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
package org.alfresco.mobile.android.platform.configuration;

import android.content.Context;

/**
 * @author Jean Marie Pascal
 */
public final class ConfigUtils
{
    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private static final String STRING = "string";

    private static final String SEPARATOR = "_";

    public static final String FAMILY_VIEW = "config";

    public static final String FAMILY_FORM = "form";

    public static final String FAMILY_VALIDATION = "validation";

    public static final String FAMILY_EVALUATORS = "evaluator";

    public static final String FAMILY_OPERATION = "operation";

    public static final String FAMILY_MANAGER = "manager";

    public static final String FAMILY_OPERATION_CALLBACK = "callback";

    public static String getString(Context context, String family, String key)
    {
        int stringId = context.getResources().getIdentifier(
                family.concat(SEPARATOR).concat(key).replace(".", SEPARATOR).replace("-", SEPARATOR), STRING, context.getApplicationContext().getPackageName());
        return (stringId == 0) ? null : context.getString(stringId);
    }

}

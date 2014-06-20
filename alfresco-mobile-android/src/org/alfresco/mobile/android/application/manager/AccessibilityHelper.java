/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.manager;

import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;

/**
 * @author Jean Marie Pascal
 */
public class AccessibilityHelper
{

    public static AccessibilityManager getAccessibilityManager(Context context)
    {
        return (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public static boolean isEnabled(Context context)
    {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am.isEnabled();
    }

    public static boolean isTouchExplorationEnabled(Context context)
    {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am.isTouchExplorationEnabled();
    }

    public static void sendAccessibilityEvent(Context context)
    {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am.isEnabled())
        {
            am.sendAccessibilityEvent(AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED));
        }
    }

    public static void addContentDescription(View v, int contentDescriptionId)
    {
        if (isEnabled(v.getContext()))
        {
            addContentDescription(v, v.getContext().getString(contentDescriptionId));
        }
    }

    public static void addHint(View v, int contentDescriptionId)
    {
        if (isEnabled(v.getContext()) && v instanceof EditText)
        {
            ((EditText) v).setHint(v.getContext().getString(contentDescriptionId));
        }
    }

    public static void addContentDescription(View v, String contentDescription)
    {
        if (isEnabled(v.getContext()))
        {
            if (AndroidVersion.isJBOrAbove())
            {
                v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
            v.setContentDescription(contentDescription);
        }
    }

    public static void notifyActionCompleted(Context context, int actionConfirmedMessageId)
    {
        notifyActionCompleted(context, context.getString(actionConfirmedMessageId));
    }

    public static void notifyActionCompleted(Context context, String actionMessage)
    {
        if (isEnabled(context))
        {
            MessengerManager.showLongToast(context, actionMessage);
        }
    }

    public static void removeContentDescription(View v)
    {
        if (isEnabled(v.getContext()))
        {
            if (AndroidVersion.isJBOrAbove())
            {
                v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            else
            {
                v.setContentDescription("\u00A0");
            }
        }
    }
}

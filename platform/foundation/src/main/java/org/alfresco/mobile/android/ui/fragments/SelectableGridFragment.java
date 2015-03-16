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
/*******************************************************************************
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.view.View;
import android.widget.GridView;

@TargetApi(11)
public abstract class SelectableGridFragment<T> extends BaseGridFragment
{
    protected List<T> selectedItems = new ArrayList<T>(1);
    
    protected boolean displayItemSelection = true;

    protected boolean allowMultipleItemSelection = false;

    // //////////////////////////////////////////////////////////////////////
    // ITEM SELECTION
    // //////////////////////////////////////////////////////////////////////
    protected boolean equalsItems(T itemAlreadySelected, T itemSelected)
    {
        return false;
    }
    
    protected void onItemUnselected(T unSelectedItem)
    {
    }
    
    protected void onItemSelected(T selectedItem)
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onListItemClick(GridView g, View v, int position, long id)
    {
        T selectedObject = (T) g.getItemAtPosition(position);
        Boolean unselect = false;

        // Detect if the object is already present
        if (!selectedItems.isEmpty())
        {
            unselect = equalsItems(selectedItems.get(0), selectedObject);
            selectedItems.clear();
        }

        // Single or multiple choice ?
        if (allowMultipleItemSelection)
        {
            g.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        }
        else
        {
            g.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        }

        // Mark views as selected and checked
        if (displayItemSelection)
        {
            g.setItemChecked(position, true);
            g.setSelection(position);
            v.setSelected(true);
        }

        if (unselect)
        {
            // Unselect one Item
            onItemUnselected(selectedObject);
            if (!allowMultipleItemSelection)
            {
                selectedItems.clear();
            }
        }
        else
        {
            //Item Selection
            selectedItems.add(selectedObject);
            onItemSelected(selectedObject);
        }
    }
}

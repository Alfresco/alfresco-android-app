/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.extension.samsung.pen;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.extension.samsung.R;

import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectContainer;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;

public class SNoteEditorActionMode implements ActionMode.Callback
{
    protected ActionMode mode;

    protected onFinishModeListerner mListener;

    private ArrayList<SpenObjectBase> selectedSpenObjects = new ArrayList<SpenObjectBase>();

    private SpenPageDoc spenPageDoc;

    private SpenSurfaceView spenSurfaceView;

    public SNoteEditorActionMode(FragmentActivity ac, SpenPageDoc spenPageDoc, SpenSurfaceView spenSurfaceView,
            List<SpenObjectBase> selectedSpenObjects)
    {
        this.selectedSpenObjects = new ArrayList<SpenObjectBase>(selectedSpenObjects);
        this.spenPageDoc = spenPageDoc;
        this.spenSurfaceView = spenSurfaceView;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        this.mode = mode;
        getMenu(menu);
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        if (mListener != null)
        {
            mListener.onFinish();
        }
        selectedSpenObjects.clear();
        spenPageDoc = null;
        spenSurfaceView = null;
    }

    public void finish()
    {
        if (spenSurfaceView != null)
        {
            spenSurfaceView.closeControl();
            spenSurfaceView.update();
        }

        mode.finish();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        SpenObjectContainer objContainer;
        SpenObjectBase object = selectedSpenObjects.get(0);
        Boolean b = false;

        switch (item.getItemId())
        {
            case SNoteMenuActionItem.CONTEXT_MENU_DELETE_ID:
                for (SpenObjectBase obj : selectedSpenObjects)
                {
                    spenPageDoc.removeObject(obj);
                }
                // spenSurfaceView.closeControl();
                spenSurfaceView.update();
                b = true;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_GROUP_ID:
                objContainer = spenPageDoc.groupObject(selectedSpenObjects, false);
                // spenSurfaceView.closeControl();
                spenPageDoc.selectObject(objContainer);
                spenSurfaceView.update();
                b = true;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_UNGROUP_ID:
                ArrayList<SpenObjectBase> objList = new ArrayList<SpenObjectBase>();
                for (SpenObjectBase selectedObj : selectedSpenObjects)
                {
                    if (selectedObj.getType() == SpenObjectBase.TYPE_CONTAINER)
                    {
                        objContainer = (SpenObjectContainer) selectedObj;
                        for (SpenObjectBase obj : objContainer.getObjectList())
                        {
                            objList.add(obj);
                        }
                        spenPageDoc.ungroupObject((SpenObjectContainer) selectedObj, false);
                    }
                }
                // spenSurfaceView.closeControl();
                spenPageDoc.selectObject(objList);
                spenSurfaceView.update();
                b = true;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BOTTOM_ID:
                spenPageDoc.moveObjectIndex(object, -spenPageDoc.getObjectIndex(object), true);
                spenSurfaceView.update();
                b = false;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BACKWARD_ID:
                if (spenPageDoc.getObjectIndex(object) > 0)
                {
                    spenPageDoc.moveObjectIndex(object, -1, true);
                    spenSurfaceView.update();
                }
                b = false;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_FORWARD_ID:
                if (spenPageDoc.getObjectIndex(object) < spenPageDoc.getObjectCount(true) - 1)
                {
                    spenPageDoc.moveObjectIndex(object, 1, true);
                    spenSurfaceView.update();
                }
                b = false;
                break;

            case SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_TOP_ID:
                spenPageDoc.moveObjectIndex(object,
                        spenPageDoc.getObjectCount(true) - 1 - spenPageDoc.getObjectIndex(object), true);
                spenSurfaceView.update();
                b = false;
                break;
            default:
                break;
        }
        if (b)
        {
            spenSurfaceView.closeControl();
        }

        return b;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    private void getMenu(Menu menu)
    {
        MenuItem mi;
        menu.clear();

        mi = menu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_DELETE_ID, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS, R.string.editor_tools_settings);
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // GROUP MENU
        if (selectedSpenObjects.size() > 1)
        {
            SubMenu groupSubMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_TOOLS, Menu.FIRST,
                    R.string.editor_group_menu);
            groupSubMenu.setIcon(R.drawable.ic_group);
            groupSubMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            mi = groupSubMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_GROUP_ID, Menu.FIRST
                    + SNoteMenuActionItem.CONTEXT_MENU_GROUP_ID, R.string.editor_group);

            mi = groupSubMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_UNGROUP_ID, Menu.FIRST
                    + SNoteMenuActionItem.CONTEXT_MENU_UNGROUP_ID, R.string.editor_ungroup);

            mi.setEnabled(false);
            for (SpenObjectBase obj : selectedSpenObjects)
            {
                if (obj.getType() == SpenObjectBase.TYPE_CONTAINER)
                {
                    mi.setEnabled(true);
                    break;
                }
            }
        }

        // UP MENU
        SubMenu upMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_TOOLS, Menu.FIRST,
                R.string.editor_front_menu);
        upMenu.setIcon(R.drawable.ic_move_up);
        upMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mi = upMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_FORWARD_ID, Menu.FIRST
                + SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_FORWARD_ID, R.string.editor_forward);

        mi = upMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_TOP_ID, Menu.FIRST
                + SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_TOP_ID, R.string.editor_front);

        // DOWN MENU
        SubMenu downMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_TOOLS, Menu.FIRST,
                R.string.editor_back_menu);
        downMenu.setIcon(R.drawable.ic_move_down);
        downMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mi = downMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BACKWARD_ID, Menu.FIRST
                + SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BACKWARD_ID, R.string.editor_backward);

        mi = downMenu.add(Menu.NONE, SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BOTTOM_ID, Menu.FIRST
                + SNoteMenuActionItem.CONTEXT_MENU_MOVE_TO_BOTTOM_ID, R.string.editor_back);
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // LISTENER
    // ///////////////////////////////////////////////////////////////////////////////////
    public interface onFinishModeListerner
    {
        void onFinish();
    }

    public void setOnFinishModeListerner(onFinishModeListerner mListener)
    {
        this.mListener = mListener;
    }

}
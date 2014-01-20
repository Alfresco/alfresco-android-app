/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.favorites;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;
import org.alfresco.mobile.android.application.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.ProgressViewHolder;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**
 * @since 1.2
 * @author Jean Marie Pascal
 */
public class FavoriteCursorAdapter extends BaseCursorLoader<ProgressViewHolder> implements OnMenuItemClickListener
{
    //private static final String TAG = FavoriteCursorAdapter.class.getName();

    private Fragment fragment;

    private List<String> selectedItems;

    private int mode;

    private List<String> selectedOptionItems = new ArrayList<String>();

    private List<Long> selectedOptionItemId = new ArrayList<Long>();

    private boolean hasSynchroActive;

    public FavoriteCursorAdapter(Fragment fr, Cursor c, int layoutResourceId, List<String> selectedItems, int mode)
    {
        super(fr.getActivity(), c, layoutResourceId);
        this.fragment = fr;
        this.selectedItems = selectedItems;
        this.mode = mode;
        vhClassName = ProgressViewHolder.class.getCanonicalName();
        hasSynchroActive = GeneralPreferences.hasActivateSync(context, SessionUtils.getAccount(context));
    }

    protected void updateIcon(ProgressViewHolder vh, Cursor cursor)
    {
        vh.icon.setImageResource(MimeTypeManager.getIcon(context, cursor.getString(SynchroSchema.COLUMN_TITLE_ID)));
    }

    protected void updateBottomText(ProgressViewHolder vh, final Cursor cursor)
    {
        int status = cursor.getInt(SynchroSchema.COLUMN_STATUS_ID);
        String nodeId = cursor.getString(SynchroSchema.COLUMN_NODE_ID_ID);
        long favoriteId = cursor.getLong(SynchroSchema.COLUMN_ID_ID);

        vh.progress.setVisibility(View.GONE);
        vh.iconTopRight.setVisibility(View.GONE);
        switch (status)
        {
            case SyncOperation.STATUS_PENDING:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncOperation.STATUS_RUNNING:
                displayStatut(vh, R.drawable.sync_status_loading);
                vh.progress.setVisibility(View.VISIBLE);
                long totalSize = cursor.getLong(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                if (totalSize == -1)
                {
                    vh.progress.setIndeterminate(true);
                }
                else
                {
                    long progress = cursor.getLong(SynchroSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                    float value = (((float) progress / ((float) totalSize)) * 100);
                    int percentage = Math.round(value);
                    vh.progress.setIndeterminate(false);
                    vh.progress.setMax(100);
                    vh.progress.setProgress(percentage);
                }
                break;
            case SyncOperation.STATUS_PAUSED:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncOperation.STATUS_MODIFIED:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncOperation.STATUS_SUCCESSFUL:
                displayStatut(vh, R.drawable.sync_status_success);
                break;
            case SyncOperation.STATUS_FAILED:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            case SyncOperation.STATUS_CANCEL:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            case SyncOperation.STATUS_REQUEST_USER:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            default:
                break;
        }

        if (selectedItems != null && selectedItems.contains(nodeId))
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), null);
        }

        if (SyncOperation.STATUS_RUNNING != status)
        {
            vh.bottomText.setVisibility(View.VISIBLE);
            vh.bottomText.setText(createContentBottomText(context, cursor));
        } else {
            vh.bottomText.setVisibility(View.GONE);
        }

        if (mode == FavoritesSyncFragment.MODE_LISTING && fragment.getActivity() instanceof MainActivity
                && hasSynchroActive)
        {
            UIUtils.setBackground(((View) vh.choose),
                    context.getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setTag(R.id.node_action, nodeId);
            vh.choose.setTag(R.id.favorite_id, favoriteId);
            vh.choose.setTag(R.id.operation_status, status);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    String item = (String) v.getTag(R.id.node_action);
                    Integer statut = (Integer) v.getTag(R.id.operation_status);
                    long favoriteId = (Long) v.getTag(R.id.favorite_id);

                    selectedOptionItems.add(item);
                    selectedOptionItemId.add(favoriteId);

                    PopupMenu popup = new PopupMenu(context, v);
                    getMenu(popup.getMenu(), statut);

                    if (AndroidVersion.isICSOrAbove())
                    {
                        popup.setOnDismissListener(new OnDismissListener()
                        {
                            @Override
                            public void onDismiss(PopupMenu menu)
                            {
                                selectedOptionItems.clear();
                            }
                        });
                    }

                    popup.setOnMenuItemClickListener(FavoriteCursorAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(((View) vh.choose), null);
        }
    }

    private String createContentBottomText(Context context, Cursor cursor)
    {
        String s = "";

        if (cursor.getLong(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID) != -1)
        {
            s = Formatter.formatToRelativeDate(context,
                    new Date(cursor.getLong(SynchroSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID)));
            s += " - " + Formatter.formatFileSize(context, cursor.getLong(SynchroSchema.COLUMN_TOTAL_SIZE_BYTES_ID));
        }
        return s;
    }

    protected void updateTopText(ProgressViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(SynchroSchema.COLUMN_TITLE_ID));
    }

    protected void displayStatut(ProgressViewHolder vh, int imageResource)
    {
        if (hasSynchroActive)
        {
            vh.iconBottomRight.setVisibility(View.VISIBLE);
            vh.iconBottomRight.setImageResource(imageResource);
        }
        else
        {
            vh.iconBottomRight.setVisibility(View.GONE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu, Integer statut)
    {
        MenuItem mi;

        switch (statut)
        {
            case SyncOperation.STATUS_HIDDEN:
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, Menu.FIRST
                        + MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE, R.string.favorite);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
            case SyncOperation.STATUS_FAILED:
            case SyncOperation.STATUS_REQUEST_USER:
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_RESOLVE_CONFLICT, Menu.FIRST
                        + MenuActionItem.MENU_RESOLVE_CONFLICT, R.string.sync_resolve_conflict);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
            default:
               mi = menu.add(Menu.NONE, MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, Menu.FIRST
                        + MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE, R.string.unfavorite);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        OperationsRequestGroup group = new OperationsRequestGroup(context, SessionUtils.getAccount(context));

        switch (item.getItemId())
        {
            case MenuActionItem.MENU_FAVORITE_GROUP_FAVORITE:
                for (String node : selectedOptionItems)
                {
                    group.enqueue(new FavoriteNodeRequest(null, node, true)
                            .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
                }
                BatchOperationManager.getInstance(context).enqueue(group);
                onMenuItemClick = true;
                break;
            case MenuActionItem.MENU_FAVORITE_GROUP_UNFAVORITE:
                for (String node : selectedOptionItems)
                {
                    group.enqueue(new FavoriteNodeRequest(null, node, false)
                            .setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
                }
                BatchOperationManager.getInstance(context).enqueue(group);
                onMenuItemClick = true;
                break;
            case MenuActionItem.MENU_RESOLVE_CONFLICT:
                onMenuItemClick = true;
                ResolveSyncConflictFragment.newInstance(selectedOptionItemId.get(0)).show(
                        fragment.getActivity().getFragmentManager(), ResolveSyncConflictFragment.TAG);
                selectedOptionItemId.clear();
                break;
            case MenuActionItem.MENU_DETAILS:
                onMenuItemClick = true;
                ((MainActivity) context).addPropertiesFragment(selectedOptionItems.get(0));
                selectedItems.clear();
                selectedItems.add(selectedOptionItems.get(0));
                notifyDataSetChanged();
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }

    public void refresh()
    {
        hasSynchroActive = GeneralPreferences.hasActivateSync(context, SessionUtils.getAccount(context));

    }
}
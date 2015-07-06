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
package org.alfresco.mobile.android.application.fragments.sync;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.GridAdapterHelper;
import org.alfresco.mobile.android.application.fragments.node.details.NodeDetailsFragment;
import org.alfresco.mobile.android.application.fragments.utils.ProgressViewHolder;
import org.alfresco.mobile.android.async.OperationRequest.OperationBuilder;
import org.alfresco.mobile.android.async.OperationStatus;
import org.alfresco.mobile.android.async.Operator;
import org.alfresco.mobile.android.async.node.sync.SyncNodeRequest;
import org.alfresco.mobile.android.platform.mimetype.MimeType;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.sync.SyncContentManager;
import org.alfresco.mobile.android.sync.SyncContentSchema;
import org.alfresco.mobile.android.sync.operations.SyncContentStatus;
import org.alfresco.mobile.android.ui.GridFragment;
import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.rendition.RenditionManager;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**
 * @since 1.2
 * @author Jean Marie Pascal
 */
public class SyncCursorAdapter extends BaseCursorLoader<ProgressViewHolder> implements OnMenuItemClickListener
{
    private static final String TAG = SyncCursorAdapter.class.getName();

    private WeakReference<Fragment> fragmentRef;

    private List<String> selectedItems;

    private int mode;

    private List<String> selectedOptionItems = new ArrayList<String>();

    private List<Long> selectedOptionItemId = new ArrayList<Long>();

    private boolean hasSynchroActive;

    public SyncCursorAdapter(Fragment fr, Cursor c, int layoutResourceId, List<String> selectedItems, int mode)
    {
        super(fr.getActivity(), c, layoutResourceId);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.selectedItems = selectedItems;
        this.mode = mode;
        vhClassName = ProgressViewHolder.class.getCanonicalName();
        hasSynchroActive = SyncContentManager.getInstance(fr.getActivity()).hasActivateSync(
                SessionUtils.getAccount(context));
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        // Specific part for dynaminc resize
        int[] layouts = GridAdapterHelper.getGridLayoutId(context, (GridFragment) fragmentRef.get());
        return createView(context, cursor, layouts[0]);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        int[] layouts = GridAdapterHelper.getGridLayoutId(context, (GridFragment) fragmentRef.get());
        if (view.findViewById(layouts[1]) == null)
        {
            // ((ViewGroup) view).removeAllViews();
            view.invalidate();
            view.refreshDrawableState();
            return;
        }
        ProgressViewHolder vh = (ProgressViewHolder) view.getTag();
        updateControls(vh, cursor);
    }

    protected void updateIcon(ProgressViewHolder vh, Cursor cursor)
    {
        if (SyncContentManager.isFolder(cursor))
        {
            vh.icon.setImageResource(R.drawable.mime_256_folder);
        }
        else
        {
            MimeType mime = MimeTypeManager.getInstance(context).getMimetype(
                    cursor.getString(SyncContentSchema.COLUMN_TITLE_ID));

            RenditionManager
                    .with(fragmentRef.get().getActivity())
                    .loadNode(cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID))
                    .placeHolder(
                            mime != null ? mime.getLargeIconId(context) : MimeTypeManager.getInstance(context).getIcon(
                                    cursor.getString(SyncContentSchema.COLUMN_TITLE_ID), true)).into(vh.icon);

            if (mime != null)
            {
                AccessibilityUtils.addContentDescription(vh.icon, mime.getDescription());
            }
            else
            {
                AccessibilityUtils.removeContentDescription(vh.icon);
            }
        }

    }

    protected void updateBottomText(ProgressViewHolder vh, final Cursor cursor)
    {
        int status = cursor.getInt(SyncContentSchema.COLUMN_STATUS_ID);
        String nodeId = cursor.getString(SyncContentSchema.COLUMN_NODE_ID_ID);
        long favoriteId = cursor.getLong(SyncContentSchema.COLUMN_ID_ID);
        boolean syncRoot = cursor.getInt(SyncContentSchema.COLUMN_IS_SYNC_ROOT_ID) > 0;

        if (syncRoot)
        {
            vh.favoriteIcon.setVisibility(View.VISIBLE);
            vh.favoriteIcon.setImageResource(R.drawable.ic_sync_light);
        }
        else
        {
            vh.favoriteIcon.setVisibility(View.GONE);
        }

        vh.progress.setVisibility(View.GONE);
        switch (status)
        {
            case SyncContentStatus.STATUS_PENDING:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncContentStatus.STATUS_RUNNING:
                displayStatut(vh, R.drawable.sync_status_loading);
                vh.progress.setVisibility(View.VISIBLE);
                vh.favoriteIcon.setVisibility(View.GONE);
                long totalSize = cursor.getLong(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
                if (totalSize == -1)
                {
                    vh.progress.setIndeterminate(true);
                }
                else
                {
                    long progress = cursor.getLong(SyncContentSchema.COLUMN_BYTES_DOWNLOADED_SO_FAR_ID);
                    float value = (((float) progress / ((float) totalSize)) * 100);
                    int percentage = Math.round(value);
                    vh.progress.setIndeterminate(false);
                    vh.progress.setMax(100);
                    vh.progress.setProgress(percentage);
                }
                break;
            case SyncContentStatus.STATUS_PAUSED:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncContentStatus.STATUS_MODIFIED:
                displayStatut(vh, R.drawable.sync_status_pending);
                break;
            case SyncContentStatus.STATUS_SUCCESSFUL:
                displayStatut(vh, R.drawable.sync_status_success);
                break;
            case SyncContentStatus.STATUS_FAILED:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            case SyncContentStatus.STATUS_CANCEL:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            case SyncContentStatus.STATUS_REQUEST_USER:
                displayStatut(vh, R.drawable.sync_status_failed);
                break;
            default:
                break;
        }

        if (selectedItems != null && selectedItems.contains(nodeId))
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent()), null);
        }

        if (SyncContentStatus.STATUS_RUNNING != status)
        {
            vh.bottomText.setVisibility(View.VISIBLE);
            vh.bottomText.setText(createContentBottomText(context, cursor));
            AccessibilityUtils
                    .addContentDescription(vh.bottomText, createContentDescriptionBottomText(context, cursor));
        }
        else
        {
            vh.bottomText.setVisibility(View.GONE);
        }

        if (mode == SyncFragment.MODE_LISTING && fragmentRef.get().getActivity() instanceof MainActivity)
        {
            if (status == OperationStatus.STATUS_SUCCESSFUL || status == OperationStatus.STATUS_PENDING)
            {
                UIUtils.setBackground(vh.choose, null);
                return;
            }
            UIUtils.setBackground(vh.choose,
                    context.getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setTag(R.id.node_action, nodeId);
            vh.choose.setTag(R.id.favorite_id, favoriteId);
            vh.choose.setTag(R.id.operation_status, status);
            vh.choose.setTag(R.id.is_synced, syncRoot);
            AccessibilityUtils.addContentDescription(
                    vh.choose,
                    String.format(context.getString(R.string.more_options_favorite),
                            cursor.getString(SyncContentSchema.COLUMN_TITLE_ID)));
            vh.choose.setOnClickListener(new OnClickListener()
            {
                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onClick(View v)
                {
                    String item = (String) v.getTag(R.id.node_action);
                    Integer statut = (Integer) v.getTag(R.id.operation_status);
                    long favoriteId = (Long) v.getTag(R.id.favorite_id);
                    boolean rootSynced = (Boolean) v.getTag(R.id.is_synced);

                    selectedOptionItems.add(item);
                    selectedOptionItemId.add(favoriteId);

                    PopupMenu popup = new PopupMenu(context, v);
                    getMenu(popup.getMenu(), statut, rootSynced);

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

                    popup.setOnMenuItemClickListener(SyncCursorAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(vh.choose, null);
        }
    }

    private String createContentBottomText(Context context, Cursor cursor)
    {
        String s = "";

        if (cursor.getLong(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID) != -1)
        {
            s = Formatter.formatToRelativeDate(context,
                    new Date(cursor.getLong(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID)));
            long size = cursor.getLong(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
            if (size > 0)
            {
                s += " - " + Formatter.formatFileSize(context, size);
            }
        }

        return s;
    }

    private String createContentDescriptionBottomText(Context context, Cursor cursor)
    {
        StringBuilder s = new StringBuilder();

        s.append(context.getString(R.string.metadata_modified));
        s.append(Formatter.formatToRelativeDate(context,
                new Date(cursor.getLong(SyncContentSchema.COLUMN_SERVER_MODIFICATION_TIMESTAMP_ID))));
        long size = cursor.getLong(SyncContentSchema.COLUMN_TOTAL_SIZE_BYTES_ID);
        if (size > 0)
        {
            s.append(" - ");
            s.append(context.getString(R.string.metadata_size));
            s.append(Formatter.formatFileSize(context, size));
        }
        return s.toString();
    }

    protected void updateTopText(ProgressViewHolder vh, Cursor cursor)
    {
        vh.topText.setText(cursor.getString(SyncContentSchema.COLUMN_TITLE_ID));
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
    public void getMenu(Menu menu, Integer statut, boolean rootSynced)
    {
        MenuItem mi;

        switch (statut)
        {
            case SyncContentStatus.STATUS_HIDDEN:
                mi = menu.add(Menu.NONE, R.id.menu_action_favorite_group, Menu.FIRST, R.string.favorite);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
            case SyncContentStatus.STATUS_FAILED:
            case SyncContentStatus.STATUS_REQUEST_USER:
                mi = menu.add(Menu.NONE, R.id.menu_sync_resolution, Menu.FIRST, R.string.sync_resolve_conflict);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
            default:
                if (rootSynced)
                {
                    mi = menu.add(Menu.NONE, R.id.menu_action_sync_group_unsync, Menu.FIRST, R.string.unsync);
                    mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        List<OperationBuilder> requestsBuilder = new ArrayList<OperationBuilder>(selectedOptionItems.size());

        switch (item.getItemId())
        {
            case R.id.menu_action_sync_group_sync:
                for (String nodeId : selectedOptionItems)
                {
                    requestsBuilder.add(new SyncNodeRequest.Builder(nodeId, true, true));
                }
                Operator.with(context, SessionUtils.getAccount(context)).load(requestsBuilder);
                onMenuItemClick = true;
                break;
            case R.id.menu_action_sync_group_unsync:
                for (String nodeId : selectedOptionItems)
                {
                    requestsBuilder.add(new SyncNodeRequest.Builder(nodeId, false, true));
                }
                Operator.with(context, SessionUtils.getAccount(context)).load(requestsBuilder);
                onMenuItemClick = true;
                break;
            case R.id.menu_sync_resolution:
                onMenuItemClick = true;
                ResolveConflictSyncDialogFragment.newInstance(selectedOptionItemId.get(0)).show(
                        fragmentRef.get().getActivity().getFragmentManager(), ResolveConflictSyncDialogFragment.TAG);
                selectedOptionItemId.clear();
                break;
            case R.id.menu_node_details:
                onMenuItemClick = true;
                NodeDetailsFragment.with((Activity) context).nodeId(selectedOptionItems.get(0)).display();
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
        hasSynchroActive = SyncContentManager.getInstance(context).hasActivateSync(SessionUtils.getAccount(context));
    }
}
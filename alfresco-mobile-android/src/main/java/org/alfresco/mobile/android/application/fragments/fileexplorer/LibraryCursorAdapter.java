/*
 *  Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;

/**
 * @since 1.3
 * @author Jean Marie Pascal
 */
public class LibraryCursorAdapter extends BaseCursorLoader<TwoLinesViewHolder>implements OnMenuItemClickListener
{
    private List<File> selectedItems;

    private List<File> selectedOptionItems = new ArrayList<File>();

    private WeakReference<Fragment> fragmentRef;

    private final String sdcardPath;

    private final String downloadPath;

    private int mediaTypeId;

    private int mode = ListingModeFragment.MODE_LISTING;

    private RenditionManagerImpl renditionManager;

    public LibraryCursorAdapter(Fragment fr, Cursor c, int layoutId, List<File> selectedItems, int mediaTypeId,
            int mode)
    {
        super(fr.getActivity(), c, layoutId);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.selectedItems = selectedItems;
        this.sdcardPath = Environment.getExternalStorageDirectory().getPath();
        File f = AlfrescoStorageManager.getInstance(context)
                .getDownloadFolder(((BaseActivity) fr.getActivity()).getCurrentAccount());
        this.downloadPath = (f != null) ? f.getPath() : sdcardPath;
        this.mediaTypeId = mediaTypeId;
        this.mode = mode;
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        this.vhClassName = TwoLinesViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateTopText(TwoLinesViewHolder vh, Cursor cursor)
    {
        String topText = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
        if (topText != null)
        {
            topText = topText.replace(sdcardPath, "");
        }
        else
        {
            topText = "";
        }
        vh.topText.setText(topText);
    }

    @Override
    protected void updateBottomText(TwoLinesViewHolder vh, Cursor cursor)
    {
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
        File f = new File(data);
        if (!f.exists())
        {
            // Responsible to remove unused files from the medias store
            String where = MediaStore.Files.FileColumns._ID + "=?";
            String[] selectionArgs = { Long.toString(id) };
            context.getContentResolver().delete(MediaStore.Files.getContentUri("external"), where, selectionArgs);

            // Display an alert. The file is not available.
            vh.bottomText.setText("Non available");
        }
        else
        {
            if (data.startsWith(downloadPath))
            {
                vh.bottomText.setText(data.replace(downloadPath, ""));
            }
            else
            {
                vh.bottomText.setText(data.replace(sdcardPath, ""));
            }
        }

        HolderUtils.makeMultiLine(vh.bottomText, 3);

        if (selectedItems != null && selectedItems.contains(f))
        {
            UIUtils.setBackground(((RelativeLayout) vh.icon.getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((RelativeLayout) vh.icon.getParent()), null);
        }
    }

    @Override
    protected void updateIcon(TwoLinesViewHolder vh, Cursor cursor)
    {
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        File f = new File(data);

        switch (mediaTypeId)
        {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                vh.icon.setImageResource(R.drawable.mime_img);
                renditionManager.getPicasso().load(f).resize(150, 150).centerCrop().placeholder(R.drawable.mime_256_img)
                        .error(R.drawable.mime_256_img).into(vh.icon);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                vh.icon.setImageResource(R.drawable.mime_video);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                vh.icon.setImageResource(R.drawable.mime_audio);
                break;
            default:
                Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                vh.icon.setImageResource(MimeTypeManager.getInstance(context).getIcon(uri.getLastPathSegment()));
                break;
        }

        if (mode == FileExplorerFragment.MODE_LISTING && fragmentRef.get().getActivity() instanceof MainActivity)
        {
            vh.choose.setImageResource(R.drawable.ic_more_options);
            vh.choose.setBackgroundResource(R.drawable.alfrescohololight_list_selector_holo_light);
            int d_16 = DisplayUtils.getPixels(context, R.dimen.d_16);
            vh.choose.setPadding(d_16, d_16, d_16, d_16);
            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setTag(R.id.node_action, f);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onClick(View v)
                {
                    File item = (File) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(context, v);
                    getMenu(popup.getMenu(), item);
                    popup.setOnDismissListener(new OnDismissListener()
                    {
                        @Override
                        public void onDismiss(PopupMenu menu)
                        {
                            selectedOptionItems.clear();
                        }
                    });

                    popup.setOnMenuItemClickListener(LibraryCursorAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(vh.choose, null);
            vh.choose.setVisibility(View.GONE);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void getMenu(Menu menu, File f)
    {
        MenuItem mi;

        if (f.isFile())
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_share, Menu.FIRST, R.string.share);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            mi = menu.add(Menu.NONE, R.id.menu_upload, Menu.FIRST + 30, R.string.upload);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (f.canWrite() && downloadPath != null && f.getPath().startsWith(downloadPath))
        {
            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 1000, R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case R.id.menu_upload:
                onMenuItemClick = true;
                ActionUtils.actionSendDocumentToAlfresco((FragmentActivity) context, selectedOptionItems.get(0));
                break;
            case R.id.menu_action_share:
                onMenuItemClick = true;
                ActionUtils.actionShareContent((FragmentActivity) context, selectedOptionItems.get(0));
                break;
            case R.id.menu_action_delete:
                onMenuItemClick = true;
                FileActions.delete(fragmentRef.get(), new ArrayList<File>(selectedOptionItems));
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}

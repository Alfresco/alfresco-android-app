package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.BaseCursorLoader;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.utils.AndroidVersion;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class LibraryCursorAdapter extends BaseCursorLoader<GenericViewHolder> implements OnMenuItemClickListener
{
    private List<File> selectedItems;

    private List<File> selectedOptionItems = new ArrayList<File>();

    private Fragment fragment;

    private final String sdcardPath;

    private final String downloadPath;

    private String topText;

    private int mediaTypeId;

    private int mode = ListingModeFragment.MODE_LISTING;

    public LibraryCursorAdapter(Fragment fr, Cursor c, int layoutId, List<File> selectedItems, int mediaTypeId, int mode)
    {
        super(fr.getActivity(), c, layoutId);
        this.fragment = fr;
        this.selectedItems = selectedItems;
        this.sdcardPath = Environment.getExternalStorageDirectory().getPath();
        File f = StorageManager.getDownloadFolder(context, ((BaseActivity) fr.getActivity()).getCurrentAccount());
        this.downloadPath = (f != null) ? f.getPath() : sdcardPath;
        this.mediaTypeId = mediaTypeId;
        this.mode = mode;
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Cursor cursor)
    {
        topText = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
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
    protected void updateBottomText(GenericViewHolder vh, Cursor cursor)
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

        if (selectedItems != null && selectedItems.contains(f))
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()),
                    context.getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.icon.getParent().getParent()), null);
        }
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Cursor cursor)
    {
        //int myID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        File f = new File(data);

        switch (mediaTypeId)
        {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                /*String[] thumbColumns = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
                CursorLoader thumbCursorLoader = new CursorLoader(context,
                        MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns,
                        MediaStore.Images.Thumbnails.IMAGE_ID + "=" + myID, null, null);
                Cursor thumbCursor = thumbCursorLoader.loadInBackground();

                Bitmap myBitmap = null;
                if (thumbCursor.moveToFirst())
                {
                    int thCulumnIndex = thumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                    String thumbPath = thumbCursor.getString(thCulumnIndex);
                    myBitmap = BitmapFactory.decodeFile(thumbPath);
                    vh.icon.setImageBitmap(myBitmap);
                }
                else
                {
                    vh.icon.setImageResource(R.drawable.mime_img);
                }
                thumbCursor.close();*/
                vh.icon.setImageResource(R.drawable.mime_img);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                vh.icon.setImageResource(R.drawable.mime_video);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
                vh.icon.setImageResource(R.drawable.mime_audio);
                break;
            default:
                Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                vh.icon.setImageResource(MimeTypeManager.getIcon(uri.getLastPathSegment()));
                break;
        }

        if (mode == FileExplorerFragment.MODE_LISTING && fragment.getActivity() instanceof MainActivity)
        {
            UIUtils.setBackground(((View) vh.choose),
                    context.getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setTag(R.id.node_action, f);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    File item = (File) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(context, v);
                    getMenu(popup.getMenu(), item);

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

                    popup.setOnMenuItemClickListener(LibraryCursorAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(((View) vh.choose), null);
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
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SHARE, Menu.FIRST + MenuActionItem.MENU_SHARE, R.string.share);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                    R.string.upload);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (f.canWrite() && downloadPath != null && f.getPath().startsWith(downloadPath))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                    R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;
        switch (item.getItemId())
        {
            case MenuActionItem.MENU_UPLOAD:
                onMenuItemClick = true;
                ActionManager.actionUpload((Activity) context, selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_SHARE:
                onMenuItemClick = true;
                ActionManager.actionShareContent((Activity) context, selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_DELETE:
                onMenuItemClick = true;
                FileActions.delete(fragment, new ArrayList<File>(selectedOptionItems));
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }
}

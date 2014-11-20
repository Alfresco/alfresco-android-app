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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.fragments.utils.ProgressViewHolder;
import org.alfresco.mobile.android.application.managers.ActionUtils;
import org.alfresco.mobile.android.application.managers.RenditionManagerImpl;
import org.alfresco.mobile.android.platform.io.AlfrescoStorageManager;
import org.alfresco.mobile.android.platform.mimetype.MimeTypeManager;
import org.alfresco.mobile.android.platform.security.DataProtectionManager;
import org.alfresco.mobile.android.platform.utils.AccessibilityUtils;
import org.alfresco.mobile.android.platform.utils.AndroidVersion;
import org.alfresco.mobile.android.platform.utils.SessionUtils;
import org.alfresco.mobile.android.ui.ListingModeFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.Formatter;
import org.alfresco.mobile.android.ui.utils.UIUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
 * Provides access to files and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class FileExplorerAdapter extends BaseListAdapter<File, ProgressViewHolder> implements OnMenuItemClickListener
{
    private List<File> originalFiles;

    private List<File> selectedItems;

    private List<File> selectedOptionItems = new ArrayList<File>();

    private WeakReference<Fragment> fragmentRef;

    private HashMap<String, File> nodeNameIndexer = new HashMap<String, File>();

    private int mode = ListingModeFragment.MODE_LISTING;

    private String downloadPath;

    private RenditionManagerImpl renditionManager;

    public FileExplorerAdapter(Fragment fr, int textViewResourceId, List<File> listItems)
    {
        this(fr, textViewResourceId, ListingModeFragment.MODE_LISTING, listItems, null);
    }

    public FileExplorerAdapter(Fragment fr, int textViewResourceId, int mode, List<File> listItems,
            List<File> selectedItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragmentRef = new WeakReference<Fragment>(fr);
        this.selectedItems = selectedItems;
        this.originalFiles = listItems;
        this.mode = mode;
        if (((BaseActivity) fr.getActivity()).getCurrentAccount() != null)
        {
            File f = AlfrescoStorageManager.getInstance(fr.getActivity()).getDownloadFolder(
                    ((BaseActivity) fr.getActivity()).getCurrentAccount());
            this.downloadPath = (f != null) ? f.getPath() : null;
        }
        this.renditionManager = RenditionManagerImpl.getInstance(fr.getActivity());
        this.vhClassName = ProgressViewHolder.class.getCanonicalName();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    @Override
    protected void updateTopText(ProgressViewHolder vh, File item)
    {
        vh.topText.setText(item.getName());
    }

    @Override
    protected void updateBottomText(ProgressViewHolder vh, File item)
    {
        vh.bottomText.setText(createContentBottomText(getContext(), item));

        if (selectedItems != null && selectedItems.contains(item))
        {
            UIUtils.setBackground(((LinearLayout) vh.choose.getParent()),
                    getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
        }
        else
        {
            UIUtils.setBackground(((LinearLayout) vh.choose.getParent()), null);
        }

        if (DataProtectionManager.getInstance(getContext()).isEncrypted(item.getPath()))
        {
            vh.favoriteIcon.setVisibility(View.VISIBLE);
            vh.favoriteIcon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_encrypt));
        }
        else
        {
            vh.favoriteIcon.setVisibility(View.GONE);
        }
    }

    private String createContentBottomText(Context context, File file)
    {
        String s = "";
        s = formatDate(context, new Date(file.lastModified()));
        if (file.isFile())
        {
            s += " - " + Formatter.formatFileSize(context, file.length());
        }
        return s;
    }

    @Override
    protected void updateIcon(ProgressViewHolder vh, File item)
    {
        if (item.isFile())
        {
            Drawable drawable = getContext().getResources().getDrawable(
                    MimeTypeManager.getInstance(getContext()).getIcon(item.getName()));
            renditionManager.getPicasso().load(item).placeholder(drawable).error(drawable).into(vh.icon);
            AccessibilityUtils.addContentDescription(vh.icon, R.string.mime_document);
        }
        else if (item.isDirectory())
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_folder));
            AccessibilityUtils.addContentDescription(vh.icon, R.string.mime_folder);
        }

        if (mode == FileExplorerFragment.MODE_LISTING && fragmentRef.get().getActivity() instanceof MainActivity
                && ((downloadPath != null && item.getPath().startsWith(downloadPath)) || (item.isFile())))
        {
            UIUtils.setBackground(((View) vh.choose),
                    getContext().getResources().getDrawable(R.drawable.quickcontact_badge_overlay_light));

            vh.choose.setVisibility(View.VISIBLE);
            vh.choose.setTag(R.id.node_action, item);
            vh.choose.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    File item = (File) v.getTag(R.id.node_action);
                    selectedOptionItems.add(item);
                    PopupMenu popup = new PopupMenu(getContext(), v);
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

                    popup.setOnMenuItemClickListener(FileExplorerAdapter.this);

                    popup.show();
                }
            });
        }
        else
        {
            UIUtils.setBackground(((View) vh.choose), null);
            vh.choose.setVisibility(View.GONE);
        }
    }

    // /////////////////////////////////////////////////////////////
    // CRUD LIST
    // ////////////////////////////////////////////////////////////
    @Override
    public void clear()
    {
        nodeNameIndexer.clear();
        super.clear();
    }

    @Override
    public void add(File file)
    {
        if (file != null)
        {
            nodeNameIndexer.put(file.getPath(), file);
        }
        super.add(file);
    }

    @Override
    public void addAll(Collection<? extends File> collection)
    {
        File objects[] = (File[]) collection.toArray(new File[0]);

        int size = objects.length;
        for (int i = 0; i < size; i++)
        {
            add(objects[i]);
        }
    }

    public synchronized void replaceFile(File file)
    {
        if (nodeNameIndexer.containsKey(file.getPath()))
        {
            int position = getPosition(nodeNameIndexer.get(file.getPath()));
            originalFiles.remove(position);
            originalFiles.add(file);
        }
        else
        {
            originalFiles.add(file);
        }

        originalFiles.removeAll(Collections.singleton(null));
        Collections.sort(originalFiles, new FileComparator(true));

        List<File> tmpFile = new ArrayList<File>(originalFiles);
        clear();
        addAll(tmpFile);
    }

    public synchronized void remove(String nodeName)
    {
        if (nodeNameIndexer.containsKey(nodeName))
        {
            int position = getPosition(nodeNameIndexer.get(nodeName));
            originalFiles.remove(position);
            List<File> tmpNodes = new ArrayList<File>(originalFiles);
            clear();
            addAll(tmpNodes);
        }
    }

    public List<File> getNodes()
    {
        return originalFiles;
    }

    public boolean hasNode(String indexValue)
    {
        return nodeNameIndexer.containsKey(indexValue);
    }

    public File getNode(String indexValue)
    {
        int position = getPosition(nodeNameIndexer.get(indexValue));
        return originalFiles.get(position);
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
            mi = menu.add(Menu.NONE, R.id.menu_action_edit, Menu.FIRST + 50, R.string.action_rename);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            mi = menu.add(Menu.NONE, R.id.menu_action_delete, Menu.FIRST + 100, R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (DataProtectionManager.getInstance(getContext()).isEncryptionEnable())
        {
            if (DataProtectionManager.getInstance(getContext()).isEncrypted(f.getPath()))
            {
                mi = menu.add(Menu.NONE, R.id.menu_file_decrypt, Menu.FIRST + 70, R.string.decrypt_action);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
            else
            {
                mi = menu.add(Menu.NONE, R.id.menu_file_encrypt, Menu.FIRST + 70, R.string.encrypt_action);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
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
                ActionUtils.actionSendDocumentToAlfresco((Activity) getContext(), selectedOptionItems.get(0));
                break;
            case R.id.menu_action_share:
                onMenuItemClick = true;
                ActionUtils.actionShareContent((Activity) getContext(), selectedOptionItems.get(0));
                break;
            case R.id.menu_action_edit:
                onMenuItemClick = true;
                FileActions.edit(fragmentRef.get(), selectedOptionItems.get(0));
                break;
            case R.id.menu_action_delete:
                onMenuItemClick = true;
                FileActions.delete(fragmentRef.get(), new ArrayList<File>(selectedOptionItems));
                break;
            case R.id.menu_file_encrypt:
                onMenuItemClick = true;
                DataProtectionManager.getInstance(getContext()).checkEncrypt(
                        SessionUtils.getAccount(fragmentRef.get().getActivity()), selectedOptionItems.get(0));
                break;
            case R.id.menu_file_decrypt:
                onMenuItemClick = true;
                DataProtectionManager.getInstance(getContext()).checkDecrypt(
                        SessionUtils.getAccount(fragmentRef.get().getActivity()), selectedOptionItems.get(0));
                break;
            default:
                onMenuItemClick = false;
                break;
        }
        selectedOptionItems.clear();
        return onMenuItemClick;
    }

    public List<File> getFiles()
    {
        return originalFiles;
    }
}

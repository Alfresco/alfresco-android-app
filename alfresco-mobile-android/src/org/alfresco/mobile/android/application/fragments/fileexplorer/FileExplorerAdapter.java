/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.fragments.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.commons.utils.AndroidVersion;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.MimeTypeManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.utils.ProgressViewHolder;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

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

    private Fragment fragment;

    private HashMap<String, File> nodeNameIndexer = new HashMap<String, File>();

    private int mode = ListingModeFragment.MODE_LISTING;

    private String downloadPath;

    public FileExplorerAdapter(Fragment fr, int textViewResourceId, List<File> listItems)
    {
        this(fr, textViewResourceId, ListingModeFragment.MODE_LISTING, listItems, null);
    }

    public FileExplorerAdapter(Fragment fr, int textViewResourceId, int mode, List<File> listItems,
            List<File> selectedItems)
    {
        super(fr.getActivity(), textViewResourceId, listItems);
        this.fragment = fr;
        this.selectedItems = selectedItems;
        this.originalFiles = listItems;
        this.mode = mode;
        if (((BaseActivity) fr.getActivity()).getCurrentAccount() != null)
        {
            File f = StorageManager.getDownloadFolder(fr.getActivity(),
                    ((BaseActivity) fr.getActivity()).getCurrentAccount());
            this.downloadPath = (f != null) ? f.getPath() : null;
        }

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
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(MimeTypeManager.getIcon(item.getName())));
        }
        else if (item.isDirectory())
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_folder));
        }

        if (mode == FileExplorerFragment.MODE_LISTING && fragment.getActivity() instanceof MainActivity
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

            vh.icon.setTag("drag");
            ((View) vh.icon.getParent().getParent()).setOnDragListener(new myDragEventListener());
            vh.icon.setOnLongClickListener(new View.OnLongClickListener()
            {
                // Defines the one method for the interface, which is called when the View is long-clicked
                public boolean onLongClick(View v)
                {
                    // Create a new ClipData.Item from the ImageView object's tag
                    ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());

                    // Create a new ClipData using the tag as a label, the plain text MIME type, and
                    // the already-created item. This will create a new ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    ClipData dragData = new ClipData((CharSequence) v.getTag(), new String[] { "text/plain" }, item);

                    DragShadowBuilder drag = new View.DragShadowBuilder((View) v.getParent().getParent());
                    UIUtils.setBackground(drag.getView(), getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
                    
                    v.startDrag(dragData, // the data to be dragged
                            new View.DragShadowBuilder((View) v.getParent().getParent()), // the drag shadow builder
                            null, // no need to use local data
                            0 // flags (not currently used, set to 0)
                    );
                    return true;
                }
            });

            /*SMultiWindowDropListener dl = new SMultiWindowDropListener()
            {
                @Override
                public void onDrop(DragEvent event)
                {
                    MessengerManager.showToast(getContext(), "DROP!!");
                    ClipData clipData = event.getClipData();
                    if (clipData != null)
                    {
                        int count = clipData.getItemCount();
                        for (int index = 0; index < count; ++index)
                        {
                            ClipData.Item item = clipData.getItemAt(index);
                            if (item.getText() != null)
                            {
                                MessengerManager.showToast(getContext(), "TEXT");
                            } // Handle any text
                            if (item.getUri() != null)
                            {
                                MessengerManager.showToast(getContext(), "URI");
                            } // Handle any URIs
                            if (item.getIntent() != null)
                            {
                                MessengerManager.showToast(getContext(), "INTENT");
                            }
                            // Handle any intents
                        }
                    }
                }
            };
            ((View) vh.icon.getParent().getParent()).setOnDragListener(dl);*/

        }
        else
        {
            UIUtils.setBackground(((View) vh.choose), null);
        }
    }

    protected class myDragEventListener implements OnDragListener
    {

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event)
        {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            CharSequence dragData;
            // Handles each of the expected events
            switch (action)
            {

                case DragEvent.ACTION_DRAG_STARTED:

                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                    {

                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.
                        UIUtils.setBackground(v, getContext().getResources().getDrawable(R.drawable.bg_gradient));

                        // ((ImageView) v).setColorFilter(Color.BLUE);

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate();

                        // returns true to indicate that the View can accept the dragged data.
                        return (true);

                    }
                    else
                    {

                        // Returns false. During the current drag and drop operation, this View will
                        // not receive events again until ACTION_DRAG_ENDED is sent.
                        return (false);

                    }

                case DragEvent.ACTION_DRAG_ENTERED:

                    // Applies a green tint to the View. Return true; the return value is ignored.
                    UIUtils.setBackground(v, getContext().getResources().getDrawable(R.drawable.bg_gradient));

                    // ((ImageView) v).setColorFilter(Color.GREEN);

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return (true);

                case DragEvent.ACTION_DRAG_LOCATION:

                    // Ignore the event
                    return (true);

                case DragEvent.ACTION_DRAG_EXITED:

                    // Re-sets the color tint to blue. Returns true; the return value is ignored.
                    // UIUtils.setBackground(v, null);
                    UIUtils.setBackground(v, getContext().getResources().getDrawable(R.drawable.list_longpressed_holo));
                    // ((ImageView) v).setColorFilter(Color.BLUE);

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return (true);

                case DragEvent.ACTION_DROP:

                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    dragData = item.getText();

                    // Displays a message containing the dragged data.
                    Toast.makeText(getContext(), "Dragged data is " + dragData, Toast.LENGTH_LONG);

                    // Turns off any color tints
                    UIUtils.setBackground(v, null);
                    // ((ImageView) v).clearColorFilter();

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // Returns true. DragEvent.getResult() will return true.
                    return (true);

                case DragEvent.ACTION_DRAG_ENDED:

                    // Turns off any color tinting
                    UIUtils.setBackground(v, null);
                    // ((ImageView) v).clearColorFilter();

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // Does a getResult(), and displays what happened.
                    if (event.getResult())
                    {
                        Toast.makeText(getContext(), "The drop was handled.", Toast.LENGTH_LONG);

                    }
                    else
                    {
                        Toast.makeText(getContext(), "The drop didn't work.", Toast.LENGTH_LONG);

                    }
                    ;

                    // returns true; the value is ignored.
                    return (true);

                    // An unknown action type was received.
                default:
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");

                    break;
            }
            ;
            return false;
        };
    };

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
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_SHARE, Menu.FIRST + MenuActionItem.MENU_SHARE, R.string.share);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            mi = menu.add(Menu.NONE, MenuActionItem.MENU_UPLOAD, Menu.FIRST + MenuActionItem.MENU_UPLOAD,
                    R.string.upload);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (f.canWrite() && downloadPath != null && f.getPath().startsWith(downloadPath))
        {
            mi = menu.add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                    R.string.action_rename);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            mi = menu.add(Menu.NONE, MenuActionItem.MENU_DELETE, Menu.FIRST + MenuActionItem.MENU_DELETE,
                    R.string.delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (DataProtectionManager.getInstance(getContext()).isEncryptionEnable())
        {
            if (DataProtectionManager.getInstance(getContext()).isEncrypted(f.getPath()))
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_DECRYPT, Menu.FIRST + MenuActionItem.MENU_DECRYPT,
                        R.string.decrypt_action);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
            else
            {
                mi = menu.add(Menu.NONE, MenuActionItem.MENU_ENCRYPT, Menu.FIRST + MenuActionItem.MENU_ENCRYPT,
                        R.string.encrypt_action);
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
            case MenuActionItem.MENU_UPLOAD:
                onMenuItemClick = true;
                ActionManager.actionSendDocumentToAlfresco((Activity) getContext(), selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_SHARE:
                onMenuItemClick = true;
                ActionManager.actionShareContent((Activity) getContext(), selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_EDIT:
                onMenuItemClick = true;
                FileActions.edit(fragment, selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_DELETE:
                onMenuItemClick = true;
                FileActions.delete(fragment, new ArrayList<File>(selectedOptionItems));
                break;
            case MenuActionItem.MENU_ENCRYPT:
                onMenuItemClick = true;
                DataProtectionManager.getInstance(getContext()).checkEncrypt(SessionUtils.getAccount(fragment.getActivity()), selectedOptionItems.get(0));
                break;
            case MenuActionItem.MENU_DECRYPT:
                onMenuItemClick = true;
                DataProtectionManager.getInstance(getContext()).checkDecrypt(SessionUtils.getAccount(fragment.getActivity()), selectedOptionItems.get(0));
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

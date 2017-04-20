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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.application.extension.samsung.R;
import org.alfresco.mobile.android.application.extension.samsung.utils.SNoteUtils;
import org.alfresco.mobile.android.platform.AlfrescoNotificationManager;
import org.alfresco.mobile.android.platform.extensions.AnalyticsHelper;
import org.alfresco.mobile.android.platform.extensions.AnalyticsManager;
import org.alfresco.mobile.android.platform.intent.BaseActionUtils;
import org.alfresco.mobile.android.ui.activity.AlfrescoActivity;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingTextInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenFlickListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingSelectionLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingTextLayout;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SNoteEditorActivity extends AlfrescoActivity
{
    protected static final String TAG = SNoteEditorActivity.class.getName();

    private static final int SWIPE_MIN_DISTANCE = 350;

    private static final int SWIPE_THRESHOLD_VELOCITY = 700;

    private Context context;

    // DOCUMENT
    private SpenNoteDoc spenNoteDoc;

    private SpenPageDoc spenPageDoc;

    // SETTINGS
    private SpenSurfaceView spenSurfaceView;

    private SpenSettingPenLayout spenSettingView;

    private SpenSettingEraserLayout eraserSettingView;

    private SpenSettingTextLayout textSettingView;

    private SpenSettingSelectionLayout selectionSettingView;

    // UI
    private SubMenu toolsSubMenu;

    private SNoteEditorActionMode nActions;

    // FLAGS
    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    private boolean isDiscard = true;

    private TextView mTxtView;

    private File file;

    private GestureDetector gdt;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        AnalyticsHelper.reportScreen(this, AnalyticsManager.SCREEN_SAMSUNG_SNOTE_EDITOR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.snote_editor);
        context = this;

        // TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Retrieve information
        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equals(action))
        {
            if (getIntent().getData() != null)
            {
                String filePath = BaseActionUtils.getPath(this, getIntent().getData());
                file = new File(filePath);
            }
            else
            {
                AlfrescoNotificationManager.getInstance(this).showLongToast(getString(R.string.editor_error_open));
                finish();
                return;
            }
        }

        // Init Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try
        {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        }
        catch (SsdkUnsupportedException e)
        {
            if (SNoteUtils.processUnsupportedException(this, e)) { return; }
        }
        catch (Exception e1)
        {
            Log.e(TAG, Log.getStackTraceString(e1));
            finish();
        }

        FrameLayout spenViewContainer = (FrameLayout) findViewById(R.id.spenViewContainer);
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // PEN SETTINGS
        spenSettingView = new SpenSettingPenLayout(context, "", spenViewLayout);
        if (spenSettingView == null)
        {
            finish();
        }
        spenViewContainer.addView(spenSettingView);

        // ERASER SETTINGS
        eraserSettingView = new SpenSettingEraserLayout(context, "", spenViewLayout);
        if (eraserSettingView == null)
        {
            finish();
        }
        spenViewContainer.addView(eraserSettingView);

        // TEXT SETTINGS
        textSettingView = new SpenSettingTextLayout(context, "", new HashMap<String, String>(), spenViewLayout);
        if (textSettingView == null)
        {
            finish();
        }
        spenViewContainer.addView(textSettingView);

        // SELECTION SETTINGS
        selectionSettingView = new SpenSettingSelectionLayout(context, "", spenViewLayout);
        if (textSettingView == null)
        {
            finish();
        }
        spenViewContainer.addView(selectionSettingView);

        // SURFACE VIEW
        spenSurfaceView = new SpenSurfaceView(context);
        if (spenSurfaceView == null)
        {
            finish();
        }
        spenViewLayout.addView(spenSurfaceView);
        spenSettingView.setCanvasView(spenSurfaceView);
        eraserSettingView.setCanvasView(spenSurfaceView);
        textSettingView.setCanvasView(spenSurfaceView);
        selectionSettingView.setCanvasView(spenSurfaceView);

        // NOTE DOCUMENT
        Display display = getWindowManager().getDefaultDisplay();
        Rect mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        try
        {
            if (file != null && file.length() > 0)
            {
                spenNoteDoc = new SpenNoteDoc(context, file.getAbsolutePath(), mScreenRect.width(),
                        SpenNoteDoc.MODE_WRITABLE);
                if (spenNoteDoc.getPageCount() == 0)
                {
                    spenPageDoc = spenNoteDoc.appendPage();
                }
                else
                {
                    spenPageDoc = spenNoteDoc.getPage(spenNoteDoc.getLastEditedPageIndex());
                }
            }
            else
            {
                spenNoteDoc = new SpenNoteDoc(context, SpenNoteDoc.ORIENTATION_LANDSCAPE,
                        (mScreenRect.width() > mScreenRect.height()) ? mScreenRect.width() : mScreenRect.height(),
                        (mScreenRect.width() < mScreenRect.height()) ? mScreenRect.width() : mScreenRect.height());
                spenPageDoc = spenNoteDoc.appendPage();
                spenPageDoc.setBackgroundColor(getResources().getColor(android.R.color.white));
                spenPageDoc.clearHistory();
            }
        }
        catch (Exception e)
        {
            finish();
        }

        // Display Document
        spenSurfaceView.setPageDoc(spenPageDoc, true);
        spenSurfaceView.setBlankColor(getResources().getColor(R.color.alfresco_dbp_gray));

        if (!isSpenFeatureEnabled)
        {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            spenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);

            // Touch listener for swipe if on Finger mode
            gdt = new GestureDetector(context, new GestureListener());
            spenSurfaceView.setOnTouchListener(touchListener);
        }

        // Init Pages
        mTxtView = (TextView) findViewById(R.id.spen_page);
        mTxtView.setText(String.format(getString(R.string.editor_paging),
                String.valueOf((spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1)), spenNoteDoc.getPageCount()));

        // INIT Setting & Listeners
        initSettingInfo();
        spenSurfaceView.setTouchListener(penTouchListener);
        spenSurfaceView.setControlListener(controlListener);
        spenSurfaceView.setFlickListener(mFlickListener);

    }

    @Override
    protected void onStart()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        destroySettingView();

        if (spenSurfaceView != null)
        {
            spenSurfaceView.closeControl();
            spenSurfaceView.close();
            spenSurfaceView = null;
        }

        if (spenNoteDoc != null)
        {
            try
            {
                if (isDiscard)
                {
                    spenNoteDoc.discard();
                }
                else
                {
                    spenNoteDoc.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            spenNoteDoc = null;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (spenNoteDoc != null && spenNoteDoc.isChanged())
        {
            // Request to save before quit
            requestSave();
        }
        else
        {
            super.onBackPressed();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // OPTIONS
    // ///////////////////////////////////////////////////////////////////////////
    private void initSettingInfo()
    {
        // Pen setting
        List<SpenPenInfo> penList = new ArrayList<SpenPenInfo>();
        SpenPenManager penManager = new SpenPenManager(context);
        penList = penManager.getPenInfoList();
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        for (SpenPenInfo info : penList)
        {
            if (info.name.equalsIgnoreCase("InkPen"))
            {
                penInfo.name = info.className;
                break;
            }
        }
        penInfo.color = Color.BLACK;
        penInfo.size = 1;
        spenSurfaceView.setPenSettingInfo(penInfo);
        spenSettingView.setInfo(penInfo);

        // Eraser setting
        SpenSettingEraserInfo eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 25;
        spenSurfaceView.setEraserSettingInfo(eraserInfo);
        eraserSettingView.setInfo(eraserInfo);

        // Text setting
        SpenSettingTextInfo textInfo = new SpenSettingTextInfo();
        textInfo.size = 20;
        spenSurfaceView.setTextSettingInfo(textInfo);
        textSettingView.setInfo(textInfo);
    }

    public void requestSave()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editor_save_request);
        builder.setCancelable(false);
        builder.setMessage(Html.fromHtml(getString(R.string.editor_save_description)));
        builder.setIcon(R.drawable.ic_save);
        builder.setPositiveButton(R.string.editor_save, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                save(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.editor_discard, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        TextView messageText = (TextView) alert.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
    }

    private void save(boolean stopActivity)
    {
        try
        {
            spenNoteDoc.save(file.getAbsolutePath());
            AlfrescoNotificationManager.getInstance(this).showToast(R.string.editor_save_confirmation);

            if (stopActivity)
            {
                this.finish();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LISTENERS - EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    private SpenTouchListener penTouchListener = new SpenTouchListener()
    {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getToolType(0) == mToolType)
            {
                SpenControlBase control = spenSurfaceView.getControl();
                if (control == null)
                {
                    if (spenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT)
                    {
                        SpenObjectTextBox obj = addTextObject(event.getX(), event.getY(), null);
                        spenPageDoc.selectObject(obj);
                        spenSurfaceView.update();
                        return true;
                    }
                }
            }
            return false;
        }
    };

    private SpenControlListener controlListener = new SpenControlListener()
    {
        @Override
        public void onRotationChanged(float arg0, SpenObjectBase arg1)
        {
        }

        @Override
        public void onRectChanged(RectF arg0, SpenObjectBase arg1)
        {
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> arg0)
        {
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> objectList, int itemId)
        {
            return false;
        }

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList, ArrayList<Rect> relativeRectList,
                ArrayList<SpenContextMenuItemInfo> menu, ArrayList<Integer> styleList, int pressType, PointF point)
        {
            nActions = new SNoteEditorActionMode(SNoteEditorActivity.this, spenPageDoc, spenSurfaceView, objectList);
            SNoteEditorActivity.this.startActionMode(nActions);
            return true;
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> arg0)
        {
            if (nActions != null)
            {
                nActions.finish();
            }
            return true;
        }
    };

    private OnTouchListener touchListener = new OnTouchListener()
    {

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            gdt.onTouchEvent(event);
            return false;
        }
    };

    private class GestureListener extends SimpleOnGestureListener
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            if (spenNoteDoc != null && spenNoteDoc.getPageCount() == 1) { return false; }
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                swipePage(SpenFlickListener.DIRECTION_RIGHT);
                return true; // Right to left
            }
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                swipePage(SpenFlickListener.DIRECTION_LEFT);
                return true; // Left to right
            }
            return false;
        }
    }

    private SpenFlickListener mFlickListener = new SpenFlickListener()
    {

        @Override
        public boolean onFlick(int direction)
        {
            return swipePage(direction);
        }
    };

    private boolean swipePage(int direction)
    {
        int pageIndex = spenNoteDoc.getPageIndexById(spenPageDoc.getId());
        int pageCount = spenNoteDoc.getPageCount();
        if (pageCount > 1)
        {
            if (direction == SpenFlickListener.DIRECTION_LEFT)
            {
                spenPageDoc = spenNoteDoc.getPage((pageIndex + pageCount - 1) % pageCount);
                spenSurfaceView.setPageDoc(spenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_LEFT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
            }
            else if (direction == SpenFlickListener.DIRECTION_RIGHT)
            {
                spenPageDoc = spenNoteDoc.getPage((pageIndex + 1) % pageCount);
                spenSurfaceView.setPageDoc(spenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
            }
            mTxtView = (TextView) findViewById(R.id.spen_page);
            mTxtView.setText(String.format(getString(R.string.editor_paging),
                    String.valueOf((spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1)), spenNoteDoc.getPageCount()));
            return true;
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION
    // ///////////////////////////////////////////////////////////////////////////
    private final int REQUEST_CODE_ATTACH_IMAGE = 6510;

    private DisplayMetrics dm;

    private static final String IMAGE_KEY = "IMAGE_";

    private void requestPickImage()
    {
        // gallery에서 image를 가져온다.
        try
        {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, REQUEST_CODE_ATTACH_IMAGE);
        }
        catch (ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (data == null) { return; }

            if (requestCode == REQUEST_CODE_ATTACH_IMAGE)
            {
                try
                {
                    String imagePath = BaseActionUtils.getPath(context, data.getData());
                    String attachmentId = IMAGE_KEY + spenNoteDoc.getAttachedFileCount();
                    spenNoteDoc.attachFile(attachmentId, imagePath);

                    spenSurfaceView.setZoom(0, 0, 1);
                    spenSurfaceView.update();
                    float zoomRatio = spenSurfaceView.getZoomRatio();
                    float panX = spenSurfaceView.getPan().x;
                    float panY = spenSurfaceView.getPan().y;
                    float x = (zoomRatio < 1) ? (spenSurfaceView.getCanvasWidth() / 2) * zoomRatio : (spenSurfaceView
                            .getCanvasWidth() / 2) * zoomRatio - (panX / 2);
                    float y = (zoomRatio < 1) ? (spenSurfaceView.getCanvasHeight() / 2) * zoomRatio : (spenSurfaceView
                            .getCanvasHeight() / 2) * zoomRatio - (panY / 2);

                    addImgObject(attachmentId, x, y);
                }
                catch (Exception e)
                {
                    // DO NOTHING
                }
            }
        }
    }

    private static int calculateInSampleSize(float width, float height, float reqWidth, float reqHeight)
    {
        // Raw height and width of image
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int heightRatio = Math.round(height / reqHeight);
            final int widthRatio = Math.round(width / reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI UTILS
    // ///////////////////////////////////////////////////////////////////////////
    private RectF getRealPoint(float x, float y, float width, float height)
    {
        float panX = spenSurfaceView.getPan().x;
        float panY = spenSurfaceView.getPan().y;
        float zoom = spenSurfaceView.getZoomRatio();
        width *= zoom;
        height *= zoom;
        RectF realRect = new RectF();
        realRect.set((x - width / 2) / zoom + panX, (y - height / 2) / zoom + panY, (x + width / 2) / zoom + panX,
                (y + height / 2) / zoom + panY);
        return realRect;
    }

    private SpenObjectTextBox addTextObject(float x, float y, String str)
    {
        SpenObjectTextBox textObj = new SpenObjectTextBox();
        RectF rect = getRealPoint(x, y, 0, 0);
        rect.right += 200;
        rect.bottom += 150;
        textObj.setRect(rect, true);
        textObj.setText(str);
        spenPageDoc.appendObject(textObj);
        spenSurfaceView.update();

        return textObj;
    }

    private void addImgObject(String attachmentKey, float x, float y)
    {
        SpenObjectImage imgObj = new SpenObjectImage();
        Bitmap imageBitmap;
        if (spenNoteDoc.hasAttachedFile(attachmentKey))
        {
            if (dm == null)
            {
                dm = new DisplayMetrics();
                ((FragmentActivity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            }
            imageBitmap = SNoteUtils.decodeFile(new File(spenNoteDoc.getAttachedFile(attachmentKey)),
                    spenSurfaceView.getCanvasWidth(), dm.densityDpi);
        }
        else
        {
            imageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.mime_img);
        }
        imgObj.setImage(imageBitmap);

        float imgWidth = imageBitmap.getWidth();
        float imgHeight = imageBitmap.getHeight();

        int scale = calculateInSampleSize(imgWidth, imgHeight, spenSurfaceView.getCanvasWidth(),
                spenSurfaceView.getCanvasHeight());

        RectF rect = getRealPoint(x, y, imgWidth / scale, imgHeight / scale);
        imgObj.setRect(rect, true);
        spenPageDoc.appendObject(imgObj);
        spenPageDoc.selectObject(imgObj);
        spenSurfaceView.update();

        imageBitmap.recycle();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        toolsSubMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_TOOLS, Menu.FIRST,
                R.string.editor_tools);
        toolsSubMenu.setIcon(R.drawable.ic_edit);
        toolsSubMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // TOOLS MENU
        MenuItem mi = toolsSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_PEN, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_PEN, R.string.editor_pen);
        mi.setIcon(R.drawable.ic_edit);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_TEXT, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_TEXT, R.string.editor_text);
        mi.setIcon(R.drawable.ic_text);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SELECTION, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SELECTION, R.string.editor_selection);
        mi.setIcon(R.drawable.ic_selection);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_ERASER, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_ERASER, R.string.editor_eraser);
        mi.setIcon(R.drawable.ic_eraser);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // TOOL SETTINGS MENU
        SubMenu toolsSettingSubMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SETTINGS, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS, R.string.editor_tools_settings);
        toolsSettingSubMenu.setIcon(R.drawable.ic_action_settings);
        toolsSettingSubMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mi = toolsSettingSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SETTINGS_PEN, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS_PEN, R.string.editor_pen);
        mi.setIcon(R.drawable.ic_edit);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSettingSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SETTINGS_TEXT, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS_TEXT, R.string.editor_text);
        mi.setIcon(R.drawable.ic_text);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSettingSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SETTINGS_SELECTION, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS_SELECTION, R.string.editor_selection);
        mi.setIcon(R.drawable.ic_selection);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = toolsSettingSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SETTINGS_ERASER, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SETTINGS_ERASER, R.string.editor_eraser);
        mi.setIcon(R.drawable.ic_eraser);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // ADD MENU
        SubMenu addSubMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_ADD, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_ADD, R.string.editor_add_menu);
        addSubMenu.setIcon(R.drawable.ic_add);
        addSubMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // IMAGE
        mi = addSubMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_ADD_IMAGE, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_ADD_IMAGE, R.string.editor_add_image);
        mi.setIcon(R.drawable.ic_add_image);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // PAGES
        SubMenu pagesMenu = menu.addSubMenu(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_PAGE, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_PAGE, R.string.editor_pages);
        pagesMenu.setIcon(R.drawable.ic_pages);
        pagesMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mi = pagesMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_PAGE_MOVE, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_PAGE_MOVE, R.string.editor_pages_move);
        mi.setIcon(R.drawable.ic_pages);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = pagesMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_PAGE_ADD, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_PAGE_ADD, R.string.editor_pages_add);
        mi.setIcon(R.drawable.ic_add_page);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = pagesMenu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_PAGE_DELETE, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_PAGE_DELETE, R.string.editor_pages_remove);
        mi.setIcon(R.drawable.ic_delete);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // EXTRA SETTINGS
        // SAVE
        mi = menu.add(Menu.NONE, SNoteMenuActionItem.MENU_EDITOR_SAVE, Menu.FIRST
                + SNoteMenuActionItem.MENU_EDITOR_SAVE, R.string.editor_save);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case SNoteMenuActionItem.MENU_EDITOR_PEN:
                spenSurfaceView.closeControl();
                closeSettingView();
                if (spenSurfaceView.getToolTypeAction(mToolType) != SpenSurfaceView.ACTION_STROKE)
                {
                    spenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
                }
                toolsSubMenu.getItem().setIcon(R.drawable.ic_edit);
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_TEXT:
                spenSurfaceView.closeControl();
                closeSettingView();
                if (spenSurfaceView.getToolTypeAction(mToolType) != SpenSurfaceView.ACTION_TEXT)
                {
                    spenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_TEXT);
                }
                toolsSubMenu.getItem().setIcon(R.drawable.ic_text);
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SELECTION:
                spenSurfaceView.closeControl();
                closeSettingView();
                if (spenSurfaceView.getToolTypeAction(mToolType) != SpenSurfaceView.ACTION_SELECTION)
                {
                    spenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_SELECTION);
                }
                toolsSubMenu.getItem().setIcon(R.drawable.ic_selection);
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_ERASER:
                spenSurfaceView.closeControl();
                closeSettingView();
                if (spenSurfaceView.getToolTypeAction(mToolType) != SpenSurfaceView.ACTION_ERASER)
                {
                    spenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_ERASER);
                }
                toolsSubMenu.getItem().setIcon(R.drawable.ic_eraser);
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SETTINGS_PEN:
                closeSettingView();
                if (spenSettingView.isShown())
                {
                    spenSettingView.setVisibility(View.GONE);
                }
                else
                {
                    spenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                    spenSettingView.setVisibility(View.VISIBLE);
                }
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SETTINGS_ERASER:
                closeSettingView();
                if (eraserSettingView.isShown())
                {
                    eraserSettingView.setVisibility(View.GONE);
                }
                else
                {
                    eraserSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                    eraserSettingView.setVisibility(View.VISIBLE);
                }
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SETTINGS_TEXT:
                closeSettingView();
                if (textSettingView.isShown())
                {
                    textSettingView.setVisibility(View.GONE);
                }
                else
                {
                    textSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                    textSettingView.setVisibility(View.VISIBLE);
                }
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SETTINGS_SELECTION:
                closeSettingView();
                if (selectionSettingView.isShown())
                {
                    selectionSettingView.setVisibility(View.GONE);
                }
                else
                {
                    selectionSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_NORMAL);
                    selectionSettingView.setVisibility(View.VISIBLE);
                }
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_PAGE_MOVE:
                SNotePagesDialogFragment df = new SNotePagesDialogFragment();
                df.setInfo(spenNoteDoc.getPageIndexById(spenPageDoc.getId()), spenNoteDoc.getPageCount());
                df.show(getSupportFragmentManager(), SNotePagesDialogFragment.TAG);
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_PAGE_ADD:
                spenSurfaceView.closeControl();
                closeSettingView();
                spenPageDoc = spenNoteDoc.insertPage(spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1);
                spenPageDoc.clearHistory();
                spenSurfaceView.setPageDoc(spenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
                mTxtView = (TextView) findViewById(R.id.spen_page);
                mTxtView.setText(String.format(getString(R.string.editor_paging),
                        String.valueOf((spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1)),
                        spenNoteDoc.getPageCount()));
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_PAGE_DELETE:
                spenSurfaceView.closeControl();
                closeSettingView();
                int pageIndex = spenNoteDoc.getPageIndexById(spenPageDoc.getId());
                spenNoteDoc.removePage(pageIndex);
                spenPageDoc.clearHistory();
                int pageCount = spenNoteDoc.getPageCount();
                if (pageCount == 0)
                {
                    spenPageDoc = spenNoteDoc.appendPage();
                }
                else
                {
                    spenPageDoc = spenNoteDoc.getPage((pageIndex + pageCount - 1) % pageCount);
                }
                spenSurfaceView.setPageDoc(spenPageDoc, true);

                mTxtView = (TextView) findViewById(R.id.spen_page);
                mTxtView.setText(String.format(getString(R.string.editor_paging),
                        String.valueOf((spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1)),
                        spenNoteDoc.getPageCount()));
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_ADD_IMAGE:
                requestPickImage();
                return true;
            case SNoteMenuActionItem.MENU_EDITOR_SAVE:
                save(false);
                return true;
            case android.R.id.home:
                if (spenPageDoc.getObjectCount(true) > 0 && spenPageDoc.isChanged())
                {
                    requestSave();
                }
                else
                {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI Cleaner
    // ///////////////////////////////////////////////////////////////////////////
    private void closeSettingView()
    {
        spenSettingView.setVisibility(SpenSurfaceView.GONE);
        eraserSettingView.setVisibility(SpenSurfaceView.GONE);
        textSettingView.setVisibility(SpenSurfaceView.GONE);
        selectionSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    private void destroySettingView()
    {
        if (spenSettingView != null)
        {
            spenSettingView.close();
        }

        if (eraserSettingView != null)
        {
            eraserSettingView.close();
        }

        if (textSettingView != null)
        {
            textSettingView.close();
        }

        if (selectionSettingView != null)
        {
            selectionSettingView.close();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHOD
    // ///////////////////////////////////////////////////////////////////////////
    public void movePage(int pageIndex)
    {
        spenPageDoc = spenNoteDoc.getPage(pageIndex);
        spenPageDoc.clearHistory();
        spenSurfaceView.setPageDoc(spenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
        mTxtView = (TextView) findViewById(R.id.spen_page);
        mTxtView.setText(String.format(getString(R.string.editor_paging),
                String.valueOf((spenNoteDoc.getPageIndexById(spenPageDoc.getId()) + 1)), spenNoteDoc.getPageCount()));
    }
}

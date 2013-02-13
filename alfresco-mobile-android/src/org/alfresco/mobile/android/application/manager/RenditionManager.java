/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.mobile.android.application.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.services.impl.AbstractPersonService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache.Editor;
import org.alfresco.mobile.android.ui.utils.thirdparty.DiskLruCache.Snapshot;
import org.alfresco.mobile.android.ui.utils.thirdparty.LruCache;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Utility class for downloading content and display it.
 * 
 * @author jpascal
 */
public class RenditionManager
{

    private static final String TAG = "RenditionManager";

    private Activity context;

    private AlfrescoSession session;

    private DisplayMetrics dm;

    private LruCache<String, Bitmap> mMemoryCache;

    private DiskLruCache mDiskCache;

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    private static final String DISK_CACHE_SUBDIR = "renditions";

    public static final int TYPE_NODE = 0;

    public static final int TYPE_PERSON = 1;

    public RenditionManager(Activity context, AlfrescoSession session)
    {
        this.context = context;
        this.session = session;

        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/10th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 10;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };

        try
        {
            File cacheDir = StorageManager.getCacheDir(context, DISK_CACHE_SUBDIR);
            mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
            mDiskCache.delete();
            mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
        }
        catch (IOException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (key == null || bitmap == null) { return; }
        String hashKey = StorageManager.md5(key);
        if (getBitmapFromMemCache(hashKey) == null)
        {
            mMemoryCache.put(hashKey, bitmap);
            Log.d(TAG, "Add MemoryCache : " + key);
        }
    }

    public void addBitmapToDiskMemoryCache(String key, ContentStream cf)
    {
        if (key == null || key.isEmpty()) { return; }
        String hashKey = StorageManager.md5(key);
        try
        {
            if (mDiskCache != null && mDiskCache.get(hashKey) == null)
            {
                Editor editor = mDiskCache.edit(hashKey);

                IOUtils.copyStream(cf.getInputStream(), editor.newOutputStream(0));
                editor.commit();
            }
            Log.d(TAG, "Add DiskCache : " + key);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    public Bitmap getBitmapFromMemCache(String key)
    {
        if (key == null || key.isEmpty()) { return null; }
        String hashKey = StorageManager.md5(key);
        return mMemoryCache.get(hashKey);
    }

    public Bitmap getBitmapFromDiskCache(String key)
    {
        return getBitmapFromDiskCache(key, null);
    }

    public Bitmap getBitmapFromDiskCache(String key, Integer preview)
    {
        if (key == null || key.isEmpty()) { return null; }
        String hashKey = StorageManager.md5(key);
        Snapshot snapshot = null;
        try
        {
            snapshot = mDiskCache.get(hashKey);
            if (snapshot != null)
            {
                Log.d(TAG, "GET DiskCache : " + key);
                if (preview != null)
                {
                    return decodeStream(mDiskCache, hashKey, preview, dm);
                }
                else
                {
                    return decodeStream(snapshot.getInputStream(0), dm);
                }
            }
        }
        catch (IOException e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    /**
     * Display the content of the url inside an imageview. (thumbnails)
     * 
     * @param iv
     * @param url
     * @param initDrawableId
     */
    public void display(ImageView iv, Node n, int initDrawableId)
    {
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, null);
    }

    public void display(ImageView iv, int initDrawableId, String identifier)
    {
        display(iv, identifier, initDrawableId, TYPE_NODE, null);
    }

    public void display(ImageView iv, String username, int initDrawableId)
    {
        display(iv, username, initDrawableId, TYPE_PERSON, null);
    }

    public void preview(ImageView iv, Node n, int initDrawableId, Integer size)
    {
        display(iv, n.getIdentifier(), initDrawableId, TYPE_NODE, size);
    }

    public void preview(ImageView iv, int initDrawableId, String identifier)
    {
        display(iv, identifier, initDrawableId, TYPE_NODE, null);
    }

    private void display(ImageView iv, String identifier, int initDrawableId, int type, Integer preview)
    {
        final String imageKey = identifier;
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null)
        {
            iv.setImageBitmap(bitmap);
            Log.d(TAG, "Cache : " + identifier);
        }
        else if (cancelPotentialWork(identifier, iv))
        {
            final BitmapWorkerTask task = new BitmapWorkerTask(session, iv, identifier, type, preview);
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), initDrawableId);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), bm, task);
            iv.setImageDrawable(asyncDrawable);
            task.execute();
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }

        Log.d(TAG, "height:" + height + "width" + width);

        return inSampleSize;
    }

    public static Bitmap decodeFile(File f, int requiredSize, int dpiClassification)
    {
        InputStream fis = null;
        Bitmap bmp = null;
        try
        {
            fis = new BufferedInputStream(new FileInputStream(f));
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            // Find the correct scale value. It should be the power of 2.
            int scale = calculateInSampleSize(o, requiredSize, requiredSize);
            // Log.d(TAG, "Scale:" + scale + "Px" +requiredSizePx + "DPI" +
            // dpiClassification);

            // decode with inSampleSize
            fis = new BufferedInputStream(new FileInputStream(f));
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dpiClassification;
            o.inJustDecodeBounds = false;
            o.inPurgeable = true;
            bmp = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            IOUtils.closeStream(fis);
        }
        return bmp;
    }

    public static Bitmap decodeStream(DiskLruCache mDiskCache, String snap, int requiredSize, DisplayMetrics dm)
    {
        InputStream fis = null;
        Bitmap bmp = null;
        try
        {
            fis = new BufferedInputStream(mDiskCache.get(snap).getInputStream(0));
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            // Find the correct scale value. It should be the power of 2.
            int requiredSizePx = getDPI(dm, requiredSize);
            int scale = calculateInSampleSize(o, requiredSizePx, requiredSizePx);
            Log.d(TAG, "Scale:" + scale + " Px" + requiredSizePx + " Dpi" + dm.densityDpi);

            // decode with inSampleSize
            fis = new BufferedInputStream(mDiskCache.get(snap).getInputStream(0));
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dm.densityDpi;
            o.inJustDecodeBounds = false;
            o.inPurgeable = true;
            bmp = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            IOUtils.closeStream(fis);
        }
        return bmp;
    }

    public static Bitmap decodeStream(InputStream is, DisplayMetrics dm)
    {
        if (is == null) { return null; }
        try
        {
            BufferedInputStream bis = new BufferedInputStream(is);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inDither = false;
            o.inScaled = false;
            o.inPreferredConfig = Bitmap.Config.ARGB_8888;
            o.inTargetDensity = dm.densityDpi;
            o.inPurgeable = true;
            return BitmapFactory.decodeStream(bis, null, o);
        }
        catch (Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        finally
        {
            IOUtils.closeStream(is);
        }
        return null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    public class BitmapWorkerTask extends AsyncTask<Void, Integer, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;

        private String identifier;

        private AlfrescoSession session;

        private String username;

        private Integer preview;

        public BitmapWorkerTask(AlfrescoSession session, ImageView imageView, String identifier, int type)
        {
            this(session, imageView, identifier, type, null);
        }

        public BitmapWorkerTask(AlfrescoSession session, ImageView imageView, String identifier, int type,
                Integer preview)
        {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.session = session;

            if (type == TYPE_NODE)
            {
                this.identifier = identifier;
            }
            else if (type == TYPE_PERSON)
            {
                this.username = identifier;
            }
            this.preview = preview;
        }

        private String getId()
        {
            if (identifier != null)
            {
                return identifier;
            }
            else if (username != null) { return username; }
            return null;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params)
        {
            if (session == null) { return null; }

            Bitmap bm = null;
            ContentStream cf = null;
            String key = getId();

            if (mDiskCache != null)
            {
                bm = getBitmapFromDiskCache(key);
            }

            if (bm == null)
            {
                if (identifier != null)
                {
                    try
                    {
                        String renditionId = DocumentFolderService.RENDITION_THUMBNAIL;
                        if (preview != null)
                        {
                            renditionId = DocumentFolderService.RENDITION_PREVIEW;
                        }

                        cf = ((AbstractDocumentFolderServiceImpl) session.getServiceRegistry()
                                .getDocumentFolderService()).getRenditionStream(identifier, renditionId);
                    }
                    catch (AlfrescoServiceException e)
                    {
                        cf = null;
                    }
                }
                else if (username != null)
                {
                    try
                    {
                        cf = ((AbstractPersonService) session.getServiceRegistry().getPersonService())
                                .getAvatarStream(username);
                        key = username;
                    }
                    catch (AlfrescoServiceException e)
                    {
                        cf = null;
                    }
                }
                if (cf != null && cf.getInputStream() != null)
                {
                    if (mDiskCache != null)
                    {
                        addBitmapToDiskMemoryCache(key, cf);
                        bm = getBitmapFromDiskCache(key, preview);
                    }
                    else
                    {
                        bm = decodeStream(cf.getInputStream(), dm);
                    }
                }
            }

            addBitmapToMemoryCache(key, bm);
            return bm;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if (isCancelled())
            {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null)
            {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null)
                {
                    imageView.setImageBitmap(bitmap);

                    // We create preview with a shadow effect around the image.
                    if (preview != null)
                    {
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setBackgroundResource(org.alfresco.mobile.android.ui.R.drawable.shadow_picture);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                        params.width = bitmap.getWidth();
                        params.height = bitmap.getHeight();
                        imageView.setLayoutParams(params);
                        Log.d(TAG, "W:" + bitmap.getWidth() + " H:" + bitmap.getHeight());
                    }
                }
            }
            else if (preview != null && bitmap == null)
            {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null && ((ViewGroup) imageView.getParent()).findViewById(R.id.preview_message) != null)
                {
                    ((ViewGroup) imageView.getParent()).findViewById(R.id.preview_message).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static int getDPI(DisplayMetrics dm, int sizeInDp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, dm);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    public static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask)
        {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask()
        {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView)
    {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null)
        {
            final String bitmapData = bitmapWorkerTask.username;
            if (bitmapData != null && !bitmapData.equals(data))
            {
                bitmapWorkerTask.cancel(true);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView)
    {
        if (imageView != null)
        {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable)
            {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}

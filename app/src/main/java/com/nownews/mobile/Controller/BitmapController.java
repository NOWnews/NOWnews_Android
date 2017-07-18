
package com.nownews.mobile.Controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nownews.R;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Widget.CustomImageTopcrop;
import com.nownews.mobile.Widget.ZoomImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class BitmapController {

    public final static int IMAGE_SRC = 0;
    public final static int IMAGE_SRC_FROM_NEWS_LIST = 1;
    public final static int IMAGE_SRC_FROM_NEWS_PAGE = 2;
    public final static int IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE = 3;
    private final static String TAG = "BitmapController";
    private static BitmapController mBitmapControllerInstance;
    private final int TYPE_LOAD = 0x568;
    private final int TYPE_PRELOAD = 0x987;
    private Context mContext;
    private LruCache<String, Bitmap> mImageCache;
    private ReSizeLayoutParams mResizeLayoutParams;
    private String KEY_IMG_W = "imgW";
    private String KEY_IMG_H = "imgH";
    private HashMap<String, HashMap<String, Integer>> mImageSizeInfo;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean useGlide = true;

    public BitmapController(Context aContext) {
        mContext = aContext;
        mResizeLayoutParams = new ReSizeLayoutParams(mContext);
        processScreenSize();
        initImageCache();
    }

    private void initImageCache(){
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.i(TAG, "maxMemory: " + maxMemory);
        int cacheSize = maxMemory / 8;
        Log.i(TAG, "cacheSize: " + cacheSize);
        mImageCache = new LruCache<String, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(mImageCache.get(key)==null){
            mImageCache.put(key, bitmap);
        }
    }

    public static BitmapController getInstance(Context aContext) {
        if (mBitmapControllerInstance == null) {
            mBitmapControllerInstance = new BitmapController(aContext.getApplicationContext());
        } else {
            if(aContext!=null){
                mBitmapControllerInstance.mContext = aContext.getApplicationContext();
            }
        }
//        Glide.get(mBitmapControllerInstance.mContext).clearMemory();
        return mBitmapControllerInstance;
    }

    public void unregistBitmapController(Context aContext){
        if(mBitmapControllerInstance!=null && mBitmapControllerInstance.mContext!=null
                && mBitmapControllerInstance.mContext==aContext){
            mBitmapControllerInstance.mContext = null;
        }
    }

    public void loadImageWithOriginalSize(String aUrl, final View aView, int aImageType, int aImgW, int aImgH, ImageLoadingListener aImageLoadingListener) {

        if (Utility.DEBUG) Log.e(TAG, "***** loadImageWithOriginalSize() *****");

        if (aUrl == null) {
            setBitmapAsBackgroundOrImage(aView, null, aUrl, aImageType, TYPE_LOAD, aImageLoadingListener, aImgW, aImgH);
            return;
        }

        if (Utility.DEBUG) Log.e(TAG, "aUrl: " + aUrl);

        if(useGlide){
            loadImageWithGlide(aUrl, aView, aImageType, aImgW, aImgH, aImageLoadingListener, TYPE_LOAD);
            return;
        }

    }

    public void setDefaultImage(ImageView aView){
        Glide.with(mContext)
                .load(R.drawable.default_img)
                .into(aView);
    }

    public void loadImageWithGlide(final String aUrl, final View aView, final int aImageType, final int aImgW,
                                    final int aImgH, final ImageLoadingListener aImageLoadingListener, final int aImageLoadType){
        Glide.with(mContext)
                .setDefaultRequestOptions(new RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .error(R.drawable.default_img)
                        .skipMemoryCache(true)
                        .centerCrop())
                .asBitmap()
                .load(aUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                        if (mImageSizeInfo == null) {
                            mImageSizeInfo = new HashMap<String, HashMap<String, Integer>>();
                        }
                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put(KEY_IMG_W, resource.getWidth());
                        map.put(KEY_IMG_H, resource.getHeight());
                        mImageSizeInfo.put(aUrl, map);
                        Log.d(TAG, "resource.getWidth(): " + resource.getWidth());
                        Log.d(TAG, "resource.getHeight(): " + resource.getHeight());
//
//                        addBitmapToMemoryCache(aUrl, resource);

                        setBitmapAsBackgroundOrImage(aView, resource, aUrl, aImageType, aImageLoadType, aImageLoadingListener, aImgW, aImgH);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        if (aImageLoadingListener != null) {
                            aImageLoadingListener.onLoadingFailed(aUrl, aView, null);
                        }
                        if (Utility.DEBUG) Log.e(TAG, "aView is null or not??: " + (aView==null? "true":"false"));
                        if (Utility.DEBUG) Log.e(TAG, "errorDrawable is null or not??: " + (errorDrawable==null? "true":"false"));
                        if(aView!=null && errorDrawable!=null){
                            if(aView instanceof CustomImageTopcrop){
                                ((CustomImageTopcrop)aView).setCenterCrop();
                                ((CustomImageTopcrop)aView).setImageDrawable(errorDrawable);
                            }else{
                                ((ImageView)aView).setImageDrawable(errorDrawable);
                            }
                        }else if(aView!=null && errorDrawable==null){
                            ((ImageView)aView).setImageResource(R.drawable.default_img);
                        }

                        super.onLoadFailed(errorDrawable);
                    }
                });
    }

    public void preloadOriginalImageFromUrl(String aUrl, View aView, int aImageType, int aImgW, int aImgH, ImageLoadingListener aImageLoadingListener) {

        if (Utility.DEBUG) Log.e(TAG, "***** preloadOriginalImageFromUrl() *****");

        if (Utility.DEBUG) Log.e(TAG, "aUrl: " + aUrl);

        if(useGlide){
            loadImageWithGlide(aUrl, aView, aImageType, aImgW, aImgH, aImageLoadingListener, TYPE_PRELOAD);
            return;
        }

    }

    private void setBitmapAsBackgroundOrImage(final View aView, final Bitmap cache, final String aUrl,
                                              final int aType, final int loadStatus, final ImageLoadingListener aImageLoadingListener,
                                              final int aImgW, final int aImgH) {
    	if(Utility.DEBUG)Log.w(TAG, "setBitmapAsBackgroundOrImage");
    	if(Utility.DEBUG)Log.w(TAG, "loadStatus: " + loadStatus);

        if (loadStatus == TYPE_PRELOAD) {
            Log.w(TAG, "loadStatus == TYPE_PRELOAD");
            if (aImageLoadingListener != null) {
                aImageLoadingListener.onLoadingComplete(aUrl, aView, cache);
            }
            return;
        }

        if (aView == null) {
            Log.w(TAG, "aView == null");
            if (aImageLoadingListener != null) {
                aImageLoadingListener.onLoadingComplete(aUrl, aView, cache);
            }
            return;
        }

        if (aView instanceof ZoomImageView) {
            Log.w(TAG, "aView instanceof ZoomImageView");

            if(UserDataInfo.getCurrentContext()!=null){
                ((Activity) UserDataInfo.getCurrentContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (cache == null || cache.isRecycled()) {
                            setImageViewIntoDefaultSize(aView, 300);
                            ((ZoomImageView) aView).setImageResource(R.drawable.default_img);
                        } else {
                            ((ZoomImageView) aView).setImageBitmap(cache);
                        }

                    }
                });
            }


        } else if (aView instanceof ImageView) {
            Log.w(TAG, "aView instanceof ImageView");

            if(UserDataInfo.getCurrentContext()!=null){
                ((Activity) UserDataInfo.getCurrentContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (aType == IMAGE_SRC_FROM_NEWS_PAGE || aType == IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE) {
                            if (Utility.DEBUG) Log.i(TAG, "aType: " + aType);
    //                        if (!aUrl.contains("imgapi")) {
                                if (mImageSizeInfo != null) {
                                    HashMap<String, Integer> map = mImageSizeInfo.get(aUrl);
                                    if (map != null) {
                                        int imgH = map.get(KEY_IMG_H);
                                        int imgW = map.get(KEY_IMG_W);
                                        if (imgH > 0) {
                                            ViewGroup.LayoutParams params = aView.getLayoutParams();
                                            float scale = ((float) mScreenWidth) / imgW;
                                            params.height = (int) (imgH * scale);
                                            ((ImageView) aView).setLayoutParams(params);
                                        }
                                    }
                                }
    //                        } else {
    //                            if (mImageSizeInfo != null) {
    //                                HashMap<String, Integer> map = mImageSizeInfo.get(aUrl);
    //                                if (map != null) {
    //                                    int imgH = map.get(KEY_IMG_H);
    //                                    if (imgH > 0) {
    //                                        ViewGroup.LayoutParams params = aView.getLayoutParams();
    //                                        params.height = (int) (imgH * mDensity);
    //                                        ((ImageView) aView).setLayoutParams(params);
    //                                    }
    //                                }
    //                            }
    //                        }
                            if (cache == null || cache.isRecycled()) {
                                ((ImageView) aView).setImageResource(R.drawable.default_img);
                            } else {
                                ((ImageView) aView).setImageBitmap(cache);
                            }
                        } else if (aType == IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE) {
                            if (Utility.DEBUG) Log.i(TAG, "IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE");
    //						if(Utility.DEBUG)Log.e(TAG, "cache.isRecycled(): " + cache.isRecycled());
                            if (cache == null || cache.isRecycled()) {
                                setImageViewIntoDefaultSize(aView, 300);
                                ((CustomImageTopcrop) aView).setCenterCrop();
                                ((CustomImageTopcrop) aView).setImageResource(R.drawable.default_img);
                            } else {
                                if(mImageSizeInfo != null){
                                    HashMap<String, Integer> map = mImageSizeInfo.get(aUrl);
                                    if (map != null) {
                                        int imgH = map.get(KEY_IMG_H);
                                        int imgW = map.get(KEY_IMG_W);
                                        if (Utility.DEBUG) Log.i(TAG, "=== originImage info ===");
                                        if (Utility.DEBUG) Log.e(TAG, "aUrl: " + aUrl);
                                        if (Utility.DEBUG) Log.e(TAG, "imgH: " + imgH);
                                        if (!aUrl.contains("imgapi")) {
                                            if (imgH > 0) {
                                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                                                float scale = ((float) mScreenWidth) / imgW;
                                                params.height = (int) (imgH * scale);
                                                ((CustomImageTopcrop) aView).setLayoutParams(params);
                                            }
                                        } else {
                                            int mNewsBigImgMaxHeight = 300;
                                            mNewsBigImgMaxHeight = mResizeLayoutParams.getPixelAfterScale(mNewsBigImgMaxHeight);
                                            if (Utility.DEBUG) Log.e(TAG, "mNewsBigImgMaxHeight: " + mNewsBigImgMaxHeight);
                                            if (imgH > mNewsBigImgMaxHeight) {
                                                setImageViewIntoDefaultSize(aView, 300);
                                            }
                                        }
                                        ((CustomImageTopcrop) aView).setImageBitmap(cache);
                                    }else{
                                        if (Utility.DEBUG) Log.e(TAG, "map==null!!");
                                        mImageSizeInfo.remove(aUrl);
                                        mImageCache.remove(aUrl);
                                        if(loadStatus==TYPE_PRELOAD){
                                            preloadOriginalImageFromUrl(aUrl, aView, aType, aImgW, aImgH, aImageLoadingListener);
                                        }else{
                                            loadImageWithOriginalSize(aUrl, aView, aType, aImgW, aImgH, aImageLoadingListener);
                                        }
                                    }
                                }
                            }
                        } else if (aType == IMAGE_SRC_FROM_NEWS_LIST) {
                            if (Utility.DEBUG) Log.i(TAG, "$$$$$$$$$$ IMAGE_SRC_FROM_NEWS_LIST");
    //						if(Utility.DEBUG)Log.e(TAG, "cache.isRecycled(): " + cache.isRecycled());
                            if (cache == null || cache.isRecycled()) {
                                Log.w(TAG, "111111111111111");
                                ((CustomImageTopcrop) aView).setCenterCrop();
                                ((CustomImageTopcrop) aView).setImageResource(R.drawable.default_img);
                            } else {
                                Log.w(TAG, "232222222");
                                ((CustomImageTopcrop) aView).setImageBitmap(cache);
                            }
                        } else if (aType == IMAGE_SRC) {
                            if (Utility.DEBUG) Log.i(TAG, "IMAGE_SRC");
    //						if(Utility.DEBUG)Log.e(TAG, "cache.isRecycled(): " + cache.isRecycled());
                            if (cache == null || cache.isRecycled()) {
                                setImageViewIntoDefaultSize(aView, 200);
                                ((ImageView) aView).setImageResource(R.drawable.default_img);
                            } else {
                                ((ImageView) aView).setImageBitmap(cache);
                            }
                        } else {
                            Log.w(TAG, "3333333333333");
                            if (cache == null || cache.isRecycled()) {
                                setImageViewIntoDefaultSize(aView, 200);
                                ((ImageView) aView).setBackgroundResource(R.drawable.default_img);
                            } else {
                                Drawable drawable = new BitmapDrawable(mContext.getResources(), cache);
                                ((ImageView) aView).setBackgroundDrawable(drawable);
                            }
                        }
                    }
                });

            }
        } else if (aView instanceof RelativeLayout) {
            if (Utility.DEBUG) Log.e(TAG, "RelativeLayout");
            Drawable drawable = new BitmapDrawable(mContext.getResources(), cache);
            ((RelativeLayout) aView).setBackgroundDrawable(drawable);
        } else if (aView instanceof LinearLayout) {
            if (Utility.DEBUG) Log.e(TAG, "LinearLayout");
            Drawable drawable = new BitmapDrawable(mContext.getResources(), cache);
            ((LinearLayout) aView).setBackgroundDrawable(drawable);
        } else if (aView instanceof FrameLayout) {
            if (Utility.DEBUG) Log.e(TAG, "FrameLayout");
            Drawable drawable = new BitmapDrawable(mContext.getResources(), cache);
            ((FrameLayout) aView).setBackgroundDrawable(drawable);
        } else {
            if (Utility.DEBUG) Log.e(TAG, "ELSE");
            Drawable drawable = new BitmapDrawable(mContext.getResources(), cache);
            aView.setBackgroundDrawable(drawable);
        }
        if (aImageLoadingListener != null) {
            aImageLoadingListener.onLoadingComplete(aUrl, aView, cache);
        }
    }

    private void setImageViewIntoDefaultSize(View aView, int aNewsBigImgMaxHeight){
        aNewsBigImgMaxHeight = mResizeLayoutParams.getPixelAfterScale(aNewsBigImgMaxHeight);
        if (Utility.DEBUG) Log.e(TAG, "mNewsBigImgMaxHeight: " + aNewsBigImgMaxHeight);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.height = aNewsBigImgMaxHeight;
        aView.setLayoutParams(params);
    }

    //TODO clearCache()
    public void clearCache() {
        if (Utility.DEBUG) Log.v(TAG, "===clearCache()===");
        if(mImageCache!=null){
            mImageCache.evictAll();
        }
        if(mImageSizeInfo!=null){
            mImageSizeInfo.clear();
        }
    }

    private void processScreenSize() {

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        final DisplayMetrics metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");

        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;

        if (Utility.DEBUG) Log.e(TAG, "mScreenWidth: " + mScreenWidth);
        if (Utility.DEBUG) Log.e(TAG, "mScreenHeight: " + mScreenHeight);

    }

    public File convertBitmapToFile(String folderPath, String fileName, Bitmap cache) {

        if (Utility.DEBUG) Log.e(TAG, "convertBitmapToFile");

        File file = null;
        FileOutputStream fos;
        try {

            if (Utility.DEBUG) Log.e(TAG, "folderPath: " + folderPath);
            if (Utility.DEBUG) Log.e(TAG, "fileName: " + fileName);

            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            file = new File(folderPath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            //Convert bitmap to byte array
            Bitmap bitmap = cache;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CompressFormat compressFormat = null;
            String mimeType = getFileExtension(file.getAbsolutePath());
            if (mimeType.equals("png")) {
                compressFormat = CompressFormat.PNG;
            } else if (mimeType.equals("jpeg")
                    || mimeType.equals("jpg")) {
                compressFormat = CompressFormat.JPEG;
            }
            if (compressFormat != null) {
                bitmap.compress(compressFormat, 60, bos);
            }
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public String getFileExtension(final String aPath) throws Exception {
        final int lastIndexOfDot = aPath.lastIndexOf(".");
        return aPath.subSequence(lastIndexOfDot + 1, aPath.length()).toString();
    }

    private String getResourceName(int aResourceId) {

        String resourceIdString = mContext.getResources().getString(aResourceId);
        String resourceName = resourceIdString.substring(resourceIdString.lastIndexOf("."), resourceIdString.length()); //.png need to fix
        if (Utility.DEBUG) Log.e(TAG, "resourceName: " + resourceName);

        return resourceName;

    }

    public interface ImageLoadingListener {
        abstract void onLoadingStart(String aImageUrl, View aView);

        abstract void onLoadingFailed(String aImageUrl, View aView, Exception aException);

        abstract void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap);

        abstract void onLoadingCancelled();

        abstract void onProgressUpdate(String aImageUrl, int aProgress, int max);
    }

}

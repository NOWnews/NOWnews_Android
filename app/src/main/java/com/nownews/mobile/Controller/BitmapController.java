
package com.nownews.mobile.Controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.NewsCategory.NewsListRecyclerViewAdapter;
import com.nownews.mobile.Splash.SplashActivity;
import com.nownews.mobile.Widget.CustomImageTopcrop;
import com.nownews.mobile.Widget.ZoomImageView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitmapController {

    public final static int IMAGE_SRC = 0;
    public final static int IMAGE_SRC_FROM_NEWS_LIST = 1;
    public final static int IMAGE_SRC_FROM_NEWS_PAGE = 2;
    public final static int IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE = 3;
    private final static String TAG = "BitmapController";
    private static BitmapController mBitmapControllerInstance;
    public final String DOWNLOAD_FOLDER_NAME = UserDataInfo.ThumbnailPath;
    private final int TYPE_LOAD = 0x568;
    private final int TYPE_PRELOAD = 0x987;
    private Context mContext;
    private LruCache<String, Bitmap> mImageCache;
    private HashMap<String, SoftReference<DownloadBitmapTask>> mCurrentRunTask;
    //	private DownloadBitmapTask mDownloadBitmapTask;
    private ReSizeLayoutParams mResizeLayoutParams;
    private String KEY_IMG_W = "imgW";
    private String KEY_IMG_H = "imgH";
    private HashMap<String, HashMap<String, Integer>> mImageSizeInfo;
    private boolean needToStop = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mDensity;
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
            mBitmapControllerInstance.mContext = aContext.getApplicationContext();
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

        int currentViewWidth;
        int currentViewHeight;
        if (aView == null) {
            currentViewWidth = 0;
            currentViewHeight = 0;
        } else {
            int[] currentViewInfo = getCurrentViewInfo(aView);
            currentViewWidth = currentViewInfo[0];
            currentViewHeight = currentViewInfo[1];
        }

        int targetViewWidth = 0;
        int targetViewHeight = 0;
        if (aUrl.contains("imgapi") || aUrl.contains("youtube") || !aUrl.contains("http://s.nownews.com/")) {
            //do nothing...
        } else if (currentViewWidth > 0 && currentViewHeight > 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewWidth;
            targetViewHeight = currentViewHeight;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
        } else if (currentViewWidth <= 0 && currentViewHeight > 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewHeight;
            targetViewHeight = currentViewHeight;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, "", targetViewHeight, Utility.IMG_QUALITY, aUrl);
        } else if (currentViewWidth > 0 && currentViewHeight <= 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewWidth;
            targetViewHeight = currentViewWidth;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
        } else {
            int[] targetViewInfo = getOriginalWightHeight(aImgW, aImgH, currentViewWidth, currentViewHeight);
            targetViewWidth = targetViewInfo[0];
            targetViewHeight = targetViewInfo[1];
            if (targetViewWidth > 0) {
                aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
            } else if (targetViewHeight > 0) {
                aUrl = String.format(WebAPIUrl.SCALE_IMAGE, "", targetViewHeight, Utility.IMG_QUALITY, aUrl);
            } else {
                if(Utility.DEBUG)Log.w(TAG, "targetViewWidth==0 && targetViewHeight==0");
                if(aUrl.contains("s.nownews.com")){
                    targetViewWidth = Utility.getScreenWidth(mContext);
                    aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
                }
            }
        }
        if (Utility.DEBUG) Log.e(TAG, "aUrl: " + aUrl);


//        Bitmap cache = mImageCache.get(aUrl);
//        if (cache != null) {
//            setBitmapAsBackgroundOrImage(aView, cache, aUrl, aImageType, TYPE_LOAD, aImageLoadingListener, aImgW, aImgH);
//            return;
//        }
//        mImageCache.remove(aUrl);

        if(useGlide){
            loadImageWithGlide(aUrl, aView, aImageType, aImgW, aImgH, aImageLoadingListener, TYPE_LOAD);
            return;
        }

        if (aUrl != null && !aUrl.trim().equals("")) {
            if (mCurrentRunTask == null) {
                mCurrentRunTask = new HashMap<String, SoftReference<DownloadBitmapTask>>();
            }
            DownloadBitmapTask downloadBitmapTask = new DownloadBitmapTask();
            downloadBitmapTask.executeOnExecutor(Utility.getExecutorForShow(), aUrl,
                    aView, aImageType, targetViewWidth, targetViewHeight,
                    TYPE_LOAD, 0, aImageLoadingListener, aImgW, aImgH);
            SoftReference<DownloadBitmapTask> ref = new SoftReference<DownloadBitmapTask>(downloadBitmapTask);
            mCurrentRunTask.put(aUrl, ref);
        }

    }

    private void loadImageWithGlide(final String aUrl, final View aView, final int aImageType, final int aImgW,
                                    final int aImgH, final ImageLoadingListener aImageLoadingListener, final int aImageLoadType){
        Glide.with(mContext)
                .load(aUrl)
                .asBitmap()
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.default_img)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Log.v(TAG, "onResourceReady");

                        if (mImageSizeInfo == null) {
                            mImageSizeInfo = new HashMap<String, HashMap<String, Integer>>();
                        }
                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put(KEY_IMG_W, resource.getWidth());
                        map.put(KEY_IMG_H, resource.getHeight());
                        mImageSizeInfo.put(aUrl, map);
//
//                        addBitmapToMemoryCache(aUrl, resource);

                        setBitmapAsBackgroundOrImage(aView, resource, aUrl, aImageType, aImageLoadType, aImageLoadingListener, aImgW, aImgH);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        if (aImageLoadingListener != null) {
                            aImageLoadingListener.onLoadingFailed(aUrl, aView, null);
                        }
                        super.onLoadFailed(e, errorDrawable);
                    }
                });
    }

    public void preloadOriginalImageFromUrl(String aUrl, View aView, int aImageType, int aImgW, int aImgH, ImageLoadingListener aImageLoadingListener) {

        if (Utility.DEBUG) Log.e(TAG, "***** preloadOriginalImageFromUrl() *****");

        int currentViewWidth;
        int currentViewHeight;
        if (aView == null) {
            currentViewWidth = 0;
            currentViewHeight = 0;
        } else {
            int[] currentViewInfo = getCurrentViewInfo(aView);
            currentViewWidth = currentViewInfo[0];
            currentViewHeight = currentViewInfo[1];
        }

        int targetViewWidth = 0;
        int targetViewHeight = 0;
        if (aUrl.contains("imgapi") || aUrl.contains("youtube") || !aUrl.contains("http://s.nownews.com/")) {
            //do nothing...
        } else if (currentViewWidth > 0 && currentViewHeight > 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewWidth;
            targetViewHeight = currentViewHeight;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
        } else if (currentViewWidth <= 0 && currentViewHeight > 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewHeight;
            targetViewHeight = currentViewHeight;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, "", targetViewHeight, Utility.IMG_QUALITY, aUrl);
        } else if (currentViewWidth > 0 && currentViewHeight <= 0 && (aImgW == 0 || aImgH == 0)) {
            targetViewWidth = currentViewWidth;
            targetViewHeight = currentViewWidth;
            aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
        } else {
            int[] targetViewInfo = getOriginalWightHeight(aImgW, aImgH, currentViewWidth, currentViewHeight);
            targetViewWidth = targetViewInfo[0];
            targetViewHeight = targetViewInfo[1];
            if (targetViewWidth > 0) {
                aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
            } else if (targetViewHeight > 0) {
                aUrl = String.format(WebAPIUrl.SCALE_IMAGE, "", targetViewHeight, Utility.IMG_QUALITY, aUrl);
            } else {
                Log.w(TAG, "targetViewWidth==0 && targetViewHeight==0");
                if(aUrl.contains("s.nownews.com")){
                    targetViewWidth = Utility.getScreenWidth(mContext);
                    aUrl = String.format(WebAPIUrl.SCALE_IMAGE, targetViewWidth, "", Utility.IMG_QUALITY, aUrl);
                }
            }
        }
        if (Utility.DEBUG) Log.e(TAG, "aUrl: " + aUrl);

//        Bitmap cache = mImageCache.get(aUrl);
//        if (cache == null) {
//            mImageCache.remove(aUrl);
//        }else{
//            setBitmapAsBackgroundOrImage(aView, cache, aUrl, aImageType, TYPE_LOAD, aImageLoadingListener, aImgW, aImgH);
//            return;
//        }

        if(useGlide){
            loadImageWithGlide(aUrl, aView, aImageType, aImgW, aImgH, aImageLoadingListener, TYPE_PRELOAD);
            return;
        }

        if (aUrl != null && !aUrl.trim().equals("")) {
            if (mCurrentRunTask == null) {
                mCurrentRunTask = new HashMap<String, SoftReference<DownloadBitmapTask>>();
            }
            DownloadBitmapTask downloadBitmapTask = new DownloadBitmapTask();
            downloadBitmapTask.executeOnExecutor(Utility.getExecutorForShow(), aUrl,
                    aView, aImageType, targetViewWidth, targetViewHeight,
                    TYPE_PRELOAD, 0, aImageLoadingListener, aImgW, aImgH);
            SoftReference<DownloadBitmapTask> ref = new SoftReference<DownloadBitmapTask>(downloadBitmapTask);
            mCurrentRunTask.put(aUrl, ref);
        }

    }

    private int[] getOriginalWightHeight(int aImgW, int aImgH, int aCurrentViewWidth, int aCurrentViewHeight) {

        int targetViewWidth = 0;
        int targetViewHeight = 0;

        if (aCurrentViewWidth <= 0) {
            aCurrentViewWidth = mScreenWidth;
        }
        if (aCurrentViewHeight <= 0) {
            aCurrentViewHeight = mScreenHeight;
        }

        //不管直的或橫的都以寬為準
        if (aImgW >= aCurrentViewWidth) { //寬大於目標容器
            targetViewWidth = aCurrentViewWidth;
            targetViewHeight = (aImgH * aCurrentViewWidth) / aImgW;
        } else {
            targetViewWidth = aImgW;
            targetViewHeight = aImgH;
        }

        if (Utility.DEBUG) Log.v(TAG, "targetViewWidth: " + targetViewWidth);
        if (Utility.DEBUG) Log.v(TAG, "targetViewHeight: " + targetViewHeight);

        int[] viewInfo = new int[]{targetViewWidth, targetViewHeight};

        return viewInfo;
    }

    private void setBitmapAsBackgroundOrImage(final View aView, final Bitmap cache, final String aUrl,
                                              final int aType, final int loadStatus, final ImageLoadingListener aImageLoadingListener,
                                              final int aImgW, final int aImgH) {
    	if(Utility.DEBUG)Log.w(TAG, "setBitmapAsBackgroundOrImage");
    	if(Utility.DEBUG)Log.w(TAG, "loadStatus: " + loadStatus);

        if (loadStatus == TYPE_PRELOAD) {
            Log.w(TAG, "loadStatus == TYPE_PRELOAD");
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

        } else if (aView instanceof ImageView) {
            Log.w(TAG, "aView instanceof ImageView");

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

    public synchronized Bitmap cacheBitmap(final byte[] aBytes, final int width, final int height, String mImageUrl) throws IOException {

        if(mImageCache==null){
            initImageCache();
        }

        Bitmap cache = mImageCache.get(mImageUrl);
        if (cache != null) {
            return cache;
        }
        mImageCache.remove(mImageUrl);
        cache = autoSimpleSize(aBytes, width, height, mImageUrl);
        if (cache != null) {
            addBitmapToMemoryCache(mImageUrl, cache);
        }
        return cache;
    }

    public Bitmap autoSimpleSize(final byte[] aBytes, final int width, final int height, String mImageUrl) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        options.inPreferredConfig = Config.RGB_565;
        options.inDither = true;
//        BitmapFactory.decodeFile(mImageUrl, options);
        BitmapFactory.decodeByteArray(aBytes, 0, aBytes.length, options);

        if (mImageSizeInfo == null) {
            mImageSizeInfo = new HashMap<String, HashMap<String, Integer>>();
        }
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(KEY_IMG_W, options.outWidth);
        map.put(KEY_IMG_H, options.outHeight);
        mImageSizeInfo.put(mImageUrl, map);

        if (width <= 0 || height <= 0) {
            options.inSampleSize = 1;
        } else if (options.outWidth >= options.outHeight) { //橫的or正方
            if (Utility.DEBUG) Log.e(TAG, "橫的or正方");
            if (options.outWidth > width) {
                options.inSampleSize = (options.outWidth / width);
            } else {
                options.inSampleSize = 1;
            }
        } else if (options.outHeight > options.outWidth) { //直的
            if (Utility.DEBUG) Log.e(TAG, "直的");
            if (options.outHeight > height) {
                options.inSampleSize = (options.outHeight / height);
            } else {
                options.inSampleSize = 1;
            }
        }

        if (Utility.DEBUG) Log.d(TAG, "options.inSampleSize: " + options.inSampleSize);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(aBytes, 0, aBytes.length, options);

        return bitmap;
    }

    private int computeSampleSize(final BitmapFactory.Options options, final int minSideLength, final int maxNumOfPixels) {
        final int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels); // -1 2073600  return 128
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private int computeInitialSampleSize(final BitmapFactory.Options options, final int minSideLength, final int maxNumOfPixels) {

        final double w = options.outWidth;
        final double h = options.outHeight;

        final int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels)); // 1
        final int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength)); //128
        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private int[] getCurrentViewInfo(View aView) {

        ViewGroup.LayoutParams lp = aView.getLayoutParams();
        int currentViewWidth = lp.width;
        int currentViewHeight = lp.height;

        if (Utility.DEBUG) Log.v(TAG, "currentViewWidth: " + currentViewWidth);
        if (Utility.DEBUG) Log.v(TAG, "currentViewHeight: " + currentViewHeight);

        int[] viewInfo = new int[]{currentViewWidth, currentViewHeight};

        return viewInfo;
    }

    //TODO clearCache()
    public void clearCache() {
        if (Utility.DEBUG) Log.v(TAG, "===clearCache()===");
        mImageCache.evictAll();
        mImageSizeInfo.clear();
//        Glide.get(mContext).clearMemory();
//        try {
//            if (mImageCache != null) {
//                Map<String, Bitmap> map = mImageCache.snapshot();
//                Iterator it = map.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry pair = (Map.Entry) it.next();
//                    if (Utility.DEBUG) Log.e(TAG, pair.getKey() + " = " + pair.getValue());
//                    Bitmap bitmap = map.get(pair.getKey());
//                    if (Utility.DEBUG)Log.e(TAG, "bitmap is null or not?? " + (bitmap == null ? "true" : "false"));
//                    if (bitmap != null) {
//                        bitmap.recycle();
//                        bitmap = null;
//                    }
//                    it.remove(); // avoids a ConcurrentModificationException
//                }
//                if(Utility.DEBUG)Log.i(TAG, "b mImageCache.size(): " + mImageCache.size());
//                mImageCache.evictAll();
//                if(Utility.DEBUG)Log.v(TAG, "a mImageCache.size(): " + mImageCache.size());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    //TODO clearSignalCache()
    public void clearSignalCache(String aKey) {
        if (Utility.DEBUG) Log.v(TAG, "===clearSignalCache()===");
//    	ImageLoader.getInstance().clearMemoryCache();
//    	ImageLoader.getInstance().clearDiskCache();
        mImageCache.remove(aKey);
//        if (mImageCache != null) {
//            for (String key : mImageCache.keySet()) {
//                if (Utility.DEBUG) Log.e(TAG, "key: " + key);
//                if (key.equals(aKey)) {
//                    SoftReference<Bitmap> value = mImageCache.get(key);
//                    Bitmap bitmap = value.get();
//                    if (bitmap != null) {
////                        bitmap.recycle();
//                        bitmap = null;
//                    }
//                    mImageCache.remove(key);
//                    break;
//                }
//            }
//            if(Utility.DEBUG)Log.e(TAG, "mImageCache.size(): " + mImageCache.size());
//        }
    }

    //TODO closeBitmapController()
    public void closeBitmapController() {
        if (Utility.DEBUG) Log.v(TAG, "===closeBitmapController()===");
        needToStop = true;
        if (mCurrentRunTask != null) {
            for (String key : mCurrentRunTask.keySet()) {
                if (Utility.DEBUG) Log.e(TAG, "key: " + key);
                SoftReference<DownloadBitmapTask> value = mCurrentRunTask.get(key);
                DownloadBitmapTask task = value.get();
                if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                }
                task = null;
            }
            mCurrentRunTask.clear();
        }
        needToStop = false;
    }

    public void setNeedToStop(boolean isNeedToStop) {
        this.needToStop = isNeedToStop;
    }

    public LruCache<String, Bitmap> getImageCache() {
        return mImageCache;
    }

    private void processScreenSize() {

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        final DisplayMetrics metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        mDensity = metric.density;

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

    private File convertBitmapToFile(int aResourceId, Bitmap cache) {

        File file = null;
        FileOutputStream fos;
        try {

            String folderPath = Environment.getExternalStorageDirectory() + File.separator + DOWNLOAD_FOLDER_NAME + File.separator;
            String fileName = getResourceName(aResourceId);

            File folder = new File(folderPath);
            if (!folder.exists() && folder.isDirectory()) {
                folder.mkdirs();
            }

            file = new File(folderPath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            //Convert bitmap to byte array
            Bitmap bitmap = cache;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private String getResourceName(int aResourceId) {

        String resourceIdString = mContext.getResources().getString(aResourceId);
        String resourceName = resourceIdString.substring(resourceIdString.lastIndexOf("."), resourceIdString.length()); //.png need to fix
        if (Utility.DEBUG) Log.e(TAG, "resourceName: " + resourceName);

        return resourceName;

    }

    public void loadImageFromFile(final ImageView aImage, final String aPath, final Handler aHandler) {

        if (Utility.DEBUG) Log.v(TAG, "loadImageFromFile::: " + aPath);

        new Thread(new Runnable() {

            @Override
            public void run() {

                Bitmap bitmap = null;
                File file = new File(aPath);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Config.RGB_565;
                option.inPurgeable = true;
                option.inInputShareable = true;
                option.inJustDecodeBounds = true;

                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    BitmapFactory.decodeStream(fileInputStream, null, option);
                    fileInputStream.close();

                    int IMAGE_MAX_SIZE = 1024; // maximum dimension limit
                    int scale = 1;
                    if (option.outHeight > IMAGE_MAX_SIZE || option.outWidth > IMAGE_MAX_SIZE) {
                        scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(option.outHeight, option.outWidth)) / Math.log(0.5)));
                    }

                    // Decode with inSampleSize
                    option.inSampleSize = scale;
                    option.inJustDecodeBounds = false;

                    fileInputStream = new FileInputStream(file);
                    bitmap = BitmapFactory.decodeStream(fileInputStream, null, option);
                    fileInputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (aHandler != null) {
                    Message message = new Message();
                    message.what = SplashActivity.GET_SPLASH_INFO;
                    message.obj = bitmap;
                    aHandler.sendMessage(message);
                }

            }
        }).start();

    }

    public Bitmap getSingalBitmap(String aBigImgUrl) {
        if (Utility.DEBUG) Log.d(TAG, "getSingalBitmap::: " + aBigImgUrl);
        Bitmap cache = mImageCache.get(aBigImgUrl);
        if (Utility.DEBUG) Log.d(TAG, "getSingalBitmap::: 1111111111111111");
        return cache;
    }

    public interface ImageLoadingListener {
        abstract void onLoadingStart(String aImageUrl, View aView);

        abstract void onLoadingFailed(String aImageUrl, View aView, Exception aException);

        abstract void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap);

        abstract void onLoadingCancelled();

        abstract void onProgressUpdate(String aImageUrl, int aProgress, int max);
    }

    class DownloadBitmapTask extends AsyncTask<Object, Integer, Bitmap> {

        private String mUrl = null;
        private View mView = null;
        private int mType;
        private int mTargetViewWidth;
        private int mTargetViewHeight;
        private int mCurrentStatus;
        private int mRetryCount;
        private ImageLoadingListener mImageLoadingListener;

        private int mImgW;
        private int mImgH;

        @Override
        protected Bitmap doInBackground(Object... params) {

//        	if(DEBUG)Log.e(TAG, "##################### doInBackground #####################");

            mUrl = (String) params[0];
            mView = (View) params[1];
            mType = (Integer) params[2];
            mTargetViewWidth = (Integer) params[3];
            mTargetViewHeight = (Integer) params[4];
            mCurrentStatus = (Integer) params[5];
            mRetryCount = (Integer) params[6];
            mImageLoadingListener = (ImageLoadingListener) params[7];
            mImgW = (Integer) params[8];
            mImgH = (Integer) params[9];

            Bitmap bitmap = null;

            try {

                bitmap = downloadBitmap(mUrl);

            } catch (Exception e) {
                e.printStackTrace();
                setBitmapAsBackgroundOrImage(mView, null, mUrl, mType, mCurrentStatus, mImageLoadingListener, mImgW, mImgH);
                if (mImageLoadingListener != null) {
                    mImageLoadingListener.onLoadingFailed(mUrl, mView, e);
                }
            }

            return bitmap;

        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if (result != null && mImageCache != null) {

                if (Utility.DEBUG)
                    Log.e(TAG, "##################### onPostExecute not null #####################");
                if (Utility.DEBUG) Log.e(TAG, "onPostExecute mUrl: " + mUrl);
                setBitmapAsBackgroundOrImage(mView, result, mUrl, mType, mCurrentStatus, mImageLoadingListener, mImgW, mImgH);
                addBitmapToMemoryCache(mUrl, result);

            } else {

                if (Utility.DEBUG)
                    Log.e(TAG, "##################### onPostExecute is null#####################");
                if (mRetryCount == 0) {
                    if (Utility.DEBUG) Log.v(TAG, "Retry!!!!!!!!");
                    mRetryCount++;
                    DownloadBitmapTask retryTask = new DownloadBitmapTask();
                    retryTask.executeOnExecutor(Utility.getExecutorForShow(), mUrl,
                            mView, mType, mTargetViewWidth, mTargetViewHeight,
                            mCurrentStatus, mRetryCount, mImageLoadingListener, mImgW, mImgH);
                } else if (mRetryCount == 1) {
                    Log.v(TAG, "still failed set default img");
                    setBitmapAsBackgroundOrImage(mView, null, mUrl, mType, mCurrentStatus, mImageLoadingListener, mImgW, mImgH);
                    if (mImageLoadingListener != null) {
                        mImageLoadingListener.onLoadingFailed(mUrl, mView, null);
                    }
                }
            }

        }

        public Bitmap downloadBitmap(String aUrl) {

            InputStream inputStream = null;
            ByteArrayOutputStream outputStream = null;
            Bitmap bitmap = null;
            OkHttpClient okHttpClient;
            Request request;
            Response response;
            long fileLength;
            int statusCode = -1;

            // Download Images from the Internet
            if (aUrl != null && !aUrl.trim().equals("")) {
                long StartTime = System.currentTimeMillis();
                try {

                    okHttpClient = new OkHttpClient();
                    request = new Request.Builder()
                            .url(aUrl)
                            .build();
                    response = okHttpClient.newCall(request).execute();
                    statusCode = response.code();

                    if (Utility.DEBUG) Log.i(TAG, "statusCode: " + statusCode);
                    if (statusCode != HttpURLConnection.HTTP_OK && statusCode != 500) {
                        if (Utility.DEBUG) Log.i(TAG, "ErrorMessage: " + response.message());
                        return null;
                    }

                    fileLength = response.body().contentLength(); //檔案大小
                    if (Utility.DEBUG) Log.d(TAG, "fileLength: " + fileLength);
                    inputStream = response.body().byteStream();
                    outputStream = new ByteArrayOutputStream();

                    byte[] data = new byte[1024];

                    int next = -1;
                    int size = 0;
                    int tempPercentage = 0;
                    while ((next = inputStream.read(data)) != -1) {

                        if (needToStop) {
                            if (mImageLoadingListener != null) {
                                mImageLoadingListener.onLoadingCancelled();
                            }
                            break;
                        }

                        outputStream.write(data, 0, next);
                        size += next;
                        if (mImageLoadingListener != null) {
                            double persentage = size * 100D / fileLength;
                            if ((int) persentage != tempPercentage) {
                                mImageLoadingListener.onProgressUpdate(aUrl, (int) persentage, 100);
                            }
                            tempPercentage = (int) persentage;
                        }
                    }
                    bitmap = cacheBitmap(outputStream.toByteArray(), mTargetViewWidth, mTargetViewHeight, mUrl);
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                            outputStream.flush();
                        }
                        if (response !=null) {
                            response.body().close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }

                    long ProcessTime = System.currentTimeMillis() - StartTime;
                    if (Utility.DEBUG)
                        Log.e(TAG, "----------------------------------------- \n"
                                + "statusCode: " + statusCode
                                + "\nurl: " + aUrl
                                + "\nProcessTime: " + ProcessTime + "ms"
                                + "\n-----------------------------------------");
                    return bitmap;
                } catch (Exception ex) {
                    if (Utility.DEBUG)Log.e(TAG, "Download Error Message!!!!: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (Throwable ex) {
                    if (Utility.DEBUG)Log.e(TAG, "Download Error Message2!!!!: " + ex.getMessage());
                    if (ex instanceof OutOfMemoryError) {
                        if (Utility.DEBUG) Log.e(TAG, "OutOfMemoryError!!!!!!");
                        mImageCache.evictAll();
                    }
                } finally {
                    long ProcessTime = System.currentTimeMillis() - StartTime;
                    if (Utility.DEBUG)
                        Log.e(TAG, "----------------------------------------- \n"
                                + "statusCode: " + statusCode
                                + "\nurl: " + aUrl
                                + "\nProcessTime: " + ProcessTime + "ms"
                                + "\n-----------------------------------------");
                }
            }

            return null;
        }

    }
}

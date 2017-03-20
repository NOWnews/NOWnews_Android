package com.nownews.mobile.Service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.SplashImageJson;
import com.nownews.mobile.Widget.CustomImageTopcrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by cindy on 2017/2/24.
 */

public class GetSplashImageService extends Service {

    private final String TAG = getClass().getSimpleName();
    private BitmapController mBitmapController;
    private ApiController mApiController;
    private ApiHandler mHandler;
    private String mSplashImageUrl;

    public GetSplashImageService(){ }

    private static class ApiHandler extends Handler {

        private String TAG = getClass().getSimpleName();
        private WeakReference<GetSplashImageService> mSplash;

        private ApiHandler(GetSplashImageService aSplash){
            mSplash = new WeakReference<GetSplashImageService>(aSplash);
        }

        @Override
        public void handleMessage(Message msg) {

            GetSplashImageService splash = mSplash.get();

            switch (msg.what) {
                case ParameterSet.GET_SPLASH_IMAGE_DONE:
                    SplashImageJson splashImageInfo = (SplashImageJson) msg.obj;
                    if (splashImageInfo == null) {
                        break;
                    }
                    splash.mSplashImageUrl = splashImageInfo.url;
                    splash.mSplashImageUrl = Utility.getSrcFromImgapi(splash.mSplashImageUrl);
                    int screenHeight = Utility.getScreenHeight(splash);
                    int screenWidth = Utility.getScreenWidth(splash);
                    splash.mSplashImageUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, screenHeight, Utility.IMG_QUALITY, splash.mSplashImageUrl);
                    Log.d(TAG, "splash.mSplashImageUrl: " + splash.mSplashImageUrl);
                    splash.processImage();
                    break;
                case ParameterSet.GET_SPLASH_IMAGE_FAILED:
                case ParameterSet.SOCKET_TIME_OUT:
                    if (Utility.DEBUG) Log.d(TAG, "SOCKET_TIME_OUT!!!");
                    Utility.openSocketTimeoutDialog(splash);
                    break;
            }

        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(TAG, "^^^^onStartCommand^^^^");
        initController();
        getSplashImage();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initController(){
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(this);
        mHandler = new ApiHandler(this);
    }

    private void getSplashImage(){
        if (mApiController != null) {
            mApiController.getSplashImage(mHandler);
        }
    }


    private void processImage() {
        if (mSplashImageUrl != null
                && !mSplashImageUrl.trim().equals("")) {

//            mSplashImageUrl = "http://s.nownews.com/d3/5b/d35b4954e132a62a9bf87d6d5cb1875d.jpg";

            Glide.with(this)
                    .load(mSplashImageUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.drawable.default_img)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Log.v(TAG, "onResourceReady");

                            try {
                                File file = new File(UserDataInfo.ThumbnailPath);
                                if(file!=null && !file.exists()){
                                    file.mkdirs();
                                }
                                FileOutputStream out = new FileOutputStream(file.getAbsolutePath() + File.separator + "Splash.jpg");
                                resource.compress(Bitmap.CompressFormat.JPEG, 70, out);
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) { }
                    });

//            mBitmapController.loadImageWithOriginalSize(mSplashImageUrl, null, BitmapController.IMAGE_SRC, 0, 0, new BitmapController.ImageLoadingListener() {
//
//                @Override
//                public void onProgressUpdate(String aImageUrl, int aProgress, int max) { }
//
//                @Override
//                public void onLoadingStart(String aImageUrl, View aView) { }
//
//                @Override
//                public void onLoadingFailed(String aImageUrl, View aView, Exception aException) { }
//
//                @Override
//                public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {
//
//                    if (aBitmap != null) {
//                        Log.v(TAG, "onLoadingComplete && aBitmpa is not null");
//                        Log.w(TAG, "aImageUrl: " + aImageUrl);
//                        mBitmapController.convertBitmapToFile(UserDataInfo.ThumbnailPath, "Splash.jpg", aBitmap);
//                    }
//
//                }
//
//                @Override
//                public void onLoadingCancelled() {
//
//                }
//            });
        }
    }

    @Override
    public void onDestroy() {

        if(mHandler!=null){
            mHandler.removeCallbacks(null);
            mHandler = null;
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

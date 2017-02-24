package com.nownews.mobile.Splash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nownews.R;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Controller.BitmapController.ImageLoadingListener;
import com.nownews.mobile.Json.SplashImageJson;
import com.nownews.mobile.Service.GetSplashImageService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SplashActivity extends Activity {

    public final static int GET_SPLASH_INFO = 0x572;
    public final static int NETWORK_SLOW_OK_CLICK = 0x213;
    private final String TAG = getClass().getSimpleName();
    private final static int GOTO_HOME = 0x654;
    private ImageView vSplashImage;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private SharedPreferencesMethods mPreferencesMethods;
    private String mSplashImageUrl;
    private Bitmap mSplashImage;
    private boolean isAllreadyCallGoHome = false;
    private ApiHandler mHandler;
    private static class ApiHandler extends Handler {

        private String TAG = getClass().getSimpleName();
        private WeakReference<SplashActivity> mSplash;

        private ApiHandler(SplashActivity aSplash){
            mSplash = new WeakReference<SplashActivity>(aSplash);
        }

        @Override
        public void handleMessage(Message msg) {

            SplashActivity splash = mSplash.get();

            switch (msg.what) {
                case ParameterSet.GET_SPLASH_IMAGE_DONE:
                    SplashImageJson splashImageInfo = (SplashImageJson) msg.obj;
                    if (splashImageInfo == null) {
                        splash.mHandler.sendEmptyMessageDelayed(GOTO_HOME, 2000);
                        break;
                    }
                    splash.mSplashImageUrl = splashImageInfo.url;
                    int screenHeight = Utility.getScreenHeight(splash);
                    int screenWidth = Utility.getScreenWidth(splash);
                    splash.mSplashImageUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, screenHeight, Utility.IMG_QUALITY, splash.mSplashImageUrl);
                    splash.processImage();
                    break;
                case ParameterSet.GET_SPLASH_IMAGE_FAILED:
                case GOTO_HOME:
                    Intent intent = new Intent();
                    intent.setClass(splash, NewHome.class);
//                    intent.setClass(SplashActivity.this, VideoNewsCategoryFragment.class);
//                    intent.putExtra(VideoNewsCategoryFragment.KEY_POSITION, 0);
                    splash.startActivity(intent);
                    splash.finish();
                    break;
                case GET_SPLASH_INFO:
                    splash.mSplashImage = (Bitmap) msg.obj;
                    if (splash.mSplashImage != null) {
                        splash.vSplashImage.setImageBitmap(splash.mSplashImage);
                    } else {
                        splash.vSplashImage.setImageResource(R.drawable.default_img);
                    }
                    if (!Utility.showNetworkSlowDialog(splash, splash.mHandler, true)) {
                        splash.getSplashImage();
                    }
                    break;
                case NETWORK_SLOW_OK_CLICK:
                    splash.getSplashImage();
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    if (Utility.DEBUG) Log.d(TAG, "SOCKET_TIME_OUT!!!");
                    Utility.openSocketTimeoutDialog(splash);
                    break;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startSplashActivity();

    }

    private void changeSharePreferenceTextSize() {
        mPreferencesMethods = new SharedPreferencesMethods(this);
        String textSize = mPreferencesMethods.getNewsContentTextSize();
        boolean isAlreadyChange = mPreferencesMethods.getAlreadyChangeTextSizeText();
        if (Utility.DEBUG) Log.e(TAG, "111 isAlreadyChange: " + isAlreadyChange);
        if (isAlreadyChange) {
            return;
        }
        if (textSize.equals(getString(R.string.old_max))) {
            mPreferencesMethods.saveNewsContentTextSize(getString(R.string.max));
        } else if (textSize.equals(getString(R.string.old_mid))) {
            mPreferencesMethods.saveNewsContentTextSize(getString(R.string.mid));
        } else if (textSize.equals(getString(R.string.old_small))) {
            mPreferencesMethods.saveNewsContentTextSize(getString(R.string.small));
        }
        mPreferencesMethods.saveAlreadyChangeTextSizeText(true);
        if (Utility.DEBUG) Log.e(TAG, "222 isAlreadyChange: " + isAlreadyChange);
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(this);
//        mBitmapController = new BitmapController(this);
        mHandler = new ApiHandler(this);
    }

    private void processView() {
        vSplashImage = (ImageView) findViewById(R.id.splash_img);
    }

    private void getSplashImage() {

        Log.v(TAG, "getSplashImage");

        Intent intent = new Intent();
        intent.setClass(this, GetSplashImageService.class);
        startService(intent);
        mHandler.sendEmptyMessageDelayed(GOTO_HOME, 2000);
//        if (mApiController != null) {
//            mApiController.getSplashImage(mHandler);
//        }
    }

    private void processImage() {
        if (mSplashImageUrl != null
                && !mSplashImageUrl.trim().equals("")) {

//			ImageLoader.getInstance().displayImage(mSplashImageUrl, vSplashImage, Utility.getOptions(), new com.nostra13.universalimageloader.core.listener.ImageLoadingListener() {
//				
//				@Override
//				public void onLoadingStarted(String arg0, View arg1) {
//					
//				}
//				
//				@Override
//				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
//					mHandler.sendEmptyMessageDelayed(GOTO_HOME, 5000);
//				}
//				
//				@Override
//				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
//					mHandler.sendEmptyMessageDelayed(GOTO_HOME, 5000);
//				}
//				
//				@Override
//				public void onLoadingCancelled(String arg0, View arg1) {
//					
//				}
//			});

//			mSplashImageUrl = "http://s.nownews.com/w/upload/splash_android.?t=1447064854018";
            mBitmapController.loadImageWithOriginalSize(mSplashImageUrl, vSplashImage, BitmapController.IMAGE_SRC, 0, 0, new ImageLoadingListener() {

                @Override
                public void onProgressUpdate(String aImageUrl, int aProgress, int max) {

                }

                @Override
                public void onLoadingStart(String aImageUrl, View aView) {

                }

                @Override
                public void onLoadingFailed(String aImageUrl, View aView,
                                            Exception aException) {

                    if (!isAllreadyCallGoHome) {
                        isAllreadyCallGoHome = true;
                        mHandler.sendEmptyMessageDelayed(GOTO_HOME, 2000);
                    }

                }

                @Override
                public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {

                    if (aBitmap != null) {
                        Log.v(TAG, "onLoadingComplete && aBitmpa is not null");
                        mBitmapController.convertBitmapToFile(UserDataInfo.ThumbnailPath, "Splash.jpg", aBitmap);
                    }
                    if (!isAllreadyCallGoHome) {
                        isAllreadyCallGoHome = true;
                        mHandler.sendEmptyMessageDelayed(GOTO_HOME, 2000);
                    }

                }

                @Override
                public void onLoadingCancelled() {

                }
            });
        } else {
            mHandler.sendEmptyMessageDelayed(GOTO_HOME, 2000);
        }
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsFunction.setScreenName(this, "開場畫面");
        UserDataInfo.activityResumed(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);

        //clear bitmapcontroller
        if (mBitmapController != null) {
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(this);
        }

        //clear vSplashImage
        Drawable drawable = vSplashImage.getDrawable();
        if (Utility.DEBUG)
            Log.e(TAG, "drawable is null or not?? " + (drawable == null ? "true" : "false"));
        drawable.setCallback(null);
        drawable = null;
        vSplashImage.setImageDrawable(null);

        if (Utility.DEBUG)
            Log.e(TAG, "drawable is null or not?? " + (drawable == null ? "true" : "false"));
        if(mApiController!=null){
//            mApiController.removeCallbacks(null);
            mApiController = null;
        }

        if(mHandler!=null){
            mHandler.removeMessages(GOTO_HOME);
            mHandler.removeCallbacks(null);
            mHandler = null;
        }

        if(mPreferencesMethods!=null){
            mPreferencesMethods.unRegistContext(this);
        }

        if(mSplashImage!=null){
            mSplashImage.recycle();
            mSplashImage = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mHandler != null) {
            mHandler.removeMessages(GOTO_HOME);
        }
        super.onBackPressed();
    }

    public void startSplashActivity() {

        changeSharePreferenceTextSize();
        initController();
        processView();
//        mBitmapController.loadImageFromFile(vSplashImage, UserDataInfo.ThumbnailPath + "Splash.jpg", mHandler);
        String thumbnailPath = UserDataInfo.ThumbnailPath + "Splash.jpg";
        Log.d(TAG, "thumbnailPath: " + thumbnailPath);
        Glide.with(this)
                .load(thumbnailPath)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.default_img)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        if (resource != null) {
                            vSplashImage.setImageBitmap(resource);
                        } else {
                            vSplashImage.setImageResource(R.drawable.default_img);
                        }
                        if (!Utility.showNetworkSlowDialog(getApplicationContext(), mHandler, true)) {
                            getSplashImage();
                        }

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {

                        vSplashImage.setImageDrawable(errorDrawable);
                        if (!Utility.showNetworkSlowDialog(getApplicationContext(), mHandler, true)) {
                            getSplashImage();
                        }

                        super.onLoadFailed(e, errorDrawable);
                    }
                });

//        mBitmapController.loadImageWithGlide(thumbnailPath,
//                vSplashImage, BitmapController.IMAGE_SRC, 0, 0, new ImageLoadingListener() {
//                    @Override
//                    public void onLoadingStart(String aImageUrl, View aView) {
//
//                    }
//
//                    @Override
//                    public void onLoadingFailed(String aImageUrl, View aView, Exception aException) {
//
//                    }
//
//                    @Override
//                    public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {
//                        if (aBitmap != null) {
//                            vSplashImage.setImageBitmap(aBitmap);
//                        } else {
//                            vSplashImage.setImageResource(R.drawable.default_img);
//                        }
//                        if (!Utility.showNetworkSlowDialog(getApplicationContext(), mHandler, true)) {
//                            getSplashImage();
//                        }
//                    }
//
//                    @Override
//                    public void onLoadingCancelled() {
//
//                    }
//
//                    @Override
//                    public void onProgressUpdate(String aImageUrl, int aProgress, int max) {
//
//                    }
//                }, 0x568);

    }

}

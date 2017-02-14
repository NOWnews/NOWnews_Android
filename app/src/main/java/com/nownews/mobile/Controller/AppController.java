package com.nownews.mobile.Controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Service.FileDownloadService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
//import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class AppController extends Handler {

    //TODO=================handleMessage=================
    public final static int THREAD_INTERRUPTED_SUCCESS = 0x111;
    public final static int THREAD_INTERRUPTED_EXCEPTION = 0x123;
    public final static int CLEAR_API_SCHEDULER_DONE = 0x321;
    public static BlockingQueue<Runnable> mApiQueue = new LinkedBlockingQueue<Runnable>();
    public static AppController mAppControllerInstance;
    private final String TAG = getClass().getSimpleName();
    public AppScheduler mApiScheduler;
    float density;
    private Context mContext;
    private DisplayMetrics metric;
    private Display display;
    private int scaleW;
    private int scaleH;
    private int cacheW = 320;
    private int cacheH = 533;
    private int cacheW_B = 600;
    private int cacheH_B = 1000;
    private boolean isNeedRestart = false;

    //	public void initImageLoader(boolean setMemoryCacheExtraOptions) {
////		WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
////		display = wm.getDefaultDisplay();
////		
////		metric = new DisplayMetrics();
////		display.getMetrics(metric);
////		DecimalFormat df = new DecimalFormat("0.000");
////		density = metric.density;
////
////		if(Utility.DEBUG)Log.e(TAG, "density: " + density);
////		if(Utility.DEBUG)Log.e(TAG, "metric.widthPixels: " + metric.widthPixels);
////		if(Utility.DEBUG)Log.e(TAG, "metric.heightPixels: " + metric.heightPixels);
////		
////		float dpWidth  = metric.widthPixels / density;
////		float dpHeight = metric.heightPixels / density;
////		if(Utility.DEBUG)Log.e(TAG, "dpWidth: " + dpWidth);
////		if(Utility.DEBUG)Log.e(TAG, "dpHeight: " + dpHeight);
////		
////		cacheW = (int) (dpWidth/2);
////		cacheH = (scaleH*cacheW)/scaleW;
//		
//		boolean isInit = ImageLoader.getInstance().isInited();
//		if(isInit){
//			ImageLoader.getInstance().destroy();
//		}
//		
//		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(mContext);
//		if(setMemoryCacheExtraOptions){
//			config.memoryCacheExtraOptions(cacheW, cacheH);// max width, max height，即保存的每個緩存文件的最大長寬
//		}
////		else{
////			config.memoryCacheExtraOptions(cacheW_B, cacheH_B);
////		}
//		config.threadPoolSize(3);//線程池內加載的數量
//		config.threadPriority(Thread.NORM_PRIORITY - 2);
//		config.denyCacheImageMultipleSizesInMemory();
//		config.memoryCache(new WeakMemoryCache());
//		config.memoryCacheSize(50 * 1024 * 1024);
//		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());//將保存的時候的URI名稱用MD5加密
//		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//		config.tasksProcessingOrder(QueueProcessingType.FIFO);
//		config.writeDebugLogs(); // Remove for release app
//
//		// Initialize ImageLoader with configuration.
//		ImageLoader.getInstance().init(config.build());
//		
//	}
    private Handler mRestartHandler;

    private AppController(Context aContext) {
        this.mContext = aContext;
        startAppScheduler();
//		initImageLoader(false);
    }

    public static AppController getInstance(Context aContext) {
        if (mAppControllerInstance == null) {
            mAppControllerInstance = new AppController(aContext);
        } else {
            mAppControllerInstance.mContext = aContext;
        }
        return mAppControllerInstance;
    }

    public void downloadFileFromUrl(String aUrl, String aFileName, String aFolderPath, Handler aUiHandler) {

        if (Utility.DEBUG) Log.i(TAG, "aUrl: " + aUrl);
        if (Utility.DEBUG) Log.i(TAG, "aFileName: " + aFileName);
        if (Utility.DEBUG) Log.i(TAG, "aFolderPath: " + aFolderPath);

        mApiQueue.add(new FileDownloadService(aUrl, aFileName, aFolderPath, aUiHandler));
    }

    public void stopAppScheduler() {
        if (mApiScheduler != null) {
            mApiScheduler.stopThread();
            mApiScheduler.interrupt();
            if (Utility.DEBUG) Log.e(TAG, "isInterrupted(): " + mApiScheduler.isInterrupted());
            this.sendEmptyMessage(THREAD_INTERRUPTED_SUCCESS);
        }
    }

    public void startAppScheduler() {
        if (mApiScheduler != null) {
            mApiScheduler = null;
        }
        mApiScheduler = new AppScheduler(mApiQueue, this);
        mApiScheduler.start();
    }

    public void clearApiScheduler(Handler aHandler) {
        mRestartHandler = aHandler;
        isNeedRestart = true;
        stopAppScheduler();
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case THREAD_INTERRUPTED_EXCEPTION:
            case THREAD_INTERRUPTED_SUCCESS:
                if (Utility.DEBUG) Log.e(TAG, "THREAD_INTERRUPTED_EXCEPTION");
                if (isNeedRestart) {
                    startAppScheduler();
                    if (mRestartHandler != null) {
                        if (mRestartHandler.hasMessages(CLEAR_API_SCHEDULER_DONE)) {
                            mRestartHandler.removeMessages(CLEAR_API_SCHEDULER_DONE);
                        }
                        mRestartHandler.sendEmptyMessage(CLEAR_API_SCHEDULER_DONE);
                    }
                }
                break;
        }

    }

}

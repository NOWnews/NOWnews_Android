package com.nownews.mobile.Api;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;

import java.util.concurrent.BlockingQueue;

public class ApiScheduler extends Thread {

    private final String TAG = getClass().getSimpleName();

    private BlockingQueue<Runnable> mApiQueue = null;

    private boolean isRunning = true;
    private Handler mHandler;

    public ApiScheduler(BlockingQueue<Runnable> aApiQueue, Handler aHandler) {
        mApiQueue = aApiQueue;
        mHandler = aHandler;
    }

    public void setIsRunning(boolean aIsRunning) {
        isRunning = aIsRunning;
    }

    public void stopThread() {
        isRunning = false;
        mApiQueue.clear();
    }

    public int getApiQueueSize() {
        return mApiQueue.size();
    }

    @Override
    public void run() {
        if (Utility.DEBUG) Log.d(TAG, "API QUEUE IS REDAY !!");

        while (isRunning) {

            Runnable task = null;
            try {
                task = mApiQueue.take();
                if (task != null) {
                    if (Utility.DEBUG)
                        Log.i(TAG, "task start!! " + task.getClass().getSimpleName());
                    task.run();
                    if (Utility.DEBUG)
                        Log.i(TAG, "task finish!! " + task.getClass().getSimpleName());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(ApiController.THREAD_INTERRUPTED_EXCEPTION);
                }
            } finally {
                if (task != null) {
                    task = null;
                }
            }

        }

    }


}

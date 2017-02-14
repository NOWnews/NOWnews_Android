package com.nownews.mobile.Controller;

import android.os.Handler;
import android.util.Log;

import com.nownews.mobile.Common.Utility;

import java.util.concurrent.BlockingQueue;

public class AppScheduler extends Thread {

    private final String TAG = getClass().getSimpleName();

    private BlockingQueue<Runnable> mApiQueue = null;
    private boolean isRunning = true;
    private Handler mHandler;

    public AppScheduler(BlockingQueue<Runnable> aApiQueue, Handler aHandler) {
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

            try {
                Runnable task = mApiQueue.take();
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
                    mHandler.sendEmptyMessage(AppController.THREAD_INTERRUPTED_EXCEPTION);
                }
            }

        }

    }


}

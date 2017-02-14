package com.nownews.mobile.Service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.TrafficStats;
import android.os.IBinder;
import android.util.Log;

import com.nownews.mobile.Common.Utility;

import java.util.Timer;
import java.util.TimerTask;

public class DetectNetworkFlowService extends Service {

    private final String TAG = getClass().getSimpleName();
    private Timer mProgressTimer;
    private long new_KB = 0;
    private long old_KB = 0;
    private long now_KB = 0;
    private TimerTask mProgressTask = new TimerTask() {

        @Override
        public void run() {

            now_KB = getUidRxBytes();
            new_KB = now_KB - old_KB;
            old_KB = now_KB;
//			if(mProgressHandler!=null){
//				Message message = new Message();
//				message.what = 0;
//				message.obj = new_KB;
//				mProgressHandler.sendMessage(message);
//			}

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (Utility.DEBUG) Log.e(TAG, "onStart");
        startProgressTimer();
    }

    private void startProgressTimer() {

        if (mProgressTimer == null) {
            mProgressTimer = new Timer();
        }
        mProgressTimer.schedule(mProgressTask, 1000, 5000);

    }

    public long getUidRxBytes() { // 获取总的接受字节数，包含Mobile和WiFi等

        ApplicationInfo ai = getApplicationInfo();
        if (TrafficStats.getUidRxBytes(ai.uid) != TrafficStats.UNSUPPORTED) {
            long TotalRxKB = (TrafficStats.getTotalRxBytes() / 1024);
            long MobileRxKB = (TrafficStats.getMobileRxBytes() / 1024);
            long UidRxKB = (TrafficStats.getUidRxBytes(ai.uid) / 1024);
            if (Utility.DEBUG) Log.e(TAG, "-------------------------------");
            if (Utility.DEBUG) Log.w(TAG, "TotalRxKB: " + TotalRxKB);
            if (Utility.DEBUG) Log.w(TAG, "MobileRxKB: " + MobileRxKB);
            if (Utility.DEBUG) Log.w(TAG, "UidRxKB: " + UidRxKB);
            return TotalRxKB;
        } else {
            return 0;
        }

    }

//	private Handler mProgressHandler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			
//			switch(msg.what){
//			case 0:
//				long s_KB = (Long) msg.obj;
//				String hint = getString(R.string.progress_hint);
//				mProgressHint.setText(String.format(hint, String.valueOf(s_KB)));
//				break;
//			}
//			
//		}
//		
//		
//	};

    @Override
    public void onDestroy() {
        if (mProgressTask != null) {
            mProgressTask.cancel();
        }
        super.onDestroy();
    }

}

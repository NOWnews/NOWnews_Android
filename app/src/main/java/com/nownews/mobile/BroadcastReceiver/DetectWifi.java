package com.nownews.mobile.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;

public class DetectWifi extends BroadcastReceiver {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    private String TAG = "DetectWifi";
    private Context mContext;

    private boolean network;
    private int bandwidth;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        String action = intent.getAction();

        if (Utility.DEBUG) Log.e(TAG, "action: " + action);

        if ((action.equals(ConnectivityManager.CONNECTIVITY_ACTION))) {

            if (UserDataInfo.isActivityVisible()) {
                boolean connectStatus = Utility.getConnectivityStatus(mContext);
                if (!connectStatus) {
                    Utility.openNetworkErrorDialog();
                } else {
                    if (Utility.mNetworkErrordialog != null && Utility.mNetworkErrordialog.isShowing()) {
                        Utility.mNetworkErrordialog.cancel();
                    }
                    //reload當前畫面
                    Utility.reloadActivity();
                }
            }

        }

    }
}


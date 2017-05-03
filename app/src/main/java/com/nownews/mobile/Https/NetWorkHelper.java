package com.nownews.mobile.Https;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetWorkHelper {

    private static String LOG_TAG = "NetWorkHelper";

    public static Uri uri = Uri.parse("content://telephony/carriers");

    /**
     * 判斷是否有網路連線
     */
    public static boolean checkNetworkConnected(Context context) {
        boolean result = false;
        ConnectivityManager CM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (CM == null) {
            result = false;
        } else {
            NetworkInfo info = CM.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (!info.isAvailable()) {
                    result = false;
                } else {
                    result = true;
                }
                Log.d(LOG_TAG, "[目前連線方式]" + info.getTypeName());
                Log.d(LOG_TAG, "[目前連線狀態]" + info.getState());
                Log.d(LOG_TAG, "[目前網路是否可使用]" + info.isAvailable());
                Log.d(LOG_TAG, "[網路是否已連接]" + info.isConnected());
                Log.d(LOG_TAG, "[網路是否已連接 或 連線中]" + info.isConnectedOrConnecting());
                Log.d(LOG_TAG, "[網路目前是否有問題 ]" + info.isFailover());
                Log.d(LOG_TAG, "[網路目前是否在漫遊中]" + info.isRoaming());
            }
        }
        return result;
    }

    public static boolean checkNetState(Context context) {
        boolean netstate = false;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }

    /**
     * 判斷是否為漫遊
     */
    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(LOG_TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null
                    && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null && tm.isNetworkRoaming()) {
                    Log.d(LOG_TAG, "network is roaming");
                    return true;
                } else {
                    Log.d(LOG_TAG, "network is not roaming");
                }
            } else {
                Log.d(LOG_TAG, "not using mobile network");
            }
        }
        return false;
    }

    /**
     * 判斷MOBILE網路是否可用
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static boolean isMobileDataEnable(Context context) throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileDataEnable = false;

        isMobileDataEnable = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

        return isMobileDataEnable;
    }

    /**
     * 判斷wifi 是否可用
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static boolean isWifiDataEnable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isWifiDataEnable = false;
            isWifiDataEnable = connectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
            return isWifiDataEnable;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 設置Mobile網路開關
     *
     * @param context
     * @param enabled
     * @throws Exception
     */
    // public static void setMobileDataEnabled(Context context, boolean enabled)
    // throws Exception {
    // APNManager apnManager = APNManager.getInstance(context);
    // List<APN> list = apnManager.getAPNList();
    // if (enabled) {
    // for (APN apn : list) {
    // ContentValues cv = new ContentValues();
    // cv.put("apn", apnManager.matchAPN(apn.apn));
    // cv.put("type", apnManager.matchAPN(apn.type));
    // context.getContentResolver().update(uri, cv, "_id=?",
    // new String[] { apn.apnId });
    // }
    // } else {
    // for (APN apn : list) {
    // ContentValues cv = new ContentValues();
    // cv.put("apn", apnManager.matchAPN(apn.apn) + "mdev");
    // cv.put("type", apnManager.matchAPN(apn.type) + "mdev");
    // context.getContentResolver().update(uri, cv, "_id=?",
    // new String[] { apn.apnId });
    // }
    // }
    // }

}

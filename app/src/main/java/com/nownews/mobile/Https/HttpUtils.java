package com.nownews.mobile.Https;

import android.content.Context;
import android.util.Log;

import com.nownews.mobile.Baselibs.RequestCacheUtil;


public class HttpUtils {

    public static String postByHttpURLConnection(String authorization, String strUrl, String value, RequestCacheUtil.Method method) {
        String result = "";
        if (method == RequestCacheUtil.Method.POST) {
            result = CustomHttpURLConnection.PostFromWebByHttpURLConnection(authorization, strUrl,
                    value, method);
        } else if (method == RequestCacheUtil.Method.GET || method == RequestCacheUtil.Method.DELETE) {
            result = CustomHttpURLConnection.GetFromWebByHttpURLConnection(authorization, strUrl, method);
        } else if (method == RequestCacheUtil.Method.PUT) {
            result = CustomHttpURLConnection.PutFromWebByHttpURLConnection(authorization, strUrl, value);
        }
        return result;
    }

    // 判斷mobile網路是否可用
    public static boolean isMobileDataEnable(Context context) {
        String TAG = "httpUtils.isMobileDataEnable()";
        try {
            return NetWorkHelper.isMobileDataEnable(context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 判斷wifi是否可用
    public static boolean isWifiDataEnable(Context context) {
        String TAG = "httpUtils.isWifiDataEnable()";
        try {
            return NetWorkHelper.isWifiDataEnable(context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 判斷是否為漫遊
    public static boolean isNetworkRoaming(Context context) {
        return NetWorkHelper.isNetworkRoaming(context);
    }
}

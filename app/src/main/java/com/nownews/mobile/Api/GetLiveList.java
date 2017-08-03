package com.nownews.mobile.Api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.CheckVersionJson;
import com.nownews.mobile.Json.LiveListJson;

import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class GetLiveList implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;
    public final static String KEY_VERSION = "X-NOWnewsAPP-Version";
    public final static String KEY_OS = "X-NOWnewsAPP-OS";
    public final static String KEY_MODE = "X-NOWnewsAPP-Mode";

    public GetLiveList(Handler aHandler) {
        mHandler = aHandler;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();
        Message message = new Message();

        try {

            String webApiUrl = WebAPIUrl.LIVE_LIST;

            ConcurrentHashMap<String, String> headerMap = null;
            headerMap = new ConcurrentHashMap<>();
            headerMap.put(KEY_VERSION, Utility.getAppVersionName(Utility.getApplicationContext()));
            headerMap.put(KEY_OS, "ANDROID");
            headerMap.put(KEY_MODE, (Utility.DEBUG? "develop":"production"));

            String jsonValue = WebApi.DoGet(webApiUrl, true, null, headerMap);
//			Log.e(TAG, "jsonValue: " + jsonValue);
            LiveListJson jsonValueClb = new Gson().fromJson(jsonValue, LiveListJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = jsonValueClb;
            message.what = ParameterSet.GET_LIVE_LIST_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_LIVE_LIST_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_LIVE_LIST_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_LIVE_LIST_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

}

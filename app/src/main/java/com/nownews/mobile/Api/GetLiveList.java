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

public class GetLiveList implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;

    public GetLiveList(Handler aHandler) {
        mHandler = aHandler;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();

        try {

            String webApiUrl = WebAPIUrl.LIVE_LIST;

            String jsonValue = WebApi.DoGet(webApiUrl, false);
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

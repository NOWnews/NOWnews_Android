package com.nownews.mobile.Api;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.SearchInfoJson;

import java.net.SocketTimeoutException;

public class GetSearchInfo implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;
    private String mKeyWords;
    private int mPage;

    public GetSearchInfo(Handler aHandler, String aKeyWords, int aPage) {
        mHandler = aHandler;
        mKeyWords = aKeyWords;
        mPage = aPage;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();
        Message message = new Message();

        try {

            String webApiUrl = WebAPIUrl.SEARCH;
            webApiUrl = String.format(webApiUrl, mKeyWords, mPage);

            String jsonValue = WebApi.DoGet(webApiUrl, true);
            if (Utility.DEBUG) Log.e(TAG, "jsonValue: " + jsonValue);
            SearchInfoJson jsonValueClb = new Gson().fromJson(jsonValue, SearchInfoJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = jsonValueClb.getNewsList();
            message.what = ParameterSet.GET_SEARCH_INFO_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_SEARCH_INFO_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_SEARCH_INFO_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_SEARCH_INFO_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

}

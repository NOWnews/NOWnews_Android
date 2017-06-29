package com.nownews.mobile.Api;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsCategoryJson;
import com.nownews.mobile.Json.NewsCategoryJson.CategoryInfo;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetNewsCategory implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;

    public GetNewsCategory(Handler aHandler) {
        mHandler = aHandler;
    }

    public void setData(Handler aHandler){
        mHandler = aHandler;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();

        try {

            String webApiUrl = WebAPIUrl.NEWS_CATEGORY;

            String jsonValue = WebApi.DoGet(webApiUrl, true);
            jsonValue = "{\"newsCategory\":" + jsonValue + "}";
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            NewsCategoryJson jsonValueClb = new Gson().fromJson(jsonValue, NewsCategoryJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj =jsonValueClb.getCategoryInfo();
            message.what = ParameterSet.GET_NEWS_CATEGORY_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_NEWS_CATEGORY_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_NEWS_CATEGORY_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_NEWS_CATEGORY_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
//            ApiController.mApiControllerInstance.getApiQueueSize();
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }
}

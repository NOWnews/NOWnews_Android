package com.nownews.mobile.Api;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.nownews.R;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.SpecialNewsListJson;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetSpecialNewsList implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;
    private int mNewsId;

    public GetSpecialNewsList(Handler aHandler, int aNewsId) {
        mHandler = aHandler;
        mNewsId = aNewsId;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();
        Message message = new Message();

        try {

            String webApiUrl = WebAPIUrl.SPECIAL_NEWS_LIST;
            webApiUrl = String.format(webApiUrl, mNewsId);

            String jsonValue = WebApi.DoGet(webApiUrl, true);
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            SpecialNewsListJson jsonValueClb = new Gson().fromJson(jsonValue, SpecialNewsListJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = jsonValueClb.getNewsList();
            message.what = ParameterSet.GET_SPECIAL_NEWS_LIST_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_SPECIAL_NEWS_LIST_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_SPECIAL_NEWS_LIST_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_SPECIAL_NEWS_LIST_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

}

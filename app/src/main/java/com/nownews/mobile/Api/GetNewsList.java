package com.nownews.mobile.Api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GetNewsList implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;
    private int mLimit = -1;
    private int mPage = -1;
    private String mUrl;
    public final static String KEY_LIMIT = "limit";
    public final static String KEY_PAGE = "page";

    public GetNewsList(Handler aHandler, String aUrl, int aLimit, int aPage) {
        mHandler = aHandler;
        mUrl = aUrl;
        mLimit = aLimit;
        mPage = aPage;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();
        Message message = new Message();

        String webApiUrl = WebAPIUrl.HOST + mUrl;

        try {

            ConcurrentHashMap<String, String> queryMap = null;
            if(mLimit!=-1 && mPage!=-1){
                queryMap = new ConcurrentHashMap<>();
                queryMap.put(KEY_LIMIT, String.valueOf(mLimit));
                queryMap.put(KEY_PAGE, String.valueOf(mPage));
            }

            String jsonValue = WebApi.DoGet(webApiUrl, true, queryMap);
            if (Utility.DEBUG) Log.e(TAG, "jsonValue: " + jsonValue);
            NewsListJson jsonValueClb = new Gson().fromJson(jsonValue, NewsListJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = jsonValueClb.getNewsList();
            message.what = ParameterSet.GET_NEWS_LIST_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_NEWS_LIST_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_NEWS_LIST_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_NEWS_LIST_FAILED \n ApiUrl: " + webApiUrl);
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
//            ApiController.mApiControllerInstance.getApiQueueSize();
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

}

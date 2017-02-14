package com.nownews.mobile.Api;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.NewsListJson.NewsContent;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetInstantNews implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;

    public GetInstantNews(Handler aHandler) {
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

            String webApiUrl = WebAPIUrl.INSTANT_NEWS;

            String jsonValue = WebApi.DoGet(webApiUrl, true);
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            NewsListJson jsonValueClb = new Gson().fromJson(jsonValue, NewsListJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = checkNull(jsonValueClb.newsList);
            message.what = ParameterSet.GET_INSTANT_NEWS_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_INSTANT_NEWS_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_INSTANT_NEWS_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_INSTANT_NEWS_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
            ApiController.mApiControllerInstance.getApiQueueSize();
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

    private List<NewsContent> checkNull(List<NewsContent> aData) {
        Iterator<NewsContent> iterator = aData.iterator();
        while (iterator.hasNext()) {
            NewsContent content = iterator.next();
            if (content.field_short_title == null
                    || content.field_short_title.value == null
                    || content.field_short_title.value.trim().isEmpty()
                    || content.image == null
                    || content.image.originImage == null
                    || content.image.originImage.trim().isEmpty()) {
                iterator.remove();
            }
        }
        return aData;
    }
}

package com.nownews.mobile.Api;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.VideosCategoryJson;
import com.nownews.mobile.Json.VideosCategoryJson.CategoryInfo;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetVideosCategory implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;

    public GetVideosCategory(Handler aHandler) {
        mHandler = aHandler;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();
        Message message = new Message();

        try {

            String webApiUrl = WebAPIUrl.VIDEOS_CATEGORY;

            String jsonValue = WebApi.DoGet(webApiUrl, true);
            jsonValue = "{\"videosCategory\":" + jsonValue + "}";
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            VideosCategoryJson jsonValueClb = new Gson().fromJson(jsonValue, VideosCategoryJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = checkNull(jsonValueClb.videosCategory);
            message.what = ParameterSet.GET_VIDEOS_CATEGORY_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_VIDEOS_CATEGORY_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_VIDEOS_CATEGORY_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_VIDEOS_CATEGORY_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

    private List<CategoryInfo> checkNull(List<CategoryInfo> aData) {
        Iterator<CategoryInfo> iterator = aData.iterator();
        while (iterator.hasNext()) {
            CategoryInfo content = iterator.next();
            if (content.tid == -1
                    || content.name == null
                    || content.name.trim().equals("")) {
                iterator.remove();
            }
        }
        return aData;
    }
}

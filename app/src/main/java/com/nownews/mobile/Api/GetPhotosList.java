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
import com.nownews.mobile.Json.PhotosListJson;
import com.nownews.mobile.Json.PhotosListJson.PhotosContent;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetPhotosList implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;
    private int mNewsId;
    private int mPage;

    public GetPhotosList(Handler aHandler, int aNewsId, int aPage) {
        mHandler = aHandler;
        mNewsId = aNewsId;
        mPage = aPage;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();

        try {

            String webApiUrl = WebAPIUrl.PHOTOS_LIST;
            webApiUrl = String.format(webApiUrl, mNewsId, mPage);

            String jsonValue = WebApi.DoGet(webApiUrl, true);
            jsonValue = "{\"photosList\":" + jsonValue + "}";
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            PhotosListJson jsonValueClb = new Gson().fromJson(jsonValue, PhotosListJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = checkNull(jsonValueClb.photosList);
            message.what = ParameterSet.GET_PHOTOS_LIST_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_PHOTOS_LIST_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_PHOTOS_LIST_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_PHOTOS_LIST_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

    private List<PhotosContent> checkNull(List<PhotosContent> aData) {

        Iterator<PhotosContent> iterator = aData.iterator();
        while (iterator.hasNext()) {
            PhotosContent content = iterator.next();
            if (content.nodeId == -1
                    || content.title == null
                    || content.title.trim().isEmpty()
                    || content.thumbnail == null
                    || content.thumbnail.trim().isEmpty()) {
                iterator.remove();
            }
        }

        return aData;
    }

}

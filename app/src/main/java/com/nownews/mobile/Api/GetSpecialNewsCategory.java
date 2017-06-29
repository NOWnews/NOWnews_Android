package com.nownews.mobile.Api;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.SpecialNewsCategoryJson;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;

public class GetSpecialNewsCategory implements Runnable {

    private final String TAG = getClass().getSimpleName();
    private Handler mHandler;

    public GetSpecialNewsCategory(Handler aHandler) {
        mHandler = aHandler;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
        Message message = ApiController.mApiControllerInstance.mHandler.obtainMessage();

        try {

            String webApiUrl = WebAPIUrl.SPECIAL_NEWS_CATEGORY;

            String jsonValue = WebApi.DoGet(webApiUrl, true);
//			if(Utility.DEBUG)Log.e(TAG, "jsonValue: " + jsonValue);
            SpecialNewsCategoryJson jsonValueClb = new Gson().fromJson(jsonValue, SpecialNewsCategoryJson.class);

            Utility.writeJsonToFile(TAG, jsonValue);

            message.obj = checkNull(jsonValueClb.getSpecialChannels());
            message.what = ParameterSet.GET_SPECIAL_NEWS_CATEGORY_DONE;

            if (Utility.DEBUG) Log.v(TAG, "GET_SPECIAL_NEWS_CATEGORY_DONE");

        } catch (SocketTimeoutException ex) {

            message.what = ParameterSet.SOCKET_TIME_OUT;
            if (Utility.DEBUG) Log.e(TAG, "SOCKET_TIME_OUT");
            ex.printStackTrace();

        } catch (Exception ex) {

            message.what = ParameterSet.GET_SPECIAL_NEWS_CATEGORY_FAILED;
            if (Utility.DEBUG) Log.e(TAG, "GET_SPECIAL_NEWS_CATEGORY_FAILED");
            ex.printStackTrace();

        } finally {
            if (mHandler != null)
                mHandler.sendMessage(message);
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

    private List<SpecialNewsCategoryJson.SpecialChannelsBean> checkNull(List<SpecialNewsCategoryJson.SpecialChannelsBean> specialChannels) {

        Iterator<SpecialNewsCategoryJson.SpecialChannelsBean> iterator = specialChannels.iterator();
        while (iterator.hasNext()) {
            SpecialNewsCategoryJson.SpecialChannelsBean content = iterator.next();
            if (content.getSn() == -1
                    || content.getTitle() == null
                    || content.getTitle().trim().isEmpty()
                    || content.getNewsList() == null
                    || content.getNewsList().size() == 0) {
                iterator.remove();
            }else if (content!=null && content.getNewsList()!=null) {
                Iterator<String> newsList = content.getNewsList().iterator();
                while (newsList.hasNext()){
                    String id = newsList.next();
                    if(id==null){
                        newsList.remove();
                    }
                }
                if(content.getNewsList().size()==0){
                    iterator.remove();
                }
            }
        }
        return specialChannels;

    }

}

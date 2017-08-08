package com.nownews.mobile.Service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.HeadlineNewsJson;
import com.nownews.mobile.Json.InstantNewsJson;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetApiService extends Service {

    private final String TAG = getClass().getSimpleName();
    private ApiController mApiController;
    private ApiHandler mHandler;

    public GetApiService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(TAG, TAG + " onHandleIntent");

        initController();
        getInstantNews();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initController(){
        mApiController = ApiController.getInstance();
        mHandler = new ApiHandler(this);

    }

    private void getInstantNews(){
        if(mApiController!=null){
            mApiController.getInstantNews(mHandler);
        }
    }

    private boolean isGetInstantNewsDone;
    private static class ApiHandler extends Handler {

        private final WeakReference<GetApiService> mService;

        public ApiHandler(GetApiService aFragment){
            mService = new WeakReference<GetApiService>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            GetApiService service = mService.get();

            switch (msg.what) {
                case ParameterSet.GET_INSTANT_NEWS_DONE:
                    List<InstantNewsJson.NewsListBean> instantList = (List<InstantNewsJson.NewsListBean>)msg.obj;
                    UserDataInfo.setInstantNewsContent(instantList);
                    service.isGetInstantNewsDone = true;
                    service.checkStatus();
                    break;
                case ParameterSet.GET_INSTANT_NEWS_FAILED:
                    //TODO [v4] Error process 錯誤處理
                    service.isGetInstantNewsDone = true;
                    service.checkStatus();
                    break;
            }

        }

    };

    private void checkStatus(){
        if(isGetInstantNewsDone){
            if(Utility.DEBUG)Log.v(TAG, "checkStatus()");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if(Utility.DEBUG)Log.v(TAG, "onDestroy()");
        if(mHandler!=null){
            mHandler.removeCallbacks(null);
            mHandler = null;
        }
        super.onDestroy();
    }
}

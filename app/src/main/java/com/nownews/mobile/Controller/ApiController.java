package com.nownews.mobile.Controller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nownews.mobile.Api.GetHeadlineNews;
import com.nownews.mobile.Api.GetHotNews;
import com.nownews.mobile.Api.GetInstantNews;
import com.nownews.mobile.Api.GetLiveInfo;
import com.nownews.mobile.Api.GetLiveList;
import com.nownews.mobile.Api.GetNearByNews;
import com.nownews.mobile.Api.GetNewsCategory;
import com.nownews.mobile.Api.GetNewsInfo;
import com.nownews.mobile.Api.GetNewsList;
import com.nownews.mobile.Api.GetPhotosCategory;
import com.nownews.mobile.Api.GetPhotosInfo;
import com.nownews.mobile.Api.GetPhotosList;
import com.nownews.mobile.Api.GetRelationsNewsInfo;
import com.nownews.mobile.Api.GetSearchInfo;
import com.nownews.mobile.Api.GetSpecialNewsCategory;
import com.nownews.mobile.Api.GetSpecialNewsList;
import com.nownews.mobile.Api.GetSplashImage;
import com.nownews.mobile.Api.GetVideosCategory;
import com.nownews.mobile.Api.GetVideosInfo;
import com.nownews.mobile.Api.GetVideosList;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Service.SignalService;

public class ApiController {

    //TODO=================handleMessage=================
    public final static int THREAD_INTERRUPTED_SUCCESS = 0x111;
    public final static int THREAD_INTERRUPTED_EXCEPTION = 0x123;
    public final static int CLEAR_API_SCHEDULER_DONE = 0x321;
    public final static int RESTART_API_SCHEDULER = 0x428;
    public static ApiController mApiControllerInstance;
    private final String TAG = getClass().getSimpleName();
    private boolean isNeedRestart = false;

    private ApiController() {
    }

    public static ApiController getInstance() {
        if (mApiControllerInstance == null) {
            mApiControllerInstance = new ApiController();
        }
        return mApiControllerInstance;
    }

    private void startNetworkSignalService(Context aContext) {
        Intent intent = new Intent();
        intent.setClass(aContext, SignalService.class);
        aContext.startService(intent);
    }

    /**
     * 取得頭條新聞
     */
    private GetHeadlineNews mGetHeadlineNews;
    public void getHeadlineNews(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getHeadlineNews called");
        if(mGetHeadlineNews==null){
            mGetHeadlineNews = new GetHeadlineNews(aHandler);
            if (Utility.DEBUG) Log.w(TAG, "mGetHeadlineNews this task is NULL");
        }else{
            if (Utility.DEBUG) Log.w(TAG, "mGetHeadlineNews this task is NOT null");
            mGetHeadlineNews.setData(aHandler);
        }
        new Thread(mGetHeadlineNews).start();
    }

    /**
     * 取得熱門新聞
     */
    private GetHotNews mGetHotNews;
    public void getHotNews(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getHotNews called");
        if(mGetHotNews==null){
            mGetHotNews = new GetHotNews(aHandler);
            if (Utility.DEBUG) Log.w(TAG, "mGetHotNews this task is NULL");
        }else{
            if (Utility.DEBUG) Log.w(TAG, "mGetHotNews this task is NOT null");
            mGetHotNews.setData(aHandler);
        }
        new Thread(mGetHotNews).start();
    }

    /**
     * 取得及時新聞
     */
    private GetInstantNews mGetInstantNews;
    public void getInstantNews(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getInstantNews called");
        if(mGetInstantNews==null){
            mGetInstantNews = new GetInstantNews(aHandler);
            if (Utility.DEBUG) Log.w(TAG, "getInstantNews this task is NULL");
        }else{
            if (Utility.DEBUG) Log.w(TAG, "getInstantNews this task is NOT null");
            mGetInstantNews.setData(aHandler);
        }
        new Thread(mGetInstantNews).start();
    }

    /**
     * 取得附近的人在看
     */
    private GetNearByNews mGetNearByNews;
    public void getNearByNews(Handler aHandler, double aLongitude, double aLatitude) {
        if (Utility.DEBUG) Log.i(TAG, "getNearByNews called");
        if(mGetNearByNews==null){
            mGetNearByNews = new GetNearByNews(aHandler, aLongitude, aLatitude);
            if (Utility.DEBUG) Log.w(TAG, "mGetNearByNews this task is NULL");
        }else{
            if (Utility.DEBUG) Log.w(TAG, "mGetNearByNews this task is NOT null");
            mGetNearByNews.setData(aHandler, aLongitude, aLatitude);
        }
        new Thread(mGetNearByNews).start();
    }
    //region 新聞系列

    /**
     * 取得大分類
     */
    private GetNewsCategory mGetNewsCategory;
    public void getNewsCategory(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getNewsCategory called");
        if(mGetNewsCategory==null){
            mGetNewsCategory = new GetNewsCategory(aHandler);
            if (Utility.DEBUG) Log.w(TAG, "mGetNewsCategory this task is NULL");
        }else{
            if (Utility.DEBUG) Log.w(TAG, "mGetNewsCategory this task is NOT null");
            mGetNewsCategory.setData(aHandler);
        }
        new Thread(mGetNewsCategory).start();
    }

    /**
     * 取得新聞列表
     */
    public void getNewsList(Handler aHandler, String aUrl, int aLimit, int aPage) {
        if (Utility.DEBUG) Log.i(TAG, "getNewsList called");
        new Thread(new GetNewsList(aHandler, aUrl, aLimit, aPage)).start();
    }

    /**
     * 取得新聞內容
     */
    public void getNewsInfo(Handler aHandler, int aNewsId) {
        if (Utility.DEBUG) Log.i(TAG, "getNewsInfo called");
        new Thread(new GetNewsInfo(aHandler, aNewsId)).start();
    }

    /**
     * 取得相關新聞內容
     */
    public void getRelationsNewsInfo(Handler aHandler, int aNewsId) {
        if (Utility.DEBUG) Log.i(TAG, "getNewsInfo called");
        new Thread(new GetRelationsNewsInfo(aHandler, aNewsId)).start();
    }
    //endregion

    //region 圖集系列

    /**
     * 取得圖集大分類
     */
    public void getPhotosCategory(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getPhotosCategory called");
        new Thread(new GetPhotosCategory(aHandler)).start();
    }

    /**
     * 取得圖集列表
     */
    public void getPhotosList(Handler aHandler, int aNewsId, int aPage) {
        if (Utility.DEBUG) Log.i(TAG, "getPhotosList called");
        new Thread(new GetPhotosList(aHandler, aNewsId, aPage)).start();
    }

    /**
     * 取得圖集內容
     */
    public void getPhotosInfo(Handler aHandler, int aNewsId) {
        if (Utility.DEBUG) Log.i(TAG, "getPhotosInfo called");
        new Thread(new GetPhotosInfo(aHandler, aNewsId)).start();
    }
    //endregion

    //region 影音系列

    /**
     * 取得影音大分類
     */
    public void getVideosCategory(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getVideosCategory called");
        new Thread(new GetVideosCategory(aHandler)).start();
    }

    /**
     * 取得影音列表
     */
    public void getVideosList(Handler aHandler, int aNewsId, int aPage) {
        if (Utility.DEBUG) Log.i(TAG, "getVideosList called");
        new Thread(new GetVideosList(aHandler, aNewsId, aPage)).start();
    }

    /**
     * 取得影音內容
     */
    public void getVideosInfo(Handler aHandler, int aNewsId) {
        if (Utility.DEBUG) Log.i(TAG, "getVideosInfo called");
        new Thread(new GetVideosInfo(aHandler, aNewsId)).start();
    }
    //endregion

    /**
     * 取得特輯大分類
     */
    public void getSpecialNewsCategory(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getSpecialNewsCategory called");
        new Thread(new GetSpecialNewsCategory(aHandler)).start();
    }

    /**
     * 取得特輯列表
     */
    public void getSpecialNewsList(Handler aHandler, int aNewsCategoryId) {
        if (Utility.DEBUG) Log.i(TAG, "getSpecialNewsList called");
        new Thread(new GetSpecialNewsList(aHandler, aNewsCategoryId)).start();
    }

    /**取得開場圖*/
    public void getSplashImage(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getSplashImage called");
        new Thread(new GetSplashImage(aHandler)).start();
    }

    /**取得搜尋資訊*/
    public void getSearchInfo(Handler aHandler, String aKeyWords, int aPage) {
        if (Utility.DEBUG) Log.i(TAG, "getSearchInfo called");
        new Thread(new GetSearchInfo(aHandler, aKeyWords, aPage)).start();
    }

    /**取得直播資訊*/
    public void getLiveList(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getLiveList called");
        new Thread(new GetLiveList(aHandler)).start();
    }

    /**取得KMT直播資訊*/
    public void getLiveInfo(Handler aHandler) {
        if (Utility.DEBUG) Log.i(TAG, "getLiveInfo called");
        new Thread(new GetLiveInfo(aHandler)).start();
    }

    public Handler mHandler = new Handler();

}

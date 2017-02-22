package com.nownews.mobile.Common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.comscore.Analytics;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.Json.NewsCategoryJson.CategoryInfo;
import com.nownews.mobile.Json.NewsInfoJson.ReferenceNewsInfo;
import com.nownews.mobile.Json.NewsListJson.NewsContent;
import com.nownews.mobile.Json.PhotosCategoryJson;
import com.nownews.mobile.Json.SearchInfoJson.SearchInfoContent;
import com.nownews.mobile.Json.VideosCategoryJson;
import com.nownews.mobile.Json.VideosListJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UserDataInfo {

    public final static String PROJECT_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/NOWnews/";
    public final static String JSON_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/NOWnews/JS/";
    public final static String ThumbnailPath = PROJECT_FOLDER_PATH + "thumbnail/";
    //    public final static String SENDER_ID = "399608812512"; //old
    public final static String SENDER_ID = "581106378067";
    public final static String WECHAT_APP_ID = "wx808d87ccb283e62d";
    ;
    public final static String YOUTUBE_DEVELOPER_KEY = "AIzaSyAC9YhsrjGVszAjIxdxrHJJR8Jgwn3KU4k";
    public final static String WEIBO_APP_KEY = "2417128385";
    private static final String TAG = "UserDataInfo";
    private static final boolean DEBUG = false;
    public static boolean isVersionDialogShow;
    public static int mPageSwapCount;
    public static int mHomeDFPCount;
    public static boolean isSingalNewsFromAction;
    public static List<PhotosCategoryJson.CategoryInfo> mPhotosCategoryContent;
    public static List<VideosCategoryJson.CategoryInfo> mVideosCategoryContent;
    private static Context mCurrentContext;
    private static List<CategoryInfo> mNewsCategoryContent;
    private static List<NewsContent> mHeadlineContent;
    private static List<NewsContent> mHotNewsContent;
    private static List<NewsContent> mInstantNewsContent;
    private static List<ReferenceNewsInfo> mReferenceNewsList;
    private static List<NewsContent> mNewsList;
    private static List<VideosListJson.VideosContent> mVideoNewsList;
    private static List<SearchInfoContent> mSearchList;
    private static ArrayList<Integer> mDisplayIndexList;
    private static boolean activityVisible;
    private static List<LiveListJson.Data> mLiveList;

    public static void setLiveList(List<LiveListJson.Data> aLiveList){
        mLiveList = aLiveList;
    }

    public static List<LiveListJson.Data> getLiveList(){
        return mLiveList;
    }

    /**
     * 判斷Android系统SDK的版本，我們一般用currentVersion < android.os.Build.VERSION_CODES.FROYO
     * 的方式進行判斷是2.2以下版本
     *
     * @return
     */
    public static int getSdkVersion() {
        // See http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion;
    }

    public static Context getCurrentContext() {
        return mCurrentContext;
    }

    public static void setCurrentContext(Activity mCurrentActivity) {
        UserDataInfo.mCurrentContext = mCurrentActivity;
    }

    public static void AppExit(Context context) {
        try {
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public static List<CategoryInfo> getNewsCategoryContent() {
        return mNewsCategoryContent;
    }

    public static void setNewsCategoryContent(List<CategoryInfo> aNewsCategoryContent) {
        mNewsCategoryContent = aNewsCategoryContent;
    }

    public static List<PhotosCategoryJson.CategoryInfo> getPhotosCategoryContent() {
        return mPhotosCategoryContent;
    }

    public static void setPhotosCategoryContent(List<PhotosCategoryJson.CategoryInfo> aPhotosCategoryContent) {
        mPhotosCategoryContent = aPhotosCategoryContent;
    }

    public static List<VideosCategoryJson.CategoryInfo> getVideosCategoryContent() {
        return mVideosCategoryContent;
    }

    public static void setVideosCategoryContent(List<VideosCategoryJson.CategoryInfo> aVideosCategoryContent) {
        mVideosCategoryContent = aVideosCategoryContent;
    }

    public static List<NewsContent> getNewsList() {
        return mNewsList;
    }

    public static void setNewsList(List<NewsContent> aNewsList) {
        mNewsList = aNewsList;
    }

    public static List<VideosListJson.VideosContent> getVideoNewsList() {
        return mVideoNewsList;
    }

    public static void setVideoNewsList(List<VideosListJson.VideosContent> aVideoNewsList) {
        mVideoNewsList = aVideoNewsList;
    }

    public static List<SearchInfoContent> getSearchList() {
        return mSearchList;
    }

    public static void setSearchList(List<SearchInfoContent> aSearchList) {
        mSearchList = aSearchList;
    }

    public static List<NewsContent> getHeadlineContent() {
        return mHeadlineContent;
    }

    public static void setHeadlineContent(List<NewsContent> aHeadlineContent) {
        UserDataInfo.mHeadlineContent = aHeadlineContent;
    }

    public static ArrayList<Integer> getDisplayIndexList() {
        return mDisplayIndexList;
    }

    public static void setDisplayIndexList(ArrayList<Integer> aDisplayIndexList) {
        mDisplayIndexList = aDisplayIndexList;
    }

    public static List<NewsContent> getHotNewsContent() {
        return mHotNewsContent;
    }

    public static void setHotNewsContent(List<NewsContent> aHotNewsContent) {
        UserDataInfo.mHotNewsContent = aHotNewsContent;
    }

    public static List<NewsContent> getInstantNewsContent() {
        return mInstantNewsContent;
    }

    public static void setInstantNewsContent(List<NewsContent> aInstantNewsContent) {
        UserDataInfo.mInstantNewsContent = aInstantNewsContent;
    }

    public static List<ReferenceNewsInfo> getReferenceNewsList() {
        return mReferenceNewsList;
    }

    public static void setReferenceNewsList(List<ReferenceNewsInfo> aHotNewsContent) {
        UserDataInfo.mReferenceNewsList = aHotNewsContent;
    }

    public static boolean isActivityVisible() {
        if (Utility.DEBUG) Log.e(TAG, "activityVisible: " + activityVisible);
        return activityVisible;
    }

    public static void activityResumed(Activity aContext) {
        if (Utility.DEBUG) Log.e(TAG, "activityResumed() " + aContext.getClass().getSimpleName());
        setCurrentContext(aContext);
        activityVisible = true;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            Analytics.notifyEnterForeground();
        }
    }

    public static void activityPaused() {
        if (Utility.DEBUG) Log.e(TAG, "activityPaused()");
        activityVisible = false;
        if (Utility.mNetworkErrordialog != null && Utility.mNetworkErrordialog.isShowing()) {
            Utility.mNetworkErrordialog.cancel();
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            Analytics.notifyExitForeground();
        }
    }

    public static void activityDestroy(Context aContext) {
        if (Utility.DEBUG)
            Log.e(TAG, "***** activityDestroy() " + aContext.getClass().getSimpleName());
        if (getCurrentContext() == aContext) {
            setCurrentContext(null);
        }
    }

}

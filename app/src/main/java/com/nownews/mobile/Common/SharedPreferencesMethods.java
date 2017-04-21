
package com.nownews.mobile.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nownews.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class SharedPreferencesMethods {

    public final static String GCM_REGIST_ID = "RegistId";
    private static final String LAST_APP_VERSION = "appVersion";
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private final String KEY_SEARCH_HISTORY = "search_history_";
    private final String KEY_FAVORITE_IMAGE = "favorite_image_";
    private final String KEY_FAVORITE_IMAGE_TITLE = "favorite_imagetitle_";
    private final String KEY_FAVORITE_IMAGE_NEWS_SHARE_URL = "favorite_image_news_share_url";
    private final String KEY_NEWS_CONTENT_TEXT_SIZE = "news_content_text_size";
    private final String KEY_ALREADY_CHANGE_TEXT_SIZE_NAME = "already_change_text_size_name";
    private final String NOTIFICATION_STATUS = "notification_status";
    private final String NOTIFICATION_SOUND_STATUS = "notification_sound_status";
    private final String NOTIFICATION_VIBRATE_STATUS = "notification_vibrate_status";
    private final String NOTIFICATION_TIME = "notification_time";
    private final String ALREADY_ASK_OPEN_NOTIFICATION = "already_ask_open_notification";
    private final String KEY_IS_V3_VERSION = "is_v3_version";
    private final String KEY_IS_ALREADY_SHOW_NOTIFICATION_SETTING = "is_already_show_notification_setting";
    private final String KEY_LIVE_STOP_WATCHING_TIME = "live_stop_watching_time";
    private final String KEY_SHARE_APP_SUCCESS = "share_app_success";
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public SharedPreferencesMethods(Context aContext) {
        mContext = aContext;
        if(mContext==null){
            mContext = Utility.getApplicationContext();
        }
        if(mContext==null){
            return;
        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mPreferences.edit();
    }

    public void unRegistContext(Context aContext){
        if(mContext==aContext){
            mContext = null;
        }
    }

    public boolean isFavoriteIdExist(String aImageId) {
        if (aImageId == null) {
            return false;
        }
        Map map = mPreferences.getAll();
        boolean isExist = false;
        for (Object key : map.keySet()) {
            if (((String) key).equals(KEY_FAVORITE_IMAGE + aImageId)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    public String searchFavoriteId(String aUrl) {
        Map map = mPreferences.getAll();
        for (Object key : map.keySet()) {
            if (((String) key).contains(KEY_FAVORITE_IMAGE)) {
                String url = mPreferences.getString((String) key, null);
                if (url.equals(aUrl)) {
                    return ((String) key).replace(KEY_FAVORITE_IMAGE, "");
                }
            }
        }
        return null;
    }

    public String searchFavoriteAlbumTitle(String aImageId) {
        return mPreferences.getString(KEY_FAVORITE_IMAGE_TITLE + aImageId, null);
    }

    public String searchFavoriteAlbumNewsUrl(String aImageId) {
        return mPreferences.getString(KEY_FAVORITE_IMAGE_NEWS_SHARE_URL + aImageId, null);
    }

    public void saveFavoriteImageInfo(String aKey, String aValue, String atitle, String aNewsUrl) {
        if (Utility.DEBUG) Log.v(TAG, "============ saveFavoriteImageInfo ============");
        if (Utility.DEBUG) Log.v(TAG, "aValue: " + aValue);
        if (Utility.DEBUG) Log.v(TAG, "atitle: " + atitle);
        mEditor.putString(KEY_FAVORITE_IMAGE + aKey, aValue);
        mEditor.putString(KEY_FAVORITE_IMAGE_TITLE + aKey, atitle);
        mEditor.putString(KEY_FAVORITE_IMAGE_NEWS_SHARE_URL + aKey, aNewsUrl);
        mEditor.commit();
    }

    public void removeFavoriteImageInfo(String aKey) {
        mEditor.remove(KEY_FAVORITE_IMAGE + aKey);
        mEditor.remove(KEY_FAVORITE_IMAGE_TITLE + aKey);
        mEditor.remove(KEY_FAVORITE_IMAGE_NEWS_SHARE_URL + aKey);
        mEditor.commit();
    }

    public ArrayList<String> getFavoriteImageList() {

        ArrayList<String> mList = new ArrayList<String>();
        Map map = mPreferences.getAll();
        for (Object key : map.keySet()) {
            if (((String) key).contains(KEY_FAVORITE_IMAGE) && !((String) key).contains(KEY_FAVORITE_IMAGE_NEWS_SHARE_URL)) {
                String imageUrl = mPreferences.getString((String) key, null);
                mList.add(imageUrl);
            }
        }
        return mList;
    }

    public void saveSearchKeywords(String aKeyWords) {
        mEditor.putString(KEY_SEARCH_HISTORY + aKeyWords, aKeyWords);
        mEditor.commit();
    }

    public ArrayList<String> getSearchHistoryList() {

        ArrayList<String> mList = new ArrayList<String>();
        Map map = mPreferences.getAll();
        for (Object key : map.keySet()) {
            if (((String) key).contains(KEY_SEARCH_HISTORY)) {
                String keywords = mPreferences.getString((String) key, null);
                mList.add(keywords);
            }
        }
        return mList;
    }

    public void clearSearchHistoryList() {
        ArrayList<String> mList = new ArrayList<String>();
        Map map = mPreferences.getAll();
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (((String) key).contains(KEY_SEARCH_HISTORY)) {
                mEditor.remove(key);
                mEditor.commit();
            }
        }
    }

    public String getNewsContentTextSize() {
        return mPreferences.getString(KEY_NEWS_CONTENT_TEXT_SIZE, mContext.getString(R.string.small));
    }

    public void saveNewsContentTextSize(String aTextSize) {
        mEditor.putString(KEY_NEWS_CONTENT_TEXT_SIZE, aTextSize);
        mEditor.commit();
    }

    public boolean getAlreadyChangeTextSizeText() {
        return mPreferences.getBoolean(KEY_ALREADY_CHANGE_TEXT_SIZE_NAME, false);
    }

    public void saveAlreadyChangeTextSizeText(boolean isChange) {
        mEditor.putBoolean(KEY_ALREADY_CHANGE_TEXT_SIZE_NAME, isChange);
        mEditor.commit();
    }

    public String getGcmRegistId() {
        return mPreferences.getString(GCM_REGIST_ID, null);
    }

    public void setGcmRegistId(String aRegistId) {
        if(Utility.DEBUG)Log.v(TAG, "&&& aRegistId: " + aRegistId);
        mEditor.putString(GCM_REGIST_ID, aRegistId);
        mEditor.commit();
    }

    public boolean getNotificationStatus() {
        return mPreferences.getBoolean(NOTIFICATION_STATUS, true);
    }

    public void setNotificationStatus(boolean status) {
        mEditor.putBoolean(NOTIFICATION_STATUS, status);
        mEditor.commit();
    }

    public boolean getNotificationSoundStatus(){
        return mPreferences.getBoolean(NOTIFICATION_SOUND_STATUS, true);
    }

    public void setNotificationSoundStatus(boolean status) {
        mEditor.putBoolean(NOTIFICATION_SOUND_STATUS, status);
        mEditor.commit();
    }

    public boolean getNotificationVibrateStatus(){
        return mPreferences.getBoolean(NOTIFICATION_VIBRATE_STATUS, true);
    }

    public void setNotificationVibrateStatus(boolean status) {
        mEditor.putBoolean(NOTIFICATION_VIBRATE_STATUS, status);
        mEditor.commit();
    }

    public int getNotificationTime() {
        return mPreferences.getInt(NOTIFICATION_TIME, 0);
    }

    public void setNotificationTime(int aTime) {
        mEditor.putInt(NOTIFICATION_TIME, aTime);
        mEditor.commit();
    }

    public boolean getAskOpenNotificationStatus() {
        return mPreferences.getBoolean(ALREADY_ASK_OPEN_NOTIFICATION, false);
    }

    public void setAskOpenNotificationStatus(boolean status) {
        mEditor.putBoolean(ALREADY_ASK_OPEN_NOTIFICATION, status);
        mEditor.commit();
    }

    public int getLastVersion() {
        int lastVersion = mPreferences.getInt(LAST_APP_VERSION, 0);
        return lastVersion;
    }

    public void saveLastVersion(int aVersion) {
        mEditor.putInt(LAST_APP_VERSION, aVersion);
        mEditor.commit();
    }

    public void clearAllSharedPreferencesData(){
        mEditor.clear().commit();
    }

    public boolean isV3Version(){
        return mPreferences.getBoolean(KEY_IS_V3_VERSION, false);
    }

    public void setIsV3Version(){
        mEditor.putBoolean(KEY_IS_V3_VERSION, true);
        mEditor.commit();
    }

    public boolean isAlreadyShowNotificationSetting(){
        return mPreferences.getBoolean(KEY_IS_ALREADY_SHOW_NOTIFICATION_SETTING, false);
    }

    public void setIsAlreadyShowNotificationSetting(){
        mEditor.putBoolean(KEY_IS_ALREADY_SHOW_NOTIFICATION_SETTING, true);
        mEditor.commit();
    }

    public long getLiveStopWatchingTime(){
        return mPreferences.getLong(KEY_LIVE_STOP_WATCHING_TIME, -1);
    }

    public void setLiveStopWatchingTime(long aLiveStopWatchingTime){
        mEditor.putLong(KEY_LIVE_STOP_WATCHING_TIME, aLiveStopWatchingTime);
        mEditor.commit();
    }

    public boolean isShareAppSuccess(){
        return mPreferences.getBoolean(KEY_SHARE_APP_SUCCESS, true);
    }

    public void setShareAppSuccess(boolean isShareAppSuccess) {
        mEditor.putBoolean(KEY_SHARE_APP_SUCCESS, isShareAppSuccess);
        mEditor.commit();
    }
}

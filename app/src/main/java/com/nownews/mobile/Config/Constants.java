package com.nownews.mobile.Config;


import android.os.Environment;

public class Constants {
    // 側欄標籤
    public static final class TAGS {
        public static final String TEST_TAG = "Test";
    }

    public static final class Download {
        public static final String FileName = "NownewsApp.apk";
        public static final String Folder = Environment.getExternalStorageDirectory() + "/Nownews/";
    }

    // 資料庫中的資料類型
    public static final class DBContentType {
        // 列表
        public static final String Content_list = "list";
        // 列表內容
        public static final String Content_content = "content";
        // 討論
        public static final String Discuss = "discuss";
    }

    // 資料庫中的暫存時間
    public static final class DBContentCacheTime {
        // 分鐘
        public static int Content_ListCacheTime = 3;
        public static int Content_ContentCacheTime = 60 * 24 * 3;
        public static int ImageCacheTime = 60 * 24 * 15;
        public static int Content_DefaultCacheTime = 60 * 24 * 3;

        public static int DiscussCacheTime = 60;
    }

    public static final class WebSourceType {
        public static final String Json = "json";
        public static final String Xml = "xml";
    }

    public static final class HeaderFields {
        public static final String statuscode = "status_code";
        public static final String message = "message";
    }

    public static final class StatusCode {
        public static final int Err_200 = 200;
        public static final int BadRequest_Err_40000 = 40000;
        public static final int Err_49801 = 49801;
        public static final int Err_49804 = 49804;
        public static final int Err_40101 = 40101;
        public static final int Err_40300 = 40300;
        public static final int Err_40301 = 40301;
        public static final int No_Intent_Err = -999;
    }

    public static final class GaID {
        public static final int MENU_CHANNEL_ID = 7533966;
        public static final int MENU_STAR_ID = 7533967;
        public static final int MENU_RECORDS_ID = 7533968;
    }

    public static final class AdUrl {
        public static final String Vod = "https://pubads.g.doubleclick.net/gampad/ads?sz=1024x768&iu=/123939770/Vidol_Android_player_VOD&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        public static final String Live = "https://pubads.g.doubleclick.net/gampad/ads?sz=1024x768&iu=/123939770/Vidol_Android_player_LIVE&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        public static final String Channel = "https://pubads.g.doubleclick.net/gampad/ads?sz=1024x768&iu=/123939770/Vidol_Android_player_Channel&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";

    }
}
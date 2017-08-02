package com.nownews.mobile.Api;

public class WebAPIUrl {

    // -Header
    public final static String HEADER_KEY = "X-NOWnews-API"; //v4
    public final static String HEADER_VALUE = "YouCanSeeMeJohnCena"; //v4
    public final static String HOST = "https://v4api.nownews.com";

    // -Nownews Pc版首頁
    public final static String NOWNEWS_PC_DOMAIN = "https://www.nownews.com";

    // -Nownews Pc版新聞內頁
    public final static String NOWNEWS_PC_NEWS_DOMAIN = "https://www.nownews.com/news/";

    // -Nownews Mobile Web新聞內頁
    public final static String NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN = "https://m.nownews.com/news/";

    // -取得版本號
    public final static String GET_CURRENT_APP_VERSION = HOST + "/app/version?os=ANDROID&device=PHONE";

    // -縮圖
    public final static String SCALE_IMAGE = "https://imgapiv2.nownews.com/?w=%s&h=%s&q=%s&src=%s";

    // -推播ID回傳Server
    public final static String REGIST_ID_RETURN = HOST + "/app/info";

    // -首頁進入圖
    public final static String NOWNEWS_SPLASH_IMAGE = HOST + "/app/splash?device=PHONE";

    // -頭條新聞
    public final static String HEADLINE_NEWS = HOST + "/indexpage";

    // -熱門新聞
    public final static String NOWNEWS_HOT_NEWS = HOST + "/news/hotNews";

    // -速報
    public final static String INSTANT_NEWS = HOST + "/instant";

    // -搜尋
    public final static String SEARCH = HOST + "/search/%s?timeRange=lastYear";

    // -新聞大分類
    public final static String NEWS_CATEGORY = HOST + "/menus";

    // -新聞內頁
    public final static String NEWS_INFO = HOST + "/news/%d";

    // -特輯大分類
    public final static String SPECIAL_NEWS_CATEGORY = HOST + "/specialchannels";

    // -特輯列表
    public final static String SPECIAL_NEWS_LIST = HOST + "/specialchannels/%d";

    // -live
    public final static String LIVE_LIST = HOST + "/stream/channels";

    // -直播Bar
    public final static String LIVE_INFO = HOST + "/live/info";

    // -附近的人在看
    public final static String NEAR_BY_NEWS = HOST + "/nearByNews?longitude=%f&latitude=%f";

    // -相關新聞
    public final static String RELATIONS_NEWS = NEWS_INFO + "/relations";
}

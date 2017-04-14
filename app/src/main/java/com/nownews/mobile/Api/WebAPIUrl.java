package com.nownews.mobile.Api;

public class WebAPIUrl {

    //region Header
    public final static String HEADER_KEY = "X-NOWnews-API";
    public final static String HEADER_VALUE = "NOWnewsTaiwanNumberOne";
    //endregion

    //region Domain
    // -Nownews Pc版首頁
    public final static String NOWNEWS_PC_DOMAIN = "http://www.nownews.com";
    // -Nownews Pc版新聞內頁
    public final static String NOWNEWS_PC_NEWS_DOMAIN = "http://www.nownews.com/n/";
    // -Nownews Pc版圖集內頁
    public final static String NOWNEWS_PC_PHOTO_DOMAIN = "http://www.nownews.com/p/";
    // -Nownews Pc版影音內頁
    public final static String NOWNEWS_PC_VIDEO_DOMAIN = "http://www.nownews.com/v/";
    //endregion

    // -Nownews Mobile Web新聞內頁
    public final static String NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN = "https://m.nownews.com/news/";
    // -Nownews Mobile Web圖集內頁
    public final static String NOWNEWS_MOBIEL_WEB_PHOTO_DOMAIN = "https://m.nownews.com/photo/";
    // -Nownews Mobile Web影音內頁
    public final static String NOWNEWS_MOBIEL_WEB_VIDEO_DOMAIN = "https://m.nownews.com/video/";
    // -取得版本號
    public final static String GET_CURRENT_APP_VERSION = "http://e.nownews.com/api/android/version";
    // -縮圖
    public final static String SCALE_IMAGE = "http://imgapi.nownews.com/?w=%s&h=%s&q=%s&src=%s";
    //旅食樂
    public final static String FOOD = "http://play.nownews.com";
    //健康百科
    public final static String Health = "http://healthmedia.nownews.com/";

    private final static String HOST = "http://v3.api.nownews.com";
//    private final static String HOST = "http://61.67.121.26";

    // -推播ID回傳Server
    public final static String REGIST_ID_RETURN = HOST + "/devicetoken";
    //    public final static String REGIST_ID_RETURN = "http://rev.nownews.com:86/mobile/notification";
    // -首頁進入圖
    public final static String NOWNEWS_SPLASH_IMAGE = HOST + "/headimage/android";
    // -頭條新聞
    public final static String BIG_3_SMALL_6 = HOST + "/news/headline";
    // -熱門新聞
    public final static String NOWNEWS_HOT_NEWS = HOST + "/news/hotNews";
    // -速報
    public final static String INSTANT_NEWS = HOST + "/news/instant";
    // -搜尋
    public final static String SEARCH = HOST + "/search?keyword=%s&page=%d";

    //region 新聞系列
    // -新聞大分類
    public final static String NEWS_CATEGORY = HOST + "/category/news";
    // -新聞內頁
    public final static String NEWS_INFO = HOST + "/news/%d";
    // -新聞列表
    public final static String NEWS_LIST = HOST + "/category/news/%d?page=%d";
    //endregion

    //region 圖集系列
    // -圖集大分類
    public final static String PHOTOS_CATEGORY = HOST + "/category/photos";
    // -圖集列表
    public final static String PHOTOS_LIST = HOST + "/category/photos/%d?page=%d";
    // -圖集內頁
    public final static String PHOTOS_INFO = HOST + "/photos/%d";
    //endregion

    //region 影音系列
    // -影音大分類
    public final static String VIDEOS_CATEGORY = HOST + "/category/videos";
    // -影音列表
    public final static String VIDEOS_LIST = HOST + "/category/videos/%d?page=%d";
    // -影音內頁
    public final static String VIDEOS_INFO = HOST + "/videos/%d";
    //endregion

    //region  特輯系列
    // -特輯大分類
    public final static String SPECIAL_NEWS_CATEGORY = HOST + "/channels/news";
    // -特輯列表
    public final static String SPECIAL_NEWS_LIST = HOST + "/channels/news/%d";
    //endregion

    //live
    public final static String LIVE_LIST = HOST + "/lifefei/channels";

    //附近的人在看
    public final static String NEAR_BY_NEWS = HOST + "/nearByNews?longitude=%f&latitude=%f";
}

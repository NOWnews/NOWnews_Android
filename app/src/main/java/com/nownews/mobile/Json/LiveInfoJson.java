package com.nownews.mobile.Json;

/**
 * Created by cindy on 2017/4/19.
 */

public class LiveInfoJson {

    /**
     * title : 第28屆金曲獎頒獎典禮紅毯 ！#NOW直擊 媒體採訪區
     * teaserTitle : 第28屆金曲獎頒獎典禮紅毯 ！#NOW直擊 媒體採訪區
     * banner : http://legacy.nownews.com/NOWnews_static/live-banner.jpg
     * alt : 第28屆金曲獎頒獎典禮紅毯 ！#NOW直擊 媒體採訪區
     * url : https://www.youtube.com/embed/rqESGX2vt3M
     * wowza :
     * youtubeId : rqESGX2vt3M
     * livePage : https://m.nownews.com/live/rqESGX2vt3M
     * background : https://legacy.nownews.com/NOWnews_static/live-background.jpg
     * backgroundColor : #403534
     * redirect :
     * isOnAir : false
     * campainStatus : false
     */

    private String title;
    private String url;
    private String livePage;
    private String background;
    private boolean isOnAir;
    private boolean campainStatus;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLivePage() {
        return livePage;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public boolean isIsOnAir() {
        return isOnAir;
    }

    public void setIsOnAir(boolean isOnAir) {
        this.isOnAir = isOnAir;
    }

    public boolean isCampainStatus() {
        return campainStatus;
    }

    public void setCampainStatus(boolean campainStatus) {
        this.campainStatus = campainStatus;
    }
}

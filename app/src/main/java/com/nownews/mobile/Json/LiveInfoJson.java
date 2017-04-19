package com.nownews.mobile.Json;

/**
 * Created by cindy on 2017/4/19.
 */

public class LiveInfoJson {

    /**
     * title : 2017 國民黨黨主席辯論直播
     * url : https://www.youtube.com/embed/KyrppX4KFH4
     * livePage :
     * youtubeId : KyrppX4KFH4
     * isOnAir : true
     * campainStatus : true
     */

    private String title;
    private String url;
    private String livePage;
    private String youtubeId;
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

    public void setLivePage(String livePage) {
        this.livePage = livePage;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
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

package com.nownews.mobile.Json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cindy on 2017/4/19.
 */

public class LiveInfoJson {
    /**
     *
     * {
     *   "title": "直播 /「龍談大小事」- 藍營準黨魁大PK",
     *   "teaserTitle": "19:00 「龍談大小事」- 藍營準黨魁大PK",
     *   "url": "https://www.youtube.com/embed/Ps1SpLK-pqw",
     *   "wowza": "http://59.124.93.43/live/KMT.stream/playlist.m3u8?pf=mm",
     *   "youtubeId": "Ps1SpLK-pqw",
     *   "livePage": "https://m.nownews.com/live/Ps1SpLK-pqw",
     *   "background": "https://legacy.nownews.com/NOWnews_static/live-background.jpg",
     *   "isOnAir": false,
     *   "campainStatus": false
     *   }
     */
    private String title;
    private String teaserTitle;
    private String url;
    private String wowza;
    private String youtubeId;
    private String livePage;
    private String background;
    @JsonProperty("isOnAir")
    private boolean isOnAir;
    private boolean campainStatus;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeaserTitle() {
        return teaserTitle;
    }

    public void setTeaserTitle(String teaserTitle) {
        this.teaserTitle = teaserTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWowza() {
        return wowza;
    }

    public void setWowza(String wowza) {
        this.wowza = wowza;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getLivePage() {
        return livePage;
    }

    public void setLivePage(String livePage) {
        this.livePage = livePage;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public boolean isOnAir() {
        return isOnAir;
    }

    public void setOnAir(boolean onAir) {
        isOnAir = onAir;
    }

    public boolean isCampainStatus() {
        return campainStatus;
    }

    public void setCampainStatus(boolean campainStatus) {
        this.campainStatus = campainStatus;
    }
}

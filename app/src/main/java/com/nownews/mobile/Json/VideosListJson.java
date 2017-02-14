package com.nownews.mobile.Json;

import java.util.List;

public class VideosListJson {

    public List<VideosContent> videosList;

    public class VideosContent {
        public int nodeId;
        public String title;
        public String createdAt;
        public String youtubeThumbnail;
    }

}

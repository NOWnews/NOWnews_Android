package com.nownews.mobile.Json;

import java.util.List;

public class VideosInfoJson {

    public int _id;
    public Body body;
    public String title;
    public String image;
    public String youtubeId;
    public String src;
    public String createdAt;
    public List<CategoryInfo> categories;

    public class Body {
        public String value;
    }

    public class CategoryInfo {
        public int _id;
        public String name;
    }

}

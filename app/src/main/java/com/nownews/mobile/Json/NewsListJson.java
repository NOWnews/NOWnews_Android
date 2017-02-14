package com.nownews.mobile.Json;

import java.util.List;

public class NewsListJson {

    public List<NewsContent> newsList;

    public class NewsContent {
        public int _id;
        public String createdAt;
        public ShortTitle field_short_title;
        public CategoryInfo category;
        public ImageInfo image;
    }

    public class CategoryInfo {
        public int _id;
        public String name;
    }

    public class ShortTitle {
        public String value;
    }

    public class ImageInfo {
        public String originImage;
        public String thumbnail;
        public String url;
    }

}

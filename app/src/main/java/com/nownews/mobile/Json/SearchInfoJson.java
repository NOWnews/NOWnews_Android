package com.nownews.mobile.Json;

import java.util.List;

public class SearchInfoJson {

    public List<SearchInfoContent> newsList;

    public class SearchInfoContent {
        public int _id;
        public ShortTitle field_short_title;
        public ImageInfo image;
        public CategoryInfo category;
        public String createdAt;

        public class ShortTitle {
            public String value;
        }

        public class ImageInfo {
            public String originImage;
        }

        public class CategoryInfo {
            public int _id;
            public String name;
        }

    }

}

package com.nownews.mobile.Json;

import java.util.List;

public class NewsInfoJson {

    public int nodeId;
    public String title;
    public String url;
    public TopImageInfo image;
    public String summary;
    public String createdAt;
    public String htmlBody;
    public List<VideoInfo> videos;
    public List<MobileBody> mobileBody;
    public String freeBody;
    public String author;
    public String adult;
    public PreviousNextNewsInfo prev;
    public PreviousNextNewsInfo next;
    public MainNewsCategory category;
    public List<ReferenceNewsInfo> refNews;

    public class TopImageInfo {
        public String title;
//        public String originImage;
        public String thumbnail;
        public String url;
    }

    public class VideoInfo {

    }

    public class MobileBody {
        public String tag;
        public String content;
        public String src;
        public String style;
    }

    public class PreviousNextNewsInfo {
        public int _id;
        public ShortTitle field_short_title;

        public class ShortTitle {
            public String value;
        }
    }

    public class MainNewsCategory {
        public int _id;
        public String name;
    }

    public class ReferenceNewsInfo {
        public int _id;
        public String title;
        public ImageInfo image;
        public CategoryInfo category;

        public class ImageInfo {
            public String originImage;
            public String thumbnail;
            public String url;
        }

        public class CategoryInfo {
            public int _id;
            public String name;
        }
    }

}

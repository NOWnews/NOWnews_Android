package com.nownews.mobile.Json;

import java.util.List;

public class PhotosListJson {

    public List<PhotosContent> photosList;

    public class PhotosContent {
        public String title;
        public int nodeId;
        public String thumbnail;
    }

}

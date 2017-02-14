package com.nownews.mobile.Json;

import java.util.List;

public class PhotosInfoJson {

    public int nodeId;
    public String title;
    public String url;
    public List<PhotoInfo> collectionImages;

    public class PhotoInfo {
        public int nodeId;
        public String cite;
//        public String originImage;
        public String thumbnail;
    }

}

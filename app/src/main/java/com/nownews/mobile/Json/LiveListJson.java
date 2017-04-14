package com.nownews.mobile.Json;


import java.util.List;

public class LiveListJson {

    public List<Data> data;
    public LifeInfo liveInfo;

    public class LifeInfo{

        public int watchTime;
        public int lockTime;
        public boolean watchable;
        public String icon;
        public String titleMessage;
        public boolean downloadable;
        public String iosDownloadLink;
        public String androidDownloadLink;

    }

    public class Data{

        public String categoryName;
        public int count;
        public List<ChannelList> list;

    }

    public class ChannelList{

        public String SN;
        public String code;
        public String title;
        public String path;

    }

}

package com.nownews.mobile.Json;


import java.util.List;

public class LiveListJson {

    public List<Data> data;

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

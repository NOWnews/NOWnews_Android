package com.nownews.mobile.Json;

import java.util.List;

public class NewsCategoryJson {

    public List<CategoryInfo> newsCategory;

    public class CategoryInfo {
        public int tid;
        public String name;
    }
}

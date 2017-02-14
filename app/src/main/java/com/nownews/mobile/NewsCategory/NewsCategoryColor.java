package com.nownews.mobile.NewsCategory;

/**
 * Created by cindy on 2016/10/12.
 */

public enum NewsCategoryColor {

    頭條(0xFF0083FF),
    熱門(0xFFFFB900),
    速報(0xFFFFB4C6),
    政治(0xFFE71900),
    財經(0xFFFF5A17),
    生活(0xFFFF9900),
    地方(0xFFFFE72E),
    社會(0xFF72FF5C),
    運動(0xFF53FFE0),
    娛樂(0xFF2EE7FF),
    國際(0xFF0099FF),
    大陸(0xFF2763FF),
    新奇(0xFF6645FF),
    消費(0xFFFF737D),
    旅遊(0xFFFF456E),
    科技(0xFFFF45D5),
    健康(0xFFD073FF),
    旅食樂(0xFFD20A41),
    健康百科(0xFFB6FFFF),
    附近的人在看(0xFFFFe589),
    不分類(0xFF000000);

    private int mColor;

    NewsCategoryColor(final int aColor) {
        mColor = aColor;
    }

    public int getColor() {
        return mColor;
    }

}

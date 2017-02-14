package com.nownews.mobile.VideoNewsCategory;

/**
 * Created by cindy on 2016/10/12.
 */

public enum VideoNewsCategoryColor {

    影音(0xFF0083FF),
    新聞(0xFFFFB900),
    美食(0xFFFFB4C6),
    娛樂(0xFF2EE7FF),
    旅遊(0xFFFF456E),
    運動(0xFF53FFE0),
    時尚(0xFF2763EF),
    文化(0xFF6645FF),
    知識(0xFFFF737D),
    寵物(0xFFFF45D5);

    private int mColor;

    VideoNewsCategoryColor(final int aColor) {
        mColor = aColor;
    }

    public int getColor() {
        return mColor;
    }

}

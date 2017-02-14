package com.nownews.mobile.AlbumCategory;

/**
 * Created by cindy on 2016/10/12.
 */

public enum AlbumsCategoryColor {

    圖輯總覽(0xFF0083FF),
    影劇(0xFFFFB900),
    正妹(0xFFFFB4C6),
    要聞(0xFFE71900),
    新奇(0xFFFF5A17),
    寵物(0xFFFF9900),
    運動(0xFF53FFE0),
    旅遊(0xFFFF456E),
    名人(0xFFD073FF),
    其他(0xFF2EE7FF);

    private int mColor;

    AlbumsCategoryColor(final int aColor) {
        mColor = aColor;
    }

    public int getColor() {
        return mColor;
    }

}

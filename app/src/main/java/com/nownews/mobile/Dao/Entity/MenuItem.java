package com.nownews.mobile.Dao.Entity;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public class MenuItem {
    private int icon;
    private String title;
    private boolean isNotify = false;
    private boolean isSecction = false;

    public MenuItem(int icon, String title, boolean isNotify) {
        this.icon = icon;
        this.title = title;
        this.isNotify = isNotify;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public boolean isSecction() {
        return isSecction;
    }

    public void setSecction(boolean secction) {
        isSecction = secction;
    }
}

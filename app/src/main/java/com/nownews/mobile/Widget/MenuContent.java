package com.nownews.mobile.Widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Dao.CheckVerDao;
import com.nownews.mobile.Dao.Entity.MenuItem;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbum;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.Widget.Adapter.MenuRecyclerAdapter;

import java.util.LinkedList;

public class MenuContent extends RelativeLayout {

    public static final String KEY_ICON = "icon";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CATEGORY = "category";
    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private NewHome baseAct;
    private DrawerLayout vDrawerLayout;
    private RecyclerView recyclerview;

    private LinkedList<MenuItem> mMenuList;
    private MenuRecyclerAdapter menuRecyclerAdapter;

    private DialogInterface.OnDismissListener mNotificationDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            menuRecyclerAdapter.notifyDataSetChanged();
        }
    };

    public MenuContent(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void init(NewHome aContext, DrawerLayout aDrawerLayout) {
        this.baseAct = aContext;
        this.vDrawerLayout = aDrawerLayout;
        this.recyclerview = (RecyclerView) findViewById(R.id.list);
        initMenu();
    }

    private void initMenu() {
        this.mMenuList = new LinkedList<>();
        String[] listContent = getResources().getStringArray(R.array.menu_list);
        for (int i = 0; i < listContent.length; i++) {
            MenuItem item = null;
            if (listContent[i].equals(this.mContext.getString(R.string.return_home))) {
                item = new MenuItem(R.drawable.menu_home, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.album))) {
                item = new MenuItem(R.drawable.photos, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.favorite_album))) {
                item = new MenuItem(R.drawable.love, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.nowvote))) {
                item = new MenuItem(R.drawable.vote, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.setting))) {
                item = new MenuItem(0, listContent[i], false);
                item.setSecction(true);
            } else if (listContent[i].equals(this.mContext.getString(R.string.version))) {
                item = new MenuItem(R.drawable.version, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.notification_setting))) {
                item = new MenuItem(R.drawable.notification, listContent[i], true);
            } else if (listContent[i].equals(this.mContext.getString(R.string.about))) {
                item = new MenuItem(R.drawable.now, listContent[i], false);
            } else if (listContent[i].equals(this.mContext.getString(R.string.share_app))) {
                item = new MenuItem(R.drawable.ic_share_white_36dp, listContent[i], false);
            }
            this.mMenuList.add(item);
        }
        this.menuRecyclerAdapter = new MenuRecyclerAdapter(this.baseAct, this.recyclerview, this, this.mMenuList);
        this.recyclerview.setAdapter(this.menuRecyclerAdapter);
        this.menuRecyclerAdapter.notifyDataSetChanged();
    }

    public void itemClick(int index) {
        String itemName = this.mMenuList.get(index).getTitle();
        GoogleAnalyticsFunction.sendHitInfo(this.mContext, this.mContext.getString(R.string.left_menu), itemName, "");

        if (itemName != null
                && itemName.equals(this.mContext.getString(R.string.return_home))) {
            this.baseAct.goHomePage();
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.favorite_album))) {
            gotoFavoriteAlbum();
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.version))) {
            // 顯示版本alert
            this.baseAct.isMenu = true;
            this.baseAct.DoSync(new CheckVerDao(this.baseAct));
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.category_edit))) {
            openNewsPreference();
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.notification_setting))) {
            Utility.showNotificationSwicherDialog(this.mContext, this.mNotificationDialogDismissListener);
            return;
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.about))) {
            String url = "http://m.nownews.com/about";
            Intent intent = new Intent();
            intent.setClass(this.mContext, WebActivity.class);
            intent.putExtra(WebActivity.KEY_URL, url);
            this.mContext.startActivity(intent);
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.nowvote))) {
            String url = "http://vote.nownews.com/";
            Intent intent = new Intent();
            intent.setClass(this.mContext, WebActivity.class);
            intent.putExtra(WebActivity.KEY_URL, url);
            this.mContext.startActivity(intent);
        } else if (itemName != null && itemName.equals(this.mContext.getString(R.string.share_app))) {
            Utility.shareApp(this.mContext);
            return;
        }
        this.vDrawerLayout.closeDrawers();
    }

    public void setHasNewVersion(boolean value) {
        this.menuRecyclerAdapter.setHasNewVersion(value);
    }

    private void gotoFavoriteAlbum() {
        Intent intent = new Intent();
        intent.setClass(this.mContext, FavoriteAlbum.class);
        this.mContext.startActivity(intent);
    }

    private void openNewsPreference() {
//        Intent intent = new Intent();
//        intent.setClass(mContext, HomeGridPreferenceActivity.class);
//        if (mContext instanceof VideoNewsCategoryFragment) {
//            if (Utility.DEBUG) Log.e(TAG, "VideoNewsCategoryFragment!!!");
//            intent.putExtra(HomeGridPreferenceActivity.KEY_FROM, HomeGridPreferenceActivity.FROM_NEWS_CATEGORY);
//            ((Activity) mContext).startActivity(intent);
//            ((Activity) mContext).finish();
//        } else if (mContext instanceof AlbumCategoryFragment) {
//            if (Utility.DEBUG) Log.e(TAG, "AlbumCategoryFragment!!!");
//            intent.putExtra(HomeGridPreferenceActivity.KEY_FROM, HomeGridPreferenceActivity.FROM_ALBUM_CATEGORY);
//            ((Activity) mContext).startActivity(intent);
//            ((Activity) mContext).finish();
//        } else {
//            ((Activity) mContext).startActivityForResult(intent, Home.REQUEST_CODE);
//        }
    }
}

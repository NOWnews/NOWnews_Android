package com.nownews.mobile.VideoNewsCategory;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.VideosCategoryJson;
import com.nownews.mobile.Json.VideosCategoryJson.CategoryInfo;

import java.util.HashMap;
import java.util.List;

public class VideoNewsCategoryFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private List<VideosCategoryJson.CategoryInfo> mNewsCategoryList;
    private int mPageCount;
    private HashMap<String, Fragment> mNewsListFragmentList;
    private Handler mHandler;

    public VideoNewsCategoryFragmentAdapter(Context aContext, FragmentManager fm, List<VideosCategoryJson.CategoryInfo> aNewsCategoryList, int aPageCount, Handler aHandler) {
        super(fm);
        mContext = aContext;
        mNewsCategoryList = aNewsCategoryList;
        mPageCount = aPageCount;
        mHandler = aHandler;
    }

    public void setData(List<VideosCategoryJson.CategoryInfo> aNewsCategoryList, int aPageCount) {
        mNewsCategoryList = aNewsCategoryList;
        mPageCount = aPageCount;
        notifyDataSetChanged();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        String mTitle = mNewsCategoryList.get(position).name;
        mTitle = "	" + mTitle + "	";
        return mTitle;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem::: " + position);
        Fragment fragment = null;
        int id = -1;
        if (mNewsListFragmentList == null) {
            mNewsListFragmentList = new HashMap<String, Fragment>();
        } else {
            if (mNewsListFragmentList.size() > 0) {
                boolean isFragmentExist = false;
                for (String key : mNewsListFragmentList.keySet()) {
                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
                        isFragmentExist = true;
                        break;
                    }
                }
                if (isFragmentExist) {
                    fragment = mNewsListFragmentList.get(KEY_FRAGMENT_INDEX + position);
                    if (fragment != null) {
                        return fragment;
                    }
                }
            }
        }

        String itemName = mNewsCategoryList.get(position).name;
        if (Utility.DEBUG) Log.e(TAG, "itemName: " + itemName);
        id = mNewsCategoryList.get(position).tid;

        fragment = new VideoNewsListFragment();
        ((VideoNewsListFragment) fragment).setData(id, mHandler, itemName);
        mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, fragment);
        if (Utility.DEBUG)Log.d(TAG, "is fragment null or not?? " + (fragment == null ? "true" : "false"));
        return fragment;
    }

    @Override
    public int getCount() {
        return mPageCount;
    }

    public void clearFragmentList() {
        if (Utility.DEBUG) Log.e(TAG, "clearFragmentList 111");
        if (mNewsListFragmentList != null && mNewsListFragmentList.size() >= 0) {
            if (Utility.DEBUG)
                Log.e(TAG, "mAlbumGridFragmentList.size(): " + mNewsListFragmentList.size());
            mNewsListFragmentList.clear();
            if (Utility.DEBUG)
                Log.e(TAG, "mAlbumGridFragmentList.size(): " + mNewsListFragmentList.size());
            mNewsListFragmentList = null;
        }
    }

}

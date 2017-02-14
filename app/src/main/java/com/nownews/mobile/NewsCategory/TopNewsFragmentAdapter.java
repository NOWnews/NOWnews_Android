package com.nownews.mobile.NewsCategory;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.NewsListJson.NewsContent;

import java.util.HashMap;
import java.util.List;

public class TopNewsFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private List<NewsContent> mNewsList;
    private HashMap<String, TopNewsFragment> mNewsListFragmentList;
    private Handler mHandler;
    private String mCategoryName;

    public TopNewsFragmentAdapter(Context aContext, FragmentManager fm, List<NewsContent> aNewsList, Handler aHandler, String aCategoryName) {
        super(fm);
        Log.d(TAG, "TopNewsFragmentAdapter!!");
        mContext = aContext;
        mNewsList = aNewsList;
        mHandler = aHandler;
        mCategoryName = aCategoryName;
    }

    public void setData(List<NewsContent> aNewsList, String aCategoryName) {
        mNewsList = aNewsList;
        mCategoryName = aCategoryName;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem::: " + position);
        TopNewsFragment topNewsFragment = null;
//        if (mNewsListFragmentList == null) {
//            mNewsListFragmentList = new HashMap<String, TopNewsFragment>();
//        } else {
//            if (mNewsListFragmentList.size() > 0) {
//                boolean isFragmentExist = false;
//                for (String key : mNewsListFragmentList.keySet()) {
//                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
//                        isFragmentExist = true;
//                        break;
//                    }
//                }
//                if (isFragmentExist) {
//                    topNewsFragment = mNewsListFragmentList.get(KEY_FRAGMENT_INDEX + position);
//                    if (topNewsFragment != null) {
//                        return topNewsFragment;
//                    }
//                }
//            }
//        }
        topNewsFragment = new TopNewsFragment();
        topNewsFragment.setData(mNewsList, position, mCategoryName);
//        mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, topNewsFragment);
        return topNewsFragment;
    }

    @Override
    public int getCount() {
        if (mNewsList.size() < 4) {
            return mNewsList.size();
        }
        return 4;
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TopNewsFragment topNewsFragment = (TopNewsFragment) super.instantiateItem(container, position);
        topNewsFragment.setData(mNewsList, position, mCategoryName);
        return topNewsFragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}

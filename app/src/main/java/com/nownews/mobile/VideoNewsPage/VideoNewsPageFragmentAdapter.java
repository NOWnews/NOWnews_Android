package com.nownews.mobile.VideoNewsPage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.VideosListJson.VideosContent;

import java.util.HashMap;
import java.util.List;

public class VideoNewsPageFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private List<VideosContent> mNewsList;
    private HashMap<String, VideoNewsPageRecyclerViewFragment> mNewsPageFragmentList;
    private String mNewsUrl;
    private int mNewsId;
    private VideoNewsPageRecyclerViewFragment.OnPageLoadFinishedListener mOnPageLoadFinishedListener;

    public VideoNewsPageFragmentAdapter(Context aContext, FragmentManager fm, List<VideosContent> aNewsList, VideoNewsPageRecyclerViewFragment.OnPageLoadFinishedListener aOnPageLoadFinishedListener) {
        super(fm);
        mContext = aContext;
        mNewsList = aNewsList;
        mOnPageLoadFinishedListener = aOnPageLoadFinishedListener;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
        VideoNewsPageRecyclerViewFragment newsPageFragment = null;
        String newsUrl = null;
        int newsId = -1;
        if (mNewsList != null) {
            List<VideosContent> newsInfoList = UserDataInfo.getVideoNewsList();
            if (newsInfoList != null
                    && newsInfoList.get(position) != null
                    && newsInfoList.get(position).nodeId != -1) {
                newsId = newsInfoList.get(position).nodeId;
            }
        }

        if (mNewsPageFragmentList == null) {
            mNewsPageFragmentList = new HashMap<String, VideoNewsPageRecyclerViewFragment>();
        } else {
            if (mNewsPageFragmentList.size() > 0) {
                boolean isFragmentExist = false;
                for (String key : mNewsPageFragmentList.keySet()) {
                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
                        isFragmentExist = true;
                        break;
                    }
                }
                if (isFragmentExist) {
                    newsPageFragment = mNewsPageFragmentList.get(KEY_FRAGMENT_INDEX + position);
                    if (newsPageFragment != null) {
                        return newsPageFragment;
                    }
                }
            }
        }

        newsPageFragment = new VideoNewsPageRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VideoNewsPageFragment.KEY_NEWS_ID, newsId);
        bundle.putInt(VideoNewsPageFragment.KEY_NEWS_POSITION, position);
        newsPageFragment.setArguments(bundle);
        newsPageFragment.setOnPageLoadFinishedListener(mOnPageLoadFinishedListener);
        mNewsPageFragmentList.put(KEY_FRAGMENT_INDEX + position, newsPageFragment);
        return newsPageFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mNewsList != null) {
            count = mNewsList.size();
        }
        return count;
    }

    public void clearFragmentList() {
        if (mNewsPageFragmentList != null) {
            mNewsPageFragmentList.clear();
            mNewsPageFragmentList = null;
        }
    }

}

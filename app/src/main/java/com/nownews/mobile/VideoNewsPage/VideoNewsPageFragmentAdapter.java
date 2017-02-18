package com.nownews.mobile.VideoNewsPage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.VideosListJson.VideosContent;

import java.util.HashMap;
import java.util.List;

public class VideoNewsPageFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private List<VideosContent> mNewsList;
//    private HashMap<String, VideoNewsPageRecyclerViewFragment> mNewsPageFragmentList;
    private String mNewsUrl;
    private int mNewsId;
    private int mNewsType;
    private FragmentManager mFragmentManager;

    public VideoNewsPageFragmentAdapter(FragmentManager fm, List<VideosContent> aNewsList, int aNewsType, int aNewsId) {
        super(fm);
        mFragmentManager = fm;
        mNewsList = aNewsList;
        mNewsType = aNewsType;
        mNewsId = aNewsId;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
        VideoNewsPageRecyclerViewFragment newsPageFragment = null;
        String newsUrl = null;
        int newsId = -1;
        if(mNewsType==VideoNewsPage.TYPE_SINGAL_NEWS && mNewsId!=-1){
            newsId = mNewsId;
        }else if (mNewsList != null) {
            List<VideosContent> newsInfoList = UserDataInfo.getVideoNewsList();
            if (newsInfoList != null
                    && newsInfoList.get(position) != null
                    && newsInfoList.get(position).nodeId != -1) {
                newsId = newsInfoList.get(position).nodeId;
            }
        }

//        if (mNewsPageFragmentList == null) {
//            mNewsPageFragmentList = new HashMap<String, VideoNewsPageRecyclerViewFragment>();
//        } else {
//            if (mNewsPageFragmentList.size() > 0) {
//                boolean isFragmentExist = false;
//                for (String key : mNewsPageFragmentList.keySet()) {
//                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
//                        isFragmentExist = true;
//                        break;
//                    }
//                }
//                if (isFragmentExist) {
//                    newsPageFragment = mNewsPageFragmentList.get(KEY_FRAGMENT_INDEX + position);
//                    if (newsPageFragment != null) {
//                        return newsPageFragment;
//                    }
//                }
//            }
//        }

        newsPageFragment = new VideoNewsPageRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VideoNewsPageFragment.KEY_NEWS_ID, newsId);
        bundle.putInt(VideoNewsPageFragment.KEY_NEWS_POSITION, position);
        newsPageFragment.setArguments(bundle);
//        mNewsPageFragmentList.put(KEY_FRAGMENT_INDEX + position, newsPageFragment);
        return newsPageFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if(mNewsType==VideoNewsPage.TYPE_SINGAL_NEWS && mNewsId!=-1){
            count = 1;
        }
        if (mNewsList != null) {
            count = mNewsList.size();
        }
        return count;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        if(Utility.DEBUG)Log.v(TAG, TAG + "$$$destroyItem");
        FragmentTransaction trans = mFragmentManager.beginTransaction();
        trans.remove((Fragment)object);
        trans.commit();

        super.destroyItem(container, position, object);
    }
}

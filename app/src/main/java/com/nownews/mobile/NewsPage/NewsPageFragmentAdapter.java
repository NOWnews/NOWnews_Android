package com.nownews.mobile.NewsPage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.NewsInfoJson.ReferenceNewsInfo;
import com.nownews.mobile.Json.NewsListJson.NewsContent;
import com.nownews.mobile.Json.SearchInfoJson.SearchInfoContent;
import com.nownews.mobile.NewsPage.NewsPage.SingalNewsType;

import java.util.List;

public class NewsPageFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private List<NewsContent> mNewsList;
    private List<NewsContent> mHeadlineNewsList;
    private List<NewsContent> mInstantNewsContent;
    private List<ReferenceNewsInfo> mReferenceNewsList;
    private List<SearchInfoContent> mSearchList;
    private String mNewsUrl;
    private int mNewsId;
    private FragmentManager mFragmentManager;

    public NewsPageFragmentAdapter(Context aContext, FragmentManager fm, List<NewsContent> aNewsList) {
        super(fm);
        mContext = aContext;
        mNewsList = aNewsList;
        mFragmentManager = fm;
    }

    public NewsPageFragmentAdapter(Context aContext, FragmentManager fm, List<?> aNewsList, int aType) {
        super(fm);
        mContext = aContext;
        switch (aType) {
            case NewsPage.TYPE_HEADLINE_NEWS:
                mHeadlineNewsList = (List<NewsContent>) aNewsList;
                break;
            case NewsPage.TYPE_INSTANT_NEWS:
                mInstantNewsContent = (List<NewsContent>) aNewsList;
                break;
            case NewsPage.TYPE_SEARCH_NEWS:
                mSearchList = (List<SearchInfoContent>) aNewsList;
                break;
            case NewsPage.TYPE_REFERENCE_NEWS:
                mReferenceNewsList = (List<ReferenceNewsInfo>) aNewsList;
        }
    }

    public NewsPageFragmentAdapter(NewsPage aContext, FragmentManager fm, Object aValue, SingalNewsType aType) {
        super(fm);
        mContext = aContext;
        if (aType == SingalNewsType.url) {
            mNewsUrl = (String) aValue;
        } else if (aType == SingalNewsType.id) {
            mNewsId = (int) aValue;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
//        NewsPageFragment newsPageFragment = null;
        NewsPageRecyclerViewFragment newsPageFragment = null;
        String newsUrl = null;
        int newsId = -1;
        if (mNewsList != null) {
            List<NewsContent> newsInfoList = UserDataInfo.getNewsList();
            if (newsInfoList != null
                    && newsInfoList.get(position) != null
                    && newsInfoList.get(position)._id != -1) {
                newsId = newsInfoList.get(position)._id;
            }
        } else if (mHeadlineNewsList != null
                && mHeadlineNewsList.get(position) != null
                && mHeadlineNewsList.get(position)._id != -1) {

            newsId = mHeadlineNewsList.get(position)._id;

        } else if (mInstantNewsContent != null
                && mInstantNewsContent.get(position) != null
                && mInstantNewsContent.get(position)._id != -1) {

            newsId = mInstantNewsContent.get(position)._id;

        } else if (mSearchList != null
                && mSearchList.get(position) != null
                && mSearchList.get(position)._id != -1) {

            newsId = mSearchList.get(position)._id;

        } else if (mReferenceNewsList != null
                && mReferenceNewsList.get(position) != null
                && mReferenceNewsList.get(position)._id != -1) {

            newsId = mReferenceNewsList.get(position)._id;

        } else if (mNewsUrl != null && !mNewsUrl.trim().equals("")) {
            newsUrl = mNewsUrl;
        } else if (mNewsId != -1) {
            newsId = mNewsId;
        }
        newsPageFragment = new NewsPageRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(NewsPageRecyclerViewFragment.KEY_NEWS_URL, newsUrl);
        bundle.putInt(NewsPageRecyclerViewFragment.KEY_NEWS_ID, newsId);
        newsPageFragment.setArguments(bundle);
        return newsPageFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mNewsList != null) {
            count = mNewsList.size();
        } else if (mHeadlineNewsList != null) {
            count = mHeadlineNewsList.size();
        } else if (mInstantNewsContent != null) {
            count = mInstantNewsContent.size();
        } else if (mSearchList != null) {
            count = mSearchList.size();
        } else if (mReferenceNewsList != null) {
            count = mReferenceNewsList.size();
        } else if (mNewsUrl != null) {
            count = 1;
        } else if (mNewsId != -1) {
            count = 1;
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

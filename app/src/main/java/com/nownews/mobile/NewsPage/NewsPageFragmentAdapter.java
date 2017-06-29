package com.nownews.mobile.NewsPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.HeadlineNewsJson;
import com.nownews.mobile.Json.InstantNewsJson;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.RelationsNewsInfoJson;
import com.nownews.mobile.Json.SearchInfoJson;
import com.nownews.mobile.Json.SpecialNewsListJson;
import com.nownews.mobile.NewsPage.NewsPage.SingalNewsType;

import java.util.List;

public class NewsPageFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private List<NewsListJson.NewsListBean> mNewsList;
    private List<HeadlineNewsJson.CarouselsBean> mHeadlineNewsList;
    private List<InstantNewsJson.NewsListBean> mInstantNewsContent;
    private List<RelationsNewsInfoJson.RelationsNewsBean> mReferenceNewsList;
    private List<SpecialNewsListJson.NewsListBean> mSpecialNewsList;
    private List<SearchInfoJson.NewsListBean> mSearchList;
    private String mNewsUrl;
    private int mNewsId;
    private FragmentManager mFragmentManager;

    public NewsPageFragmentAdapter(FragmentManager fm, List<NewsListJson.NewsListBean> aNewsList) {
        super(fm);
        mNewsList = aNewsList;
        mFragmentManager = fm;
    }

    public NewsPageFragmentAdapter(FragmentManager fm, List<?> aNewsList, int aType) {
        super(fm);
        switch (aType) {
            case NewsPage.TYPE_HEADLINE_NEWS:
                mHeadlineNewsList = (List<HeadlineNewsJson.CarouselsBean>) aNewsList;
                break;
            case NewsPage.TYPE_INSTANT_NEWS:
                mInstantNewsContent = (List<InstantNewsJson.NewsListBean>) aNewsList;
                break;
            case NewsPage.TYPE_SEARCH_NEWS:
                mSearchList = (List<SearchInfoJson.NewsListBean>) aNewsList;
                break;
            case NewsPage.TYPE_REFERENCE_NEWS:
                mReferenceNewsList = (List<RelationsNewsInfoJson.RelationsNewsBean>) aNewsList;
                break;
            case NewsPage.TYPE_SPECIAL_NEWS:
                mSpecialNewsList = (List<SpecialNewsListJson.NewsListBean>) aNewsList;
                break;

        }
    }

    public NewsPageFragmentAdapter(FragmentManager fm, Object aValue, SingalNewsType aType) {
        super(fm);
        if (aType == SingalNewsType.url) {
            mNewsUrl = (String) aValue;
        } else if (aType == SingalNewsType.id) {
            mNewsId = (int) aValue;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
        NewsPageRecyclerViewFragment newsPageFragment = null;
        String newsUrl = null;
        int newsId = -1;
        if (mNewsList != null) {
            List<NewsListJson.NewsListBean> newsInfoList = UserDataInfo.getNewsList();
            if (newsInfoList != null
                    && newsInfoList.get(position) != null
                    && newsInfoList.get(position).getSn() != -1) {
                newsId = newsInfoList.get(position).getSn();
            }
        } else if (mHeadlineNewsList != null
                && mHeadlineNewsList.get(position) != null
                && mHeadlineNewsList.get(position).getSn() != -1) {

            newsId = mHeadlineNewsList.get(position).getSn();

        } else if (mInstantNewsContent != null
                && mInstantNewsContent.get(position) != null
                && mInstantNewsContent.get(position).getSn() != -1) {

            newsId = mInstantNewsContent.get(position).getSn();

        } else if (mSearchList != null
                && mSearchList.get(position) != null
                && mSearchList.get(position).getSn() != -1) {

            newsId = mSearchList.get(position).getSn();

        } else if (mReferenceNewsList != null
                && mReferenceNewsList.get(position) != null
                && mReferenceNewsList.get(position).getSn() != -1) {

            newsId = mReferenceNewsList.get(position).getSn();

        } else if (mSpecialNewsList != null
                && mSpecialNewsList.get(position) != null
                && mSpecialNewsList.get(position).getSn() != -1) {

            newsId = mSpecialNewsList.get(position).getSn();

        }else if (mNewsUrl != null && !mNewsUrl.trim().equals("")) {
            newsUrl = mNewsUrl;
        } else if (mNewsId != -1) {
            newsId = mNewsId;
        }
        newsPageFragment = new NewsPageRecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(NewsPageRecyclerViewFragment.KEY_NEWS_CATEGORY, mNewsCategory);
        bundle.putString(NewsPageRecyclerViewFragment.KEY_NEWS_BIG_CATEGORY, mBigCategory);
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
        } else if (mSpecialNewsList !=null) {
            count = mSpecialNewsList.size();
        }else if (mNewsUrl != null) {
            count = 1;
        } else if (mNewsId != -1) {
            count = 1;
        }
        return count;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        if(Utility.DEBUG)Log.v(TAG, TAG + "$$$destroyItem");
        if(mFragmentManager==null){
            return;
        }
        FragmentTransaction trans = mFragmentManager.beginTransaction();
        trans.remove((Fragment)object);
        trans.commitAllowingStateLoss();

        super.destroyItem(container, position, object);
    }

    private String mNewsCategory;
    private String mBigCategory;
    public void setCategory(String aBigCategory, String aNewsCategory) {
        mBigCategory = aBigCategory;
        mNewsCategory = aNewsCategory;
    }
}

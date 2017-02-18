package com.nownews.mobile.NewsCategory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.NewsCategoryJson.CategoryInfo;
import com.nownews.mobile.Widget.WebFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsCategoryFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private List<CategoryInfo> mCategoryContent;
    private int mPageCount;
//    private HashMap<String, Fragment> mNewsListFragmentList;
    private Handler mHandler;
    private RecyclerView.RecycledViewPool mPool = new RecyclerView.RecycledViewPool();
    private FragmentManager mFragmentManager;

    public NewsCategoryFragmentAdapter(Context aContext, FragmentManager fm, List<CategoryInfo> aCategoryContent, int aPageCount, Handler aHandler) {
        super(fm);
        mContext = aContext;
        mCategoryContent = aCategoryContent;
        mPageCount = aPageCount;
        mHandler = aHandler;
        mFragmentManager = fm;
    }

    public void setData(List<CategoryInfo> aCategoryContent, int aPageCount) {
        mCategoryContent = aCategoryContent;
        mPageCount = aPageCount;
        notifyDataSetChanged();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        String mTitle = mCategoryContent.get(position).name;
        mTitle = "	" + mTitle.substring(mTitle.lastIndexOf("_") + 1, mTitle.length()) + "	";
        return mTitle;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem::: " + position);
        Fragment fragment = null;
        int id = -1;
//        if (mNewsListFragmentList == null) {
//            mNewsListFragmentList = new HashMap<String, Fragment>();
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
//                    fragment = mNewsListFragmentList.get(KEY_FRAGMENT_INDEX + position);
//                    if (fragment != null) {
//                        return fragment;
//                    }
//                }
//            }
//        }
        String itemName = mCategoryContent.get(position).name;
        itemName = itemName.substring(itemName.lastIndexOf("_") + 1, itemName.length());
        if (Utility.DEBUG) Log.e(TAG, "itemName: " + itemName);
        if (mCategoryContent != null) {
            for (int i = 0; i < mCategoryContent.size(); i++) {
                if (mCategoryContent.get(i) != null
                        && mCategoryContent.get(i).name != null
                        && !mCategoryContent.get(i).name.trim().isEmpty()) {
                    String categoryName = mCategoryContent.get(i).name;
                    if (itemName.equals(categoryName)) {
                        id = mCategoryContent.get(i).tid;
                        break;
                    }
                }
            }
        }
        if (id == -1) {
            if (itemName.equals(mContext.getString(R.string.food))) {
                fragment = new WebFragment();
                Bundle bundle = new Bundle();
                bundle.putString(WebFragment.KEY_URL, WebAPIUrl.FOOD);
                fragment.setArguments(bundle);
//                mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, fragment);
            } else if (itemName.equals(mContext.getString(R.string.health))) {
                fragment = new WebFragment();
                Bundle bundle = new Bundle();
                bundle.putString(WebFragment.KEY_URL, WebAPIUrl.Health);
                fragment.setArguments(bundle);
//                mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, fragment);
            } else {
                fragment = new OtherNewsListFragment();
                ((OtherNewsListFragment) fragment).setData(mHandler, mPool);
                Bundle bundle = new Bundle();
                bundle.putString(OtherNewsListFragment.KEY_CATEGORY_NAME, itemName);
                fragment.setArguments(bundle);
//                mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, fragment);
            }
        } else {
            fragment = new NewsListFragment();
            ((NewsListFragment) fragment).setData(id, mHandler, itemName, mPool);
//            mNewsListFragmentList.put(KEY_FRAGMENT_INDEX + position, fragment);
        }
        Log.d(TAG, "is fragment null or not?? " + (fragment == null ? "true" : "false"));
        return fragment;
    }

    @Override
    public int getCount() {
        return mPageCount;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        if(Utility.DEBUG)Log.v(TAG, TAG + "$$$destroyItem");
        FragmentTransaction trans = mFragmentManager.beginTransaction();
        trans.remove((Fragment)object);
        trans.commit();

        super.destroyItem(container, position, object);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        try{
            super.finishUpdate(container);
        } catch (NullPointerException nullPointerException){
            System.out.println("Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
        }
    }
}

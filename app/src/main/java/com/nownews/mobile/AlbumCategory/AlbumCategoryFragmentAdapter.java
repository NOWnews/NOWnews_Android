package com.nownews.mobile.AlbumCategory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.PhotosCategoryJson.CategoryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumCategoryFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private ArrayList<String> mNewsCategoryList;
    private List<CategoryInfo> mCategoryContent;
    private String mNewsUrlFormat;
    private int mPageCount;
    private HashMap<String, AlbumGridFragment> mAlbumGridFragmentList;
    private Handler mHandler;

    public AlbumCategoryFragmentAdapter(Context aContext, FragmentManager fm, ArrayList<String> aNewsCategoryList, int aPageCount, Handler aHandler) {
        super(fm);
        if (Utility.DEBUG) Log.e(TAG, "AlbumCategoryFragmentAdapter%%%%%");
        mContext = aContext;
        mNewsCategoryList = aNewsCategoryList;
        mPageCount = aPageCount;
        mHandler = aHandler;
        init();
    }

    public void setData(ArrayList<String> aNewsCategoryList, int aPageCount) {
        mNewsCategoryList = aNewsCategoryList;
        mPageCount = aPageCount;
        notifyDataSetChanged();
    }

    private void init() {
        mCategoryContent = UserDataInfo.getPhotosCategoryContent();
        if (mCategoryContent == null) {
            return;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String mTitle = mNewsCategoryList.get(position);
        mTitle = "	" + mTitle.substring(mTitle.lastIndexOf("_") + 1, mTitle.length()) + "	";
        return mTitle;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem::: " + position);
        AlbumGridFragment albumGridFragment;
        int id = -1;
        if (mAlbumGridFragmentList == null) {
            mAlbumGridFragmentList = new HashMap<String, AlbumGridFragment>();
        } else {
            if (mAlbumGridFragmentList.size() > 0) {
                boolean isFragmentExist = false;
                for (String key : mAlbumGridFragmentList.keySet()) {
                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
                        isFragmentExist = true;
                        break;
                    }
                }
                if (isFragmentExist) {
                    albumGridFragment = mAlbumGridFragmentList.get(KEY_FRAGMENT_INDEX + position);
                    if (albumGridFragment != null) {
                        return albumGridFragment;
                    }
                }
            }
        }
        String itemName = mNewsCategoryList.get(position);
        itemName = itemName.substring(itemName.lastIndexOf("_") + 1, itemName.length());
        if (Utility.DEBUG) Log.e(TAG, "itemName: " + itemName);
        if (mCategoryContent != null) {
            for (int i = 0; i < mCategoryContent.size(); i++) {
                if (mCategoryContent.get(i) != null
                        && mCategoryContent.get(i).name != null
                        && !mCategoryContent.get(i).name.trim().isEmpty()) {
                    String categoryName = mCategoryContent.get(i).name;
                    if (Utility.DEBUG) Log.e(TAG, "categoryName: " + categoryName);
                    if (itemName.equals(categoryName)) {
                        id = mCategoryContent.get(i).tid;
                        break;
                    }
                }
            }
        }
        albumGridFragment = new AlbumGridFragment();
        albumGridFragment.setData(id, mHandler, itemName);
        mAlbumGridFragmentList.put(KEY_FRAGMENT_INDEX + position, albumGridFragment);
        return albumGridFragment;
    }

    @Override
    public int getCount() {
        return mPageCount;
    }

    public void clearFragmentList() {
        if (Utility.DEBUG) Log.e(TAG, "clearFragmentList 111");
        if (mAlbumGridFragmentList != null && mAlbumGridFragmentList.size() >= 0) {
            if (Utility.DEBUG)
                Log.e(TAG, "mAlbumGridFragmentList.size(): " + mAlbumGridFragmentList.size());
            mAlbumGridFragmentList.clear();
            if (Utility.DEBUG)
                Log.e(TAG, "mAlbumGridFragmentList.size(): " + mAlbumGridFragmentList.size());
            mAlbumGridFragmentList = null;
        }
    }

}

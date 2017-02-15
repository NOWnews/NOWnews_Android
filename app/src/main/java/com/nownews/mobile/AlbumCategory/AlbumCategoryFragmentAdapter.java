package com.nownews.mobile.AlbumCategory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.nownews.mobile.Json.PhotosCategoryJson;
import com.nownews.mobile.Json.PhotosCategoryJson.CategoryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumCategoryFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private List<CategoryInfo> mCategoryContent;
    private String mNewsUrlFormat;
    private int mPageCount;
//    private HashMap<String, AlbumGridFragment> mAlbumGridFragmentList;
    private Handler mHandler;
    private RecyclerView.RecycledViewPool mPool = new RecyclerView.RecycledViewPool();
    private FragmentManager mFragmentManager;

    public AlbumCategoryFragmentAdapter(Context aContext, FragmentManager fm, List<PhotosCategoryJson.CategoryInfo> aCategoryContent, int aPageCount, Handler aHandler) {
        super(fm);
        if (Utility.DEBUG) Log.e(TAG, "AlbumCategoryFragmentAdapter%%%%%");
        mContext = aContext;
        mFragmentManager = fm;
        mCategoryContent = aCategoryContent;
        mPageCount = aPageCount;
        mHandler = aHandler;
    }

    public void setData(List<PhotosCategoryJson.CategoryInfo> aCategoryContent, int aPageCount) {
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
        AlbumGridFragment albumGridFragment;
        int id = -1;
//        if (mAlbumGridFragmentList == null) {
//            mAlbumGridFragmentList = new HashMap<String, AlbumGridFragment>();
//        } else {
//            if (mAlbumGridFragmentList.size() > 0) {
//                boolean isFragmentExist = false;
//                for (String key : mAlbumGridFragmentList.keySet()) {
//                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
//                        isFragmentExist = true;
//                        break;
//                    }
//                }
//                if (isFragmentExist) {
//                    albumGridFragment = mAlbumGridFragmentList.get(KEY_FRAGMENT_INDEX + position);
//                    if (albumGridFragment != null) {
//                        return albumGridFragment;
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
                    if (Utility.DEBUG) Log.e(TAG, "categoryName: " + categoryName);
                    if (itemName.equals(categoryName)) {
                        id = mCategoryContent.get(i).tid;
                        break;
                    }
                }
            }
        }
        albumGridFragment = new AlbumGridFragment();
        albumGridFragment.setData(id, mHandler, itemName, mPool);
//        mAlbumGridFragmentList.put(KEY_FRAGMENT_INDEX + position, albumGridFragment);
        return albumGridFragment;
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
}

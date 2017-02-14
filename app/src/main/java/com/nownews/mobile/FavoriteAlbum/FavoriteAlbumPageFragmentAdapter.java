package com.nownews.mobile.FavoriteAlbum;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteAlbumPageFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private ArrayList<String> mFavoriteAlbumList;
    private HashMap<String, FavoritePageFragment> mAlbumPageFragmentList;
    private SharedPreferencesMethods mSharedPref;
    private int mCurrentType;
    private String mImageTitle;
    private String mNewsUrl;

    public FavoriteAlbumPageFragmentAdapter(Context aContext, FragmentManager fm, ArrayList<String> aFavoriteAlbumList, int aCurrentType) {
        super(fm);
        mContext = aContext;
        mFavoriteAlbumList = aFavoriteAlbumList;
        mCurrentType = aCurrentType;
        mSharedPref = new SharedPreferencesMethods(mContext);
    }

    public FavoriteAlbumPageFragmentAdapter(Context aContext, FragmentManager fm, ArrayList<String> aFavoriteAlbumList, int aCurrentType, String aImageTitle, String aNewsUrl) {
        super(fm);
        mContext = aContext;
        mFavoriteAlbumList = aFavoriteAlbumList;
        mCurrentType = aCurrentType;
        mImageTitle = aImageTitle;
        mNewsUrl = aNewsUrl;
        mSharedPref = new SharedPreferencesMethods(mContext);
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
        FavoritePageFragment albumPageFragment = null;
        String imageUrl = null;
        String id = null;
        String imageTitle = null;
        String imageNewsUrl = null;

        if (mCurrentType == FavoriteAlbumPage.TYPE_NEWS_IMAGES) {
            if (mFavoriteAlbumList != null
                    && mFavoriteAlbumList.get(position) != null) {
                imageUrl = mFavoriteAlbumList.get(position);
                imageTitle = mImageTitle;
                imageNewsUrl = mNewsUrl;
            }
        } else if (mCurrentType == FavoriteAlbumPage.TYPE_FAVORITE) {
            if (mFavoriteAlbumList != null
                    && mFavoriteAlbumList.get(position) != null) {
                imageUrl = mFavoriteAlbumList.get(position);
                id = mSharedPref.searchFavoriteId(imageUrl);
                imageTitle = mSharedPref.searchFavoriteAlbumTitle(id);
                imageNewsUrl = mSharedPref.searchFavoriteAlbumNewsUrl(id);

                if(mSharedPref!=null){
                    mSharedPref.unRegistContext(mContext);
                }
            }
        }

        if (mAlbumPageFragmentList == null) {
            mAlbumPageFragmentList = new HashMap<String, FavoritePageFragment>();
        } else {
            if (mAlbumPageFragmentList.size() > 0) {
                boolean isFragmentExist = false;
                for (String key : mAlbumPageFragmentList.keySet()) {
                    if (key.equals(KEY_FRAGMENT_INDEX + position)) {
                        isFragmentExist = true;
                        break;
                    }
                }
                if (isFragmentExist) {
                    albumPageFragment = mAlbumPageFragmentList.get(KEY_FRAGMENT_INDEX + position);
                    if (albumPageFragment != null) {
                        return albumPageFragment;
                    }
                }
            }
        }
        albumPageFragment = new FavoritePageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FavoritePageFragment.KEY_IMAGE_URL, imageUrl);
        bundle.putString(FavoritePageFragment.KEY_IMAGE_TITLE, imageTitle);
        bundle.putString(FavoritePageFragment.KEY_IMAGE_NEWS_URL, imageNewsUrl);
        albumPageFragment.setArguments(bundle);
        mAlbumPageFragmentList.put(KEY_FRAGMENT_INDEX + position, albumPageFragment);
        return albumPageFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mFavoriteAlbumList != null) {
            count = mFavoriteAlbumList.size();
        }
        return count;
    }

    public void clearFragmentList() {
        if (mAlbumPageFragmentList != null && mAlbumPageFragmentList.size() >= 0) {
            mAlbumPageFragmentList.clear();
            mAlbumPageFragmentList = null;
        }
    }

}

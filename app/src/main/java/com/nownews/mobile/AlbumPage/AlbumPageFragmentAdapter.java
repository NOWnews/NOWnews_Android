package com.nownews.mobile.AlbumPage;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.PhotosInfoJson;
import com.nownews.mobile.Json.PhotosInfoJson.PhotoInfo;

import java.util.HashMap;

public class AlbumPageFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private PhotosInfoJson mAlbumInfo;
//    private HashMap<String, AlbumPageFragment> mAlbumPageFragmentList;

    public AlbumPageFragmentAdapter(Context aContext, FragmentManager fm, PhotosInfoJson aAlbumInfo) {
        super(fm);
        mContext = aContext;
        mAlbumInfo = aAlbumInfo;
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "getItem() position: " + position);
        AlbumPageFragment albumPageFragment = null;
        PhotoInfo imageInfo = null;
        if (mAlbumInfo != null
                && mAlbumInfo.collectionImages != null
                && mAlbumInfo.collectionImages.size() > 0) {
            imageInfo = mAlbumInfo.collectionImages.get(position);
//            if (mAlbumPageFragmentList == null) {
//                mAlbumPageFragmentList = new HashMap<String, AlbumPageFragment>();
//            } else {
//                if (mAlbumPageFragmentList.size() > 0) {
//                    boolean isFragmentExist = false;
//                    for (String key : mAlbumPageFragmentList.keySet()) {
//                        if (key.equals(KEY_FRAGMENT_INDEX + position)) {
//                            isFragmentExist = true;
//                            break;
//                        }
//                    }
//                    if (isFragmentExist) {
//                        albumPageFragment = mAlbumPageFragmentList.get(KEY_FRAGMENT_INDEX + position);
//                        if (albumPageFragment != null) {
//                            return albumPageFragment;
//                        }
//                    }
//                }
//            }
            albumPageFragment = new AlbumPageFragment();
            albumPageFragment.setImageInfo(imageInfo);
//            mAlbumPageFragmentList.put(KEY_FRAGMENT_INDEX + position, albumPageFragment);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.data_error), Toast.LENGTH_LONG).show();
        }
        return albumPageFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mAlbumInfo != null
                && mAlbumInfo.collectionImages != null
                && mAlbumInfo.collectionImages.size() > 0) {
            count = mAlbumInfo.collectionImages.size();
        }
        return count;
    }

    public void clearFragmentList() {
//        if (mAlbumPageFragmentList != null && mAlbumPageFragmentList.size() >= 0) {
//            mAlbumPageFragmentList.clear();
//            mAlbumPageFragmentList = null;
//        }
    }

}

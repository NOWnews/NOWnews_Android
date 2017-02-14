package com.nownews.mobile;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.NewsListJson.NewsContent;

import java.util.HashMap;
import java.util.List;

public class HeadlineFragmentAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final String KEY_FRAGMENT_INDEX = "fragment_";
    private Context mContext;
    private HashMap<String, HeadlineFragment> mHeadlineFragmentList;
    private List<NewsContent> mHeadlineContent;

    public HeadlineFragmentAdapter(Context aContext, FragmentManager fm, List<NewsContent> aHeadlineContent) {
        super(fm);
        if (Utility.DEBUG)
            Log.v(TAG, "HeadlineFragmentAdapter()!!! extends FragmentStatePagerAdapter");
        mContext = aContext;
        mHeadlineContent = aHeadlineContent;
    }

    public void setData(List<NewsContent> aBig3Small6Content) {
        if (Utility.DEBUG) Log.v(TAG, "setData()!!!");
        mHeadlineContent = aBig3Small6Content;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (Utility.DEBUG) Log.e(TAG, "====== getItem() position: " + position);
        HeadlineFragment headlineFragment = null;
        if (mHeadlineContent != null) {
            if (mHeadlineFragmentList == null) {
                mHeadlineFragmentList = new HashMap<String, HeadlineFragment>();
            } else {
                if (mHeadlineFragmentList.size() > 0) {
                    boolean isFragmentExist = false;
                    for (String key : mHeadlineFragmentList.keySet()) {
                        if (key.equals(KEY_FRAGMENT_INDEX + position)) {
                            isFragmentExist = true;
                            break;
                        }
                    }
                    if (isFragmentExist) {
                        headlineFragment = mHeadlineFragmentList.get(KEY_FRAGMENT_INDEX + position);
                        if (headlineFragment != null) {
                            return headlineFragment;
                        }
                    }
                }
            }
            headlineFragment = new HeadlineFragment();
            headlineFragment.setData(mHeadlineContent, position);
            mHeadlineFragmentList.put(KEY_FRAGMENT_INDEX + position, headlineFragment);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.data_error), Toast.LENGTH_LONG).show();
        }
        return headlineFragment;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mHeadlineContent != null) {
            count = mHeadlineContent.size();
        }
        return count;
    }

    public void clearFragmentList() {
        if (mHeadlineFragmentList != null && mHeadlineFragmentList.size() >= 0) {
            mHeadlineFragmentList.clear();
            mHeadlineFragmentList = null;
        }
    }

}

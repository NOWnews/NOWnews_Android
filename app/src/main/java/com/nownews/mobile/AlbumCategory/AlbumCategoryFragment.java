package com.nownews.mobile.AlbumCategory;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.PhotosCategoryJson;
import com.nownews.mobile.Widget.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

public class AlbumCategoryFragment extends Fragment {

    public final static String KEY_POSITION = "position";
    private final String TAG = getClass().getSimpleName();
    private final int REQUEST_CODE = 0x123;
    private final int RESULT_CODE = 0x321;
    private RelativeLayout vTabGroup;
    private SlidingTabLayout vTab;
    private ViewPager vViewPager;
    private TextView vErrorMessage;
    private AlbumCategoryFragmentAdapter mAdapter;
    private int mPosition;
    private SharedPreferencesMethods mSharedPref;
    private OnPageChangeListener mViewPageChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {
            if (Utility.DEBUG) Log.e(TAG, "onPageSelected()");
            String categoryName = mPhotoCategoryContent.get(position).name;
            categoryName = categoryName.substring(categoryName.lastIndexOf("_") + 1, categoryName.length());
            GoogleAnalyticsFunction.sendHitInfo(getActivity(), "圖集列表", "切換至" + categoryName + "圖集", "");
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (Utility.DEBUG) Log.e(TAG, "onPageScrolled");
            if (checkDirection) {
                if (Utility.DEBUG) Log.d(TAG, "position: " + position);
                if (Utility.DEBUG) Log.d(TAG, "thresholdOffset: " + thresholdOffset);
                if (Utility.DEBUG) Log.d(TAG, "positionOffset: " + positionOffset);
                if (positionOffset == 0.0) {
                    if (position == 0) {
                        Toast.makeText(getActivity(), "這是第一個圖集類別喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(getActivity(), "最後一個圖集類別囉!", Toast.LENGTH_LONG).show();
                    }
                } else if (thresholdOffset > positionOffset) {
                    if (Utility.DEBUG) Log.i(TAG, "going left");
                } else {
                    if (Utility.DEBUG) Log.i(TAG, "going right");
                }
                checkDirection = false;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (!scrollStarted && state == ViewPager.SCROLL_STATE_DRAGGING) {
                scrollStarted = true;
                checkDirection = true;
            } else {
                scrollStarted = false;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_news_category, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startFragment();

    }

    private void startFragment() {

        initController();
        processBundle();
        processView();
        processListener();
        processData();

    }

    private void processData(){
//        mPhotoCategoryContent = UserDataInfo.getPhotosCategoryContent();
//        if(mPhotoCategoryContent==null){
            getPhotosCategory();
//        }else{
//            setTab();
//        }
    }

    private ApiController mApiController;
    private void initController() {
        mSharedPref = new SharedPreferencesMethods(getActivity());
        mApiController = ApiController.getInstance();
    }

    private void processBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt(KEY_POSITION);
        }
    }

    private void processView() {

        View view = getView();

        vTabGroup = (RelativeLayout) view.findViewById(R.id.tab_group);
        vTabGroup.setBackgroundColor(getResources().getColor(R.color.photos_tab_bar_background_color));
        vTab = (SlidingTabLayout) view.findViewById(R.id.tab);
        vViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

    }

    private void processListener() {

    }

    private void setTab() {

        if (mPhotoCategoryContent == null && mPhotoCategoryContent.size() == 0) {

            if (vViewPager != null) {
                vViewPager.setAdapter(null);
            }
            if (vTab != null) {
                vTab.setViewPager(null);
            }
            return;

        } else if (mPhotoCategoryContent != null && mPhotoCategoryContent.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new AlbumCategoryFragmentAdapter(getActivity(), getChildFragmentManager(), mPhotoCategoryContent, mPhotoCategoryContent.size(), mUiHandler);
            } else {
                mAdapter.setData(mPhotoCategoryContent, mPhotoCategoryContent.size());
            }
            vViewPager.setAdapter(mAdapter);
            vViewPager.setCurrentItem(mPosition);
            vViewPager.addOnPageChangeListener(mViewPageChangeListener);
            vTab.setBackgroundColor(getResources().getColor(R.color.photos_tab_bar_background_color));
            vTab.setCustomTabView(R.layout.widget_tab_layout, 0);
            vTab.setDistributeEvenly(true);
            vTab.setPhotosCategoryContent(mPhotoCategoryContent);
            vTab.setTextViewTextSize(20);
            vTab.setSelectedIndicatorColors(getResources().getColor(android.R.color.transparent));
            vTab.setViewPager(vViewPager);
        }

    }

    public static final int LOAD_PAGE_START = 0x617;
    public static final int LOAD_PAGE_END = 0x357;
    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case LOAD_PAGE_START:
                    ((AlbumActivity)getActivity()).showSnackBar();
                    break;
                case LOAD_PAGE_END:
                    ((AlbumActivity)getActivity()).dissmissSnackBar();
                    break;

            }

        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            setTab();
        }

    }

    private void getPhotosCategory() {
        if (Utility.DEBUG) Log.v(TAG, "getPhotosCategory()");
        if (mApiController != null) {
            mApiController.getPhotosCategory(mApiHandler);
        }
    }

    public boolean isApiLoadingSuccess;
    private int mRetryCount;
    private final int RELOAD_API = 0x159;
    private List<PhotosCategoryJson.CategoryInfo> mPhotoCategoryContent;
    private Handler mApiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_PHOTOS_CATEGORY_DONE:
                    isApiLoadingSuccess = true;
                    mRetryCount = 0;
                    mPhotoCategoryContent = (List<PhotosCategoryJson.CategoryInfo>) msg.obj;
                    processCategory();
                    setTab();
                    break;
                case ParameterSet.GET_PHOTOS_CATEGORY_FAILED:
                    if (Utility.DEBUG)Log.e(TAG, "mRetryCount: " + mRetryCount);
                    isApiLoadingSuccess = false;
                    if(mRetryCount<5){
                        mRetryCount++;
                        if(hasMessages(RELOAD_API)){
                            removeMessages(RELOAD_API);
                        }
                        sendEmptyMessage(RELOAD_API);
                        break;
                    }else{
                        mRetryCount = 0;
                        vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_PHOTOS_CATEGORY_FAILED));
                    }
                    break;
                case RELOAD_API:
                    getPhotosCategory();
                    break;
            }

        }
    };

    private void processCategory() {

        if (mPhotoCategoryContent == null) {
            return;
        }

//        //Album
//        for (int i = 0; i < mPhotoCategoryContent.size(); i++) {
//            PhotosCategoryJson.CategoryInfo content = mPhotoCategoryContent.get(i);
//            String categoryName = content.name;
//            mSharedPref.saveHomeNewsItems(i + "_" + SharedPreferencesMethods.HOME_ALBUM_ITEMS + categoryName, true);
//        }
    }

    @Override
    public void onResume() {

        GoogleAnalyticsFunction.setScreenName(getActivity(), "圖集列表");

        super.onResume();
    }

    @Override
    public void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(getActivity());
        }
        super.onDestroyView();
    }

    public void reload() {
        startFragment();
    }

}

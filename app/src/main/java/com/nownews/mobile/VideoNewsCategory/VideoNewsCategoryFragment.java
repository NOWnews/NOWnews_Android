package com.nownews.mobile.VideoNewsCategory;

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
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.VideosCategoryJson.CategoryInfo;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;
import com.nownews.mobile.Widget.SlidingTabLayout;

import java.lang.ref.WeakReference;
import java.util.List;

public class VideoNewsCategoryFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    public final static String KEY_POSITION = "position";
    public static final int LOAD_PAGE_START = 0x617;
    public static final int LOAD_PAGE_END = 0x357;
    private final int REQUEST_CODE = 0x123;
    private final int RESULT_CODE = 0x321;
    //View
    private RelativeLayout vTabGroup;
    private SlidingTabLayout vTab;
    private ViewPager vViewPager;
    private RelativeLayout vLoadingPageLayout;
    private TextView vErrorMessage;
    //View

    //For DFP End
    private VideoNewsCategoryFragmentAdapter mAdapter;
    private int mPosition;
    //For DFP Start

    private OnPageChangeListener mViewPageChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {

            int currentPage = vViewPager.getCurrentItem();
            VideoNewsListFragment fragment = (VideoNewsListFragment) mAdapter.instantiateItem(vViewPager, currentPage);
            ((VideoNewsListFragment)fragment).destroyAD2();

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (checkDirection) {
                if (positionOffset == 0.0) {
                    if (position == 0) {
                        Toast.makeText(getActivity(), "這是第一個新聞類別喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(getActivity(), "最後一個新聞類別囉!", Toast.LENGTH_LONG).show();
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
    private boolean isLadingLayoutVisible = false;
    private ApiController mApiController;
    private List<CategoryInfo> mVideoNewsCategoryContent;
    private UiHandler mUiHandler;
    private static class UiHandler extends Handler {

        private final WeakReference<VideoNewsCategoryFragment> mFragment;

        public UiHandler(VideoNewsCategoryFragment aFragment){
            mFragment = new WeakReference<VideoNewsCategoryFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            VideoNewsCategoryFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case LOAD_PAGE_START:
                    ((NewHome)fragment.getActivity()).showSnackBar();
                    break;
                case LOAD_PAGE_END:
                    ((NewHome)fragment.getActivity()).dissmissSnackBar();
                    break;

            }

        }

    };

    private int mRetryCount;
    private final static int RELOAD_API = 0x159;
    public boolean isApiLoadingSuccess;
    private ApiHandler mApiHandler;
    private static class ApiHandler extends Handler{

        private String TAG = getClass().getSimpleName();
        private final WeakReference<VideoNewsCategoryFragment> mFragment;

        public ApiHandler(VideoNewsCategoryFragment aFragment){
            mFragment = new WeakReference<VideoNewsCategoryFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            VideoNewsCategoryFragment fragment = mFragment.get();

            switch (msg.what) {
                case ParameterSet.GET_VIDEOS_CATEGORY_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mVideoNewsCategoryContent = (List<CategoryInfo>) msg.obj;
                    fragment.setTab();
                    break;
                case ParameterSet.GET_VIDEOS_CATEGORY_FAILED:
                    if (Utility.DEBUG)Log.e(TAG, "mRetryCount: " + fragment.mRetryCount);
                    fragment.isApiLoadingSuccess = false;
                    if(fragment.mRetryCount<5){
                        fragment.mRetryCount++;
                        if(hasMessages(RELOAD_API)){
                            removeMessages(RELOAD_API);
                        }
                        sendEmptyMessage(RELOAD_API);
                        break;
                    }else{
                        fragment.mRetryCount = 0;
                        fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_VIDEOS_CATEGORY_FAILED));
                    }
                    break;
                case RELOAD_API:
                    fragment.getVideoNewsCategory();
                    break;
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

    //TODO: startFragment()
    private void startFragment() {
        initController();
        processArgument();
        processView();
        processListener();
        processData();
    }

    private void processData(){
//        mVideoNewsCategoryContent = UserDataInfo.getVideosCategoryContent();
//        if(mVideoNewsCategoryContent ==null){
            getVideoNewsCategory();
//        }else{
//            setTab();
//        }
    }

    private void getVideoNewsCategory() {
        if (Utility.DEBUG) Log.v(TAG, "getVideoNewsCategory()");
        if (mApiController != null) {
            mApiController.getVideosCategory(mApiHandler);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mUiHandler = new UiHandler(this);
        mApiHandler = new ApiHandler(this);
    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt(KEY_POSITION);
        }
    }

    private void processView() {

        View view = getView();

        vTabGroup = (RelativeLayout) view.findViewById(R.id.tab_group);
        vTabGroup.setBackgroundColor(getResources().getColor(R.color.videos_tab_bar_background_color));
        vTab = (SlidingTabLayout) view.findViewById(R.id.tab);
        vViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        vLoadingPageLayout = (RelativeLayout) view.findViewById(R.id.list_loading_layout);
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

    }

    private void processListener(){
    }

    private void setTab() {

        if (mVideoNewsCategoryContent != null && mVideoNewsCategoryContent.size() == 0) {

            if (vViewPager != null) {
                vViewPager.setAdapter(null);
            }
            if (vTab != null) {
                vTab.setViewPager(null);
            }
            return;

        } else if (mVideoNewsCategoryContent != null && mVideoNewsCategoryContent.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new VideoNewsCategoryFragmentAdapter(getActivity(), getChildFragmentManager(), mVideoNewsCategoryContent, mVideoNewsCategoryContent.size(), mUiHandler);
            } else {
                mAdapter.setData(mVideoNewsCategoryContent, mVideoNewsCategoryContent.size());
            }
            vViewPager.setAdapter(mAdapter);
            vViewPager.setCurrentItem(mPosition);
            vViewPager.addOnPageChangeListener(mViewPageChangeListener);
            vTab.setBackgroundColor(getResources().getColor(R.color.videos_tab_bar_background_color));
            vTab.setCustomTabView(R.layout.widget_tab_layout, 0);
            vTab.setDistributeEvenly(true);
            vTab.setVideosCategoryContent(mVideoNewsCategoryContent);
            vTab.setTextViewTextSize(20);
            vTab.setSelectedIndicatorColors(getResources().getColor(android.R.color.transparent));
            vTab.setViewPager(vViewPager);
        }

    }

    @Override
    public void onResume() {

        GoogleAnalyticsFunction.setScreenName(getActivity(), "新聞列表");

        if (UserDataInfo.mPageSwapCount > 0) {
            UserDataInfo.mPageSwapCount = 0;
        }

        super.onResume();
    }

    @Override
    public void onDestroyView() {

        if(mUiHandler!=null){
            mUiHandler.removeCallbacks(null);
            mUiHandler = null;
        }

        if(mApiHandler!=null){
            mApiHandler.removeCallbacks(null);
            mApiHandler = null;
        }

        super.onDestroyView();
    }

    public void reload() {
        if(Utility.DEBUG)Log.v(TAG, "startSplashActivity");

        mRetryCount = 0;
        if(mApiHandler!=null && mApiHandler.hasMessages(RELOAD_API)){
            mApiHandler.removeMessages(RELOAD_API);
        }
        startFragment();
    }

}

package com.nownews.mobile.NewsCategory;

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
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsCategoryJson;
import com.nownews.mobile.Json.NewsCategoryJson.CategoryInfo;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.Widget.SlidingTabLayout;
import com.nownews.mobile.Widget.WebFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NewsCategoryFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    public final static String KEY_POSITION = "position";
    public static final int LOAD_PAGE_START = 0x617;
    public static final int LOAD_PAGE_END = 0x357;
    private final int REQUEST_CODE = 0x123;
    private final int RESULT_CODE = 0x321;
    //View
    private SlidingTabLayout vTab;
    private ViewPager vViewPager;
    private RelativeLayout vLoadingPageLayout;
    private TextView vErrorMessage;
    private TextView vLoadingText;
    //View

    //For DFP End
//    private ArrayList<String> mNewsCategoryList;
    private NewsCategoryFragmentAdapter mAdapter;
    private int mPosition;
    //For DFP Start

    private OnPageChangeListener mViewPageChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {
//            if (Utility.DEBUG) Log.e(TAG, "onPageSelected()");

            int currentPage = vViewPager.getCurrentItem();
            Fragment fragment = (Fragment) mAdapter.instantiateItem(vViewPager, currentPage);
            if(fragment instanceof NewsListFragment){
                ((NewsListFragment)fragment).destroyAD2();
            }else if(fragment instanceof OtherNewsListFragment){
                ((OtherNewsListFragment)fragment).destroyAD2();
            }

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            if (Utility.DEBUG) Log.e(TAG, "onPageScrolled");
            if (checkDirection) {
//                if (Utility.DEBUG) Log.d(TAG, "position: " + position);
//                if (Utility.DEBUG) Log.d(TAG, "thresholdOffset: " + thresholdOffset);
//                if (Utility.DEBUG) Log.d(TAG, "positionOffset: " + positionOffset);
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
    private List<CategoryInfo> mNewsCategoryContent;
    private UiHandler mUiHandler;

    public boolean checkIsWebFragment() {
        int currentPage = vViewPager.getCurrentItem();
        Fragment fragment = (Fragment) mAdapter.instantiateItem(vViewPager, currentPage);
        if(fragment instanceof WebFragment){
            return true;
        }
        return false;
    }

    public boolean checkIsWebFragmentCanGoBack() {
        int currentPage = vViewPager.getCurrentItem();
        Fragment fragment = (Fragment) mAdapter.instantiateItem(vViewPager, currentPage);
        if(fragment instanceof WebFragment){
            return ((WebFragment)fragment).canGoBack();
        }
        return false;
    }

    public void doWebFragmentGoBack() {
        int currentPage = vViewPager.getCurrentItem();
        Fragment fragment = (Fragment) mAdapter.instantiateItem(vViewPager, currentPage);
        if(fragment instanceof WebFragment){
            ((WebFragment)fragment).goBack();
        }
    }

    private static class UiHandler extends Handler {

        private final WeakReference<NewsCategoryFragment> mFragment;

        public UiHandler(NewsCategoryFragment aFragment){
            mFragment = new WeakReference<NewsCategoryFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            NewsCategoryFragment fragment = mFragment.get();
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

    public boolean isApiLoadingSuccess;
    private int mRetryCount;
    private final static int RELOAD_API = 0x159;
    private ApiHandler mApiHandler;
    private static class ApiHandler extends Handler {

        private String TAG = getClass().getSimpleName();
        private final WeakReference<NewsCategoryFragment> mFragment;

        public ApiHandler(NewsCategoryFragment aFragment){
            mFragment = new WeakReference<NewsCategoryFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            NewsCategoryFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_NEWS_CATEGORY_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsCategoryContent = (List<CategoryInfo>) msg.obj;
//                    fragment.processCategory();
                    fragment.setTab();
                    break;
                case ParameterSet.GET_NEWS_CATEGORY_FAILED:
                    Log.e(TAG, "mRetryCount: " + fragment.mRetryCount);
                    fragment.isApiLoadingSuccess = false;
                    if(fragment.mRetryCount<5){
                        fragment.mRetryCount++;
                        if(hasMessages(fragment.RELOAD_API)){
                            removeMessages(fragment.RELOAD_API);
                        }
                        sendEmptyMessage(fragment.RELOAD_API);
                        break;
                    }else{
                        fragment.mRetryCount = 0;
//                        fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_NEWS_CATEGORY_FAILED));
                        //補洞
//                        fragment.processCategory();
                        fragment.setTab();
                    }
                    break;
                case RELOAD_API:
                    fragment.getNewsCategory();
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
        getNewsCategory();
    }

    private void getNewsCategory() {
        if (Utility.DEBUG) Log.v(TAG, "getNewsCategory()");
        if (mApiController != null) {
            mApiController.getNewsCategory(mApiHandler);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mUiHandler = new UiHandler(this);
        mApiHandler = new ApiHandler(this);
        mResize = new ReSizeLayoutParams(getActivity());
    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt(KEY_POSITION);
        }
    }

    private ReSizeLayoutParams mResize;
    private void processView() {

        View view = getView();

        vTab = (SlidingTabLayout) view.findViewById(R.id.tab);
        mResize.setPadding(vTab, 20, 0, 20, 0);

        vLoadingText = (TextView) view.findViewById(R.id.loading_text);
        mResize.setTextSize(vLoadingText);

        vErrorMessage = (TextView) view.findViewById(R.id.error_message);
        mResize.setTextSize(vErrorMessage);

        vViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        vLoadingPageLayout = (RelativeLayout) view.findViewById(R.id.list_loading_layout);
    }

    private void processListener(){
    }

    private void setTab() {

        if (mNewsCategoryContent == null || mNewsCategoryContent.size() == 0) {

            if (vViewPager != null) {
                vViewPager.setAdapter(null);
            }
            if (vTab != null) {
                vTab.setViewPager(null);
            }
            return;

        } else if (mNewsCategoryContent != null && mNewsCategoryContent.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new NewsCategoryFragmentAdapter(getActivity(), getChildFragmentManager(), mNewsCategoryContent, mNewsCategoryContent.size(), mUiHandler);
            } else {
                mAdapter.setData(mNewsCategoryContent, mNewsCategoryContent.size());
            }
            vViewPager.setAdapter(mAdapter);
            vViewPager.setCurrentItem(mPosition);
            vViewPager.addOnPageChangeListener(mViewPageChangeListener);
            vTab.setCustomTabView(R.layout.widget_tab_layout, 0);
            vTab.setDistributeEvenly(true);
            vTab.setNewsCategoryContent(mNewsCategoryContent);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void reload() {
        if(Utility.DEBUG)Log.v(TAG, "startSplashActivity");

        mRetryCount = 0;
        if(mApiHandler!=null && mApiHandler.hasMessages(RELOAD_API)){
            mApiHandler.removeMessages(RELOAD_API);
        }
        startFragment();
    }

    //Not use in v4
//    private void processCategory() {
//
//        //News
//        //TODO: Need to add Headline, HotNews and InstanceNews, Food, Health and nearbynews
//        //Headline
//        addCategoryInfo(getString(R.string.headline), -1, 0);
//        //HotNews
//        addCategoryInfo(getString(R.string.hot_news), -1, 1);
//        //InstanceNews
//        addCategoryInfo(getString(R.string.instant_news), -1, 2);
//        //Food
//        addCategoryInfo(getString(R.string.food), -1, mNewsCategoryContent.size());
//        //Health
//        addCategoryInfo(getString(R.string.health), -1, mNewsCategoryContent.size());
//        //Nearby
//        addCategoryInfo(getString(R.string.nearbynews), -1, mNewsCategoryContent.size());
//
//    }
//
//    private void addCategoryInfo(String aCategoryName, int aTid, int aPosition) {
//        NewsCategoryJson categoryJson = new NewsCategoryJson();
//        NewsCategoryJson.CategoryInfo categoryInfo = categoryJson.new CategoryInfo();
//        categoryInfo.name = aCategoryName;
//        categoryInfo.tid = aTid;
//        if(mNewsCategoryContent == null){
//            mNewsCategoryContent = new ArrayList<>();
//        }
//        mNewsCategoryContent.add(aPosition, categoryInfo);
//    }

}

package com.nownews.mobile.SpecialNewsCategory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.SpecialNewsCategoryJson.*;
import com.nownews.mobile.Json.SpecialNewsListJson;
import com.nownews.mobile.NewsCategory.NewsListRecyclerViewAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

public class SpecialNewsCategoryFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView vList;
    private SwipeRefreshLayout vRefreshLayout;
    private RelativeLayout vLoadingLayout;
    private TextView vCategoryName;
    private ImageView vArrowUp;
    private RecyclerView vCategoryList;
    private RelativeLayout vChooseCategoryBar;
    private TextView vErrorMessage;

    private ApiController mApiController;
    private List<SpecialNewsListJson.NewsListBean> mNewsList;
    private SpecialNewsListRecyclerViewAdapter mAdapter;
    private NewsListRecyclerViewAdapter mErrorAdapter;
    private boolean isRefereshing = false;
    private boolean isScrollToBottom = false;
    private boolean isOnDestroy = false;
    private List<SpecialChannelsBean> mCategoryInfo;
    private int mRetryCount;
    private final static int RELOAD_SPECIAL_NEWS_LIST_API = 0x159;
    private final static int RELOAD_SPECIAL_NEWS_CATEGORY_API = 0x357;
    public boolean isApiLoadingSuccess;
    private ApiHandler mApiHandler;
    private static class ApiHandler extends Handler {

        private WeakReference<SpecialNewsCategoryFragment> mFragment;

        private ApiHandler(SpecialNewsCategoryFragment aFragment){
            mFragment = new WeakReference<SpecialNewsCategoryFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            SpecialNewsCategoryFragment fragment = mFragment.get();

            switch (msg.what) {
                case ParameterSet.GET_SPECIAL_NEWS_CATEGORY_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mCategoryInfo = (List<SpecialChannelsBean>) msg.obj;
                    fragment.processCategoryList();
                    break;
                case ParameterSet.GET_SPECIAL_NEWS_CATEGORY_FAILED:
                    fragment.isApiLoadingSuccess = false;
                    if(fragment.mRetryCount<5){
                        fragment.mRetryCount++;
                        if(hasMessages(fragment.RELOAD_SPECIAL_NEWS_CATEGORY_API)){
                            removeMessages(fragment.RELOAD_SPECIAL_NEWS_CATEGORY_API);
                        }
                        sendEmptyMessage(fragment.RELOAD_SPECIAL_NEWS_CATEGORY_API);
                        break;
                    }else {
                        fragment.mRetryCount = 0;
                        fragment.vLoadingLayout.setVisibility(View.GONE);
                        fragment.vErrorMessage.setVisibility(View.VISIBLE);
                        fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_SPECIAL_NEWS_CATEGORY_FAILED));
                    }
                    break;

                case ParameterSet.GET_SPECIAL_NEWS_LIST_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsList = (List<SpecialNewsListJson.NewsListBean>) msg.obj;
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    if (!fragment.isOnDestroy) {
                        fragment.processList(fragment.mNewsList);
                    }
                    fragment.vLoadingLayout.setVisibility(View.GONE);
                    break;
                case ParameterSet.GET_SPECIAL_NEWS_LIST_FAILED:
                    fragment.isApiLoadingSuccess = false;
                    if(fragment.mRetryCount<5){
                        fragment.mRetryCount++;
                        if(hasMessages(fragment.RELOAD_SPECIAL_NEWS_LIST_API)){
                            removeMessages(fragment.RELOAD_SPECIAL_NEWS_LIST_API);
                        }
                        sendEmptyMessage(fragment.RELOAD_SPECIAL_NEWS_LIST_API);
                        break;
                    }else {
                        if (fragment.isRefereshing) {
                            // Stop refresh animation
                            fragment.isRefereshing = false;
                            fragment.vRefreshLayout.setRefreshing(false);
                        }
                        fragment.mRetryCount = 0;
                        fragment.vLoadingLayout.setVisibility(View.GONE);
                        fragment.vErrorMessage.setVisibility(View.VISIBLE);
//                        fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_SPECIAL_NEWS_LIST_FAILED));
                        //補洞
                        fragment.setReplaceNews();
                    }
                    break;

                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    break;

                case RELOAD_SPECIAL_NEWS_LIST_API:
                    fragment.getSpecialNewsList(fragment.mCategoryInfo.get(fragment.mCurrentItem).getSn());
                    break;

                case RELOAD_SPECIAL_NEWS_CATEGORY_API:
                    fragment.getSpecialNewsCategory();
                    break;
            }

        }

    };

    private void setReplaceNews(){
        if(Utility.DEBUG)Log.w(TAG, "mCurrentItem: " + mCurrentItem);
        List<?> list = null;
        NewsListRecyclerViewAdapter.ReplaceType replaceType = null;
        switch(mCurrentItem%2){
            case 0:
                list = UserDataInfo.getHeadlineContent();
                replaceType = NewsListRecyclerViewAdapter.ReplaceType.Headline;
                if(list==null){
                    if(UserDataInfo.getInstantNewsContent()!=null){
                        list = UserDataInfo.getInstantNewsContent();
                        replaceType = NewsListRecyclerViewAdapter.ReplaceType.Instant;
                    }else{
                        vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_SPECIAL_NEWS_LIST_FAILED));
                    }
                }
                if (Utility.DEBUG) Log.e(TAG, "list: " + list);
                break;
            case 1:
                list = UserDataInfo.getInstantNewsContent();
                replaceType = NewsListRecyclerViewAdapter.ReplaceType.Instant;
                if(list==null){
                    if(UserDataInfo.getHeadlineContent()!=null){
                        list = UserDataInfo.getHeadlineContent();
                        replaceType = NewsListRecyclerViewAdapter.ReplaceType.Headline;
                    }else{
                        vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_SPECIAL_NEWS_LIST_FAILED));
                    }
                }
                if (Utility.DEBUG) Log.e(TAG, "list: " + list);
                break;
        }
        if(list!=null){
            if (!isOnDestroy) {
                processErrorList(list, replaceType);
            }
        }
    }

    private GridLayoutManager mGridLayoutManager;
    private SpecialNewsCategoryAdapter mSpecialNewsCategoryAdapter;
    private void processCategoryList(){

        Log.w(TAG, "mCategoryInfo: " + mCategoryInfo);

        mGridLayoutManager = new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);
        vCategoryList.setLayoutManager(mGridLayoutManager);
        mSpecialNewsCategoryAdapter = new SpecialNewsCategoryAdapter(getActivity(), mCategoryInfo, mCategoryItemClickListener);
        vCategoryList.setAdapter(mSpecialNewsCategoryAdapter);
        mSpecialNewsCategoryAdapter.itemClick(0);

    }

    private RecyclerView.OnScrollListener mListScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!recyclerView.canScrollVertically(1)) {
                    if(Utility.DEBUG)Log.e(TAG, "滑到底了!!");
                    Toast.makeText(getActivity(), "下面沒有了哦...", Toast.LENGTH_SHORT).show();
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    if(Utility.DEBUG)Log.e(TAG, "滑到頂了!!");
                    Toast.makeText(getActivity(), "上面沒有了哦...", Toast.LENGTH_SHORT).show();
                }
            }

        }

    };

    private int mCurrentItem;
    private SpecialNewsCategoryAdapter.OnItemClickListener mCategoryItemClickListener = new SpecialNewsCategoryAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int aPosition, String aCategoryName) {

            mCurrentItem = aPosition;
            vCategoryName.setText(aCategoryName);
            closeCategoryList();
            clearNewsList();
            getSpecialNewsList(mCategoryInfo.get(aPosition).getSn());

        }
    };

    private void clearNewsList(){
        if(mNewsList!=null){
            mNewsList.clear();
            if(mAdapter!=null){
                mAdapter.setData(mNewsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getActivity().getString(R.string.special));
            }
            if(mErrorAdapter!=null){
                mErrorAdapter.setData(mNewsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getActivity().getString(R.string.special));
            }
        }
    }

    public SpecialNewsCategoryFragment() {
        // Do nothing...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_special_news_category, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mAdapter != null) {
            mAdapter = null;
        }

        if (mErrorAdapter != null) {
            mErrorAdapter = null;
        }

        initController();
        processView();
        processListener();
        getSpecialNewsCategory();

    }

    private void getSpecialNewsCategory() {
        if (mApiController != null) {
            mApiController.getSpecialNewsCategory(mApiHandler);
        }
    }

    private void getSpecialNewsList(int aNewsCategoryId) {
        vLoadingLayout.setVisibility(View.VISIBLE);
        vErrorMessage.setVisibility(View.GONE);
        if (mApiController != null) {
            mApiController.getSpecialNewsList(mApiHandler, aNewsCategoryId);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler(this);
    }

    private void processView() {

        View view = getView();

        vList = (RecyclerView) view.findViewById(R.id.list);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                clearNewsList();
                getSpecialNewsList(mCategoryInfo.get(mCurrentItem).getSn());
            }
        });

        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vCategoryName = (TextView) view.findViewById(R.id.category_name);
        vArrowUp = (ImageView) view.findViewById(R.id.arrow_up);
        vCategoryList = (RecyclerView) view.findViewById(R.id.category_list);
        vChooseCategoryBar = (RelativeLayout) view.findViewById(R.id.choose_category_bar);
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

    }

    private void processListener(){

        vChooseCategoryBar.setOnClickListener(mCategoryListClickListener);

    }

    private View.OnClickListener mCategoryListClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(vCategoryList.getVisibility()==View.VISIBLE){
                closeCategoryList();
            }else{
                openCategoryList();
            }

        }
    };

    private void openCategoryList(){
        vCategoryList.setVisibility(View.VISIBLE);
        vArrowUp.setVisibility(View.VISIBLE);
        vCategoryName.setVisibility(View.INVISIBLE);
    }

    private void closeCategoryList(){
        vCategoryList.setVisibility(View.GONE);
        vArrowUp.setVisibility(View.INVISIBLE);
        vCategoryName.setVisibility(View.VISIBLE);
    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processList(List<SpecialNewsListJson.NewsListBean> mNewsList) {

        if (Utility.DEBUG) Log.e(TAG, "processList()");
        if (isScrollToBottom) {
            isScrollToBottom = false;
        }

        if (mAdapter == null) {
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            vList.setLayoutManager(mLinearLayoutManager);
            mAdapter = new SpecialNewsListRecyclerViewAdapter(getActivity(), mNewsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getString(R.string.special));
            vList.setAdapter(mAdapter);
            vList.addOnScrollListener(mListScrollListener);
        } else {
            mAdapter.setData(mNewsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getString(R.string.special));
        }

        vLoadingLayout.setVisibility(View.GONE);
        vErrorMessage.setVisibility(View.GONE);
        Log.e(TAG, "1237456");

    }

    private void processErrorList(List<?> newsList, NewsListRecyclerViewAdapter.ReplaceType aReplaceType) {

        if (Utility.DEBUG) Log.e(TAG, "processErrorList()");
        if (isScrollToBottom) {
            isScrollToBottom = false;
        }

        if (mErrorAdapter == null) {
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            vList.setLayoutManager(mLinearLayoutManager);
            mErrorAdapter = new NewsListRecyclerViewAdapter(getActivity(), newsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getString(R.string.special), aReplaceType);
            vList.setAdapter(mErrorAdapter);
            vList.addOnScrollListener(mListScrollListener);
        } else {
            mErrorAdapter.setData(newsList, mCategoryInfo.get(mCurrentItem).getTitle(), getChildFragmentManager(), getString(R.string.special));
        }

        vLoadingLayout.setVisibility(View.GONE);
        vErrorMessage.setVisibility(View.GONE);
        Log.e(TAG, "654789");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Utility.DEBUG) Log.e(TAG, "onDestroy()");
        isOnDestroy = true;
        if(mApiHandler!=null){
            mApiHandler.removeCallbacks(null);
            mApiHandler = null;
        }
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");
        super.onDestroyView();
    }

}

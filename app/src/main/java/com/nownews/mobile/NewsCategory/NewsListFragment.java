package com.nownews.mobile.NewsCategory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson;

import java.lang.ref.WeakReference;
import java.util.List;

public class NewsListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView vList;
    private SwipeRefreshLayout vRefreshLayout;
    private String mNewsCategoryUrl;
    private ApiController mApiController;

    private List<NewsListJson.NewsListBean> mNewsList;
    //	private NewsListFragmentAdapter mAdapter;
    private RelativeLayout vLoadingLayout;
    private TextView vErrorMessage;
    private TextView vLoadingText;
    private Handler mHandler;
    private String mCategoryName;
    private int mCurrentPage = 1;
    private boolean isRefereshing = false;
    private boolean isScrollToBottom = false;
    private boolean isOnDestroy = false;
    private int mRetryCount;
    private final static int RELOAD_API = 0x159;
    public boolean isApiLoadingSuccess;
    private ApiHandler mApiHandler;

    private static class ApiHandler extends Handler {

        private String TAG = getClass().getSimpleName();
        private final WeakReference<NewsListFragment> mFragment;

        public ApiHandler(NewsListFragment aFragment){
            mFragment = new WeakReference<NewsListFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            NewsListFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_NEWS_LIST_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    if (fragment.mCurrentPage > 1) {
                        fragment.mNewsList.addAll((List<NewsListJson.NewsListBean>) msg.obj);
                        if (fragment.mHandler != null) {
                            fragment.mHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                        }
                    } else {
                        fragment.mNewsList = (List<NewsListJson.NewsListBean>) msg.obj;
                    }
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    if (!fragment.isOnDestroy && fragment.isAdded()) {
                        fragment.processList(fragment.mNewsList, null);
                    }
                    break;
                case ParameterSet.GET_NEWS_LIST_FAILED:

                    if(Utility.DEBUG)Log.e(TAG, fragment.mCategoryName + " mRetryCount: " + fragment.mRetryCount);
                    fragment.isApiLoadingSuccess = false;
                    if(fragment.mRetryCount<3){
                        fragment.mRetryCount++;
                        if(hasMessages(RELOAD_API)){
                            removeMessages(RELOAD_API);
                        }
                        sendEmptyMessage(RELOAD_API);
                        break;
                    }else{
                        fragment.mRetryCount = 0;
                        if (fragment.isRefereshing) {
                            // Stop refresh animation
                            fragment.isRefereshing = false;
                            fragment.vRefreshLayout.setRefreshing(false);
                        }
                        if (fragment.isScrollToBottom) {
                            if (fragment.mHandler != null) {
                                fragment.mHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                            }
                            fragment.mCurrentPage--;
                            fragment.isScrollToBottom = false;
                        }else{
                            fragment.vLoadingLayout.setVisibility(View.GONE);
                            fragment.vList.setVisibility(View.GONE);
                            fragment.vErrorMessage.setVisibility(View.VISIBLE);
//                            fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_NEWS_LIST_FAILED));
                            //補洞
                            fragment.setReplaceNews();
                        }
                    }
                    break;
                case RELOAD_API:
                    fragment.getNewsList();
                    break;
            }

        }

    };

    private void setReplaceNews(){
        List<?> list = null;
        NewsListRecyclerViewAdapter.ReplaceType replaceType = null;
        switch(mCurrentPosition%2){
            case 0:
                list = UserDataInfo.getHeadlineContent();
                replaceType = NewsListRecyclerViewAdapter.ReplaceType.Headline;
                if(list==null){
                    if(UserDataInfo.getInstantNewsContent()!=null){
                        list = UserDataInfo.getInstantNewsContent();
                        replaceType = NewsListRecyclerViewAdapter.ReplaceType.Instant;
                    }else{
                        vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_NEWS_LIST_FAILED));
                    }
                }
                break;
            case 1:
                list = UserDataInfo.getInstantNewsContent();
                replaceType = NewsListRecyclerViewAdapter.ReplaceType.Instant;
                if(list==null){
                    if(UserDataInfo.getHeadlineContent()!=null){
                        list = UserDataInfo.getHeadlineContent();
                        replaceType = NewsListRecyclerViewAdapter.ReplaceType.Headline;
                    }else{
                        vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_NEWS_LIST_FAILED));
                    }
                }
                break;
        }
        if(list!=null){
            if (!isOnDestroy && isAdded()) {
                processList(list, replaceType);
            }
        }
    }

    private RecyclerView.OnScrollListener mListScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            int findFirstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
            int findLastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();

            if(Utility.DEBUG)Log.v(TAG, "findFirstVisibleItemPosition: " + findFirstVisibleItemPosition);
            if(Utility.DEBUG)Log.v(TAG, "findLastVisibleItemPosition: " + findLastVisibleItemPosition);

            if(newState==RecyclerView.SCROLL_STATE_IDLE){
                Log.d(TAG, "onScrollStateChanged!!! " + newState);
                if (!recyclerView.canScrollVertically(-1)) {
                    if(Utility.DEBUG)Log.e(TAG, "滑到頂了!!");
                    Toast.makeText(getActivity(), "上面沒有了哦...", Toast.LENGTH_SHORT).show();
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            Log.d(TAG, "onScrolled");
            int findLastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();

            if(Utility.DEBUG)Log.v(TAG, "mAdapter.getItemCount(): " + vList.getAdapter().getItemCount());
            if(Utility.DEBUG)Log.v(TAG, "findLastVisibleItemPosition: " + findLastVisibleItemPosition);

            if (vList != null && vList.getAdapter() != null) {
                if (findLastVisibleItemPosition == vList.getAdapter().getItemCount() - 1) {
                    if (!isScrollToBottom && mNewsList != null && mNewsList.size() > 0) {
                        isScrollToBottom = true;
						if(Utility.DEBUG)Log.e(TAG, "滑到底了!!");
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_START);
                        }
                        mCurrentPage++;
                        getNewsList();
                    }
                }
            }
        }

    };

    public NewsListFragment() {
        // Do nothing...
    }

    private RecyclerView.RecycledViewPool mPool;
    private int mCurrentPosition;
    public void setData(String aNewsCategoryUrl, Handler aHandler, String aCategoryName, RecyclerView.RecycledViewPool aPool, int aPosition) {
        mNewsCategoryUrl = aNewsCategoryUrl;
        mHandler = aHandler;
        mCategoryName = aCategoryName;
        mPool = aPool;
        mCurrentPosition = aPosition;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(Utility.DEBUG)Log.v(TAG, mCategoryName + "onCreateView");
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Utility.DEBUG)Log.v(TAG, mCategoryName + "onActivityCreated");

        initController();
        processView();
        getNewsList();

    }

    private void getNewsList() {
        if(isRefereshing){
            if(mNewsList!=null && mNewsList.size()>0){
                vLoadingLayout.setVisibility(View.GONE);
                vList.setVisibility(View.VISIBLE);
            }else{
                vLoadingLayout.setVisibility(View.VISIBLE);
                vList.setVisibility(View.GONE);
            }
        }
        vErrorMessage.setVisibility(View.GONE);
        if (mApiController != null) {
            mApiController.getNewsList(mApiHandler, mNewsCategoryUrl, Utility.NEWS_LIST_LIMIT_COUNT, mCurrentPage);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler(this);
        mResize = new ReSizeLayoutParams(getActivity());
    }

    private ReSizeLayoutParams mResize;
    private void processView() {

        View view = getView();

        vList = (RecyclerView) view.findViewById(R.id.list);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                mCurrentPage = 1;
                getNewsList();
            }
        });

        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);

        vLoadingText = (TextView) view.findViewById(R.id.image_loading_txt);
        mResize.setTextSize(vLoadingText);

        vErrorMessage = (TextView) view.findViewById(R.id.error_message);
        mResize.setTextSize(vErrorMessage);

    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processList(List<?> newsList, NewsListRecyclerViewAdapter.ReplaceType aReplaceType) {

        vLoadingLayout.setVisibility(View.GONE);
        vList.setVisibility(View.VISIBLE);

        if (Utility.DEBUG) Log.e(TAG, "processList()");
        if (isScrollToBottom) {
            isScrollToBottom = false;
        }

//		mAdapter = new NewsListFragmentAdapter(getActivity(), mNewsList);
//		vList.setAdapter(mAdapter);

        if(vList.getAdapter()==null){
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mLinearLayoutManager.setRecycleChildrenOnDetach(true);
            vList.setLayoutManager(mLinearLayoutManager);
            if(mPool!=null){
                vList.setRecycledViewPool(mPool);
            }
            NewsListRecyclerViewAdapter adapter = null;
            adapter = new NewsListRecyclerViewAdapter(getActivity(), newsList, mCategoryName, getChildFragmentManager(), getString(R.string.news), aReplaceType, mHandler);
            vList.setAdapter(adapter);
            vList.addOnScrollListener(mListScrollListener);
        }else{
            ((NewsListRecyclerViewAdapter) vList.getAdapter()).setData(newsList, mCategoryName, getChildFragmentManager(), getString(R.string.news), aReplaceType, mHandler);
        }

        vLoadingLayout.setVisibility(View.GONE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Utility.DEBUG) Log.e(TAG, mCategoryName + "onDestroy()");
        isOnDestroy = true;
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, mCategoryName + "onDestroyView()");
        if(vList != null && vList.getAdapter() != null){
            ((NewsListRecyclerViewAdapter)vList.getAdapter()).clearBitmapController();
            ((NewsListRecyclerViewAdapter)vList.getAdapter()).unRegistContext(getActivity());
            vList.setLayoutManager(null);
            vList.setAdapter(null);
            vList = null;
        }
        if(mApiHandler !=null){
            mApiHandler.removeCallbacks(null);
            mApiHandler = null;
        }
        super.onDestroyView();
    }

    public void destroyAD2() {
        if(vList != null && vList.getAdapter() != null){
            ((NewsListRecyclerViewAdapter)vList.getAdapter()).destroyAD2();
        }
    }

}

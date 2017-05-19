package com.nownews.mobile.VideoNewsCategory;

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
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.VideosListJson.VideosContent;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;

import java.lang.ref.WeakReference;
import java.util.List;

public class VideoNewsListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView vList;
    private SwipeRefreshLayout vRefreshLayout;
    private int mNewsCategoryId;
    private ApiController mApiController;

    private List<VideosContent> mVideoNewsList;
    //	private NewsListFragmentAdapter mAdapter;
    private RelativeLayout vLoadingLayout;
    private TextView vErrorMessage;
    private Handler mAdHandler;
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
        private final WeakReference<VideoNewsListFragment> mFragment;

        public ApiHandler(VideoNewsListFragment aFragment){
            mFragment = new WeakReference<VideoNewsListFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            VideoNewsListFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_VIDEOS_LIST_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    if (fragment.mCurrentPage > 1) {
                        fragment.mVideoNewsList.addAll((List<VideosContent>) msg.obj);
                        if (fragment.mAdHandler != null) {
                            fragment.mAdHandler.sendEmptyMessage(VideoNewsCategoryFragment.LOAD_PAGE_END);
                        }
                    } else {
                        fragment.mVideoNewsList = (List<VideosContent>) msg.obj;
                    }
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    if (!fragment.isOnDestroy) {
                        fragment.processList();
                    }
                    break;
                case ParameterSet.GET_VIDEOS_LIST_FAILED:

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
                        if (fragment.isRefereshing) {
                            // Stop refresh animation
                            fragment.isRefereshing = false;
                            fragment.vRefreshLayout.setRefreshing(false);
                        }
                        if (fragment.isScrollToBottom) {
                            if (fragment.mAdHandler != null) {
                                fragment.mAdHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                            }
                            fragment.mCurrentPage--;
                            fragment.isScrollToBottom = false;
                        }else{
                            fragment.vLoadingLayout.setVisibility(View.GONE);
                            fragment.vList.setVisibility(View.GONE);
                            fragment.vErrorMessage.setVisibility(View.VISIBLE);
                            fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_VIDEOS_LIST_FAILED));
                        }
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    break;
                case RELOAD_API:
                    fragment.getNewsList();
                    break;
            }

        }

    };

    public VideoNewsListFragment() {
        // Do nothing...
    }

    private RecyclerView.RecycledViewPool mPool;
    public void setData(int aNewsCategoryId, Handler aAdHandler, String aCategoryName, RecyclerView.RecycledViewPool aPool) {
        mNewsCategoryId = aNewsCategoryId;
        mAdHandler = aAdHandler;
        mCategoryName = aCategoryName;
        mPool = aPool;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initController();
        processView();
        getNewsList();

    }

    private void getNewsList() {
        if(isRefereshing){
            if(mVideoNewsList!=null && mVideoNewsList.size()>0){
                vLoadingLayout.setVisibility(View.GONE);
                vList.setVisibility(View.VISIBLE);
            }else{
                vLoadingLayout.setVisibility(View.VISIBLE);
                vList.setVisibility(View.GONE);
            }
        }
        vErrorMessage.setVisibility(View.GONE);
        if (mApiController != null) {
            mApiController.getVideosList(mApiHandler, mNewsCategoryId, mCurrentPage);
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
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processList() {

        vLoadingLayout.setVisibility(View.GONE);
        vList.setVisibility(View.VISIBLE);

        if (Utility.DEBUG) Log.e(TAG, "processList()");
        if (isScrollToBottom) {
            isScrollToBottom = false;
        }

//		mAdapter = new NewsListFragmentAdapter(getActivity(), mVideoNewsList);
//		vList.setAdapter(mAdapter);

        if (vList.getAdapter() == null) {
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mLinearLayoutManager.setRecycleChildrenOnDetach(true);
            vList.setLayoutManager(mLinearLayoutManager);
            if(mPool!=null){
                vList.setRecycledViewPool(mPool);
            }
            VideoNewsListRecyclerViewAdapter adapter = new VideoNewsListRecyclerViewAdapter(getActivity(), mVideoNewsList, mCategoryName, getChildFragmentManager());
            vList.setAdapter(adapter);
            vList.addOnScrollListener(mListScrollListener);
        } else {
            ((VideoNewsListRecyclerViewAdapter)vList.getAdapter()).setData(mVideoNewsList, getChildFragmentManager());
        }

        vLoadingLayout.setVisibility(View.GONE);

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
                if (Utility.DEBUG)Log.d(TAG, "onScrollStateChanged!!! " + newState);
                if (!recyclerView.canScrollVertically(-1)) {
                    if(Utility.DEBUG)Log.e(TAG, "滑到頂了!!");
                    Toast.makeText(getActivity(), "上面沒有了哦...", Toast.LENGTH_SHORT).show();
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (Utility.DEBUG)Log.d(TAG, "onScrolled");
            int findLastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();

            if (vList.getAdapter() != null) {
                if (findLastVisibleItemPosition == vList.getAdapter().getItemCount() - 1) {
                    if (!isScrollToBottom && mVideoNewsList != null && mVideoNewsList.size() > 0) {
                        isScrollToBottom = true;
//						if(Utility.DEBUG)Log.e(TAG, "滑到底了!!");
                        if (mAdHandler != null) {
                            mAdHandler.sendEmptyMessage(VideoNewsCategoryFragment.LOAD_PAGE_START);
                        }
                        mCurrentPage++;
                        getNewsList();
                    }
                }
            }
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Utility.DEBUG) Log.e(TAG, "onDestroy()");
        isOnDestroy = true;
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");
        if(vList != null && vList.getAdapter() != null){
            ((VideoNewsListRecyclerViewAdapter)vList.getAdapter()).clearBitmapController();
            ((VideoNewsListRecyclerViewAdapter)vList.getAdapter()).unRegistContext(getActivity());
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
            ((VideoNewsListRecyclerViewAdapter)vList.getAdapter()).destroyAD2();
        }
    }

}

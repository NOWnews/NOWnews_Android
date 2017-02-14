package com.nownews.mobile.AlbumCategory;

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

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.PhotosListJson.PhotosContent;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;

import java.util.ArrayList;
import java.util.List;

public class AlbumGridFragment extends Fragment {

    public final static String KEY_API_URL = "apiUrl";
    public final static String KEY_CATEGORY_ID = "categoryID";
    public final static String KEY_CATEGORY_NAME = "categoryName";
    private final String TAG = getClass().getSimpleName();
    private RecyclerView vList;
    private SwipeRefreshLayout vRefreshLayout;
    private int mCategoryId;
    private ApiController mApiController;
    private List<PhotosContent> mAlbumList;
    private LinearLayoutManager mLinearLayoutManager;
    private AlbumGridFragmentAdapter mAdapter;
    private RelativeLayout vLoadingLayout;
    private TextView vErrorMessage;
    private Handler mAdHandler;
    private String mCategoryName;
    private boolean isRefereshing = false;
    private boolean isScrollToBottom = false;
    private ArrayList<String> mAlbumImageList;
    private int mAlbumImageListCount;
    private int mRetryCount;
    private final int RELOAD_API = 0x159;
    public boolean isApiLoadingSuccess;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_PHOTOS_LIST_DONE:
                    isApiLoadingSuccess = true;
                    mRetryCount = 0;
                    if (mCurrentPage > 1) {
                        mAlbumList.addAll((List<PhotosContent>) msg.obj);
                        if (mAdHandler != null) {
                            mAdHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                        }
                    } else {
                        mAlbumList = (List<PhotosContent>) msg.obj;
                    }
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vRefreshLayout.setRefreshing(false);
                    }
                    processList();
                    break;
                case ParameterSet.GET_PHOTOS_LIST_FAILED:

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
                        if (isRefereshing) {
                            // Stop refresh animation
                            isRefereshing = false;
                            vRefreshLayout.setRefreshing(false);
                        }
                        if (isScrollToBottom) {
                            if (mAdHandler != null) {
                                mAdHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                            }
                            mCurrentPage--;
                            isScrollToBottom = false;
                        }else{
                            vLoadingLayout.setVisibility(View.GONE);
                            vList.setVisibility(View.GONE);
                            vErrorMessage.setVisibility(View.VISIBLE);
                            vErrorMessage.setText(String.format(getString(R.string.api_loading_error), ParameterSet.GET_PHOTOS_LIST_FAILED));
                        }
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(getActivity());
                    break;
                case RELOAD_API:
                    getAlbumsList();
                    break;

            }

        }

    };

    private int mCurrentPage = 1;
    private RecyclerView.OnScrollListener mListScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (Utility.DEBUG)Log.d(TAG, "onScrolled");
            int findLastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();

            if(Utility.DEBUG)Log.v(TAG, "mAdapter.getItemCount(): " + mAdapter.getItemCount());
            if(Utility.DEBUG)Log.v(TAG, "findLastVisibleItemPosition: " + findLastVisibleItemPosition);

            if (mAdapter != null) {
                if (findLastVisibleItemPosition == mAdapter.getItemCount() - 1) {
                    if (!isScrollToBottom && mAlbumList != null && mAlbumList.size() > 0) {
                        isScrollToBottom = true;
                        if(Utility.DEBUG)Log.e(TAG, "滑到底了!!");
                        if (mAdHandler != null) {
                            mAdHandler.sendEmptyMessage(AlbumCategoryFragment.LOAD_PAGE_START);
                        }
                        mCurrentPage++;
                        getAlbumsList();
                    }
                }
            }
        }

    };

    public AlbumGridFragment() {
        // Do nothing...
    }

    public void setData(int aNewsCategoryId, Handler aAdHandler, String aCategoryName) {
        mCategoryId = aNewsCategoryId;
        mAdHandler = aAdHandler;
        mCategoryName = aCategoryName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_grid, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initController();
        processView();
        getAlbumsList();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private void getAlbumsList() {
        if(isRefereshing){
            if(mAlbumList!=null && mAlbumList.size()>0){
                vLoadingLayout.setVisibility(View.GONE);
                vList.setVisibility(View.VISIBLE);
            }else{
                vLoadingLayout.setVisibility(View.VISIBLE);
                vList.setVisibility(View.GONE);
            }
        }
        vErrorMessage.setVisibility(View.GONE);
        if (mApiController != null) {
            mApiController.getPhotosList(mHandler, mCategoryId, mCurrentPage);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
    }

    private void processView() {

        View view = getView();

        vList = (RecyclerView) view.findViewById(R.id.list);
//		mLinearLayoutManager = new LinearLayoutManager(getActivity());
//		mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//		vList.setLayoutManager(mLinearLayoutManager);

        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                mCurrentPage = 1;
                getAlbumsList();
            }
        });

    }

    private void processList() {

        vLoadingLayout.setVisibility(View.GONE);
        vList.setVisibility(View.VISIBLE);

        if (Utility.DEBUG) Log.e(TAG, "processList()");
        if (isScrollToBottom) {
            isScrollToBottom = false;
        }

        if(mAdapter==null){
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            vList.setLayoutManager(mLinearLayoutManager);
            mAdapter = new AlbumGridFragmentAdapter(getActivity(), mAlbumList, mCategoryName);
            vList.setAdapter(mAdapter);
            vList.addOnScrollListener(mListScrollListener);
        }else{
            mAdapter.setData(mAlbumList, mCategoryName);
        }

        vLoadingLayout.setVisibility(View.GONE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Utility.DEBUG) Log.e(TAG, "onDestroy()");
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");
        super.onDestroyView();
        if(mAdapter!=null){
            mAdapter.destoryView();
        }
    }
}

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
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.PhotosListJson.PhotosContent;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;

import java.lang.ref.WeakReference;
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
    private RelativeLayout vLoadingLayout;
    private TextView vErrorMessage;
    private Handler mAdHandler;
    private String mCategoryName;
    private boolean isRefereshing = false;
    private boolean isScrollToBottom = false;
    private ArrayList<String> mAlbumImageList;
    private int mAlbumImageListCount;
    private int mRetryCount;
    private final static int RELOAD_API = 0x159;
    public boolean isApiLoadingSuccess;
    private ApiHandler mApiHandler;
    private static class ApiHandler extends Handler{

        private String TAG = getClass().getSimpleName();
        private final WeakReference<AlbumGridFragment> mFragment;

        public ApiHandler(AlbumGridFragment aFragment){
            mFragment = new WeakReference<AlbumGridFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            AlbumGridFragment fragment = mFragment.get();

            switch (msg.what) {
                case ParameterSet.GET_PHOTOS_LIST_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    if (fragment.mCurrentPage > 1) {
                        fragment.mAlbumList.addAll((List<PhotosContent>) msg.obj);
                        if (fragment.mAdHandler != null) {
                            fragment.mAdHandler.sendEmptyMessage(NewsCategoryFragment.LOAD_PAGE_END);
                        }
                    } else {
                        fragment.mAlbumList = (List<PhotosContent>) msg.obj;
                    }
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    fragment.processList();
                    break;
                case ParameterSet.GET_PHOTOS_LIST_FAILED:

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
                            fragment.vErrorMessage.setText(String.format(fragment.getString(R.string.api_loading_error), ParameterSet.GET_PHOTOS_LIST_FAILED));
                        }
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    break;
                case RELOAD_API:
                    fragment.getAlbumsList();
                    break;

            }

        }

    };

    private int mCurrentPage = 1;
    private RecyclerView.OnScrollListener mListScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
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

            if(Utility.DEBUG)Log.v(TAG, "mAdapter.getItemCount(): " + vList.getAdapter().getItemCount());
            if(Utility.DEBUG)Log.v(TAG, "findLastVisibleItemPosition: " + findLastVisibleItemPosition);

            if (vList != null && vList.getAdapter() != null) {
                if (findLastVisibleItemPosition == vList.getAdapter().getItemCount() - 1) {
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

    private RecyclerView.RecycledViewPool mPool;
    public void setData(int aNewsCategoryId, Handler aAdHandler, String aCategoryName, RecyclerView.RecycledViewPool aPool) {
        mCategoryId = aNewsCategoryId;
        mAdHandler = aAdHandler;
        mCategoryName = aCategoryName;
        mPool = aPool;
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
            mApiController.getPhotosList(mApiHandler, mCategoryId, mCurrentPage);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler(this);
    }

    private void processView() {

        View view = getView();

        vList = (RecyclerView) view.findViewById(R.id.list);
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

        if(vList.getAdapter()==null){
            mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mLinearLayoutManager.setRecycleChildrenOnDetach(true);
            vList.setLayoutManager(mLinearLayoutManager);
            if(mPool!=null){
                vList.setRecycledViewPool(mPool);
            }
            AlbumGridFragmentAdapter adapter = new AlbumGridFragmentAdapter(getActivity(), mAlbumList, mCategoryName);
            vList.setAdapter(adapter);
            vList.addOnScrollListener(mListScrollListener);
        }else{
            ((AlbumGridFragmentAdapter)vList.getAdapter()).setData(mAlbumList, mCategoryName);
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
        if(vList != null && vList.getAdapter() != null){
            ((AlbumGridFragmentAdapter)vList.getAdapter()).clearBitmapController();
            ((AlbumGridFragmentAdapter)vList.getAdapter()).unRegistContext(getActivity());
            vList.setLayoutManager(null);
            vList.setAdapter(null);
            vList = null;
        }
        if(mApiHandler !=null){
            mApiHandler.removeCallbacks(null);
            mApiHandler = null;
        }
    }
}

package com.nownews.mobile.NewsCategory;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.NewsListJson.NewsContent;

import java.lang.ref.WeakReference;
import java.util.List;

public class OtherNewsListFragment extends Fragment {

    public final static String KEY_CATEGORY_NAME = "categoryName";
    private final String TAG = getClass().getSimpleName();
    private final static int CHECK_LIST = 0x651;
    private RecyclerView vList;
    private SwipeRefreshLayout vRefreshLayout;
    private ApiController mApiController;
    private List<NewsContent> mNewsList;
    private RelativeLayout vLoadingLayout;
    private TextView vErrorMessage;
    private String mCategoryName;
    private boolean isRefereshing = false;
    private boolean isOnDestroy = false;
    private boolean isGetHeadLineDone = false;
    private boolean isGetHotNewsDone = false;
    private boolean isGetInstantNewsDone = false;
    private boolean isGetNearByNewsDone = false;
    private int mRetryCount;
    private final static int RELOAD_API = 0x159;
    public boolean isApiLoadingSuccess;
    private ApiHandler mApiHandler;
    private static class ApiHandler extends Handler {

        private String TAG = getClass().getSimpleName();
        private final WeakReference<OtherNewsListFragment> mFragment;

        public ApiHandler(OtherNewsListFragment aFragment){
            mFragment = new WeakReference<OtherNewsListFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            OtherNewsListFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_HEADLINE_NEWS_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsList = (List<NewsContent>) msg.obj;
                    if(Utility.DEBUG)Log.w(TAG, "mNewsList: " + fragment.mNewsList);
                    fragment.isGetHeadLineDone = true;
                    fragment.refreshDone();
                    break;
                case ParameterSet.GET_HEADLINE_NEWS_FAILED:
                    fragment.loadingFailed(ParameterSet.GET_HEADLINE_NEWS_FAILED);
                    break;

                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    break;

                case ParameterSet.GET_HOT_NEWS_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsList = (List<NewsContent>) msg.obj;
                    fragment.isGetHotNewsDone = true;
                    fragment.refreshDone();
                    break;
                case ParameterSet.GET_HOT_NEWS_FAILED:
                    fragment.loadingFailed(ParameterSet.GET_HOT_NEWS_FAILED);
                    break;

                case ParameterSet.GET_INSTANT_NEWS_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsList = (List<NewsContent>) msg.obj;
                    fragment.isGetInstantNewsDone = true;
                    fragment.refreshDone();
                    break;
                case ParameterSet.GET_INSTANT_NEWS_FAILED:
                    fragment.loadingFailed(ParameterSet.GET_INSTANT_NEWS_FAILED);
                    break;

                case ParameterSet.GET_NEAR_BY_NEWS_DONE:
                    fragment.isApiLoadingSuccess = true;
                    fragment.mRetryCount = 0;
                    fragment.mNewsList = (List<NewsContent>) msg.obj;
                    fragment.isGetNearByNewsDone = true;
                    fragment.refreshDone();
                    break;
                case ParameterSet.GET_NEAR_BY_NEWS_FAILED:
                    fragment.loadingFailed(ParameterSet.GET_NEAR_BY_NEWS_FAILED);
                    break;

                case CHECK_LIST:
                    if (fragment.isGetHeadLineDone && fragment.mNewsList != null) {
                        UserDataInfo.setHeadlineContent(fragment.mNewsList);
                        if(fragment.isAdded()){
                            fragment.processList();
                        }
                        break;
                    } else if (fragment.isGetHotNewsDone && fragment.mNewsList != null) {
                        UserDataInfo.setHotNewsContent(fragment.mNewsList);
                        if(fragment.isAdded()) {
                            fragment.processList();
                        }
                        break;
                    } else if ((fragment.isGetInstantNewsDone || fragment.isGetNearByNewsDone) && fragment.mNewsList != null) {
                        if(fragment.isAdded()) {
                            fragment.processList();
                        }
                        break;
                    } else {
                        sendEmptyMessageDelayed(CHECK_LIST, 1000);
                    }
                    break;


                case RELOAD_API:
                    fragment.getNewsList();
                    break;

            }

        }

    };

    private void loadingFailed(int aErrorCode){
        Log.e(TAG, "mRetryCount: " + mRetryCount);
        isApiLoadingSuccess = false;
        if(mRetryCount<3){
            mRetryCount++;
            if(mApiHandler !=null && mApiHandler.hasMessages(RELOAD_API)){
                mApiHandler.removeMessages(RELOAD_API);
            }
            mApiHandler.sendEmptyMessage(RELOAD_API);
        }else{
            refreshDone();
            mRetryCount = 0;
            vLoadingLayout.setVisibility(View.GONE);
            vList.setVisibility(View.GONE);
            vErrorMessage.setVisibility(View.VISIBLE);
            vErrorMessage.setText(String.format(getString(R.string.api_loading_error), aErrorCode));
            switch(aErrorCode){
                case ParameterSet.GET_HEADLINE_NEWS_FAILED:
                    isGetHeadLineDone = true;
                    break;
                case ParameterSet.GET_HOT_NEWS_FAILED:
                    isGetHotNewsDone = true;
                    break;
                case ParameterSet.GET_INSTANT_NEWS_FAILED:
                    isGetInstantNewsDone = true;
                    break;
                case ParameterSet.GET_NEAR_BY_NEWS_FAILED:
                    isGetNearByNewsDone = true;
                    break;
            }
        }
    }

    private void refreshDone(){
        if (isRefereshing) {
            // Stop refresh animation
            isRefereshing = false;
            vRefreshLayout.setRefreshing(false);
        }
    }

    public OtherNewsListFragment() {
        // Do nothing...
    }

    private Handler mAdHandler;
    private RecyclerView.RecycledViewPool mPool;
    public void setData(Handler aAdHandler, RecyclerView.RecycledViewPool aPool){
        mAdHandler = aAdHandler;
        mPool = aPool;
    }

    public void setData(String aCategoryName) {
        mCategoryName = aCategoryName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");

        initController();
        processArgument();
        processView();
        getNewsList();
        if (mApiHandler != null) {
            mApiHandler.sendEmptyMessage(CHECK_LIST);
        }
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

        isGetHeadLineDone = false;
        isGetHotNewsDone = false;
        isGetInstantNewsDone = false;
        isGetNearByNewsDone = false;
        if (mCategoryName.equals(getString(R.string.headline))) {
            getHeadlineNews();
        } else if (mCategoryName.equals(getString(R.string.hot_news))) {
            getHotNews();
        } else if (mCategoryName.equals(getString(R.string.instant_news))) {
            getInstantNews();
        } else if (mCategoryName.equals(getString(R.string.nearbynews))){
            getNearByNews();
        }
    }

    private void getHeadlineNews() {
        if (Utility.DEBUG) Log.v(TAG, "getHeadlineNews()");
        if (mApiController != null) {
            mApiController.getHeadlineNews(mApiHandler);
        }
    }

    private void getInstantNews() {
        if (Utility.DEBUG) Log.v(TAG, "getInstantNews()");
        if (mApiController != null) {
            mApiController.getInstantNews(mApiHandler);
        }
    }

    private void getHotNews() {
        if (Utility.DEBUG) Log.v(TAG, "getHotNews()");
        if (mApiController != null) {
            mApiController.getHotNews(mApiHandler);
        }
    }

    private void getNearByNews(){
        if (Utility.DEBUG) Log.v(TAG, "getNearByNews()");
        double[] longitudeLatitude = getLongitudeLatitude();
        if (mApiController != null) {
            mApiController.getNearByNews(mApiHandler, longitudeLatitude[0], longitudeLatitude[1]);
        }
    }

    private double[] getLongitudeLatitude(){
        double[] longitudeLatitude = new double[2];

        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
        long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGPSEnabled && !isNetworkEnabled){
            return longitudeLatitude;
        }else if(isGPSEnabled){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mLocationListener);
            if(locationManager!=null){
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location!=null){
                    longitudeLatitude[0] = location.getLongitude();
                    longitudeLatitude[1] = location.getLatitude();
                    Log.i(TAG, "isGPSEnabled longitude: " + longitudeLatitude[0]);
                    Log.i(TAG, "isGPSEnabled latitude: " + longitudeLatitude[1]);
                    locationManager.removeUpdates(mLocationListener);
                    return longitudeLatitude;
                }else{
                    Log.w(TAG, "isGPSEnabled location==null");
                }
                locationManager.removeUpdates(mLocationListener);
            }else{
                Log.w(TAG, "isGPSEnabled locationManager==null");
            }
        }

        if(isNetworkEnabled){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mLocationListener);
            if(locationManager!=null){
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null){
                    longitudeLatitude[0] = location.getLongitude();
                    longitudeLatitude[1] = location.getLatitude();
                    Log.i(TAG, "isNetworkEnabled longitude: " + longitudeLatitude[0]);
                    Log.i(TAG, "isNetworkEnabled latitude: " + longitudeLatitude[1]);
                }else{
                    Log.w(TAG, "isNetworkEnabled location==null");
                }
                locationManager.removeUpdates(mLocationListener);
            }else{
                Log.w(TAG, "isNetworkEnabled locationManager==null");
            }
        }

        return longitudeLatitude;
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void processArgument() {
        mCategoryName = getArguments().getString(KEY_CATEGORY_NAME);
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler(this);
    }

    private void processView() {

        View view = getView();

        vList = (RecyclerView) view.findViewById(R.id.list);
        vList.setVisibility(View.GONE);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getNewsList();
            }
        });

        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vErrorMessage = (TextView) view.findViewById(R.id.error_message);

    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processList() {

        if (Utility.DEBUG) Log.e(TAG, "processList()");

        vLoadingLayout.setVisibility(View.GONE);
        vList.setVisibility(View.VISIBLE);

//		mAdapter = new NewsListFragmentAdapter(getActivity(), mNewsList);
//		vList.setAdapter(mAdapter);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setRecycleChildrenOnDetach(true);
        vList.setLayoutManager(mLinearLayoutManager);
        if(mPool!=null){
            vList.setRecycledViewPool(mPool);
        }
        NewsListRecyclerViewAdapter adapter = new NewsListRecyclerViewAdapter(getActivity(), mNewsList, mCategoryName, getChildFragmentManager(), getString(R.string.news));
        vList.setAdapter(adapter);
        vList.setVisibility(View.VISIBLE);
        vLoadingLayout.setVisibility(View.GONE);

    }

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

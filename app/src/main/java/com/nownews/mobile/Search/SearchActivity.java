package com.nownews.mobile.Search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.SearchInfoJson;
import com.nownews.mobile.NewsCategory.NewsListRecyclerViewAdapter;
import com.nownews.mobile.Widget.CustomAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private ImageButton vBack;
    private CustomAutoCompleteTextView vSearchView;
    private ImageButton vClearButton;
    private RecyclerView vSearchList;
    private RelativeLayout vSearchHistoryLayout;
    private RelativeLayout vLoadingLayout;
    private TextView vLoadingText;
    private TextView vLoadingTextKeywords;
    private SwipeRefreshLayout vSwipeRefreshLayout;
    private ListView vSearchHistory;
    private TextView vHint;

    private String[] mSearchKeywords;
    private ApiController mApiController;
    private List<SearchInfoJson.NewsListBean> mSearchInfo;
    private NewsListRecyclerViewAdapter mAdapter;

    private SharedPreferencesMethods mSharedPref;
    private ArrayList<String> mSearchHistoryList;
    private String mSearchKeyWords;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private OnClickListener mSearchViewClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            vSearchView.isKeyboardShowing = true;
        }
    };
    private OnClickListener mClearClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (vSearchView != null && !vSearchView.getText().toString().equals("")) {
                vSearchView.setText("");
            }
        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_SEARCH_INFO_DONE:
                    mSearchInfo = (List<SearchInfoJson.NewsListBean>) msg.obj;
                    if (mSearchInfo.size() == 0) {
                        vSearchHistoryLayout.setVisibility(View.GONE);
                        vSearchList.setVisibility(View.GONE);
                        vHint.setVisibility(View.VISIBLE);
                        String hint = getString(R.string.no_search_resourt);
                        hint = String.format(hint, mSearchKeyWords);
                        vHint.setText(hint);
                    } else {
                        processList();
                    }
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vSwipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case ParameterSet.GET_SEARCH_INFO_FAILED:
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vSwipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(SearchActivity.this);
                    break;
            }

        }
    };
    private OnItemClickListener mSearchHistoryItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position == mSearchHistoryList.size() - 1) {
                //clear list
                mSharedPref.clearSearchHistoryList();
                processSearchHistory();
                return;
            }

            mSearchKeyWords = (String) parent.getItemAtPosition(position);
            if (Utility.DEBUG) Log.e(TAG, "mSearchKeyWords: " + mSearchKeyWords);
            startSearch(mSearchKeyWords);

        }
    };
    private OnEditorActionListener mSearchActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mSearchKeyWords = v.getText().toString().trim();
                startSearch(mSearchKeyWords);
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        startActivity();

    }

    private void startActivity() {

        initController();
        processView();
        processListener();
        processKeywords();
        processSearchHistory();

    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mSharedPref = new SharedPreferencesMethods(this);
    }

    private boolean isRefereshing = false;
    private void processView() {

        vBack = (ImageButton) findViewById(R.id.btn_back);
        vSearchView = (CustomAutoCompleteTextView) findViewById(R.id.search_edittext);
        vClearButton = (ImageButton) findViewById(R.id.btn_clear);
        vSearchList = (RecyclerView) findViewById(R.id.list);
        vSearchList.setLayoutManager(new LinearLayoutManager(this));

        vSearchHistoryLayout = (RelativeLayout) findViewById(R.id.search_history_layout);
        vSearchHistory = (ListView) findViewById(R.id.search_history);
        vHint = (TextView) findViewById(R.id.hint);

        vSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        vSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                reload();
            }
        });

        vLoadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        vLoadingText = (TextView) findViewById(R.id.image_loading_txt);
        vLoadingTextKeywords = (TextView) findViewById(R.id.image_loading_keywords);

    }

    private void processListener() {

        vBack.setOnClickListener(mBackClickListener);
        vSearchView.setOnClickListener(mSearchViewClickListener);
        vSearchView.setOnEditorActionListener(mSearchActionListener);
        vClearButton.setOnClickListener(mClearClickListener);

    }

    private void processKeywords() {
//		mSearchKeywords = getResources().getStringArray(R.array.search_keywords);
        mSearchHistoryList = mSharedPref.getSearchHistoryList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSearchHistoryList);
        vSearchView.setAdapter(adapter);
    }

    private void processSearchHistory() {
        vLoadingLayout.setVisibility(View.GONE);
        vHint.setVisibility(View.GONE);
        vSearchHistoryLayout.setVisibility(View.VISIBLE);
        vSwipeRefreshLayout.setVisibility(View.GONE);
        mSearchHistoryList = mSharedPref.getSearchHistoryList();
        if (mSearchHistoryList.size() > 0) {
            mSearchHistoryList.add("清除最近搜尋關鍵字");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSearchHistoryList);
        vSearchHistory.setAdapter(adapter);
        vSearchHistory.setOnItemClickListener(mSearchHistoryItemClickListener);
    }

    private void startSearch(String searchKeyWords) {
        vLoadingLayout.setVisibility(View.VISIBLE);
        vLoadingTextKeywords.setText(searchKeyWords);
        vSearchHistoryLayout.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vSearchView.getWindowToken(), 0);
        vSearchView.isKeyboardShowing = false;
        getSearchInfo(searchKeyWords, 1);
        mSharedPref.saveSearchKeywords(searchKeyWords);
    }

    @Override
    public void onBackPressed() {
        if(vSwipeRefreshLayout.getVisibility()==View.VISIBLE){
            vSwipeRefreshLayout.setVisibility(View.GONE);
            vSearchHistoryLayout.setVisibility(View.VISIBLE);
            vLoadingLayout.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    private void getSearchInfo(String aSearchKeyWords, int aPage) {
        if (mApiController == null) {
            mApiController = ApiController.getInstance();
        }
        mApiController.getSearchInfo(mHandler, aSearchKeyWords, aPage);
    }

    private void processList() {
        String categoryName = getString(R.string.search);
        vHint.setVisibility(View.GONE);
        vSearchHistoryLayout.setVisibility(View.GONE);
        vSwipeRefreshLayout.setVisibility(View.VISIBLE);
        vLoadingLayout.setVisibility(View.GONE);
        if(mAdapter==null) {
            mAdapter = new NewsListRecyclerViewAdapter(this, mSearchInfo, categoryName, getSupportFragmentManager(), categoryName, null);
            vSearchList.setAdapter(mAdapter);
        }else{
            mAdapter.setData(mSearchInfo, categoryName, getSupportFragmentManager(), categoryName);
        }
    }

    @Override
    protected void onResume() {

        GoogleAnalyticsFunction.setScreenName(this, "搜尋頁面");
        UserDataInfo.activityResumed(this);

        super.onResume();
    }

    @Override
    protected void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    public void reload() {
        if (mSearchKeyWords != null && !mSearchKeyWords.isEmpty()) {
            startSearch(mSearchKeyWords);
        } else {
            startActivity();
        }
    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(this);
        }
        super.onDestroy();
    }

}

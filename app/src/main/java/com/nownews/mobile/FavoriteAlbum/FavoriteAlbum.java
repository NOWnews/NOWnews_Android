package com.nownews.mobile.FavoriteAlbum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.AppController;

import java.util.ArrayList;

public class FavoriteAlbum extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private Toolbar vToolbar;
    private RecyclerView vRecyclerView;
    private SwipeRefreshLayout vRefreshLayout;
    private TextView vHint;
    private SharedPreferencesMethods mSharedPref;
    private ArrayList<String> mImageList;
    private LinearLayoutManager mStaggeredGridLayoutManager;
    private FavoriteAlbumGridAdapter mAdapter;
    private AppController mAppController;
    private boolean isRefereshing = false;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_album);

        initController();
        processView();
        processToolbar();
        getFavoriteImageList();
        processRecyclerView();

    }

    private void initController() {
        mSharedPref = new SharedPreferencesMethods(this);
        mAppController = AppController.getInstance(this);
    }

    private void processView() {
        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vRecyclerView = (RecyclerView) findViewById(R.id.favorite_album_grid);
        vHint = (TextView) findViewById(R.id.hint);

        vRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getFavoriteImageList();
                processRecyclerView();
            }
        });
    }

    private void getFavoriteImageList() {
        mImageList = mSharedPref.getFavoriteImageList();
    }

    private void processRecyclerView() {
        mStaggeredGridLayoutManager = new LinearLayoutManager(this);
        vRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        if (mAdapter == null) {
            mAdapter = new FavoriteAlbumGridAdapter(this, mImageList);
        } else {
            mAdapter.setData(mImageList);
        }
        vRecyclerView.setAdapter(mAdapter);
        if (isRefereshing) {
            // Stop refresh animation
            isRefereshing = false;
            vRefreshLayout.setRefreshing(false);
        }
        if (mImageList.size() > 0) {
            vHint.setVisibility(View.GONE);
        } else {
            vHint.setVisibility(View.VISIBLE);
        }
    }

    public void processToolbar() {

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        vToolbar.setLogo(R.drawable.nownews_logo);
        vToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        vToolbar.setNavigationOnClickListener(mBackClickListener);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//		case R.id.action_share: //分享
//			break;
//		case R.id.action_favorite: //加入我的最愛
//			break;
//		case R.id.action_comment: //留言
//			break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_favorite_album_page, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //reset
        if (resultCode == FavoriteAlbumPage.RESULT_CODE) {
            getFavoriteImageList();
            processRecyclerView();
        }

    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);
        if(mAdapter!=null){
            mAdapter.destoryView();
        }
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(this);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsFunction.setScreenName(this, "最愛圖集");
        UserDataInfo.activityResumed(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    public void reload() {

        if (Utility.DEBUG) Log.e(TAG, "startSplashActivity()");
        initController();
        processView();
        processToolbar();
        getFavoriteImageList();
        processRecyclerView();

    }
}

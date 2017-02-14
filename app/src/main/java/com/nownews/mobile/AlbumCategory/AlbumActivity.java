package com.nownews.mobile.AlbumCategory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Search.SearchActivity;
import com.nownews.mobile.Widget.MenuContent;

/**
 * Created by cindy on 2016/11/1.
 */

public class AlbumActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private CoordinatorLayout vCoordinatorLayout;
    private DrawerLayout vDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar vToolbar;
    private MenuContent vMenuContent;
    public final static int REQUEST_CODE = 0x123;
    public final static int RESULT_CODE = 0x321;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        if (Utility.DEBUG)Log.d(TAG, "in NewHome");
        processView();
        processListener();
        processDrawerLayout();
        processAlbumFragment();

    }

    private AlbumCategoryFragment mAlbumCategoryFragment;
    private void processAlbumFragment(){
        if(mAlbumCategoryFragment==null){
            mAlbumCategoryFragment = new AlbumCategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(AlbumCategoryFragment.KEY_POSITION, 0);
            mAlbumCategoryFragment.setArguments(bundle);
        }else{
            if(!mAlbumCategoryFragment.isApiLoadingSuccess){
                mAlbumCategoryFragment.reload();
            }
        }
        getSupportFragmentManager().beginTransaction().add(R.id.main_body, mAlbumCategoryFragment).commit();
    }

    @Override
    public void onResume() {

        if (Utility.DEBUG)Log.v(TAG, "@@@onResume()@@@");

        GoogleAnalyticsFunction.setScreenName(this, getString(R.string.album));
        UserDataInfo.activityResumed(this);

        if (UserDataInfo.mPageSwapCount > 0) {
            UserDataInfo.mPageSwapCount = 0;
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        UserDataInfo.activityDestroy(this);
        super.onDestroy();
    }

    private void processView(){

        vCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator_layout);
        vDrawerLayout = (DrawerLayout) findViewById(R.id.drw_layout);
        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vMenuContent = (MenuContent) findViewById(R.id.menu_content);

    }

    private void processListener(){
        vMenuContent.init(this, vDrawerLayout);
    }

    private void processDrawerLayout() {

        setSupportActionBar(vToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        mDrawerToggle = new ActionBarDrawerToggle(this, vDrawerLayout, vToolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                GoogleAnalyticsFunction.sendHitInfo(AlbumActivity.this, getString(R.string.album), "點擊Menu", "");
                vMenuContent.getVersionInfo();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    vDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    vDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        mDrawerToggle.syncState();
        vDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_search: //action bar上的search鈕

                GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.album), "點擊搜尋", "");

                Intent intent = new Intent();
                intent.setClass(AlbumActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE);
        super.onBackPressed();
    }

    public Snackbar mSnackbar;
    public void showSnackBar(){

        if(mSnackbar!=null && mSnackbar.isShown()){
            return;
        }

        if(mSnackbar==null){
            mSnackbar = Snackbar.make(vCoordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout)mSnackbar.getView();
            TextView textView = (TextView)layout.findViewById(android.support.design.R.id.snackbar_text);
            textView.setVisibility(View.INVISIBLE);
            textView.setMaxLines(0);

            LayoutInflater inflater = LayoutInflater.from(AlbumActivity.this);
            View customSnackView = inflater.inflate(R.layout.widget_download_list_layout, null);
            layout.addView(customSnackView);
        }

        mSnackbar.show();
        if (Utility.DEBUG)Log.d(TAG, "mSnackbar SHOW");

    }

    public void dissmissSnackBar(){
        if(mSnackbar!=null && mSnackbar.isShown()){
            mSnackbar.dismiss();
        }
    }

    public void showErrorSnackBar(String aErrorMessage){
        Snackbar.make(vCoordinatorLayout, aErrorMessage, Snackbar.LENGTH_LONG).show();
    }

}

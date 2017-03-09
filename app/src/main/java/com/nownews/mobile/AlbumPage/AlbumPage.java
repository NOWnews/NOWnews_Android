package com.nownews.mobile.AlbumPage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Common.Utility.ShareType;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.PhotosInfoJson;
import com.nownews.mobile.Widget.CustomViewPager;

public class AlbumPage extends AppCompatActivity {

    public static final String KEY_ALBUM_ID = "albumId";
    public static final String KEY_ALBUM_URL = "albumUrl";
    public static final String KEY_ALBUM_LIST = "albumList";
    public static final String KEY_ALBUM_CATEGORY = "albumCategory";
    public static final String KEY_FROM_WHERE = "fromWhere";
    private final String TAG = getClass().getSimpleName();
    private Toolbar vToolbar;
    private TextView vToolbarText;
    private CustomViewPager vViewPager;
    private String mAlbumUrl;
    private int mAlbumId;
    private int mIndex;
    private String mFromWhere;

    private AlbumPageFragmentAdapter mAdapter;
    private ApiController mApiController;
    private SharedPreferencesMethods mSharedPref;
    private String mAlbumCategory;

    private PhotosInfoJson mAlbumInfo;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private Menu mMenu;
    private OnPageChangeListener mViewPagerChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {
            mIndex = position;
            String imageId = null;
            if (mAlbumInfo != null
                    && mAlbumInfo.collectionImages != null
                    && mAlbumInfo.collectionImages.get(position) != null
                    && mAlbumInfo.collectionImages.get(position).thumbnail != null
                    && mAlbumInfo.collectionImages.get(position).nodeId != -1) {
                imageId = String.valueOf(mAlbumInfo.collectionImages.get(position).nodeId);
                vToolbarText.setText((position + 1) + "/" + mAlbumInfo.collectionImages.size());
            }
            boolean isExist = mSharedPref.isFavoriteIdExist(imageId);
            MenuItem menuItem = mMenu.findItem(R.id.action_favorite);
            if (isExist) {
                menuItem.setIcon(R.drawable.ic_favorite_white_24dp);
            } else {
                menuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
            }
            AlbumPageFragment albumPageFragment = (AlbumPageFragment) mAdapter.getItem(position);
            if (albumPageFragment != null) {
                albumPageFragment.setHitInfo(mAlbumCategory);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (Utility.DEBUG) Log.e(TAG, "onPageScrolled");
            if (checkDirection) {
                if (Utility.DEBUG) Log.d(TAG, "position: " + position);
                if (Utility.DEBUG) Log.d(TAG, "thresholdOffset: " + thresholdOffset);
                if (Utility.DEBUG) Log.d(TAG, "positionOffset: " + positionOffset);
                if (positionOffset == 0.0) {
                    if (position == 0) {
                        Toast.makeText(AlbumPage.this, "這是第一張照片喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(AlbumPage.this, "最後一張照片囉!", Toast.LENGTH_LONG).show();
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
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_PHOTOS_INFO_DONE:
                    mAlbumInfo = (PhotosInfoJson) msg.obj;
                    processViewPager();
                    break;
                case ParameterSet.GET_PHOTOS_INFO_FAILED:
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(AlbumPage.this);
                    break;
            }

        }

    };
    private boolean isToolBarVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_page);

        initController();
        processBundle();
        processView();
        processToolbar();

    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mSharedPref = new SharedPreferencesMethods(this);
    }

    private void processBundle() {
        if (getIntent() != null) {
            mAlbumUrl = getIntent().getStringExtra(KEY_ALBUM_URL);
            mAlbumId = getIntent().getIntExtra(KEY_ALBUM_ID, -1);
            if (mAlbumUrl != null && mAlbumUrl.startsWith("/n/")) {
                mAlbumId = Integer.parseInt(mAlbumUrl.substring(mAlbumUrl.lastIndexOf("/") + 1));
            } else if (mAlbumUrl != null) {
                mAlbumId = Integer.parseInt(mAlbumUrl.substring(mAlbumUrl.lastIndexOf("/") + 1, mAlbumUrl.lastIndexOf("?")));
            }
            mAlbumCategory = getIntent().getStringExtra(KEY_ALBUM_CATEGORY);
        }
    }

    public void processView() {

        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vToolbarText = (TextView) vToolbar.findViewById(R.id.toolbar_title);
        vViewPager = (CustomViewPager) findViewById(R.id.album_page_viewpager);

    }

    private void processViewPager() {

        setHitInfo();

        String imageId = null;
        if (mAlbumInfo != null
                && mAlbumInfo.collectionImages != null
                && mAlbumInfo.collectionImages.get(0) != null
                && mAlbumInfo.collectionImages.get(0).nodeId != -1) {
            imageId = String.valueOf(mAlbumInfo.collectionImages.get(0).nodeId);
        }
        boolean isExist = mSharedPref.isFavoriteIdExist(imageId);
        MenuItem menuItem = mMenu.findItem(R.id.action_favorite);
        if (isExist) {
            menuItem.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            menuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
        }

        mAdapter = new AlbumPageFragmentAdapter(AlbumPage.this, getSupportFragmentManager(), mAlbumInfo);
        vViewPager.setAdapter(mAdapter);
        vViewPager.addOnPageChangeListener(mViewPagerChangeListener);
        vToolbarText.setText("1/" + mAlbumInfo.collectionImages.size());

    }

    private void setHitInfo(){
          if(mAlbumInfo!=null){
              String title = mAlbumInfo.title;
              if (title != null && title.contains("▲")) {
                  title = title.replaceAll("▲", "");
              }
              if (title != null && title.contains("▼")) {
                  title = title.replaceAll("▼", "");
              }
              String url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mAlbumInfo.url;
              if(mFromWhere!=null && mFromWhere.equals("GcmIntentService")){
                  GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.cloud_message), getString(R.string.cloud_message_click), title + " " + url);
              }else{
                  GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.album), mAlbumCategory, title + " " + url);
              }
          }
    }

    public void processToolbar() {

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        vToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        vToolbar.setNavigationOnClickListener(mBackClickListener);

    }

    private void getNewsInfo() {
        if (mApiController != null) {
            mApiController.getPhotosInfo(mHandler, mAlbumId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_share: //分享
                if (mAlbumInfo == null) {
                    break;
                }
                String newsTitle = mAlbumInfo.title;
                int newsId = mAlbumInfo.nodeId;
                String newsUrl = WebAPIUrl.NOWNEWS_MOBIEL_WEB_PHOTO_DOMAIN + newsId;
                String shareMessage = Utility.getShareMessage(AlbumPage.this, newsUrl, newsTitle, ShareType.photo);
                Utility.shareToSNS(this, shareMessage);
                break;
            case R.id.action_favorite: //加入我的最愛
                addToFavorite(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addToFavorite(MenuItem item) {
        int currentFragmentIndex = vViewPager.getCurrentItem();
        if (mAlbumInfo != null
                && mAlbumInfo.collectionImages != null
                && mAlbumInfo.collectionImages.get(currentFragmentIndex) != null) {

            int imageId = mAlbumInfo.collectionImages.get(currentFragmentIndex).nodeId;
            String imageUrl = mAlbumInfo.collectionImages.get(currentFragmentIndex).thumbnail;
//            imageUrl = Utility.getSrcFromImgapi(imageUrl);
            String title = mAlbumInfo.collectionImages.get(currentFragmentIndex).cite;
            String newsUrl = WebAPIUrl.NOWNEWS_MOBIEL_WEB_PHOTO_DOMAIN + imageId;

            if (Utility.DEBUG) Log.e(TAG, "imageId: " + imageId);
            if (Utility.DEBUG) Log.e(TAG, "imageUrl: " + imageUrl);
            if (Utility.DEBUG) Log.e(TAG, "title: " + title);

            if (imageId == -1
                    || imageUrl == null
                    || imageUrl.trim().equals("")) {
                Toast.makeText(this, getString(R.string.data_error), Toast.LENGTH_LONG).show();
                return;
            }

            boolean isExist = mSharedPref.isFavoriteIdExist(String.valueOf(imageId));
            if (isExist) {
                mSharedPref.removeFavoriteImageInfo(String.valueOf(imageId));
                item.setIcon(R.drawable.ic_favorite_outline_white_24dp);
            } else {
                mSharedPref.saveFavoriteImageInfo(String.valueOf(imageId), imageUrl, title, newsUrl);
                item.setIcon(R.drawable.ic_favorite_white_24dp);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_album_page, menu);

        mMenu = menu;
        getNewsInfo();
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    public void setToolBarElevation(float aElevation) {
        vToolbar.setElevation(aElevation);
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null) {
            mAdapter.clearFragmentList();
        }
        super.onBackPressed();
    }

    public boolean getToolBarVisibility() {
        return isToolBarVisible;
    }

    public void hideToolBar() {
        vToolbar.animate().translationY(-vToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        isToolBarVisible = false;
    }

    public void showToolBar() {
        vToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        isToolBarVisible = true;
    }

    public void stopViewPagerSwipe() {
        vViewPager.setPagingEnabled(false);
    }

    public void startViewPagerSwipe() {
        vViewPager.setPagingEnabled(true);
    }

    @Override
    protected void onResume() {
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
        if (mAdapter != null) {
            AlbumPageFragment albumPageFragment = (AlbumPageFragment) mAdapter.getItem(mIndex);
            if (albumPageFragment != null) {
                albumPageFragment.reload();
            }
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

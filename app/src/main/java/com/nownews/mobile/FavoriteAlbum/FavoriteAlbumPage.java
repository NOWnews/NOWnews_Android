package com.nownews.mobile.FavoriteAlbum;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
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
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Common.Utility.ShareType;
import com.nownews.mobile.Widget.CustomViewPager;

import java.util.ArrayList;

public class FavoriteAlbumPage extends AppCompatActivity {

    public static final String KEY_FAVORITE_ALBUM_POSITION = "position";
    public static final String KEY_FAVORITE_ALBUM_LIST = "favoritAlbumList";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IMAGE_TITLE = "imageTitle";
    public static final String KEY_NEWS_URL = "newsUrl";
    public static final int TYPE_FAVORITE = 0x543;
    public static final int TYPE_NEWS_IMAGES = 0x916;
    //	public static final String KEY_FAVORITE_ALBUM_TITLE_LIST = "favoritAlbumTitleList";
    public static final int RESULT_CODE = 0x555;
    private final String TAG = getClass().getSimpleName();
    private Toolbar vToolbar;
    private TextView vToolbarText;
    private CustomViewPager vViewPager;
    private int mCurrentPosition = 1;
    private ArrayList<String> mFavoriteAlbumList;

    private FavoriteAlbumPageFragmentAdapter mAdapter;
    private SharedPreferencesMethods mSharedPref;
    private int mCurrentType;
    private String mImageTitle;
    private String mNewsUrl;
    private int mIndex;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private Menu mMenu;
    private boolean isToolBarVisible = true;
    private OnPageChangeListener mViewPagerChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {
            mIndex = position;
            String url = mFavoriteAlbumList.get(position);
            String imageId = mSharedPref.searchFavoriteId(url);
            boolean isExist = mSharedPref.isFavoriteIdExist(imageId);
            MenuItem menuItem = mMenu.findItem(R.id.action_favorite);
            if (isExist) {
                menuItem.setIcon(R.drawable.ic_favorite_white_24dp);
            } else {
                menuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
            }

            int currentFragmentIndex = vViewPager.getCurrentItem();
            if (getToolBarVisibility()) {
                ((FavoritePageFragment) (mAdapter.getItem(currentFragmentIndex))).changeTitleVisibility(true);
            } else {
                ((FavoritePageFragment) (mAdapter.getItem(currentFragmentIndex))).changeTitleVisibility(false);
            }

            if (mCurrentType == TYPE_FAVORITE) {
                GoogleAnalyticsFunction.sendHitInfo(FavoriteAlbumPage.this, "最愛圖集", url, "");
            } else if (mCurrentType == TYPE_NEWS_IMAGES) {
                GoogleAnalyticsFunction.sendHitInfo(FavoriteAlbumPage.this, "新聞圖片", url, "");
            }
            vToolbarText.setText((position + 1) + "/" + mFavoriteAlbumList.size());
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
                        Toast.makeText(FavoriteAlbumPage.this, "這是第一張照片喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(FavoriteAlbumPage.this, "最後一張照片囉!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_page);
        if (Utility.DEBUG) Log.e(TAG, "onCreate");

        startActivity();

    }

    private void startActivity() {

        initController();
        processBundle();
        processView();
        processToolbar();
//		Utility.processReturnBar(this);

    }

    private void initController() {
        mSharedPref = new SharedPreferencesMethods(this);
    }

    private void processBundle() {
        if (getIntent() != null) {
            mCurrentPosition = getIntent().getIntExtra(KEY_FAVORITE_ALBUM_POSITION, 0);
            mFavoriteAlbumList = getIntent().getStringArrayListExtra(KEY_FAVORITE_ALBUM_LIST);
            mCurrentType = getIntent().getIntExtra(KEY_TYPE, TYPE_FAVORITE);
            mImageTitle = getIntent().getStringExtra(KEY_IMAGE_TITLE);
            mNewsUrl = getIntent().getStringExtra(KEY_NEWS_URL);
            if (Utility.DEBUG) Log.e(TAG, "mFavoriteAlbumList: " + mFavoriteAlbumList);
        }
    }

    public void processView() {

        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vToolbarText = (TextView) vToolbar.findViewById(R.id.toolbar_title);
        vViewPager = (CustomViewPager) findViewById(R.id.album_page_viewpager);

    }

    private void processViewPager() {

        if (mCurrentType == TYPE_FAVORITE) {
            mAdapter = new FavoriteAlbumPageFragmentAdapter(FavoriteAlbumPage.this, getSupportFragmentManager(), mFavoriteAlbumList, mCurrentType);
        } else if (mCurrentType == TYPE_NEWS_IMAGES) {
            mAdapter = new FavoriteAlbumPageFragmentAdapter(FavoriteAlbumPage.this, getSupportFragmentManager(), mFavoriteAlbumList, mCurrentType, mImageTitle, mNewsUrl);
        }
        vViewPager.setAdapter(mAdapter);
        vViewPager.addOnPageChangeListener(mViewPagerChangeListener);
        vViewPager.setCurrentItem(mCurrentPosition);
        vToolbarText.setText((mCurrentPosition + 1) + "/" + mFavoriteAlbumList.size());

    }

    public void processToolbar() {

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        vToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        vToolbar.setNavigationOnClickListener(mBackClickListener);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_share: //分享
                if(mCurrentType == TYPE_FAVORITE && mFavoriteAlbumList!=null){
                    mNewsUrl = mFavoriteAlbumList.get(mCurrentPosition);
                    String id = mSharedPref.searchFavoriteId(mNewsUrl);
                    mImageTitle = mSharedPref.searchFavoriteAlbumTitle(id);
                }
                String newsTitle = mImageTitle;
                String newsUrl = mNewsUrl;
                String shareMessage = Utility.getShareMessage(FavoriteAlbumPage.this, newsUrl, newsTitle, ShareType.photo);
                Utility.shareToSNS(this, shareMessage);
                break;
            case R.id.action_favorite: //加入我的最愛
                addToFavorite(item);
                break;
//		case R.id.action_comment: //留言
//			Toast.makeText(FavoriteAlbumPage.this, "Comment", Toast.LENGTH_LONG).show();
////			showShareDialog();
//			break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addToFavorite(MenuItem item) {
        int currentFragmentIndex = vViewPager.getCurrentItem();
        String imageUrl = mFavoriteAlbumList.get(currentFragmentIndex);
        String imageId = mSharedPref.searchFavoriteId(imageUrl);
        if (imageId == null) {
            imageId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            if (Utility.DEBUG) Log.v(TAG, "imageUrl: " + imageUrl);
            if (Utility.DEBUG) Log.v(TAG, "imageId: " + imageId);
        }
        String title = ((FavoritePageFragment) (mAdapter.getItem(currentFragmentIndex))).getImageTitle();
        String newsUrl = ((FavoritePageFragment) (mAdapter.getItem(currentFragmentIndex))).getImageNewsUrl();
        boolean isExist = mSharedPref.isFavoriteIdExist(imageId);
        if (isExist) {
//			Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show();
            mSharedPref.removeFavoriteImageInfo(imageId);
            item.setIcon(R.drawable.ic_favorite_outline_white_24dp);
        } else {
//			Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
            mSharedPref.saveFavoriteImageInfo(imageId, imageUrl, title, newsUrl);
            item.setIcon(R.drawable.ic_favorite_white_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utility.DEBUG) Log.e(TAG, "onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_album_page, menu);

        mMenu = menu;
        String url = mFavoriteAlbumList.get(mCurrentPosition);
        String imageId = mSharedPref.searchFavoriteId(url);
        boolean isExist = mSharedPref.isFavoriteIdExist(imageId);
        MenuItem menuItem = mMenu.findItem(R.id.action_favorite);
        if (isExist) {
            menuItem.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            menuItem.setIcon(R.drawable.ic_favorite_outline_white_24dp);
        }
        processViewPager();

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
        setResult(RESULT_CODE);
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
        if (Utility.DEBUG) Log.e(TAG, "reload()");
        if (mAdapter != null) {
            FavoritePageFragment favoritePageFragment = (FavoritePageFragment) mAdapter.getItem(mIndex);
            if (favoritePageFragment != null) {
                favoritePageFragment.reload();
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

    public enum Type {FavoriteAlbum, NewsImages}

}

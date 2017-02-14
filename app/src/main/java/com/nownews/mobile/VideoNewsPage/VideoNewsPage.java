package com.nownews.mobile.VideoNewsPage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.ad2iction.mobileads.Ad2ictionErrorCode;
import com.ad2iction.mobileads.Ad2ictionInterstitial;
import com.ad2iction.mobileads.Ad2ictionInterstitial.InterstitialAdListener;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import com.google.android.youtube.player.YouTubePlayer;
import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.VideosListJson;
import com.nownews.mobile.Json.VideosListJson.VideosContent;
import com.nownews.mobile.Json.NewsListJson.NewsContent;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Widget.CustomViewPager;

import java.util.List;

public class VideoNewsPage extends AppCompatActivity implements InterstitialAdListener, VideoNewsPageRecyclerViewFragment.OnPageLoadFinishedListener {

    public static final String KEY_NEWS_ID = "newsId";
    public static final String KEY_NEWS_URL = "newsUrl";
    public static final String KEY_NEWS_INDEX = "index";
    public static final String KEY_NEWS_CATEGORY = "newsCategory";
    //For AD2 Page AD Start
//	public static final String PUB_ID_INTERSTITIAL = "355b3228-c16b-11e3-ade5-f23c91dba5f7";
    public static final String PUB_ID_INTERSTITIAL = "e94d08f0-6817-11e5-8e01-f23c9173ed43"; //2015-10-01 add
    private final String TAG = getClass().getSimpleName();
    private final int RESULT_CODE = 0x321;
    public boolean isReload = false;
    private Toolbar vToolbar;
    private CustomViewPager vViewPager;
    private int mNewsId = -1;
    private String mNewsUrl;
    private int mNewsIndex;
    private List<VideosContent> mNewsList;
    private String mNewsCategory;
    private VideoNewsPageFragmentAdapter mAdapter;
    private Ad2ictionInterstitial mAd2ictionInterstitial;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private SharedPreferencesMethods mSharedPref;
    private boolean isAD2LoadFinished = false;
    private VideoNewsPageRecyclerViewFragment mTempVideoNewsPageRecyclerViewFragment;
    private OnPageChangeListener mViewPagerChangeListener = new OnPageChangeListener() {

        private static final float thresholdOffset = 0.5f;
        private boolean scrollStarted, checkDirection;

        @Override
        public void onPageSelected(int position) {
            if (Utility.DEBUG) Log.e(TAG, "onPageSelected()");

            if (!UserDataInfo.isSingalNewsFromAction) {
                UserDataInfo.mPageSwapCount++;
                if (Utility.DEBUG)
                    Log.e(TAG, "UserDataInfo.mPageSwapCount: " + UserDataInfo.mPageSwapCount);
                if (UserDataInfo.mPageSwapCount % 4 == 0) {
                    showAD2PageAD();
                } else if (UserDataInfo.mPageSwapCount % 2 == 0) {
                    preloadAD2PageAD();
                }
            }

            mNewsIndex = position;
            if (mSharedPref == null) {
                mSharedPref = new SharedPreferencesMethods(VideoNewsPage.this);
            }
            String textSize = mSharedPref.getNewsContentTextSize();
            int currentPage = vViewPager.getCurrentItem();
            VideoNewsPageRecyclerViewFragment newsPageFragment = (VideoNewsPageRecyclerViewFragment) mAdapter.getItem(currentPage);
            if (newsPageFragment != null) {
                newsPageFragment.changeTextSize(textSize);
                newsPageFragment.setHitInfo(mNewsCategory);
                if(mTempVideoNewsPageRecyclerViewFragment!=null){
                    if (Utility.DEBUG)Log.d(TAG, "mTempVideoNewsPageRecyclerViewFragment!=null");
                    mTempVideoNewsPageRecyclerViewFragment.removeYoutubeFragment();
                }
                newsPageFragment.processVideo();
            }
            mTempVideoNewsPageRecyclerViewFragment = newsPageFragment;
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
                        Toast.makeText(VideoNewsPage.this, "這是第一則新聞喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(VideoNewsPage.this, "最後一則新聞囉!\n請回上一層觀看更多新聞。", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_news_page);

        startNewsPage();

    }

    private void startNewsPage() {

        processBundle();
        processView();
        processListener();
        processToolbar();

    }

    private void processBundle() {
        if (getIntent() != null) {

            Intent intent = getIntent();
            String action = intent.getAction();

            mNewsId = intent.getIntExtra(KEY_NEWS_ID, -1);
            mNewsUrl = intent.getStringExtra(KEY_NEWS_URL);
            mNewsCategory = intent.getStringExtra(KEY_NEWS_CATEGORY);
            mNewsIndex = intent.getIntExtra(KEY_NEWS_INDEX, 0);

            if (Utility.DEBUG) Log.e(TAG, "mNewsId: " + mNewsId);
            if (Utility.DEBUG) Log.e(TAG, "mNewsUrl: " + mNewsUrl);
            if (Utility.DEBUG) Log.e(TAG, "mNewsCategory: " + mNewsCategory);
            if (Utility.DEBUG) Log.e(TAG, "mNewsIndex: " + mNewsIndex);

            mNewsList = UserDataInfo.getVideoNewsList();

        }
    }

    public void processView() {

        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vViewPager = (CustomViewPager) findViewById(R.id.news_page_viewpager);

    }

    private void processListener() {

        mAdapter = new VideoNewsPageFragmentAdapter(VideoNewsPage.this, getSupportFragmentManager(), mNewsList, this);
        vViewPager.setAdapter(mAdapter);
        vViewPager.setCurrentItem(mNewsIndex);
        vViewPager.addOnPageChangeListener(mViewPagerChangeListener);
        VideoNewsPageRecyclerViewFragment newsPageFragment = (VideoNewsPageRecyclerViewFragment) mAdapter.getItem(mNewsIndex);
        mTempVideoNewsPageRecyclerViewFragment = newsPageFragment;

    }

    public void preloadAD2PageAD() {
        if (mAd2ictionInterstitial == null) {
            mAd2ictionInterstitial = new Ad2ictionInterstitial(this, PUB_ID_INTERSTITIAL);
            mAd2ictionInterstitial.setInterstitialAdListener(this);
        }
        mAd2ictionInterstitial.load();
    }

    public void showAD2PageAD() {
        if (mAd2ictionInterstitial != null && isAD2LoadFinished) {
            mAd2ictionInterstitial.show();
        }
        isAD2LoadFinished = false;
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
            case R.id.action_text: //字體大小
                if (Utility.DEBUG) Log.e(TAG, "Text Size");
                GoogleAnalyticsFunction.sendHitInfo(this, "新聞內頁", "點擊字體大小", "");
                showTextSizeDialog();
                break;
            case R.id.action_share: //分享

                String newsTitle = null;
                int newsId = -1;
                String newsUrl = null;

                if (mNewsList == null) {
                    return false;
                }
                newsTitle = mNewsList.get(vViewPager.getCurrentItem()).title;
                newsId = mNewsList.get(vViewPager.getCurrentItem()).nodeId;
                newsUrl = WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN + newsId;

                String shareMessage = Utility.getShareMessage(VideoNewsPage.this, newsUrl, newsTitle, Utility.ShareType.news);
                Utility.shareToSNS(this, shareMessage);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTextSizeDialog() {

        if (mSharedPref == null) {
            mSharedPref = new SharedPreferencesMethods(this);
        }
        String textSize = mSharedPref.getNewsContentTextSize();
        int checkboxIndex = 0;
        if (textSize.equals(getString(R.string.small))) {
            checkboxIndex = 2;
        } else if (textSize.equals(getString(R.string.mid))) {
            checkboxIndex = 1;
        } else if (textSize.equals(getString(R.string.max))) {
            checkboxIndex = 0;
        }

        new MaterialDialog.Builder(this)
                .title(getString(R.string.choose_text_size))
                .items(R.array.text_size_list)
                .itemsCallbackSingleChoice(checkboxIndex, new ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                        GoogleAnalyticsFunction.sendHitInfo(VideoNewsPage.this, "字體大小", (String) text, "");

                        int currentPage = vViewPager.getCurrentItem();
                        VideoNewsPageRecyclerViewFragment newsPageFragment = (VideoNewsPageRecyclerViewFragment) mAdapter.getItem(currentPage);
                        if (newsPageFragment != null) {
                            newsPageFragment.changeTextSize((String) text);
                        }

                        return true;
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_news_page, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    public void setToolBarElevation(float aElevation) {
        vToolbar.setElevation(aElevation);
    }

    @Override
    public void onBackPressed() {
        int currentPage = vViewPager.getCurrentItem();
        VideoNewsPageRecyclerViewFragment newsPageFragment = (VideoNewsPageRecyclerViewFragment) mAdapter.getItem(currentPage);
        if (mAdapter != null) {
            mAdapter.clearFragmentList();
        }
        if (mAd2ictionInterstitial != null) {
            mAd2ictionInterstitial.destroy();
            mAd2ictionInterstitial = null;
        }
        setResult(RESULT_CODE);
        super.onBackPressed();
    }

    @Override
    public void onInterstitialClicked(Ad2ictionInterstitial arg0) {

    }

    @Override
    public void onInterstitialDismissed(Ad2ictionInterstitial arg0) {

    }

    @Override
    public void onInterstitialFailed(Ad2ictionInterstitial arg0, Ad2ictionErrorCode arg1) {

    }

    @Override
    public void onInterstitialLoaded(Ad2ictionInterstitial arg0) {
        isAD2LoadFinished = true;
        if (Utility.DEBUG) Log.e(TAG, "isAD2LoadFinished: " + isAD2LoadFinished);
        mAd2ictionInterstitial = arg0;
    }

    @Override
    public void onInterstitialShown(Ad2ictionInterstitial arg0) {

    }
    //For AD2 Page AD End

    public void setViewPagerSwappable(boolean isPageSwappable) {
        vViewPager.setPagingEnabled(isPageSwappable);
    }

    public void gotoHeadlineNewsPage(int position, List<NewsContent> aHeadlineNewsList) {

        if (aHeadlineNewsList == null
                || aHeadlineNewsList.get(position) == null) {
            Toast.makeText(this, getString(R.string.reference_error), Toast.LENGTH_LONG).show();
            return;
        }

        int newsId = aHeadlineNewsList.get(position)._id;
        if (newsId == -1) {
            Toast.makeText(this, getString(R.string.reference_error), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.setClass(this, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_INDEX, position);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_HEADLINE_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, getString(R.string.headline_news));
        startActivity(intent);

        if (!UserDataInfo.isSingalNewsFromAction) {
            UserDataInfo.mPageSwapCount++;
            if (Utility.DEBUG)
                Log.e(TAG, "UserDataInfo.mPageSwapCount: " + UserDataInfo.mPageSwapCount);
            if (UserDataInfo.mPageSwapCount % 4 == 0) {
                showAD2PageAD();
            } else if (UserDataInfo.mPageSwapCount % 2 == 0) {
                preloadAD2PageAD();
            }
        }
    }

    @Override
    protected void onPause() {
        if (Utility.DEBUG) Log.e(TAG, "onPause()");
        UserDataInfo.activityPaused();
        if (mAd2ictionInterstitial != null) {
            mAd2ictionInterstitial.destroy();
            mAd2ictionInterstitial = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        GoogleAnalyticsFunction.setScreenName(this, "新聞內頁");
        UserDataInfo.activityResumed(this);

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);
        if (mAdapter != null) {
            mAdapter.clearFragmentList();
        }
        if (mAd2ictionInterstitial != null) {
            mAd2ictionInterstitial.destroy();
            mAd2ictionInterstitial = null;
        }
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(this);
        }
        super.onDestroy();
    }

    public String getCurrentNewsCategory() {
        return mNewsCategory;
    }

    public void reload() {
        if (mAdapter != null) {
            VideoNewsPageRecyclerViewFragment newsPageFragment = (VideoNewsPageRecyclerViewFragment) mAdapter.getItem(mNewsIndex);
            if (newsPageFragment != null) {
                newsPageFragment.reload();
            }
        }
    }

    private int getNextPageIndex() {
        int listSize = 0;
        listSize = mNewsList.size();
        int currentPage = getCurrentPage();
        if ((currentPage + 1) == listSize) {
            currentPage = 0;
        } else {
            currentPage += 1;
        }
        return currentPage;
    }

    private int getPrevPageIndex() {
        int listSize = 0;
        listSize = mNewsList.size();
        int currentPage = getCurrentPage();
        if (currentPage == 0) {
            currentPage = listSize - 1;
        } else {
            currentPage -= 1;
        }
        return currentPage;
    }

    private String getTitleInList(int aIndex) {
        String title = null;
        title = mNewsList.get(aIndex).title;
        return title;
    }

    public void gotoNextNews() {
        vViewPager.setCurrentItem(getNextPageIndex());
    }

    public void gotoPrevNews() {
        vViewPager.setCurrentItem(getPrevPageIndex());
    }

    public String getNextPageTitle() {
        return getTitleInList(getNextPageIndex());
    }

    public String getPrevPageTitle() {
        return getTitleInList(getPrevPageIndex());
    }

    public int getCurrentPage() {
        return vViewPager.getCurrentItem();
    }

    public boolean isLastPage() {
        if (getCurrentPage() == mAdapter.getCount() - 1) {
            return true;
        }
        return false;
    }

    public enum SingalNewsType {url, id}

    @Override
    public void OnPageLoadFinished(VideoNewsPageRecyclerViewFragment aCurrentFragment, int aCurrentFragmentPosition) {
        int currentPage = vViewPager.getCurrentItem();
        if (Utility.DEBUG)Log.w(TAG, "@@@ currentPage: " + currentPage);
        if (Utility.DEBUG)Log.w(TAG, "@@@ aCurrentFragmentPosition: " + aCurrentFragmentPosition);
        if(currentPage==aCurrentFragmentPosition){
            if (Utility.DEBUG)Log.e(TAG, "@@@ OnPageLoadFinished!!!");
            aCurrentFragment.processVideo();
        }
    }

    @Override
    public void OnYoutubeInitializationSuccess(VideoNewsPageRecyclerViewFragment aCurrentFragment, int aCurrentFragmentPosition, YouTubePlayer youTubePlayer, String aYoutubeId) {
        int currentPage = vViewPager.getCurrentItem();
        if (Utility.DEBUG)Log.w(TAG, "@@@ currentPage: " + currentPage);
        if (Utility.DEBUG)Log.w(TAG, "@@@ aCurrentFragmentPosition: " + aCurrentFragmentPosition);
        if(currentPage==aCurrentFragmentPosition){
            if (Utility.DEBUG)Log.e(TAG, "@@@ OnYoutubeInitializationSuccess!!!");
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            youTubePlayer.cueVideo(aYoutubeId); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

}

package com.nownews.mobile.NewsPage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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
import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.HeadlineNewsJson;
import com.nownews.mobile.Json.NewsInfoJson;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.RelationsNewsInfoJson;
import com.nownews.mobile.Json.SearchInfoJson;
import com.nownews.mobile.Json.SpecialNewsListJson;
import com.nownews.mobile.NewHome;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnInterstitialAd;

import java.util.HashSet;
import java.util.List;

import static com.nownews.mobile.Common.Utility.isVponTestMode;

public class NewsPage extends AppCompatActivity implements InterstitialAdListener /**, IWeiboHandler.Response*/
{

    public static final String KEY_NEWS_ID = "newsId";
    public static final String KEY_NEWS_URL = "newsUrl";
    public static final String KEY_NEWS_INDEX = "index";
    public static final String KEY_NEWS_LIST = "newsList";
    public static final String KEY_NEWS_TYPE = "newsType";
    public static final String KEY_NEWS_CATEGORY = "newsCategory";
    public static final String KEY_NEWS_BIG_CATEGORY = "bigCategory";
    public static final int TYPE_HEADLINE_NEWS = 0x999;
    public static final int TYPE_NORMAL_NEWS = 0x888;
    public static final int TYPE_SINGAL_NEWS = 0x777;
    public static final int TYPE_SEARCH_NEWS = 0x666;
    public static final int TYPE_INSTANT_NEWS = 0x555;
    public static final int TYPE_REFERENCE_NEWS = 0x444;
    public static final int TYPE_SPECIAL_NEWS = 0x333;
    //For AD2 Page AD Start
//	public static final String PUB_ID_INTERSTITIAL = "355b3228-c16b-11e3-ade5-f23c91dba5f7";
    public static final String PUB_ID_INTERSTITIAL = "e94d08f0-6817-11e5-8e01-f23c9173ed43"; //2015-10-01 add
    private final String TAG = getClass().getSimpleName();
    private final int RESULT_CODE = 0x321;
    public boolean isReload = false;
    private Toolbar vToolbar;
    private ViewPager vViewPager;
    private int mNewsId = -1;
    private String mNewsUrl;
    private int mNewsIndex;
    private List<HeadlineNewsJson.CarouselsBean> mHeadlineNewsList;
    private List<NewsListJson.NewsListBean> mNewsList;
    private List<RelationsNewsInfoJson.RelationsNewsBean> mReferenceNewsList;
    private List<SpecialNewsListJson.NewsListBean> mSpecialNewsList;
    private List<SearchInfoJson.NewsListBean> mSearchList;
    private int mNewsType;
    private String mNewsCategory;
    private String mBigCategory;
    private NewsPageFragmentAdapter mAdapter;
    private Ad2ictionInterstitial mAd2ictionInterstitial;
    private OnClickListener mBackClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private SharedPreferencesMethods mSharedPref;
    private boolean isAD2LoadFinished = false;
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
//                    showAD2PageAD();
                    showVponPageAD();
                } else if (UserDataInfo.mPageSwapCount % 2 == 0) {
//                    preloadAD2PageAD();
                    preloadVponPageAD();
                }
            }

            mNewsIndex = position;
            if (mSharedPref == null) {
                mSharedPref = new SharedPreferencesMethods(NewsPage.this);
            }
            String textSize = mSharedPref.getNewsContentTextSize();
            int currentPage = vViewPager.getCurrentItem();
            NewsPageRecyclerViewFragment newsPageFragment = (NewsPageRecyclerViewFragment) mAdapter.instantiateItem(vViewPager, currentPage);
            if (newsPageFragment != null) {
                newsPageFragment.changeTextSize(textSize);
                Log.v(TAG, "newsPageFragment.isNewsInfoLoadSucess(): " + newsPageFragment.isNewsInfoLoadSucess());
                if(!newsPageFragment.isNewsInfoLoadSucess()){
                    newsPageFragment.getNewsInfo();
                }
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
                        Toast.makeText(NewsPage.this, "這是第一則新聞喔~", Toast.LENGTH_LONG).show();
                    } else if (position == mAdapter.getCount() - 1) {
                        Toast.makeText(NewsPage.this, "最後一則新聞囉!\n請回上一層觀看更多新聞。", Toast.LENGTH_LONG).show();
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
//	public void showAD2PageAD() {
//		if(mAd2ictionInterstitial!=null){
//			mAd2ictionInterstitial.destroy();
//			mAd2ictionInterstitial = null;
//		}
//		mAd2ictionInterstitial = new Ad2ictionInterstitial(this, PUB_ID_INTERSTITIAL);
//		mAd2ictionInterstitial.setInterstitialAdListener(this);
//		mAd2ictionInterstitial.load();
//	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page);

        startNewsPage();

    }

    private void startNewsPage() {

//        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, UserDataInfo.WEIBO_APP_KEY);
        processBundle();
        processView();
        processListener();
        processToolbar();

    }

    private void processBundle() {
        if (getIntent() != null) {

            Intent intent = getIntent();
            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                Uri uri = intent.getData();
                String path = uri.getPath();
				if(Utility.DEBUG)Log.e(TAG, "uri: " + uri);
				if(Utility.DEBUG)Log.e(TAG, "path: " + path);
                mNewsId = -1;
                mNewsUrl = path;
                mNewsIndex = 0;
                mNewsType = TYPE_SINGAL_NEWS;
                mNewsCategory = "外部開啟";
                UserDataInfo.isSingalNewsFromAction = true;
            } else {
                mNewsId = intent.getIntExtra(KEY_NEWS_ID, -1);
                mNewsUrl = intent.getStringExtra(KEY_NEWS_URL);
                mNewsCategory = intent.getStringExtra(KEY_NEWS_CATEGORY);
                mBigCategory = intent.getStringExtra(KEY_NEWS_BIG_CATEGORY);
                mNewsIndex = intent.getIntExtra(KEY_NEWS_INDEX, 0);
                mNewsType = intent.getIntExtra(KEY_NEWS_TYPE, 0);
            }

            if (Utility.DEBUG) Log.e(TAG, "mNewsId: " + mNewsId);
            if (Utility.DEBUG) Log.e(TAG, "mNewsUrl: " + mNewsUrl);
            if (Utility.DEBUG) Log.e(TAG, "mNewsCategory: " + mNewsCategory);
            if (Utility.DEBUG) Log.e(TAG, "mNewsIndex: " + mNewsIndex);
            if (Utility.DEBUG) Log.e(TAG, "mNewsType: " + mNewsType);

            switch (mNewsType) {
                case TYPE_HEADLINE_NEWS:
                    mHeadlineNewsList = UserDataInfo.getHeadlineContent();
                    break;
                case TYPE_NORMAL_NEWS:
                    mNewsList = UserDataInfo.getNewsList();
                    break;
                case TYPE_SEARCH_NEWS:
                    mSearchList = UserDataInfo.getSearchList();
                    break;
                case TYPE_REFERENCE_NEWS:
                    mReferenceNewsList = UserDataInfo.getReferenceNewsList();
                    break;
                case TYPE_SPECIAL_NEWS:
                    mSpecialNewsList = UserDataInfo.getSpecialNewsList();
                    break;
            }
        }
    }

    public void processView() {

        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vViewPager = (ViewPager) findViewById(R.id.news_page_viewpager);

    }

    private void processListener() {

        switch (mNewsType) {
            case TYPE_HEADLINE_NEWS:
                mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mHeadlineNewsList, TYPE_HEADLINE_NEWS);
                break;
            case TYPE_NORMAL_NEWS:
                mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mNewsList);
                break;
            case TYPE_SEARCH_NEWS:
                mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mSearchList, TYPE_SEARCH_NEWS);
                break;
            case TYPE_REFERENCE_NEWS:
                mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mReferenceNewsList, TYPE_REFERENCE_NEWS);
                break;
            case TYPE_SPECIAL_NEWS:
                mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mSpecialNewsList, TYPE_SPECIAL_NEWS);
                break;
            case TYPE_SINGAL_NEWS:
                if (mNewsId == -1 && mNewsUrl != null) {
                    if (Utility.DEBUG) Log.d(TAG, "mNewsId==null && mNewsUrl!=null");
                    mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mNewsUrl, SingalNewsType.url);
                } else if (mNewsId != -1 && mNewsUrl == null) {
                    if (Utility.DEBUG) Log.d(TAG, "mNewsId!=null && mNewsUrl==null");
                    mAdapter = new NewsPageFragmentAdapter(getSupportFragmentManager(), mNewsId, SingalNewsType.id);
                }
                break;
        }
        if(mAdapter!=null){
            mAdapter.setCategory(mBigCategory, mNewsCategory);
            vViewPager.setAdapter(mAdapter);
            vViewPager.setCurrentItem(mNewsIndex);
            vViewPager.addOnPageChangeListener(mViewPagerChangeListener);
        }

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
                showTextSizeDialog();
                break;
            case R.id.action_share: //分享

                String newsTitle = null;
                int newsId = -1;
                String newsUrl = null;

                switch (mNewsType) {
                    case TYPE_HEADLINE_NEWS:
                        if (mHeadlineNewsList == null) {
                            return false;
                        }
                        newsTitle = mHeadlineNewsList.get(vViewPager.getCurrentItem()).getShortTitle();
                        newsId = mHeadlineNewsList.get(vViewPager.getCurrentItem()).getSn();
                        break;
                    case TYPE_NORMAL_NEWS:
                        if (mNewsList == null) {
                            return false;
                        }
                        newsTitle = mNewsList.get(vViewPager.getCurrentItem()).getShortTitle();
                        newsId = mNewsList.get(vViewPager.getCurrentItem()).getSn();
                        break;
                    case TYPE_SEARCH_NEWS:
                        if (mSearchList == null) {
                            return false;
                        }
                        newsTitle = mSearchList.get(vViewPager.getCurrentItem()).getShortTitle();
                        newsId = mSearchList.get(vViewPager.getCurrentItem()).getSn();
                        break;
                    case TYPE_REFERENCE_NEWS:
                        if (mReferenceNewsList == null) {
                            return false;
                        }
                        newsTitle = mReferenceNewsList.get(vViewPager.getCurrentItem()).getShortTitle();
                        newsId = mReferenceNewsList.get(vViewPager.getCurrentItem()).getSn();
                        break;
                    case TYPE_SPECIAL_NEWS:
                        if(mSpecialNewsList == null){
                            return false;
                        }
                        newsTitle = mSpecialNewsList.get(vViewPager.getCurrentItem()).getShortTitle();
                        newsId = mSpecialNewsList.get(vViewPager.getCurrentItem()).getSn();
                        break;
                    case TYPE_SINGAL_NEWS:
                        Log.d(TAG, "TYPE_SINGAL_NEWS");
                        NewsPageRecyclerViewFragment newsPageFragment = (NewsPageRecyclerViewFragment) mAdapter.instantiateItem(vViewPager, vViewPager.getCurrentItem());
                        NewsInfoJson newsInfo = newsPageFragment.getCurrentNewsInfo();
                        if(newsInfo==null){
                            return false;
                        }
                        newsTitle = newsInfo.getTitle();
                        newsId = newsInfo.getSn();
                        break;
                }

                if(newsId!=-1){
                    newsUrl = WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN + newsId;
                }
                String shareMessage = Utility.getShareMessage(NewsPage.this, newsUrl, newsTitle, Utility.ShareType.news);
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

                        int currentPage = vViewPager.getCurrentItem();
                        NewsPageRecyclerViewFragment newsPageFragment = (NewsPageRecyclerViewFragment) mAdapter.instantiateItem(vViewPager, currentPage);
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
        NewsPageRecyclerViewFragment newsPageFragment = (NewsPageRecyclerViewFragment) mAdapter.instantiateItem(vViewPager, currentPage);
        if(newsPageFragment!=null && newsPageFragment.isVideoFrameLayoutVisibile()){
            newsPageFragment.closeVideoFrame();
            return;
        }
        if (mAd2ictionInterstitial != null) {
            mAd2ictionInterstitial.destroy();
            mAd2ictionInterstitial = null;
        }
        if(UserDataInfo.isSingalNewsFromAction){
            Intent intent = new Intent();
            intent.setClass(this, NewHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            setResult(RESULT_CODE);
        }
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

    public void gotoReferenceNewsPage(int position, List<RelationsNewsInfoJson.RelationsNewsBean> aReferenceNewsList) {

        if (aReferenceNewsList == null
                || aReferenceNewsList.get(position) == null) {
            Toast.makeText(this, getString(R.string.reference_error), Toast.LENGTH_LONG).show();
            return;
        }

        int newsId = aReferenceNewsList.get(position).getSn();
        if (newsId == -1) {
            Toast.makeText(this, getString(R.string.reference_error), Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.setClass(this, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_INDEX, position);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_REFERENCE_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, getString(R.string.reference_news));
        startActivity(intent);

        if (!UserDataInfo.isSingalNewsFromAction) {
            UserDataInfo.mPageSwapCount++;
            if (Utility.DEBUG)
                Log.e(TAG, "UserDataInfo.mPageSwapCount: " + UserDataInfo.mPageSwapCount);
            if (UserDataInfo.mPageSwapCount % 4 == 0) {
//                showAD2PageAD();
                showVponPageAD();
            } else if (UserDataInfo.mPageSwapCount % 2 == 0) {
//                preloadAD2PageAD();
                preloadVponPageAD();
            }
        }
    }

    public void gotoHeadlineNewsPage(int position, List<HeadlineNewsJson.CarouselsBean> aHeadlineNewsList) {

        if (aHeadlineNewsList == null
                || aHeadlineNewsList.get(position) == null) {
            Toast.makeText(this, getString(R.string.reference_error), Toast.LENGTH_LONG).show();
            return;
        }

        int newsId = aHeadlineNewsList.get(position).getSn();
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
//                showAD2PageAD();
                showVponPageAD();
            } else if (UserDataInfo.mPageSwapCount % 2 == 0) {
//                preloadAD2PageAD();
                preloadVponPageAD();
            }
        }
    }

    private boolean isInterstitialAdReady;
    private VpadnInterstitialAd mVpadnAd;
    private void preloadVponPageAD(){
        if (Utility.DEBUG) Log.i(TAG, "preloadVponPageAD()");
        final String TAG = "preloadVponPageAD";
        mVpadnAd = new VpadnInterstitialAd(this, getString(R.string.vpon_all_page), "TW");
        mVpadnAd.setAdListener(new VpadnAdListener() {
            @Override
            public void onVpadnReceiveAd(VpadnAd vpadnAd) {
                if (Utility.DEBUG) Log.i(TAG, "onVpadnReceiveAd");
                isInterstitialAdReady = true;
            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd vpadnAd, VpadnAdRequest.VpadnErrorCode vpadnErrorCode) {
                if (Utility.DEBUG) Log.i(TAG, "onVpadnFailedToReceiveAd");
                isInterstitialAdReady = false;
            }

            @Override
            public void onVpadnPresentScreen(VpadnAd vpadnAd) {
                if (Utility.DEBUG) Log.i(TAG, "onVpadnPresentScreen");
            }

            @Override
            public void onVpadnDismissScreen(VpadnAd vpadnAd) {
                if (Utility.DEBUG) Log.i(TAG, "onVpadnDismissScreen");
            }

            @Override
            public void onVpadnLeaveApplication(VpadnAd vpadnAd) {
                if (Utility.DEBUG) Log.i(TAG, "onVpadnLeaveApplication");
            }

        });
        VpadnAdRequest request = new VpadnAdRequest();
        if(isVponTestMode){
            HashSet<String> testDevicesImeiSet = new HashSet<>();
            testDevicesImeiSet.add(Utility.getAdvertisingId());
            request.setTestDevices(testDevicesImeiSet);
        }
        mVpadnAd.loadAd(request);
    }

    private void showVponPageAD(){
        if (Utility.DEBUG) Log.i(TAG, "showVponPageAD()");
        if(isInterstitialAdReady && mVpadnAd.isReady()){
            mVpadnAd.show();
            isInterstitialAdReady = false;
        }
    }

//	@Override
//	public void onWindowFocusChanged(boolean hasFocus)
//	{
//	    super.onWindowFocusChanged(hasFocus);
//	    int currentApiVersion = android.os.Build.VERSION.SDK_INT;
//	    if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
//	    {
//	        getWindow().getDecorView().setSystemUiVisibility(
//	            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//	                | View.SYSTEM_UI_FLAG_FULLSCREEN
//	                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//	    }
//	}

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
            NewsPageRecyclerViewFragment newsPageFragment = (NewsPageRecyclerViewFragment) mAdapter.instantiateItem(vViewPager, mNewsIndex);
            if (newsPageFragment != null) {
                newsPageFragment.reload();
            }
        }
    }

    private int getNextPageIndex() {
        int listSize = 0;
        switch (mNewsType) {
            case TYPE_HEADLINE_NEWS:
                listSize = mHeadlineNewsList.size();
                break;
            case TYPE_NORMAL_NEWS:
                listSize = mNewsList.size();
                break;
            case TYPE_SEARCH_NEWS:
                listSize = mSearchList.size();
                break;
            case TYPE_REFERENCE_NEWS:
                listSize = mReferenceNewsList.size();
                break;
            case TYPE_SPECIAL_NEWS:
                listSize = mSpecialNewsList.size();
                break;
        }
        int currentPage = getCurrentPage();
        if ((currentPage + 1) == listSize) {
            currentPage = 0;
        } else {
            currentPage += 1;
        }
        return currentPage;
    }

//	@Override
//	public void onResponse(BaseResponse baseResp) {
//		switch (baseResp.errCode) {
//        case WBConstants.ErrorCode.ERR_OK:
//            Toast.makeText(this, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
//            break;
//        case WBConstants.ErrorCode.ERR_CANCEL:
//            Toast.makeText(this, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
//            break;
//        case WBConstants.ErrorCode.ERR_FAIL:
//            Toast.makeText(this, 
//                    getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: " + baseResp.errMsg, 
//                    Toast.LENGTH_LONG).show();
//            break;
//        }
//	}
//	
//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		mWeiboShareAPI.handleWeiboResponse(intent, this);
//	}

    private int getPrevPageIndex() {
        int listSize = 0;
        switch (mNewsType) {
            case TYPE_HEADLINE_NEWS:
                listSize = mHeadlineNewsList.size();
                break;
            case TYPE_NORMAL_NEWS:
                listSize = mNewsList.size();
                break;
            case TYPE_SEARCH_NEWS:
                listSize = mSearchList.size();
                break;
            case TYPE_REFERENCE_NEWS:
                listSize = mReferenceNewsList.size();
                break;
            case TYPE_SPECIAL_NEWS:
                listSize = mSpecialNewsList.size();
                break;
        }
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
        switch (mNewsType) {
            case TYPE_HEADLINE_NEWS:
                title = mHeadlineNewsList.get(aIndex).getShortTitle();
                break;
            case TYPE_NORMAL_NEWS:
                title = mNewsList.get(aIndex).getShortTitle();
                break;
            case TYPE_SEARCH_NEWS:
                title = mSearchList.get(aIndex).getShortTitle();
                break;
            case TYPE_REFERENCE_NEWS:
                title = mReferenceNewsList.get(aIndex).getShortTitle();
                break;
            case TYPE_SPECIAL_NEWS:
                title = mSpecialNewsList.get(aIndex).getShortTitle();
                break;
        }
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
}

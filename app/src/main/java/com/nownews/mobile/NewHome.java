package com.nownews.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.nownews.R;
import com.nownews.mobile.Basic.BaseSideActivity;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Dao.CheckVerDao;
import com.nownews.mobile.Dao.LiveInfoDao;
import com.nownews.mobile.Download.DownloadAsyncTask;
import com.nownews.mobile.Download.DownloadListener;
import com.nownews.mobile.GCM.GcmＭanager;
import com.nownews.mobile.Json.CheckVersionJson;
import com.nownews.mobile.Json.LiveInfoJson;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Live.LiveFragment;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Search.SearchActivity;
import com.nownews.mobile.SpecialNewsCategory.SpecialNewsCategoryFragment;
import com.nownews.mobile.VideoNewsCategory.VideoNewsCategoryFragment;
import com.nownews.mobile.Widget.LiveMarquee;
import com.nownews.mobile.Widget.MenuContent;

import java.io.File;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

/**
 * Created by cindy on 2016/11/1.
 */

public class NewHome extends BaseSideActivity implements AHBottomNavigation.OnTabSelectedListener,
        MaterialDialog.SingleButtonCallback, DownloadListener.DownloadStatus, DialogInterface.OnShowListener {

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout vCoordinatorLayout;
    @BindView(R.id.bottom_navigation)
    AHBottomNavigation vBottomNavigation;

    ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.tool_bar)
    Toolbar vToolbar;
    @BindView(R.id.menu_content)
    MenuContent vMenuContent;
    @BindView(R.id.x)
    TextView vX;
    @BindView(R.id.csmuse_logo)
    ImageView vCsmuse;
    @BindView(R.id.llv_left_drawer)
    LinearLayout vLeftDrawer;
    @BindView(R.id.live_marquee)
    LiveMarquee vLiveMarquee;

    public final static int REQUEST_CODE = 0x123;
    public final static int RESULT_CODE = 0x321;
    public final static int RESULT_CODE_FROM_LIVE = 0x159;
    public final static int RESULT_CODE_FROM_LIVE_BAR = 0x357;

    private SharedPreferencesMethods mSharedPref;
    private GcmＭanager mGcmＭanager;
    private ReSizeLayoutParams mResize;
    private PublisherInterstitialAd mDFPInterstitial;
    private MaterialDialog mNotificationSwitchDialog;
    private boolean isNotificationOpen;
    private LiveInfoJson mLiveInfo;
    private DownloadAsyncTask downloadtask;

    private int mCurrentCategoryPage = -1;
    private LiveFragment mLiveFragment;

    private String mGooglePlayStorePackageName = "com.android.vending";
    private MaterialDialog vDownloadProgressDialog;

    private MaterialDialog mConfirmDialog;
    public Snackbar mSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utility.DEBUG) Log.d(TAG, "in NewHome");
        initContent();
        processBottomNavigation();
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_new_home;
    }

    @Override
    protected int getContentLayoutID() {
        return R.id.main_body;
    }

    protected void initContent() {
        this.mResize = new ReSizeLayoutParams(this);
        this.mSharedPref = new SharedPreferencesMethods(this);
        checkIsV3Version();
        this.vLeftDrawer.setLayoutParams(this.mResize.setOnSize(vLeftDrawer));
        this.mResize.unregisterCallback(this);
        processListener();
        processDrawerLayout();
        startGCM();
        /* 取得KMT直播資訊 &  取得版本資訊 */
        this.DoSync(new LiveInfoDao(this), new CheckVerDao(this));
    }

    @Override
    protected void LoaderResult(Object result) {
        if (result instanceof CheckVersionJson) {
            boolean isNeedToUpdate = this.checkVersion((CheckVersionJson) result);
            /**
             * 若需要跳更新訊息則不顯示蓋版廣告，反之，顯示
             * */
            if (!isNeedToUpdate && !this.isMenu) {
                this.showAllPageDFPAD();
            }
            this.isMenu = false;
        } else if (result instanceof LiveInfoJson) {
            this.mLiveInfo = (LiveInfoJson) result;
            if (this.mLiveInfo == null
                    || !this.mLiveInfo.isCampainStatus()
                    || this.mLiveInfo.getTitle() == null
                    || this.mLiveInfo.getTitle().trim().isEmpty()) {
                this.vLiveMarquee.setVisibility(View.GONE);
            } else {
                this.vLiveMarquee.setVisibility(View.VISIBLE);
                this.vLiveMarquee.setBackgroundUrl(this.mLiveInfo.getBackground());
                this.vLiveMarquee.setIsOnAir(this.mLiveInfo.isOnAir());
                this.vLiveMarquee.setLiveTitle(this.mLiveInfo.getTitle());
                this.vLiveMarquee.setLiveUrl(this.mLiveInfo.getLivePage());
            }
        }
    }

    private void checkIsV3Version() {
        if (this.mSharedPref != null) {
            if (!this.mSharedPref.isV3Version()) {
                this.mSharedPref.clearAllSharedPreferencesData();
                this.mSharedPref.setIsV3Version();
            }
        }
    }

    private void checkIsAlreadyShowNotificationSetting() {
        if (this.mSharedPref != null) {
            if (!this.mSharedPref.isAlreadyShowNotificationSetting()) {
                Utility.showNotificationSwicherDialog(this, mNotificationDialogDismissListener);
            }
        }
    }

    private DialogInterface.OnDismissListener mNotificationDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            mSharedPref.setIsAlreadyShowNotificationSetting();
        }
    };

    @Override
    public void onResume() {
        if (Utility.DEBUG) Log.v(TAG, "@@@onResume()@@@");
        if (Utility.DEBUG) Log.i(TAG, "mCurrentCategoryPage: " + this.mCurrentCategoryPage);
        if (this.vBottomNavigation != null) {
            this.vBottomNavigation.setCurrentItem(this.mCurrentCategoryPage);
        }
        GoogleAnalyticsFunction.setScreenName(this, "首頁");
        UserDataInfo.activityResumed(this);
        if (UserDataInfo.mPageSwapCount > 0) {
            UserDataInfo.mPageSwapCount = 0;
        }
        super.onResume();
        checkIsAlreadyShowNotificationSetting();
    }

    @Override
    public void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        UserDataInfo.activityDestroy(this);
        if (BitmapController.getInstance(this) != null) {
            BitmapController.getInstance(this).clearCache();
            BitmapController.getInstance(this).closeBitmapController();
            BitmapController.getInstance(this).unregistBitmapController(this);
        }
        if (this.mSharedPref != null) {
            this.mSharedPref.unRegistContext(this);
        }
        super.onDestroy();
    }

    private void startGCM() {
        if (this.mGcmＭanager == null) {
            this.mGcmＭanager = new GcmＭanager(this);
        }
        this.mGcmＭanager.startGCM();
        String FCMReistId = this.mSharedPref.getGcmRegistId();
        if (FCMReistId == null
                || FCMReistId.trim().isEmpty()) {
            FCMReistId = this.mGcmＭanager.getToken();
            Utility.processGCMRegisterId(getApplicationContext(), FCMReistId);
        }
        if (Utility.DEBUG) Log.i(TAG, "###FCMReistId: " + FCMReistId);
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        // wasSelected => 是否正被選中 != isFocused 不要搞混了
        if (this.mCurrentCategoryPage == position) {
            return false;
        }
        Bundle bundle = null;
        switch (position) {
            case 0:
                if (Utility.DEBUG) Log.w(TAG, "Show 新聞");
                // 設定地首頁
                bundle = new Bundle();
                bundle.putInt(NewsCategoryFragment.KEY_POSITION, 0);
                setFistPage(new NewsCategoryFragment(), bundle);

                hideCsmuseIconOnToolBar();
                this.mCurrentCategoryPage = position;
                break;

            case 1:
                if (Utility.DEBUG) Log.w(TAG, "Show 特輯");
                hideCsmuseIconOnToolBar();
                changeFragment(new SpecialNewsCategoryFragment(), true, null);
                this.mCurrentCategoryPage = position;
                break;

            case 2:
                if (Utility.DEBUG) Log.w(TAG, "Show 影音");
                bundle = new Bundle();
                bundle.putInt(NewsCategoryFragment.KEY_POSITION, 0);
                changeFragment(new VideoNewsCategoryFragment(), true, bundle);
                hideCsmuseIconOnToolBar();

                this.mCurrentCategoryPage = position;
                break;

            case 3:
                if (Utility.DEBUG) Log.w(TAG, "Show 直播");
                if (this.mLiveFragment == null) {
                    this.mLiveFragment = new LiveFragment();
                } else {
                    if (!this.mLiveFragment.isApiLoadingSuccess) {
                        this.mLiveFragment.reload();
                    }
                }
                changeFragment(new LiveFragment(), true, null);
//                    showCsmuseIconOnToolBar();
                this.mCurrentCategoryPage = position;
                break;
        }
        return true;
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        switch (which) {
            case NEGATIVE:
                if (dialog == this.mConfirmDialog) {
                    // 退出
                    isNeedToLeave = true;
                    onBackPressed();
                } else {
                    goUpdate();
                }
                break;

            case POSITIVE:
                if (dialog == this.mNotificationSwitchDialog) {
                    this.mSharedPref.setNotificationStatus(this.isNotificationOpen);
                    this.mSharedPref.setAskOpenNotificationStatus(true);
                    startGCM();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (dialog == this.mConfirmDialog) {
            View view = mConfirmDialog.getCustomView();
            ImageView image = (ImageView) view.findViewById(R.id.image);
            TextView title = (TextView) view.findViewById(R.id.title);
            RelativeLayout newsLayout = (RelativeLayout) view.findViewById(R.id.news_layout);
            final List<NewsListJson.NewsContent> hotNewsList = UserDataInfo.getHotNewsContent();
            if (hotNewsList != null && hotNewsList.size() > 0) {
                Random random = new Random();
                final int index = random.nextInt(hotNewsList.size());
                if (hotNewsList.get(index) != null
                        && hotNewsList.get(index).image != null
                        && hotNewsList.get(index).image.thumbnail != null
                        && hotNewsList.get(index).field_short_title != null
                        && hotNewsList.get(index).field_short_title.value != null) {
                    String imageUrl = hotNewsList.get(index).image.thumbnail;
                    if (imageUrl != null) {
                        BitmapController.getInstance(NewHome.this).loadImageWithOriginalSize(imageUrl, image, BitmapController.IMAGE_SRC, 0, 0, null);
                    }
                    String titleText = hotNewsList.get(index).field_short_title.value;
                    title.setText(titleText);
                    newsLayout.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            int id = hotNewsList.get(index)._id;
                            Intent intent = new Intent();
                            intent.setClass(NewHome.this, NewsPage.class);
                            intent.putExtra(NewsPage.KEY_NEWS_ID, id);
                            intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_SINGAL_NEWS);
                            intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, "離開提醒");
                            startActivity(intent);
                            mConfirmDialog.dismiss();

                        }
                    });
                } else {
                    newsLayout.setVisibility(View.GONE);
                }
            }
        }
        if (dialog == this.mNotificationSwitchDialog) {
            View view = this.mNotificationSwitchDialog.getCustomView();
            ((TextView) view.findViewById(R.id.message)).setText("期待已久的新聞推播功能來啦！\n請選擇您要開啟或關閉新聞推播通知");
            Switch switcher = (Switch) view.findViewById(R.id.switcher);
            this.isNotificationOpen = this.mSharedPref.getNotificationStatus();
            switcher.setChecked(this.isNotificationOpen);
            switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isNotificationOpen = isChecked;
                }
            });
        }
    }

    @Override
    public void onDownloadSize(String url, int size) {
        if (this.vDownloadProgressDialog == null) {
            this.vDownloadProgressDialog = new MaterialDialog.Builder(this)
                    .title(R.string.downloading)
                    .progress(false, 100, true)
                    .cancelable(false).show();
        }
        this.vDownloadProgressDialog.setProgress(size);
    }

    @Override
    public void downloadFinish(String url) {
        this.vDownloadProgressDialog.dismiss();
        this.vDownloadProgressDialog = null;
        this.installAPK();
    }

    private void goUpdate() {
        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
            @Override
            public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                String url = null;
                switch ((int) item.getTag()) {
                    case 0:
                        // google play
                        Intent MyIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.nownews"));
                        startActivity(MyIntent);
                        break;

                    case 1:
                        // download one
                        downloadApk(getString(R.string.update_url1));
                        break;

                    case 2:
                        // download two
                        downloadApk(getString(R.string.update_url2));
                        break;
                }
                dialog.dismiss();
            }
        });
        if (Utility.isPackageExisted(NewHome.this, mGooglePlayStorePackageName)) {
            adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                    .content(getString(R.string.google_play))
                    .icon(R.drawable.googleplay_icon)
                    .tag(0)
                    .build());
        }
        adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                .content(getString(R.string.download_one))
                .icon(R.drawable.download_icon)
                .tag(1)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                .content(getString(R.string.download_two))
                .icon(R.drawable.download_icon)
                .tag(2)
                .build());

        new MaterialDialog.Builder(NewHome.this)
                .title("請選擇更新方式")
                .adapter(adapter, null)
                .show();
    }

    private void downloadApk(String url) {
        this.downloadtask = new DownloadAsyncTask(this);
        this.downloadtask.execute(url);
    }

    public void openFCM() {
        this.mSharedPref.setNotificationStatus(true);
        this.mSharedPref.setAskOpenNotificationStatus(true);
    }

    // 顯示更新
    private void showNewFunctionUpdate() {
        boolean wrapInScrollView = false;
        this.mNotificationSwitchDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.widget_notification_switcher, wrapInScrollView)
                .title("新功能上線!!")
                .positiveText("完成")
                .onPositive(this)
                .showListener(this)
                .show();
    }

    private void processListener() {
        this.vMenuContent.init(this, this.vDrawerLayout);
    }

    private void processDrawerLayout() {
        setSupportActionBar(vToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
        this.mDrawerToggle = new ActionBarDrawerToggle(this, this.vDrawerLayout, this.vToolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        this.mDrawerToggle.setDrawerIndicatorEnabled(false);
        this.mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        this.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    vDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    vDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        this.mDrawerToggle.syncState();
        this.vDrawerLayout.addDrawerListener(this.mDrawerToggle);
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

                Intent intent = new Intent();
                intent.setClass(NewHome.this, SearchActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //open confirm dialog
        if (this.mCurrentFragment instanceof NewsCategoryFragment) {
            boolean isWebFragment = ((NewsCategoryFragment) this.mCurrentFragment).checkIsWebFragment();
            if (isWebFragment) {
                boolean canGoBack = ((NewsCategoryFragment) this.mCurrentFragment).checkIsWebFragmentCanGoBack();
                if (canGoBack) {
                    ((NewsCategoryFragment) this.mCurrentFragment).doWebFragmentGoBack();
                    return;
                }
            }
        }
        if (this.isNeedToLeave) {
            UserDataInfo.isVersionDialogShow = false;
            UserDataInfo.mHomeDFPCount = 0;
            if (Utility.DEBUG)
                Log.e(TAG, "UserDataInfo.mHomeDFPCount: " + UserDataInfo.mHomeDFPCount);
            super.onBackPressed();
        } else {
            openConfirmDialog();
        }
    }

    protected void openConfirmDialog() {
        boolean wrapInScrollView = false;
        this.mConfirmDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.widget_leave_confirm_dialog, wrapInScrollView)
                .title("確定離開NOWnews今日新聞?")
                .positiveText("我要留下來")
                .negativeText("離開")
                .onNegative(this)
                .showListener(this)
                .show();
    }

    private void processBottomNavigation() {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getString(R.string.news), R.drawable.news_static);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getString(R.string.special), R.drawable.spacial_static);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getString(R.string.video), R.drawable.video_static);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(getString(R.string.live), R.drawable.btn_live);
        this.vBottomNavigation.addItem(item1);
        this.vBottomNavigation.addItem(item2);
        this.vBottomNavigation.addItem(item3);
        this.vBottomNavigation.addItem(item4);
        this.vBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        this.vBottomNavigation.setAccentColor(Color.parseColor("#0083ff"));
        this.vBottomNavigation.setInactiveColor(Color.parseColor("#b0b0b0"));
        this.vBottomNavigation.setOnTabSelectedListener(this);
        this.vBottomNavigation.setCurrentItem(0);
    }

    private void showCsmuseIconOnToolBar() {
        this.vX.setVisibility(View.VISIBLE);
        this.vCsmuse.setVisibility(View.VISIBLE);
    }

    private void hideCsmuseIconOnToolBar() {
        this.vX.setVisibility(View.GONE);
        this.vCsmuse.setVisibility(View.GONE);
    }

    public void showSnackBar() {
        if (this.mSnackbar != null && this.mSnackbar.isShown()) {
            return;
        }
        if (this.mSnackbar == null) {
            this.mSnackbar = Snackbar.make(this.vCoordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) this.mSnackbar.getView();
            TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
            textView.setVisibility(View.INVISIBLE);
            textView.setMaxLines(0);

            LayoutInflater inflater = LayoutInflater.from(NewHome.this);
            View customSnackView = inflater.inflate(R.layout.widget_download_list_layout, null);
            layout.addView(customSnackView);
        }
        this.mSnackbar.show();
        if (Utility.DEBUG) Log.d(TAG, "mSnackbar SHOW");
    }

    public void dissmissSnackBar() {
        if (this.mSnackbar != null && this.mSnackbar.isShown()) {
            this.mSnackbar.dismiss();
        }
    }

//    public void switchFragment(Fragment fragment) {
//        if (this.mCurrentFragment == null) {
//            getSupportFragmentManager().beginTransaction().add(R.id.main_body, fragment).commit();
//        } else if (fragment != this.mCurrentFragment) {
//            if (!fragment.isAdded()) {
//                getSupportFragmentManager().beginTransaction().hide(this.mCurrentFragment)
//                        .add(R.id.main_body, fragment).commit();
//            } else {
//                getSupportFragmentManager().beginTransaction().hide(this.mCurrentFragment)
//                        .show(fragment).commit();
//            }
//        }
//        this.mCurrentFragment = fragment;
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (resultCode == RESULT_CODE) {
            showAllPageDFPAD();
        } else if (resultCode == RESULT_CODE_FROM_LIVE) {
            if (mCurrentFragment != null && mCurrentFragment instanceof LiveFragment) {
                ((LiveFragment) mCurrentFragment).openDownloadDialog(0);
            }
        } else if (requestCode == RESULT_CODE_FROM_LIVE_BAR) {
            /* 取得KMT直播資訊 */
            this.DoSync(new LiveInfoDao(this));
        } else if (requestCode == 0x789 && (resultCode == 3 || resultCode == -1)) {
            Log.e(TAG, "share success!!");
            this.mSharedPref.setShareAppSuccess(true);

            String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            if (ids != null) {
                Log.e(TAG, "ids.length: " + ids.length);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            }
        }
    }

    private void showAllPageDFPAD() {
        // 建立插頁式廣告
        this.mDFPInterstitial = new PublisherInterstitialAd(this);
        this.mDFPInterstitial.setAdUnitId(getString(R.string.dfp_interstitial));

        // 建立廣告請求
        PublisherAdRequest.Builder adRequest = new PublisherAdRequest.Builder();

        // 開始載入您的插頁式廣告
        this.mDFPInterstitial.loadAd(adRequest.build());
        // 設定「廣告接聽程式」，使其使用下方的回呼函式
        this.mDFPInterstitial.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (Utility.DEBUG) Log.e(TAG, "onAdClosed!!!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (Utility.DEBUG) Log.e(TAG, "onAdOpened!!!");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (Utility.DEBUG) Log.e(TAG, "onAdLeftApplication!!!");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (Utility.DEBUG) Log.e(TAG, "AD Loaded!!!");
                UserDataInfo.mHomeDFPCount++;
                if (Utility.DEBUG)
                    Log.e(TAG, "UserDataInfo.mHomeDFPCount: " + UserDataInfo.mHomeDFPCount);
                if (UserDataInfo.mHomeDFPCount == 2
                        || UserDataInfo.mHomeDFPCount == 4
                        || UserDataInfo.mHomeDFPCount == 6) {
                    if (mDFPInterstitial != null && mDFPInterstitial.isLoaded()) {
                        mDFPInterstitial.show();
                    }
                }
            }
        });
    }

    // 安裝apk
    protected void installAPK() {
        File file = new File(Constants.Download.Folder + Constants.Download.FileName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    private boolean checkVersion(CheckVersionJson data) {
        boolean result = false;
        int currentVersion = Utility.getAppVersionCode(this);
        if (currentVersion != 0 && currentVersion < Integer.parseInt(data.android_versioncode)) {
            result = true;
            //update
            getVersion(result, data);
        }
        this.vMenuContent.setHasNewVersion(result);
        return result;
    }

    public void getVersion(boolean isHasNewVersion, CheckVersionJson data) {
        int versionCode = Utility.getAppVersionCode(this);
        String currentVersionText = getString(R.string.current_version);
        currentVersionText = String.format(currentVersionText, versionCode);
        if (data != null
                && data.android_versioncode != null
                && !data.android_versioncode.trim().isEmpty()) {
            currentVersionText = currentVersionText + "\n" + String.format(getString(R.string.newest_version), data.android_versioncode);
        }
        if (isHasNewVersion) {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.update_dialog_title))
                    .content(currentVersionText + "\n" + getString(R.string.update_dialog_message))
                    .positiveText(getString(R.string.next_time))
                    .negativeText(getString(R.string.go_now))
                    .onPositive(this)
                    .onNegative(this)
                    .show();
            UserDataInfo.isVersionDialogShow = true;
        } else {
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.version))
                    .content(currentVersionText)
                    .negativeText(getString(R.string.ok))
                    .onNegative(this)
                    .show();
        }
    }
}

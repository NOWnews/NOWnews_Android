package com.nownews.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.GCM.GCMController;
import com.nownews.mobile.Json.LiveInfoJson;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Live.LiveFragment;
import com.nownews.mobile.NewsCategory.NewsCategoryFragment;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Search.SearchActivity;
import com.nownews.mobile.SpecialNewsCategory.SpecialNewsCategoryFragment;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.AppController;
import com.nownews.mobile.Json.CheckVersionJson;
import com.nownews.mobile.Service.FileDownloadService;
import com.nownews.mobile.VideoNewsCategory.VideoNewsCategoryFragment;
import com.nownews.mobile.Widget.LiveMarquee;
import com.nownews.mobile.Widget.MenuContent;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

/**
 * Created by cindy on 2016/11/1.
 */

public class NewHome extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private CoordinatorLayout vCoordinatorLayout;
    private AHBottomNavigation vBottomNavigation;
    private DrawerLayout vDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar vToolbar;
    private MenuContent vMenuContent;
    private TextView vX;
    private ImageView vCsmuse;
    private LinearLayout vLeftDrawer;
    private LiveMarquee vLiveMarquee;
    public final static int REQUEST_CODE = 0x123;
    public final static int RESULT_CODE = 0x321;
    public final static int RESULT_CODE_FROM_LIVE = 0x159;
    public final static int RESULT_CODE_FROM_LIVE_BAR = 0x357;
    private final static int SHOW_DFP_AD_PAGE = 0x951;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);

        if (Utility.DEBUG)Log.d(TAG, "in NewHome");
        initController();
        checkIsV3Version();
        //20170421 Cindy add checkIsAlreadyShowNotificationSetting()
        checkIsAlreadyShowNotificationSetting();
        processView();
        processListener();
        processDrawerLayout();
        startGCM();
        getLiveInfo();
        getVersionInfo();
        processBottomNavigation();

    }

    private void checkIsV3Version(){
        if(mSharedPref!=null){
            boolean isV3Version = mSharedPref.isV3Version();
            if(!isV3Version){
                mSharedPref.clearAllSharedPreferencesData();
                mSharedPref.setIsV3Version();
            }
        }
    }

    private void checkIsAlreadyShowNotificationSetting(){
        if(mSharedPref!=null){
            boolean isAlreadyShowNotificationSetting = mSharedPref.isAlreadyShowNotificationSetting();
            if(!isAlreadyShowNotificationSetting){
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

        if (Utility.DEBUG)Log.v(TAG, "@@@onResume()@@@");
        if (Utility.DEBUG)Log.i(TAG, "mCurrentCategoryPage: " + mCurrentCategoryPage);
        if(vBottomNavigation!=null){
            vBottomNavigation.setCurrentItem(mCurrentCategoryPage);
        }

        GoogleAnalyticsFunction.setScreenName(this, "首頁");
        UserDataInfo.activityResumed(this);

        if (isPause) {
            isPause = false;
        }

        if (UserDataInfo.mPageSwapCount > 0) {
            UserDataInfo.mPageSwapCount = 0;
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        UserDataInfo.activityPaused();
        isPause = true;
        if (mApiHandler != null) {
            if (mApiHandler.hasMessages(SHOW_DFP_AD_PAGE)) {
                mApiHandler.removeMessages(SHOW_DFP_AD_PAGE);
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        UserDataInfo.activityDestroy(this);
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(this);
        }
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(this);
        }
        if(mApiHandler!=null){
            mApiHandler.removeCallbacks(null);
            mApiHandler = null;
        }
        super.onDestroy();
    }

    private GCMController mGCMController;
    private void startGCM() {
        if (mGCMController == null) {
            mGCMController = new GCMController(this, mApiHandler);
        }
        mGCMController.startGCM();
        String FCMReistId = mSharedPref.getGcmRegistId();
        if(FCMReistId==null || FCMReistId.trim().isEmpty()){
            FCMReistId = mGCMController.getToken();
            Utility.processGCMRegisterId(getApplicationContext(), FCMReistId);
        }
        if(Utility.DEBUG)Log.i(TAG, "###FCMReistId: " + FCMReistId);
    }

    private LiveInfoJson mLiveInfo;
    private static class ApiHandler extends Handler {

        private final WeakReference<NewHome> mNewHome;

        public ApiHandler(NewHome aNewHome){
            mNewHome = new WeakReference<NewHome>(aNewHome);
        }

        @Override
        public void handleMessage(Message msg) {

            NewHome newHome = mNewHome.get();
            if(newHome==null){
                return;
            }

            switch (msg.what) {
                case ParameterSet.CHECK_VERSION_DONE:
                    newHome.mVersionInfo = (CheckVersionJson) msg.obj;
                    if (newHome.mVersionInfo != null) {
                        newHome.mCurrentVersion = newHome.mVersionInfo.android_versioncode;
                        if (newHome.mCurrentVersion != null) {
                            boolean isNeedToUpdate = newHome.checkVersion(Integer.parseInt(newHome.mCurrentVersion));
                            /**
                             * 若需要跳更新訊息則不顯示蓋版廣告，反之，顯示
                             * */
                            if (!isNeedToUpdate) {
                                newHome.showAllPageDFPAD();
                            }
                        }
                    }
                    break;
                case ParameterSet.CHECK_VERSION_FAILED:
                    newHome.showAllPageDFPAD();
                    break;

                //KMT Live
                case ParameterSet.GET_LIVE_INFO_DONE:
                    newHome.mLiveInfo = (LiveInfoJson)msg.obj;
                    if(newHome.mLiveInfo==null
                            || !newHome.mLiveInfo.isCampainStatus()
                            || newHome.mLiveInfo.getTitle()==null
                            || newHome.mLiveInfo.getTitle().trim().isEmpty()){
                        newHome.vLiveMarquee.setVisibility(View.GONE);
                    }else{
                        newHome.vLiveMarquee.setVisibility(View.VISIBLE);
                        newHome.vLiveMarquee.setBackgroundUrl(newHome.mLiveInfo.getBackground());
                        newHome.vLiveMarquee.setIsOnAir(newHome.mLiveInfo.isIsOnAir());
                        newHome.vLiveMarquee.setLiveTitle(newHome.mLiveInfo.getTitle());
                        newHome.vLiveMarquee.setLiveUrl(newHome.mLiveInfo.getLivePage());
                    }
                    break;
                case ParameterSet.GET_LIVE_INFO_FAILED:
                    newHome.vLiveMarquee.setVisibility(View.GONE);
                    break;

                case FileDownloadService.FILE_DOWNLOAD_PERSENTAGE:
                    if (msg.arg2 > 0) {
                        long persentage = msg.arg1 * 100L / msg.arg2;
                        newHome.showProgressDialog((int) persentage);
                        if (persentage == 100) {
                            newHome.vDownloadProgressDialog.dismiss();
                            newHome.vDownloadProgressDialog = null;
                            newHome.installAPK((String) msg.obj);
                        }
                    }
                    break;
                case GCMController.SHOW_NEW_FUNCTION:
                    newHome.openFCM();
//                    showNewFunctionUpdate();
                    break;

                case SHOW_DFP_AD_PAGE:
                    UserDataInfo.mHomeDFPCount++;
                    if (Utility.DEBUG)
                        Log.e(newHome.TAG, "UserDataInfo.mHomeDFPCount: " + UserDataInfo.mHomeDFPCount);
                    if (UserDataInfo.mHomeDFPCount == 2
                            || UserDataInfo.mHomeDFPCount == 4
                            || UserDataInfo.mHomeDFPCount == 6) {
                        if (newHome.mDFPInterstitial != null && newHome.mDFPInterstitial.isLoaded()) {
                            newHome.mDFPInterstitial.show();
                        }
                    }
                    break;

            }

        }

    };

    private void openFCM(){
        mSharedPref.setNotificationStatus(true);
        mSharedPref.setAskOpenNotificationStatus(true);
    }

    private MaterialDialog mNotificationSwitchDialog;
    private boolean isNotificationOpen;
    private void showNewFunctionUpdate() {
        boolean wrapInScrollView = false;
        mNotificationSwitchDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.widget_notification_switcher, wrapInScrollView)
                .title("新功能上線!!")
                .positiveText("完成")
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mSharedPref.setNotificationStatus(isNotificationOpen);
                        mSharedPref.setAskOpenNotificationStatus(true);
                        startGCM();
                        super.onPositive(dialog);
                    }

                })
                .showListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        View view = mNotificationSwitchDialog.getCustomView();
                        TextView message = (TextView) view.findViewById(R.id.message);
                        message.setText("期待已久的新聞推播功能來啦！\n請選擇您要開啟或關閉新聞推播通知");
                        Switch switcher = (Switch) view.findViewById(R.id.switcher);
                        isNotificationOpen = mSharedPref.getNotificationStatus();
                        switcher.setChecked(isNotificationOpen);
                        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                isNotificationOpen = isChecked;
                            }
                        });

                    }
                })
                .show();
    }

    private ApiController mApiController;
    private SharedPreferencesMethods mSharedPref;
    private ApiHandler mApiHandler;
    private void initController(){
        mSharedPref = new SharedPreferencesMethods(this);
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler(this);
        mBitmapController = BitmapController.getInstance(this);
        mResize = new ReSizeLayoutParams(this);
    }

    private ReSizeLayoutParams mResize;
    private void processView(){

        vCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator_layout);
        vBottomNavigation = (AHBottomNavigation)findViewById(R.id.bottom_navigation);
        vDrawerLayout = (DrawerLayout) findViewById(R.id.drw_layout);
        vToolbar = (Toolbar) findViewById(R.id.tool_bar);
        vMenuContent = (MenuContent) findViewById(R.id.menu_content);
        vX = (TextView) findViewById(R.id.x);
        vCsmuse = (ImageView) findViewById(R.id.csmuse_logo);
        vLeftDrawer = (LinearLayout) findViewById(R.id.llv_left_drawer);
        vLeftDrawer.setLayoutParams(mResize.setOnSize(vLeftDrawer));
        vLiveMarquee = (LiveMarquee)findViewById(R.id.live_marquee);

        mResize.unregisterCallback(this);

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

        if (vDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            vDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if(mCurrentFragment instanceof NewsCategoryFragment){
            boolean isWebFragment = ((NewsCategoryFragment)mCurrentFragment).checkIsWebFragment();
            if(isWebFragment){
                boolean canGoBack = ((NewsCategoryFragment)mCurrentFragment).checkIsWebFragmentCanGoBack();
                if(canGoBack){
                    ((NewsCategoryFragment)mCurrentFragment).doWebFragmentGoBack();
                    return;
                }
            }
        }

        if(isNeedToLeave){
            UserDataInfo.isVersionDialogShow = false;
            UserDataInfo.mHomeDFPCount = 0;
            if(Utility.DEBUG)Log.e(TAG, "UserDataInfo.mHomeDFPCount: " + UserDataInfo.mHomeDFPCount);
            super.onBackPressed();
        }else{
            openConfirmDialog();
        }
    }

    private BitmapController mBitmapController;
    private boolean isNeedToLeave = false;
    private MaterialDialog mConfirmDialog;
    private void openConfirmDialog() {
        boolean wrapInScrollView = false;
        mConfirmDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.widget_leave_confirm_dialog, wrapInScrollView)
                .title("確定離開NOWnews今日新聞?")
                .positiveText("我要留下來")
                .negativeText("離開")
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        isNeedToLeave = true;
                        onBackPressed();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }

                })
                .showListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        View view = mConfirmDialog.getCustomView();
                        ImageView image = (ImageView)view.findViewById(R.id.image);
                        TextView title = (TextView)view.findViewById(R.id.title);
                        RelativeLayout newsLayout = (RelativeLayout)view.findViewById(R.id.news_layout);
                        final List<NewsListJson.NewsContent> hotNewsList = UserDataInfo.getHotNewsContent();
                        if(hotNewsList!=null && hotNewsList.size()>0){
                            Random random = new Random();
                            final int index = random.nextInt(hotNewsList.size());
                            if(hotNewsList.get(index)!=null
                                    && hotNewsList.get(index).image!=null
                                    && hotNewsList.get(index).image.thumbnail!=null
                                    && hotNewsList.get(index).field_short_title!=null
                                    && hotNewsList.get(index).field_short_title.value!=null){
                                String imageUrl = hotNewsList.get(index).image.thumbnail;
                                if(imageUrl!=null){
                                    mBitmapController.loadImageWithOriginalSize(imageUrl, image, BitmapController.IMAGE_SRC, 0, 0, null);
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
                            }else{
                                newsLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .show();
    }

    private void processBottomNavigation(){

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getString(R.string.news), R.drawable.news_static);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getString(R.string.special), R.drawable.spacial_static);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getString(R.string.video), R.drawable.video_static);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(getString(R.string.live), R.drawable.btn_live);
        vBottomNavigation.addItem(item1);
        vBottomNavigation.addItem(item2);
        vBottomNavigation.addItem(item3);
        vBottomNavigation.addItem(item4);
        vBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        vBottomNavigation.setAccentColor(Color.parseColor("#0083ff"));
        vBottomNavigation.setInactiveColor(Color.parseColor("#b0b0b0"));
        vBottomNavigation.setOnTabSelectedListener(mBottomTabSelectListener);
        vBottomNavigation.setCurrentItem(0);

    }

    private int mCurrentCategoryPage = -1;
    private NewsCategoryFragment mNewsCategoryFragment;
    private SpecialNewsCategoryFragment mSpecialNewsCategoryFragment;
    private VideoNewsCategoryFragment mVideoNewsCategoryFragment;
    private LiveFragment mLiveFragment;
    private Fragment mCurrentFragment;
    private AHBottomNavigation.OnTabSelectedListener mBottomTabSelectListener = new AHBottomNavigation.OnTabSelectedListener(){

        @Override
        public boolean onTabSelected(int position, boolean wasSelected) {

            //wasSelected=>是否正被選中!=isFocused不要搞混了
            if(mCurrentCategoryPage == position){
                return false;
            }
            switch(position) {
                case 0:
                    if (Utility.DEBUG)Log.w(TAG, "Show 新聞");
                    if(mNewsCategoryFragment==null){
                        mNewsCategoryFragment = new NewsCategoryFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(NewsCategoryFragment.KEY_POSITION, 0);
                        mNewsCategoryFragment.setArguments(bundle);
                    }else{
                        if(!mNewsCategoryFragment.isApiLoadingSuccess){
                            mNewsCategoryFragment.reload();
                        }
                    }
                    hideCsmuseIconOnToolBar();
                    switchFragment(mNewsCategoryFragment);
                    mCurrentCategoryPage = position;
                    break;
                case 1:
                    if (Utility.DEBUG)Log.w(TAG, "Show 特輯");
                    if(mSpecialNewsCategoryFragment==null){
                        mSpecialNewsCategoryFragment = new SpecialNewsCategoryFragment();
                    }
                    hideCsmuseIconOnToolBar();
                    switchFragment(mSpecialNewsCategoryFragment);
                    mCurrentCategoryPage = position;
                    break;
                case 2:
                    if (Utility.DEBUG)Log.w(TAG, "Show 影音");
                    if(mVideoNewsCategoryFragment==null){
                        mVideoNewsCategoryFragment = new VideoNewsCategoryFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(NewsCategoryFragment.KEY_POSITION, 0);
                        mVideoNewsCategoryFragment.setArguments(bundle);
                    }else{
                        if(!mVideoNewsCategoryFragment.isApiLoadingSuccess){
                            mVideoNewsCategoryFragment.reload();
                        }
                    }
                    hideCsmuseIconOnToolBar();
                    switchFragment(mVideoNewsCategoryFragment);
                    mCurrentCategoryPage = position;
                    break;
                case 3:
                    if (Utility.DEBUG)Log.w(TAG, "Show 直播");
                    if(mLiveFragment==null){
                        mLiveFragment = new LiveFragment();
                    }else{
                        if(!mLiveFragment.isApiLoadingSuccess){
                            mLiveFragment.reload();
                        }
                    }
//                    showCsmuseIconOnToolBar();
                    switchFragment(mLiveFragment);
                    mCurrentCategoryPage = position;
                    break;
            }

            return true;
        }
    };

    private void showCsmuseIconOnToolBar(){
        vX.setVisibility(View.VISIBLE);
        vCsmuse.setVisibility(View.VISIBLE);
    }

    private void hideCsmuseIconOnToolBar(){
        vX.setVisibility(View.GONE);
        vCsmuse.setVisibility(View.GONE);
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

            LayoutInflater inflater = LayoutInflater.from(NewHome.this);
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

    private void switchFragment(Fragment fragment) {

        if (mCurrentFragment ==null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_body, fragment).commit();
        } else if (fragment != mCurrentFragment) {
            if (!fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().hide(mCurrentFragment)
                        .add(R.id.main_body, fragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().hide(mCurrentFragment)
                        .show(fragment).commit();
            }
        }
        mCurrentFragment = fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (resultCode == RESULT_CODE) {
            showAllPageDFPAD();
        }else if(resultCode == RESULT_CODE_FROM_LIVE){
            if(mCurrentFragment!=null && mCurrentFragment instanceof LiveFragment){
                ((LiveFragment) mCurrentFragment).openDownloadDialog(0);
            }
        }else if(resultCode == RESULT_CODE_FROM_LIVE_BAR){
            getLiveInfo();
        }else if(requestCode == 0x789 && (resultCode == 3 || resultCode == -1)){
            Log.e(TAG, "share success!!");
            mSharedPref.setShareAppSuccess(true);

            String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            if(ids!=null){
                Log.e(TAG, "ids.length: " + ids.length);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            }

        }

    }

    public void getLiveInfo() {
        if (mApiController != null) {
            mApiController.getLiveInfo(mApiHandler);
        }
    }

    public void getVersionInfo() {
        if (!UserDataInfo.isVersionDialogShow) {
            if (mApiController != null) {
                mApiController.checkVersion(mApiHandler);
            }
        }
    }

    private CheckVersionJson mVersionInfo;
    private String mCurrentVersion;
    private PublisherInterstitialAd mDFPInterstitial;
    private boolean isPause = false;
    private void showAllPageDFPAD() {

        // 建立插頁式廣告
        mDFPInterstitial = new PublisherInterstitialAd(this);
        mDFPInterstitial.setAdUnitId(getString(R.string.dfp_interstitial));

        // 建立廣告請求
        PublisherAdRequest.Builder adRequest = new PublisherAdRequest.Builder();

        // 開始載入您的插頁式廣告
        mDFPInterstitial.loadAd(adRequest.build());
        // 設定「廣告接聽程式」，使其使用下方的回呼函式
        mDFPInterstitial.setAdListener(new AdListener() {

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
                if (mApiHandler != null && !isPause) {
                    mApiHandler.sendEmptyMessage(SHOW_DFP_AD_PAGE);
                }
            }

        });
    }

    protected void installAPK(String aApkName) {

        String filePath = mNownewsApkFolder + aApkName;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private MaterialDialog vDownloadProgressDialog;
    private void showProgressDialog(int progress) {

        if (vDownloadProgressDialog == null) {
            vDownloadProgressDialog = new MaterialDialog.Builder(NewHome.this)
                    .title(R.string.downloading)
                    .progress(false, 100, true)
                    .cancelable(false).show();
        }
        vDownloadProgressDialog.setProgress(progress);
    }

    private boolean checkVersion(int aVersionOnServer) {
        int currentVersion = Utility.getAppVersionCode(this);
        if (currentVersion != 0 && currentVersion < aVersionOnServer) {
            //update
            getVersion(true);
            return true;
        }
        return false;
    }

    public void getVersion(boolean isHasNewVersion) {

        int versionCode = Utility.getAppVersionCode(this);
        String currentVersionText = getString(R.string.current_version);
        currentVersionText = String.format(currentVersionText, versionCode);
        if (mVersionInfo != null && mVersionInfo.android_versioncode != null && !mVersionInfo.android_versioncode.trim().isEmpty()) {
            currentVersionText = currentVersionText + "\n" + String.format(getString(R.string.newest_version), mVersionInfo.android_versioncode);
        }
        if (isHasNewVersion) {

            new MaterialDialog.Builder(this)
                    .title(getString(R.string.update_dialog_title))
                    .content(currentVersionText + "\n" + getString(R.string.update_dialog_message))
                    .positiveText(getString(R.string.next_time))
                    .negativeText(getString(R.string.go_now))
                    .onPositive(mNextTimeClickListener)
                    .onNegative(mGoUpdateClickListener)
                    .show();
            UserDataInfo.isVersionDialogShow = true;

        } else {

            new MaterialDialog.Builder(this)
                    .title(getString(R.string.version))
                    .content(currentVersionText)
                    .negativeText(getString(R.string.ok))
                    .onNegative(mOkClickListener)
                    .show();

        }

    }

    private MaterialDialog.SingleButtonCallback mOkClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //dismiss
        }
    };

    private MaterialDialog.SingleButtonCallback mNextTimeClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //dismiss
        }
    };

    private String mNownewsApkFolder = Environment.getExternalStorageDirectory() + "/Nownews/";
    private String mGooglePlayStorePackageName = "com.android.vending";
    private MaterialDialog.SingleButtonCallback mGoUpdateClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

            final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                @Override
                public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {

                    if (item.getContent().toString().equals(getString(R.string.google_play))) {
                        Intent MyIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.nownews"));
                        startActivity(MyIntent);
                    } else if (item.getContent().toString().equals(getString(R.string.download_one))) {
                        //download
                        String fileUrl = "http://210.242.196.110/NowNews_Mobile.apk";
                        String fileName = "NownewsApp.apk";
                        File folder = new File(mNownewsApkFolder);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        AppController appController = AppController.getInstance(NewHome.this);
                        appController.downloadFileFromUrl(fileUrl, fileName, mNownewsApkFolder, mApiHandler);
                    } else if (item.getContent().toString().equals(getString(R.string.download_two))) {
                        //download
                        String fileUrl = "http://www.megamediatech.com/NowNews_Mobile.apk";
                        String fileName = "NownewsApp.apk";
                        File folder = new File(mNownewsApkFolder);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        AppController appController = AppController.getInstance(NewHome.this);
                        appController.downloadFileFromUrl(fileUrl, fileName, mNownewsApkFolder, mApiHandler);
                    }
                    dialog.dismiss();

                }
            });
            if (Utility.isPackageExisted(NewHome.this, mGooglePlayStorePackageName)) {
                adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                        .content(getString(R.string.google_play))
                        .icon(R.drawable.googleplay_icon)
                        .build());
            }
            adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                    .content(getString(R.string.download_one))
                    .icon(R.drawable.download_icon)
                    .build());
            adapter.add(new MaterialSimpleListItem.Builder(NewHome.this)
                    .content(getString(R.string.download_two))
                    .icon(R.drawable.download_icon)
                    .build());

            new MaterialDialog.Builder(NewHome.this)
                    .title("請選擇更新方式")
                    .adapter(adapter, null)
                    .show();


        }
    };

}

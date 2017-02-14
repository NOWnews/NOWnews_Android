package com.nownews.mobile.Widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.nownews.R;
import com.nownews.mobile.AlbumCategory.AlbumActivity;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbum;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.AppController;
import com.nownews.mobile.Json.CheckVersionJson;
import com.nownews.mobile.Json.NewsCategoryJson.CategoryInfo;
import com.nownews.mobile.Service.FileDownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuContent extends RelativeLayout {

    public static final String KEY_ICON = "icon";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CATEGORY = "category";
    public static final int MENU_GO_HOME = 0x001;
    public static final int MENU_TO_NEWS = 0x002;
    public static final int MENU_TO_ALBUM = 0x003;
    public static final int MENU_TO_FAVORITE_ALBUM = 0x004;
    public static final int MENU_TO_VERSION = 0x005;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private ListView vListView;
    private DrawerLayout vDrawerLayout;
    private ArrayList<HashMap<String, Object>> mMenuList;
    private MenuContentAdapter mMenuContentAdapter;
    private ApiController mApiController;
    private boolean isHasNewVersion;
    private boolean isNShoppingMenuOpen;
    private SharedPreferencesMethods mSharedPref;
    private boolean isNotificationOpen;
    private MaterialDialog mNotificationSwitchDialog;
    private MaterialDialog.SingleButtonCallback mOkClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //dismiss
        }
    };
    private String mNownewsApkFolder = Environment.getExternalStorageDirectory() + "/Nownews/";
    private String mGooglePlayStorePackageName = "com.android.vending";
    private MaterialDialog.SingleButtonCallback mNextTimeClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //dismiss
            GoogleAnalyticsFunction.sendHitInfo(mContext, "版本資訊", "點擊下次再說", "");
        }
    };
    private MaterialDialog vDownloadProgressDialog;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case FileDownloadService.FILE_DOWNLOAD_PERSENTAGE:
                    long persentage = msg.arg1 * 100L / msg.arg2;
                    showProgressDialog((int) persentage);
                    if (persentage == 100) {
                        vDownloadProgressDialog.dismiss();
                        vDownloadProgressDialog = null;
                        installAPK((String) msg.obj);
                    }
                    break;

            }

        }

    };
    private MaterialDialog.SingleButtonCallback mGoUpdateClickListener = new MaterialDialog.SingleButtonCallback() {

        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

            GoogleAnalyticsFunction.sendHitInfo(mContext, "版本資訊", "點擊立即前往", "");
            final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                @Override
                public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {

                    if (item.getContent().toString().equals(mContext.getString(R.string.google_play))) {
                        Intent MyIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.nownews"));
                        mContext.startActivity(MyIntent);
                        GoogleAnalyticsFunction.sendHitInfo(mContext, "更新方式", mContext.getString(R.string.google_play), "");
                    } else if (item.getContent().toString().equals(mContext.getString(R.string.download_one))) {
                        //download
                        String fileUrl = "http://210.242.196.110/NowNews_Mobile.apk";
                        String fileName = "NownewsApp.apk";
                        File folder = new File(mNownewsApkFolder);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        AppController appController = AppController.getInstance(mContext);
                        appController.downloadFileFromUrl(fileUrl, fileName, mNownewsApkFolder, mHandler);
                        GoogleAnalyticsFunction.sendHitInfo(mContext, "更新方式", mContext.getString(R.string.download_one), "");
                    } else if (item.getContent().toString().equals(mContext.getString(R.string.download_two))) {
                        //download
                        String fileUrl = "http://legacy.nownews.com/events/adtips/mobile_app/NowNews_Mobile.apk";
                        String fileName = "NownewsApp.apk";
                        File folder = new File(mNownewsApkFolder);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        AppController appController = AppController.getInstance(mContext);
                        appController.downloadFileFromUrl(fileUrl, fileName, mNownewsApkFolder, mHandler);
                        GoogleAnalyticsFunction.sendHitInfo(mContext, "更新方式", mContext.getString(R.string.download_two), "");
                    }
                    dialog.dismiss();

                }
            });
            if (Utility.isPackageExisted(mContext, mGooglePlayStorePackageName)) {
                adapter.add(new MaterialSimpleListItem.Builder(mContext)
                        .content(mContext.getString(R.string.google_play))
                        .icon(R.drawable.googleplay_icon)
                        .build());
            }
            adapter.add(new MaterialSimpleListItem.Builder(mContext)
                    .content(mContext.getString(R.string.download_one))
                    .icon(R.drawable.download_icon)
                    .build());
            adapter.add(new MaterialSimpleListItem.Builder(mContext)
                    .content(mContext.getString(R.string.download_two))
                    .icon(R.drawable.download_icon)
                    .build());

            new MaterialDialog.Builder(mContext)
                    .title("請選擇更新方式")
                    .adapter(adapter, null)
                    .show();


        }
    };
    private List<CategoryInfo> mCategoryContent;
    private CheckVersionJson mVersionInfo;
    private String mCurrentVersion;
    private Handler mMenuHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.CHECK_VERSION_DONE:
                    mVersionInfo = (CheckVersionJson) msg.obj;
                    if (mVersionInfo != null) {
                        mCurrentVersion = mVersionInfo.android_versioncode;
                        if (mCurrentVersion != null) {
                            checkVersion(Integer.parseInt(mCurrentVersion));
                        }
                    }
                    break;
                case ParameterSet.CHECK_VERSION_FAILED:
                    break;

                case ParameterSet.GET_NEWS_CATEGORY_DONE:
                    mCategoryContent = (List<CategoryInfo>) msg.obj;
                    UserDataInfo.setNewsCategoryContent(mCategoryContent);
                    break;
                case ParameterSet.GET_NEWS_CATEGORY_FAILED:
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(mContext);
                    break;
            }

        }

    };
    private OnItemClickListener mListItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);
            String itemName = (String) map.get(MenuContent.KEY_TITLE);
            String category = (String) map.get(MenuContent.KEY_CATEGORY);

            if (itemName != null && itemName.equals(mContext.getString(R.string.return_home))) {
                //要判斷目前是不是首頁
                if (mContext instanceof NewHome) {
                    //do nothing
                } else {
                    GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                    goHome();
                }
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.album))) {
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                gotoAlbum();
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.favorite_album))) {
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                gotoFavoriteAlbum();
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.version))) {
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                getVersion(isHasNewVersion);
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.category_edit))) {
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                openNewsPreference();
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.notification_setting))) {
//				Toast.makeText(mContext, "notification_setting", Toast.LENGTH_LONG).show();
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                showNotificationSwicherDialog();
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.about))) {
//				Toast.makeText(mContext, "notification_setting", Toast.LENGTH_LONG).show();
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                String url = "http://m.nownews.com/about";
                Intent intent = new Intent();
                intent.setClass(mContext, WebActivity.class);
                intent.putExtra(WebActivity.KEY_URL, url);
                mContext.startActivity(intent);
                vDrawerLayout.closeDrawers();
            } else if (itemName != null && itemName.equals(mContext.getString(R.string.nowvote))) {
//				Toast.makeText(mContext, "notification_setting", Toast.LENGTH_LONG).show();
                GoogleAnalyticsFunction.sendHitInfo(mContext, "Menu", "點擊" + itemName, "");
                String url = "http://vote.nownews.com/";
                Intent intent = new Intent();
                intent.setClass(mContext, WebActivity.class);
                intent.putExtra(WebActivity.KEY_URL, url);
                mContext.startActivity(intent);
                vDrawerLayout.closeDrawers();
            }

        }
    };

    public MenuContent(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void init(Context aContext, DrawerLayout aDrawerLayout) {
        mContext = aContext;
        vDrawerLayout = aDrawerLayout;
        mSharedPref = new SharedPreferencesMethods(mContext);
        initController();
        processView();
        processList();
    }

    public void getVersionInfo() {
        if (mApiController != null) {
            mApiController.checkVersion(mMenuHandler);
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
    }

    private void processList() {
        mMenuList = new ArrayList<HashMap<String, Object>>();
        String[] listContent = getResources().getStringArray(R.array.menu_list);
        for (int i = 0; i < listContent.length; i++) {
            String item = listContent[i];
            HashMap<String, Object> map = new HashMap<String, Object>();
            int iconId = 0;
            if (item.equals(getResources().getString(R.string.return_home))) {
                iconId = R.drawable.menu_home;
            } else if (item.equals(getResources().getString(R.string.album))) {
                iconId = R.drawable.photos;
            } else if (item.equals(getResources().getString(R.string.favorite_album))) {
                iconId = R.drawable.love;
            } else if (item.equals(getResources().getString(R.string.nowvote))) {
                iconId = R.drawable.vote;
            }
//			else if(item.equals(getResources().getString(R.string.my_bookmark))){
//				iconId = R.drawable.booklet;
//			}else if(item.equals(getResources().getString(R.string.view_history))){
//				iconId = R.drawable.eye;
//			}
//			else if(item.equals(getResources().getString(R.string.broke_the_news))){
//				iconId = R.drawable.megaphone2;
//			}
//            else if (item.equals(getResources().getString(R.string.category_edit))) {
//                iconId = R.drawable.check;
//            }
            else if (item.equals(getResources().getString(R.string.version))) {
                iconId = R.drawable.version;
            } else if (item.equals(getResources().getString(R.string.notification_setting))) {
                iconId = R.drawable.notification;
            } else if (item.equals(getResources().getString(R.string.about))) {
                iconId = R.drawable.now;
            }
//			else if(item.equals(getResources().getString(R.string.theme))){
//				iconId = R.drawable.brightness;
//			}else if(item.equals(getResources().getString(R.string.clear_cache))){
//				iconId = R.drawable.recycle;
//			}
            map.put(KEY_TITLE, item);
            map.put(KEY_ICON, iconId);
            mMenuList.add(map);
        }
        if (mMenuContentAdapter == null) {
            mMenuContentAdapter = new MenuContentAdapter(mContext, mMenuList);
            vListView.setAdapter(mMenuContentAdapter);
            vListView.setOnItemClickListener(mListItemClickListener);
        } else {
            mMenuContentAdapter.setData(mMenuList);
        }
    }

    private void processView() {
        vListView = (ListView) findViewById(R.id.list);
    }

    private void goHome() {
        ((Activity) mContext).setResult(NewHome.RESULT_CODE);
        ((Activity) mContext).finish();
    }

    private void showNotificationSwicherDialog() {
        boolean wrapInScrollView = false;
        mNotificationSwitchDialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.widget_notification_switcher, wrapInScrollView)
                .title("請選擇您要開啟或關閉推播通知")
                .positiveText("完成")
                .callback(new ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mSharedPref.setNotificationStatus(isNotificationOpen);
                        mMenuContentAdapter.notifyDataSetChanged();
                        super.onPositive(dialog);
                    }

                })
                .showListener(new OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        View view = mNotificationSwitchDialog.getCustomView();
                        TextView message = (TextView) view.findViewById(R.id.message);
                        message.setVisibility(View.GONE);
                        Switch switcher = (Switch) view.findViewById(R.id.switcher);
                        isNotificationOpen = mSharedPref.getNotificationStatus();
                        switcher.setChecked(isNotificationOpen);
                        switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                isNotificationOpen = isChecked;
                            }
                        });

                    }
                })
                .show();
    }

    private void gotoAlbum(){
        Intent intent = new Intent();
        intent.setClass(mContext, AlbumActivity.class);
        mContext.startActivity(intent);
    }

    private void gotoFavoriteAlbum() {
        Intent intent = new Intent();
        intent.setClass(mContext, FavoriteAlbum.class);
        mContext.startActivity(intent);
    }

    private void openNewsPreference() {

//        Intent intent = new Intent();
//        intent.setClass(mContext, HomeGridPreferenceActivity.class);
//        if (mContext instanceof VideoNewsCategoryFragment) {
//            if (Utility.DEBUG) Log.e(TAG, "VideoNewsCategoryFragment!!!");
//            intent.putExtra(HomeGridPreferenceActivity.KEY_FROM, HomeGridPreferenceActivity.FROM_NEWS_CATEGORY);
//            ((Activity) mContext).startActivity(intent);
//            ((Activity) mContext).finish();
//        } else if (mContext instanceof AlbumCategoryFragment) {
//            if (Utility.DEBUG) Log.e(TAG, "AlbumCategoryFragment!!!");
//            intent.putExtra(HomeGridPreferenceActivity.KEY_FROM, HomeGridPreferenceActivity.FROM_ALBUM_CATEGORY);
//            ((Activity) mContext).startActivity(intent);
//            ((Activity) mContext).finish();
//        } else {
//            ((Activity) mContext).startActivityForResult(intent, Home.REQUEST_CODE);
//        }

    }

    private void getVersion(boolean isHasNewVersion) {

        int versionCode = Utility.getAppVersionCode(mContext);
        String currentVersionText = mContext.getString(R.string.current_version);
        currentVersionText = String.format(currentVersionText, versionCode);
        if (mVersionInfo != null && mVersionInfo.android_versioncode != null && !mVersionInfo.android_versioncode.trim().isEmpty()) {
            currentVersionText = currentVersionText + "\n" + String.format(mContext.getString(R.string.newest_version), mVersionInfo.android_versioncode);
        }
        if (isHasNewVersion) {

            GoogleAnalyticsFunction.sendHitInfo(mContext, "版本資訊", "有更新", "");
            new MaterialDialog.Builder(mContext)
                    .title(mContext.getString(R.string.update_dialog_title))
                    .content(currentVersionText + "\n" + mContext.getString(R.string.update_dialog_message))
                    .positiveText(mContext.getString(R.string.next_time))
                    .negativeText(mContext.getString(R.string.go_now))
                    .onPositive(mNextTimeClickListener)
                    .onNegative(mGoUpdateClickListener)
                    .show();
            UserDataInfo.isVersionDialogShow = true;

        } else {

            new MaterialDialog.Builder(mContext)
                    .title(mContext.getString(R.string.version))
                    .content(currentVersionText)
                    .negativeText(mContext.getString(R.string.ok))
                    .onNegative(mOkClickListener)
                    .show();

        }

    }

    private void showProgressDialog(int progress) {

        if (vDownloadProgressDialog == null) {
            vDownloadProgressDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.downloading)
                    .progress(false, 100, true)
                    .cancelable(false).show();
        }
        vDownloadProgressDialog.setProgress(progress);
    }

    protected void installAPK(String aApkName) {

        String filePath = mNownewsApkFolder + aApkName;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private void checkVersion(int aVersionOnServer) {
        int currentVersion = Utility.getAppVersionCode(mContext);
        if (currentVersion < aVersionOnServer) {
            //update
            isHasNewVersion = true;
        } else {
            isHasNewVersion = false;
        }
        mMenuContentAdapter.setVersionStatus(isHasNewVersion);
    }

}

package com.nownews.mobile.Common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.nownews.R;
import com.nownews.mobile.AlbumCategory.AlbumsCategoryColor;
import com.nownews.mobile.AlbumPage.AlbumPage;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Api.WebApi;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbum;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbumPage;
import com.nownews.mobile.NewsCategory.NewsCategoryColor;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Search.SearchActivity;
import com.nownews.mobile.Service.NetworkType;
import com.nownews.mobile.Splash.SplashActivity;
import com.nownews.mobile.VideoNewsCategory.VideoNewsCategoryColor;
import com.nownews.mobile.VideoNewsPage.VideoNewsPage;
import com.nownews.mobile.Widget.WebActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class Utility {

    public final static int IMG_QUALITY = 50;
    public final static boolean DEBUG = false;
    public final static boolean SAVE_JSON = false;
    //    private static DisplayImageOptions options;
    public static String mIPAddress = "";
    //TODO shareToWeChat()
    public static boolean isNeedToShowWeChatDialog = false;
    public static MaterialDialog mWeChatShareDialog;
    //TODO openNetworkErrorDialog()
    public static Dialog mNetworkErrordialog;
    //TODO openSocketTimeoutDialog()
    public static Dialog mSocketTimoutDialog;
    public static boolean isNetworkSlow = false;
    public static boolean isUserKnowNetworkSlow = false;
    public static boolean isVponTestMode = false;
    private static Context mAppilicationContext;
    public static int NEWS_LIST_LIMIT_COUNT = 30;

    //TODO getOptions()
//    public static DisplayImageOptions getOptions(){
//    	if(Utility.DEBUG)Log.e(TAG, "getOptions no cacheOnDisk");
//    	if(options==null){
//    		options = new DisplayImageOptions.Builder()
//    		.showImageForEmptyUri(R.drawable.default_img)
//    		.showImageOnFail(R.drawable.default_img)
//    		.imageScaleType(ImageScaleType.EXACTLY)
//    		.cacheInMemory(true)
//			.cacheOnDisk(true)
//			.considerExifParams(true)
//			.bitmapConfig(Bitmap.Config.RGB_565)
////			.displayer(new FadeInBitmapDisplayer(500))//是否图片加载好后渐入的动画时间
////			.resetViewBeforeLoading(true)
//			.build();
//    	}
//    	return options;
//    }
    private static String TAG = "Utility";
    private static ExecutorService mExecutorServiceForShow;
    private static float mDensity;
    private static int mScreenWidth;
    private static int mScreenHeight;
    private static int mScreenDPWidth;
    private static int mScreenDPHeight;
//    private static IWeiboShareAPI mWeiboShareAPI;
    private static boolean isBadSignal;

    public static void setApplicationContext(Context aApplicationContext){
        mAppilicationContext = aApplicationContext;
    }

    public static Context getApplicationContext(){
        return mAppilicationContext;
    }

    public static String processGCMRegisterId(Context aContext, String aNewToken) {
        if (Utility.DEBUG) Log.v(TAG, "@@@ aNewToken: " + aNewToken);
        sendRegisterId(aContext, aNewToken);
        return aNewToken;
    }

    //傳送推播ID給伺服器
    public static void storeRegistrationId(Context context, String aRegistId) {
        SharedPreferencesMethods sharePref = new SharedPreferencesMethods(context);
        int currentVerstionCode = Utility.getAppVersionCode(context);
        sharePref.setGcmRegistId(aRegistId);
        sharePref.saveLastVersion(currentVerstionCode);
        sharePref.unRegistContext(context);
    }

    //通知GCM推播ID使用
    private static void sendRegisterId(Context aContext, String regId) {
        Log.v(TAG, "sendRegisterId()");
        String BuildSERIAL = android.os.Build.SERIAL;
        Log.v("Utility", "BuildSERIAL:" + BuildSERIAL);
        ApiController apiController = ApiController.getInstance();
        apiController.sendFCMRegisterID(aContext, regId, BuildSERIAL);
    }

    //TODO writeJsonToFile()
    public static void writeJsonToFile(String aFileName, String aJsonValue) throws IOException {

        if (!SAVE_JSON) {
            deleteJsonFolder();
            return;
        }

        String jsonFolderPath = UserDataInfo.JSON_FOLDER_PATH + aFileName + "/";
        if (Utility.DEBUG) Log.v("Utility", "jsonFolderPath: " + jsonFolderPath);

        File folder = new File(jsonFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String jsonFilePath = jsonFolderPath + aFileName + ".txt";
        if (Utility.DEBUG) Log.v("Utility", "jsonFilePath: " + jsonFilePath);

        FileWriter fWriter = new FileWriter(jsonFilePath);
        fWriter.write(aJsonValue);
        fWriter.flush();
        fWriter.close();

    }

    private static void deleteJsonFolder() {
        File folder = new File(UserDataInfo.JSON_FOLDER_PATH);
        if (folder.exists()) {
            if (DEBUG) Log.e(TAG, "folder is exist!!");
            deleteFile(folder);
        }
    }

    private static void deleteFile(File aFile) {
        if (aFile == null) {
            return;
        } else if (aFile.isDirectory()) {
            File[] files = aFile.listFiles();
            for (File file : files) {
                deleteFile(file);
            }
            aFile.delete();
        } else {
            aFile.delete();
        }
    }

    //TODO getMac()
    public static String getMac(Context aContext) {

        String mac = "";

        try {
            NetworkInterface NIC = NetworkInterface.getByName("eth0");
            if (NIC != null) {
                byte[] b = NIC.getHardwareAddress();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < b.length; i++) {
                    if (i != 0) {
                        buffer.append(':');
                    }
                    String str = Integer.toHexString(b[i] & 0xFF);
                    buffer.append(str.length() == 1 ? 0 + str : str);
                }
                mac = buffer.toString().toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		/*  phone or tablet or Other  */
        if (mac != null && mac.equals("")) {
            WifiManager wifiManager = (WifiManager) aContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            mac = wifiInfo.getMacAddress();
        }

        return mac;

    }

    //TODO getExecutorForShow()
    public static ExecutorService getExecutorForShow() {
        if (mExecutorServiceForShow == null || mExecutorServiceForShow.isShutdown())
            mExecutorServiceForShow = Executors.newScheduledThreadPool(5);
        return mExecutorServiceForShow;
    }

    //TODO getConnectivityStatus()
    public static boolean getConnectivityStatus(Context aContext) {

        if (aContext == null) {
            return false;
        }

        ConnectivityManager connectManager = (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (isEthernet()
                    || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    //TODO isEthernet()
    private static boolean isEthernet() {

        if (Utility.DEBUG) Log.e("Utility", "isEthernet()");

        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
//                if(Utility.DEBUG)Log.d("Utility", "Interface: " + networkInterface.getDisplayName());
                if (networkInterface.getDisplayName().equals("eth0")) {
                    final List<InterfaceAddress> ips = networkInterface.getInterfaceAddresses();
//                    if(Utility.DEBUG)Log.d("Utility", "ips.size(): " + ips.size());
                    for (int i = 0; i < ips.size(); i++) {
                        if (i == 1) {
                            mIPAddress = ips.get(i).getAddress().getHostAddress();
//                            if(Utility.DEBUG)Log.d("Utility", "IP: " + mIPAddress);
                            return true;
                        }
                    }
                } else {
                    mIPAddress = "0.0.0.0";
                }
            }
        } catch (final SocketException e) {
            if (Utility.DEBUG) Log.e("Utility", "XXX");
        }

        return false;

    }

    //TODO getCurrentActivityName()
    public static String getCurrentActivityName(Context aContext) {

        ActivityManager manager = (ActivityManager) aContext.getSystemService(aContext.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = manager.getRunningTasks(1);
        ComponentName componentInfo = tasks.get(0).topActivity;

        return componentInfo.getClassName();

    }

    //TODO getCurrentActivityPackage()
    public static String getCurrentActivityPackage(Context aContext) {

        ActivityManager manager = (ActivityManager) aContext.getSystemService(aContext.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = manager.getRunningTasks(1);
        ComponentName componentInfo = tasks.get(0).topActivity;

        return componentInfo.getPackageName();

    }

    //TODO getImageMD5()
    @SuppressWarnings("resource")
    public static String getImageMD5(final String aAbsolutePath) {
        StringBuffer sb = null;
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            sb = new StringBuffer();
            if (Utility.DEBUG) Log.d("Utility", "aAbsolutePath = " + aAbsolutePath);
            final FileInputStream fis = new FileInputStream(aAbsolutePath);
            final byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            final byte[] mdbytes = md.digest();
            for (final byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            fis.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //TODO isPackageExisted()
    public static boolean isPackageExisted(Context aContext, String aTargetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = aContext.getPackageManager();
        packages = pm.getInstalledApplications(0);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(aTargetPackage)) {
                return true;
            }
        }
        return false;
    }

    //TODO callApk()
    public static void callApk(Context aContext, String APK_Name) {
        final Intent LaunchIntent = aContext.getPackageManager().getLaunchIntentForPackage(APK_Name);
        aContext.startActivity(LaunchIntent);
    }

    //TODO getIPAddress()
    public static String getIPAddress() {
        if (mIPAddress == null) {
            isEthernet();
        }
        if (Utility.DEBUG) Log.i("Utility", "mIPAddress: " + mIPAddress);
        return mIPAddress;
    }

    //TODO getAppVersionCode()
    public static int getAppVersionCode(Context aContext) {
        if (aContext == null) {
            return 0;
        }
        PackageManager packageManager = aContext.getPackageManager();
        String packageName = aContext.getPackageName();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info == null ? 0 : info.versionCode;
    }

    //TODO getAppVersionName()
    public static String getAppVersionName(Context aContext) {
        if (aContext == null) {
            return null;
        }
        PackageManager packageManager = aContext.getPackageManager();
        String packageName = aContext.getPackageName();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info == null ? null : info.versionName;
    }

    public static String getShareMessage(Context aContext, String aShareUrl, String aShareText, ShareType aShareType) {

        String sendingMessageFormat;
        String sendingMessage = null;
        if (aShareText != null && !aShareText.trim().equals("")
                && aShareUrl != null && !aShareUrl.trim().equals("")) {
            //分享內容和Url都有
            if (aShareType == ShareType.photo) {
                sendingMessageFormat = aContext.getString(R.string.album_sending_message);
                sendingMessage = String.format(sendingMessageFormat, aShareText, aShareUrl);
            } else {
                sendingMessageFormat = aContext.getString(R.string.sending_message);
                sendingMessage = String.format(sendingMessageFormat, aShareText, aShareUrl);
            }
        } else if(aShareText != null && !aShareText.trim().equals("")){
            //僅有分享Text
            if (aShareType == ShareType.photo) {
                sendingMessageFormat = aContext.getString(R.string.album_sending_message_no_title);
            } else {
                sendingMessageFormat = aContext.getString(R.string.sending_message_no_title);
            }
            sendingMessage = String.format(sendingMessageFormat, aShareText);
        } else if(aShareUrl != null && !aShareUrl.trim().equals("")){
            //僅有分享URL
            if (aShareType == ShareType.photo) {
                sendingMessageFormat = aContext.getString(R.string.album_sending_message_no_title);
            } else {
                sendingMessageFormat = aContext.getString(R.string.sending_message_no_title);
            }
            sendingMessage = String.format(sendingMessageFormat, aShareUrl);
        }
        return sendingMessage;
    }

    //TODO shareToFacebook()
    public static void shareToFacebook(Context aContext, String aShareUrl) {
        if (Utility.DEBUG) Log.e(TAG, "Facebook click");

        if (isPackageExisted(aContext, "com.facebook.katana")) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, aShareUrl);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.facebook.katana");
            aContext.startActivity(sendIntent);
        } else {
            Toast.makeText(aContext, "您沒有安裝Facebook喔~~", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO shareToLine()
    public static void shareToLine(Context aContext, String aShareUrl, String aShareText, ShareType aShareType) {
        if (Utility.DEBUG) Log.e(TAG, "Line click");

        String sendingMessageFormat;
        String sendingMessage = null;
        if (aShareText != null && !aShareText.trim().equals("")) {
            //分享內容和Url都有
            if (aShareType == ShareType.photo) {
                sendingMessageFormat = aContext.getString(R.string.album_sending_message);
                sendingMessage = String.format(sendingMessageFormat, aShareText, aShareUrl);
            } else {
                sendingMessageFormat = aContext.getString(R.string.sending_message);
                sendingMessage = String.format(sendingMessageFormat, aShareText, aShareUrl);
            }
        } else {
            //僅有分享URL
            if (aShareType == ShareType.photo) {
                sendingMessageFormat = aContext.getString(R.string.album_sending_message_no_title);
            } else {
                sendingMessageFormat = aContext.getString(R.string.sending_message_no_title);
            }
            sendingMessage = String.format(sendingMessageFormat, aShareUrl);
        }

        if (isPackageExisted(aContext, "jp.naver.line.android")) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, sendingMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("jp.naver.line.android");
            aContext.startActivity(sendIntent);
        } else {
            Toast.makeText(aContext, "您沒有安裝Line喔~~", Toast.LENGTH_SHORT).show();
        }
    }

//    public static void shareToWeChat(final Context aContext, final String aShareUrl, final String aShareText, final String aImgUrl, final WeChatShareContent aShareContent) {
//        if (Utility.DEBUG) Log.e(TAG, "Wechat click");
//
////		GoogleAnalyticsFunction.sendSocialInteractions(aContext, "WeChat", "Share", aShareUrl);
//        GoogleAnalyticsFunction.sendHitInfo(aContext, "WeChat分享", aShareUrl, "");
//
//        if (mWeChatShareDialog != null && mWeChatShareDialog.isShowing()) {
//            return;
//        }
//
//        if (isPackageExisted(aContext, "com.tencent.mm")) {
//            mWeChatShareDialog = new MaterialDialog.Builder(aContext)
//                    .title("選擇分享方式")
//                    .items(new String[]{"分享給朋友", "分享至朋友圈"})
//                    .itemsCallback(new MaterialDialog.ListCallback() {
//
//                        @Override
//                        public void onSelection(MaterialDialog dialog, View itemView, int which,
//                                                CharSequence text) {
//
//                            switch (which) {
//                                case 0:
//                                    shareToWechatFriend(aContext, aShareUrl, aShareText, aImgUrl, aShareContent);
//                                    break;
//                                case 1:
//                                    shareToWechatFriendGroup(aContext, aShareUrl, aShareText, aImgUrl, aShareContent);
//                                    break;
//                            }
//
//                        }
//
//                    }).show();
//
//
//        } else {
//            Toast.makeText(aContext, "您沒有安裝Wechat喔~~", Toast.LENGTH_SHORT).show();
//        }
//
//        /** 利用Intent傳送訊息至Wechat，但無法分享至朋友圈
//         if(isPackageExisted(aContext, "com.tencent.mm")){
//         Intent intent = new Intent();
//         //			ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"); //必須一個一個選朋友
//         ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"); //必須一個一個選朋友
//         intent.setComponent(componentName);
//         intent.setAction(Intent.ACTION_SEND);
//         intent.setType("image/*");
//         //			intent.putExtra(Intent.EXTRA_TEXT, sendingMessage);
//         intent.putExtra("Kdescription", sendingMessage);
//         Uri uri = null;
//         File file = new File(UserDataInfo.ThumbnailPath + "Splash.jpg");
//         intent.putExtra(Intent.EXTRA_STREAM, uri);
//         aContext.startActivity(intent);
//         }else{
//         Toast.makeText(aContext, "您沒有安裝Wechat喔~~", Toast.LENGTH_SHORT).show();
//         }
//         */
//
//    }

//    protected static void shareToWechatFriendGroup(Context aContext, String aShareUrl, String aShareText,
//                                                   String aImgUrl, WeChatShareContent aShareContent) {
//
//        Bitmap bitmap = null;
//        if (aImgUrl == null) {
//            bitmap = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.ic_launcher);
//        } else {
//            BitmapController mBitmapController = BitmapController.getInstance(aContext);
//            bitmap = mBitmapController.getSingalBitmap(aImgUrl);
//        }
//        if (aShareText == null) {
//            aShareText = aContext.getString(R.string.share_message_end);
//        } else {
//            aShareText = aShareText + " | " + aContext.getString(R.string.share_message_end);
//        }
//
//        final IWXAPI wxApi = WXAPIFactory.createWXAPI(aContext, UserDataInfo.WECHAT_APP_ID, true);
//        wxApi.registerApp(UserDataInfo.WECHAT_APP_ID);
//
//        WXMediaMessage msg = new WXMediaMessage();
//        final SendMessageToWX.Req req = new SendMessageToWX.Req();
//
//        if (aShareContent == WeChatShareContent.Webpage) {
//
//            WXWebpageObject urlObject = new WXWebpageObject();
//            urlObject.webpageUrl = aShareUrl;
//            msg.mediaObject = urlObject;
//            msg.title = aShareText;
//            if (bitmap == null || (bitmap != null && bitmap.isRecycled())) {
//                bitmap = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.ic_launcher);
//            }
//            msg.thumbData = Util.bmpToByteArray(bitmap, true);
//
//            req.transaction = "webpage" + String.valueOf(System.currentTimeMillis());
//            req.message = msg;
//            req.scene = SendMessageToWX.Req.WXSceneTimeline;
//            wxApi.sendReq(req);
//
//        } else if (aShareContent == WeChatShareContent.Image) {
//
//            BitmapController mBitmapController = BitmapController.getInstance(aContext);
//            bitmap = mBitmapController.getSingalBitmap(aShareUrl);
//
//            WXImageObject imgObj = new WXImageObject(bitmap);
//
//            msg.mediaObject = imgObj;
//            Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
//            msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//            req.transaction = "originImage" + String.valueOf(System.currentTimeMillis());
//            req.message = msg;
//            req.scene = SendMessageToWX.Req.WXSceneTimeline;
//            wxApi.sendReq(req);
//
//        } else {
//            Toast.makeText(aContext, "資料錯誤", Toast.LENGTH_SHORT).show();
//        }
//
//    }

//	public static void processReturnBar(Context aContext) {
//		
//		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
//
//	    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//	        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//	        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//	        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//	        | View.SYSTEM_UI_FLAG_FULLSCREEN
//	        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//		
//		if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
//	    {
//
//			((Activity)aContext).getWindow().getDecorView().setSystemUiVisibility(flags);
//
//	        // Code below is to handle presses of Volume up or Volume down.
//	        // Without this, after pressing volume buttons, the navigation bar will
//	        // show up and won't hide
//	        final View decorView = ((Activity)aContext).getWindow().getDecorView();
//	        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
//            {
//
//                @Override
//                public void onSystemUiVisibilityChange(int visibility)
//                {
//                    if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
//                    {
//                        decorView.setSystemUiVisibility(flags);
//                    }
//                }
//            });
//	    }
//		
//	}

//    private static void shareToWechatFriend(Context aContext, String aShareUrl, String aShareText,
//                                            String aImgUrl, WeChatShareContent aShareContent) {
//
//        Bitmap bitmap = null;
//        if (aImgUrl == null) {
//            bitmap = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.ic_launcher);
//        } else {
//            BitmapController mBitmapController = BitmapController.getInstance(aContext);
//            bitmap = mBitmapController.getSingalBitmap(aImgUrl);
//        }
//        if (aShareText == null) {
//            aShareText = aContext.getString(R.string.share_message_end);
//        } else {
//            aShareText = aShareText + " | " + aContext.getString(R.string.share_message_end);
//        }
//
//        final IWXAPI wxApi = WXAPIFactory.createWXAPI(aContext, UserDataInfo.WECHAT_APP_ID, true);
//        wxApi.registerApp(UserDataInfo.WECHAT_APP_ID);
//
//        WXMediaMessage msg = new WXMediaMessage();
//        final SendMessageToWX.Req req = new SendMessageToWX.Req();
//
//        if (aShareContent == WeChatShareContent.Webpage) {
//
//            WXWebpageObject urlObject = new WXWebpageObject();
//            urlObject.webpageUrl = aShareUrl;
//            msg.mediaObject = urlObject;
//            msg.title = "NOWnews 新聞搶先報";
//            msg.description = aShareText;
//            if (bitmap == null || (bitmap != null && bitmap.isRecycled())) {
//                bitmap = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.ic_launcher);
//            }
//            msg.thumbData = Util.bmpToByteArray(bitmap, true);
//
//            req.transaction = "webpage" + String.valueOf(System.currentTimeMillis());
//            req.message = msg;
//            req.scene = SendMessageToWX.Req.WXSceneSession;
//            wxApi.sendReq(req);
//
//        } else if (aShareContent == WeChatShareContent.Image) {
//
//            BitmapController mBitmapController = BitmapController.getInstance(aContext);
//            bitmap = mBitmapController.getSingalBitmap(aShareUrl);
//
//            WXImageObject imgObj = new WXImageObject(bitmap);
//
//            msg.mediaObject = imgObj;
//            Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
//            msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//            req.transaction = "originImage" + String.valueOf(System.currentTimeMillis());
//            req.message = msg;
//            req.scene = SendMessageToWX.Req.WXSceneSession;
//            wxApi.sendReq(req);
//
//        } else {
//            Toast.makeText(aContext, "資料錯誤", Toast.LENGTH_SHORT).show();
//        }
//
//    }

//    //TODO shareToWeibo() 新浪微博分享
//    public static void shareToWeibo(final Context aContext, String aShareUrl, String aShareText, String aShareSummary, Bitmap aSmallShareImage, Bitmap aBigShareImage) {
//        if (Utility.DEBUG) Log.e(TAG, "Weibo click");
//
//        GoogleAnalyticsFunction.sendHitInfo(aContext, "微博分享", aShareUrl, "");
//
//        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(aContext, UserDataInfo.WEIBO_APP_KEY);
//        if (!isPackageExisted(aContext, "com.sina.weibo")) {
//            Toast.makeText(aContext, "您沒有安裝微博喔~~", Toast.LENGTH_SHORT).show();
//        } else {
//
//            mWeiboShareAPI.registerApp();
//
//            if (aShareSummary == null) {
//                aShareSummary = "";
//            }
//            if (aShareText == null) {
//                aShareText = aContext.getString(R.string.share_message_end) + " " + aContext.getString(R.string.share_tag);
//            } else {
//                aShareText = aShareText + " | " + aContext.getString(R.string.share_message_end) + " " + aContext.getString(R.string.share_tag);
//            }
//
//            WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
//
//            if (aBigShareImage != null) {
//                if (Utility.DEBUG) Log.e(TAG, "big image is NOT null!!!");
//                ImageObject imageObject = new ImageObject();
//                imageObject.setImageObject(aBigShareImage);
//                weiboMessage.imageObject = imageObject;
//            }
//
//            //網址
//            WebpageObject mediaObject = new WebpageObject();
//            String identify = com.sina.weibo.sdk.utils.Utility.generateGUID();
//            if (identify != null) {
//                mediaObject.identify = identify;
//            } else {
//                mediaObject.identify = "";
//            }
//            mediaObject.title = aShareText;
//            mediaObject.description = aShareSummary;
//
//            // 设置 Bitmap 类型的图片到视频对象里
//            if (aSmallShareImage == null) {
//                aSmallShareImage = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.ic_launcher);
//            }
//            mediaObject.setThumbImage(aSmallShareImage);
//            mediaObject.actionUrl = aShareUrl;
//            mediaObject.defaultText = aShareText;
//            weiboMessage.mediaObject = mediaObject;
//
//            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
//            request.transaction = String.valueOf(System.currentTimeMillis());
//            request.multiMessage = weiboMessage;
//
//            if (request != null) {
//                mWeiboShareAPI.sendRequest(request);
//            }
//
//        }
//
//
////		if(isPackageExisted(aContext, "com.sina.weibo")){
////
////			Uri uri = Uri.fromFile(new File(UserDataInfo.ThumbnailPath, "Splash.jpg"));
////			ArrayList<Uri> images = new ArrayList<Uri>();
////			images.add(uri);
////
////			Intent sendIntent = new Intent();
////			sendIntent.setAction(Intent.ACTION_SEND);
////			sendIntent.putExtra(Intent.EXTRA_TEXT, aShareUrl);
////			sendIntent.setType("image/*");
////			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
////			sendIntent.setClassName("com.sina.weibo", "com.sina.weibo.EditActivity");
////			aContext.startActivity(sendIntent);
////		}else{
////			Toast.makeText(aContext, "您沒有安裝新浪微博喔~~", Toast.LENGTH_SHORT).show();
////		}
////
////		Intent shareIntent = new Intent(Intent.ACTION_SEND);
////		Uri uri = Uri.fromFile(new File(UserDataInfo.ThumbnailPath, "Splash.jpg"));
////	    if(uri!=null){
////	        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
////	        shareIntent.setType("image/*");
////	        //当用户选择短信时使用sms_body取得文字
////	        shareIntent.putExtra("sms_body", "Hello");
////	    }else{
////	        shareIntent.setType("text/plain");
////	    }
////	    shareIntent.putExtra(Intent.EXTRA_TEXT, "Hello");
////	    //自定义选择框的标题
////	    aContext.startActivity(Intent.createChooser(shareIntent, "邀请好友"));
////	    //系统默认标题
//
//    }

    //TODO getScreenWidth()
    public static int getScreenWidth(Context aContext) {
        if (mScreenWidth == 0) {
            processScreenSize(aContext);
        }
        return mScreenWidth;
    }

    //TODO getScreenHeight()
    public static int getScreenHeight(Context aContext) {
        if (mScreenHeight == 0) {
            processScreenSize(aContext);
        }
        return mScreenHeight;
    }

    //TODO getScreenDPWidth()
    public static int getScreenDPWidth(Context aContext) {
        if (mScreenDPWidth == 0) {
            processScreenSize(aContext);
        }
        return mScreenDPWidth;
    }

    //TODO getScreenDPHeight()
    public static int getScreenDPHeight(Context aContext) {
        if (mScreenDPHeight == 0) {
            processScreenSize(aContext);
        }
        return mScreenDPHeight;
    }

    //TODO getDensity()
    public static float getDensity(Context aContext) {
        if (mDensity == 0) {
            processScreenSize(aContext);
        }
        return mDensity;
    }

    //TODO setDensity()
    public static void setDensity(float mDensity) {
        Utility.mDensity = mDensity;
    }

    //TODO processScreenSize()
    public static void processScreenSize(Context aContext) {

        WindowManager window = (WindowManager) aContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        final DisplayMetrics metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        mDensity = metric.density;

        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;

        mScreenDPWidth = (int)(mScreenWidth / mDensity);
        mScreenDPHeight = (int)(mScreenHeight / mDensity);

        if (DEBUG) Log.e(TAG, "mScreenWidth: " + mScreenWidth);
        if (DEBUG) Log.e(TAG, "mScreenHeight: " + mScreenHeight);
        if (DEBUG) Log.e(TAG, "mDensity: " + mDensity);

    }

    public static void openNetworkErrorDialog() {
        if(!UserDataInfo.isActivityVisible()){
            return;
        }else if (mNetworkErrordialog != null && mNetworkErrordialog.isShowing()) {
            return;
        }
        final Context context = UserDataInfo.getCurrentContext();
        mNetworkErrordialog = new MaterialDialog.Builder(context)
                .cancelable(false)
                .customView(R.layout.dialog_network_error, false)
                .positiveText(R.string.exit)
                .negativeText(R.string.retry)
                .callback(new ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                        //Retry
                        if (UserDataInfo.isActivityVisible() && !getConnectivityStatus(context)) {
                            openNetworkErrorDialog();
                        } else if (UserDataInfo.isActivityVisible() && getConnectivityStatus(context)) {
                            reloadActivity();
                        }
                        super.onNegative(dialog);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.cancel();
                        //Finish
//				((Activity)context).finish();
//				UserDataInfo.finishApp();
                        UserDataInfo.AppExit(context);
                        super.onPositive(dialog);
                    }
                }).build();
//		mNetworkErrordialog = new AlertDialogWrapper.Builder(aContext)
//		.setTitle(R.string.warning)
//		.setMessage(R.string.network_warning)
//		.setPositiveButton(R.string.ok, new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.cancel();
//				if(UserDataInfo.isActivityVisible() && !getConnectivityStatus(aContext)){
//					openNetworkErrorDialog(aContext);
//				}
//			}
//		})
//		.setNegativeButton(R.string.goto_network_setting, new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//				if(!(aContext instanceof Activity)){
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				}
//				aContext.startActivity(intent);
//			}
//		}).create();
        mNetworkErrordialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mNetworkErrordialog.show();
    }

    public static void openSocketTimeoutDialog(final Context aContext) {
        if(!UserDataInfo.isActivityVisible()){
            return;
        }else if (isNetworkSlow() && !isUserKnowNetworkSlow) {
            showNetworkSlowDialog(aContext, null, true);
            return;
        } else if (isNetworkSlow() && isUserKnowNetworkSlow) {
            return;
        } else if (!getConnectivityStatus(aContext)) {
            return;
        }
        if (mSocketTimoutDialog != null && mSocketTimoutDialog.isShowing()) {
            return;
        }
        mSocketTimoutDialog = new MaterialDialog.Builder(aContext)
                .cancelable(true)
                .customView(R.layout.dialog_socket_timeout, false)
                .positiveText(R.string.exit)
                .negativeText(R.string.retry)
                .callback(new ButtonCallback() {

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                        //Retry
                        if (UserDataInfo.isActivityVisible() && !getConnectivityStatus(aContext)) {
                            openNetworkErrorDialog();
                        } else if (UserDataInfo.isActivityVisible() && getConnectivityStatus(aContext)) {
                            reloadActivity();
                        }
                        super.onNegative(dialog);
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.cancel();
                        //Finish
//				((Activity)aContext).finish();
//				UserDataInfo.finishApp();
                        UserDataInfo.AppExit(aContext);
                        super.onPositive(dialog);
                    }
                }).build();
//		mSocketTimoutDialog = new AlertDialogWrapper.Builder(aContext)
//		.setTitle(R.string.sorry)
//		.setMessage(R.string.socket_timout_error_message)
//		.setPositiveButton(R.string.ok, new OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.cancel();
//				//Finish
//				((Activity)aContext).finish();
//			}
//		})
//		.setNegativeButton(R.string.retry, new OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.cancel();
//				//Reload
//				reloadActivity();
//			}
//		}).create();
        mSocketTimoutDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mSocketTimoutDialog.show();
    }

    public static boolean isBadSignal(Context aContext) {
        return isBadSignal;
    }

    public static void setNetworkSignal(boolean aNetworkSignal) {
        isBadSignal = aNetworkSignal;
    }

    public static boolean isNetworkSlow() {
        return isNetworkSlow;
    }

    public static void setNetworkSlow(boolean isNetworkSlow) {
        Utility.isNetworkSlow = isNetworkSlow;
    }

    public static boolean showNetworkSlowDialog(final Context aContext, final Handler aHandler, boolean isNeedToShow) {
        if (aContext == null) {
            if (Utility.DEBUG) Log.e(TAG, "aContext==null");
            return false;
        }
        int connectType = getConnectType(aContext);
        boolean isNetworkSlow = false;
        if (connectType == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) aContext.getSystemService(Context.WIFI_SERVICE);
            int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
            String wifiName = wifiManager.getConnectionInfo().getSSID();
            isNetworkSlow = isWifiSlow(linkSpeed);
            if (Utility.DEBUG)
                Log.e(TAG, "TYPE_WIFI / " + wifiName + " / " + (isNetworkSlow ? "Slow" : "Fast"));
        } else if (connectType == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = telephonyManager.getNetworkType();
            String networkTypeName = getMobileNetworkType(networkType);
            isNetworkSlow = isNetworkSlow(networkType);
            if (Utility.DEBUG)
                Log.e(TAG, "TYPE_MOBILE / " + networkTypeName + " / " + (isNetworkSlow ? "Slow" : "Fast"));
        }
        setNetworkSlow(isNetworkSlow);
        if (isNetworkSlow && isNeedToShow && ((Activity)aContext).hasWindowFocus()) {
            new MaterialDialog.Builder(aContext)
                    .cancelable(false)
                    .customView(R.layout.dialog_network_slow, false)
                    .negativeText(R.string.ok_i_know)
                    .callback(new ButtonCallback() {

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            isUserKnowNetworkSlow = true;
                            //Retry
                            if (UserDataInfo.isActivityVisible() && !getConnectivityStatus(aContext)) {
                                openNetworkErrorDialog();
                            } else if (UserDataInfo.isActivityVisible() && getConnectivityStatus(aContext)) {
                                if (aHandler != null) {
                                    aHandler.sendEmptyMessage(SplashActivity.NETWORK_SLOW_OK_CLICK);
                                } else {
                                    reloadActivity();
                                }
                            }
                            super.onNegative(dialog);
                        }

                    }).show();
        }
        return isNetworkSlow;
    }

    private static int getConnectType(Context aContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo == null ? 0 : networkInfo.getType();
    }

    public static String getMobileNetworkType(int aNetworkType) {
        return NetworkType.getNetworkTypeName(aNetworkType);
    }

    public static boolean isNetworkSlow(int aNetworkType) {
        return NetworkType.isNetworkSlow(aNetworkType);
    }

    public static boolean isWifiSlow(int aLinkSpeed) {
        if (aLinkSpeed > 15) {
            return false;
        } else {
            return true;
        }
    }

    public static void reloadActivity() {
        Context mCurrentContext = UserDataInfo.getCurrentContext();
        if (mCurrentContext == null) {
            return;
        }
        if (mCurrentContext instanceof SplashActivity) {
            ((SplashActivity) mCurrentContext).startSplashActivity();
        }else if (mCurrentContext instanceof VideoNewsPage) {
            ((VideoNewsPage) mCurrentContext).reload();
        }
//        else if (mCurrentContext instanceof AlbumCategoryFragment) {
//            ((AlbumCategoryFragment) mCurrentContext).startSplashActivity();
//        }
        else if (mCurrentContext instanceof NewsPage) {
            ((NewsPage) mCurrentContext).reload();
        } else if (mCurrentContext instanceof AlbumPage) {
            ((AlbumPage) mCurrentContext).reload();
        } else if (mCurrentContext instanceof FavoriteAlbum) {
            ((FavoriteAlbum) mCurrentContext).reload();
        } else if (mCurrentContext instanceof FavoriteAlbumPage) {
            ((FavoriteAlbumPage) mCurrentContext).reload();
        } else if (mCurrentContext instanceof SearchActivity) {
            ((SearchActivity) mCurrentContext).reload();
        } else if (mCurrentContext instanceof WebActivity) {
            ((WebActivity) mCurrentContext).reload();
        }
        //Not ready
//		else if(mCurrentContext instanceof NotificationDialog){
//			((NotificationDialog)mCurrentContext).startSplashActivity();
//		}
    }

    public static String getSrcFromImgapi(String aUrl) {
        if (aUrl.contains("imgapiv2")) {
            aUrl = aUrl.substring(aUrl.lastIndexOf("http"));
        }
        if(aUrl.contains("%3A")){
            aUrl = aUrl.replaceAll("%3A", ":");
        }
        if(aUrl.contains("%2F")){
            aUrl = aUrl.replaceAll("%2F", "/");
        }
        return aUrl;
    }

    public static String processDate(String aDateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date;
        try {
            date = simpleDateFormat.parse(aDateString);
            aDateString = simpleDateFormat.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return aDateString;
    }

    public static String getTimeAgo(Context aContext, String aCreateAt) {

        if (Utility.DEBUG) Log.e(TAG, "aCreateAt: " + aCreateAt);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(aCreateAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar currentCalendar = Calendar.getInstance();

        long newsDate = calendar.getTimeInMillis();
        long currentDate = currentCalendar.getTimeInMillis();
        if (Utility.DEBUG) Log.e(TAG, "newsDate: " + newsDate);
        if (Utility.DEBUG) Log.e(TAG, "currentDate: " + currentDate);

        long diff = currentDate - newsDate;
        long diffSec = diff / 1000;
        long diffMin = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffSec > 0 && diffSec < 60) {
            return diffSec + aContext.getString(R.string.seconds_ago);
        } else if (diffMin > 0 && diffMin < 60) {
            return diffMin + aContext.getString(R.string.minutes_ago);
        } else if (diffHours > 0 && diffHours < 24) {
            return diffHours + aContext.getString(R.string.hours_ago);
        } else {
            return diffDays + aContext.getString(R.string.days_ago);
        }
    }

    public static String getKeyHash(Context aContext) {

        try{

            PackageInfo info = aContext.getPackageManager().getPackageInfo("com.nownews", PackageManager.GET_SIGNATURES);
            for (Signature signature: info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }

        } catch (NameNotFoundException e){
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static enum ColorType{
        News, Album, Video
    }
    public static void setCategoryTextColor(String aCategory, TextView aTextView, ColorType aColorType) {
//        int colorString = 0xFFFFFFFF;
//        switch(aColorType){
//            case News:
//                for (NewsCategoryColor colorInfo : NewsCategoryColor.values()) {
//                    if (colorInfo.name().equals(aCategory)) {
//                        colorString = colorInfo.getColor();
//                        break;
//                    }
//                }
//                break;
//            case Album:
//                for (AlbumsCategoryColor colorInfo : AlbumsCategoryColor.values()) {
//                    if (colorInfo.name().equals(aCategory)) {
//                        colorString = colorInfo.getColor();
//                        break;
//                    }
//                }
//                break;
//            case Video:
//                for (VideoNewsCategoryColor colorInfo : VideoNewsCategoryColor.values()) {
//                    if (colorInfo.name().equals(aCategory)) {
//                        colorString = colorInfo.getColor();
//                        break;
//                    }
//                }
//                break;
//        }
//
//        aTextView.setTextColor(colorString);
        int colorString = 0xFF0099FF;
        aTextView.setTextColor(colorString);
    }

    public enum ShareType {news, photo, normal}

    /**
     * 微信分享內容
     */
    public enum WeChatShareContent {
        /**
         * 網頁
         */
        Webpage,
        /**
         * 圖片
         */
        Image
    }

    /**
     * 微信分享方式
     */
    public enum WeChatShareType {
        /**
         * 微信朋友圈
         */
        FriendGroup,
        /**
         * 微信朋友
         */
        ChooseFriend
    }

    public static String getAdvertisingId (Context aContext){
        try{
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(aContext);
            if(info!=null){
                mAdvertisingId = info.getId();
            }
        } catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return mAdvertisingId;
    }

    private static String mAdvertisingId;

    public static String getAdvertisingId(){
        return mAdvertisingId;
    }

    public static void shareToSNS(Context aContext, String aShareMessage){
        Log.d(TAG, "aShareMessage: " + aShareMessage);
        Intent sendIntent = getShareIntent(aContext, aShareMessage);
        aContext.startActivity(Intent.createChooser(sendIntent, "分享這則新聞至..."));
    }

    public static Intent getShareIntent(Context aContext, String aShareMessage){
        if(aShareMessage!=null && aShareMessage.contains("▲")){
            aShareMessage = aShareMessage.replaceAll("▲", "");
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, aShareMessage);
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    public static void shareApp(Context mContext){
        Uri deeplink = createDeepLink(mContext);
        Log.d(TAG, "deeplink: " + deeplink.toString());
        Intent intent = new AppInviteInvitation.IntentBuilder("分享NOWnews今日新聞")
                .setMessage("NOWnews今日新聞94狂!!\n最新最快最勁爆的新聞都在這!!\n還有免費直播讓你看!!\n還不趕快下載!!")
                .setDeepLink(Uri.parse("https://qv5h4.app.goo.gl/OnxH"))
                .setCallToActionText("點我下載")
                .build();
        ((Activity)mContext).startActivityForResult(intent, 0x789);
    }

    private static Uri createDeepLink(Context mContext){
        String scheme = mContext.getString(R.string.dynamic_links_scheme);
        String appCode = mContext.getString(R.string.dynamic_links_app_code);
        String domain = mContext.getString(R.string.dynamic_links_domain);
        String deeplinkAddress = mContext.getString(R.string.deeplink_address);
        Uri.Builder builder = new Uri.Builder()
                .scheme(scheme)
                .authority(appCode + domain)
                .path("/")
                .appendQueryParameter("link", deeplinkAddress)
                .appendQueryParameter("apn", mContext.getPackageName())
                .appendQueryParameter("amv", Integer.toString(Utility.getAppVersionCode(mContext)));
        return builder.build();
    }

    //20170421 Cindy move from MenuContent
    private static MaterialDialog mNotificationSwitchDialog;
    public static void showNotificationSwicherDialog(final Context aContext, DialogInterface.OnDismissListener aDialogDismissListener) {

        View notificationSettingView = LayoutInflater.from(aContext).inflate(R.layout.widget_notification_switcher, null);
        final Switch switcher = (Switch) notificationSettingView.findViewById(R.id.switcher);
        final TextView switcherStatus = (TextView)notificationSettingView.findViewById(R.id.switch_status);
        final LinearLayout soundSetting = (LinearLayout)notificationSettingView.findViewById(R.id.notification_sound_setting);
        final CheckBox sound = (CheckBox)notificationSettingView.findViewById(R.id.sound);
        final CheckBox vibrate = (CheckBox)notificationSettingView.findViewById(R.id.vibrate);
        final LinearLayout timeSetting = (LinearLayout)notificationSettingView.findViewById(R.id.notification_time_setting);
        final RadioGroup timeRadio = (RadioGroup)notificationSettingView.findViewById(R.id.time_radios);

        final SharedPreferencesMethods mSharedPref = new SharedPreferencesMethods(aContext);

        boolean isNotificationOpen = mSharedPref.getNotificationStatus();
        if(isNotificationOpen){
            switcherStatus.setText(aContext.getString(R.string.open));
            soundSetting.setVisibility(View.VISIBLE);
            timeSetting.setVisibility(View.VISIBLE);
        }else{
            switcherStatus.setText(aContext.getString(R.string.close));
            soundSetting.setVisibility(View.GONE);
            timeSetting.setVisibility(View.GONE);
        }
        switcher.setChecked(isNotificationOpen);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    switcherStatus.setText(aContext.getString(R.string.open));
                    soundSetting.setVisibility(View.VISIBLE);
                    timeSetting.setVisibility(View.VISIBLE);
                }else{
                    switcherStatus.setText(aContext.getString(R.string.close));
                    soundSetting.setVisibility(View.GONE);
                    timeSetting.setVisibility(View.GONE);
                }

            }
        });

        boolean isSoundOpen = mSharedPref.getNotificationSoundStatus();
        sound.setChecked(isSoundOpen);

        boolean isVibrate = mSharedPref.getNotificationVibrateStatus();
        vibrate.setChecked(isVibrate);

        int timeRadioBtnId = mSharedPref.getNotificationTime();
        switch(timeRadioBtnId){
            case 0:
                timeRadio.check(R.id.all_day);
                break;
            case 1:
                timeRadio.check(R.id.only_am);
                break;
            case 2:
                timeRadio.check(R.id.only_pm);
                break;
        }

        boolean wrapInScrollView = false;
        mNotificationSwitchDialog = new MaterialDialog.Builder(aContext)
                .customView(notificationSettingView, wrapInScrollView)
                .title(aContext.getString(R.string.notification_settings_title))
                .positiveText("完成")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        boolean isNotificationOpen = switcher.isChecked();

                        mSharedPref.setNotificationStatus(isNotificationOpen);
                        GoogleAnalyticsFunction.sendHitInfo(aContext, aContext.getString(R.string.cloud_message),
                                (isNotificationOpen? aContext.getString(R.string.cloud_message_open):aContext.getString(R.string.cloud_message_close)), "");

                        mSharedPref.setNotificationSoundStatus(sound.isChecked());
                        GoogleAnalyticsFunction.sendHitInfo(aContext, aContext.getString(R.string.cloud_message),
                                (sound.isChecked()? aContext.getString(R.string.sound_open):aContext.getString(R.string.sound_close)), "");

                        mSharedPref.setNotificationVibrateStatus(vibrate.isChecked());
                        GoogleAnalyticsFunction.sendHitInfo(aContext, aContext.getString(R.string.cloud_message),
                                (vibrate.isChecked()? aContext.getString(R.string.vibrate_open):aContext.getString(R.string.vibrate_close)), "");

                        int timeRadioBtnId = timeRadio.getCheckedRadioButtonId();
                        int timeStatus = 0;
                        String timeString = aContext.getString(R.string.notification_settings_time) + " ";
                        switch(timeRadioBtnId){
                            case R.id.all_day:
                                timeStatus = 0;
                                timeString = timeString + aContext.getString(R.string.all_day);
                                break;
                            case R.id.only_am:
                                timeStatus = 1;
                                timeString = timeString + aContext.getString(R.string.only_am);
                                break;
                            case R.id.only_pm:
                                timeStatus = 2;
                                timeString = timeString + aContext.getString(R.string.only_pm);
                                break;
                        }
                        mSharedPref.setNotificationTime(timeStatus);
                        GoogleAnalyticsFunction.sendHitInfo(aContext, aContext.getString(R.string.cloud_message), timeString, "");

                    }
                })
                .dismissListener(aDialogDismissListener)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if(mSharedPref!=null){
                            mSharedPref.unRegistContext(aContext);
                        }
                    }
                })
                .show();
    }

    public static double[] getLongitudeLatitude(Context aContext){
        double[] longitudeLatitude = new double[2];

        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
        long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

        LocationManager locationManager = (LocationManager)aContext.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGPSEnabled && !isNetworkEnabled){
            return longitudeLatitude;
        }else if(isGPSEnabled){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                int accessFineLocationPermission = aContext.checkSelfPermission(ACCESS_FINE_LOCATION);
                int accessCoarseLocationPermission = aContext.checkSelfPermission(ACCESS_COARSE_LOCATION);
                if(accessFineLocationPermission != PackageManager.PERMISSION_GRANTED
                        || accessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                    //未取得權限，向使用者要求允許權限
                    if (Utility.DEBUG)Log.e(TAG, "未取得Location權限");
                    return longitudeLatitude;
                }
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mLocationListener);
            if(locationManager!=null){
                mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(mCurrentLocation!=null){
                    longitudeLatitude[0] = mCurrentLocation.getLongitude();
                    longitudeLatitude[1] = mCurrentLocation.getLatitude();
                    if (Utility.DEBUG) Log.i(TAG, "isGPSEnabled longitude: " + longitudeLatitude[0]);
                    if (Utility.DEBUG) Log.i(TAG, "isGPSEnabled latitude: " + longitudeLatitude[1]);
                    locationManager.removeUpdates(mLocationListener);
                    return longitudeLatitude;
                }else{
                    if (Utility.DEBUG) Log.w(TAG, "isGPSEnabled location==null");
                }
                locationManager.removeUpdates(mLocationListener);
            }else{
                if (Utility.DEBUG) Log.w(TAG, "isGPSEnabled locationManager==null");
            }
        }

        if(isNetworkEnabled){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mLocationListener);
            if(locationManager!=null){
                mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(mCurrentLocation!=null){
                    longitudeLatitude[0] = mCurrentLocation.getLongitude();
                    longitudeLatitude[1] = mCurrentLocation.getLatitude();
                    if (Utility.DEBUG) Log.i(TAG, "isNetworkEnabled longitude: " + longitudeLatitude[0]);
                    if (Utility.DEBUG) Log.i(TAG, "isNetworkEnabled latitude: " + longitudeLatitude[1]);
                }else{
                    if (Utility.DEBUG) Log.w(TAG, "isNetworkEnabled location==null");
                }
                locationManager.removeUpdates(mLocationListener);
            }else{
                if (Utility.DEBUG) Log.w(TAG, "isNetworkEnabled locationManager==null");
            }
        }

        return longitudeLatitude;
    }

    public static LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) { }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    private static Location mCurrentLocation;
    public static Location getLocation(Context aContext){
        getLongitudeLatitude(aContext);
        return mCurrentLocation;
    }

    public static void MuteAudio(Context aContext){
        AudioManager mAlramMAnager = (AudioManager) aContext.getSystemService(aContext.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    public static void UnMuteAudio(Context aContext){
        AudioManager mAlramMAnager = (AudioManager) aContext.getSystemService(aContext.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

}

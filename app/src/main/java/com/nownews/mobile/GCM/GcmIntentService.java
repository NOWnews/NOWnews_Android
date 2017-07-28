package com.nownews.mobile.GCM;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.nownews.R;
import com.nownews.mobile.AlbumPage.AlbumPage;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.BroadcastReceiver.GcmShareAppReceiver;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Controller.BitmapController.ImageLoadingListener;
import com.nownews.mobile.Json.GetNotificationInfo;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Widget.WebActivity;

import java.util.Calendar;
import java.util.Map;

public class GcmIntentService extends FirebaseMessagingService {

    private final String TAG = getClass().getSimpleName();
    private NotificationManager mNotificationManager;
    private SharedPreferencesMethods mSharedPref;
    private GetNotificationInfo mNotificationInfo;
    private Bitmap bitmap = null;
    private ImageLoadingListener mImageLoadingListener;
    private RemoteMessage mRemoteMessage;
    private final String INTENT_FILTER = "INTENT_FILTER";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        /**
         * onMessageReceived(RemoteMessage message) is called "in the background" (not on the UI/Main thread).
         * */

        mRemoteMessage = message;
        Intent intent = new Intent(INTENT_FILTER);
        sendBroadcast(intent);

    }

    @Override
    public void handleIntent(Intent intent) {
        super.handleIntent(intent);
        if (Utility.DEBUG) Log.i(TAG, "handleIntent intent: " + intent);
        if (Utility.DEBUG) Log.i(TAG, "handleIntent intent: " + intent.getExtras());
        if (Utility.DEBUG) Log.i(TAG, "handleIntent intent: " + intent.getStringExtra("data"));
        if (Utility.DEBUG) Log.i(TAG, "handleIntent intent: " + intent.getStringExtra("notification"));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mFCMReceiver, new IntentFilter(INTENT_FILTER));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mFCMReceiver);
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.unregistBitmapController(this);
        }
    }

    private boolean isSoundOpen;
    private boolean isVibrateOpen;
    private BroadcastReceiver mFCMReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mSharedPref = new SharedPreferencesMethods(context);
            boolean isGCMOpen = mSharedPref.getNotificationStatus();
            if (!isGCMOpen) {
                return;
            }
            boolean isInTime = checkTime(mSharedPref.getNotificationTime());
            if(!isInTime){
                return;
            }
            isSoundOpen = mSharedPref.getNotificationSoundStatus();
            isVibrateOpen = mSharedPref.getNotificationVibrateStatus();
            if(mSharedPref!=null){
                mSharedPref.unRegistContext(context);
            }

            String from = mRemoteMessage.getFrom();
            if (from != null) {
                if (Utility.DEBUG) Log.i(TAG, "from: " + from);
            }

//            Map data = mRemoteMessage.getData();
//            String messageFromServer = (String) data.get("message");
//            if (messageFromServer != null) {
//                if (Utility.DEBUG) Log.i(TAG, "messageFromServer: " + messageFromServer);
//                processMessage(messageFromServer);
//                setNotification();
//            }

            //TODO getData()
            Map data = mRemoteMessage.getData();
            RemoteMessage.Notification notification = mRemoteMessage.getNotification();
            if(data!=null){
                if (data.size() > 0) {
                    if (Utility.DEBUG) Log.i(TAG, "data: " + data);
                }
                String title = (String)data.get("title");
                String summary = (String)data.get("summary");
                String image = (String)data.get("image");
                String id = (String)data.get("id");
                String type = (String)data.get("type");
                String url = (String)data.get("url");
                if (Utility.DEBUG) Log.i(TAG, "title: " + title);
                if (Utility.DEBUG) Log.i(TAG, "summary: " + summary);
                if (Utility.DEBUG) Log.i(TAG, "image: " + image);
                if (Utility.DEBUG) Log.i(TAG, "id: " + id);
                if (Utility.DEBUG) Log.i(TAG, "type: " + type);
                if (Utility.DEBUG) Log.i(TAG, "url: " + url);
            }
            if(notification!=null){
                //TODO getNotification()
                Log.w(TAG, "notification.toString(): " + notification.toString());
                String title = notification.getTitle();
                String body = notification.getBody();
                String icon = notification.getIcon();
                String clickAction = notification.getClickAction();
                if (Utility.DEBUG) Log.i(TAG, "title: " + title);
                if (Utility.DEBUG) Log.i(TAG, "body: " + body);
                if (Utility.DEBUG) Log.i(TAG, "icon: " + icon);
                if (Utility.DEBUG) Log.i(TAG, "clickAction: " + clickAction);
            }


        }
    };

    private boolean checkTime(int aNotificationTime){
        Calendar calendar = Calendar.getInstance();
        int ampm = calendar.get(Calendar.AM_PM); //0->AM 1->PM
        switch(aNotificationTime){
            case 0: //all day
                return true;
            case 1: //only am
                if(ampm==0){
                    return true;
                }
                break;
            case 2: //only pm
                if(ampm==1){
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onSendError(String msgId, Exception error) {
        super.onSendError(msgId, error);
        if (Utility.DEBUG) Log.e(TAG, "[onSendError] msgId: " + msgId + " error: " + error);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        if (Utility.DEBUG) Log.w(TAG, "[onDeletedMessages]");
    }

    private void processMessage(String aMessage) {
        mNotificationInfo = new Gson().fromJson(aMessage, GetNotificationInfo.class);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setNotification() {
        if (Utility.DEBUG) Log.w(TAG, "mNotificationInfo: " + mNotificationInfo);
        if (mNotificationInfo == null || mNotificationInfo.type == null) {
            return;
        }

        String newsTitle;
        String newsSummary;
        String type;
        String notificationUrl;
        String notificationTitle;
        int notificationId;

        type = mNotificationInfo.type;
        notificationUrl = mNotificationInfo.url;
        notificationTitle = mNotificationInfo.title;
        if (Utility.DEBUG) Log.e(TAG, "type: " + type);
        if (Utility.DEBUG) Log.e(TAG, "notificationUrl: " + notificationUrl);
        if (Utility.DEBUG) Log.e(TAG, "notificationTitle: " + notificationTitle);

        notificationId = -1;
        if (notificationUrl != null && !notificationUrl.isEmpty()) {
            if (notificationUrl.contains("/n/") || notificationUrl.contains("/news/")) {
                type = "news";
                notificationId = Integer.parseInt(notificationUrl.substring(notificationUrl.lastIndexOf("/") + 1));
            } else if (notificationUrl.contains("/p/") || notificationUrl.contains("/photo/")) {
                type = "album";
                notificationId = Integer.parseInt(notificationUrl.substring(notificationUrl.lastIndexOf("/") + 1));
            }
        }
        if (Utility.DEBUG) Log.w(TAG, "type: " + type);
        if (Utility.DEBUG) Log.w(TAG, "notificationId: " + notificationId);

        //設置標題
        newsTitle = getString(R.string.nownews);
        if (newsTitle == null) {
            newsTitle = "NOWnews今日新聞";
        }
        //設置內容
        if (mNotificationInfo.title == null) {
            newsSummary = "最新消息!!!";
        } else {
            newsSummary = mNotificationInfo.title;
        }

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        //建立按下訊息嵌板後所要轉跳的Intent
        Intent intent = createNotificationGoWhere(type, notificationUrl, notificationId);
        //設定請求碼，請求碼若相同則會以最新的為準
        int requestCode = 1;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //設定鈴聲
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/raw/notification_sound");

        //設定振動頻率
        long[] vibratepattern = {0, 300, 200, 300};

        //建立-通知服務建構器
        int currentSDKVersion = UserDataInfo.getSdkVersion();
        if (currentSDKVersion >= Build.VERSION_CODES.JELLY_BEAN) {

            //Api Level 16 (4.1)以上
            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            final Builder builder = new Builder(this);
            //定義Notification建構器
            builder
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setLargeIcon(iconBitmap);


            if(currentSDKVersion >= Build.VERSION_CODES.KITKAT_WATCH){

                //Share Intent
                Intent shareIntent = createShareIntent(type, notificationUrl, notificationTitle);
                PendingIntent sharePendingIntent = PendingIntent.getBroadcast(this, notificationId, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                Notification.Action action = new Notification.Action.Builder(R.drawable.ic_share_grey600_24dp, "立即分享", sharePendingIntent).build();
                if(action!=null){
                    builder.addAction(action);
                }

            }

            if(isSoundOpen){
                builder.setSound(sound);
            }

            if(isVibrateOpen){
                builder.setVibrate(vibratepattern);
            }

            if (currentSDKVersion >= Build.VERSION_CODES.LOLLIPOP) {
                //Api Level 21 (5.0)以上
                //棒棒糖才有鎖屏通知
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            setBigStyleNotification(type, newsTitle, newsSummary, builder, notificationId);
        } else {
            //Api Level 16 (4.1)以下不包含4.1
            setNormalNotification(newsTitle, newsSummary, pendingIntent, vibratepattern, sound, notificationId);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setNormalNotification(String newsTitle, String newsSummary, PendingIntent pendingIntent, long[] vibratepattern, Uri sound, int notificationId) {
        Notification notification;
        int currentSDKVersion = UserDataInfo.getSdkVersion();
        if (currentSDKVersion < Build.VERSION_CODES.HONEYCOMB) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(newsTitle)
                    .setContentTitle(newsTitle)
                    .setContentText(newsSummary)
                    .setContentIntent(pendingIntent);

            if(isSoundOpen){
                builder.setSound(sound);
            }

            if(isVibrateOpen){
                builder.setVibrate(vibratepattern);
            }

            notification = builder.build();

        } else {
            Builder builder = new Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(newsTitle)
                    .setContentTitle(newsTitle)
                    .setContentText(newsSummary)
                    .setContentIntent(pendingIntent);

            if(isSoundOpen){
                builder.setSound(sound);
            }

            if(isVibrateOpen){
                builder.setVibrate(vibratepattern);
            }

            notification = builder.build();

        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(notificationId != -1? notificationId:0 , notification);
    }

    private String mImageUrl;
    private BitmapController mBitmapController;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBigStyleNotification(String type, String newsTitle, final String newsSummary, final Builder builder, final int notificationId) {

        builder.setSmallIcon(R.drawable.now);

        if (type.equalsIgnoreCase("news")) {
            builder.setSubText(getString(R.string.news));
        } else if (type.equalsIgnoreCase("album")) {
            builder.setSubText(getString(R.string.album));
        }

        if (newsTitle != null) {
            builder.setContentTitle(newsTitle);
            builder.setTicker(newsSummary);
        }

        if (newsSummary != null) {
            builder.setContentText(newsSummary);
        }

        mImageUrl = mNotificationInfo.image;
        if (mImageUrl == null || (mImageUrl != null && mImageUrl.isEmpty())) {
            //設定大圖
            Notification notification = builder.build();
            mNotificationManager.notify(notificationId != -1? notificationId:0 , notification);
        } else {
            mBitmapController = BitmapController.getInstance(this);
            mImageUrl = Utility.getSrcFromImgapi(mImageUrl);
            mImageLoadingListener = new ImageLoadingListener() {

                @Override
                public void onProgressUpdate(String aImageUrl, int aProgress, int max) {
                }

                @Override
                public void onLoadingStart(String aImageUrl, View aView) {
                }

                @Override
                public void onLoadingFailed(String aImageUrl, View aView, Exception aException) {
                    Notification notification = builder.build();
                    mNotificationManager.notify(0, notification);
                }

                @Override
                public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {

                    Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle();
                    bigPictureStyle.bigPicture(aBitmap);
                    bigPictureStyle.setSummaryText(newsSummary);
                    builder.setStyle(bigPictureStyle);

                    Notification notification = builder.build();
                    mNotificationManager.notify(notificationId != -1? notificationId:0 , notification);
                }

                @Override
                public void onLoadingCancelled() {
                }
            };
//            final String url = String.format(WebAPIUrl.SCALE_IMAGE, "180", "", 50, mImageUrl);
//            mBitmapController.loadImageWithOriginalSize(url, null, BitmapController.IMAGE_SRC, 0, 0, mImageLoadingListener);
            mBitmapController.loadImageWithOriginalSize(mImageUrl, null, BitmapController.IMAGE_SRC, 0, 0, mImageLoadingListener);
        }
    }

    private Intent createNotificationGoWhere(String type, String notificationUrl, int notificationId) {
        Intent intent = null;

        if (Utility.DEBUG && type != null) Log.d(TAG, "type: " + type);
        if (Utility.DEBUG && notificationUrl != null) Log.d(TAG, "url: " + notificationUrl);
        if (Utility.DEBUG && notificationId != -1) Log.d(TAG, "notificationId: " + notificationId);

        if (type.equalsIgnoreCase("news")) {
            if(Utility.DEBUG)Log.w(TAG, "news!!!!!");
            intent = new Intent();
            intent.setClass(this, NewsPage.class);
            intent.putExtra(NewsPage.KEY_NEWS_ID, (mNotificationInfo.id.equals("") ? notificationId : mNotificationInfo.id));
            intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_SINGAL_NEWS);
            intent.putExtra(NewsPage.KEY_NEWS_BIG_CATEGORY, getString(R.string.cloud_message));
            intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, getString(R.string.cloud_message_click));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            UserDataInfo.isSingalNewsFromAction = true;
        } else if (type.equalsIgnoreCase("album")) {
            if(Utility.DEBUG)Log.w(TAG, "album!!!!!");
            intent = new Intent();
            intent.setClass(this, AlbumPage.class);
            intent.putExtra(AlbumPage.KEY_ALBUM_ID, (mNotificationInfo.id.equals("") ? notificationId : mNotificationInfo.id));
            intent.putExtra(AlbumPage.KEY_FROM_WHERE, GcmIntentService.this.getClass().getSimpleName());
        } else if (type.equalsIgnoreCase("normal")) {
            if(Utility.DEBUG)Log.w(TAG, "normal!!!!!");
            intent = new Intent();
            intent.setClass(this, GcmDialog.class);
            intent.putExtra(GcmDialog.KEY_TITLE, mNotificationInfo.title);
            intent.putExtra(GcmDialog.KEY_SUMMARY, mNotificationInfo.summary);
            intent.putExtra(GcmDialog.KEY_URL, mNotificationInfo.url);
            intent.putExtra(GcmDialog.KEY_FROM_WHERE, GcmIntentService.this.getClass().getSimpleName());
        }
        String action = null;
        if(mNotificationInfo.title!=null){
            action = mNotificationInfo.title;
        }
        if(notificationUrl!=null){
            action = action + " " + notificationUrl;
        }
        GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.cloud_message), getString(R.string.cloud_message_reveive), action);

        return intent;
    }

    private Intent createShareIntent(String type, String notificationUrl, String notificationTitle) {
        Intent intent = new Intent();
        intent.setClass(this, GcmShareAppReceiver.class);
        intent.putExtra(GcmShareAppReceiver.NOTIFICATION_TYPE, type);
        intent.putExtra(GcmShareAppReceiver.NOTIFICATION_URL, notificationUrl);
        intent.putExtra(GcmShareAppReceiver.NOTIFICATION_TITLE, notificationTitle);
        return intent;
    }

}

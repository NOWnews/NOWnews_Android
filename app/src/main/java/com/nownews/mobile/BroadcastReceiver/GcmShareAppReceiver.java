package com.nownews.mobile.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.Utility;

public class GcmShareAppReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String NOTIFICATION_URL = "notification_url";
    public static final String NOTIFICATION_TITLE = "notification_title";

    private String mNotificationType;
    private String mNotificationUrl;
    private String mNotificationTitle;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        processIntent(intent);
        openShareAppIntent();

    }

    private void processIntent(Intent intent){

        mNotificationType = intent.getStringExtra(NOTIFICATION_TYPE);
        mNotificationUrl = intent.getStringExtra(NOTIFICATION_URL);
        mNotificationTitle = intent.getStringExtra(NOTIFICATION_TITLE);

    }

    private void openShareAppIntent(){

        Utility.ShareType shareType = null;

        if (mNotificationType.equalsIgnoreCase("news")) {
            if(Utility.DEBUG) Log.w(TAG, "news!!!!!");
            shareType = Utility.ShareType.news;
        } else if (mNotificationType.equalsIgnoreCase("album")) {
            if(Utility.DEBUG)Log.w(TAG, "album!!!!!");
            shareType = Utility.ShareType.photo;
        } else if (mNotificationType.equalsIgnoreCase("normal")) {
            if(Utility.DEBUG)Log.w(TAG, "normal!!!!!");
            shareType = Utility.ShareType.normal;
        }

        String shareMessage = Utility.getShareMessage(mContext, mNotificationUrl, mNotificationTitle, shareType);
        Intent intent = Utility.getShareIntent(mContext, shareMessage);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        String action = null;
        if(mNotificationTitle!=null){
            action = mNotificationTitle;
        }
        if(mNotificationUrl!=null){
            action = action + " " + mNotificationUrl;
        }
        GoogleAnalyticsFunction.sendHitInfo(mContext, mContext.getString(R.string.cloud_message), mContext.getString(R.string.cloud_message_click_share_news), action);

    }

}

package com.InstallationTracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.NownewsApplication;

public class CustomCampaignTrackingReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();
    private Tracker mTracker;

    @Override
    public void onReceive(Context context, Intent intent) {

        /**
         * https://play.google.com/store/apps/details?id=com.nownews&referrer=utm_source=employee&utm_medium=personal_url&utm_campaign=personal_referral_link_competition&utm_term=1liwei
         * */

        Log.i(TAG, "intent: " + intent);
        String playStoreUrl = "https://play.google.com/store/apps/details?id=com.nownews&referrer=";
        String referrer = intent.getStringExtra("referrer");
        Log.i(TAG, "intent referrer: " + referrer);
        mTracker = GoogleAnalyticsFunction.getTracker(GoogleAnalyticsFunction.TrackerName.APP_TRACKER);
        mTracker.send(new HitBuilders.ScreenViewBuilder()
        .setCampaignParamsFromUrl(playStoreUrl+referrer)
        .build());

    }
}

package com.nownews.mobile.Common;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class GoogleAnalyticsFunction {

    private final static String TAG = "GoogleAnalyticsFunction";
    private final static String GA_CODE = "UA-4021556-53";
    private static Tracker mTracker;
    private static HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    private static Tracker getTracker(Context aContext, TrackerName aTrackerId) {

        if(aContext==null){
            return getTracker(aTrackerId);
        }

        if (!mTrackers.containsKey(aTrackerId)) {

            Utility.processScreenSize(aContext);

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(aContext);
            analytics.setLocalDispatchPeriod(1800);

            Tracker tracker = analytics.newTracker(GA_CODE);
    		tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
    		tracker.enableAutoActivityTracking(true);
            tracker.setAppVersion(Utility.getAppVersionName(aContext));
            tracker.setScreenResolution(Utility.getScreenWidth(aContext), Utility.getScreenHeight(aContext));

            mTrackers.put(aTrackerId, tracker);

        }
        return mTrackers.get(aTrackerId);
    }

    public static Tracker getTracker(TrackerName aTrackerId){
        if(mTrackers!=null && mTrackers.containsKey(aTrackerId)){
            return mTrackers.get(aTrackerId);
        }else{
            return null;
        }
    }

    public static void setScreenName(Context aContext, String aScreenName) {
        Tracker tracker = getTracker(aContext, TrackerName.APP_TRACKER);
        tracker.setScreenName(aScreenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendHitInfo(Context aContext, String aCategory,
                                   String aAction, String aLabel) {
        if(Utility.DEBUG)Log.d(TAG, "aCategory: " + aCategory);
        if(Utility.DEBUG)Log.d(TAG, "aAction: " + aAction);
        if(Utility.DEBUG)Log.d(TAG, "aLabel: " + aLabel);
        Tracker tracker = getTracker(aContext, TrackerName.APP_TRACKER);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(aCategory)
                .setAction(aAction)
                .setLabel(aLabel)
                .build());
    }

    public static void sendSocialInteractions(Context aContext, String aSocialNetwork,
                                              String aSocialAction, String aTarget) {
        Tracker tracker = getTracker(aContext, TrackerName.APP_TRACKER);
        tracker.send(new HitBuilders.SocialBuilder()
                .setNetwork(aSocialNetwork)
                .setAction(aSocialAction)
                .setTarget(aTarget)
                .build());
    }

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

}

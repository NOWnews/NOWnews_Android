package com.nownews.mobile;

import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.cmcm.adsdk.CMAdManager;
import com.comscore.Analytics;
import com.comscore.PublisherConfiguration;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.nownews.R;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.AppController;

public class NownewsApplication extends MultiDexApplication {

    private final String TAG = getClass().getSimpleName();
    private AppController mAppController;

    @Override
    public void onCreate() {
        super.onCreate();

        Utility.setApplicationContext(this);

        if (Utility.DEBUG) Log.e(TAG, TAG + " in");
        new Thread(new Runnable() {
            @Override
            public void run() {

                String advertisingId = Utility.getAdvertisingId(NownewsApplication.this);
                if (Utility.DEBUG)Log.d(TAG, "advertisingId: " + advertisingId);

                String keyHashs = Utility.getKeyHash(NownewsApplication.this);
                if (Utility.DEBUG)Log.d(TAG, "keyHashs: " + keyHashs);

            }
        }).start();

        mAppController = AppController.getInstance(this);
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        //雪豹
        CMAdManager.applicationInit(this, getString(R.string.ileopard_mid));

        //comscore
        PublisherConfiguration myPublisherConfig = new PublisherConfiguration.Builder()
                .publisherId(getString(R.string.comscore_publisher_id))
                .publisherSecret(getString(R.string.comscore_publisher_secret))
                .build();
        Analytics.getConfiguration().addClient(myPublisherConfig);
        Analytics.start(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

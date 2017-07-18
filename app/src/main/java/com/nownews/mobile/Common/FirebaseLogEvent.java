package com.nownews.mobile.Common;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * Created by cindy on 2017/7/4.
 */

public class FirebaseLogEvent {

    private FirebaseAnalytics mFirebaseAnalytics;

    private void test(Context aContext){

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(aContext);

//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
//        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}

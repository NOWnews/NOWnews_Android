package com.nownews.mobile.GCM;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nownews.mobile.Common.Utility;

public class GcmIDListenerService extends FirebaseInstanceIdService {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onTokenRefresh() {
        if (Utility.DEBUG) Log.v(TAG, "GcmIDListenerService onTokenRefresh()");
        String newToken = FirebaseInstanceId.getInstance().getToken();
        Utility.processGCMRegisterId(getApplicationContext(), newToken);
        super.onTokenRefresh();
    }

}

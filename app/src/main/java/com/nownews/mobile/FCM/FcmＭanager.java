package com.nownews.mobile.FCM;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.NewHome;

/**
 * Created by cindy on 2016/10/12.
 */

public class FcmＭanager {

    public final static int SHOW_NEW_FUNCTION = 0x164;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = getClass().getSimpleName();
    private NewHome mContext;
    private SharedPreferencesMethods mSharedPref;

    public FcmＭanager(NewHome aContext) {
        this.mContext = aContext;
        this.mSharedPref = new SharedPreferencesMethods(this.mContext);
    }

    public void startGCM() {
        boolean isAlreadyAskOpenNotification = this.mSharedPref.getAskOpenNotificationStatus();
        if (!isAlreadyAskOpenNotification) {
            //Ask User to open notification
            this.mContext.openFCM();
            return;
        }

        if(this.mSharedPref != null) {
            this.mSharedPref.unRegistContext(mContext);
        }
    }

    /**
     * 檢查是否支援GooglePlayServices
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity) mContext, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public String getToken(){
        String token = null;
        try {
            token = FirebaseInstanceId.getInstance().getToken();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return token;
    }


}

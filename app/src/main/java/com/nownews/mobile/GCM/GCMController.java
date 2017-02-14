package com.nownews.mobile.GCM;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.Utility;

/**
 * Created by cindy on 2016/10/12.
 */

public class GCMController {

    public final static int SHOW_NEW_FUNCTION = 0x164;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private Handler mUiHandler;
    private SharedPreferencesMethods mSharedPref;

    public GCMController(Context aContext, Handler aUiHandler) {
        mContext = aContext;
        mUiHandler = aUiHandler;
        mSharedPref = new SharedPreferencesMethods(mContext);
    }

    public void startGCM() {

        boolean isAlreadyAskOpenNotification = mSharedPref.getAskOpenNotificationStatus();
        if (!isAlreadyAskOpenNotification) {
            //Ask User to open notification
            if (mUiHandler != null) {
                mUiHandler.sendEmptyMessage(SHOW_NEW_FUNCTION);
            }
            return;
        }

        if(mSharedPref!=null){
            mSharedPref.unRegistContext(mContext);
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


}

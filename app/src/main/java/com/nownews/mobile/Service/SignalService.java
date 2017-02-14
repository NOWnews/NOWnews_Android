package com.nownews.mobile.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nownews.mobile.Common.Utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SignalService extends Service {

    private final String TAG = getClass().getSimpleName();
    private PhoneStateListener mPhoneStateListner = new PhoneStateListener() {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (signalStrength.isGsm()) {
                if (Utility.DEBUG)
                    Log.d(TAG, "Sign Strength: " + signalStrength.getGsmSignalStrength());
            } else if (signalStrength.getCdmaDbm() > 0) {
                if (Utility.DEBUG) Log.d(TAG, "Cdma Dbm: " + signalStrength.getCdmaDbm() + " dbm");
                if (Utility.DEBUG) Log.d(TAG, "Cdma Ecio: " + signalStrength.getCdmaEcio());
            } else {
                if (Utility.DEBUG) Log.d(TAG, "Evdo Dbm: " + signalStrength.getEvdoDbm() + " dbm");
                if (Utility.DEBUG) Log.d(TAG, "Evdo Ecio: " + signalStrength.getEvdoEcio());
                if (Utility.DEBUG) Log.d(TAG, "Evdo Snr: " + signalStrength.getEvdoSnr());
            }

            // Reflection code starts from here
            try {
                Method[] methods = SignalStrength.class.getMethods();
                for (Method mthd : methods) {
                    if (mthd.getName().equals("getLteSignalStrength")
                            || mthd.getName().equals("getLteRsrp")
                            || mthd.getName().equals("getLteRsrq")
                            || mthd.getName().equals("getLteRssnr")
                            || mthd.getName().equals("getLteCqi")) {
                        if (Utility.DEBUG)
                            Log.i(TAG, "onSignalStrengthsChanged: " + mthd.getName() + " " + mthd.invoke(signalStrength));
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListner, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        return super.onStartCommand(intent, flags, startId);
    }

}

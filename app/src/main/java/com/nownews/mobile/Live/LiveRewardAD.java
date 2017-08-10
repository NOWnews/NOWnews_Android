package com.nownews.mobile.Live;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.core.adnsdk.AdObject;
import com.core.adnsdk.AdProfile;
import com.core.adnsdk.AdReward;
import com.core.adnsdk.AdRewardListener;
import com.core.adnsdk.AdRewardType;
import com.core.adnsdk.AuthList;
import com.core.adnsdk.AuthListBuilder;
import com.core.adnsdk.ErrorMessage;
import com.nownews.mobile.Common.Utility;

public class LiveRewardAD extends Fragment {

    private final String TAG = getClass().getSimpleName();
//    private String mApiKey = "5630c874cef2370b13942b8f";
//    private String mPlacementName = "placement(reward_video)";
    private String mApiKey = "58d9dc63a42145d817e097fd";
    private String mPlacementName = "58d9dd95a42145d817e09801";
    private boolean mTestMode = false;
    private AdReward mAdReward;
    private static AdRewardListener mAdRewardListener;

    public static Fragment getInstance(AdRewardListener aAdRewardListener) {
        mAdRewardListener = aAdRewardListener;
        return new LiveRewardAD();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Utility.DEBUG)Log.d(TAG, "onCreate");

        AuthList mAuthList = new AuthListBuilder()
                .add(mApiKey, mPlacementName)
                .build();

        AdProfile mAdProfile = new AdProfile.AdProfileBuilder()
                .setAuthList(mAuthList)
                .setTestMode(mTestMode)
                .build();

        mAdReward = new AdReward(getActivity(), mAdProfile, AdRewardType.REWARD);
        if(mAdRewardListener!=null){
            mAdReward.setAdListener(mAdRewardListener);
        }
        mAdReward.setTestMode(mTestMode);
        mAdReward.loadAd();

    }

    @Override
    public void onResume() {
        if (mAdReward != null) {
            mAdReward.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mAdReward != null) {
            mAdReward.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdReward != null) {
            mAdReward.onDestroy();
            mAdReward = null;
        }
        super.onDestroy();
    }

    public void showAD() {
        if(mAdReward != null){
            mAdReward.showAd();
        }
    }

}

package com.nownews.mobile.Invite;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by cindy on 2017/3/3.
 */

public class InviteCodeInputPage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = getClass().getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, TAG + "onCreate");
        initAppInvite();

    }

    private void initAppInvite(){
        Log.v(TAG, TAG + "initAppInvite");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult result) {

                        if(result.getStatus().isSuccess()){
                            Intent intent = result.getInvitationIntent();
                            String deepLink = AppInviteReferral.getDeepLink(intent);
                            String invitationId = AppInviteReferral.getInvitationId(intent);
                            Log.e(TAG, "deepLink: " + deepLink);
                            Log.e(TAG, "invitationId: " + invitationId);
                        }else{
                            Log.e(TAG, "getInvitation: no deep link found.");
                        }

                    }
                });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }
}

package com.nownews.mobile.GCM;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.afollestad.materialdialogs.Theme;
import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Widget.WebActivity;

public class GcmDialog extends AppCompatActivity {

    public final static String KEY_TITLE = "title";
    public final static String KEY_SUMMARY = "summary";
    public final static String KEY_URL = "url";
    public static final String KEY_FROM_WHERE = "fromWhere";
    private final String TAG = getClass().getSimpleName();
    private String mTitle;
    private String mSummary;
    private String mUrl;
    private String mFromWhere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processIntent();
        openDialog();

    }

    private void processIntent() {

        Intent intent = getIntent();
        if (intent != null) {
            mTitle = intent.getStringExtra(KEY_TITLE);
            mSummary = intent.getStringExtra(KEY_SUMMARY);
            mUrl = intent.getStringExtra(KEY_URL);
            mFromWhere = intent.getStringExtra(KEY_FROM_WHERE);
            if (Utility.DEBUG) Log.d(TAG, "mTitle: " + mTitle);
            if (Utility.DEBUG) Log.d(TAG, "mSummary: " + mSummary);
            if (Utility.DEBUG) Log.d(TAG, "mUrl: " + mUrl);
            if (Utility.DEBUG) Log.d(TAG, "mFromWhere: " + mFromWhere);
        }
        if(mFromWhere!=null && mFromWhere.equals("GcmIntentService")){
            String action = null;
            if(mTitle!=null){
                action = mTitle;
            }
            if(mUrl!=null){
                action = action + " " + mUrl;
            }
            GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.cloud_message), getString(R.string.cloud_message_click), action);
        }

    }

    private void openDialog() {

//		Toast.makeText(this, "normal", Toast.LENGTH_SHORT).show();
        Drawable icon = getResources().getDrawable(R.mipmap.ic_launcher);
        new MaterialDialog.Builder(this)
                .icon(icon)
                .positiveText(mUrl==null? R.string.understand:R.string.gcm_dialog_lets_go)
                .title(mTitle)
                .content(mSummary)
                .theme(Theme.LIGHT)
                .callback(new ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        if(mUrl!=null){
                            Intent intent = new Intent();
                            intent.setClass(GcmDialog.this, WebActivity.class);
                            intent.putExtra(WebActivity.KEY_URL, mUrl);
                            startActivity(intent);
                        }

                        dialog.cancel();
                        super.onPositive(dialog);
                    }

                })
                .cancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                        finish();

                    }
                }).show();

    }

    @Override
    protected void onResume() {
        UserDataInfo.activityResumed(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);
        super.onDestroy();
    }
}

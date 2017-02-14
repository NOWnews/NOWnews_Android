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
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;

public class GcmDialog extends AppCompatActivity {

    public final static String KEY_TITLE = "title";
    public final static String KEY_SUMMARY = "summary";
    private final String TAG = getClass().getSimpleName();
    private String mTitle;
    private String mSummary;

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
            if (Utility.DEBUG) Log.d(TAG, "mTitle: " + mTitle);
            if (Utility.DEBUG) Log.d(TAG, "mSummary: " + mSummary);
        }

    }

    private void openDialog() {

//		Toast.makeText(this, "normal", Toast.LENGTH_SHORT).show();
        Drawable icon = getResources().getDrawable(R.mipmap.ic_launcher);
        new MaterialDialog.Builder(this)
                .icon(icon)
                .positiveText(R.string.understand)
                .title(mTitle)
                .content(mSummary)
                .theme(Theme.LIGHT)
                .callback(new ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {
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

package com.nownews.mobile.Widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.nownews.R;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.Live.LivePlayer;
import com.nownews.mobile.NewHome;

import java.util.List;

/**
 * Created by cindy on 2016/12/19.
 */

public class CSTVDownloadDialog extends Dialog {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private RelativeLayout vRootLayout;
    private RelativeLayout vBody;
    private TextView vMessage;
    private Button vWatchNow;
    private Button vDownloadNow;
    private CardView vCardViewGroup;
    private LinearLayout vButtonsGroup;
    private ImageView vIcon;

    private ReSizeLayoutParams mResize;

    private String mTitle;
    private List<LiveListJson.Data> mLiveList;
    private String mPath;
    private int mCategoryIndex;
    private int mChannelIndex;
    private boolean isCountdownType;

    public CSTVDownloadDialog(Context context, String aTitle, List<LiveListJson.Data> aLiveList, String aPath, int aCategoryIndex, int aChannelIndex) {
        super(context, R.style.FullScreenDialogStyle);

        mTitle = aTitle;
        mLiveList = aLiveList;
        mPath = aPath;
        mCategoryIndex = aCategoryIndex;
        mChannelIndex = aChannelIndex;
        if(mChannelIndex==-1){
            isCountdownType = true;
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mContext = context;
        setContentView(R.layout.dialog_upgrade);
    }

    public CSTVDownloadDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected CSTVDownloadDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initController();
        processView();
        processListener();
        checkTime();
        checkShareAppStatus();
        if(isCountdownType){
            vMessage.setText(mContext.getString(R.string.cstv_download_hint2));
            startCountdownTimer();
        }

    }

    private boolean isShareAppSuccess;
    private void checkShareAppStatus(){
        isShareAppSuccess = sharedPreferencesMethods.isShareAppSuccess();
        if(isShareAppSuccess){
            isCountdownType = false;
        }
    }

    private SharedPreferencesMethods sharedPreferencesMethods;
    private long stopWatchingTime;
    private long enableWatchingTime;
    private void initController(){
        sharedPreferencesMethods = new SharedPreferencesMethods(mContext);
    }

//    private final int mIntervalHours = 2;
    private final int mIntervalMinutes = 30;
    private void checkTime(){

        stopWatchingTime = sharedPreferencesMethods.getLiveStopWatchingTime();
        Log.e(TAG, "stopWatchingTime: " + stopWatchingTime);
        if(stopWatchingTime==-1){
            return;
        }
        long currentTime = System.currentTimeMillis();
        long spentTime = currentTime - stopWatchingTime;
        long minutes = (spentTime/1000)/60;
        Log.e(TAG, "currentTime: " + currentTime);
        Log.e(TAG, "spentTime: " + spentTime);
        Log.e(TAG, "minutes: " + minutes);
//        if(minutes>=mIntervalHours*60){
        if(minutes>=mIntervalMinutes){ // for test
            isCountdownType = false;
        }else{
            isCountdownType = true;
        }

    }

    private Handler mCountdownTimer = new Handler();
    private void startCountdownTimer(){

//        enableWatchingTime = stopWatchingTime + (mIntervalHours*60*60*1000);
        enableWatchingTime = stopWatchingTime + (mIntervalMinutes*60*1000); // for test
        Log.e(TAG, "enableWatchingTime: " + enableWatchingTime);
        if(mCountdownTimer!=null){
            mCountdownTimer.removeCallbacks(mCountdownRunnalbe);
            mCountdownTimer.post(mCountdownRunnalbe);
        }

    }

    private Runnable mCountdownRunnalbe = new Runnable() {
        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();
            long leftTime = Math.abs(currentTime - enableWatchingTime);
            long minutes = (leftTime/1000)/60;
            long seconds = (leftTime/1000)%60;
            if(minutes>0 || (minutes==0 && seconds>0)){
                if(Utility.DEBUG)Log.d(TAG, "時間剩下: " + (minutes<10? "0"+minutes:minutes) + ":" +(seconds<10? "0"+seconds:seconds));
                vMessage.setText(mContext.getString(R.string.cstv_download_hint2) + "\n距離下次收看還剩下: " + (minutes<10? "0"+minutes:minutes) + ":" +(seconds<10? "0"+seconds:seconds));
                vWatchNow.setText(mContext.getString(R.string.click_me_share));
                vWatchNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Utility.shareApp(mContext);

                    }
                });
                mCountdownTimer.postDelayed(this, 1000);
            }else{
                if(Utility.DEBUG)Log.e(TAG, "時間到!!");
                vWatchNow.setEnabled(true);
                vWatchNow.setText(mContext.getString(R.string.cstv_watch_now));
            }

        }
    };

    private void processView(){

        mResize = new ReSizeLayoutParams(mContext);

        vRootLayout = (RelativeLayout)findViewById(R.id.root_layout);
        vRootLayout.setLayoutParams(mResize.setOnSize(vRootLayout, 0, 0, 0, 0));

        vBody = (RelativeLayout)findViewById(R.id.body);

        vCardViewGroup = (CardView)findViewById(R.id.cardview_group);
        vCardViewGroup.setLayoutParams(mResize.setOnSize(vCardViewGroup, 0, 0, 0, 0));

        vMessage = (TextView)findViewById(R.id.message);
        vMessage.setLayoutParams(mResize.setMargins(vMessage, 0, 0, 0, 10));
        mResize.setTextSize(vMessage);

        vButtonsGroup = (LinearLayout)findViewById(R.id.buttons_group);
        vButtonsGroup.setLayoutParams(mResize.setMargins(vButtonsGroup, 10, 10, 10, 10));

        vWatchNow = (Button)findViewById(R.id.watch_now);
        vWatchNow.setLayoutParams(mResize.setOnSize(vWatchNow, 0, 0, 0, 0));
        mResize.setTextSize(vWatchNow);

        vDownloadNow = (Button)findViewById(R.id.download_now);
        vDownloadNow.setLayoutParams(mResize.setOnSize(vDownloadNow, 10, 0, 0, 0));
        mResize.setTextSize(vDownloadNow);

        vIcon = (ImageView)findViewById(R.id.cstv_icon);
        mResize.setPadding(vIcon, 20, 20, 20, 10);


    }

    private void processListener(){

        vRootLayout.setOnClickListener(mRootLayoutClickListener);
        vWatchNow.setOnClickListener(mWatchNowClickListener);
        vDownloadNow.setOnClickListener(mDownloadNowClickListener);
        vBody.setOnClickListener(mBodyClickListener);

    }

    private View.OnClickListener mBodyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            return;

        }
    };

    private View.OnClickListener mDownloadNowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent MyIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.csmuse.dvbt.nownews.mobile"));
            mContext.startActivity(MyIntent);
            dismiss();

        }
    };

    private View.OnClickListener mRootLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dismiss();

        }
    };

    private View.OnClickListener mWatchNowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Log.d(TAG, "mWatchNowClickListener");

            if(Utility.DEBUG)Log.v(TAG, "mTitle: " + mTitle);
            UserDataInfo.setLiveList(mLiveList);
            Intent intent = new Intent();
            intent.setClass(mContext, LivePlayer.class);
            intent.putExtra(LivePlayer.KEY_PLAY_URL, mPath);
            intent.putExtra(LivePlayer.KEY_CATEGORY_INDEX, mCategoryIndex);
            intent.putExtra(LivePlayer.KEY_CHANNEL_INDEX, mChannelIndex);
            ((NewHome)mContext).startActivityForResult(intent, NewHome.RESULT_CODE_FROM_LIVE);

            dismiss();

        }
    };

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        super.setOnCancelListener(listener);
        destroy();
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);
        destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        destroy();
    }

    private void destroy(){
        if(mCountdownTimer!=null){
            mCountdownTimer.removeCallbacks(mCountdownRunnalbe);
        }
        if(sharedPreferencesMethods!=null){
            sharedPreferencesMethods.unRegistContext(mContext);
        }
    }
}

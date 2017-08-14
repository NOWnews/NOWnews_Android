package com.nownews.mobile.Live;

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

import com.nownews.R;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.Live.LivePlayer;
import com.nownews.mobile.NewHome;

import java.util.List;

/**
 * Created by cindy on 2016/12/19.
 */

public class LiveAppDownloadDialog extends Dialog {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private RelativeLayout vRootLayout;
    private RelativeLayout vBody;
    private TextView vMessage;
    private Button vWatchNow;
    private Button vDownloadNow;
    private Button vOnlyOneButton;
    private CardView vCardViewGroup;
    private LinearLayout vButtonsGroup;
    private ImageView vIcon;

    private ReSizeLayoutParams mResize;
    private BitmapController mBitmapController;

    private String mTitle;
    private List<LiveListJson.DataBean> mLiveList;
    private String mPath;
    private int mCategoryIndex;
    private int mChannelIndex;
    private int mWatchTime;
    private int mLockTime;
    private String mIconUrl;
    private String mTitleMessage;
    private boolean downloadable;
    private boolean watchable;
    private String downloadLink;
    private LiveListJson mLiveInfoJson;
    private boolean isCountdownType;
    private boolean videoAD;
    private String rightButton;
    private String leftButton;

    public LiveAppDownloadDialog(Context context, String aTitle, List<LiveListJson.DataBean> aLiveList, String aPath,
                                 int aCategoryIndex, int aChannelIndex, LiveListJson aLiveInfoJson) {
        super(context, R.style.FullScreenDialogStyle);

        mTitle = aTitle;
        mLiveList = aLiveList;
        mPath = aPath;
        mCategoryIndex = aCategoryIndex;
        mChannelIndex = aChannelIndex;
        mLiveInfoJson = aLiveInfoJson;
        if(mLiveInfoJson!=null){
            mWatchTime = mLiveInfoJson.getLiveInfo().getWatchTime();
            mLockTime = mLiveInfoJson.getLiveInfo().getLockTime();
            mIconUrl = mLiveInfoJson.getLiveInfo().getIcon();
            mTitleMessage = mLiveInfoJson.getLiveInfo().getTitleMessage();
            watchable = mLiveInfoJson.getLiveInfo().isWatchable();
            downloadLink = mLiveInfoJson.getLiveInfo().getAndroidDownloadLink();
            videoAD = mLiveInfoJson.getLiveInfo().isVideoAD();
            leftButton = mLiveInfoJson.getLiveInfo().getLeftbutton();
            rightButton = mLiveInfoJson.getLiveInfo().getRightbutton();
        }
        if(mChannelIndex==-1){
            isCountdownType = true;
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mContext = context;
        setContentView(R.layout.dialog_upgrade);
    }

    public LiveAppDownloadDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected LiveAppDownloadDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
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
            vMessage.setText(mContext.getString(R.string.download_hint2));
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
        mBitmapController = BitmapController.getInstance(mContext);
    }

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
//        if(minutes>=mIntervalHours*60){ // for test
        if(minutes>=mLockTime){
            isCountdownType = false;
        }else{
            isCountdownType = true;
        }

    }

    private Handler mCountdownTimer = new Handler();
    private void startCountdownTimer(){

//        enableWatchingTime = stopWatchingTime + (mIntervalHours*60*60*1000);
        enableWatchingTime = stopWatchingTime + (mLockTime*60*1000); // for test
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
                vMessage.setText(String.format(mContext.getString(R.string.download_hint2), mLockTime) + "\n距離下次收看還剩下: " + (minutes<10? "0"+minutes:minutes) + ":" +(seconds<10? "0"+seconds:seconds));

                if(watchable && downloadable && !downloadLink.trim().isEmpty()){
                    vButtonsGroup.setVisibility(View.VISIBLE);
                    vOnlyOneButton.setVisibility(View.GONE);
                    vWatchNow.setText(mContext.getString(R.string.click_me_share));
                    vDownloadNow.setText(mContext.getString(R.string.download_now));
                    vWatchNow.setEnabled(true);
                    vWatchNow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Utility.shareApp(mContext);

                        }
                    });
                }else if((watchable && downloadable && downloadLink.trim().isEmpty()) || (watchable && !downloadable)){
                    vButtonsGroup.setVisibility(View.GONE);
                    vOnlyOneButton.setVisibility(View.VISIBLE);
                    vOnlyOneButton.setText(mContext.getString(R.string.click_me_share));
                    vOnlyOneButton.setOnClickListener(mWatchNowClickListener);
                    vOnlyOneButton.setEnabled(true);
                    vOnlyOneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Utility.shareApp(mContext);

                        }
                    });
                }

                mCountdownTimer.postDelayed(this, 1000);
            }else{
                if(Utility.DEBUG)Log.e(TAG, "時間到!!");
                vMessage.setText(mTitleMessage);
                processListener();
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
        vMessage.setText(mTitleMessage);
//        vMessage.setText("本服務由天暢國際股份有限公司提供\n\n線上客服請搜尋\nLINE/wechat ID：nowlink_cs");

        vButtonsGroup = (LinearLayout)findViewById(R.id.buttons_group);
        vButtonsGroup.setLayoutParams(mResize.setMargins(vButtonsGroup, 10, 10, 10, 10));

        vWatchNow = (Button)findViewById(R.id.watch_now);
        vWatchNow.setLayoutParams(mResize.setOnSize(vWatchNow, 0, 0, 0, 0));
        mResize.setTextSize(vWatchNow);

        vDownloadNow = (Button)findViewById(R.id.download_now);
        vDownloadNow.setLayoutParams(mResize.setOnSize(vDownloadNow, 10, 0, 0, 0));
        mResize.setTextSize(vDownloadNow);

        vOnlyOneButton = (Button)findViewById(R.id.only_one_button);
        mResize.setTextSize(vOnlyOneButton);

        vIcon = (ImageView)findViewById(R.id.cstv_icon);
        vIcon.setLayoutParams(mResize.setOnSize(vIcon, 0, 0, 0, 0));
        mResize.setPadding(vIcon, 20, 20, 20, 10);
        if(mBitmapController!=null && mIconUrl!=null && !mIconUrl.trim().isEmpty()){
            mBitmapController.loadImageWithOriginalSize(mIconUrl, vIcon, BitmapController.IMAGE_SRC, 0, 0, null);
        }


    }

    private void processListener(){

        vRootLayout.setOnClickListener(mRootLayoutClickListener);
        vWatchNow.setOnClickListener(mWatchNowClickListener);
        vDownloadNow.setOnClickListener(mDownloadNowClickListener);
        vBody.setOnClickListener(mBodyClickListener);

        if(watchable && downloadable && !downloadLink.trim().isEmpty()){
            vButtonsGroup.setVisibility(View.VISIBLE);
            vOnlyOneButton.setVisibility(View.GONE);
            vWatchNow.setText((rightButton==null? mContext.getString(R.string.watch_now):rightButton));
            vDownloadNow.setText((leftButton==null? mContext.getString(R.string.download_now):leftButton));
            vWatchNow.setEnabled(true);
        }else if((watchable && downloadable && downloadLink.trim().isEmpty()) || (watchable && !downloadable)){
            vButtonsGroup.setVisibility(View.GONE);
            vOnlyOneButton.setVisibility(View.VISIBLE);
            vOnlyOneButton.setText((rightButton==null? mContext.getString(R.string.watch_now):rightButton));
            vOnlyOneButton.setOnClickListener(mWatchNowClickListener);
            vOnlyOneButton.setEnabled(true);
        }else if(!watchable && downloadable && !downloadLink.trim().isEmpty()){
            vButtonsGroup.setVisibility(View.VISIBLE);
            vOnlyOneButton.setVisibility(View.GONE);
            vWatchNow.setText((rightButton==null? mContext.getString(R.string.watch_now):rightButton));
            vDownloadNow.setText((leftButton==null? mContext.getString(R.string.download_now):leftButton));
            vWatchNow.setEnabled(false);
        }else{
            vButtonsGroup.setVisibility(View.GONE);
            vOnlyOneButton.setVisibility(View.VISIBLE);
            vOnlyOneButton.setText((rightButton==null? mContext.getString(R.string.watch_now):rightButton));
            vOnlyOneButton.setEnabled(false);
        }

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
                    Uri.parse(downloadLink));
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
            intent.putExtra(LivePlayer.KEY_WATCH_TIME, mWatchTime);
            intent.putExtra(LivePlayer.KEY_VIDEO_AD, videoAD);
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
        if(mBitmapController!=null){
            mBitmapController.unregistBitmapController(mContext);
        }
    }
}

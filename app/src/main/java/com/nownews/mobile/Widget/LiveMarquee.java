package com.nownews.mobile.Widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.NewHome;

/**
 * TODO: document your custom view class.
 */
public class LiveMarquee extends RelativeLayout {

    private String TAG = getClass().getSimpleName();
    private Context mContext;

    private RelativeLayout vLiveSignal;
    private RelativeLayout vLiveTitleGroup;
    private TextView vLive;
    private TextView vLiveTitle;

    public LiveMarquee(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public LiveMarquee(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public LiveMarquee(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {

        processView();
        processListener();

    }

    private void processView(){
        LayoutInflater.from(mContext).inflate(R.layout.widget_live_marquee, this, true);
        vLiveSignal = (RelativeLayout)findViewById(R.id.live_signal);
        vLiveTitleGroup = (RelativeLayout)findViewById(R.id.live_title_group);
        vLive = (TextView)findViewById(R.id.live);
        vLiveTitle = (TextView)findViewById(R.id.live_title);
    }

    private void processAnimation(){
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_live_shiny);
        animation.reset();
        vLive.clearAnimation();
        vLive.startAnimation(animation);
    }

    private void processListener(){
        this.setOnClickListener(mLiveMarqueeClickListener);
    }

    private OnClickListener mLiveMarqueeClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            if(mLiveUrl!=null && !mLiveUrl.trim().isEmpty()){
                Intent intent = new Intent();
                intent.setClass(mContext, WebActivity.class);
                intent.putExtra(WebActivity.KEY_URL, mLiveUrl);
                ((NewHome)mContext).startActivityForResult(intent, NewHome.RESULT_CODE_FROM_LIVE_BAR);
            }

        }
    };

    public void setIsOnAir(boolean isOnAir){
        if(vLive!=null && isOnAir){
            vLive.setText(mContext.getString(R.string.live_signal));
            vLive.setBackgroundResource(R.drawable.live_signal_background);
            processAnimation();
        }else if(vLive!=null && !isOnAir){
            vLive.setText(mContext.getString(R.string.live_preview));
            vLive.setBackgroundColor(Color.TRANSPARENT);
            vLive.clearAnimation();
        }
    }

    public void setLiveTitle(String aTitle){
        if(vLiveTitle!=null && aTitle!=null && !aTitle.isEmpty()){
            vLiveTitle.setText(aTitle);
        }
    }

    private String mLiveUrl;
    public void setLiveUrl(String aLiveUrl){
        mLiveUrl = aLiveUrl;
        if(Utility.DEBUG)Log.i(TAG, "mLiveUrl: " + mLiveUrl);
    }

    public void setBackgroundUrl(String aBackgroundUrl){
        if(vLiveTitleGroup!=null && aBackgroundUrl!=null && !aBackgroundUrl.trim().isEmpty()){
            final BitmapController bitmapController = BitmapController.getInstance(mContext);
            bitmapController.loadImageWithOriginalSize(aBackgroundUrl, vLiveTitleGroup, BitmapController.IMAGE_SRC, 0, 0, new BitmapController.ImageLoadingListener() {
                @Override
                public void onLoadingStart(String aImageUrl, View aView) { }

                @Override
                public void onLoadingFailed(String aImageUrl, View aView, Exception aException) {
                    bitmapController.unregistBitmapController(mContext);
                }

                @Override
                public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {
                    bitmapController.unregistBitmapController(mContext);
                }

                @Override
                public void onLoadingCancelled() {
                    bitmapController.unregistBitmapController(mContext);
                }

                @Override
                public void onProgressUpdate(String aImageUrl, int aProgress, int max) { }
            });
        }
    }

}

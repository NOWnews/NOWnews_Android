package com.nownews.mobile.Widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;

/**
 * TODO: document your custom view class.
 */
public class LiveMarquee extends RelativeLayout {

    private String TAG = getClass().getSimpleName();
    private Context mContext;

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
        processAnimation();
        processListener();

    }

    private void processView(){
        LayoutInflater.from(mContext).inflate(R.layout.widget_live_marquee, this, true);
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
                mContext.startActivity(intent);
            }

        }
    };

    public void setLiveTitle(String aTitle){
        if(vLiveTitle!=null && aTitle!=null && !aTitle.isEmpty()){
            vLiveTitle.setText(aTitle);
        }
    }

    private String mLiveUrl;
    public void setLiveUrl(String aLiveUrl){
        mLiveUrl = aLiveUrl;
    }

}

package com.nownews.mobile.Common;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;

import static com.nownews.mobile.Common.Utility.DEBUG;

public class ReSizeLayoutParams {

    public final static String TAG = "ReSizeLayoutParams";
    private boolean doResize = false;
    float density;
    private Context context;
    private DisplayMetrics metric;
    private Display display;
    private String subW;
    private String subH;

    public ReSizeLayoutParams(Context context) {
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.display = wm.getDefaultDisplay();
        init();
    }

    public void unregisterCallback(Context c) {
        if (c == context) {
            context = null;
        }
    }

    public void setDoResize(boolean doResize){
        this.doResize = doResize;
    }

    public void init() {

        metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        density = metric.density;

        if (DEBUG) Log.e(TAG, "density: " + density);
        if (DEBUG) Log.e(TAG, "metric.widthPixels: " + metric.widthPixels);
        if (DEBUG) Log.e(TAG, "metric.heightPixels: " + metric.heightPixels);

        float dpWidth = metric.widthPixels / density;
        float dpHeight = metric.heightPixels / density;
        if (DEBUG) Log.e(TAG, "dpWidth: " + dpWidth);
        if (DEBUG) Log.e(TAG, "dpHeight: " + dpHeight);

        float ScaleW;
        float ScaleH;
        if (dpWidth >= dpHeight) {
            //橫的
            ScaleW = (float) dpWidth / 1280f;
            ScaleH = (float) dpHeight / 672f;
        } else {
            //直的 以G3為標準
            ScaleW = (float) dpWidth / 360f;
            ScaleH = (float) dpHeight / 598f;
        }

        if (DEBUG) Log.e(TAG, "ScaleW: " + ScaleW);
        if (DEBUG) Log.e(TAG, "ScaleH: " + ScaleH);

        String strW = df.format(ScaleW);
        String strH = df.format(ScaleH);
        if (DEBUG) Log.e(TAG, "strW: " + strW);
        if (DEBUG) Log.e(TAG, "strH: " + strH);

        subW = strW.substring(0, strW.length() - 1);
        subH = strH.substring(0, strH.length() - 1);
        if (DEBUG) Log.e(TAG, "subW: " + subW);
        if (DEBUG) Log.e(TAG, "subH: " + subH);
        if (DEBUG) Log.e(TAG, "==================================");
    }

    public LayoutParams setOnSize(View v) {

        if(!doResize)return v.getLayoutParams();

        if (DEBUG) Log.d(TAG, "v.getLayoutParams().width: " + v.getLayoutParams().width);
        if (DEBUG) Log.d(TAG, "v.getLayoutParams().height: " + v.getLayoutParams().height);

        LayoutParams params = (LayoutParams) v.getLayoutParams();
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;
        if (width >= 0) {
            int ScaleWidth = (int) (v.getLayoutParams().width * Float.parseFloat(subW));
            params.width = ScaleWidth;
        }
        if (height >= 0) {
            int ScaleHeight = (int) (v.getLayoutParams().height * Float.parseFloat(subH));
            params.height = ScaleHeight;
        }

        return params;
    }

    public LayoutParams setOnSize(View v, int aMarginL, int aMarginTop, int aMarginR, int aMarginBot) {

        if(!doResize)return v.getLayoutParams();

        if (DEBUG) Log.d(TAG, "v.getLayoutParams().width: " + v.getLayoutParams().width);
        if (DEBUG) Log.d(TAG, "v.getLayoutParams().height: " + v.getLayoutParams().height);

        LayoutParams params = (LayoutParams) v.getLayoutParams();
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;
        if (width >= 0) {
            int ScaleWidth = (int) (v.getLayoutParams().width * Float.parseFloat(subW));
            params.width = ScaleWidth;
        }
        if (height >= 0) {
            int ScaleHeight = (int) (v.getLayoutParams().height * Float.parseFloat(subH));
            params.height = ScaleHeight;
        }

        params = setMargins(v, aMarginL, aMarginTop, aMarginR, aMarginBot);

        return params;
    }

    public LayoutParams setMargins(View v, int aMarginL, int aMarginTop, int aMarginR, int aMarginBot) {

        if(!doResize)return v.getLayoutParams();

        LayoutParams params = (LayoutParams) v.getLayoutParams();

        aMarginL = getPixelAfterScale(aMarginL);
        aMarginTop = getPixelAfterScale(aMarginTop);
        aMarginR = getPixelAfterScale(aMarginR);
        aMarginBot = getPixelAfterScale(aMarginBot);
        if (DEBUG) Log.w(TAG, "aMarginL: " + aMarginL);
        if (DEBUG) Log.w(TAG, "aMarginTop: " + aMarginTop);
        if (DEBUG) Log.w(TAG, "aMarginR: " + aMarginR);
        if (DEBUG) Log.w(TAG, "aMarginBot: " + aMarginBot);

        ((MarginLayoutParams) params).setMargins(aMarginL, aMarginTop, aMarginR, aMarginBot);

        return params;
    }

    public void setPadding(View v, int aPaddingL, int aPaddingTop, int aPaddingR, int aPaddingBot) {

        if(!doResize)return;

        v.setPadding(getPixelAfterScale(aPaddingL),
                getPixelAfterScale(aPaddingTop),
                getPixelAfterScale(aPaddingR),
                getPixelAfterScale(aPaddingBot));
    }

    public void setTextSize(TextView aTextView) {

        if(!doResize)return;

        float textSize = aTextView.getTextSize() / density;
        textSize = textSize * Float.parseFloat(subW);
        aTextView.setTextSize(textSize);

    }

    public int getDpAfterScale(int aWidth) {
        return (int) (aWidth * Float.parseFloat(subW));
    }

    public int getPixelAfterScale(int aWidth) {
        aWidth = (int) (aWidth * Float.parseFloat(subW));
        aWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aWidth, context.getResources().getDisplayMetrics());
        return aWidth;
    }

    public float getPixelAfterScale(float aWidth) {
        aWidth = aWidth * Float.parseFloat(subW);
        aWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aWidth, context.getResources().getDisplayMetrics());
        return aWidth;
    }
}

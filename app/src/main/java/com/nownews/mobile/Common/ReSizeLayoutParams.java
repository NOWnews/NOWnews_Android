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

import java.text.DecimalFormat;

public class ReSizeLayoutParams {

    public final static String TAG = "ReSizeLayoutParams";
    float density;
    private Context context;
    private DisplayMetrics metric;
    private Display display;
    private float textSize;
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

    public void init() {

        metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        density = metric.density;

        if (Utility.DEBUG) Log.e(TAG, "density: " + density);
        if (Utility.DEBUG) Log.e(TAG, "metric.widthPixels: " + metric.widthPixels);
        if (Utility.DEBUG) Log.e(TAG, "metric.heightPixels: " + metric.heightPixels);

        float dpWidth = metric.widthPixels / density;
        float dpHeight = metric.heightPixels / density;
        if (Utility.DEBUG) Log.e(TAG, "dpWidth: " + dpWidth);
        if (Utility.DEBUG) Log.e(TAG, "dpHeight: " + dpHeight);

        float ScaleW = (float) dpWidth / 411f;
        float ScaleH = (float) dpHeight / 683f;
        if (Utility.DEBUG) Log.e(TAG, "ScaleW: " + ScaleW);
        if (Utility.DEBUG) Log.e(TAG, "ScaleH: " + ScaleH);

        String strW = df.format(ScaleW);
        String strH = df.format(ScaleH);
        if (Utility.DEBUG) Log.e(TAG, "strW: " + strW);
        if (Utility.DEBUG) Log.e(TAG, "strH: " + strH);

        subW = strW.substring(0, strW.length() - 1);
        subH = strH.substring(0, strH.length() - 1);
        if (Utility.DEBUG) Log.e(TAG, "subW: " + subW);
        if (Utility.DEBUG) Log.e(TAG, "subH: " + subH);
        if (Utility.DEBUG) Log.e(TAG, "==================================");
    }

    public LayoutParams setOnSize(View v, int aMarginL, int aMarginTop, int aMarginR, int aMarginBot){

        if(Utility.DEBUG)Log.d(TAG, "v.getLayoutParams().width: " + v.getLayoutParams().width);
        if(Utility.DEBUG)Log.d(TAG, "v.getLayoutParams().height: " + v.getLayoutParams().height);

        LayoutParams params = (LayoutParams) v.getLayoutParams();
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;
        if(width>=0){
            int ScaleWidth = (int) (v.getLayoutParams().width * Float.parseFloat(subW));
            params.width = ScaleWidth;
        }
        if(height>=0){
            int ScaleHeight = (int) (v.getLayoutParams().height * Float.parseFloat(subH));
            params.height = ScaleHeight;
        }

        params = setMargins(v, aMarginL, aMarginTop, aMarginR, aMarginBot);

        return params;
    }

    public LayoutParams setMargins(View v, int MarginL, int MarginTop, int MarginR, int MarginBot) {

        LayoutParams params = (LayoutParams) v.getLayoutParams();

        MarginL = getPixelAfterScale(MarginL);
        MarginTop = getPixelAfterScale(MarginTop);
        MarginR = getPixelAfterScale(MarginR);
        MarginBot = getPixelAfterScale(MarginBot);

        ((MarginLayoutParams) params).setMargins(MarginL, MarginTop, MarginR, MarginBot);

        return params;
    }

    public void setPadding(View v, int MarginL, int MarginTop, int MarginR, int MarginBot) {
        v.setPadding(getPixelAfterScale(MarginL),
                getPixelAfterScale(MarginTop),
                getPixelAfterScale(MarginR),
                getPixelAfterScale(MarginBot));
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {

        if (Utility.DEBUG) Log.v(TAG, "textSize: " + textSize);
        this.textSize = textSize / density;
//		this.textSize = textSize;
        this.textSize = (this.textSize * Float.parseFloat(subW));

    }

    public int getDpAfterScale(int aWidth) {
        return (int) (aWidth * Float.parseFloat(subW));
    }

    public int getPixelAfterScale(int aWidth) {
        aWidth = (int) (aWidth * Float.parseFloat(subW));
        aWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aWidth, context.getResources().getDisplayMetrics());
        return aWidth;
    }
}

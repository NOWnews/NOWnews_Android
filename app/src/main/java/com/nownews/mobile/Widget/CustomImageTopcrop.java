package com.nownews.mobile.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.nownews.mobile.Common.Utility;

public class CustomImageTopcrop extends ImageView {

    private final String TAG = getClass().getSimpleName();

    public CustomImageTopcrop(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CustomImageTopcrop(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public CustomImageTopcrop(Context context) {
        super(context);
        setup();
    }

    private void setup() {
        setScaleType(ScaleType.CENTER_CROP);
        setScaleType(ScaleType.MATRIX);
    }

    private boolean isCenterCrop = false;
    public void setCenterCrop(){
        setScaleType(ScaleType.CENTER_CROP);
        isCenterCrop = true;
    }

    @Override
    protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom) {
        if(Utility.DEBUG)Log.v(TAG, "@@@setFrame@@@");

        if(isCenterCrop){
            if(Utility.DEBUG)Log.e(TAG, "isCenterCrop: " + isCenterCrop);
            return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
        }

        if (getDrawable() == null) {
            if(Utility.DEBUG)Log.e(TAG, "getDrawable() == null");
            return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
        }

        float frameWidth = frameRight - frameLeft;
        float frameHeight = frameBottom - frameTop;

        float originalImageWidth = (float) getDrawable().getIntrinsicWidth();
        float originalImageHeight = (float) getDrawable().getIntrinsicHeight();

        float usedScaleFactor = 1;

        if ((frameWidth > originalImageWidth) || (frameHeight > originalImageHeight)) {
            // If frame is bigger than image  
            // => Crop it, keep aspect ratio and position it at the bottom and center horizontally  

            float fitHorizontallyScaleFactor = frameWidth / originalImageWidth;
            float fitVerticallyScaleFactor = frameHeight / originalImageHeight;

            usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
        }

        float newImageWidth = originalImageWidth * usedScaleFactor;
        float newImageHeight = originalImageHeight * usedScaleFactor;

        Matrix matrix = getImageMatrix();
        matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0); // Replaces the old matrix completly  
        // matrix.postTranslate((frameWidth - newImageWidth) / 2, frameHeight - newImageHeight);//BottomCrop  
        matrix.postTranslate((frameWidth - newImageWidth) / 2, 0);//Top Crop  
        setImageMatrix(matrix);
        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (Utility.DEBUG) Log.i(TAG, "setImageBitmap");
        super.setImageBitmap(bm);
    }

}

package com.ad2iction.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.WeakHashMap;

/**
 * Created by neko on 2017/5/11.
 */

public class Ad2ictionAdLocalEventHandler {

    @NonNull
    private final ImpressionTracker mImpressionTracker;

    // Used to keep track of the last NativeResponse a view was associated with in order to clean
    // up its state before associating with a new NativeResponse
    private static final WeakHashMap<View, NativeResponse> sNativeResponseMap = new WeakHashMap<View, NativeResponse>();

    public Ad2ictionAdLocalEventHandler(@NonNull Context context) {
        mImpressionTracker = new ImpressionTracker(context);
    }

    public void recycle() {
        if(mImpressionTracker!=null){
            mImpressionTracker.destroy();
        }
    }

    public void handleEvent(@NonNull final View view, @NonNull final NativeResponse nativeResponse) {
        clearNativeResponse(mImpressionTracker, view, sNativeResponseMap.remove(view));

        if (!nativeResponse.isDestroyed()) {
            sNativeResponseMap.put(view, nativeResponse);
            prepareNativeResponse(mImpressionTracker, view, nativeResponse);
        }
    }

    static void clearNativeResponse(@NonNull final ImpressionTracker impressionTracker,
                                    @NonNull final View view,
                                    @Nullable final NativeResponse nativeResponse) {
        impressionTracker.removeView(view);
        if (nativeResponse != null) {
            nativeResponse.clear(view);
        }
    }

    static void prepareNativeResponse(@NonNull final ImpressionTracker impressionTracker,
                                      @NonNull final View view,
                                      @NonNull final NativeResponse nativeResponse) {
        if (!nativeResponse.isOverridingImpressionTracker()) {
            impressionTracker.addView(view, nativeResponse);
        }
        nativeResponse.prepare(view);
    }
}

package com.nownews.mobile.Baselibs;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.nownews.mobile.Dao.BaseDao;

import java.io.IOException;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class CustomLoader<T> extends AsyncTaskLoader<T> {
    private String Tag = getClass().getSimpleName();
    private BaseDao<T> baseDao;

    public CustomLoader(Context context, BaseDao daos, boolean init) {
        super(context);
        onContentChanged();
        this.baseDao = daos;
    }

    @Override
    public T loadInBackground() {
        try {
            this.baseDao.checkToken();
            return this.baseDao.mapperJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(T obj) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        if (isStarted()) {
            super.deliverResult(obj);
        }
    }

    @Override
    protected void onStartLoading() {
//        if (!init) {
//            forceLoad();
//        }
        if (takeContentChanged()) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(T obj) {
        super.onCanceled(obj);
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        Log.e(Tag, "onReset");
    }
}

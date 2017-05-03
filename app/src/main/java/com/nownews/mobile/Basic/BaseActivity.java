package com.nownews.mobile.Basic;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.nownews.R;
import com.nownews.mobile.Baselibs.CustomLoader;
import com.nownews.mobile.Config.Configs;
import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Dao.BaseDao;
import com.nownews.mobile.Dao.Entity.ResponseErr;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public abstract class BaseActivity<T> extends AppCompatActivity implements LoaderManager.LoaderCallbacks<T> {
    @BindView(android.R.id.content)
    View RootView;
    protected BaseDao baseDao;
    protected boolean isMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initPageLayoutID());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // in Activity's onCreate() for instance
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAfterTransition(this);
    }

    /**
     * 返回主佈局id
     */
    protected abstract int initPageLayoutID();

    /**
     * init Content
     */
    protected abstract void initContent();

    /**
     * Loader Result
     *
     * @return
     */
    protected abstract void LoaderResult(T result);

    /**
     * run AsyncTask
     *
     * @param params no parameter set NULL
     */
    public void DoSync(BaseDao... params) {
        if (params.length != 0) {
            for (int i = 0; i < params.length; i++) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Configs.Bundle_key, params[i]);
                bundle.putBoolean(Configs.Bundle_key_isLoad, false);
                getSupportLoaderManager().restartLoader(i, bundle, this);
            }
        }
    }

    @Override
    public Loader<T> onCreateLoader(int id, Bundle args) {
        return new CustomLoader(this, (BaseDao) args.getSerializable(Configs.Bundle_key),
                args.getBoolean(Configs.Bundle_key_isLoad));
    }

    @Override
    public void onLoadFinished(Loader<T> loader, T data) {
        if (data instanceof ResponseErr) {
            if (((ResponseErr) data).getStatus_code() == Constants.StatusCode.No_Intent_Err) {
                showSnackbar(getString(R.string.miss_connect), getString(R.string.retry), loader);
            } else if (((ResponseErr) data).getStatus_code() < Constants.StatusCode.BadRequest_Err_40000) {
                // 一般錯誤
                showSnackbar(getString(R.string.socket_timout_error_message));
            } else {
                LoaderResult(data);
            }
        } else {
            LoaderResult(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
    }

    public void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar
                .make(this.RootView, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackbar(String msg, String btnText, final Loader<T> loader) {
        Snackbar snackbar = Snackbar
                .make(this.RootView, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(btnText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loader.onContentChanged();
                    }
                });
        snackbar.show();
    }

    public void showNetworkError() {
        Snackbar snackbar = Snackbar
                .make(this.RootView, getString(R.string.miss_connect), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initContent();
                    }
                });
        snackbar.show();
    }
}

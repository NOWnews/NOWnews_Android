package com.nownews.mobile.Basic;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.nownews.R;
import com.nownews.mobile.Baselibs.CustomLoader;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Config.Configs;
import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Dao.BaseDao;
import com.nownews.mobile.Dao.Entity.ResponseErr;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ChengYuanChin on 2017/4/26.
 */

public abstract class BaseSideActivity<T> extends AppCompatActivity implements LoaderManager.LoaderCallbacks<T> {
    @BindView(R.id.drw_layout)
    protected DrawerLayout vDrawerLayout;
    @BindView(R.id.bottom_navigation)
    protected AHBottomNavigation vBottomNavigation;

    protected BaseDao baseDao;
    public boolean isMenu = false;
    private FragmentTransaction ft;
    private FragmentManager fm;
    protected boolean isNeedToLeave = false;
    protected int mCurrentCategoryPage = -1;

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

    /**
     * 返回主佈局id
     */
    protected abstract int initPageLayoutID();

    /**
     * 返回Fragment Content佈局id
     */
    protected abstract int getContentLayoutID();

    /**
     * exit dialog
     */
    protected abstract void openConfirmDialog();

    /**
     * Loader Result
     *
     * @return
     */
    protected abstract void LoaderResult(T result);

    @Override
    public void onBackPressed() {
        if (this.vDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.vDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            setBottomVisibility(View.VISIBLE);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (this.isNeedToLeave) {
                UserDataInfo.isVersionDialogShow = false;
                UserDataInfo.mHomeDFPCount = 0;
                if (Utility.DEBUG)
                    Log.e(getClass().getSimpleName(), "UserDataInfo.mHomeDFPCount: " + UserDataInfo.mHomeDFPCount);
                super.onBackPressed();
            } else {
                openConfirmDialog();
            }
        }
    }

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
                .make(this.vDrawerLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackbar(String msg, String btnText, final Loader<T> loader) {
        Snackbar snackbar = Snackbar
                .make(this.vDrawerLayout, msg, Snackbar.LENGTH_INDEFINITE)
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
                .make(this.vDrawerLayout, getString(R.string.miss_connect), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        initContent();
                    }
                });
        snackbar.show();
    }

    /**
     * 設定首頁
     *
     * @param fragment 切換內容
     * @param Data     Pass Data
     */
    protected void setFistPage(Fragment fragment, Bundle Data) {
        if (Data != null) {
            fragment.setArguments(Data);
        }
        fm = null;
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(getContentLayoutID(), fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    /**
     * 切換內容
     *
     * @param f      切換內容
     * @param status 頁面是否堆疊
     * @param Data   Pass Data
     */
    public void changeFragment(Fragment f, boolean status,
                               Bundle Data) {
        if (Data != null) {
            f.setArguments(Data);
        }

        ft = fm.beginTransaction();
        ft.replace(getContentLayoutID(), f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (status)
            ft.addToBackStack(null);
        ft.commit();
    }

    public Fragment getVisibleFragment() {
        List<Fragment> fragments = this.getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    public void setBottomVisibility(int visibility) {
        this.vBottomNavigation.setVisibility(visibility);
    }
}

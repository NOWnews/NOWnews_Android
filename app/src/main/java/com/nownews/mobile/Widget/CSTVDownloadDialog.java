package com.nownews.mobile.Widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.Live.LivePlayer;

import java.util.List;

/**
 * Created by cindy on 2016/12/19.
 */

public class CSTVDownloadDialog extends Dialog {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    private RelativeLayout vRootLayout;
    private RelativeLayout vBody;
    private TextView vMessage;
    private Button vWatchNow;
    private Button vDownloadNow;
    private CardView vCardViewGroup;
    private LinearLayout vButtonsGroup;
    private ImageView vIcon;

    private ReSizeLayoutParams mResize;

    private String mTitle;
    private List<LiveListJson.Data> mLiveList;
    private String mPath;
    private int mCategoryIndex;
    private int mChannelIndex;

    public CSTVDownloadDialog(Context context, String aTitle, List<LiveListJson.Data> aLiveList, String aPath, int aCategoryIndex, int aChannelIndex) {
        super(context, R.style.FullScreenDialogStyle);

        mTitle = aTitle;
        mLiveList = aLiveList;
        mPath = aPath;
        mCategoryIndex = aCategoryIndex;
        mChannelIndex = aChannelIndex;

        setCancelable(true);
        setCanceledOnTouchOutside(true);
        mContext = context;
        setContentView(R.layout.dialog_upgrade);
    }

    public CSTVDownloadDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected CSTVDownloadDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processView();
        processListener();

    }

    private void processView(){

        mResize = new ReSizeLayoutParams(mContext);

        vRootLayout = (RelativeLayout)findViewById(R.id.root_layout);
        vRootLayout.setLayoutParams(mResize.setOnSize(vRootLayout, 0, 0, 0, 0));

        vBody = (RelativeLayout)findViewById(R.id.body);

        vCardViewGroup = (CardView)findViewById(R.id.cardview_group);
        vCardViewGroup.setLayoutParams(mResize.setOnSize(vCardViewGroup, 0, 0, 0, 0));

        vMessage = (TextView)findViewById(R.id.message);
        vMessage.setLayoutParams(mResize.setMargins(vMessage, 0, 0, 0, 10));
        mResize.setTextSize(vMessage.getTextSize());
        vMessage.setTextSize(mResize.getTextSize());

        vButtonsGroup = (LinearLayout)findViewById(R.id.buttons_group);
        vButtonsGroup.setLayoutParams(mResize.setMargins(vButtonsGroup, 10, 10, 10, 10));

        vWatchNow = (Button)findViewById(R.id.watch_now);
        vWatchNow.setLayoutParams(mResize.setOnSize(vWatchNow, 0, 0, 0, 0));
        mResize.setTextSize(vWatchNow.getTextSize());
        vWatchNow.setTextSize(mResize.getTextSize());

        vDownloadNow = (Button)findViewById(R.id.download_now);
        vDownloadNow.setLayoutParams(mResize.setOnSize(vDownloadNow, 10, 0, 0, 0));
        mResize.setTextSize(vDownloadNow.getTextSize());
        vDownloadNow.setTextSize(mResize.getTextSize());

        vIcon = (ImageView)findViewById(R.id.cstv_icon);
        mResize.setPadding(vIcon, 20, 20, 20, 10);


    }

    private void processListener(){

        vRootLayout.setOnClickListener(mRootLayoutClickListener);
        vWatchNow.setOnClickListener(mWatchNowClickListener);
        vDownloadNow.setOnClickListener(mDownloadNowClickListener);
        vBody.setOnClickListener(mBodyClickListener);

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
                    Uri.parse("market://details?id=com.csmuse.dvbt.nownews.mobile"));
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
            mContext.startActivity(intent);

            dismiss();

        }
    };

}

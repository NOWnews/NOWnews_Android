package com.nownews.mobile.FavoriteAlbum;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Widget.ZoomImageView;

public class FavoritePageFragment extends Fragment {

    public static final String KEY_NEWS_URL = "newsUrl";
    public final static String KEY_IMAGE_URL = "imageUrl";
    public final static String KEY_IMAGE_TITLE = "imageTitle";
    public final static String KEY_IMAGE_NEWS_URL = "imageNewsUrl";
    private final String TAG = getClass().getSimpleName();
    private ZoomImageView vImage;
    private TextView vTitle;
    private ProgressBar vImageProgressBar;
    private RelativeLayout vLoadingLayout;
    private String mImageUrl;
    private String mImageTitle;
    private String mImageNewsUrl;
    private BitmapController mBitmapController;
    private String mShareImgUrl;
    private OnClickListener mImageClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (Utility.DEBUG) Log.e(TAG, "image click");
            if (((FavoriteAlbumPage) getActivity()).getToolBarVisibility()) {
                ((FavoriteAlbumPage) getActivity()).hideToolBar();
                if (mImageTitle != null && !mImageTitle.trim().equals("")) {
                    vTitle.animate().translationY(vTitle.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                }
            } else {
                ((FavoriteAlbumPage) getActivity()).showToolBar();
                if (mImageTitle != null && !mImageTitle.trim().equals("")) {
                    vTitle.setVisibility(View.VISIBLE);
                    vTitle.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                }
            }

        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ZoomImageView.ZOOM:
                    ((FavoriteAlbumPage) getActivity()).stopViewPagerSwipe();
                    break;
                case ZoomImageView.NONE:
                    ((FavoriteAlbumPage) getActivity()).startViewPagerSwipe();
                    break;
            }

        }

    };
    private boolean isOnPause = false;

    public FavoritePageFragment() {
        //DO NOTHING...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Utility.DEBUG) Log.e(TAG, "onActivityCreated");

        startFragment();

    }

    private void startFragment() {

        initController();
        processArgument();
        processView();
        processListener();
        setData();

    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {

            String imageUrl = bundle.getString(KEY_IMAGE_URL);
            String imageTitle = bundle.getString(KEY_IMAGE_TITLE);
            String imageNewsUrl = bundle.getString(KEY_IMAGE_NEWS_URL);

            //For Share
            if (imageUrl.startsWith("http://s.nownews.com")) {
                mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, 50, imageUrl);
                if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
            }

            if (imageUrl.startsWith("http://s.nownews.com")) {
                int screenWidth = Utility.getScreenWidth(getActivity());
                imageUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, imageUrl);
                if (Utility.DEBUG) Log.v(TAG, "imageUrl: " + imageUrl);
            }

            mImageUrl = imageUrl;
            mImageTitle = imageTitle;
            mImageNewsUrl = imageNewsUrl;

        }
    }

    private void initController() {
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
    }

    public void setData() {

        mBitmapController.loadImageWithOriginalSize(mImageUrl, vImage, BitmapController.IMAGE_SRC, 0, 0, null);
        if (mShareImgUrl != null) {
            mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, vImage, BitmapController.IMAGE_SRC, 0, 0, null);
        }
        if (mImageTitle == null || mImageTitle.trim().equals("")) {
            vTitle.setVisibility(View.GONE);
        } else {
            vTitle.setVisibility(View.VISIBLE);
            vTitle.setText(mImageTitle);
        }

    }

    public void processView() {

        View view = getView();

        vImage = (ZoomImageView) view.findViewById(R.id.album_img);
        vImage.setHandler(mHandler);
        vTitle = (TextView) view.findViewById(R.id.album_title);
        vImageProgressBar = (ProgressBar) view.findViewById(R.id.image_progressbar);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);

    }

    private void processListener() {
        vImage.setOnClickListener(mImageClickListener);
    }

    public String getImageTitle() {
        return mImageTitle;
    }

    public String getImageNewsUrl() {
        return mImageNewsUrl;
    }

    public String getShareImageUrl() {
        return mShareImgUrl;
    }

    public Bitmap getBigShareImage() {
        if (vImage != null) {
            Bitmap bitmap = ((BitmapDrawable) vImage.getDrawable()).getBitmap();
            return bitmap;
        }
        return null;
    }

    public void changeTitleVisibility(boolean isShow) {
        if (isShow) {
            if (mImageTitle != null && !mImageTitle.trim().equals("")) {
                if (vTitle == null) {
                    return;
                }
                vTitle.setVisibility(View.VISIBLE);
            } else {
                if (vTitle == null) {
                    return;
                }
                vTitle.setVisibility(View.GONE);
            }
        } else {
            vTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        isOnPause = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        if (isOnPause) {
            isOnPause = false;
            setData();
        }
        super.onResume();
    }

    public void reload() {
        startFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(getActivity());
        }
    }
}

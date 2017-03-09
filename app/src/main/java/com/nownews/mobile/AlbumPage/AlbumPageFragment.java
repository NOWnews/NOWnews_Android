package com.nownews.mobile.AlbumPage;

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
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Controller.BitmapController.ImageLoadingListener;
import com.nownews.mobile.Json.PhotosInfoJson.PhotoInfo;
import com.nownews.mobile.Widget.ZoomImageView;

public class AlbumPageFragment extends Fragment {

    public final static String KEY_IMAGE_INFO = "imageInfo";
    private final String TAG = getClass().getSimpleName();
    private ZoomImageView vImage;
    private TextView vTitle;
    private ProgressBar vImageProgressBar;
    private ProgressBar vAnimProgressBar;
    private RelativeLayout vLoadingLayout;
    private PhotoInfo mImageInfo;
    private BitmapController mBitmapController;
    private String mShareImgUrl;
    private OnClickListener mImageClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (Utility.DEBUG) Log.e(TAG, "image click");
            if (((AlbumPage) getActivity()).getToolBarVisibility()) {
                ((AlbumPage) getActivity()).hideToolBar();
                vTitle.animate().translationY(vTitle.getHeight()).setInterpolator(new AccelerateInterpolator(2));
            } else {
                ((AlbumPage) getActivity()).showToolBar();
                vTitle.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }

        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ZoomImageView.ZOOM:
                    ((AlbumPage) getActivity()).stopViewPagerSwipe();
                    break;
                case ZoomImageView.NONE:
                    ((AlbumPage) getActivity()).startViewPagerSwipe();
                    break;
            }

        }

    };
    private int mCurrentImageProgress;
    private int mCurrentImageSize;
    private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {

        @Override
        public void onProgressUpdate(String aImageUrl, final int aProgress, final int max) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                    if (aProgress < 0) {
                        vImageProgressBar.setVisibility(View.GONE);
                        vAnimProgressBar.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (Utility.DEBUG) Log.e(TAG, "onProgressUpdate" + aProgress + "/" + max);
                    mCurrentImageProgress = aProgress;
                    mCurrentImageSize = max;

                    vImageProgressBar.setVisibility(View.VISIBLE);
                    vAnimProgressBar.setVisibility(View.GONE);
                    vImageProgressBar.setMax(mCurrentImageSize);
                    vImageProgressBar.setProgress(mCurrentImageProgress);
                    if (mCurrentImageProgress == mCurrentImageSize) {
                        vLoadingLayout.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onLoadingStart(String aImageUrl, View aView) {

        }

        @Override
        public void onLoadingFailed(String aImageUrl, View aView,
                                    Exception aException) {

        }

        @Override
        public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {
            vAnimProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingCancelled() {

        }
    };
    private boolean isOnPause = false;

    public AlbumPageFragment() {
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

    public void setImageInfo(PhotoInfo aBigImageInfo) {
        mImageInfo = aBigImageInfo;
    }

    private void startFragment() {

        initController();
        processView();
        processListener();
        setData();

    }

    private void initController() {
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
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

    public void setData() {

        if (mImageInfo != null
                && mImageInfo.thumbnail != null
                && !mImageInfo.thumbnail.trim().equals("")) {
            String imgUrl = mImageInfo.thumbnail;
            mBitmapController.loadImageWithOriginalSize(imgUrl, vImage, BitmapController.IMAGE_SRC, 0, 0, mImageLoadingListener);
            String shareImageUrl = Utility.getSrcFromImgapi(mImageInfo.thumbnail);
            mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, 50, shareImageUrl);
            if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
            mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, null, BitmapController.IMAGE_SRC, 0, 0, null);
        }

        if (mImageInfo != null
                && mImageInfo.cite != null
                && !mImageInfo.cite.trim().equals("")) {
            String title = mImageInfo.cite;
            vTitle.setText(title);
        }

    }

    public void processView() {

        View view = getView();

        vImage = (ZoomImageView) view.findViewById(R.id.album_img);
        vImage.setHandler(mHandler);
        vTitle = (TextView) view.findViewById(R.id.album_title);
        vImageProgressBar = (ProgressBar) view.findViewById(R.id.image_progressbar);
        vAnimProgressBar = (ProgressBar) view.findViewById(R.id.anim_progressbar);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);

    }

    private void processListener() {
        vImage.setOnClickListener(mImageClickListener);
    }

    public void setHitInfo(String aAlbumCategory) {
        String url = mImageInfo.thumbnail;
        url = Utility.getSrcFromImgapi(url);
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

}

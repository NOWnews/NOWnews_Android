package com.nownews.mobile.FavoriteAlbum;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.nownews.R;
import com.nownews.imagedownloadmanager.ImageDownloadManager;
import com.nownews.imagedownloadmanager.ImageDownloadManagerActivity;
import com.nownews.imagedownloadmanager.ImageInfo;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Widget.ZoomImageView;

import java.io.File;

import static com.nownews.mobile.Common.Utility.getApplicationContext;

public class FavoritePageFragment extends Fragment{

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

            mImageUrl = imageUrl;
            mImageTitle = imageTitle;
            mImageNewsUrl = imageNewsUrl;

        }
    }

    private void initController() {
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
    }

    private int mImageWidth;
    private int mImageHeight;
    public void setData() {

        //TODO load original image
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
                .asBitmap()
                .load(mImageUrl)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.e(TAG, "onLoadFailed");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "onResourceReady");
                        mImageWidth = resource.getWidth();
                        mImageHeight = resource.getHeight();
                        Log.d(TAG, "mImageWidth: " + mImageWidth);
                        Log.d(TAG, "mImageHeight: " + mImageHeight);
                        return false;
                    }
                }).submit();


        if (mImageUrl.startsWith("http://s.nownews.com")) {
            int screenWidth = Utility.getScreenWidth(getActivity());
            mImageUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, mImageUrl);
            if (Utility.DEBUG) Log.v(TAG, "imageUrl: " + mImageUrl);
        }
        mBitmapController.loadImageWithOriginalSize(mImageUrl, vImage, BitmapController.IMAGE_SRC, 0, 0, null);

        //share image
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
//        vImage.setLongClickable(true);
//        vImage.setOnLongClickListener(mDownloagImage);
    }

    private ImageDownloadManager mImageDownloadManager;
    private View.OnLongClickListener mDownloagImage = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

//            Toast.makeText(getActivity(), "Image Long Click!!", Toast.LENGTH_LONG).show();
            String originalImageUrl = getArguments().getString(KEY_IMAGE_URL);
            ImageInfo originalImageInfo = new ImageInfo();
            originalImageInfo.setUrl(originalImageUrl);
            originalImageInfo.setImgW(mImageWidth);
            originalImageInfo.setImgH(mImageHeight);

            ImageInfo thumbnailImageInfo = null;
            int screenWidth = Utility.getScreenWidth(getActivity());
            if(mImageWidth>screenWidth){
                float scale = ((float) screenWidth) / mImageWidth;
                int newHeight = (int) (mImageHeight * scale);
                String thumbnailUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, newHeight, Utility.IMG_QUALITY, originalImageUrl);
                thumbnailImageInfo = new ImageInfo();
                thumbnailImageInfo.setUrl(thumbnailUrl);
                thumbnailImageInfo.setImgW(screenWidth);
                thumbnailImageInfo.setImgH(newHeight);
            }

            String fileName = getString(R.string.nownews) + "_" + originalImageUrl.substring(originalImageUrl.lastIndexOf("/")+1);

//            if(getActivity().hasWindowFocus()){
                mImageDownloadManager = new ImageDownloadManager(getActivity());
                if(originalImageInfo!=null){
                    mImageDownloadManager.setOriginalImageInfo(originalImageInfo);
                }
                if(thumbnailImageInfo!=null){
                    mImageDownloadManager.setThumbnailImageInfo(thumbnailImageInfo);
                }
                mImageDownloadManager.isCopyrightDialogShow(true, getString(R.string.copyright_content), getString(R.string.copyright_positive));
                mImageDownloadManager.setDefaultDownloadFolderPath("/sdcard/Download");
                mImageDownloadManager.setDownloadDialogTitle("圖片下載中");
                mImageDownloadManager.setDownloadDialogContent("再等一下下就好囉...");
                mImageDownloadManager.setDownloadFileName(fileName);
                mImageDownloadManager.start();
//            }

            return false;
        }
    };

    public void startDownload(String folderPath){
        mImageDownloadManager.startDownload(folderPath);
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
            mBitmapController.unregistBitmapController(getActivity());
        }
    }
}

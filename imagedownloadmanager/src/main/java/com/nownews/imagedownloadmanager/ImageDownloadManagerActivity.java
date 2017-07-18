package com.nownews.imagedownloadmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.text.DecimalFormat;

public class ImageDownloadManagerActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback{

    private final String TAG = getClass().getSimpleName();
    private ImageView vDownloadImage;
    private String mImageUrl = "http://s.nownews.com/92/01/9201ff23356b88146d1828dd63ad76f8.jpg";
    private String SCALE_IMAGE = "https://imgapiv2.nownews.com/?w=%s&h=%s&q=%s&src=%s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processScreenSize();
        processView();
        processImage();

    }

    private void processView(){
        vDownloadImage = (ImageView)findViewById(R.id.download_image);
        vDownloadImage.setLongClickable(true);
        vDownloadImage.setOnLongClickListener(mDownloagImage);
    }

    private int mImageWidth;
    private int mImageHeight;
    private void processImage(){
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
                .asBitmap()
                .load("http://s.nownews.com/92/01/9201ff23356b88146d1828dd63ad76f8.jpg")
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
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
                })
                .into(vDownloadImage);
    }

    private ImageDownloadManager mImageDownloadManager;
    private View.OnLongClickListener mDownloagImage = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

//            Toast.makeText(ImageDownloadManagerActivity.this, "Image Long Click!!", Toast.LENGTH_LONG).show();
            ImageInfo originalImageInfo = new ImageInfo();
            originalImageInfo.setUrl(mImageUrl);
            originalImageInfo.setImgW(mImageWidth);
            originalImageInfo.setImgH(mImageHeight);

            ImageInfo thumbnailImageInfo = null;
            if(mImageWidth>mScreenWidth){
                float scale = ((float) mScreenWidth) / mImageWidth;
                int newHeight = (int) (mImageHeight * scale);
                String thumbnailUrl = String.format(SCALE_IMAGE, mScreenWidth, newHeight, "50", mImageUrl);
                thumbnailImageInfo = new ImageInfo();
                thumbnailImageInfo.setUrl(thumbnailUrl);
                thumbnailImageInfo.setImgW(mScreenWidth);
                thumbnailImageInfo.setImgH(newHeight);
            }

            String fileName = mImageUrl.substring(mImageUrl.lastIndexOf("/")+1);

            mImageDownloadManager = new ImageDownloadManager(ImageDownloadManagerActivity.this);
            if(originalImageInfo!=null){
                mImageDownloadManager.setOriginalImageInfo(originalImageInfo);
            }
            if(thumbnailImageInfo!=null){
                mImageDownloadManager.setThumbnailImageInfo(thumbnailImageInfo);
            }
            mImageDownloadManager.isCopyrightDialogShow(true, "Copyright Content", "ok i know");
            mImageDownloadManager.setDefaultDownloadFolderPath("/sdcard/Download");
            mImageDownloadManager.setDownloadDialogTitle("下載中");
            mImageDownloadManager.setDownloadDialogContent("等一下就好嘿...");
            mImageDownloadManager.setDownloadFileName(fileName);
            mImageDownloadManager.start();

            return false;
        }
    };

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        String folderPath = folder.getAbsolutePath();
        Log.d(TAG, "folderPath: " + folderPath);
        if(mImageDownloadManager!=null){
            mImageDownloadManager.startDownload(folderPath);
        }
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {

    }

    private int mScreenWidth;
    private int mScreenHeight;
    private float mDensity;
    //TODO processScreenSize()
    private void processScreenSize() {

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        final DisplayMetrics metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        mDensity = metric.density;

        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;

        Log.e(TAG, "mScreenWidth: " + mScreenWidth);
        Log.e(TAG, "mScreenHeight: " + mScreenHeight);
        Log.e(TAG, "mDensity: " + mDensity);

    }

}

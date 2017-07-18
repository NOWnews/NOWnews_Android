package com.nownews.imagedownloadmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by cindy on 2017/6/30.
 */

public class ImageDownloadManager implements FolderChooserDialog.FolderCallback, DownloadListener.DownloadStatus{

    private final boolean DEBUG = false;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private String mOriginalImageUrl;
    private String mThumbnailImageUrl;
    private int[] mOriginalImageWH;
    private int[] mThumbnailImageWH;
    private enum ChooseType{ORIGINAL, THUMBNAIL}
    private ChooseType mCurrentChooseType;

    public ImageDownloadManager(Context context){
        mContext = context;
        processScreenSize();
    }

    public void setOriginalImageInfo(ImageInfo imgInfo){
        mOriginalImageUrl = imgInfo.getImgUrl();
        mOriginalImageWH = imgInfo.getImgWH();
    }

    public void setThumbnailImageInfo(ImageInfo imgInfo){
        mThumbnailImageUrl = imgInfo.getImgUrl();
        mThumbnailImageWH = imgInfo.getImgWH();
    }

    private String mDefaultFolderPath;
    public void setDefaultDownloadFolderPath(String folderPath){
        mDefaultFolderPath = folderPath;
    }

    private String mFileName;
    public void setDownloadFileName(String fileName){
        mFileName = fileName;
    }

    private boolean isCopyRightDialogShow;
    private String mCopyrightDialogContent;
    private String mCopyrightDialogPositiveText;
    public void isCopyrightDialogShow(boolean isCopyRightDialogShow, String content, String positiveText){
        this.isCopyRightDialogShow = isCopyRightDialogShow;
        mCopyrightDialogContent = content;
        mCopyrightDialogPositiveText = positiveText;
    }

    public void start(){
        if(isCopyRightDialogShow){
            openCopyrightDialog();
        }else{
            checkFileSize();
        }
    }

    private void openCopyrightDialog(){
//        if(((Activity)mContext).hasWindowFocus()){
            MaterialDialog mCopyrightDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.copyright_title)
                    .content(mCopyrightDialogContent)
                    .positiveText(mCopyrightDialogPositiveText)
                    .onPositive(mCopyrightDialogPositive)
                    .show();
//        }
    }

    private MaterialDialog.SingleButtonCallback mCopyrightDialogPositive = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

//            Toast.makeText(ImageDownloadManagerActivity.this, getString(R.string.copyright_positive), Toast.LENGTH_LONG).show();
            checkFileSize();

        }
    };

    private void checkFileSize(){
        openChooseSizeDialog();
    }

    private int mScreenWidth;
    private int mScreenHeight;
    private float mDensity;
    //TODO processScreenSize()
    private void processScreenSize() {

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        final DisplayMetrics metric = new DisplayMetrics();
        display.getMetrics(metric);
        DecimalFormat df = new DecimalFormat("0.000");
        mDensity = metric.density;

        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;

        if(DEBUG)Log.e(TAG, "mScreenWidth: " + mScreenWidth);
        if(DEBUG)Log.e(TAG, "mScreenHeight: " + mScreenHeight);
        if(DEBUG)Log.e(TAG, "mDensity: " + mDensity);

    }

    private void openChooseSizeDialog(){

        ArrayList<String> items = new ArrayList<>();

        //original img info
        String originalSizeItem;
        if(mOriginalImageWH!=null){
            originalSizeItem = String.format(mContext.getString(R.string.choose_size_original_size), mOriginalImageWH[0], mOriginalImageWH[1]);
            items.add(originalSizeItem);
        }

        //thumbnail img info
        String thumbnailSizeItem;
        if(mThumbnailImageWH!=null){
            thumbnailSizeItem = String.format(mContext.getString(R.string.choose_size_thumbnail_size), mThumbnailImageWH[0], mThumbnailImageWH[1]);
            items.add(thumbnailSizeItem);
        }
//        if(((Activity)mContext).hasWindowFocus()) {
            MaterialDialog mChooseSizeDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.choose_size_title)
                    .items(items)
                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                            if(DEBUG)Log.v(TAG, "which: " + which);
                            if(DEBUG)Log.v(TAG, "text: " + text);
                            if (text.toString().contains(mContext.getString(R.string.original_size))) {
                                mCurrentChooseType = ChooseType.ORIGINAL;
                            } else if (text.toString().contains(mContext.getString(R.string.thumbnail_size))) {
                                mCurrentChooseType = ChooseType.THUMBNAIL;
                            }

                            return true;
                        }
                    })
                    .alwaysCallSingleChoiceCallback()
                    .positiveText(R.string.choose_size_positive)
                    .negativeText(R.string.choose_size_negative)
                    .onPositive(mChooseSizeDialogPositive)
                    .onNegative(mChooseSizeDialogNegative)
                    .show();
//        }
    }

    private void openFolderChooserDialog(){
        if(mDefaultFolderPath !=null){
            FolderChooserDialog mFolderChooserDialog = new FolderChooserDialog.Builder((ImageDownloadManagerActivity) mContext)
                    .chooseButton(R.string.folder_chooser_start_download)
                    .allowNewFolder(true, R.string.folder_chooser_new_folder)
                    .initialPath(mDefaultFolderPath)
                    .show();
        }else{
            if(DEBUG)Log.e(TAG, "No Folder Path!!");
        }
    }

    private MaterialDialog.SingleButtonCallback mChooseSizeDialogPositive = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

//            Toast.makeText(mContext, mContext.getString(R.string.choose_size_positive), Toast.LENGTH_LONG).show();
            if(DEBUG)Log.w(TAG, "dialog.getSelectedIndex(): " + dialog.getSelectedIndex());

            String text = dialog.getItems().get(dialog.getSelectedIndex()).toString();
            if(DEBUG)Log.v(TAG, "text: " + text);
            if(text.toString().contains(mContext.getString(R.string.original_size))){
                mCurrentChooseType = ChooseType.ORIGINAL;
            }else if(text.toString().contains(mContext.getString(R.string.thumbnail_size))){
                mCurrentChooseType = ChooseType.THUMBNAIL;
            }

            openFolderChooserDialog();

        }
    };

    private MaterialDialog.SingleButtonCallback mChooseSizeDialogNegative = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

//            Toast.makeText(mContext, mContext.getString(R.string.choose_size_negative), Toast.LENGTH_LONG).show();

        }
    };

    /**
     * For Folder Chooser Dialog
     * */
    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        String folderPath = folder.getAbsolutePath();
        if(DEBUG)Log.d(TAG, "folderPath: " + folderPath);
        startDownload(folderPath);
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
        if(DEBUG)Log.d(TAG, "Folder Chooser Dialog Dismissed!!");
    }

    private String mDownloadDialogTitle = "開始下載";
    public void setDownloadDialogTitle(String title){
        mDownloadDialogTitle = title;
    }

    private String mDownloadDialogContent = "敬請等待";
    public void setDownloadDialogContent(String content){
        mDownloadDialogContent = content;
    }

    public void startDownload(String folderPath){

        if(DEBUG)Log.v(TAG, "startDownload!! folderPath: " + folderPath);
        if(DEBUG)Log.v(TAG, "mFileName: " + mFileName);

        boolean showMinMax = true;
//        if(((Activity)mContext).hasWindowFocus()) {
            mDownloadDialog = new MaterialDialog.Builder(mContext)
                    .title(mDownloadDialogTitle)
                    .content(mDownloadDialogContent)
                    .progress(false, 100, showMinMax)
                    .dismissListener(mDownloadDialogDismissListener)
                    .show();
//        }

        String downloadImageUrl = null;
        if(mCurrentChooseType== ChooseType.ORIGINAL){
            downloadImageUrl = mOriginalImageUrl;
        }else if(mCurrentChooseType== ChooseType.THUMBNAIL){
            downloadImageUrl = mThumbnailImageUrl;
        }
        if(DEBUG)Log.d(TAG, "downloadImageUrl: " + downloadImageUrl);

        if(downloadImageUrl!=null){
            mTask = new DownloadAsyncTask(this, folderPath, mFileName);
            mTask.execute(downloadImageUrl);
        }

    }

    private DownloadAsyncTask mTask;
    private DialogInterface.OnDismissListener mDownloadDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {

            if(mCurrentProgress<100 && mTask!=null){
                mTask.exit();
            }

        }
    };

//    private DialogInterface.OnCancelListener mDownloadDialogCancelListener = new DialogInterface.OnCancelListener() {
//        @Override
//        public void onCancel(DialogInterface dialogInterface) {
//
//            if(mCurrentProgress<100 && mTask!=null){
//                mTask.exit();
//            }
//
//        }
//    };

    private MaterialDialog mDownloadDialog;
    private int mCurrentProgress;
    @Override
    public void onDownloadSize(String url, int size, int fileSize) {

        mCurrentProgress = size;

        if(mDownloadDialog !=null){
            mDownloadDialog.setProgress(size);
        }

        if(size == mDownloadDialog.getMaxProgress()){
            mDownloadDialog.setContent("下載完成");
            handler.sendEmptyMessageDelayed(CLOSE_DOWNLOAD_DIALOG, 1000);
        }

    }

    @Override
    public void downloadFinish(String url) {

    }

    @Override
    public void onErrorOccur() {

        if(mDownloadDialog !=null){
            mDownloadDialog.dismiss();
        }
        Toast.makeText(mContext, "下載失敗", Toast.LENGTH_SHORT).show();

    }

    private final int CLOSE_DOWNLOAD_DIALOG = 0x555;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CLOSE_DOWNLOAD_DIALOG:

                    if(mDownloadDialog !=null){
                        mDownloadDialog.dismiss();
                    }

                    break;
            }

        }
    };

}

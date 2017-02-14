package com.nownews.mobile.Service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nownews.mobile.Common.Utility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloadService implements Runnable {

    public final static String FILE_URL = "apkUrl";
    public final static String FILENAME = "fileName";
    public final static String FOLDERPATH = "folderPath";
    public final static String UI_MESSENGER = "uiMessenger";
    public final static String CANCEL_FLAG = "cancelFlag";
    public final static int FILE_DOWNLOAD_PERSENTAGE = 0x321;
    public static boolean isNeedStop = false;
    private final String TAG = getClass().getSimpleName();
    private String mApkUrl;
    private String mFileName;
    private String mFolderPath;
    private Handler mHandler;

    public FileDownloadService(String aUrl, String aFileName, String aFolderPath, Handler aHandler) {
        mHandler = aHandler;
        mApkUrl = aUrl;
        mFileName = aFileName;
        mFolderPath = aFolderPath;
        isNeedStop = false;
    }

    @Override
    public void run() {

        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " START!! #########");
//		Message message = AppController.mAppControllerInstance.obtainMessage();

        if (Utility.DEBUG) Log.e(TAG, "mApkUrl: " + mApkUrl);
        if (Utility.DEBUG) Log.e(TAG, "mFileName: " + mFileName);
        if (Utility.DEBUG) Log.e(TAG, "mFolderPath: " + mFolderPath);

        File folder = new File(mFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File output = new File(mFolderPath, mFileName);
        if (output.exists()) {
            output.delete();
        }

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(mApkUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength(); //bytes
            if (Utility.DEBUG) Log.i(TAG, "Content Length: " + fileLength);

            fileOutputStream = new FileOutputStream(output);
            inputStream = connection.getInputStream();
            InputStream reader = new BufferedInputStream(inputStream);

            byte[] data = new byte[1024];

            int next = -1;
            int size = 0;
            while ((next = reader.read(data)) != -1) {

                if (isNeedStop) {
                    break;
                }

                fileOutputStream.write(data, 0, next);

                size += next;
                if (mHandler != null) {
                    Message message = new Message();
                    message.what = FILE_DOWNLOAD_PERSENTAGE;
                    message.arg1 = size;
                    message.arg2 = fileLength;
                    message.obj = mFileName;
                    mHandler.sendMessage(message);
                }
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Utility.DEBUG) Log.d(TAG, "######### " + TAG + " END!! #########");
    }

}

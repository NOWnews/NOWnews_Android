package com.nownews.mobile.Download;

import android.os.AsyncTask;
import android.util.Log;

import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Download.DownloadListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ChengYuanChin on 2017/4/27.
 */

public class DownloadAsyncTask extends AsyncTask<String, Integer, String> {
    private int progress = 0;
    private DownloadListener.DownloadStatus downloadStatus;
    /** 停止下載文件
     * true: 停止下載
     * false: 執行下載
     */
    private boolean exit = false;
    /* 已下載文件長度 */
    private int downloadSize = 0;
    /* 原始文件長度 */
    private int fileSize = 0;
    /* 本地保存路徑 */
    private File fileSaveDir;
    /* 下載位置 */
    private String downloadurl;
    /* 下載監聽 */
    private DownloadListener.DownloadStatus listener = null;

    public DownloadAsyncTask(DownloadListener.DownloadStatus listener) {
        this.downloadStatus = listener;
        this.fileSaveDir = new File(Constants.Download.Folder);
        this.listener = listener;
        // 判斷目錄是否存在
        if (!fileSaveDir.exists())
            fileSaveDir.mkdirs();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting download");
    }

    @Override
    protected String doInBackground(String... downloadurl) {
        this.downloadurl = downloadurl[0];
        publishProgress(this.progress);
        File output = new File(this.fileSaveDir, Constants.Download.FileName);
        if (output.exists()) {
            output.delete();
        }

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(this.downloadurl);
            URLConnection connection = url.openConnection();
            connection.connect();

            this.fileSize = connection.getContentLength(); //bytes
            if (com.nownews.mobile.Common.Utility.DEBUG)
                print("Content Length: " + fileSize);

            fileOutputStream = new FileOutputStream(output);
            inputStream = connection.getInputStream();
            InputStream reader = new BufferedInputStream(inputStream);

            byte[] data = new byte[1024];

            int next = -1;
            while ((next = reader.read(data)) != -1) {
                if (this.exit) {
                    break;
                }
                fileOutputStream.write(data, 0, next);
                this.downloadSize += next;
                publishProgress(((int) (this.downloadSize * 100L / this.fileSize)));
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
        if (com.nownews.mobile.Common.Utility.DEBUG)
            print("#########  END!! #########");

        return this.downloadurl;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        this.progress = values[0];
        this.downloadStatus.onDownloadSize(this.downloadurl, this.progress);
    }

    /**
     * After completing background task
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        if (this.progress == 100
                && this.downloadSize == this.fileSize) {
            this.downloadStatus.downloadFinish(file_url);
        }
    }

    /**
     * 退出下載
     */
    public void exit() {
        this.exit = true;
    }
    public boolean getExit() {
        return  this.exit;
    }

    /**
     * 取得文件大小
     */
    public int getFileSize() {
        return this.fileSize;
    }

    private void print(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }
}

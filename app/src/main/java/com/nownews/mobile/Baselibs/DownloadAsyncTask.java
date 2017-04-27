package com.nownews.mobile.Baselibs;

import android.os.AsyncTask;
import android.util.Log;

import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Download.DownloadListener;
import com.nownews.mobile.Download.DownloadThread;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ChengYuanChin on 2017/4/27.
 */

public class DownloadAsyncTask extends AsyncTask<String, Integer, String> {
    private int progress = 0;
    private DownloadListener.DownloadStatus downloadStatus;
    private int threadNum = 3;
    /** 停止下載文件
     * true: 停止下載
     * false: 執行下載
     */
    private boolean exit = false;
    /* 已下載文件長度 */
    private int downloadSize = 0;
    /* 原始文件長度 */
    private int fileSize = 0;
    /* 線程數 */
    private DownloadThread[] threads;
    /* 本地保存路徑 */
    private File fileSaveDir;
    /* 本地保存文件 */
    private File saveFile;
    /* 緩存個縣城下載長度 */
    private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();
    /* 每條線程下載長度 */
    private int block;
    /* 下載位置 */
    private String downloadurl;
    /* 下載監聽 */
    private DownloadListener.DownloadStatus listener = null;

    public DownloadAsyncTask(DownloadListener.DownloadStatus listener) {
        this.downloadStatus = listener;
        this.fileSaveDir = new File(Constants.Download.Folder);
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
        try {
            publishProgress(this.progress);
            this.downloadurl = downloadurl[0];
            URL url = new URL(downloadurl[0]);
            this.threads = new DownloadThread[threadNum];
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.connect();
            printResponseHeader(conn);
            if (conn.getResponseCode() == 200) {
                this.fileSize = conn.getContentLength();
                if (this.fileSize <= 0)
                    throw new Exception("Unknown file size");
                this.saveFile = new File(this.fileSaveDir, getFileName(conn));

                if (this.data.size() == this.threads.length) {
                    // 計算所有線程已下載長度
                    for (int i = 0; i < this.threads.length; i++) {
                        this.downloadSize += this.data.get(i + 1);
                    }
                    print("已下載長度：" + this.downloadSize);
                }
                // 計算每條線程數據長度
                this.block = (this.fileSize % this.threads.length) == 0 ? this.fileSize / this.threads.length : this.fileSize / this.threads.length + 1;
            } else {
                throw new Exception("server no response");
            }
            // 執行下載
            download();
        } catch (Exception e) {
            print(e.toString());
        }

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
        if (this.progress == 100) {
            this.downloadStatus.downloadFinish(file_url);
        }
    }

    private void download() throws Exception {
        try {
            RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
            if (this.fileSize > 0)
                randOut.setLength(this.fileSize);
            randOut.close();
            URL url = new URL(this.downloadurl);

            if (this.data.size() != this.threads.length) {
                // 歸零
                this.data.clear();
                for (int i = 0; i < this.threads.length; i++) {
                    this.data.put(i + 1, 0);
                }
                this.downloadSize = 0;
            }
            for (int i = 0; i < this.threads.length; i++) {
                int downLength = this.data.get(i + 1);
                if (downLength < this.block && this.downloadSize < this.fileSize) {
                    this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i +1), i + 1);
                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                } else {
                    this.threads[i] = null;
                }
            }
            // 下載未完成
            boolean notFinish = true;
            // 循環判斷所有線程是否完成下載
            while (notFinish) {
                if (this.exit) {
                    break;
                }
                Log.e("AAA", "1111");
                Thread.sleep(3000);
                // 假設全部已完成
                notFinish = false;
                for (int i = 0; i < this.threads.length; i++) {
                    Log.e("AAA", "2222");
                    if (this.threads[i] != null && !this.threads[i].isFinish()) {
                        // 發現線程尚未完成
                        notFinish = true;
                        Log.e("AAA", "3333");
                        if (this.threads[i].getDownloadLength() == -1) {
                            // 下載失敗，重新下載
                            Log.e("AAA", "4444");
                            this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i + 1), i + 1);
                            this.threads[i].setPriority(7);
                            this.threads[i].start();
                        }
                    }
                }
                // 通知目前已下載完成數據長度，換算百分比
                publishProgress((int) (this.downloadSize * 100L / this.fileSize));
            }
            Log.e("AAA", "END");
        } catch (Exception e) {
            print(e.toString());
            throw new Exception("file download error");
        }
    }

    /**
     * 取得線程數
     */
    public int getThreadSize() {
        return this.threads.length;
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

    /**
     * 累積已下載大小
     * @param size
     */
    public synchronized void append(int size) {
        this.downloadSize += size;
    }

    /**
     * 更新指定線程最後下載位置
     * @param threadId
     * @param pos
     */
    public synchronized void update(int threadId, int pos) {
        this.data.put(threadId, pos);
    }

    /**
     * 取得文件名
     */
    private String getFileName(HttpURLConnection conn) {
        String url = null;
        try {
            url = URLDecoder.decode(this.downloadurl, "UTF-8");
            String filename = url.substring(url.lastIndexOf('/') + 1);
            if (filename == null || "".equals(filename.trim())) {// 如果取得不到文件名稱
                for (int i = 0;; i++) {
                    String mine = conn.getHeaderField(i);
                    if (mine == null)
                        break;
                    if ("content-disposition".equals(conn.getHeaderFieldKey(i)
                            .toLowerCase())) {
                        Matcher m = Pattern.compile(".*filename=(.*)").matcher(
                                mine.toLowerCase());
                        if (m.find())
                            return m.group(1);
                    }
                }
                filename = UUID.randomUUID() + ".tmp";// 默認取一個文件名
            }
            Log.e("FileDownloader", "getFileName:" + filename);
            return filename;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 取得Http響應標頭字段
     *
     * @param http
     * @return
     */
    public static Map<String, String> getHttpResponseHeader(
            HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null)
                break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }

    /**
     * 打印Http響應標頭字段
     *
     * @param http
     */
    public void printResponseHeader(HttpURLConnection http) {
        Map<String, String> header = getHttpResponseHeader(http);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey() + ":" : "";
            print(key + entry.getValue());
        }
    }

    private void print(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }
}

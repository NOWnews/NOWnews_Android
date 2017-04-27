package com.nownews.mobile.Download;

import android.util.Log;

import com.nownews.mobile.Baselibs.DownloadAsyncTask;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public class DownloadThread extends Thread {
    private static final String TAG = "DOwnloadThread";
    private File saveFile;
    private URL downloadUrl;
    private  int block;
    // 下載開始位置
    private int threadId = -1;
    private int downloadLength;
    private boolean finish = false;
    private DownloadAsyncTask doanloader;

    public DownloadThread(DownloadAsyncTask loader, URL url, File saveFile, int block, int length, int threadId) {
        this.downloadUrl = url;
        this.saveFile = saveFile;
        this.block = block;
        this.doanloader = loader;
        this.threadId = threadId;
        this.downloadLength = length;
    }

    @Override
    public void run() {
        if (this.downloadLength < this.block) {
            // 未下載成完
            try {
                HttpURLConnection http = (HttpURLConnection) downloadUrl.openConnection();
                http.setConnectTimeout(5 * 1000);
                http.setRequestMethod("GET");
                // 瀏覽器可接受的MIME類型
                http.setRequestProperty(
                        "Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                http.setRequestProperty("Accept-Language", "zh-CN"); // 瀏覽器所希望的語言，當瀏覽器能夠提供一種以上的語言版本時要用到
                http.setRequestProperty("Referer", this.downloadUrl.toString());// 包含一個URL，用戶從該URL代表的葉面出發訪問當時請求的頁面
                // 字符集
                http.setRequestProperty("Charset", "UTF-8");
                // 開始位置
                int startPos = this.block * (this.threadId - 1) + this.downloadLength;
                // 結束位置
                int endPos = this.block * threadId - 1;
                // 設置獲取實體數據範圍
                http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                // 瀏覽器類型，如果Servlet返回的內容與瀏覽器類型有關，則該值非常有用
                http.setRequestProperty(
                        "User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                http.setRequestProperty("Connection", "Keep-Alive");
                // 得到輸入流
                InputStream inStream = http.getInputStream();
                byte[] buffer = new byte[1024];
                int offset = 0;
                print("Thread " + this.threadId
                        + " start download from position " + startPos);
                // 隨機訪問文件
                RandomAccessFile threadfile = new RandomAccessFile(
                        this.saveFile, "rwd");
                // 定位到pos位置
                threadfile.seek(startPos);
                while (!this.doanloader.getExit()
                        && (offset = inStream.read(buffer)) != -1) {
                    // 寫入文件
                    threadfile.write(buffer, 0, offset);
                    // 累加下載的大小
                    this.downloadLength += offset;
                    // 更新指定線程下載最後的位置
                    this.doanloader.update(this.threadId, this.downloadLength);
                    // 累加已下載大小
                    this.doanloader.append(offset);
                }
                threadfile.close();
                inStream.close();
                print("Thread " + this.threadId + " download finish");
                this.finish = true;
            } catch (Exception e) {
                this.downloadLength = -1;
                Log.e("run", "catch");
                print("Thread " + this.threadId + ":" + e.toString());
            }
        }
    }

    private static void print(String msg) {
        Log.e(TAG, msg);
    }

    /**
     * 下載是否完成
     *
     * @return
     */
    public boolean isFinish() {
        return this.finish;
    }

    /**
     * 已經下載的內容大小
     *
     * @return 如果返回值為-1，代表下載失敗
     */
    public long getDownloadLength() {
        return this.downloadLength;
    }
}

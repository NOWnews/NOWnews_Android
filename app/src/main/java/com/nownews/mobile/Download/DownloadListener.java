package com.nownews.mobile.Download;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public class DownloadListener {

    // Download Status
    public interface DownloadStatus {
        public void onDownloadSize(String url, int size);
        public void downloadFinish(String url);
    }
}

package com.nownews.mobile.BroadcastReceiver;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nownews.R;
import com.nownews.mobile.AlbumPage.AlbumPage;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.VideoNewsPage.VideoNewsPage;
import com.nownews.mobile.Widget.WebActivity;

/**
 * Created by cindy on 2017/2/18.
 */

public class ExternalLinkOpen extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_link_open);

        if(Utility.DEBUG) Log.v(TAG, "onCreate");

        processAction();

    }

    private void processAction(){

        String action = getIntent().getAction();

        if(action!=null && action.equals(Intent.ACTION_VIEW)){
            Uri uri = getIntent().getData();
            String path = uri.getPath();
            int id = -1;

            if(uri.toString().contains(WebAPIUrl.NOWNEWS_PC_NEWS_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Pc版新聞內頁 uri: " + uri);
                id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoNewsPage(id, uri.toString());

            }else if(uri.toString().contains(WebAPIUrl.NOWNEWS_PC_PHOTO_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Pc版圖集內頁 uri: " + uri);
                id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoAlbumPage(id, uri.toString());

            }else if(uri.toString().contains(WebAPIUrl.NOWNEWS_PC_VIDEO_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Pc版影音內頁 uri: " + uri);
                id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoVideoNewsPage(id, uri.toString());

            }else if(uri.toString().contains(WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Mobile Web新聞內頁 uri: " + uri);
                id = Integer.valueOf(path.replace("/news/", ""));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoNewsPage(id, uri.toString());

            }else if(uri.toString().contains(WebAPIUrl.NOWNEWS_MOBIEL_WEB_PHOTO_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Mobile Web圖集內頁 uri: " + uri);
                id = Integer.valueOf(path.replace("/photo/", ""));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoAlbumPage(id, uri.toString());

            }else if(uri.toString().contains(WebAPIUrl.NOWNEWS_MOBIEL_WEB_VIDEO_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Mobile Web影音內頁 uri: " + uri);
                id = Integer.valueOf(path.replace("/video/", ""));
                if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                gotoVideoNewsPage(id, uri.toString());

            }else{

                if(Utility.DEBUG) Log.v(TAG, "其他NOWnews網頁 uri: " + uri);
                Intent intent = new Intent();
                intent.setClass(this, WebActivity.class);
                intent.putExtra(WebActivity.KEY_URL, uri.toString());
                startActivity(intent);
                finish();
                return;

            }

        }

    }

    private void gotoNewsPage(int aNewsId, String aUrl) {

        if (Utility.DEBUG) Log.i(TAG, "aNewsId: " + aNewsId);

        String eventAction = "外部開啟 " + aUrl;
        GoogleAnalyticsFunction.sendHitInfo(this, "外部開啟", eventAction, "");
        Intent intent = new Intent();
        intent.setClass(this, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_ID, aNewsId);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_SINGAL_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, "外部開啟");
        UserDataInfo.isSingalNewsFromAction = true;
        startActivity(intent);
        finish();

    }

    private void gotoAlbumPage(int aNewsId, String aUrl) {

        if (Utility.DEBUG) Log.i(TAG, "aNewsId: " + aNewsId);

        String eventAction = "外部開啟 " + aUrl;
        GoogleAnalyticsFunction.sendHitInfo(this, "外部開啟", eventAction, "");
        Intent intent = new Intent();
        intent.setClass(this, AlbumPage.class);
        intent.putExtra(AlbumPage.KEY_ALBUM_ID, aNewsId);
        intent.putExtra(AlbumPage.KEY_ALBUM_CATEGORY, "外部開啟");
        startActivity(intent);
        finish();

    }

    private void gotoVideoNewsPage(int aNewsId, String aUrl) {

        if (Utility.DEBUG) Log.i(TAG, "aNewsId: " + aNewsId);

        String eventAction = "外部開啟 " + aUrl;
        GoogleAnalyticsFunction.sendHitInfo(this, "外部開啟", eventAction, "");
        Intent intent = new Intent();
        intent.setClass(this, VideoNewsPage.class);
        intent.putExtra(VideoNewsPage.KEY_NEWS_ID, aNewsId);
        intent.putExtra(VideoNewsPage.KEY_NEWS_TYPE, VideoNewsPage.TYPE_SINGAL_NEWS);
        intent.putExtra(VideoNewsPage.KEY_NEWS_CATEGORY, "外部開啟");
        UserDataInfo.setVideoNewsList(null);
        startActivity(intent);
        finish();

    }

}

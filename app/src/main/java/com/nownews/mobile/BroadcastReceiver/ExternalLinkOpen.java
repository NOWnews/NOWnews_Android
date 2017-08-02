package com.nownews.mobile.BroadcastReceiver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.NewsPage.NewsPage;
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
            if(Utility.DEBUG) Log.v(TAG, "path: " + path);
            int id = -1;

            if(uri.toString().contains(WebAPIUrl.NOWNEWS_PC_NEWS_DOMAIN)
                    || uri.toString().contains(WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN)){

                if(Utility.DEBUG) Log.v(TAG, "Pc版新聞內頁 or Mobile Web新聞內頁 uri: " + uri);
                try{
                    id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                    if(Utility.DEBUG) Log.v(TAG, "id: " + id);
                    gotoNewsPage(id, uri.toString());
                }catch(NumberFormatException e){
                    e.printStackTrace();
                    gotoOtherTypeOfNOWnewsPage(uri);
                }

            }else{
                gotoOtherTypeOfNOWnewsPage(uri);
                return;

            }

        }

    }

    private void gotoOtherTypeOfNOWnewsPage(Uri uri){
        if(Utility.DEBUG) Log.v(TAG, "其他NOWnews網頁 uri: " + uri);
        Intent intent = new Intent();
        intent.setClass(this, WebActivity.class);
        intent.putExtra(WebActivity.KEY_URL, uri.toString());
        startActivity(intent);
        finish();
    }

    private void gotoNewsPage(int aNewsId, String aUrl) {

        if (Utility.DEBUG) Log.i(TAG, "aNewsId: " + aNewsId);

        Intent intent = new Intent();
        intent.setClass(this, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_ID, aNewsId);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_SINGAL_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, "外部開啟");
        UserDataInfo.isSingalNewsFromAction = true;
        startActivity(intent);
        finish();

    }

}

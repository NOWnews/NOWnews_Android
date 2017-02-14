package com.nownews.mobile.Live;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.VideoView;

import com.nownews.R;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.LiveListJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cindy on 2017/1/24.
 */

public class LivePlayer extends Activity {

    private final String TAG = getClass().getSimpleName();
    public final static String KEY_PLAY_URL = "keyPlayUrl";
    public final static String KEY_CATEGORY_INDEX = "categoryIndex";
    public final static String KEY_CHANNEL_INDEX = "channelIndex";
    private String mUrl;
    private int mCategoryIndex;
    private int mChannelIndex;

    private VideoView vLivePlayer;
    private LinearLayout vChannelListGroup;
    private ListView vCategoryList;
    private ListView vChannelList;
    private List<LiveListJson.Data> mLiveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveplayer);

        processIntent();
        processView();
        processMenu();
        play();

    }

    private void processIntent(){
        if(getIntent()!=null){
            mUrl = getIntent().getStringExtra(KEY_PLAY_URL);
            mCategoryIndex = getIntent().getIntExtra(KEY_CATEGORY_INDEX, 0);
            mChannelIndex = getIntent().getIntExtra(KEY_CHANNEL_INDEX, 0);
        }
        mLiveList = UserDataInfo.getLiveList();

    }

    private void processView(){

        vLivePlayer = (VideoView)findViewById(R.id.live_player);
        vChannelListGroup = (LinearLayout)findViewById(R.id.channel_list_group);
        vCategoryList = (ListView)findViewById(R.id.category);
        vChannelList = (ListView)findViewById(R.id.channel_list);

    }

    private void processMenu(){

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.Data data : mLiveList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("categoryName", data.categoryName);
            items.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.widget_live_player_gategory_item, new String[]{"categoryName"}, new int[]{R.id.gategory_item});
        vCategoryList.setAdapter(adapter);
        vCategoryList.setOnItemClickListener(mCategoryItemClickListener);
        vCategoryList.setSelection(mCategoryIndex);
        vCategoryList.setSelector(R.color.light_gray_tran);
        vCategoryList.performItemClick(vCategoryList.getChildAt(mCategoryIndex), mCategoryIndex, R.id.gategory_item);

    }

    private AdapterView.OnItemClickListener mCategoryItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            processChannelList(position);

        }
    };

    private List<LiveListJson.ChannelList> mCurrentChannelList;
    private void processChannelList(int position){

        mCurrentChannelList = mLiveList.get(position).list;
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.ChannelList list : mCurrentChannelList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("channelName", list.title);
            items.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.widget_live_player_channel_item, new String[]{"channelName"}, new int[]{R.id.channel_item});
        vChannelList.setAdapter(adapter);
        vChannelList.setSelection(mChannelIndex);
        vChannelList.setSelector(R.color.news_list_category_text_color);
        vChannelList.setOnItemClickListener(mChannelListItemClickListener);

    }

    private AdapterView.OnItemClickListener mChannelListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            String title = mCurrentChannelList.get(position).title;
            if(Utility.DEBUG) Log.v(TAG, "title: " + title);
            mUrl = mCurrentChannelList.get(position).path;
            play();

        }
    };

    private void play(){

        vLivePlayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    showAndHideMenu();
                    return true;
                }

                return false;
            }
        });
        vLivePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                vLivePlayer.start();
                hideMenu();

            }
        });
        vLivePlayer.setVideoURI(Uri.parse(mUrl));

    }

    private void showAndHideMenu(){
        if(vChannelListGroup.getVisibility()==View.GONE){
            showMenu();
        }else{
            hideMenu();
        }
    }

    private void showMenu(){
        vChannelListGroup.setVisibility(View.VISIBLE);
    }

    private void hideMenu(){
        vChannelListGroup.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(vChannelListGroup.getVisibility()==View.GONE){
            //ask again
            if(isBackFirstClick){
                isBackFirstClick = false;
                Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
                count = 0;
                mTimerHandler.post(mTimerRunnable);
            }else{
                mTimerHandler.removeCallbacks(mTimerRunnable);
                super.onBackPressed();
            }
        }else{
            hideMenu();
        }
    }

    private boolean isBackFirstClick = true;
    private int count = 0;
    private Handler mTimerHandler = new Handler();
    private Runnable mTimerRunnable = new Runnable() {

        @Override
        public void run() {

            count++;
            if(Utility.DEBUG)Log.e(TAG, "count: " + count);
            if(count==7){
                isBackFirstClick = true;
                mTimerHandler.removeCallbacks(mTimerRunnable);
            }else{
                mTimerHandler.postDelayed(mTimerRunnable, 500);
            }

        }
    };

}

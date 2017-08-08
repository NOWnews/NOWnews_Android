package com.nownews.mobile.Live;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.VideoView;

import com.core.adnsdk.AdObject;
import com.core.adnsdk.AdReward;
import com.core.adnsdk.AdRewardListener;
import com.core.adnsdk.ErrorMessage;
import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.NewHome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cindy on 2017/1/24.
 */

public class LivePlayer extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    public final static String KEY_PLAY_URL = "keyPlayUrl";
    public final static String KEY_CATEGORY_INDEX = "categoryIndex";
    public final static String KEY_CHANNEL_INDEX = "channelIndex";
    public final static String KEY_WATCH_TIME = "watchTime";
    public final static String KEY_VIDEO_AD = "videoAD";
    private String mUrl;
    private int mCategoryIndex;
    private int mChannelIndex;
    private int mWatchTime;
    private boolean mVideoAD;

    private VideoView vLivePlayer;
    private LinearLayout vChannelListGroup;
    private ListView vCategoryList;
    private ListView vChannelList;
    private List<LiveListJson.DataBean> mLiveList;
    private LinearLayout vAdFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveplayer);

        processIntent();
        processView();
        processMenu();
        playVM5AD();

    }

    private void processIntent(){
        if(getIntent()!=null){
            mUrl = getIntent().getStringExtra(KEY_PLAY_URL);
            mCategoryIndex = getIntent().getIntExtra(KEY_CATEGORY_INDEX, 0);
            mChannelIndex = getIntent().getIntExtra(KEY_CHANNEL_INDEX, 0);
            mWatchTime = getIntent().getIntExtra(KEY_WATCH_TIME, 0);
            mVideoAD = getIntent().getBooleanExtra(KEY_VIDEO_AD, false);
        }
        mLiveList = UserDataInfo.getLiveList();
    }

    private void processView(){

        vLivePlayer = (VideoView)findViewById(R.id.live_player);
        vChannelListGroup = (LinearLayout)findViewById(R.id.channel_list_group);
        vAdFragment = (LinearLayout)findViewById(R.id.ad_fragment);
        vCategoryList = (ListView)findViewById(R.id.category);
        vChannelList = (ListView)findViewById(R.id.channel_list);

    }

    private Fragment mRewardAD;
    private void playVM5AD(){
        if(!mVideoAD){
            return;
        }
        vAdFragment.setVisibility(View.VISIBLE);

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        mRewardAD = LiveRewardAD.getInstance(mVM5ADListener);
        trans.add(R.id.ad_fragment, mRewardAD).commit();

    }

    private void processMenu(){

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.DataBean data : mLiveList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("categoryName", data.getCategoryName());
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

            mCategoryIndex = position;
            processChannelList(position);

        }
    };

    private List<LiveListJson.DataBean.ListBean> mCurrentChannelList;
    private void processChannelList(int position){

        mCurrentChannelList = mLiveList.get(position).getList();
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.DataBean.ListBean list : mCurrentChannelList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("channelName", list.getTitle());
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

            String title = mCurrentChannelList.get(position).getTitle();
            if(Utility.DEBUG) Log.v(TAG, "title: " + title);
            mUrl = mCurrentChannelList.get(position).getPath();
            play(position);

        }
    };

    private long startTime;
    private Handler mTimeCounterHandler = new Handler();
    private void startTimer(){

        startTime = System.currentTimeMillis();
        if(mTimeCounterHandler!=null){
            mTimeCounterHandler.removeCallbacks(mTimeCounterRunnable);
            mTimeCounterHandler.post(mTimeCounterRunnable);
        }

    }

    private Runnable mTimeCounterRunnable = new Runnable() {
        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();
            long spentTime = currentTime - startTime;
            //已過分鐘數
            long minutes = (spentTime/1000)/60;
            //已過秒數
            long seconds = (spentTime/1000)%60;
            if(Utility.DEBUG)Log.d(TAG, "時間已過: " + (minutes<10? "0"+minutes:minutes) + ":" +(seconds<10? "0"+seconds:seconds));

            if(minutes>0 && minutes%mWatchTime==0){ //mWatchTime分鐘到
//            if(minutes>0 && minutes%1==0){ //1分鐘到
                if(Utility.DEBUG)Log.e(TAG, mWatchTime + "分鐘到");
//                if(Utility.DEBUG)Log.e(TAG, "1分鐘到");
                if(!mVideoAD){
                    SharedPreferencesMethods sharedPreferencesMethods = new SharedPreferencesMethods(LivePlayer.this);
                    sharedPreferencesMethods.setLiveStopWatchingTime(currentTime);
                    sharedPreferencesMethods.setShareAppSuccess(false);
                    sharedPreferencesMethods.unRegistContext(LivePlayer.this);
                    setResult(NewHome.RESULT_CODE_FROM_LIVE);
                    finish();
                }else{
                    vLivePlayer.stopPlayback();
                    playVM5AD();
                }
            }else{
                if(mTimeCounterHandler!=null){
                    mTimeCounterHandler.postDelayed(this, 1000);
                }
            }

        }
    };

    private void play(int position){

        setHitInfo(position);

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

    private void setHitInfo(int position){
        String title = mCurrentChannelList.get(position).getTitle();
        String categoryName = mLiveList.get(mCategoryIndex).getCategoryName();
        GoogleAnalyticsFunction.sendHitInfo(this, getString(R.string.live), categoryName, title);
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
                mTimerHandler = null;
                if(mTimeCounterHandler!=null){
                    mTimeCounterHandler.removeCallbacks(mTimeCounterRunnable);
                    mTimeCounterRunnable = null;
                    mTimeCounterHandler = null;
                }
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

    private AdRewardListener mVM5ADListener = new AdRewardListener() {
        @Override
        public void onAdLoaded(AdObject adObject) {
            if(Utility.DEBUG)Log.e(TAG, "廣告完成載入");
            if(mRewardAD !=null){
                ((LiveRewardAD) mRewardAD).showAD();
            }
        }

        @Override
        public void onError(ErrorMessage errorMessage) {
            if(Utility.DEBUG)Log.e(TAG, "SDK 出現錯誤\n errorMessage: " + errorMessage.toString());
            startVideo();
        }

        @Override
        public void onAdClicked() {
            if(Utility.DEBUG)Log.e(TAG, "廣告被點擊");
        }

        @Override
        public void onAdFinished() {
            if(Utility.DEBUG)Log.e(TAG, "廣告點擊完成跳轉後");
            startVideo();
        }

        @Override
        public void onAdReleased() {
            if(Utility.DEBUG)Log.e(TAG, "廣告完成卸載並且釋放所有資源");
        }

        @Override
        public boolean onAdWatched() {
            if(Utility.DEBUG)Log.e(TAG, "影片播放完畢，要自動載入下一檔廣告請回傳 true，否則回傳 false");
            return false;
        }

        @Override
        public void onAdImpressed() {
            if(Utility.DEBUG)Log.e(TAG, "廣告曝光");
        }

        @Override
        public String onAdRewarded(AdReward.RewardInfo rewardInfo) {
            if(Utility.DEBUG)Log.e(TAG, "獎勵廣告\n rewardInfo: " + rewardInfo.toString());
            return null;
        }

        @Override
        public void onAdReplayed() {
            if(Utility.DEBUG)Log.e(TAG, "廣告重新撥放");
        }

        @Override
        public void onAdClosed() {
            if(Utility.DEBUG)Log.e(TAG, "廣告關閉");
            startVideo();
        }
    };

    private void startVideo(){
        startTimer();
        play(mChannelIndex);
    }

}

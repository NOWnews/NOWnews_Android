package com.nownews.mobile.VideoNewsPage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmcm.adsdk.banner.CMAdView;
import com.cmcm.adsdk.banner.CMBannerAdListener;
import com.cmcm.adsdk.banner.CMBannerAdSize;
import com.cmcm.adsdk.banner.CMNativeBannerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Controller.BitmapController.ImageLoadingListener;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbumPage;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Json.VideosInfoJson;
import com.nownews.mobile.NewsPage.NewsPageRecyclerViewAdapter;
import com.nownews.mobile.Widget.CustomImageTopcrop;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class VideoNewsPageRecyclerViewFragment extends Fragment{

    public final static String KEY_NEWS_URL = "newsUrl";
    public final static String KEY_NEWS_ID = "newsId";
    public final static String KEY_NEWS_POSITION = "newsPosition";
    private final String TAG = getClass().getSimpleName();
    public boolean isOnPause = false;
    private RelativeLayout vLoadingLayout;
    private ImageView vBigImage;
    private LinearLayout vBody;
    private SwipeRefreshLayout vRefreshLayout;
    private TextView vError;
    private RecyclerView vContentRecyclerView;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private String mNewsUrl;
    private int mNewsId;
    private VideosInfoJson mVideoNewsInfo;
    private ArrayList<String> mImageUrlList;
    private HashMap<Integer, SoftReference<ImageView>> mImageViewList;
    private SharedPreferencesMethods mSharedPref;
    private boolean isRefereshing = false;

    private ArrayList<ConcurrentHashMap<String, Object>> mContentList;
    public final static String KEY_CONTEXT_TEXT = "context_text";
    public final static String KEY_CONTEXT_IMAGE = "context_image";
    public final static String KEY_CONTEXT_IMAGE_TEXT = "context_image_text";


    private AdListener mDfpAdListener = new AdListener() {

        @Override
        public void onAdClosed() {
            if (Utility.DEBUG) Log.e(TAG, "onAdClosed");
            super.onAdClosed();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            if (Utility.DEBUG) Log.e(TAG, "onAdFailedToLoad");
            super.onAdFailedToLoad(errorCode);
        }

        @Override
        public void onAdLeftApplication() {
            if (Utility.DEBUG) Log.e(TAG, "onAdLeftApplication");
            super.onAdLeftApplication();
        }

        @Override
        public void onAdLoaded() {
            if (Utility.DEBUG) Log.e(TAG, "onAdLoaded");
            super.onAdLoaded();
        }

        @Override
        public void onAdOpened() {
            if (Utility.DEBUG) Log.e(TAG, "onAdOpened");
            super.onAdOpened();
        }

    };

    public final static int KEY_NEWS_INFO_PREPARE_DONE = 0x456;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_VIDEOS_INFO_DONE:
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vRefreshLayout.setRefreshing(false);
                    }
                    mVideoNewsInfo = (VideosInfoJson) msg.obj;
                    if (mVideoNewsInfo != null) {
                        while (true) {
                            if (isAdded()) {
                                processNews();
                                break;
                            }
                        }
                    }
                    break;
                case ParameterSet.GET_VIDEOS_INFO_FAILED:
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(getActivity());
                    break;
                case KEY_NEWS_INFO_PREPARE_DONE:
                    mYoutubeContentViewId = msg.arg1;
                    if(mOnPageLoadFinishedListener!=null){
                        mOnPageLoadFinishedListener.OnPageLoadFinished(VideoNewsPageRecyclerViewFragment.this, mCurrentNewsPosition);
                    }
                    break;
            }

        }

    };

    private int mYoutubeContentViewId = -1;
    public VideoNewsPageRecyclerViewFragment() {
        //DO NOTHING...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_page_recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Utility.DEBUG) Log.e(TAG, "onActivityCreated");

        startFragment();

    }

    public void startFragment() {

        initController();
        processArgument();
        processView();
        getVideoNewsInfo();

    }

    private int mCurrentNewsPosition;
    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {

            String aNewsUrl = bundle.getString(KEY_NEWS_URL);
            int aNewsId = bundle.getInt(KEY_NEWS_ID, -1);
            mCurrentNewsPosition = bundle.getInt(KEY_NEWS_POSITION, -1);

            if (Utility.DEBUG) Log.e(TAG, "VideoNewsPageFragment");
            if (Utility.DEBUG) Log.e(TAG, "aNewsUrl: " + aNewsUrl);
            if (Utility.DEBUG) Log.e(TAG, "aNewsId: " + aNewsId);
            if (Utility.DEBUG) Log.e(TAG, "mCurrentNewsPosition: " + mCurrentNewsPosition);

            mNewsUrl = aNewsUrl;
            mNewsId = aNewsId;
            if (Utility.DEBUG)Log.d(TAG, "mNewsUrl: " + mNewsUrl);
            if (mNewsUrl != null && (mNewsUrl.startsWith("/n/")
                    || mNewsUrl.contains("/news/")
                    || mNewsUrl.contains("/photo/"))) {
                mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1));
            } else if (mNewsUrl != null) {
                mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1, mNewsUrl.lastIndexOf("?")));
            }
        }
    }

    public void setData(String aNewsUrl) {

        mNewsUrl = aNewsUrl;
        if (mNewsUrl.startsWith("/n/")) {
            mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1));
        } else {
            mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1, mNewsUrl.lastIndexOf("?")));
        }
        getVideoNewsInfo();

    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
        mBitmapController.closeBitmapController();
        mBitmapController.clearCache();
        mSharedPref = new SharedPreferencesMethods(getActivity());
    }

    private void getVideoNewsInfo() {
        if (mApiController != null) {
            mApiController.getVideosInfo(mHandler, mNewsId);
        }
    }

    public void processView() {

        View view = getView();

        vError = (TextView)view.findViewById(R.id.error_message);
        vContentRecyclerView = (RecyclerView)view.findViewById(R.id.content_recyclerview);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getVideoNewsInfo();
            }
        });
    }

    private void processNews() {

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }
        mImageUrlList = new ArrayList<String>();
        vLoadingLayout.setVisibility(View.GONE);

        if (mVideoNewsInfo.image != null
                && mVideoNewsInfo.image != null
                && !mVideoNewsInfo.image.trim().isEmpty()) {
            String bigImgUrl = mVideoNewsInfo.image;
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mImageUrlList.add(bigImgUrl);
        }

        if (mVideoNewsInfo.body != null
                && mVideoNewsInfo.body.value!=null
                && !mVideoNewsInfo.body.value.trim().isEmpty()) {
            String body = mVideoNewsInfo.body.value;
            processBody(body);
            processRecyclerView();
        }

    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processRecyclerView(){

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        vContentRecyclerView.setLayoutManager(mLinearLayoutManager);
        VideoNewsPageRecyclerViewAdapter mAdapter = new VideoNewsPageRecyclerViewAdapter(getActivity(), mContentList, mVideoNewsInfo, mImageUrlList, mHandler);
        vContentRecyclerView.setAdapter(mAdapter);

    }

    private YouTubePlayer mYoutubePlayer;
    public void processVideo(){

        if(mVideoNewsInfo==null || mVideoNewsInfo.youtubeId==null){
            return;
        }

        removeYoutubeFragment();

        final String mCurrentYoutubeId = mVideoNewsInfo.youtubeId;

        YouTubePlayerSupportFragment youTubePlayerSupportFragment = YouTubePlayerSupportFragment.newInstance();
        FragmentTransaction transcation = getChildFragmentManager().beginTransaction();
        transcation.add(mYoutubeContentViewId, youTubePlayerSupportFragment).commit();
        setInitializedListener(youTubePlayerSupportFragment, mCurrentYoutubeId);

    }

    private void setInitializedListener(YouTubePlayerSupportFragment aYouTubePlayerSupportFragment, final String aYoutubeId) {
        aYouTubePlayerSupportFragment.initialize(UserDataInfo.YOUTUBE_DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    mYoutubePlayer = youTubePlayer;
                    if(mOnPageLoadFinishedListener!=null){
                        mOnPageLoadFinishedListener.OnYoutubeInitializationSuccess(VideoNewsPageRecyclerViewFragment.this, mCurrentNewsPosition, youTubePlayer, aYoutubeId);
                    }
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                String errorMessage = error.toString();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                if (Utility.DEBUG)Log.d("errorMessage:", errorMessage);
            }
        });
    }

    private void processBody(String body) {
        if (Utility.DEBUG) Log.e(TAG, "body: " + body);

        if(mContentList==null){
            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
        }else{
            mContentList.clear();
        }

        String citeContent = null;
        body = body.replace("<br />", "$$$");
        body = body.replace("<br>", "&&&");
        body = body.replace("<strong>", "(((");
        body = body.replace("</strong>", ")))");
        body = body.replace("&nbsp;", "");
        body = body.trim();
        if (Utility.DEBUG) Log.i(TAG, "body: " + body);
        Document doc = Jsoup.parse(body);
        Elements paragraph = doc.select("p");
        for (int i = 0; i < paragraph.size(); i++) {
            Element pContent = paragraph.get(i);
            String p = pContent.text();
            p = p.replace("$$$", "\n");
            p = p.replace("&&&", "\n");
            if (Utility.DEBUG) Log.w(TAG, "p: " + p);

            Elements img = pContent.select("img[src]");
            Elements cite = pContent.select("cite");
            if (cite != null && cite.size() > 0) {
                for (int j = 0; j < cite.size(); j++) {
                    Element imgElement = cite.get(j);
                    if (!imgElement.text().trim().equals("▲")) {
                        citeContent = imgElement.text();
                        if (Utility.DEBUG) Log.w(TAG, "citeContent: " + citeContent);
                        break;
                    }
                }
            }

            if (p.contains("▲")) {
                citeContent = p;
            }

            if (citeContent != null) {
                p = p.replace(citeContent, "");
                if (Utility.DEBUG) Log.d(TAG, "[p] after replace: " + p);
            }

            if (p != null && !p.trim().isEmpty()) {
                if (p.contains("延伸閱讀")) {
                    break;
                }

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(KEY_CONTEXT_TEXT, p);
                mContentList.add(map);

            }

            if (img != null && img.size() > 0) {

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

                for (int j = 0; j < img.size(); j++) {
                    Element imgElement = img.get(j);
                    String imgUrl = imgElement.attr("src");
                    imgUrl = Utility.getSrcFromImgapi(imgUrl);
//                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);

                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);
                        map.put(KEY_CONTEXT_IMAGE, imgUrl);
                        mImageUrlList.add(imgUrl);
                    }

                }

                if (citeContent != null && !citeContent.trim().equals("") && !citeContent.trim().equals("▲")) {
                    map.put(KEY_CONTEXT_IMAGE_TEXT, citeContent);
                }

                mContentList.add(map);
            }


        }
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");

        if (vBigImage != null) {
            Drawable drawable = vBigImage.getDrawable();
            if (Utility.DEBUG)
                Log.e(TAG, "drawable is null or not?? " + (drawable == null ? "true" : "false"));
            if (drawable != null) {
                drawable.setCallback(null);
                drawable = null;
            }
            if (Utility.DEBUG)
                Log.e(TAG, "drawable is null or not?? " + (drawable == null ? "true" : "false"));
            vBigImage.setImageDrawable(null);
            vBigImage = null;
        }

        if (mImageViewList != null) {
            for (int key : mImageViewList.keySet()) {
                if (Utility.DEBUG) Log.e(TAG, "key: " + key);
                SoftReference<ImageView> value = mImageViewList.get(key);
                ImageView imageView = value.get();
                if (imageView != null) {
                    Drawable imgDrawable = imageView.getDrawable();
                    if (Utility.DEBUG)
                        Log.e(TAG, "drawable is null or not?? " + (imgDrawable == null ? "true" : "false"));
                    if (imgDrawable != null) {
                        imgDrawable.setCallback(null);
                        imgDrawable = null;
                    }
                    if (Utility.DEBUG)
                        Log.e(TAG, "drawable is null or not?? " + (imgDrawable == null ? "true" : "false"));
                    imageView.setImageDrawable(null);
                    imageView = null;
                }
            }
            mImageViewList.clear();
            mImageViewList = null;
        }

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }

        if (vBody != null) {
            vBody.removeAllViews();
            vBody = null;
        }

        if(mSharedPref!=null){
            mSharedPref.unRegistContext(getActivity());
        }
        super.onDestroyView();
    }

    public void changeTextSize(String aTextSize) {
        if(vContentRecyclerView!=null && vContentRecyclerView.getAdapter()!=null){
            ((VideoNewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).changeTextSize(aTextSize);
        }
        if(mSharedPref!=null){
            mSharedPref.saveNewsContentTextSize(aTextSize);
        }
    }

    public void setHitInfo(String aNewsCategory) {
        String url = null;
        if (mNewsUrl != null && mNewsUrl.startsWith("/n/")) {
            url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsUrl;
        } else if (mNewsUrl != null) {
            url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsUrl.substring(mNewsUrl.indexOf("/n/"), mNewsUrl.lastIndexOf("?"));
        }
        if (url != null) {
            GoogleAnalyticsFunction.sendHitInfo(getActivity(), aNewsCategory, url, "");
        }
    }

    @Override
    public void onPause() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onPause()");
        isOnPause = true;
        removeYoutubeFragment();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onResume()");
        if (isOnPause) {
            isOnPause = false;
            getVideoNewsInfo();
        }
        super.onResume();
    }

    public void reload() {
        startFragment();
    }

    public VideosInfoJson getCurrentNewsInfo(){
        if(mVideoNewsInfo !=null){
            return mVideoNewsInfo;
        }
        return null;
    }

    private OnPageLoadFinishedListener mOnPageLoadFinishedListener;
    public void setOnPageLoadFinishedListener(OnPageLoadFinishedListener aOnPageLoadFinishedListener){
        mOnPageLoadFinishedListener = aOnPageLoadFinishedListener;
    }

    public interface OnPageLoadFinishedListener{
        public void OnPageLoadFinished(VideoNewsPageRecyclerViewFragment aCurrentFragment, int aCurrentFragmentPosition);
        public void OnYoutubeInitializationSuccess(VideoNewsPageRecyclerViewFragment aCurrentFragment, int aCurrentFragmentPosition, YouTubePlayer aYouTubePlayer, String aYoutubeId);
    }

    public void removeYoutubeFragment(){
        if (Utility.DEBUG)Log.w(TAG, "removeYoutubeFragment()");
        if(!getChildFragmentManager().beginTransaction().isEmpty()){
            getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().findFragmentById(R.id.youtube_content_view)).commit();
        }
        if(mYoutubePlayer!=null){
            mYoutubePlayer.release();
        }
    }

    public void newsInfoLoadFinished(){

    }

}

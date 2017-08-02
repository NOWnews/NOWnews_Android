package com.nownews.mobile.NewsPage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.NewsInfoJson;
import com.nownews.mobile.Json.RelationsNewsInfoJson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NewsPageRecyclerViewFragment extends Fragment {

    public final static String KEY_NEWS_ID = "newsId";
    public final static String KEY_NEWS_CATEGORY = "newsCategory";
    public final static String KEY_NEWS_BIG_CATEGORY = "bigCategory";
    private final String TAG = getClass().getSimpleName();
    public boolean isOnPause = false;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private String mNewsCategory;
    private String mBigCategory;
    private int mNewsId;
    private NewsInfoJson mNewsInfo;
    private List<RelationsNewsInfoJson.RelationsNewsBean> mRelationsNewsList;
    private ArrayList<String> mImageUrlList;
    private SharedPreferencesMethods mSharedPref;
    private String[] mEcoDefaultImageList;
    private boolean isRefereshing = false;

    //New View
    private TextView vError;
    private RecyclerView vContentRecyclerView;
    private RelativeLayout vLoadingLayout;
    private SwipeRefreshLayout vRefreshLayout;
    private FrameLayout vVideoFrameLayout;

    private ArrayList<ConcurrentHashMap<String, Object>> mContentList;
    public final static String KEY_CONTEXT_TEXT = "context_text";
    public final static String KEY_CONTEXT_TEXT_HTML = "context_text_html";
    public final static String KEY_CONTEXT_TEXT_LINK_HTML = "context_text_link_html";
    public final static String KEY_CONTEXT_TEXT_LINK = "context_text_link";
    public final static String KEY_CONTEXT_IMAGE = "context_image";
    public final static String KEY_CONTEXT_IMAGE_TEXT = "context_image_text";
    public final static String KEY_CONTEXT_IFRAME_YOUTUBE = "context_iframe_youtube";

    private boolean isNewsInfoLoadSucess = false;
    public boolean isNewsInfoLoadSucess(){
        return isNewsInfoLoadSucess;
    }
    private ApiHandler mHandler;
    private static class ApiHandler extends Handler {

        private final WeakReference<NewsPageRecyclerViewFragment> mFragment;

        public ApiHandler(NewsPageRecyclerViewFragment aFragment){
            mFragment = new WeakReference<NewsPageRecyclerViewFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            NewsPageRecyclerViewFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_NEWS_INFO_DONE:
                    fragment.mNewsInfo = (NewsInfoJson) msg.obj;
                    if (fragment.mNewsInfo != null) {
                        if (fragment.isAdded()) {
                            fragment.processNews();
                        }
                    }
                    break;
                case ParameterSet.GET_NEWS_INFO_FAILED:
                    //TODO [v4] Error process 錯誤處理
                    break;

                case ParameterSet.GET_RELATIONS_NEWS_INFO_DONE:
                    fragment.mRelationsNewsList = (List<RelationsNewsInfoJson.RelationsNewsBean>) msg.obj;
                    if (fragment.mRelationsNewsList != null) {
                        fragment.isNewsInfoLoadSucess = true;
                        if (fragment.isRefereshing) {
                            // Stop refresh animation
                            fragment.isRefereshing = false;
                            fragment.vRefreshLayout.setRefreshing(false);
                        }
                        if (fragment.isAdded()) {
                            fragment.processRecyclerView();
                        }
                    }
                    break;
                case ParameterSet.GET_RELATIONS_NEWS_INFO_FAILED:
                    fragment.isNewsInfoLoadSucess = false;
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    if (fragment.isAdded()) {
                        fragment.processRecyclerView();
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
//                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    break;
            }

        }

    };

    public NewsPageRecyclerViewFragment() {
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

    private void startFragment() {

        initController();
        processArgument();
        processView();
        getNewsInfo();

    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {

            mNewsId = bundle.getInt(KEY_NEWS_ID, -1);
            mNewsCategory = bundle.getString(KEY_NEWS_CATEGORY);
            mBigCategory = bundle.getString(KEY_NEWS_BIG_CATEGORY);

            if (Utility.DEBUG) Log.e(TAG, "VideoNewsPageRecyclerViewFragment");
            if (Utility.DEBUG) Log.e(TAG, "mNewsId: " + mNewsId);
            if (Utility.DEBUG) Log.e(TAG, "mNewsCategory: " + mNewsCategory);
            if (Utility.DEBUG) Log.e(TAG, "mBigCategory: " + mBigCategory);

        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
        mBitmapController.clearCache();
        mSharedPref = new SharedPreferencesMethods(getActivity());
        mEcoDefaultImageList = new String[]{
                "http://s.nownews.com/w/images/mobile/defaultimg-01.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-02.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-03.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-04.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-05.jpg"};
        mHandler = new ApiHandler(this);
    }

    public void getNewsInfo() {
        if (mApiController != null) {
            mApiController.getNewsInfo(mHandler, mNewsId);
        }
    }

    public void getRelationsNewsInfo() {
        if (mApiController != null) {
            mApiController.getRelationsNewsInfo(mHandler, mNewsId);
        }
    }

    public void processView() {

        View view = getView();

        //New View
        vError = (TextView)view.findViewById(R.id.error_message);
        vContentRecyclerView = (RecyclerView)view.findViewById(R.id.content_recyclerview);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getNewsInfo();
            }
        });
        vVideoFrameLayout = (FrameLayout) view.findViewById(R.id.youtube_content_view);
    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processNews() {

        setHitInfo();

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }
        mImageUrlList = new ArrayList<String>();

        String mCategoryName = ((NewsPage) getActivity()).getCurrentNewsCategory();
        if (mNewsInfo.getMainPhoto() != null
                && mNewsInfo.getMainPhoto().getUrl() != null
                && !mNewsInfo.getMainPhoto().getUrl().trim().isEmpty()) {
            String bigImgUrl = mNewsInfo.getMainPhoto().getUrl();
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mImageUrlList.add(bigImgUrl);
        } else if (mCategoryName.contains(getString(R.string.eco))) {
            int newsId = mNewsInfo.getSn();
            int digit = newsId % 10;
            int imagePosition = digit % 5;
            String bigImgUrl = mEcoDefaultImageList[imagePosition];
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mImageUrlList.add(bigImgUrl);
        }

        //本文
        if (mNewsInfo.getContent() != null) {
            if(mContentList==null){
                mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
            }else{
                mContentList.clear();
            }
            String body = mNewsInfo.getContent();
            processBody(body);
        }

        //圖集類型
        if(mNewsInfo.getPhotos()!=null
                && mNewsInfo.getPhotos().size()>0){
            processPhotos(mNewsInfo.getPhotos());
        }

        //影音類型
        if(mNewsInfo.getMainVideo()!=null
                && mNewsInfo.getMainVideo().getUrl()!=null
                && !mNewsInfo.getMainVideo().getUrl().trim().isEmpty()){
            processVideo(mNewsInfo.getMainVideo().getUrl());
        }

        //自由欄位
        if (mNewsInfo.getFreeContent() != null) {
            if(mContentList==null){
                mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
            }
            processBody(mNewsInfo.getFreeContent());
        }

        getRelationsNewsInfo();

        //TODO: [v4] getVideos() Not Ready
//            if(mNewsInfo.getVideos()!=null){
//                processVideo(mNewsInfo.getVideos());
//            }

        //Not use in v4
//        else if (mNewsInfo.mobileBody != null) {
//            processBody(mNewsInfo.mobileBody);
//            if (mNewsInfo.freeBody != null) {
//                processFreeBody(mNewsInfo.freeBody);
//            }
//            if(mNewsInfo.videos!=null && mNewsInfo.videos.size()>0){
//                processVideos(mNewsInfo.videos);
//            }
//            processRecyclerView();
//        }

    }

    private void processPhotos(List<NewsInfoJson.Photos> photos){

        if(mContentList==null){
            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
        }

        for(NewsInfoJson.Photos photoInfo : photos){

            if(photoInfo==null){
                continue;
            }

            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

            String imgUrl = photoInfo.getUrl();
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                imgUrl = Utility.getSrcFromImgapi(imgUrl);
                if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);
                map.put(KEY_CONTEXT_IMAGE, imgUrl);
                if (photoInfo.getDesc() != null && !photoInfo.getDesc().trim().isEmpty() && !photoInfo.getDesc().trim().equals("▲")) {
                    map.put(KEY_CONTEXT_IMAGE_TEXT, photoInfo.getDesc());
                }
                mContentList.add(map);
                mImageUrlList.add(imgUrl);
            }

        }

    }

    private void processVideo(String aMainVideoUrl){

        if(mContentList==null){
            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
        }

        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put(KEY_CONTEXT_IFRAME_YOUTUBE, aMainVideoUrl);
        mContentList.add(map);

    }

//    private void processVideos(List<NewsInfoJson.VideoInfo> videos){
//
//        if(mVideoContentList==null){
//            mVideoContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
//        }
//
//        for(NewsInfoJson.VideoInfo videoInfo : videos){
//            if(videoInfo!=null && videoInfo.type.equals("youtube")){
//                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
//                map.put(KEY_CONTEXT_IFRAME_YOUTUBE, videoInfo.youtubeId);
//                mVideoContentList.add(map);
//            }
//        }
//
//    }

    private void processRecyclerView(){

        vLoadingLayout.setVisibility(View.GONE);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        vContentRecyclerView.setLayoutManager(mLinearLayoutManager);
        NewsPageRecyclerViewAdapter mAdapter = new NewsPageRecyclerViewAdapter(getActivity(), mContentList, mNewsInfo, mImageUrlList, mVideoListener, mRelationsNewsList);
        vContentRecyclerView.setAdapter(mAdapter);

    }

    private YouTubePlayer mYoutubePlayer;
    private YouTubePlayerSupportFragment mYouTubePlayerSupportFragment;
    private NewsPageRecyclerViewAdapter.OnVideoPlayButtonClick mVideoListener = new NewsPageRecyclerViewAdapter.OnVideoPlayButtonClick() {
        @Override
        public void onVideoPlayButtonClick(final String aYoutubeId) {

            vVideoFrameLayout.setVisibility(View.VISIBLE);

            if (Utility.DEBUG)Log.v(TAG, "onVideoPlayButtonClick() aYoutubeId: " + aYoutubeId);

            mYouTubePlayerSupportFragment = YouTubePlayerSupportFragment.newInstance();
            FragmentTransaction transcation = getChildFragmentManager().beginTransaction();
            transcation.add(R.id.youtube_content_view, mYouTubePlayerSupportFragment).addToBackStack(null).commit();

            mYouTubePlayerSupportFragment.initialize(UserDataInfo.YOUTUBE_DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                    if (!wasRestored) {
                        mYoutubePlayer = youTubePlayer;
                        mYoutubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                            @Override
                            public void onPlaying() {
                                if (Utility.DEBUG)Log.d(TAG, "===YoutubePlayer onPlaying===");
                            }

                            @Override
                            public void onPaused() {
                                if (Utility.DEBUG)Log.d(TAG, "===YoutubePlayer onPaused===");
                            }

                            @Override
                            public void onStopped() {
                                if (Utility.DEBUG)Log.d(TAG, "===YoutubePlayer onStopped===");
                            }

                            @Override
                            public void onBuffering(boolean b) {
                                if (Utility.DEBUG)Log.d(TAG, "===YoutubePlayer onBuffering b: " + b + " ===");
                            }

                            @Override
                            public void onSeekTo(int i) {
                                if (Utility.DEBUG)Log.d(TAG, "===YoutubePlayer onSeekTo i: " + i + " ===");
                            }
                        });
                        mYoutubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                            @Override
                            public void onLoading() {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onLoading===");
                            }

                            @Override
                            public void onLoaded(String s) {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onLoaded s: " + s + " ===");
                            }

                            @Override
                            public void onAdStarted() {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onAdStarted===");
                            }

                            @Override
                            public void onVideoStarted() {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onVideoStarted===");
                            }

                            @Override
                            public void onVideoEnded() {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onVideoEnded===");
                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason errorReason) {
                                if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer onError errorReason: " + errorReason.name() + " ===");
                                if(errorReason.equals(YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY)){
                                    if (Utility.DEBUG)Log.i(TAG, "===YoutubePlayer is visible or not???: " + (mYouTubePlayerSupportFragment.isVisible()? "true":"false") + " ===");
                                }
                            }
                        });
                        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                        youTubePlayer.loadVideo(aYoutubeId);
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                    String errorMessage = error.toString();
                    if (Utility.DEBUG)Log.d("errorMessage:", errorMessage);
                }
            });
        }
    };

//    private void processBody(List<MobileBody> jbody) {
//        if (Utility.DEBUG) Log.e(TAG, "processBody(List<JsonBody> jbody)");
//
//        if(mContentList==null){
//            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
//        }else{
//            mContentList.clear();
//        }
//
//        for (int i = 0; i < jbody.size(); i++) {
//            String type = jbody.get(i).tag;
//            String content = jbody.get(i).content;
//            if (Utility.DEBUG) Log.w(TAG, "type: " + type);
//            if (type.equals("image")) {
//
//                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
//
//                String img = jbody.get(i).src;
//                if (img != null && !img.trim().isEmpty()) {
//                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + img);
//                    map.put(KEY_CONTEXT_IMAGE, img);
//                    mImageUrlList.add(img);
//                }
//
//                if (content != null && !content.trim().equals("") && !content.trim().equals("▲")) {
//                    map.put(KEY_CONTEXT_IMAGE_TEXT, content);
//                }
//
//                mContentList.add(map);
//
//            } else if (type.equals("p")) {
//
//                if (content != null && !content.trim().equals("")) {
//
//                    if (content.contains("延伸閱讀")) {
//                        break;
//                    }
//
//                    if(content.contains("&nbsp;")){
//                        content = content.replace("&nbsp;", "");
//                        if(content.trim().isEmpty()){
//                            continue;
//                        }
//                    }
//
//                    if(content.contains("&lt;")){
//                        content = content.replace("&lt;", "<");
//                        if(content.trim().isEmpty()){
//                            continue;
//                        }
//                    }
//
//                    if(content.contains("&gt;")){
//                        content = content.replace("&gt;", ">");
//                        if(content.trim().isEmpty()){
//                            continue;
//                        }
//                    }
//
//                    ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
//                    map.put(KEY_CONTEXT_TEXT, content);
//                    mContentList.add(map);
//
//                }
//            }
//        }
//    }

    private void processBody(String body) {
        if (Utility.DEBUG) Log.e(TAG, "body: " + body);

        String citeContent = null;
        String textLinkHtml = null;
        String textLink = null;
        String pHtml = null;
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

            //init
            pHtml = null;
            textLink = null;
            textLinkHtml = null;
            citeContent = null;

            Element pContent = paragraph.get(i);
            String p = pContent.text();
            if(p.contains("(影片擷取自YouTube.com，若遭移除請見諒)")){
                continue;
            }
            p = p.replace("$$$", "\n");
            p = p.replace("&&&", "\n");
            if (Utility.DEBUG) Log.w(TAG, "p: " + p);

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

            //超連結
            Elements a = pContent.select("a[href]");
            if(a !=null && a.size() > 0){
                pHtml = pContent.toString();
                if(Utility.DEBUG)Log.v(TAG, "pHtml: " + pHtml);
                for (int j = 0; j < a.size(); j++) {
                    Element href = a.get(j);
                    textLink = href.attr("href");
                    textLinkHtml = href.toString();
                }
            }

            //圖片
            Elements img = pContent.select("img[src]");
            if (img != null && img.size() > 0) {

                for (int j = 0; j < img.size(); j++) {

                    Element imgElement = img.get(j);
                    String imgUrl = imgElement.attr("src");
                    imgUrl = Utility.getSrcFromImgapi(imgUrl);
                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                        map.put(KEY_CONTEXT_IMAGE, imgUrl);
                        if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);
                        mImageUrlList.add(imgUrl);
                        mContentList.add(map);
                    }

                }

            }

            //影音 目前只接受youtube
            Elements iframe = pContent.select("iframe[src]");
            if (iframe != null && iframe.size() > 0) {


                for (int j = 0; j < iframe.size(); j++) {
                    Element iframeElement = iframe.get(j);
                    String iframeUrl = iframeElement.attr("src");
                    if (iframeUrl != null && !iframeUrl.trim().isEmpty() && iframeUrl.contains("youtube")) {
                        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                        if (Utility.DEBUG) Log.w(TAG, "iframeUrl in body: " + iframeUrl);
                        map.put(KEY_CONTEXT_IFRAME_YOUTUBE, iframeUrl);
                        mContentList.add(map);
                    }
                }

            }

            //延伸閱讀 需略過
            if (p != null && !p.trim().isEmpty()) {
                if (p.contains("延伸閱讀")) {
                    break;
                }

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(KEY_CONTEXT_TEXT, p);

                if(textLinkHtml!=null && !textLinkHtml.trim().isEmpty()){
                    map.put(KEY_CONTEXT_TEXT_LINK_HTML, textLinkHtml);
                }
                if(textLink!=null && !textLink.trim().isEmpty()){
                    map.put(KEY_CONTEXT_TEXT_LINK, textLink);
                }
                if(pHtml!=null && !pHtml.trim().isEmpty()){
                    map.put(KEY_CONTEXT_TEXT_HTML, pHtml);
                }

                mContentList.add(map);

            }

            if(citeContent != null && !citeContent.trim().isEmpty()){

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(KEY_CONTEXT_TEXT, citeContent);
                mContentList.add(map);

            }

        }
    }

    @Override
    public void onDestroy() {
        if(Utility.DEBUG)Log.i(TAG, TAG + "%%%onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }
        if(vContentRecyclerView!=null && vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).destory();
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).unRegistContext(getActivity());
            vContentRecyclerView.getRecycledViewPool().clear();
            vContentRecyclerView.setLayoutManager(null);
            vContentRecyclerView.setAdapter(null);
            vContentRecyclerView = null;
        }

        if(mSharedPref!=null){
            mSharedPref.unRegistContext(getActivity());
        }

        if(mHandler!=null){
            mHandler.removeCallbacks(null);
            mHandler = null;
        }
        super.onDestroyView();
    }

    public void changeTextSize(String aTextSize) {
        if(Utility.DEBUG)Log.v(TAG, "changeTextSize aTextSize: " + aTextSize);
        if(Utility.DEBUG)Log.v(TAG, "vContentRecyclerView is null or not?? : " + (vContentRecyclerView==null? "true":"false"));
        if(Utility.DEBUG && vContentRecyclerView!=null) {
            Log.v(TAG, "vContentRecyclerView.getAdapter() is null or not?? : " + (vContentRecyclerView.getAdapter() == null ? "true" : "false"));
        }
        if(Utility.DEBUG)Log.v(TAG, "mSharedPref is null or not?? : " + (mSharedPref==null? "true":"false"));
        if(vContentRecyclerView!=null && vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).changeTextSize(aTextSize);
        }
        if(mSharedPref!=null){
            mSharedPref.saveNewsContentTextSize(aTextSize);
        }
    }

    public void setHitInfo() {
        String url = null;
        String title = null;
        String label = null;
        if(mNewsInfo!=null && mNewsInfo.getParseUrl()!=null && !mNewsInfo.getParseUrl().trim().isEmpty()){
            url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsInfo.getParseUrl();
        }
        if(mNewsInfo!=null && mNewsInfo.getShortTitle()!=null && !mNewsInfo.getShortTitle().isEmpty()){
            title = mNewsInfo.getShortTitle();
        }
        if(url!=null && title!=null){
            label = title + " " + url;
        }
        if (Utility.DEBUG)Log.d(TAG, "label: " + label);
        GoogleAnalyticsFunction.sendHitInfo(getActivity(), mBigCategory, mNewsCategory, label);
    }

    @Override
    public void onPause() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onPause()");
        isOnPause = true;
        if(vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).pauseDFP();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onResume()");
        if(vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).resumeDFP();
        }
//        if (isOnPause) {
//            isOnPause = false;
//            getNewsInfo();
//        }
        super.onResume();
    }

    public void reload() {
        startFragment();
    }

    public NewsInfoJson getCurrentNewsInfo(){
        if(mNewsInfo!=null){
            return mNewsInfo;
        }
        return null;
    }

    public boolean isVideoFrameLayoutVisibile(){
        return vVideoFrameLayout.getVisibility()==View.VISIBLE;
    }

    public void closeVideoFrame() {

        if(vVideoFrameLayout!=null && vVideoFrameLayout.getVisibility()==View.VISIBLE){
            removeYoutubeFragment();
            vVideoFrameLayout.setVisibility(View.GONE);
        }

    }

    public void removeYoutubeFragment(){
        if(mYoutubePlayer!=null){
            mYoutubePlayer.release();
        }
    }

}

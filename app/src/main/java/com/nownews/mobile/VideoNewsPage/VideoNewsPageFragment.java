package com.nownews.mobile.VideoNewsPage;

import android.content.Intent;
import android.content.res.Configuration;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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
import com.nownews.mobile.Widget.CustomImageTopcrop;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoNewsPageFragment extends Fragment{

    public final static String KEY_NEWS_URL = "newsUrl";
    public final static String KEY_NEWS_ID = "newsId";
    public final static String KEY_NEWS_POSITION = "newsPosition";
    private final String TAG = getClass().getSimpleName();
    public boolean isOnPause = false;
    private RelativeLayout vImageLayout;
    private RelativeLayout vLoadingLayout;
    private ImageView vBigImage;
    private LinearLayout vTopDFPLayout;
    private LinearLayout vBottomDFPLayout;
    private FrameLayout vIleopardLayout;
    private TextView vTitle;
    private TextView vCategory;
    private TextView vCategory2;
    private TextView vCategory3;
    private TextView vCreateAt;
    private LinearLayout vBody;
    private LinearLayout vHeadlineNewsBody;
    private TextView vHeadlineNewsTitle;
    private RelativeLayout vNextNewsGroup;
    private TextView vNextNewsTitle;
    private RelativeLayout vPrevNewsGroup;
    private TextView vPrevNewsTitle;
    private ProgressBar vImageProgressBar;
    private SwipeRefreshLayout vRefreshLayout;
    private FrameLayout vYoutubeContentView;
    private ScrollView vBodyScrollView;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private String mNewsUrl;
    private int mNewsId;
    private VideosInfoJson mVideoNewsInfo;
    private ArrayList<TextView> mTextViewList;
    private ArrayList<String> mImageUrlList;
    private HashMap<Integer, SoftReference<ImageView>> mImageViewList;
    private SharedPreferencesMethods mSharedPref;
    private int mContentTextSize;
    private int mTitleTextSize;
    private int mRefTitleTextSize;
    private ReSizeLayoutParams mReSizeParams;
    private boolean isRefereshing = false;
    private String mShareImgUrl;
    private ImageLoadingListener mBigImgLoadingListener = new ImageLoadingListener() {

        @Override
        public void onProgressUpdate(String aImageUrl, int aProgress, int max) {
//			if(Utility.DEBUG)Log.v(TAG, "aProgress: " + aProgress);
            if (vLoadingLayout.getVisibility() == View.GONE) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        vLoadingLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
            vImageProgressBar.setMax(max);
            vImageProgressBar.setProgress(aProgress);
        }

        @Override
        public void onLoadingStart(String aImageUrl, View aView) {
        }

        @Override
        public void onLoadingFailed(String aImageUrl, View aView, Exception aException) {
        }

        @Override
        public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {
//			if(Utility.DEBUG)Log.e(TAG, "onLoadingComplete!!!");
            vLoadingLayout.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingCancelled() {
        }
    };
    private int mYoutubeLayoutId;
    private ArrayList<PublisherAdView> mDFPADList;
    private ArrayList<PublisherAdRequest> mDFPADRequestList;
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
            }

        }

    };
    private OnClickListener mNextNewsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ((VideoNewsPage) getActivity()).gotoNextNews();

        }
    };
    private OnClickListener mPrevNewsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ((VideoNewsPage) getActivity()).gotoPrevNews();

        }
    };

    public VideoNewsPageFragment() {
        //DO NOTHING...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_news_page, container, false);
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
        getTextSize();
        processView();
        processListener();
        getVideoNewsInfo();
        processTopDFP();

    }

    private void processTopDFP(){
        addDFPAD(vTopDFPLayout, getString(R.string.dfp_news_info_top));
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

    private void getTextSize() {
        String textSize = getString(R.string.small);
        if(mSharedPref!=null){
            textSize = mSharedPref.getNewsContentTextSize();
        }
        if (textSize.equals(getString(R.string.small))) {
            mContentTextSize = Integer.valueOf(getString(R.string.small_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_small_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_small_text_size));
        } else if (textSize.equals(getString(R.string.mid))) {
            mContentTextSize = Integer.valueOf(getString(R.string.mid_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_mid_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_mid_text_size));
        } else if (textSize.equals(getString(R.string.max))) {
            mContentTextSize = Integer.valueOf(getString(R.string.max_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_max_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_max_text_size));
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
        mReSizeParams = new ReSizeLayoutParams(getActivity());
    }

    private void getVideoNewsInfo() {
        if (mApiController != null) {
            mApiController.getVideosInfo(mHandler, mNewsId);
        }
    }

    public void processView() {

        View view = getView();

        vBodyScrollView = (ScrollView) view.findViewById(R.id.body_scrollview);
        vBigImage = (ImageView) view.findViewById(R.id.big_img);
        vTopDFPLayout = (LinearLayout) view.findViewById(R.id.top_dfp);
        vBottomDFPLayout = (LinearLayout) view.findViewById(R.id.bottom_dfp);
        vIleopardLayout = (FrameLayout) view.findViewById(R.id.ileopard_ad);
        vTitle = (TextView) view.findViewById(R.id.title);
        vCategory = (TextView) view.findViewById(R.id.category);
        vCategory2 = (TextView) view.findViewById(R.id.category2);
        vCategory3 = (TextView) view.findViewById(R.id.category3);
        vCreateAt = (TextView) view.findViewById(R.id.createat);
        vBody = (LinearLayout) view.findViewById(R.id.body);
        vHeadlineNewsBody = (LinearLayout) view.findViewById(R.id.headline_news_body);
        vHeadlineNewsTitle = (TextView) view.findViewById(R.id.headline_news_title);
        vNextNewsGroup = (RelativeLayout) view.findViewById(R.id.next_news_group);
        vNextNewsTitle = (TextView) view.findViewById(R.id.next_news_title);
        vPrevNewsGroup = (RelativeLayout) view.findViewById(R.id.prev_news_group);
        vPrevNewsTitle = (TextView) view.findViewById(R.id.prev_news_title);
        vImageProgressBar = (ProgressBar) view.findViewById(R.id.image_progressbar);
        vImageLayout = (RelativeLayout) view.findViewById(R.id.image_layout);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vYoutubeContentView = (FrameLayout) view.findViewById(R.id.youtube_content_view);

        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getTextSize();
                getVideoNewsInfo();
            }
        });
    }

    private void processListener() {
        vNextNewsGroup.setOnClickListener(mNextNewsClickListener);
        vPrevNewsGroup.setOnClickListener(mPrevNewsClickListener);
    }

    private void processNews() {

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }

        mImageUrlList = new ArrayList<String>();

        String mCategoryName = ((VideoNewsPage) getActivity()).getCurrentNewsCategory();
        if (mVideoNewsInfo.image != null
                && mVideoNewsInfo.image != null
                && !mVideoNewsInfo.image.trim().isEmpty()) {
            String bigImgUrl = mVideoNewsInfo.image;
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mBitmapController.loadImageWithOriginalSize(bigImgUrl, vBigImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE, 0, 0, mBigImgLoadingListener);
            mImageUrlList.add(bigImgUrl);
            setImageClickListener(vBigImage, bigImgUrl);

            //For Share
            mShareImgUrl = mVideoNewsInfo.image;
            if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
            mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, vBigImage, BitmapController.IMAGE_SRC, 0, 0, null);
        } else {
            vImageLayout.setVisibility(View.GONE);
        }

        if (mVideoNewsInfo.title != null) {
            String title = mVideoNewsInfo.title;
            if(title!=null && title.contains("▲")){
                title = title.replaceAll("▲", "");
            }
            if(title!=null && title.contains("▼")){
                title = title.replaceAll("▼", "");
            }
            vTitle.setTextSize(mTitleTextSize);
            vTitle.setText(title);
        }

        //有可能有兩個或兩個以上
        if (mVideoNewsInfo.categories != null) {
            for(int i = 0; i<mVideoNewsInfo.categories.size(); i++){
                String categoryName = mVideoNewsInfo.categories.get(i).name;
                switch(i){
                    case 0:
                        vCategory.setText(categoryName);
                        vCategory.setVisibility(View.VISIBLE);
                        vCategory2.setVisibility(View.GONE);
                        vCategory3.setVisibility(View.GONE);
                        break;
                    case 1:
                        vCategory2.setText(categoryName);
                        vCategory.setVisibility(View.VISIBLE);
                        vCategory2.setVisibility(View.VISIBLE);
                        vCategory3.setVisibility(View.GONE);
                        break;
                    case 2:
                        vCategory3.setText(categoryName);
                        vCategory.setVisibility(View.VISIBLE);
                        vCategory2.setVisibility(View.VISIBLE);
                        vCategory3.setVisibility(View.VISIBLE);
                        break;
                }
            }
        } else {
            vCategory.setVisibility(View.GONE);
            vCategory2.setVisibility(View.GONE);
            vCategory3.setVisibility(View.GONE);
        }

        if (mVideoNewsInfo.createdAt != null
                && !mVideoNewsInfo.createdAt.trim().isEmpty()) {
            String createAt = Utility.processDate(mVideoNewsInfo.createdAt);
            vCreateAt.setVisibility(View.VISIBLE);
            vCreateAt.setText(createAt);
        } else {
            vCreateAt.setVisibility(View.GONE);
        }

        if (mVideoNewsInfo.body != null
                && mVideoNewsInfo.body.value!=null
                && !mVideoNewsInfo.body.value.trim().isEmpty()) {
            String body = mVideoNewsInfo.body.value;
            processBody(body);
        }

//        processVideo();

        processBottomBigDFP();
        processBottomBigIleopard();
        processPreviousNextNews();
        processHeadlineNews();
        if(mOnPageLoadFinishedListener!=null){
            mOnPageLoadFinishedListener.OnPageLoadFinished(VideoNewsPageFragment.this, mCurrentNewsPosition);
        }

    }

    private void processBottomBigDFP() {

        //add DFP AD
        if (Utility.DEBUG) Log.e(TAG, "Add DFP AD1!!");
        addDFPAD(vBottomDFPLayout, getString(R.string.dfp_news_info_bottom));

    }

    private ArrayList<CMNativeBannerView> mIleopardADList;
    private void processBottomBigIleopard() {

        //add 雪豹 AD
        if (Utility.DEBUG) Log.e(TAG, "Add 雪豹 AD1!!");
        final CMNativeBannerView mIleopardAd = new CMNativeBannerView(getActivity());
        mIleopardAd.setAdSize(CMBannerAdSize.BANNER_300_250);
        mIleopardAd.setPosid(getString(R.string.ileopard_300_250_banner));
        mIleopardAd.setAdListener(new CMBannerAdListener() {
            @Override
            public void onAdLoaded(CMAdView cmAdView) {
                vIleopardLayout.removeAllViews();
                vIleopardLayout.addView(mIleopardAd);
            }

            @Override
            public void adFailedToLoad(CMAdView cmAdView, int errorCode) {
                if(Utility.DEBUG)Log.e(TAG, "adFailedToLoad!! errorCode: " + errorCode);
            }

            @Override
            public void onAdClicked(CMAdView cmAdView) {

            }
        });
        mIleopardAd.loadAd();
        if(mIleopardADList==null){
            mIleopardADList = new ArrayList<CMNativeBannerView>();
        }
        mIleopardADList.add(mIleopardAd);

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
        transcation.add(R.id.youtube_content_view, youTubePlayerSupportFragment).commit();
        setInitializedListener(youTubePlayerSupportFragment, mCurrentYoutubeId);

    }

    private void setInitializedListener(YouTubePlayerSupportFragment aYouTubePlayerSupportFragment, final String aYoutubeId) {
        aYouTubePlayerSupportFragment.initialize(UserDataInfo.YOUTUBE_DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    mYoutubePlayer = youTubePlayer;
                    if(mOnPageLoadFinishedListener!=null){
                        mOnPageLoadFinishedListener.OnYoutubeInitializationSuccess(VideoNewsPageFragment.this, mCurrentNewsPosition, youTubePlayer, aYoutubeId);
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

        vBody.removeAllViews();

        if (mTextViewList != null) {
            mTextViewList.clear();
            mTextViewList = null;
        }

        mTextViewList = new ArrayList<TextView>();

        String citeContent = null;
        body = body.replace("<br />", "$$$");
        body = body.replace("<br>", "&&&");
        body = body.replace("<strong>", "(((");
        body = body.replace("</strong>", ")))");
        body = body.replace("&nbsp;", "");
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

            if (p != null && !p.trim().equals("")) {
                if (p.contains("延伸閱讀")) {
                    break;
                }
                TextView textView = new TextView(getActivity());
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, mReSizeParams.getPixelAfterScale(20), 0, mReSizeParams.getPixelAfterScale(20));
                textView.setLayoutParams(layoutParams);
                textView.setLineSpacing(20.0f, 1.0f);
                textView.setTextIsSelectable(true);
                textView.setTextSize(mContentTextSize);
                textView.setTextColor(getResources().getColor(android.R.color.black));
//				textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
//				if(p.contains("http")){
//					textView.setAutoLinkMask(Linkify.ALL);
//				}
                if (p.contains("(((") && p.contains(")))")) {
                    SpannableString content = getStrongText(p.trim());
                    textView.setText(content);
                } else {
                    textView.setText(p.trim());
                }
//				Elements strong = pContent.select("strong");
//				if(strong!=null && strong.size()>0){
//					textView.setTypeface(null, Typeface.BOLD);
//				}
                vBody.addView(textView);
                mTextViewList.add(textView);
            }

            if (img != null && img.size() > 0) {
                for (int j = 0; j < img.size(); j++) {
                    Element imgElement = img.get(j);
                    String imgUrl = imgElement.attr("src");
                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);

                    ImageView imgView = new ImageView(getActivity());
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    params.setMargins(0, 30, 0, 10);
                    params.gravity = Gravity.CENTER_HORIZONTAL;
                    imgView.setLayoutParams(params);
                    imgView.setScaleType(ScaleType.CENTER_CROP);
//					imgView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                    mBitmapController.loadImageWithOriginalSize(imgUrl, imgView, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE, 0, 0, null);
                    vBody.addView(imgView);
                    mImageUrlList.add(imgUrl);
                    setImageClickListener(imgView, imgUrl);

                }
            }

            if (citeContent != null && !citeContent.trim().equals("") && !citeContent.trim().equals("▲")) {
                TextView citeTextView = new TextView(getActivity());
                LayoutParams citeLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                citeTextView.setLayoutParams(citeLayoutParams);
                citeTextView.setTextSize(mContentTextSize);
                citeTextView.setTextColor(getResources().getColor(android.R.color.black));
//				citeTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                citeTextView.setTypeface(null, Typeface.ITALIC);
                citeTextView.setText(citeContent.trim());
                vBody.addView(citeTextView);
                mTextViewList.add(citeTextView);
                citeContent = null;
            }

        }

    }

    public void processPreviousNextNews() {
        if (((VideoNewsPage) getActivity()).getCurrentPage() == 0) {
            vPrevNewsGroup.setVisibility(View.GONE);
        } else {
            vPrevNewsGroup.setVisibility(View.VISIBLE);
            String title = ((VideoNewsPage) getActivity()).getPrevPageTitle();
            if(title!=null && title.contains("▲")){
                title = title.replaceAll("▲", "");
            }
            if(title!=null && title.contains("▼")){
                title = title.replaceAll("▼", "");
            }
            vPrevNewsTitle.setText(title);
        }
        if (((VideoNewsPage) getActivity()).isLastPage()) {
            vNextNewsGroup.setVisibility(View.GONE);
        } else {
            vNextNewsGroup.setVisibility(View.VISIBLE);
            String title = ((VideoNewsPage) getActivity()).getNextPageTitle();
            if(title!=null && title.contains("▲")){
                title = title.replaceAll("▲", "");
            }
            if(title!=null && title.contains("▼")){
                title = title.replaceAll("▼", "");
            }
            vNextNewsTitle.setText(title);
        }
    }

    private SpannableString getStrongText(String p) {
        int start = p.indexOf("(((");
        int end = p.indexOf(")))");
        p = p.replace("(((", "");
        p = p.replace(")))", "");
        SpannableString newContent = new SpannableString(p);
        end = end - 3;
        if (end > p.length()) {
            end = p.length() - 1;
        }
        newContent.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return newContent;
    }

    private void processTwoBigDFP() {

        //DFP AD Layout
        LinearLayout dfpADLayout = new LinearLayout(getActivity());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 40, 0, 0);
        dfpADLayout.setOrientation(LinearLayout.VERTICAL);
        dfpADLayout.setLayoutParams(layoutParams);
        dfpADLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        vBody.addView(dfpADLayout);

        //add DFP AD
        if (Utility.DEBUG) Log.e(TAG, "Add DFP AD1!!");
        addDFPAD(dfpADLayout, "/5799246/nownews_app_300x250_ALL_android");

    }

    private void processHeadlineNews() {

        List<NewsListJson.NewsContent> headline = UserDataInfo.getHeadlineContent();
        if (headline == null || headline.size() == 0) {
            vHeadlineNewsBody.setVisibility(View.GONE);
            vHeadlineNewsTitle.setVisibility(View.GONE);
            return;
        }

        if (vHeadlineNewsBody != null) {
            vHeadlineNewsBody.removeAllViews();
        }

        int headlineSize = headline.size();
        if (headlineSize > 3) {
            headlineSize = 3;
        }
        for (int i = 0; i < headlineSize; i++) {

            NewsListJson.NewsContent newsInfo = headline.get(i);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View convertView = inflater.inflate(R.layout.widget_reference_news_item, null);

            TextView vCategory = (TextView) convertView.findViewById(R.id.category);
            TextView vTitle = (TextView) convertView.findViewById(R.id.title);
            CustomImageTopcrop vNewsImage = (CustomImageTopcrop) convertView.findViewById(R.id.news_image);

            String imgUrl = newsInfo.image.originImage;
            mBitmapController.loadImageWithOriginalSize(imgUrl, vNewsImage, BitmapController.IMAGE_SRC, 0, 0, null);
            String title = newsInfo.field_short_title.value;
            vTitle.setText(title);
            vTitle.setTextSize(mRefTitleTextSize);
            String category;
            if (newsInfo.category == null
                    || newsInfo.category.name == null) {
                category = getString(R.string.non_category);
            } else {
                category = newsInfo.category.name;
            }
            vCategory.setText(category);
            Utility.setCategoryTextColor(category, vCategory, Utility.ColorType.News);
            setHeadlineItemClickListener(convertView, i);
            vHeadlineNewsBody.addView(convertView);

        }

    }

    private void setHeadlineItemClickListener(View aConvertView, final int position) {
        aConvertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to singal news
                List<NewsListJson.NewsContent> headline = UserDataInfo.getHeadlineContent();
                ((VideoNewsPage) getActivity()).gotoHeadlineNewsPage(position, headline);

            }
        });
    }

    private void addDFPAD(LinearLayout aTargetLayout, String aUnitId) {
        aTargetLayout.removeAllViews();
        PublisherAdView dfpAdView = new PublisherAdView(getActivity());
        dfpAdView.setAdUnitId(aUnitId);
        dfpAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);
        dfpAdView.setAdListener(mDfpAdListener);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
//        builder.addTestDevice("3F296C0E07DBC64F3ED95E7FA105ED8C");
//        builder.addTestDevice("47D3A2E05A8397836B567EB9AA537A0E");
        PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        dfpAdView.loadAd(request);
        aTargetLayout.addView(dfpAdView);
        if (mDFPADRequestList == null) {
            mDFPADRequestList = new ArrayList<PublisherAdRequest>();
        }
        mDFPADRequestList.add(request);
        if (mDFPADList == null) {
            mDFPADList = new ArrayList<PublisherAdView>();
        }
        mDFPADList.add(dfpAdView);
    }

    private void setImageClickListener(ImageView aImgView, final String aImgUrl) {
        aImgView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //show image page
                if (Utility.DEBUG) Log.e(TAG, "image click!!!");
                if (Utility.DEBUG) Log.v(TAG, "aImgUrl: " + aImgUrl);

                GoogleAnalyticsFunction.sendHitInfo(getActivity(), "新聞內頁", "點擊新聞圖片", "");
                GoogleAnalyticsFunction.sendHitInfo(getActivity(), "新聞圖片", aImgUrl, "");

                int position = 0;
                for (int i = 0; i < mImageUrlList.size(); i++) {
                    String urlInList = mImageUrlList.get(i);
                    if (aImgUrl.equals(urlInList)) {
                        position = i;
                        break;
                    }
                }

                //gotoNewsAlbumPage
                goToNewsAlbumPage(position);

            }
        });
    }

    private void goToNewsAlbumPage(int position) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), FavoriteAlbumPage.class);
        intent.putExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_POSITION, position);
        intent.putStringArrayListExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_LIST, mImageUrlList);
        intent.putExtra(FavoriteAlbumPage.KEY_TYPE, FavoriteAlbumPage.TYPE_NEWS_IMAGES);
        intent.putExtra(FavoriteAlbumPage.KEY_IMAGE_TITLE, mVideoNewsInfo.title);
        intent.putExtra(FavoriteAlbumPage.KEY_NEWS_URL, WebAPIUrl.NOWNEWS_MOBIEL_WEB_VIDEO_DOMAIN + mVideoNewsInfo._id);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.removeAllViews();
                dfp.destroy();
                dfp = null;
            }
            mDFPADList.clear();
            mDFPADList = null;
        }

        if (mDFPADRequestList != null && mDFPADRequestList.size() > 0) {
            for (PublisherAdRequest request : mDFPADRequestList) {
                request = null;
            }
            mDFPADRequestList.clear();
            mDFPADRequestList = null;
        }

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
            for (TextView textView : mTextViewList) {
                textView = null;
            }
            mTextViewList.clear();
            mTextViewList = null;
        }

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }

        if (vBody != null) {
            vBody.removeAllViews();
            vBody = null;
        }

        if (vImageLayout != null) {
            vImageLayout.removeAllViews();
            vImageLayout = null;
        }

        if(mSharedPref!=null){
            mSharedPref.unRegistContext(getActivity());
        }
        super.onDestroyView();
    }

    public void changeTextSize(String aTextSize) {
        int contentTextSize = Integer.valueOf(getString(R.string.small_text_size));
        int titleTextSize = Integer.valueOf(getString(R.string.title_small_text_size));
        int refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_small_text_size));
        if (aTextSize != null) {
            if (aTextSize.equals(getString(R.string.max))) {
                contentTextSize = Integer.valueOf(getString(R.string.max_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_max_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_max_text_size));
            } else if (aTextSize.equals(getString(R.string.mid))) {
                contentTextSize = Integer.valueOf(getString(R.string.mid_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_mid_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_mid_text_size));
            } else if (aTextSize.equals(getString(R.string.small))) {
                contentTextSize = Integer.valueOf(getString(R.string.small_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_small_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_small_text_size));
            }
        }
        if (mTextViewList != null) {
            for (TextView textView : mTextViewList) {
                if (textView != null) {
                    textView.setTextSize(contentTextSize);
                }
            }
        }
        if (vTitle != null) {
            vTitle.setTextSize(titleTextSize);
        }
        mSharedPref.saveNewsContentTextSize(aTextSize);
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
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.pause();
            }
        }
        removeYoutubeFragment();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onResume()");
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.resume();
            }
        }
        if (isOnPause) {
            isOnPause = false;
            getTextSize();
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
        public void OnPageLoadFinished(VideoNewsPageFragment aCurrentFragment, int aCurrentFragmentPosition);
        public void OnYoutubeInitializationSuccess(VideoNewsPageFragment aCurrentFragment, int aCurrentFragmentPosition, YouTubePlayer aYouTubePlayer, String aYoutubeId);
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

}

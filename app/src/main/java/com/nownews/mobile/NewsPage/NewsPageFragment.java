package com.nownews.mobile.NewsPage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import com.facebook.share.widget.LikeView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.nownews.R;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbumPage;
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
import com.nownews.mobile.Json.NewsInfoJson;
import com.nownews.mobile.Json.NewsInfoJson.MobileBody;
import com.nownews.mobile.Json.NewsInfoJson.ReferenceNewsInfo;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.Widget.CustomImageTopcrop;
import com.squareup.okhttp.internal.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NewsPageFragment extends Fragment {

    public final static String KEY_NEWS_URL = "newsUrl";
    public final static String KEY_NEWS_ID = "newsId";
    private final String TAG = getClass().getSimpleName();
    public boolean isOnPause = false;
    private RelativeLayout vImageLayout;
    private RelativeLayout vLoadingLayout;
    private CustomImageTopcrop vBigImage;
    private LinearLayout vTopDFPLayout;
    private LinearLayout vBottomDFPLayout;
    private FrameLayout vIleopardLayout;
    private TextView vTitle;
    private TextView vCategory;
    private TextView vFrom;
    private TextView vCreateAt;
    private LinearLayout vBody;
    private LinearLayout vReferenceNewsBody;
    private LinearLayout vHeadlineNewsBody;
    private TextView vHeadlineNewsTitle;
    private TextView vReferenceNewsTitle;
    private RelativeLayout vNextNewsGroup;
    private TextView vNextNewsTitle;
    private RelativeLayout vPrevNewsGroup;
    private TextView vPrevNewsTitle;
    private ProgressBar vImageProgressBar;
    private ScrollView vBodyScrollView;
    private SwipeRefreshLayout vRefreshLayout;
    private LikeView vLikeView;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private String mNewsUrl;
    private int mNewsId;
    private NewsInfoJson mNewsInfo;
    private ArrayList<TextView> mTextViewList;
    private ArrayList<TextView> mRefTitleTextViewList;
    private ArrayList<TextView> mRefCategoryTextViewList;
    private ArrayList<String> mImageUrlList;
    private HashMap<Integer, SoftReference<ImageView>> mImageViewList;
    private SharedPreferencesMethods mSharedPref;
    private int mContentTextSize;
    private int mTitleTextSize;
    private int mRefTitleTextSize;
    private int mRefCategoryTextSize;
    private String[] mEcoDefaultImageList;
    private ReSizeLayoutParams mReSizeParams;
    private boolean isRefereshing = false;
    private String mShareImgUrl;

    private ArrayList<ConcurrentHashMap<String, Object>> mContentList;
    public final static String KEY_CONTEXT_TEXT = "context_text";
    public final static String KEY_CONTEXT_IMAGE = "context_image";
    public final static String KEY_CONTEXT_IMAGE_TEXT = "context_image_text";

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
			if(Utility.DEBUG)Log.e(TAG, "onLoadingComplete!!!");
//			if(Utility.DEBUG)Log.e(TAG, "((CustomImageTopcrop)aView).getDrawable() is null or not??? " + ((CustomImageTopcrop)aView).getDrawable()==null? "true":"false");
//			if(Utility.DEBUG)Log.e(TAG, "aBitmap is null or not??? " + aBitmap==null? "true":"false");
            vLoadingLayout.setVisibility(View.GONE);
            if(aBitmap!=null){
                ((CustomImageTopcrop)aView).setImageBitmap(aBitmap);
            }
        }

        @Override
        public void onLoadingCancelled() {
        }
    };
    private List<ReferenceNewsInfo> mReferenceNewsList;
    private int mYoutubeLayoutId;
    private ArrayList<PublisherAdView> mDFPADList;
    private ArrayList<PublisherAdRequest> mDFPADRequestList;
    private ArrayList<CMNativeBannerView> mIleopardADList;
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

    private boolean isNewsInfoLoadSucess = false;
    public boolean isNewsInfoLoadSucess(){
        return isNewsInfoLoadSucess;
    }
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ParameterSet.GET_NEWS_INFO_DONE:
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vRefreshLayout.setRefreshing(false);
                    }
                    mNewsInfo = (NewsInfoJson) msg.obj;
                    if (mNewsInfo != null) {
                        isNewsInfoLoadSucess = true;
//                        while (true) {
                            if (isAdded()) {
                                processNews();
                                break;
                            }
//                        }
                    }
                    break;
                case ParameterSet.GET_NEWS_INFO_FAILED:
                    isNewsInfoLoadSucess = false;
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vRefreshLayout.setRefreshing(false);
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(getActivity());
                    if (isRefereshing) {
                        // Stop refresh animation
                        isRefereshing = false;
                        vRefreshLayout.setRefreshing(false);
                    }
                    break;
            }

        }

    };
    private OnClickListener mNextNewsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ((NewsPage) getActivity()).gotoNextNews();

        }
    };
    private OnClickListener mPrevNewsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ((NewsPage) getActivity()).gotoPrevNews();

        }
    };

    public NewsPageFragment() {
        //DO NOTHING...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_page, container, false);
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
        getTextSize();
        processView();
        processListener();
        getNewsInfo();
        processTopDFP();

    }

    private void processTopDFP(){
        addDFPAD(vTopDFPLayout, getString(R.string.dfp_news_info_top));
    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {

            String aNewsUrl = bundle.getString(KEY_NEWS_URL);
            int aNewsId = bundle.getInt(KEY_NEWS_ID, -1);

            if (Utility.DEBUG) Log.e(TAG, "VideoNewsPageFragment");
            if (Utility.DEBUG) Log.e(TAG, "aNewsUrl: " + aNewsUrl);
            if (Utility.DEBUG) Log.e(TAG, "aNewsId: " + aNewsId);

            mNewsUrl = aNewsUrl;
            mNewsId = aNewsId;
            Log.d(TAG, "mNewsUrl: " + mNewsUrl);
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
        String textSize = mSharedPref.getNewsContentTextSize();
        if (textSize.equals(getString(R.string.small))) {
            mContentTextSize = Integer.valueOf(getString(R.string.small_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_small_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_small_text_size));
            mRefCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_small_text_size));
        } else if (textSize.equals(getString(R.string.mid))) {
            mContentTextSize = Integer.valueOf(getString(R.string.mid_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_mid_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_mid_text_size));
            mRefCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_mid_text_size));
        } else if (textSize.equals(getString(R.string.max))) {
            mContentTextSize = Integer.valueOf(getString(R.string.max_text_size));
            mTitleTextSize = Integer.valueOf(getString(R.string.title_max_text_size));
            mRefTitleTextSize = Integer.valueOf(getString(R.string.ref_title_max_text_size));
            mRefCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_max_text_size));
        }
    }

    public void setData(String aNewsUrl) {

        mNewsUrl = aNewsUrl;
        if (mNewsUrl.startsWith("/n/")) {
            mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1));
        } else {
            mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1, mNewsUrl.lastIndexOf("?")));
        }
        getNewsInfo();

    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
        mBitmapController.closeBitmapController();
        mBitmapController.clearCache();
        mSharedPref = new SharedPreferencesMethods(getActivity());
        mReSizeParams = new ReSizeLayoutParams(getActivity());
        mEcoDefaultImageList = new String[]{
                "http://s.nownews.com/w/images/mobile/defaultimg-01.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-02.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-03.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-04.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-05.jpg"};
    }

    public void getNewsInfo() {
        if (mApiController != null) {
            mApiController.getNewsInfo(mHandler, mNewsId);
        }
    }

    public void processView() {

        View view = getView();

        vBigImage = (CustomImageTopcrop) view.findViewById(R.id.big_img);
        vTopDFPLayout = (LinearLayout) view.findViewById(R.id.top_dfp);
        vBottomDFPLayout = (LinearLayout) view.findViewById(R.id.bottom_dfp);
        vIleopardLayout = (FrameLayout) view.findViewById(R.id.ileopard_ad);
        vTitle = (TextView) view.findViewById(R.id.title);
        vCategory = (TextView) view.findViewById(R.id.category);
        vFrom = (TextView) view.findViewById(R.id.from);
        vCreateAt = (TextView) view.findViewById(R.id.createat);
        vBodyScrollView = (ScrollView) view.findViewById(R.id.body_scrollview);
        vBody = (LinearLayout) view.findViewById(R.id.body);
        vReferenceNewsBody = (LinearLayout) view.findViewById(R.id.reference_news_body);
        vHeadlineNewsBody = (LinearLayout) view.findViewById(R.id.headline_news_body);
        vHeadlineNewsTitle = (TextView) view.findViewById(R.id.headline_news_title);
        vReferenceNewsTitle = (TextView) view.findViewById(R.id.reference_news_title);
        vNextNewsGroup = (RelativeLayout) view.findViewById(R.id.next_news_group);
        vNextNewsTitle = (TextView) view.findViewById(R.id.next_news_title);
        vPrevNewsGroup = (RelativeLayout) view.findViewById(R.id.prev_news_group);
        vPrevNewsTitle = (TextView) view.findViewById(R.id.prev_news_title);
        vImageProgressBar = (ProgressBar) view.findViewById(R.id.image_progressbar);
        vImageLayout = (RelativeLayout) view.findViewById(R.id.image_layout);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);

        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getTextSize();
                getNewsInfo();
            }
        });
        vLikeView = (LikeView) view.findViewById(R.id.likeview);
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

        String mCategoryName = ((NewsPage) getActivity()).getCurrentNewsCategory();
        if (mNewsInfo.image != null
                && mNewsInfo.image.thumbnail != null
                && !mNewsInfo.image.thumbnail.trim().isEmpty()) {
            String bigImgUrl = mNewsInfo.image.thumbnail;
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mBitmapController.loadImageWithOriginalSize(bigImgUrl, vBigImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE, 0, 0, mBigImgLoadingListener);
            mImageUrlList.add(bigImgUrl);
            setImageClickListener(vBigImage, bigImgUrl);

            //For Share
            mShareImgUrl = mNewsInfo.image.thumbnail;
            mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, 50, mShareImgUrl);
            if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
            mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, null, BitmapController.IMAGE_SRC, 0, 0, null);
        } else if (mCategoryName.contains(getString(R.string.eco))) {
            int newsId = mNewsInfo.nodeId;
            int digit = newsId % 10;
            int imagePosition = digit % 5;
            String bigImgUrl = mEcoDefaultImageList[imagePosition];
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mBitmapController.loadImageWithOriginalSize(bigImgUrl, vBigImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE, 0, 0, mBigImgLoadingListener);
            mImageUrlList.add(bigImgUrl);
            setImageClickListener(vBigImage, bigImgUrl);

            //For Share
            mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, 50, bigImgUrl);
            if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
            mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, null, BitmapController.IMAGE_SRC, 0, 0, null);
        } else {
            vImageLayout.setVisibility(View.GONE);
        }

        if (mNewsInfo.title != null) {
            String title = mNewsInfo.title;
            vTitle.setTextSize(mTitleTextSize);
            vTitle.setText(title);
        }

        if (mNewsInfo.category != null
                && mNewsInfo.category.name != null
                && !mNewsInfo.category.name.isEmpty()) {
            vCategory.setVisibility(View.VISIBLE);
            vCategory.setText(mNewsInfo.category.name);
        } else {
            vCategory.setVisibility(View.GONE);
        }

        if (mNewsInfo.author != null) {
            String from = mNewsInfo.author;
            vFrom.setText(from);
            if (mNewsInfo.createdAt != null
                    && !mNewsInfo.createdAt.trim().isEmpty()) {
                String createAt = Utility.processDate(mNewsInfo.createdAt);
                vCreateAt.setVisibility(View.VISIBLE);
                vCreateAt.setText(createAt);
            } else {
                vCreateAt.setVisibility(View.GONE);
            }
        }

        if(mNewsInfo.url!=null){
            String url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsInfo.url;
            vLikeView.setLikeViewStyle(LikeView.Style.STANDARD);
            vLikeView.setObjectIdAndType(url, LikeView.ObjectType.PAGE);
            vLikeView.setAuxiliaryViewPosition(LikeView.AuxiliaryViewPosition.INLINE);
            vLikeView.setHorizontalAlignment(LikeView.HorizontalAlignment.RIGHT);
        }

        if (mNewsInfo.htmlBody != null) {
            String body = mNewsInfo.htmlBody;
            processBody(body);
        } else if (mNewsInfo.mobileBody != null) {
            processBody(mNewsInfo.mobileBody);
        }

        processBottomBigDFP();
        processBottomBigIleopard();
        processPreviousNextNews();
        processReferenceNews();
        processHeadlineNews();
    }

    private void processBody(List<MobileBody> jbody) {
        if (Utility.DEBUG) Log.e(TAG, "processBody(List<JsonBody> jbody)");

        vBody.removeAllViews();

        if (mTextViewList != null) {
            mTextViewList.clear();
            mTextViewList = null;
        }
        mTextViewList = new ArrayList<TextView>();

        if (mRefCategoryTextViewList != null) {
            mRefCategoryTextViewList.clear();
            mRefCategoryTextViewList = null;
        }
        mRefCategoryTextViewList = new ArrayList<TextView>();

        if (mRefTitleTextViewList != null) {
            mRefTitleTextViewList.clear();
            mRefTitleTextViewList = null;
        }
        mRefTitleTextViewList = new ArrayList<TextView>();

//        processSmallDFP();

        for (int i = 0; i < jbody.size(); i++) {
            String type = jbody.get(i).tag;
            String content = jbody.get(i).content;
            String style = jbody.get(i).style;
            if (Utility.DEBUG) Log.w(TAG, "type: " + type);
            if (type.equals("image")) {
                ImageView imgView = new ImageView(getActivity());
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                params.setMargins(0, 30, 0, 10);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                imgView.setLayoutParams(params);
                imgView.setScaleType(ScaleType.CENTER_CROP);
//                imgView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                String img = jbody.get(i).src;
                if (img != null && !img.trim().isEmpty()) {
                    img = Utility.getSrcFromImgapi(img);
                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + img);
                    mBitmapController.loadImageWithOriginalSize(img, imgView, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE, 0, 0, null);
                    vBody.addView(imgView);
                    mImageUrlList.add(img);
                    setImageClickListener(imgView, img);
                }
                if (mImageViewList == null) {
                    mImageViewList = new HashMap<Integer, SoftReference<ImageView>>();
                }
                SoftReference<ImageView> ref = new SoftReference<ImageView>(imgView);
                mImageViewList.put(i, ref);

                if (content != null && !content.trim().equals("") && !content.trim().equals("▲")) {
                    TextView citeTextView = new TextView(getActivity());
                    LayoutParams citeLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    citeTextView.setLayoutParams(citeLayoutParams);
                    citeTextView.setTextSize(mContentTextSize);
                    citeTextView.setTextColor(getResources().getColor(android.R.color.black));
//					citeTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                    citeTextView.setTypeface(null, Typeface.ITALIC);
                    citeTextView.setText(content);
                    vBody.addView(citeTextView);
                    mTextViewList.add(citeTextView);
                }
            } else if (type.equals("p")) {
                if (content != null && !content.trim().equals("")) {
                    if (content.contains("延伸閱讀")) {
                        break;
                    }
                    if(content.contains("&nbsp;")){
                        content = content.replace("&nbsp;", "");
                        if(content.trim().isEmpty()){
                            continue;
                        }
                    }
                    TextView textView = new TextView(getActivity());
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, mReSizeParams.getPixelAfterScale(20), 0, mReSizeParams.getPixelAfterScale(20));
                    textView.setLayoutParams(layoutParams);
                    textView.setLineSpacing(20.0f, 1.0f);
                    textView.setTextIsSelectable(true);
                    textView.setTextSize(mContentTextSize);
                    textView.setTextColor(getResources().getColor(android.R.color.black));
//    				textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    textView.setText(content);
                    if (style != null && style.equals("strong")) {
                        textView.setTypeface(null, Typeface.BOLD);
                    }
                    vBody.addView(textView);
                    mTextViewList.add(textView);
                }
            }
        }
    }

    private void processBody(String body) {
        if (Utility.DEBUG) Log.e(TAG, "body: " + body);

        vBody.removeAllViews();

        if (mTextViewList != null) {
            mTextViewList.clear();
            mTextViewList = null;
        }
        mTextViewList = new ArrayList<TextView>();

        if (mRefCategoryTextViewList != null) {
            mRefCategoryTextViewList.clear();
            mRefCategoryTextViewList = null;
        }
        mRefCategoryTextViewList = new ArrayList<TextView>();

        if (mRefTitleTextViewList != null) {
            mRefTitleTextViewList.clear();
            mRefTitleTextViewList = null;
        }
        mRefTitleTextViewList = new ArrayList<TextView>();

//        processSmallDFP();

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
                TextView textView = new TextView(getActivity());
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, mReSizeParams.getPixelAfterScale(20), 0, mReSizeParams.getPixelAfterScale(20));
                textView.setLayoutParams(layoutParams);
                textView.setLineSpacing(20.0f, 1.0f);
                textView.setTextIsSelectable(true);
                textView.setTextSize(mContentTextSize);
                textView.setTextColor(getResources().getColor(android.R.color.black));
//				textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                textView.setGravity(Gravity.LEFT);
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
                    imgUrl = Utility.getSrcFromImgapi(imgUrl);
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
        if (((NewsPage) getActivity()).getCurrentPage() == 0) {
            vPrevNewsGroup.setVisibility(View.GONE);
        } else {
            vPrevNewsGroup.setVisibility(View.VISIBLE);
            vPrevNewsTitle.setText(((NewsPage) getActivity()).getPrevPageTitle());
        }
        if (((NewsPage) getActivity()).isLastPage()) {
            vNextNewsGroup.setVisibility(View.GONE);
        } else {
            vNextNewsGroup.setVisibility(View.VISIBLE);
            vNextNewsTitle.setText(((NewsPage) getActivity()).getNextPageTitle());
        }
    }

    private void setYoutube(Document doc) {
        Element iframes = doc.select("iframe").first();
        if (iframes != null) {
            final String videoUrl = iframes.attr("src");
            int videoW = Integer.valueOf(iframes.attr("width"));
            int videoH = Integer.valueOf(iframes.attr("height"));
            if (Utility.DEBUG) Log.e(TAG, "videoUrl: " + videoUrl);
            if (Utility.DEBUG) Log.e(TAG, "videoW: " + videoW);
            if (Utility.DEBUG) Log.e(TAG, "videoH: " + videoH);
            if (videoUrl != null) {

                final String videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
                if (Utility.DEBUG) Log.i(TAG, "videoId: " + videoId);

                //YouTube Layout
                LinearLayout youtubeLayout = new LinearLayout(getActivity());
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 40, 0, 0);
                youtubeLayout.setOrientation(LinearLayout.VERTICAL);
                youtubeLayout.setLayoutParams(layoutParams);
                youtubeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                mYoutubeLayoutId = 0x7842 + mNewsInfo.nodeId;
                if (Utility.DEBUG) Log.e(TAG, "mYoutubeLayoutId: " + mYoutubeLayoutId);
                youtubeLayout.setId(mYoutubeLayoutId);
                vBody.addView(youtubeLayout);

//				mYoutubeFragment = new CustomYoutubePlayer(videoId);
//				getChildFragmentManager().beginTransaction().replace(mYoutubeLayoutId, mYoutubeFragment).commit();
            }
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

    private void processSmallDFP() {

        //DFP AD Layout
        LinearLayout dfpADTopLayout = new LinearLayout(getActivity());
        LayoutParams toplayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dfpADTopLayout.setLayoutParams(toplayoutParams);
        dfpADTopLayout.setOrientation(LinearLayout.VERTICAL);
        dfpADTopLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        vBody.addView(dfpADTopLayout);

        //320x50 DFP AD
        PublisherAdView dfpAdViewTop = new PublisherAdView(getActivity());
        dfpAdViewTop.setAdUnitId(getString(R.string.dfp_news_info_under_title));
        dfpAdViewTop.setAdSizes(AdSize.BANNER);
        dfpAdViewTop.setAdListener(mDfpAdListener);
        PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        dfpAdViewTop.loadAd(request);
        dfpADTopLayout.addView(dfpAdViewTop);
        if (mDFPADRequestList == null) {
            mDFPADRequestList = new ArrayList<PublisherAdRequest>();
        }
        mDFPADRequestList.add(request);
        if (mDFPADList == null) {
            mDFPADList = new ArrayList<PublisherAdView>();
        }
        mDFPADList.add(dfpAdViewTop);

    }

    private void processBottomBigDFP() {

        //add DFP AD
        if (Utility.DEBUG) Log.e(TAG, "Add DFP AD1!!");
        addDFPAD(vBottomDFPLayout, getString(R.string.dfp_news_info_bottom));

    }

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

    private void processReferenceNews() {
        Log.v(TAG, "processReferenceNews!!");
        //add reference news
        mReferenceNewsList = mNewsInfo.refNews;
        if (mReferenceNewsList == null || mReferenceNewsList.size() == 0) {
            Log.w(TAG, "mReferenceNewsList == null || mReferenceNewsList.size() == 0");
            vReferenceNewsBody.setVisibility(View.GONE);
            vReferenceNewsTitle.setVisibility(View.GONE);
            return;
        }

        if (vReferenceNewsBody != null) {
            Log.w(TAG, "vReferenceNewsBody != null");
            vReferenceNewsBody.removeAllViews();
        }

        int referenceSize = mReferenceNewsList.size();
        if (referenceSize > 5) {
            referenceSize = 5;
        }
        for (int i = 0; i < referenceSize; i++) {

            ReferenceNewsInfo newsInfo = mReferenceNewsList.get(i);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View convertView = inflater.inflate(R.layout.widget_reference_news_item, null);

            TextView vCategory = (TextView) convertView.findViewById(R.id.category);
            TextView vTitle = (TextView) convertView.findViewById(R.id.title);
            mRefTitleTextViewList.add(vTitle);
            mRefCategoryTextViewList.add(vCategory);
            CustomImageTopcrop vNewsImage = (CustomImageTopcrop) convertView.findViewById(R.id.news_image);

            String imgUrl = newsInfo.image.originImage;
            mBitmapController.loadImageWithOriginalSize(imgUrl, vNewsImage, BitmapController.IMAGE_SRC, 0, 0, null);
            String title = newsInfo.title;
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
            vCategory.setTextSize(mRefCategoryTextSize);
            Utility.setCategoryTextColor(category, vCategory, Utility.ColorType.News);
            setReferenceItemClickListener(convertView, i);
            vReferenceNewsBody.addView(convertView);

        }

    }

    private void setReferenceItemClickListener(View aConvertView, final int position) {
        aConvertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to singal news
                UserDataInfo.setReferenceNewsList(mReferenceNewsList);
                ((NewsPage) getActivity()).gotoReferenceNewsPage(position, mReferenceNewsList);

            }
        });
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
            mRefCategoryTextViewList.add(vCategory);
            mRefTitleTextViewList.add(vTitle);

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
            vCategory.setTextSize(mRefCategoryTextSize);
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
                ((NewsPage) getActivity()).gotoHeadlineNewsPage(position, headline);

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
        intent.putExtra(FavoriteAlbumPage.KEY_IMAGE_TITLE, mNewsInfo.title);
        intent.putExtra(FavoriteAlbumPage.KEY_NEWS_URL, WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN + mNewsInfo.nodeId);
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

        if (mIleopardADList != null && mIleopardADList.size() > 0) {
            for (CMNativeBannerView ileopardAD : mIleopardADList) {
                ileopardAD.onDestroy();
                ileopardAD = null;
            }
            mIleopardADList.clear();
            mIleopardADList = null;
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

        if (mTextViewList != null) {
            for (TextView textView : mTextViewList) {
                textView = null;
            }
            mTextViewList.clear();
            mTextViewList = null;
        }

        if (mRefCategoryTextViewList != null) {
            for (TextView textView : mRefCategoryTextViewList) {
                textView = null;
            }
            mRefCategoryTextViewList.clear();
            mRefCategoryTextViewList = null;
        }

        if (mRefTitleTextViewList != null) {
            for (TextView textView : mRefTitleTextViewList) {
                textView = null;
            }
            mRefTitleTextViewList.clear();
            mRefTitleTextViewList = null;
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

        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(getActivity());
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
        int refCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_small_text_size));
        if (aTextSize != null) {
            if (aTextSize.equals(getString(R.string.max))) {
                contentTextSize = Integer.valueOf(getString(R.string.max_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_max_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_max_text_size));
                refCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_max_text_size));
            } else if (aTextSize.equals(getString(R.string.mid))) {
                contentTextSize = Integer.valueOf(getString(R.string.mid_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_mid_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_mid_text_size));
                refCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_mid_text_size));
            } else if (aTextSize.equals(getString(R.string.small))) {
                contentTextSize = Integer.valueOf(getString(R.string.small_text_size));
                titleTextSize = Integer.valueOf(getString(R.string.title_small_text_size));
                refTitleTextSize = Integer.valueOf(getString(R.string.ref_title_small_text_size));
                refCategoryTextSize = Integer.valueOf(getString(R.string.ref_category_small_text_size));
            }
        }
        if (mTextViewList != null) {
            for (TextView textView : mTextViewList) {
                if (textView != null) {
                    textView.setTextSize(contentTextSize);
                }
            }
        }
        if(mRefCategoryTextViewList != null){
            for(TextView textView : mRefCategoryTextViewList){
                if(textView != null){
                    textView.setTextSize(refCategoryTextSize);
                }
            }
        }
        if(mRefTitleTextViewList != null){
            for(TextView textView : mRefTitleTextViewList){
                if(textView != null){
                    textView.setTextSize(refCategoryTextSize);
                }
            }
        }
        if (vTitle != null) {
            vTitle.setTextSize(titleTextSize);
        }
        mSharedPref.saveNewsContentTextSize(aTextSize);
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
            getNewsInfo();
        }
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

}

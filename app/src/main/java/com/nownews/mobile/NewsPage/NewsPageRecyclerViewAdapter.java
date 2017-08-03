package com.nownews.mobile.NewsPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.cmcm.adsdk.banner.CMAdView;
import com.cmcm.adsdk.banner.CMBannerAdListener;
import com.cmcm.adsdk.banner.CMBannerAdSize;
import com.cmcm.adsdk.banner.CMNativeBannerView;
import com.facebook.FacebookException;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.FavoriteAlbum.FavoriteAlbumPage;
import com.nownews.mobile.Json.HeadlineNewsJson;
import com.nownews.mobile.Json.NewsInfoJson;
import com.nownews.mobile.Json.RelationsNewsInfoJson;
import com.nownews.mobile.Widget.CustomImageTopcrop;
import com.nownews.mobile.Widget.WebActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cindy on 2017/1/25.
 */

public class NewsPageRecyclerViewAdapter extends RecyclerView.Adapter{

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private BitmapController mBitmapController;
    private SharedPreferencesMethods mSharedPref;
    private ArrayList<ConcurrentHashMap<String, Object>> mContentList;
    private List<RelationsNewsInfoJson.RelationsNewsBean> mRelationsNewsList;
    private NewsInfoJson mNewsInfo;
    private int mListSize;
    private List<HeadlineNewsJson.CarouselsBean> mHeadline;
    private ArrayList<Integer> mViewTypeList;
    private ArrayList<String> mImageUrlList;
    private String mShareImgUrl;
    private String[] mEcoDefaultImageList;
    private CMNativeBannerView mIleopardAd;
    private int mReferenceTitlePosition = -1;
    private int mHeadlineTitlePosition = -1;
    private int mReferenceListStartPosition = -1;
    private int mHeadlineListStartPosition = -1;
    private int mReferenceListSize;
    private int mHeadlineListSize;
    private boolean useWebBody = false;

    private final int VIEW_TYPE_NEWS_INFO = R.layout.widget_news_page_news_info_item;
    private final int VIEW_TYPE_CONTENT_TEXT = R.layout.widget_news_page_context_text_item;
    private final int VIEW_TYPE_CONTENT_IMAGE = R.layout.widget_news_page_context_image_item;
    private final int VIEW_TYPE_CONTENT_VIDEO = R.layout.widget_news_page_context_video_item;
    private final int VIEW_TYPE_BOTTOM_AD = R.layout.widget_news_page_ad_item;
    private final int VIEW_TYPE_PRE_NEXT_NEWS = R.layout.widget_news_page_prev_next_item;
    private final int VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE = R.layout.widget_news_page_reference_title_item;
    private final int VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS = R.layout.widget_reference_news_item;
    private final int VIEW_TYPE_WEB_BODY = R.layout.widget_news_page_web_body;

    public NewsPageRecyclerViewAdapter(Context aContext, ArrayList<ConcurrentHashMap<String, Object>> aContentList,
                                       NewsInfoJson aNewsInfo, ArrayList<String> aImageUrlList, OnVideoPlayButtonClick aListener, List<RelationsNewsInfoJson.RelationsNewsBean> aRelationsNewsList){
        mContext = aContext;
        if(Utility.DEBUG)Log.i(TAG, "mContext is null nor not?? " + (mContext==null? "true":"false"));
        mContentList = aContentList;
        mNewsInfo = aNewsInfo;
        mImageUrlList = aImageUrlList;
        mListener = aListener;
        mRelationsNewsList = aRelationsNewsList;
        initController();
        initListInfo();

    }

    private void initController(){
        mSharedPref = new SharedPreferencesMethods(mContext);
        mBitmapController = BitmapController.getInstance(mContext);
        mEcoDefaultImageList = new String[]{
                "http://s.nownews.com/w/images/mobile/defaultimg-01.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-02.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-03.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-04.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-05.jpg"};
        String textSize = mSharedPref.getNewsContentTextSize();
        if(mSharedPref!=null){
            mSharedPref.unRegistContext(mContext);
        }
        initTextSize(textSize);
    }

    private void initListInfo(){

        if(mNewsInfo==null){
            mListSize = 0;
            return;
        }

        mViewTypeList = new ArrayList<>();

        mListSize++; //VIEW_TYPE_NEWS_INFO
        mViewTypeList.add(VIEW_TYPE_NEWS_INFO);

        if(useWebBody){
            mListSize++;
            mViewTypeList.add(VIEW_TYPE_WEB_BODY);
        }else{
            if(mContentList!=null && mContentList.size()>0){
                if(Utility.DEBUG)Log.d(TAG, "mContentList.size(): " + mContentList.size());
                //VIEW_TYPE_CONTENT_TEXT OR VIEW_TYPE_CONTENT_IMAGE
                mListSize += mContentList.size();
                for(int i = 0; i<mContentList.size(); i++){
                    ConcurrentHashMap<String, Object> map = mContentList.get(i);
                    if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT)!=null){
                        mViewTypeList.add(VIEW_TYPE_CONTENT_TEXT);
                    }else if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IMAGE)!=null){
                        mViewTypeList.add(VIEW_TYPE_CONTENT_IMAGE);
                    }else if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IFRAME_YOUTUBE)!=null){
                        mViewTypeList.add(VIEW_TYPE_CONTENT_VIDEO);
                    }
                }
            }
        }

        mListSize+=2; //VIEW_TYPE_BOTTOM_AD + VIEW_TYPE_PRE_NEXT_NEWS
        mViewTypeList.add(VIEW_TYPE_BOTTOM_AD);
        mViewTypeList.add(VIEW_TYPE_PRE_NEXT_NEWS);

        if(mRelationsNewsList!=null && mRelationsNewsList.size()>0){
            if(Utility.DEBUG)Log.d(TAG, "mNewsInfo.refNews.size(): " + mRelationsNewsList.size());
            mReferenceTitlePosition = mListSize;
            mListSize++; //VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE
            mViewTypeList.add(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE);
            mReferenceListStartPosition = mListSize;
            mReferenceListSize = mRelationsNewsList.size();
            if (mReferenceListSize > 5) {
                mReferenceListSize = 5;
            }
            mListSize += mReferenceListSize; //VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS
            for(int i = 0; i<mReferenceListSize; i++){
                mViewTypeList.add(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS);
            }
        }
        mHeadline = UserDataInfo.getHeadlineContent();
        if(mHeadline!=null && mHeadline.size()>0){
            if(Utility.DEBUG)Log.d(TAG, "mHeadline.size(): " + mHeadline.size());
            mHeadlineTitlePosition = mListSize;
            mListSize++; //VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE
            mViewTypeList.add(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE);
            mHeadlineListStartPosition = mListSize;
            mHeadlineListSize = mHeadline.size();
            if (mHeadlineListSize > 3) {
                mHeadlineListSize = 3;
            }
            mListSize += mHeadlineListSize;
            for(int i = 0; i<mHeadlineListSize; i++){
                mViewTypeList.add(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS);
            }
        }
        if(Utility.DEBUG)Log.i(TAG, "mListSize: " + mListSize);
    }

    @Override
    public int getItemViewType(int position) {
        return mViewTypeList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_NEWS_INFO:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_NEWS_INFO, null);
                NewsInfoViewHolder newsInfoViewHolder = new NewsInfoViewHolder(view);
                return newsInfoViewHolder;

            case VIEW_TYPE_CONTENT_TEXT:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_CONTENT_TEXT, null);
                ContextTextViewHolder contextTextViewHolder = new ContextTextViewHolder(view);
                return contextTextViewHolder;

            case VIEW_TYPE_CONTENT_IMAGE:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_CONTENT_IMAGE, null);
                ContextImageViewHolder contextImageViewHolder = new ContextImageViewHolder(view);
                return contextImageViewHolder;

            case VIEW_TYPE_CONTENT_VIDEO:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_CONTENT_VIDEO, null);
                ContextVideoViewHolder contextVideoViewHolder = new ContextVideoViewHolder(view);
                return contextVideoViewHolder;

            case VIEW_TYPE_BOTTOM_AD:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_BOTTOM_AD, null);
                ADViewHolder adViewHolder = new ADViewHolder(view);
                return adViewHolder;

            case VIEW_TYPE_PRE_NEXT_NEWS:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_PRE_NEXT_NEWS, null);
                PrevNextNewsViewHolder prevNextNewsViewHolder = new PrevNextNewsViewHolder(view);
                return prevNextNewsViewHolder;

            case VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE, null);
                RefHeadlineTitleViewHolder refHeadlineTitleViewHolder = new RefHeadlineTitleViewHolder(view);
                return refHeadlineTitleViewHolder;

            case VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS, null);
                RefHeadlineNewsViewHolder refHeadlineNewsViewHolder = new RefHeadlineNewsViewHolder(view);
                return refHeadlineNewsViewHolder;

            case VIEW_TYPE_WEB_BODY:
                view = LayoutInflater.from(mContext).inflate(VIEW_TYPE_WEB_BODY, null);
                WebBodyViewHolder webBodyViewHolder = new WebBodyViewHolder(view);
                return webBodyViewHolder;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (Utility.DEBUG)Log.e(TAG, "position: " + position);
        if(position<0){
            return;
        }

        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_NEWS_INFO:
                ((NewsInfoViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_CONTENT_TEXT:
                ((ContextTextViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_CONTENT_IMAGE:
                ((ContextImageViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_CONTENT_VIDEO:
                ((ContextVideoViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_BOTTOM_AD:
                ((ADViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_PRE_NEXT_NEWS:
                ((PrevNextNewsViewHolder)holder).bind(position);
                break;

            case VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS_TITLE:
                if(position==mReferenceTitlePosition){
                    ((RefHeadlineTitleViewHolder)holder).referenceNewsBind(position);
                }else if(position==mHeadlineTitlePosition){
                    ((RefHeadlineTitleViewHolder)holder).headlineNewsBind(position);
                }
                break;

            case VIEW_TYPE_REFERENCE_AND_HEADLINE_NEWS:
                if(mReferenceListStartPosition!=-1
                        && (position>=mReferenceListStartPosition && position<mReferenceListStartPosition+mReferenceListSize)){
                    ((RefHeadlineNewsViewHolder)holder).referenceNewsbind(position);
                }else if(mHeadlineListStartPosition!=-1
                        && (position>=mHeadlineListStartPosition && position<mHeadlineListStartPosition+mHeadlineListSize)){
                    ((RefHeadlineNewsViewHolder)holder).headlineNewsbind(position);
                }
                break;

            case VIEW_TYPE_WEB_BODY:
                ((WebBodyViewHolder)holder).bind(position);
                break;

        }

    }

    @Override
    public int getItemCount() {
        return mListSize;
    }

    private boolean isVideosMoreThanOne;
    public class NewsInfoViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout vTopDFP;
        private TextView vTitle;
        private TextView vCategory;
        private TextView vFrom;
        private TextView vCreateAt;
        private LikeView vLikeView;
        private RelativeLayout vImageLayout;
        private RelativeLayout vLoadingLayout;
        private ProgressBar vImageProgressBar;
        private CustomImageTopcrop vBigImage;
        private TextView vBigImgageText;
        private ImageView vPlayIcon;
        private boolean hasYoutubeVideo;
        private String mYoutubeId;

        public NewsInfoViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vTopDFP = (LinearLayout) itemView.findViewById(R.id.top_dfp);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vCategory = (TextView) itemView.findViewById(R.id.category);
            vFrom = (TextView) itemView.findViewById(R.id.from);
            vCreateAt = (TextView) itemView.findViewById(R.id.createat);
            vLikeView = (LikeView) itemView.findViewById(R.id.likeview);
            vImageLayout = (RelativeLayout) itemView.findViewById(R.id.image_layout);
            vLoadingLayout = (RelativeLayout) itemView.findViewById(R.id.loading_layout);
            vImageProgressBar = (ProgressBar) itemView.findViewById(R.id.image_progressbar);
            vBigImage = (CustomImageTopcrop) itemView.findViewById(R.id.big_img);
            vBigImgageText = (TextView) itemView.findViewById(R.id.big_img_text);
            vPlayIcon = (ImageView) itemView.findViewById(R.id.play_icon);

        }

        public void bind(int position){

            //play icon
            if(mContentList!=null && mContentList.size()>0){
                for(ConcurrentHashMap<String, Object> map : mContentList){
                    if(map.containsKey(NewsPageRecyclerViewFragment.KEY_CONTEXT_IFRAME_YOUTUBE)){
                        vPlayIcon.setVisibility(View.VISIBLE);
                        hasYoutubeVideo = true;
                        mYoutubeId = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IFRAME_YOUTUBE);
                        mYoutubeId = getYoutubeIdFromUrl(mYoutubeId);
                        break;
                    }
                }
            }
            if(Utility.DEBUG)Log.d(TAG, "mYoutubeId: " + mYoutubeId);

            //DFP
            if(Utility.DEBUG)Log.i(TAG, "vTopDFP is null nor not?? " + (vTopDFP==null? "true":"false"));
            if(Utility.DEBUG)Log.i(TAG, "mContext is null nor not?? " + (mContext==null? "true":"false"));
            addDFPAD(vTopDFP, mContext.getString(R.string.dfp_news_info_top));

            //Title
            if(mNewsInfo.getTitle()!=null && !mNewsInfo.getTitle().isEmpty()){
                vTitle.setText(mNewsInfo.getTitle());
                vTitle.setTextSize(mTitleTextSize);
            }

            //Category
            String categoryName = null;
            if (mNewsInfo.getMainMenu() != null
                    && mNewsInfo.getMainMenu().getName() != null
                    && !mNewsInfo.getMainMenu().getName().isEmpty()) {
                categoryName = mNewsInfo.getMainMenu().getName();
                vCategory.setVisibility(View.VISIBLE);
                vCategory.setText(categoryName);
                vCategory.setTextSize(mRefTitleTextSize);
            } else {
                vCategory.setVisibility(View.GONE);
            }

            //Author
            if (mNewsInfo.getNewsBy() != null) {
                String from = mNewsInfo.getNewsBy();
                vFrom.setText(from);
                vFrom.setTextSize(mRefTitleTextSize);
                if (mNewsInfo.getFormatStartedAt() != null
                        && !mNewsInfo.getFormatStartedAt().trim().isEmpty()) {
                    String createAt = Utility.processDate(mNewsInfo.getFormatStartedAt());
                    vCreateAt.setVisibility(View.VISIBLE);
                    vCreateAt.setText(createAt);
                    vCreateAt.setTextSize(mRefTitleTextSize);
                } else {
                    vCreateAt.setVisibility(View.GONE);
                }
            }

            //LikeView
            if(mNewsInfo.getParseUrl()!=null){
                String url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsInfo.getParseUrl();
                vLikeView.setLikeViewStyle(LikeView.Style.STANDARD);
                vLikeView.setObjectIdAndType(url, LikeView.ObjectType.PAGE);
                vLikeView.setAuxiliaryViewPosition(LikeView.AuxiliaryViewPosition.INLINE);
                vLikeView.setHorizontalAlignment(LikeView.HorizontalAlignment.RIGHT);
                vLikeView.setOnErrorListener(new LikeView.OnErrorListener() {
                    @Override
                    public void onError(FacebookException error) {
                        if(Utility.DEBUG)Log.e(TAG, error.getMessage(), error);
                    }
                });
            }

            //BigImage
            if (mNewsInfo.getMainPhoto() != null
                    && mNewsInfo.getMainPhoto().getUrl() != null
                    && !mNewsInfo.getMainPhoto().getUrl().trim().isEmpty()) {
                String bigImgUrl = mNewsInfo.getMainPhoto().getUrl();

                if (categoryName!=null && categoryName.contains(mContext.getString(R.string.eco))
                        && (bigImgUrl.contains("defaultimg.gif") || bigImgUrl.contains("default_terry.jpg"))) {
                    int newsId = mNewsInfo.getSn();
                    int digit = newsId % 10;
                    int imagePosition = digit % 5;
                    bigImgUrl = mEcoDefaultImageList[imagePosition];
                    if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
                    mBitmapController.loadImageWithOriginalSize(bigImgUrl, vBigImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE, 0, 0, mBigImgLoadingListener);
                    setImageClickListener(vBigImage, bigImgUrl, mYoutubeId);

                    //For Share
                    mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, Utility.IMG_QUALITY, bigImgUrl);
                    if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
                    mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, null, BitmapController.IMAGE_SRC, 0, 0, null);
                }else{

                    if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
//                  bigImgUrl = Utility.getSrcFromImgapi(bigImgUrl);
                    int screenWidth = Utility.getScreenWidth(mContext);
                    bigImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, bigImgUrl);
                    mBitmapController.loadImageWithOriginalSize(bigImgUrl, vBigImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE_TOP_IMAGE, 0, 0, mBigImgLoadingListener);
                    setImageClickListener(vBigImage, mNewsInfo.getMainPhoto().getUrl(), mYoutubeId);

                    //For Share
                    mShareImgUrl = mNewsInfo.getMainPhoto().getUrl();
                    mShareImgUrl = Utility.getSrcFromImgapi(mShareImgUrl);
                    mShareImgUrl = String.format(WebAPIUrl.SCALE_IMAGE, 100, 100, Utility.IMG_QUALITY, mShareImgUrl);
                    if (Utility.DEBUG) Log.v(TAG, "===@@###mShareImgUrl: " + mShareImgUrl);
                    mBitmapController.preloadOriginalImageFromUrl(mShareImgUrl, null, BitmapController.IMAGE_SRC, 0, 0, null);
                }

            } else {
                mBitmapController.setDefaultImage(vBigImage);
                setImageClickListener(vBigImage, null, mYoutubeId);
            }

            if (mNewsInfo.getMainPhoto() != null
                    && mNewsInfo.getMainPhoto().getDesc() != null
                    && !mNewsInfo.getMainPhoto().getDesc().trim().isEmpty()) {
                vBigImgageText.setText(mNewsInfo.getMainPhoto().getDesc());
                vBigImgageText.setTextSize(mRefCategoryTextSize);
            }

        }

        private void setImageClickListener(ImageView aImgView, final String aImgUrl, final String aYoutubeId) {
            aImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    //show image page
                    if (Utility.DEBUG) Log.e(TAG, "image click!!!");
                    if (Utility.DEBUG) Log.v(TAG, "aImgUrl: " + aImgUrl);
                    if (Utility.DEBUG) Log.v(TAG, "mImageUrlList: " + mImageUrlList);

                    int position = mImageUrlList.indexOf(aImgUrl);
                    if(position!=-1){
                        if(hasYoutubeVideo && aYoutubeId!=null && !aYoutubeId.isEmpty()){
                            goToPlayYoutube(aYoutubeId);
                        }else{
                            //gotoNewsAlbumPage
                            goToNewsAlbumPage(position);
                        }
                    }else{
                        if(hasYoutubeVideo && aYoutubeId!=null && !aYoutubeId.isEmpty()){
                            goToPlayYoutube(aYoutubeId);
                        }
                    }

                }
            });
        }

        private void goToPlayYoutube(String aYoutubeId) {
            if (mListener != null) {
                mListener.onVideoPlayButtonClick(aYoutubeId);
            }
        }

        private BitmapController.ImageLoadingListener mBigImgLoadingListener = new BitmapController.ImageLoadingListener() {

            @Override
            public void onProgressUpdate(String aImageUrl, int aProgress, int max) {
//			if(Utility.DEBUG)Log.v(TAG, "aProgress: " + aProgress);
                if (vLoadingLayout.getVisibility() == View.GONE) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {

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
                vLoadingLayout.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled() {
            }
        };
    }

    private String getYoutubeIdFromUrl(String aUrl){
        if(aUrl.contains("http") || aUrl.contains("https")){

            if(aUrl.contains("v=")){
                final String regex = "v=([^\\s&#]*)";
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher = pattern.matcher(aUrl);

                if(matcher.find()) {
                    System.out.println(matcher.group(1));
                    aUrl = matcher.group(1);
                }
            }

            if(aUrl.contains("/")){
                aUrl = aUrl.substring(aUrl.lastIndexOf("/")+1);
            }

            if(aUrl.contains("?")){
                aUrl = aUrl.substring(0, aUrl.indexOf("?"));
            }

            Log.i(TAG, "aUrl: " + aUrl);
        }
        return aUrl;
    }

    private void goToNewsAlbumPage(int position) {
        Intent intent = new Intent();
        intent.setClass(mContext, FavoriteAlbumPage.class);
        intent.putExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_POSITION, position);
        intent.putStringArrayListExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_LIST, mImageUrlList);
        intent.putExtra(FavoriteAlbumPage.KEY_TYPE, FavoriteAlbumPage.TYPE_NEWS_IMAGES);
        intent.putExtra(FavoriteAlbumPage.KEY_IMAGE_TITLE, mNewsInfo.getMainPhoto().getDesc());
        intent.putExtra(FavoriteAlbumPage.KEY_NEWS_URL, mNewsInfo.getCompleteUrl());
        mContext.startActivity(intent);
    }

    public class ContextTextViewHolder extends RecyclerView.ViewHolder {

        private TextView vContentText;
        private String urlLink;

        public ContextTextViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vContentText = (TextView) itemView.findViewById(R.id.content_text);

        }

        public void bind(int position){

            if(mContentList==null || mContentList.size()==0){
                return;
            }
            int realPosition = position-1;
            ConcurrentHashMap<String, Object> map = mContentList.get(realPosition);
            if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT)!=null){
                String text = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT);
                if(Utility.DEBUG)Log.v(TAG, "text: " + text);
                if (text.contains("(((") && text.contains(")))")) {
                    SpannableString content = getStrongText(text.trim());
                    vContentText.setText(content);
                }else{
                    vContentText.setText(text);
                }

                vContentText.setTextSize(mContentTextSize);
                if(text.contains("http://") || text.contains("https://")){
                    Linkify.addLinks(vContentText, Linkify.WEB_URLS);
                    URLSpan spans[] = vContentText.getUrls();
                    for(URLSpan span: spans) {
                        urlLink = span.getURL();
                        if(Utility.DEBUG)Log.d(TAG, urlLink);
                    }
                    vContentText.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {

                            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){

                                Intent intent = new Intent();
                                intent.setClass(mContext, WebActivity.class);
                                intent.putExtra(WebActivity.KEY_URL, urlLink);
                                mContext.startActivity(intent);

                            }


                            return true;
                        }
                    });
                }
                if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_LINK)!=null
                        && map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_LINK_HTML)!=null
                        && map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_HTML)!=null){
                    final String urlLink = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_LINK) ;
                    String textLink = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_LINK_HTML);
                    String textHtml = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_TEXT_HTML);
                    textHtml = textHtml.replace("&amp;&amp;&amp;", "<br>");
                    if(Utility.DEBUG)Log.d(TAG, "urlLink: " + urlLink);
                    if(Utility.DEBUG)Log.d(TAG, "textLink: " + textLink);
                    if(Utility.DEBUG)Log.d(TAG, "textHtml: " + textHtml);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        vContentText.setText(Html.fromHtml(textHtml,Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        vContentText.setText(Html.fromHtml(textHtml));
                    }
                    MovementMethod movementMethod = new LinkMovementMethod(){
                        @Override
                        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

                            int action = event.getAction();
                            if(action==MotionEvent.ACTION_DOWN){

                                int x = (int) event.getX();
                                int y = (int) event.getY();

                                x -= widget.getTotalPaddingLeft();
                                y -= widget.getTotalPaddingTop();

                                x += widget.getScrollX();
                                y += widget.getScrollY();

                                Layout layout = widget.getLayout();
                                int line = layout.getLineForVertical(y);
                                int off = layout.getOffsetForHorizontal(line, x);

                                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                                if (link.length != 0) { // 是連結點擊

                                    if(Utility.DEBUG)Log.e(TAG, "是連結");
                                    Intent intent = new Intent();
                                    intent.setClass(mContext, WebActivity.class);
                                    intent.putExtra(WebActivity.KEY_URL, urlLink);
                                    mContext.startActivity(intent);

                                    return true;
                                } else { // 不是連結點擊
                                    if(Utility.DEBUG)Log.e(TAG, "不是連結");
                                    return false;
                                }

                            }

                            return false;
                        }
                    };
                    vContentText.setMovementMethod(movementMethod);
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

    }

    public class ContextImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CustomImageTopcrop vImage;
        private TextView vImageText;
        private String mCurrentImageUrl;

        public ContextImageViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            itemView.setOnClickListener(this);
            vImage = (CustomImageTopcrop) itemView.findViewById(R.id.image);
            vImageText = (TextView) itemView.findViewById(R.id.image_text);

        }

        public void bind(int position){

            vImage.setImageDrawable(null);
            if(mContentList==null || mContentList.size()==0){
                return;
            }
            int realPosition = position-1;
            ConcurrentHashMap<String, Object> map = mContentList.get(realPosition);
            if(map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IMAGE)!=null){
                String imageUrl = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IMAGE);
                mCurrentImageUrl = imageUrl;
//                imageUrl = Utility.getSrcFromImgapi(imageUrl);
                int screenWidth = Utility.getScreenWidth(mContext);
                if(imageUrl.contains("img.nownews.com") || imageUrl.contains("s.nownews.com")){
                    imageUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, imageUrl);
                }
                mBitmapController.loadImageWithOriginalSize(imageUrl, vImage, BitmapController.IMAGE_SRC_FROM_NEWS_PAGE, 0, 0, null);
                String imageText = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IMAGE_TEXT);
                if(Utility.DEBUG)Log.e(TAG, "imageText: " + imageText);
                if(imageText!=null){
                    vImageText.setVisibility(View.VISIBLE);
                    vImageText.setText(imageText);
                    vImageText.setTextSize(mRefCategoryTextSize);
                }else{
                    vImageText.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public void onClick(View view) {

            int position = mImageUrlList.indexOf(mCurrentImageUrl);
            if(position!=-1){
                //gotoNewsAlbumPage
                goToNewsAlbumPage(position);
            }else{
                return;
            }

        }
    }

    public class ContextVideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private YouTubeThumbnailView vYoutubeView;
        private String mCurrentYoutubeId;

        public ContextVideoViewHolder(View itemView){
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            itemView.setOnClickListener(this);
            vYoutubeView = (YouTubeThumbnailView)itemView.findViewById(R.id.content_video);

        }

        public void bind(int position){

            int realPosition = position-1;
            if(Utility.DEBUG)Log.w(TAG, "realPosition: " + realPosition);
            ConcurrentHashMap<String, Object> map = mContentList.get(realPosition);
            if(map!=null && map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IFRAME_YOUTUBE)!=null){
                mCurrentYoutubeId = (String) map.get(NewsPageRecyclerViewFragment.KEY_CONTEXT_IFRAME_YOUTUBE);
                mCurrentYoutubeId = getYoutubeIdFromUrl(mCurrentYoutubeId);
            }
            if(Utility.DEBUG)Log.d(TAG, "mCurrentYoutubeId: " + mCurrentYoutubeId);

            vYoutubeView.initialize(UserDataInfo.YOUTUBE_DEVELOPER_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {

                    youTubeThumbnailLoader.setVideo(mCurrentYoutubeId);
                    youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                        @Override
                        public void onThumbnailLoaded(final YouTubeThumbnailView youTubeThumbnailView, final String s) {

                            if(Utility.DEBUG)Log.d(TAG, "onThumbnailLoaded Success!!");

                            vYoutubeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {

                                    if(Utility.DEBUG)Log.e(TAG, "----------------- " + s + " -----------------");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        vYoutubeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    } else {
                                        //noinspection deprecation
                                        vYoutubeView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    }
                                    int width = vYoutubeView.getWidth();
                                    int height = vYoutubeView.getHeight();
                                    if(Utility.DEBUG)Log.d(TAG, "before width: " + width);
                                    if(Utility.DEBUG)Log.d(TAG, "before height: " + height);
                                    float scale = ((float)Utility.getScreenWidth(mContext)) / width;
                                    ViewGroup.LayoutParams layoutParams = vYoutubeView.getLayoutParams();
                                    layoutParams.width = Utility.getScreenWidth(mContext);
                                    layoutParams.height = (int)(height * scale);
                                    if(Utility.DEBUG)Log.d(TAG, "after width: " + layoutParams.width);
                                    if(Utility.DEBUG)Log.d(TAG, "after height: " + layoutParams.height);
                                    vYoutubeView.setLayoutParams(layoutParams);

                                }
                            });

                            youTubeThumbnailLoader.release();

                        }

                        @Override
                        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                            if(Utility.DEBUG)Log.e(TAG, "onThumbnailError errorReason: " + errorReason.name());

                        }
                    });

                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onVideoPlayButtonClick(mCurrentYoutubeId);
            }
        }

    }

    public class ADViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout vBottomDFP;
        private FrameLayout vIleopard_AD;

        public ADViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vBottomDFP = (LinearLayout) itemView.findViewById(R.id.bottom_dfp);
            vIleopard_AD = (FrameLayout) itemView.findViewById(R.id.ileopard_ad);

        }

        public void bind(int position){

            addDFPAD(vBottomDFP, mContext.getString(R.string.dfp_news_info_bottom));

            //add 雪豹 AD
            if (Utility.DEBUG) Log.e(TAG, "Add 雪豹 AD1!!");
            mIleopardAd = new CMNativeBannerView(mContext);
            mIleopardAd.setAdSize(CMBannerAdSize.BANNER_300_250);
            mIleopardAd.setPosid(mContext.getString(R.string.ileopard_300_250_banner));
            mIleopardAd.setAdListener(new CMBannerAdListener() {
                @Override
                public void onAdLoaded(CMAdView cmAdView) {
                    vIleopard_AD.removeAllViews();
                    vIleopard_AD.addView(mIleopardAd);
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
        }

    }

    public class PrevNextNewsViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout vPrevNewsGroup;
        private TextView vPrevNewsTitle;
        private RelativeLayout vNextNewsGroup;
        private TextView vNextNewsTitle;

        public PrevNextNewsViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vPrevNewsGroup = (RelativeLayout) itemView.findViewById(R.id.prev_news_group);
            vPrevNewsTitle = (TextView) itemView.findViewById(R.id.prev_news_title);
            vNextNewsGroup = (RelativeLayout) itemView.findViewById(R.id.next_news_group);
            vNextNewsTitle = (TextView) itemView.findViewById(R.id.next_news_title);

            vPrevNewsGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((NewsPage) mContext).gotoPrevNews();

                }
            });

            vNextNewsGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((NewsPage) mContext).gotoNextNews();

                }
            });

        }

        public void bind(int position){

            if (((NewsPage) mContext).getCurrentPage() == 0) {
                vPrevNewsGroup.setVisibility(View.GONE);
            } else {
                vPrevNewsGroup.setVisibility(View.VISIBLE);
                vPrevNewsTitle.setText(((NewsPage) mContext).getPrevPageTitle());
                vPrevNewsTitle.setTextSize(mRefTitleTextSize);
            }
            if (((NewsPage) mContext).isLastPage()) {
                vNextNewsGroup.setVisibility(View.GONE);
            } else {
                vNextNewsGroup.setVisibility(View.VISIBLE);
                vNextNewsTitle.setText(((NewsPage) mContext).getNextPageTitle());
                vNextNewsTitle.setTextSize(mRefTitleTextSize);
            }

        }

    }

    public class RefHeadlineTitleViewHolder extends RecyclerView.ViewHolder {

        private TextView vReferenceTitle;
        private TextView vHeadlineTitle;

        public RefHeadlineTitleViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vReferenceTitle = (TextView) itemView.findViewById(R.id.reference_news_title);
            vHeadlineTitle = (TextView) itemView.findViewById(R.id.headline_news_title);

        }

        public void referenceNewsBind(int position){

            //Set ReferenceNews
            if(mRelationsNewsList==null || mRelationsNewsList.size()==0){
                return;
            }

            vReferenceTitle.setVisibility(View.VISIBLE);
            vReferenceTitle.setTextSize(mContentTextSize);
            vHeadlineTitle.setVisibility(View.GONE);
            vHeadlineTitle.setTextSize(mContentTextSize);

        }

        public void headlineNewsBind(int position){

            if(mHeadline==null || mHeadline.size()==0){
                return;
            }

            vReferenceTitle.setVisibility(View.GONE);
            vReferenceTitle.setTextSize(mContentTextSize);
            vHeadlineTitle.setVisibility(View.VISIBLE);
            vHeadlineTitle.setTextSize(mContentTextSize);

        }
    }

    public class RefHeadlineNewsViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout vNewsItem;
        private CustomImageTopcrop vNewsImage;
        private TextView vCategory;
        private TextView vTitle;
        private int mCurrentPosition;

        public RefHeadlineNewsViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vNewsItem = (RelativeLayout) itemView.findViewById(R.id.news_item);
            vNewsImage = (CustomImageTopcrop) itemView.findViewById(R.id.news_image);
            vCategory = (TextView) itemView.findViewById(R.id.category);
            vTitle = (TextView) itemView.findViewById(R.id.title);

        }

        public void referenceNewsbind(int position){

            int realPosition = position-mReferenceListStartPosition;
            if(Utility.DEBUG)Log.i(TAG, "referenceNewsbind realPosition: " + realPosition);
            mCurrentPosition = realPosition;
            String imageUrl = mRelationsNewsList.get(realPosition).getMainPhoto().getThumbnail();
            if(Utility.DEBUG)Log.i(TAG, "referenceNewsbind imageUrl: " + imageUrl);
            mBitmapController.loadImageWithOriginalSize(imageUrl, vNewsImage, BitmapController.IMAGE_SRC_FROM_NEWS_LIST, 0, 0, null);

            RelationsNewsInfoJson.RelationsNewsBean categoryInfo = mRelationsNewsList.get(realPosition);
            String category;
            if (categoryInfo == null
                    || categoryInfo.getMainMenu() == null
                    || categoryInfo.getMainMenu().getName() == null) {
                category = mContext.getString(R.string.non_category);
            } else {
                category = categoryInfo.getMainMenu().getName();
            }
            Utility.setCategoryTextColor(category, vCategory, Utility.ColorType.News);
            vCategory.setText(category);
            vCategory.setTextSize(mRefCategoryTextSize);

            String title = mRelationsNewsList.get(realPosition).getTitle();
            vTitle.setText(title);
            vTitle.setTextSize(mRefTitleTextSize);

            vNewsItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserDataInfo.setReferenceNewsList(mRelationsNewsList);
                    ((NewsPage) mContext).gotoReferenceNewsPage(mCurrentPosition, mRelationsNewsList);
                }
            });

        }

        public void headlineNewsbind(int position){

            int realPosition = position-mHeadlineListStartPosition;
            if(Utility.DEBUG)Log.i(TAG, "realPosition: " + realPosition);
            mCurrentPosition = realPosition;
            String imageUrl = mHeadline.get(realPosition).getMainPhoto().getThumbnail();
            mBitmapController.loadImageWithOriginalSize(imageUrl, vNewsImage, BitmapController.IMAGE_SRC_FROM_NEWS_LIST, 0, 0, null);

            HeadlineNewsJson.CarouselsBean categoryInfo = mHeadline.get(realPosition);
            String category;
            if (categoryInfo == null
                    || categoryInfo.getMainMenu() == null
                    || categoryInfo.getMainMenu().getName() == null) {
                category = mContext.getString(R.string.non_category);
            } else {
                category = categoryInfo.getMainMenu().getName();
            }
            Utility.setCategoryTextColor(category, vCategory, Utility.ColorType.News);
            vCategory.setText(category);
            vCategory.setTextSize(mRefCategoryTextSize);

            String title = mHeadline.get(realPosition).getShortTitle();
            vTitle.setText(title);
            vTitle.setTextSize(mRefTitleTextSize);

            vNewsItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((NewsPage) mContext).gotoHeadlineNewsPage(mCurrentPosition, mHeadline);
                }
            });
        }
    }

    public class WebBodyViewHolder extends RecyclerView.ViewHolder{

        private WebView vWebView;

        public WebBodyViewHolder(View itemView) {
            super(itemView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);

            vWebView = (WebView) itemView.findViewById(R.id.web_body);

        }

        public void bind(int position){

            String body = mNewsInfo.getContent();
            body = "<html><body>" + body + "</body></html>";
            vWebView.loadData(body, "text/html; charset=utf-8", "UTF-8");
            vWebView.getSettings().setJavaScriptEnabled(true);
            vWebView.getSettings().setDefaultFontSize(mTitleTextSize);
            vWebView.getSettings().setUseWideViewPort(true);
            vWebView.setVerticalScrollBarEnabled(false);
            vWebView.setBackgroundColor(Color.TRANSPARENT);

        }

    }

    private ArrayList<PublisherAdView> mDFPADList;
    private void addDFPAD(LinearLayout aTargetLayout, String aUnitId) {

        aTargetLayout.removeAllViews();
        PublisherAdView dfpAdView = new PublisherAdView(mContext);
        dfpAdView.setAdUnitId(aUnitId);
        dfpAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);
//        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
//        builder.addTestDevice("3F296C0E07DBC64F3ED95E7FA105ED8C");
//        builder.addTestDevice("47D3A2E05A8397836B567EB9AA537A0E");
        PublisherAdRequest request = new PublisherAdRequest.Builder().build();
        dfpAdView.loadAd(request);
        aTargetLayout.addView(dfpAdView);
        if (mDFPADList == null) {
            mDFPADList = new ArrayList<PublisherAdView>();
        }
        mDFPADList.add(dfpAdView);
    }

    public void pauseDFP(){
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.pause();
            }
        }
    }

    public void resumeDFP(){
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.resume();
            }
        }
    }

    public void destory(){
        if (mDFPADList != null && mDFPADList.size() > 0) {
            for (PublisherAdView dfp : mDFPADList) {
                dfp.removeAllViews();
                dfp.destroy();
                dfp = null;
            }
            mDFPADList.clear();
            mDFPADList = null;
        }
        if(mIleopardAd!=null){
            mIleopardAd.onDestroy();
        }
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.unregistBitmapController(mContext);
        }
    }

    public void unRegistContext(Context aContext){
        if(mContext==aContext){
            mContext = null;
        }
    }

    private int mContentTextSize;
    private int mTitleTextSize;
    private int mRefTitleTextSize;
    private int mRefCategoryTextSize;
    public void changeTextSize(String aTextSize) {
        if(Utility.DEBUG)Log.v(TAG, "changeTextSize aTextSize: " + aTextSize);
        initTextSize(aTextSize);
        notifyDataSetChanged();
    }

    public void initTextSize(String aTextSize){
        mContentTextSize = Integer.valueOf(mContext.getString(R.string.small_text_size));
        mTitleTextSize = Integer.valueOf(mContext.getString(R.string.title_small_text_size));
        mRefTitleTextSize = Integer.valueOf(mContext.getString(R.string.ref_title_small_text_size));
        mRefCategoryTextSize = Integer.valueOf(mContext.getString(R.string.ref_category_small_text_size));
        if (aTextSize != null) {
            if (aTextSize.equals(mContext.getString(R.string.max))) {
                mContentTextSize = Integer.valueOf(mContext.getString(R.string.max_text_size));
                mTitleTextSize = Integer.valueOf(mContext.getString(R.string.title_max_text_size));
                mRefTitleTextSize = Integer.valueOf(mContext.getString(R.string.ref_title_max_text_size));
                mRefCategoryTextSize = Integer.valueOf(mContext.getString(R.string.ref_category_max_text_size));
            } else if (aTextSize.equals(mContext.getString(R.string.mid))) {
                mContentTextSize = Integer.valueOf(mContext.getString(R.string.mid_text_size));
                mTitleTextSize = Integer.valueOf(mContext.getString(R.string.title_mid_text_size));
                mRefTitleTextSize = Integer.valueOf(mContext.getString(R.string.ref_title_mid_text_size));
                mRefCategoryTextSize = Integer.valueOf(mContext.getString(R.string.ref_category_mid_text_size));
            } else if (aTextSize.equals(mContext.getString(R.string.small))) {
                mContentTextSize = Integer.valueOf(mContext.getString(R.string.small_text_size));
                mTitleTextSize = Integer.valueOf(mContext.getString(R.string.title_small_text_size));
                mRefTitleTextSize = Integer.valueOf(mContext.getString(R.string.ref_title_small_text_size));
                mRefCategoryTextSize = Integer.valueOf(mContext.getString(R.string.ref_category_small_text_size));
            }
        }

        if(Utility.DEBUG)Log.v(TAG, "mContentTextSize: " + mContentTextSize);
        if(Utility.DEBUG)Log.v(TAG, "mTitleTextSize: " + mTitleTextSize);
        if(Utility.DEBUG)Log.v(TAG, "mRefTitleTextSize: " + mRefTitleTextSize);
        if(Utility.DEBUG)Log.v(TAG, "mRefCategoryTextSize: " + mRefCategoryTextSize);
    }

    private OnVideoPlayButtonClick mListener;
    public interface OnVideoPlayButtonClick{
        public void onVideoPlayButtonClick(final String aYoutubeId);
    }

}

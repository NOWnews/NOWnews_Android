package com.nownews.mobile.VideoNewsCategory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ad2iction.nativeads.Ad2ictionAdLocalEventHandler;
import com.ad2iction.nativeads.Ad2ictionNative;
import com.ad2iction.nativeads.NativeErrorCode;
import com.ad2iction.nativeads.NativeResponse;
import com.ad2iction.nativeads.RequestParameters;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.nownews.R;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.VideosListJson;
import com.nownews.mobile.Json.VideosListJson.VideosContent;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.VideoNewsPage.VideoNewsPage;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnNativeAd;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import static com.nownews.mobile.Common.Utility.isVponTestMode;

/**
 * Created by cindy on 2016/11/28.
 */

public class VideoNewsListRecyclerViewAdapter extends RecyclerView.Adapter {


    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<VideosListJson.VideosContent> mVideoNewsList;
    private String mCategoryName;
    private FragmentManager mChidFragmentManger;
    private BitmapController mBitmapController;
    private LayoutInflater inflater;
    private String[] mEcoDefaultImageList;
    private String[] mDFPIdList;
    private String[] mVPONIdList;

    public VideoNewsListRecyclerViewAdapter(Context aContext, List<VideosListJson.VideosContent> aNewsList, String aCategoryName, FragmentManager aChidFragmentManger) {
        mContext = aContext;
        mVideoNewsList = aNewsList;
        mCategoryName = aCategoryName;
        mChidFragmentManger = aChidFragmentManger;
        mBitmapController = BitmapController.getInstance(mContext);
//        mBitmapController = new BitmapController(mContext);
        init();
    }

    public void setData(List<VideosContent> aNewsList, FragmentManager aChidFragmentManger) {
        mChidFragmentManger = aChidFragmentManger;
        mVideoNewsList = aNewsList;
        notifyDataSetChanged();
    }

    private void init() {
        inflater = LayoutInflater.from(mContext);
        mDFPIdList = mContext.getResources().getStringArray(R.array.dfp_key_list);
        mVPONIdList = mContext.getResources().getStringArray(R.array.vpon_key_list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_AD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_news_list_ad_item, parent, false);
                viewHolder = new ADViewHolder(view);
                break;
            case TYPE_NORMAL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_video_news_list_item, parent, false);
                viewHolder = new NormalViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mVideoNewsList == null) {
            count = 0;
        } else {
            count = mVideoNewsList.size();
            if (count>2 && count<5) { //3 & 4
                count = count + 1;
            } else if (count>=5 && count<=22) {
                count = count+(count-5)/3+2;
            } else if (count>22) {
                count = count + 8;
            }
        }
        return count;
    }

    private final int TYPE_AD = 0;
    private final int TYPE_NORMAL = 1;
    @Override
    public int getItemViewType(int position) {
        if (position == 2 || (position>=5 && position<=29 && (position-5)%4==0)) {
            return TYPE_AD;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (Utility.DEBUG) Log.e(TAG, "position: " + position);

        if (position == 2 || (position>=5 && position<=29 && (position-5)%4==0)) {

            ((ADViewHolder)holder).vNewsItem.setVisibility(View.VISIBLE);
            ((ADViewHolder)holder).vAdGroup.setVisibility(View.VISIBLE);
            ((ADViewHolder)holder).vNewsImage.setImageDrawable(null);
            ((ADViewHolder)holder).vNewsTitle.setText("");
            ((ADViewHolder)holder).vNewsCategory.setText("");
            ((ADViewHolder)holder).vCallToAction.setText("");
            ((ADViewHolder)holder).vNewsTitle.setTextColor(Color.WHITE);
            ((ADViewHolder)holder).vNewsCategory.setTextColor(Color.WHITE);
            ((ADViewHolder)holder).vRootView.setBackgroundColor(Color.BLACK);

            if(position == 2){

                ((ADViewHolder)holder).vAdGroup.setVisibility(View.GONE);
                processAD2(position, ((ADViewHolder)holder).vNewsCategory, ((ADViewHolder)holder).vNewsTitle,
                        ((ADViewHolder)holder).vNewsImage, ((ADViewHolder)holder).vNewsItem, ((ADViewHolder)holder).vCallToAction,
                        ((ADViewHolder)holder).itemView);
                return;
            }

            int positionInAdList = (position-5)/4;
            processVPON(positionInAdList, position,
                    ((ADViewHolder)holder).vNewsCategory, ((ADViewHolder)holder).vNewsTitle,
                    ((ADViewHolder)holder).vNewsImage, ((ADViewHolder)holder).vNewsItem, ((ADViewHolder)holder).vCallToAction,
                    ((ADViewHolder)holder).itemView);
            processDFP(((ADViewHolder)holder).vAdGroup, positionInAdList, position,
                    ((ADViewHolder)holder).itemView);

        } else {

            int realPosition = position;
            if (position>2 && position<5) { //3 & 4
                realPosition = position - 1;
            } else if (position>5 && position<29) {
                int differ = (position-5)/4+2;
                realPosition = position-differ;
            } else if (position>=30) {
                realPosition = position - 8;
            }

            if (Utility.DEBUG) Log.e(TAG, "position: " + position);
            if (Utility.DEBUG) Log.e(TAG, "realPosition: " + realPosition);

            ((NormalViewHolder)holder).vAdGroup.setVisibility(View.GONE);
            ((NormalViewHolder)holder).vNewsItem.setVisibility(View.VISIBLE);
            ((NormalViewHolder)holder).vNewsImage.setImageDrawable(null);
            ((NormalViewHolder)holder).vNewsTitle.setText("");
            ((NormalViewHolder)holder).vNewsDate.setText("");

            //Image
            ((NormalViewHolder)holder).vNewsImage.setImageDrawable(null);
            if (mVideoNewsList != null
                    && mVideoNewsList.get(realPosition) != null
                    && mVideoNewsList.get(realPosition).youtubeThumbnail != null
                    && !mVideoNewsList.get(realPosition).youtubeThumbnail.trim().isEmpty()) {
                String imageUrl = mVideoNewsList.get(realPosition).youtubeThumbnail;
                imageUrl = Utility.getSrcFromImgapi(imageUrl);
                if (imageUrl != null) {
                    mBitmapController.loadImageWithOriginalSize(imageUrl, ((NormalViewHolder)holder).vNewsImage, BitmapController.IMAGE_SRC, 0, 0, null);
                }
            }

            //Title
            String mTitle = null;
            if (mVideoNewsList != null
                    && mVideoNewsList.get(realPosition) != null
                    && mVideoNewsList.get(realPosition).title != null
                    && !mVideoNewsList.get(realPosition).title.trim().isEmpty()) {
                mTitle = mVideoNewsList.get(realPosition).title;
                if(mTitle!=null && mTitle.contains("▲")){
                    mTitle = mTitle.replaceAll("▲", "");
                }
                if(mTitle!=null && mTitle.contains("▼")){
                    mTitle = mTitle.replaceAll("▼", "");
                }
                ((NormalViewHolder)holder).vNewsTitle.setText(mTitle);
                if (Utility.DEBUG) Log.w(TAG, "mTitle: " + mTitle);
            }
            //Date
            if (mVideoNewsList != null
                    && mVideoNewsList.get(realPosition) != null
                    && mVideoNewsList.get(realPosition).createdAt != null
                    && !mVideoNewsList.get(realPosition).createdAt.trim().equals("")) {
                String date = mVideoNewsList.get(realPosition).createdAt;
                date = Utility.processDate(date);
                ((NormalViewHolder)holder).vNewsDate.setText(date);
            }
            setCardViewClickListener(((NormalViewHolder)holder).vNewsItem, realPosition, mTitle);

        }

    }

    private void setCardViewClickListener(RelativeLayout vBannerCardView, final int realPosition, final String mTitle) {
        vBannerCardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoNewsPage(realPosition, mTitle);
            }
        });
    }

    private void gotoNewsPage(int position, String title) {

        if (Utility.DEBUG) Log.i(TAG, "position: " + position);
        if (Utility.DEBUG) Log.i(TAG, "title: " + title);

        int newsId = mVideoNewsList.get(position).nodeId;
        Intent intent = new Intent();
        intent.setClass(mContext, VideoNewsPage.class);
        intent.putExtra(VideoNewsPage.KEY_NEWS_ID, newsId);
        intent.putExtra(VideoNewsPage.KEY_NEWS_INDEX, position);
        intent.putExtra(VideoNewsPage.KEY_NEWS_TYPE, VideoNewsPage.TYPE_NORMAL_NEWS);
        intent.putExtra(VideoNewsPage.KEY_NEWS_CATEGORY, mCategoryName);
        UserDataInfo.setVideoNewsList(mVideoNewsList);
        ((Activity)mContext).startActivityForResult(intent, NewHome.RESULT_CODE);

    }

    private final AdSize DFP_SIZE = AdSize.MEDIUM_RECTANGLE;
    private void processDFP(final RelativeLayout aAdCardView, final int aPositionInAdList, final int aPosition,
                            final View aItemView) {
        if (Utility.DEBUG) Log.e(TAG, "DFP");

        final PublisherAdView vDfpAdView = new PublisherAdView(mContext);
        vDfpAdView.setAdUnitId(mDFPIdList[aPositionInAdList]);
        vDfpAdView.setAdSizes(DFP_SIZE);

        AdListener DFPAdListener = new AdListener() {

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
                if (Utility.DEBUG) Log.e(TAG, "onBannerLoaded");
                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);
                aAdCardView.removeAllViews();
                aAdCardView.addView(vDfpAdView);
                aAdCardView.setVisibility(View.VISIBLE);
                setItemVisibility(true, aItemView);
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                if (Utility.DEBUG) Log.e(TAG, "onAdOpened");
                super.onAdOpened();
            }

        };

        vDfpAdView.setAdListener(DFPAdListener);
        PublisherAdRequest.Builder requestBuild = new PublisherAdRequest.Builder();
//        requestBuild.addTestDevice("3F296C0E07DBC64F3ED95E7FA105ED8C");
//        requestBuild.addTestDevice("47D3A2E05A8397836B567EB9AA537A0E");
//    	requestBuild.addTestDevice("3736523E3897CDFD02EE6FE9DD448DD5");
        vDfpAdView.loadAd(requestBuild.build());
    }

    private void processVPON(int aPositionInAdList, final int aPosition, final TextView aCategory, final TextView aTitle,
                             final ImageView aImage, final RelativeLayout aNewsItem, final Button aCallToAction,
                             final View aItemView){

        final VpadnNativeAd nativeAd = new VpadnNativeAd((Activity)mContext, mVPONIdList[aPositionInAdList], "TW");
        nativeAd.setAdListener(new VpadnAdListener() {
            @Override
            public void onVpadnReceiveAd(VpadnAd vpadnAd) {

                if(mContext==null || nativeAd==null || nativeAd!=vpadnAd){
                    if (Utility.DEBUG) Log.e(TAG, "onVpadnReceiveAd NULL!!");
                    return;
                }

                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);

                nativeAd.unregisterView();
                aCategory.setText(mContext.getString(R.string.sponsored));
                aCategory.setTextColor(mContext.getResources().getColor(android.R.color.white));
                aTitle.setText(nativeAd.getAdTitle());
                aCallToAction.setText(nativeAd.getAdCallToAction());
                VpadnNativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
                if (Utility.DEBUG) Log.i(TAG, "adCoverImage.getWidth(): " + adCoverImage.getWidth());
                if (Utility.DEBUG) Log.i(TAG, "adCoverImage.getHeight(): " + adCoverImage.getHeight());
                int screenWidth = Utility.getScreenWidth(mContext);
                float scale = (float)screenWidth / (float)adCoverImage.getWidth();
                int newHeight = (int)(adCoverImage.getHeight() * scale);
                if (Utility.DEBUG) Log.i(TAG, "screenWidth: " + screenWidth);
                if (Utility.DEBUG) Log.i(TAG, "scale: " + scale);
                if (Utility.DEBUG) Log.i(TAG, "newHeight: " + newHeight);
                Glide.with(mContext)
                        .setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565)
                                .override(screenWidth, newHeight))
                        .load(adCoverImage.getUrl())
                        .into(aImage);
//                VpadnNativeAd.downloadAndDisplayImage(adCoverImage, aImage);
                nativeAd.registerViewForInteraction(aNewsItem);
                setItemVisibility(true, aItemView);

            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd vpadnAd, VpadnAdRequest.VpadnErrorCode vpadnErrorCode) {
                if (Utility.DEBUG) Log.e(TAG, "onVpadnFailedToReceiveAd!! vpadnErrorCode: " + vpadnErrorCode);
                if(aNewsItem!=null){
                    aNewsItem.setVisibility(View.GONE);
                }
            }

            @Override
            public void onVpadnPresentScreen(VpadnAd vpadnAd) { }

            @Override
            public void onVpadnDismissScreen(VpadnAd vpadnAd) { }

            @Override
            public void onVpadnLeaveApplication(VpadnAd vpadnAd) { }
        });

        if(isVponTestMode){
            VpadnAdRequest adRequest = new VpadnAdRequest();
            HashSet<String> testDevicesImeiSet = new HashSet<>();
            testDevicesImeiSet.add(Utility.getAdvertisingId());
            adRequest.setTestDevices(testDevicesImeiSet);
            nativeAd.loadAd(adRequest);
        }else{
            //正式
            nativeAd.loadAd();
        }

    }

    private String mAd2Native;
    private Ad2ictionNative vAD2Native;
    private NativeResponse mNativeResponse;
    private Ad2ictionAdLocalEventHandler mAd2ictionAdLocalEventHandler;
    private void processAD2(final int aPosition, final TextView aCategory, final TextView aTitle,
                            final ImageView aImage, final RelativeLayout aNewsItem, final Button aCallToAction,
                            final View aItemView) {

        if(vAD2Native!=null){
            vAD2Native.destroy();
            vAD2Native = null;
        }

        if(mAd2ictionAdLocalEventHandler!=null){
            mAd2ictionAdLocalEventHandler.recycle();
        }

        mAd2Native = mContext.getString(R.string.ad2_native);
        if (Utility.DEBUG) Log.e(TAG, "===mAd2Native: " + mAd2Native);

        Ad2ictionNative.Ad2ictionNativeListener listener = new Ad2ictionNative.Ad2ictionNativeListener() {
            @Override
            public void onNativeImpression(View view) { }

            @Override
            public void onNativeClick(View view) { }

            @Override
            public void onNativeLoad(NativeResponse nativeResponse) {

                mNativeResponse = nativeResponse;
                mAd2ictionAdLocalEventHandler = new Ad2ictionAdLocalEventHandler(mContext);
                mAd2ictionAdLocalEventHandler.handleEvent(aItemView, mNativeResponse);

                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);
                if (Utility.DEBUG) Log.e(TAG, "onNativeLoad!!!!!!");
                if (Utility.DEBUG) Log.e(TAG, "mNativeResponse is null or not??" + mNativeResponse.toString());

                String text = mNativeResponse.getText();
                final String imageUrl = mNativeResponse.getMainImageUrl();
                String callToActionText = mNativeResponse.getCallToAction();
                String link = mNativeResponse.getClickDestinationUrl();
                if (Utility.DEBUG) Log.v(TAG, "text: " + text);
                if (Utility.DEBUG) Log.v(TAG, "imageUrl: " + imageUrl);
                if (Utility.DEBUG) Log.v(TAG, "link: " + link);

                aCategory.setText(mContext.getString(R.string.sponsored));
                aCategory.setTextColor(mContext.getResources().getColor(android.R.color.black));
                aTitle.setText(text);
                aCallToAction.setText(callToActionText);
                Glide.with(mContext)
                        .setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565)
                                .skipMemoryCache(true)
                                .error(R.drawable.default_img))
                        .asBitmap()
                        .load(imageUrl)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                if (Utility.DEBUG) Log.v(TAG, "onResourceReady");
                                if (Utility.DEBUG) Log.d(TAG, "resource.getWidth(): " + resource.getWidth());
                                if (Utility.DEBUG) Log.d(TAG, "resource.getHeight(): " + resource.getHeight());

//                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                                params.setMargins(0, 15, 0, 0);
//                                aImage.setLayoutParams(params);

                                int screenWidth = Utility.getScreenWidth(mContext);
                                float scale = (float)screenWidth / (float)resource.getWidth();
                                int newHeight = (int)(resource.getHeight() * scale);
                                if (Utility.DEBUG) Log.i(TAG, "screenWidth: " + screenWidth);
                                if (Utility.DEBUG) Log.i(TAG, "scale: " + scale);
                                if (Utility.DEBUG) Log.i(TAG, "newHeight: " + newHeight);
                                Glide.with(mContext)
                                        .setDefaultRequestOptions(new RequestOptions().skipMemoryCache(true)
                                                .error(R.drawable.default_img)
                                                .override(screenWidth, newHeight))
                                        .load(imageUrl)
                                        .into(aImage);
                                setItemVisibility(true, aItemView);

                            }
                        });
            }

            @Override
            public void onNativeFail(NativeErrorCode nativeErrorCode) { }
        };

        vAD2Native = new Ad2ictionNative((Activity) mContext, mAd2Native, "native", listener);
        Location currentLocation = Utility.getLocation(mContext);
        if(currentLocation!=null){
            currentLocation.setAccuracy(100);
        }
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT,
                RequestParameters.NativeAdAsset.MAIN_IMAGE);

        RequestParameters requestParameters = new RequestParameters.Builder()
                .location(currentLocation)
                .desiredAssets(assetsSet)
                .build();

        vAD2Native.makeRequest(requestParameters);

//        aItemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Log.d(TAG, "aItemView 111");
//                if(mNativeResponse!=null){
//                    Log.d(TAG, "aItemView 222");
//                    mAd2ictionAdLocalEventHandler.handleEvent(aItemView, mNativeResponse);
//                }
//
//            }
//        });

//        Ad2ictionNative.Ad2ictionNativeNetworkListener ad2ictionNativeNetworkListener = new Ad2ictionNative.Ad2ictionNativeNetworkListener() {
//            @Override
//            public void onNativeLoad(NativeResponse nativeResponse) {
//
//                mNativeResponse = nativeResponse;
//
//                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);
//                if (Utility.DEBUG) Log.e(TAG, "onNativeLoad!!!!!!");
//                if (Utility.DEBUG) Log.e(TAG, "mNativeResponse is null or not??" + mNativeResponse.toString());
//
//                String text = mNativeResponse.getText();
//                final String imageUrl = mNativeResponse.getMainImageUrl();
//                String callToActionText = mNativeResponse.getCallToAction();
//                String link = mNativeResponse.getClickDestinationUrl();
//                if (Utility.DEBUG) Log.v(TAG, "text: " + text);
//                if (Utility.DEBUG) Log.v(TAG, "imageUrl: " + imageUrl);
//                if (Utility.DEBUG) Log.v(TAG, "link: " + link);
//
//                aCategory.setText(mContext.getString(R.string.sponsored));
//                aCategory.setTextColor(mContext.getResources().getColor(android.R.color.black));
//                aTitle.setText(text);
//                aCallToAction.setText(callToActionText);
//                Glide.with(mContext)
//                        .load(imageUrl)
//                        .asBitmap()
//                        .skipMemoryCache(true)
//                        .error(R.drawable.default_img)
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                if (Utility.DEBUG) Log.v(TAG, "onResourceReady");
//                                if (Utility.DEBUG) Log.d(TAG, "resource.getWidth(): " + resource.getWidth());
//                                if (Utility.DEBUG) Log.d(TAG, "resource.getHeight(): " + resource.getHeight());
//
////                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
////                                params.setMargins(0, 15, 0, 0);
////                                aImage.setLayoutParams(params);
//
//                                int screenWidth = Utility.getScreenWidth(mContext);
//                                float scale = (float)screenWidth / (float)resource.getWidth();
//                                int newHeight = (int)(resource.getHeight() * scale);
//                                if (Utility.DEBUG) Log.i(TAG, "screenWidth: " + screenWidth);
//                                if (Utility.DEBUG) Log.i(TAG, "scale: " + scale);
//                                if (Utility.DEBUG) Log.i(TAG, "newHeight: " + newHeight);
//                                Glide.with(mContext).load(imageUrl).override(screenWidth, newHeight).into(aImage);
//                                setItemVisibility(true, aItemView);
//
//                            }
//
//                            @Override
//                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                                aImage.setImageResource(R.drawable.default_img);
//                                setItemVisibility(true, aItemView);
//                                super.onLoadFailed(e, errorDrawable);
//                            }
//                        });
//            }
//
//            @Override
//            public void onNativeFail(NativeErrorCode nativeErrorCode) {
//                setItemVisibility(false, aItemView);
//            }
//        };
//
//        Ad2ictionNative.Ad2ictionNativeEventListener ad2ictionNativeEventListener = new Ad2ictionNative.Ad2ictionNativeEventListener() {
//            @Override
//            public void onNativeImpression(View view) {
//
//            }
//
//            @Override
//            public void onNativeClick(View view) {
//                Log.d(TAG, "vAD2Native click!!");
//            }
//        };
//
//        aItemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if(mNativeResponse!=null){
////                    mNativeResponse.prepare(aItemView);
//                    mNativeResponse.handleClick(aItemView);
//                }
//
//            }
//        });
//
//        RequestParameters requestParameters = new RequestParameters.Builder().build();
//
//        vAD2Native = new Ad2ictionNative((Activity)mContext, mAd2Native, "native", ad2ictionNativeNetworkListener);
//        vAD2Native.setNativeEventListener(ad2ictionNativeEventListener);
//        vAD2Native.makeRequest(requestParameters);

    }

    private void setItemVisibility(boolean isVisible, View itemView){
        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)itemView.getLayoutParams();
        if (isVisible){
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            itemView.setVisibility(View.VISIBLE);
        }else{
            itemView.setVisibility(View.GONE);
            param.height = 0;
            param.width = 0;
        }
        itemView.setLayoutParams(param);
    }

    public void clearBitmapController() {
        if (mBitmapController != null) {
            mBitmapController.clearCache();
        }
    }

    public void unRegistContext(Context aContext){
        if(mContext==aContext){
            mContext = null;
        }
    }

    class NormalViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout vAdGroup;

        private RelativeLayout vNewsItem;
        private ImageView vNewsImage;
        private TextView vNewsTitle;
        private TextView vNewsDate;

        public NormalViewHolder(View itemView) {
            super(itemView);

            vAdGroup = (RelativeLayout) itemView.findViewById(R.id.ad_view);

            vNewsItem = (RelativeLayout) itemView.findViewById(R.id.news_item);
            vNewsImage = (ImageView) itemView.findViewById(R.id.news_img);
            vNewsTitle = (TextView) itemView.findViewById(R.id.news_title);
            vNewsDate = (TextView) itemView.findViewById(R.id.news_date);

        }

    }

    class ADViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout vAdGroup;
        private RelativeLayout vRootView;
        private RelativeLayout vNewsItem;
        private ImageView vNewsImage;
        private TextView vNewsCategory;
        private TextView vNewsTitle;
        private Button vCallToAction;

        public ADViewHolder(View itemView) {
            super(itemView);

            vAdGroup = (RelativeLayout) itemView.findViewById(R.id.ad_view);
            vRootView = (RelativeLayout) itemView.findViewById(R.id.root_view);
            vNewsItem = (RelativeLayout) itemView.findViewById(R.id.news_item);
            vNewsImage = (ImageView) itemView.findViewById(R.id.news_img);
            vNewsCategory = (TextView) itemView.findViewById(R.id.news_category);
            vNewsTitle = (TextView) itemView.findViewById(R.id.news_title);
            vCallToAction = (Button) itemView.findViewById(R.id.call_to_action);

        }

    }

    public void destroyAD2(){
        if(vAD2Native!=null){
            vAD2Native.destroy();
            vAD2Native = null;
        }
    }

}

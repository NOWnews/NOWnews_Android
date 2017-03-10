package com.nownews.mobile.NewsCategory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ad2iction.nativeads.Ad2ictionNative;
import com.ad2iction.nativeads.AdapterHelper;
import com.ad2iction.nativeads.NativeErrorCode;
import com.ad2iction.nativeads.NativeResponse;
import com.ad2iction.nativeads.RequestParameters;
import com.ad2iction.nativeads.ViewBinder;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.nownews.R;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.NewsListJson;
import com.nownews.mobile.NewHome;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Widget.CustomImageTopcrop;
import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnNativeAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.nownews.mobile.Common.Utility.isVponTestMode;

/**
 * Created by cindy on 2016/11/28.
 */

public class NewsListRecyclerViewAdapter extends RecyclerView.Adapter {


    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<NewsListJson.NewsContent> mNewsList;
    private String mCategoryName;
    private String mBigCategory;
    private FragmentManager mChidFragmentManger;
    private BitmapController mBitmapController;
    private LayoutInflater inflater;
    private String[] mEcoDefaultImageList;
    private String[] mDFPIdList;
    private String[] mVPONIdList;

    public NewsListRecyclerViewAdapter(Context aContext, List<NewsListJson.NewsContent> aNewsList,
                                       String aCategoryName, FragmentManager aChidFragmentManger, String aBigCategory) {
        mContext = aContext;
        mNewsList = aNewsList;
        mCategoryName = aCategoryName;
        mBigCategory = aBigCategory;
        mChidFragmentManger = aChidFragmentManger;
        init();
    }

    public void setData(List<NewsListJson.NewsContent> aNewsList, String aCategoryName, FragmentManager aChidFragmentManger, String aBigCategory) {
        mNewsList = aNewsList;
        mCategoryName = aCategoryName;
        mBigCategory = aBigCategory;
        mChidFragmentManger = aChidFragmentManger;
        notifyDataSetChanged();
    }

    private void init() {
        mBitmapController = BitmapController.getInstance(mContext);
//        mBitmapController = new BitmapController(mContext);
        inflater = LayoutInflater.from(mContext);
        mEcoDefaultImageList = new String[]{
                "http://s.nownews.com/w/images/mobile/defaultimg-01.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-02.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-03.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-04.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-05.jpg"};
        mDFPIdList = mContext.getResources().getStringArray(R.array.dfp_key_list);
        mVPONIdList = mContext.getResources().getStringArray(R.array.vpon_key_list);
    }

    private int mCreateADViewCount;
    private int mCreateNormalViewCount;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_AD:
                mCreateADViewCount++;
                Log.w(TAG, "ADView被調用了: " + mCreateADViewCount);
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_news_list_ad_item, parent, false);
                viewHolder = new ADViewHolder(view);
                break;
            case TYPE_NORMAL:
                mCreateNormalViewCount++;
                Log.w(TAG, "NormalView被調用了: " + mCreateNormalViewCount);
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_news_list_item, parent, false);
                viewHolder = new NormalViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mNewsList == null) {
            count = 0;
        } else {
            count = mNewsList.size();
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
        if (position>=5 && position<=29 && (position-5)%4==0) {
            return TYPE_AD;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (Utility.DEBUG) Log.e(TAG, "position: " + position);

        if (position == 2) {
            //廣告

            ((NormalViewHolder)holder).vNewsItem.setVisibility(View.GONE);
            ((NormalViewHolder)holder).vAdGroup.setVisibility(View.VISIBLE);

            processAD2(((NormalViewHolder)holder).vAdGroup, position);

        } else if (position>=5 && position<=29 && (position-5)%4==0) {

            ((ADViewHolder)holder).vNewsItem.setVisibility(View.VISIBLE);
            ((ADViewHolder)holder).vAdGroup.setVisibility(View.VISIBLE);
            ((ADViewHolder)holder).vNewsImage.setImageDrawable(null);
            ((ADViewHolder)holder).vNewsTitle.setText("");
            ((ADViewHolder)holder).vNewsCategory.setText("");

            int positionInAdList = (position-5)/4;
            processVPON(positionInAdList, position,
                    ((ADViewHolder)holder).vNewsCategory, ((ADViewHolder)holder).vNewsTitle,
                    ((ADViewHolder)holder).vNewsImage, ((ADViewHolder)holder).vNewsItem, ((ADViewHolder)holder).vCallToAction);
            processDFP(((ADViewHolder)holder).vAdGroup, positionInAdList, position);

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
            ((NormalViewHolder)holder).vNewsTitle.setText("");
            ((NormalViewHolder)holder).vNewsCategory.setText("");
            ((NormalViewHolder)holder).vNewsDate.setText("");

            //Image
            ((NormalViewHolder)holder).vNewsImage.setImageDrawable(null);
            if (mNewsList != null
                    && mNewsList.get(realPosition) != null
                    && mNewsList.get(realPosition).image != null
                    && mNewsList.get(realPosition).image.url != null
                    && !mNewsList.get(realPosition).image.url.trim().isEmpty()) {
                String imageUrl = mNewsList.get(realPosition).image.url;
                if (mCategoryName.equals(mContext.getString(R.string.eco))
                        && imageUrl.contains("defaultimg.gif")) {
                    int newsId = mNewsList.get(realPosition)._id;
                    int digit = newsId % 10;
                    int imagePosition = digit % 5;
                    imageUrl = mEcoDefaultImageList[imagePosition];
                }
                if (imageUrl != null) {
                    mBitmapController.loadImageWithOriginalSize(imageUrl, ((NormalViewHolder)holder).vNewsImage, BitmapController.IMAGE_SRC_FROM_NEWS_LIST, 0, 0, null);
                }
            }

            //Category
            String mCategory = null;
            if (mNewsList != null
                    && mNewsList.get(realPosition) != null
                    && mNewsList.get(realPosition).category != null
                    && mNewsList.get(realPosition).category.name != null
                    && !mNewsList.get(realPosition).category.name.trim().isEmpty()) {
                ((NormalViewHolder)holder).vNewsCategory.setVisibility(View.VISIBLE);
                String category = mNewsList.get(realPosition).category.name;
                ((NormalViewHolder)holder).vNewsCategory.setText(category);
                Utility.setCategoryTextColor(category, ((NormalViewHolder)holder).vNewsCategory, Utility.ColorType.News);
            } else {
                ((NormalViewHolder)holder).vNewsCategory.setVisibility(View.GONE);
            }

            //Title
            String mTitle = null;
            if (mNewsList != null
                    && mNewsList.get(realPosition) != null
                    && mNewsList.get(realPosition).field_short_title != null
                    && mNewsList.get(realPosition).field_short_title.value != null
                    && !mNewsList.get(realPosition).field_short_title.value.trim().isEmpty()) {
                mTitle = mNewsList.get(realPosition).field_short_title.value;
                ((NormalViewHolder)holder).vNewsTitle.setText(mTitle);
                if (Utility.DEBUG) Log.w(TAG, "mTitle: " + mTitle);
            }
            //Date
            if (mNewsList != null
                    && mNewsList.get(realPosition) != null
                    && mNewsList.get(realPosition).createdAt != null
                    && !mNewsList.get(realPosition).createdAt.trim().equals("")) {
                String date = mNewsList.get(realPosition).createdAt;
                date = Utility.processDate(date);
                ((NormalViewHolder)holder).vNewsDate.setVisibility(View.VISIBLE);
                ((NormalViewHolder)holder).vNewsDate.setText(date);
            }else{
                ((NormalViewHolder)holder).vNewsDate.setVisibility(View.GONE);
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

        int newsId = mNewsList.get(position)._id;
        String shortTitle = mNewsList.get(position).field_short_title.value;
        Intent intent = new Intent();
        intent.setClass(mContext, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_ID, newsId);
        intent.putExtra(NewsPage.KEY_NEWS_INDEX, position);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_NORMAL_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, mCategoryName);
        intent.putExtra(NewsPage.KEY_NEWS_BIG_CATEGORY, mBigCategory);
        UserDataInfo.setNewsList(mNewsList);
        UserDataInfo.isSingalNewsFromAction = false;
        ((Activity)mContext).startActivityForResult(intent, NewHome.RESULT_CODE);

    }

    private final AdSize DFP_SIZE = AdSize.MEDIUM_RECTANGLE;
    private void processDFP(final RelativeLayout aAdCardView, final int aPositionInAdList, final int aPosition) {
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
                if (Utility.DEBUG) Log.v(TAG, "mFirstVisibleItem: " + mFirstVisibleItem);
                if (Utility.DEBUG) Log.v(TAG, "mLastVisibleItem: " + mLastVisibleItem);
//                if (aPosition >= mFirstVisibleItem && aPosition <= mLastVisibleItem) {
                    aAdCardView.removeAllViews();
                    aAdCardView.addView(vDfpAdView);
                    aAdCardView.setVisibility(View.VISIBLE);
//                }
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
                             final ImageView aImage, final RelativeLayout aNewsItem, final Button aCallToAction){

        final VpadnNativeAd nativeAd = new VpadnNativeAd((Activity)mContext, mVPONIdList[aPositionInAdList], "TW");
        nativeAd.setAdListener(new VpadnAdListener() {
            @Override
            public void onVpadnReceiveAd(VpadnAd vpadnAd) {

                if(mContext==null || nativeAd==null || nativeAd!=vpadnAd){
                    Log.e(TAG, "onVpadnReceiveAd NULL!!");
                    return;
                }

                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);
                if (Utility.DEBUG) Log.v(TAG, "mFirstVisibleItem: " + mFirstVisibleItem);
                if (Utility.DEBUG) Log.v(TAG, "mLastVisibleItem: " + mLastVisibleItem);

                nativeAd.unregisterView();
                aCategory.setText(mContext.getString(R.string.sponsored));
                aCategory.setTextColor(mContext.getResources().getColor(android.R.color.black));
                aTitle.setText(nativeAd.getAdTitle());
                aCallToAction.setText(nativeAd.getAdCallToAction());
                VpadnNativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
                Log.i(TAG, "adCoverImage.getWidth(): " + adCoverImage.getWidth());
                Log.i(TAG, "adCoverImage.getHeight(): " + adCoverImage.getHeight());
                int screenWidth = Utility.getScreenWidth(mContext);
                float scale = (float)screenWidth / (float)adCoverImage.getWidth();
                int newHeight = (int)(adCoverImage.getHeight() * scale);
                Log.i(TAG, "screenWidth: " + screenWidth);
                Log.i(TAG, "scale: " + scale);
                Log.i(TAG, "newHeight: " + newHeight);
                Glide.with(mContext).load(adCoverImage.getUrl()).override(screenWidth, newHeight).into(aImage);
//                VpadnNativeAd.downloadAndDisplayImage(adCoverImage, aImage);
                nativeAd.registerViewForInteraction(aNewsItem);

            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd vpadnAd, VpadnAdRequest.VpadnErrorCode vpadnErrorCode) {
                Log.e(TAG, "onVpadnFailedToReceiveAd!! vpadnErrorCode: " + vpadnErrorCode);
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
    private void processAD2(final RelativeLayout aAdView, final int aPosition) {

        if(vAD2Native!=null){
            vAD2Native.destroy();
            vAD2Native = null;
        }

        mAd2Native = mContext.getString(R.string.ad2_native);
        if (Utility.DEBUG) Log.e(TAG, "===mAd2Native: " + mAd2Native);

        aAdView.removeAllViews();
        final AdapterHelper helper = new AdapterHelper(mContext, 2, 10);

        final ViewBinder viewBinder = new ViewBinder.Builder(R.layout.widget_native_layout)
                .mainImageId(R.id.native_main_image)
                .textId(R.id.native_text)
                .callToActionId(R.id.native_cta)
                .build();

        Ad2ictionNative.Ad2ictionNativeListener listener = new Ad2ictionNative.Ad2ictionNativeListener() {

            @Override
            public void onNativeImpression(View arg0) {
            }

            @Override
            public void onNativeClick(View arg0) {
            }

            @Override
            public void onNativeLoad(NativeResponse arg0) {
                if (Utility.DEBUG) Log.v(TAG, "aPosition: " + aPosition);
                if (Utility.DEBUG) Log.v(TAG, "mFirstVisibleItem: " + mFirstVisibleItem);
                if (Utility.DEBUG) Log.v(TAG, "mLastVisibleItem: " + mLastVisibleItem);
//                if (aPosition >= mFirstVisibleItem && aPosition <= mLastVisibleItem) {
                    if (Utility.DEBUG) Log.e(TAG, "onNativeLoad!!!!!!");
                    if (Utility.DEBUG) Log.e(TAG, "arg0 is null or not??" + arg0.toString());
                    View mAD2View = null;
                    mAD2View = helper.getAdView(mAD2View, aAdView, arg0, viewBinder, null);
                    aAdView.addView(mAD2View);
                    aAdView.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onNativeFail(NativeErrorCode arg0) {
            }
        };

        RequestParameters requestParameters = new RequestParameters.Builder().build();

        vAD2Native = new Ad2ictionNative(mContext, mAd2Native, "native", listener);
        vAD2Native.makeRequest(requestParameters);

    }

    private int mFirstVisibleItem;
    private int mLastVisibleItem;
    public void setPosition(int firstVisibleItem, int visibleItemCount) {
        Log.i(TAG, "setPosition");
        mFirstVisibleItem = firstVisibleItem;
        mLastVisibleItem = visibleItemCount;
//        notifyDataSetChanged();
    }

    public void clearBitmapController() {
        if (mBitmapController != null) {
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
        }
    }

    public void destroyAD2(){
        if(vAD2Native!=null){
            vAD2Native.destroy();
            vAD2Native = null;
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
        private CustomImageTopcrop vNewsImage;
        private TextView vNewsCategory;
        private TextView vNewsTitle;
        private TextView vNewsDate;

        public NormalViewHolder(View itemView) {
            super(itemView);

            vAdGroup = (RelativeLayout) itemView.findViewById(R.id.ad_view);

            vNewsItem = (RelativeLayout) itemView.findViewById(R.id.news_item);
            vNewsImage = (CustomImageTopcrop) itemView.findViewById(R.id.news_img);
            vNewsCategory = (TextView) itemView.findViewById(R.id.news_category);
            vNewsTitle = (TextView) itemView.findViewById(R.id.news_title);
            vNewsDate = (TextView) itemView.findViewById(R.id.news_date);

        }

    }

    class ADViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout vAdGroup;

        private RelativeLayout vNewsItem;
        private ImageView vNewsImage;
        private TextView vNewsCategory;
        private TextView vNewsTitle;
        private Button vCallToAction;

        public ADViewHolder(View itemView) {
            super(itemView);

            vAdGroup = (RelativeLayout) itemView.findViewById(R.id.ad_view);

            vNewsItem = (RelativeLayout) itemView.findViewById(R.id.news_item);
            vNewsImage = (ImageView) itemView.findViewById(R.id.news_img);
            vNewsCategory = (TextView) itemView.findViewById(R.id.news_category);
            vNewsTitle = (TextView) itemView.findViewById(R.id.news_title);
            vCallToAction = (Button) itemView.findViewById(R.id.call_to_action);

        }

    }


}

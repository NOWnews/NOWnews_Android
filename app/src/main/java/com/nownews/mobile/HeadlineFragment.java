package com.nownews.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.NewsListJson.NewsContent;
import com.nownews.mobile.Widget.CustomImageTopcrop;

import java.lang.reflect.Field;
import java.util.List;

public class HeadlineFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private CustomImageTopcrop vImage;
    private TextView vTitle;
    private RelativeLayout vGroup;
    private TextView vCount;

    private BitmapController mBitmapController;
    private List<NewsContent> mHeadlineContent;
    private int mCurrentPosition;
    private OnClickListener mImageClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsPage.class);
            if (mHeadlineContent != null
                    && mHeadlineContent.get(mCurrentPosition) != null
                    && mHeadlineContent.get(mCurrentPosition)._id != -1
                    && mHeadlineContent.get(mCurrentPosition).field_short_title != null
                    && mHeadlineContent.get(mCurrentPosition).field_short_title.value != null
                    && !mHeadlineContent.get(mCurrentPosition).field_short_title.value.trim().isEmpty()) {
                int newsId = mHeadlineContent.get(mCurrentPosition)._id;
                String shortTitle = mHeadlineContent.get(mCurrentPosition).field_short_title.value;
                intent.putExtra(NewsPage.KEY_NEWS_ID, newsId);
                String eventAction1 = "點擊頭條新聞";
                String eventAction2 = WebAPIUrl.NOWNEWS_MOBIEL_WEB_NEWS_DOMAIN + newsId + " " + shortTitle;
                GoogleAnalyticsFunction.sendHitInfo(getActivity(), "首頁", eventAction1, "");
                GoogleAnalyticsFunction.sendHitInfo(getActivity(), "頭條新聞", eventAction2, "");
            }
            intent.putExtra(NewsPage.KEY_NEWS_INDEX, mCurrentPosition);
            intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_HEADLINE_NEWS);
            intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, "頭條新聞");
            UserDataInfo.isSingalNewsFromAction = false;
            getActivity().startActivityForResult(intent, NewHome.REQUEST_CODE);

        }
    };

    public HeadlineFragment() {
        // Do nothing...
    }

    public void setData(List<NewsContent> aBig3Small6Content, int position) {
        mHeadlineContent = aBig3Small6Content;
        mCurrentPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_big3small6, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Utility.DEBUG) Log.e(TAG, "onActivityCreated");

        initController();
//        processArgument();
        processView();
        processListener();
        setData();

    }

//    public final static String KEY_BIG3_SMALL6_CONTENT = "big3Small6Content";
//    private void processArgument(){
//        Bundle bundle = getArguments();
//        if(bundle!=null){
//            mHeadlineContent = bundle.getSparseParcelableArray(KEY_BIG3_SMALL6_CONTENT);
//        }
//    }

    private void initController() {
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
    }

    public void setData() {

        if (mHeadlineContent != null
                && mHeadlineContent.get(mCurrentPosition) != null
                && mHeadlineContent.get(mCurrentPosition).image != null
                && mHeadlineContent.get(mCurrentPosition).image.originImage != null
                && !mHeadlineContent.get(mCurrentPosition).image.originImage.trim().isEmpty()) {
            String imgUrl = mHeadlineContent.get(mCurrentPosition).image.originImage;
            int screenWidth = Utility.getScreenWidth(getActivity());
            imgUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, imgUrl);
            mBitmapController.clearSignalCache(imgUrl);
            mBitmapController.loadImageWithOriginalSize(imgUrl, vImage, BitmapController.IMAGE_SRC,
                    0, 0, null);
        }

        if (mHeadlineContent != null
                && mHeadlineContent.get(mCurrentPosition) != null
                && mHeadlineContent.get(mCurrentPosition).field_short_title != null
                && mHeadlineContent.get(mCurrentPosition).field_short_title.value != null
                && !mHeadlineContent.get(mCurrentPosition).field_short_title.value.trim().isEmpty()) {
            String newsTitle = mHeadlineContent.get(mCurrentPosition).field_short_title.value;
            vTitle.setText(newsTitle);
        }

    }

    public void processView() {

        View view = getView();

        vImage = (CustomImageTopcrop) view.findViewById(R.id.big3_image);
        vTitle = (TextView) view.findViewById(R.id.big3_title);
        vGroup = (RelativeLayout) view.findViewById(R.id.big3);
        vCount = (TextView) view.findViewById(R.id.big3_count);
        if (mHeadlineContent != null && mHeadlineContent.size() > 0) {
            vCount.setText((mCurrentPosition + 1) + "/" + mHeadlineContent.size());
        } else {
            vCount.setVisibility(View.GONE);
        }

    }

    private void processListener() {
        vGroup.setOnClickListener(mImageClickListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(getActivity());
        }
    }
}

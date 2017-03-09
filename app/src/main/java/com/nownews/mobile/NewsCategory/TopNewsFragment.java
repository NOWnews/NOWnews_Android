package com.nownews.mobile.NewsCategory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.NewHome;
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

public class TopNewsFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private CustomImageTopcrop vImage;
    private TextView vTitle;
    private RelativeLayout vGroup;
    private ImageView vDot1;
    private ImageView vDot2;
    private ImageView vDot3;
    private ImageView vDot4;

    private BitmapController mBitmapController;
    private List<NewsContent> mNewsListContent;
    private int mCurrentPosition;
    private String mCategoryName;
    private OnClickListener mImageClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsPage.class);
            if (mNewsListContent != null
                    && mNewsListContent.get(mCurrentPosition) != null
                    && mNewsListContent.get(mCurrentPosition)._id != -1
                    && mNewsListContent.get(mCurrentPosition).field_short_title != null
                    && mNewsListContent.get(mCurrentPosition).field_short_title.value != null
                    && !mNewsListContent.get(mCurrentPosition).field_short_title.value.trim().isEmpty()) {
                int newsId = mNewsListContent.get(mCurrentPosition)._id;
                intent.putExtra(NewsPage.KEY_NEWS_ID, newsId);
                intent.putExtra(NewsPage.KEY_NEWS_INDEX, mCurrentPosition);
                intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_NORMAL_NEWS);
                intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, mCategoryName + "新聞");
                UserDataInfo.setNewsList(mNewsListContent);
                UserDataInfo.isSingalNewsFromAction = false;
                getActivity().startActivityForResult(intent, NewHome.REQUEST_CODE);
            }

        }
    };

    public TopNewsFragment() {
        // Do nothing...
    }

    public void setData(List<NewsContent> aNewsListContent, int position, String aCategoryName) {
        mNewsListContent = aNewsListContent;
        mCurrentPosition = position;
        mCategoryName = aCategoryName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.widget_top_news_list_item, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Utility.DEBUG) Log.e(TAG, "onActivityCreated");

        initController();
        processView();
        processListener();
        setData();

    }

    private void initController() {
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
    }

    public void setData() {

        if (mNewsListContent != null
                && mNewsListContent.get(mCurrentPosition) != null
                && mNewsListContent.get(mCurrentPosition).image != null
                && mNewsListContent.get(mCurrentPosition).image.originImage != null
                && !mNewsListContent.get(mCurrentPosition).image.originImage.trim().isEmpty()) {
            String imgUrl = mNewsListContent.get(mCurrentPosition).image.originImage;
            int screenWidth = Utility.getScreenWidth(getActivity());
            imgUrl = String.format(WebAPIUrl.SCALE_IMAGE, screenWidth, "", Utility.IMG_QUALITY, imgUrl);
            mBitmapController.clearSignalCache(imgUrl);
            mBitmapController.loadImageWithOriginalSize(imgUrl, vImage, BitmapController.IMAGE_SRC,
                    0, 0, null);
        }

        if (mNewsListContent != null
                && mNewsListContent.get(mCurrentPosition) != null
                && mNewsListContent.get(mCurrentPosition).field_short_title != null
                && mNewsListContent.get(mCurrentPosition).field_short_title.value != null
                && !mNewsListContent.get(mCurrentPosition).field_short_title.value.trim().isEmpty()) {
            String newsTitle = mNewsListContent.get(mCurrentPosition).field_short_title.value;
            vTitle.setText(newsTitle);
        }

        if (mNewsListContent != null) {
            if (mNewsListContent.size() < 4) {
                switch (mNewsListContent.size()) {
                    case 1:
                        vDot1.setVisibility(View.VISIBLE);
                        vDot2.setVisibility(View.GONE);
                        vDot3.setVisibility(View.GONE);
                        vDot4.setVisibility(View.GONE);
                        vDot1.setImageResource(R.drawable.top_news_dot);
                        break;
                    case 2:
                        vDot1.setVisibility(View.VISIBLE);
                        vDot2.setVisibility(View.VISIBLE);
                        vDot3.setVisibility(View.GONE);
                        vDot4.setVisibility(View.GONE);
                        if (mCurrentPosition == 0) {
                            vDot1.setImageResource(R.drawable.top_news_dot);
                            vDot2.setImageResource(R.drawable.top_news_dot_ring);
                        } else {
                            vDot1.setImageResource(R.drawable.top_news_dot_ring);
                            vDot2.setImageResource(R.drawable.top_news_dot);
                        }
                        break;
                    case 3:
                        vDot1.setVisibility(View.VISIBLE);
                        vDot2.setVisibility(View.VISIBLE);
                        vDot3.setVisibility(View.VISIBLE);
                        vDot4.setVisibility(View.GONE);
                        if (mCurrentPosition == 0) {
                            vDot1.setImageResource(R.drawable.top_news_dot);
                            vDot2.setImageResource(R.drawable.top_news_dot_ring);
                            vDot3.setImageResource(R.drawable.top_news_dot_ring);
                        } else if (mCurrentPosition == 1) {
                            vDot1.setImageResource(R.drawable.top_news_dot_ring);
                            vDot2.setImageResource(R.drawable.top_news_dot);
                            vDot3.setImageResource(R.drawable.top_news_dot_ring);
                        } else {
                            vDot1.setImageResource(R.drawable.top_news_dot_ring);
                            vDot2.setImageResource(R.drawable.top_news_dot_ring);
                            vDot3.setImageResource(R.drawable.top_news_dot);
                        }
                        break;
                }
            } else {
                vDot1.setVisibility(View.VISIBLE);
                vDot2.setVisibility(View.VISIBLE);
                vDot3.setVisibility(View.VISIBLE);
                vDot4.setVisibility(View.VISIBLE);
                if (mCurrentPosition == 0) {
                    vDot1.setImageResource(R.drawable.top_news_dot);
                    vDot2.setImageResource(R.drawable.top_news_dot_ring);
                    vDot3.setImageResource(R.drawable.top_news_dot_ring);
                    vDot4.setImageResource(R.drawable.top_news_dot_ring);
                } else if (mCurrentPosition == 1) {
                    vDot1.setImageResource(R.drawable.top_news_dot_ring);
                    vDot2.setImageResource(R.drawable.top_news_dot);
                    vDot3.setImageResource(R.drawable.top_news_dot_ring);
                    vDot4.setImageResource(R.drawable.top_news_dot_ring);
                } else if (mCurrentPosition == 2) {
                    vDot1.setImageResource(R.drawable.top_news_dot_ring);
                    vDot2.setImageResource(R.drawable.top_news_dot_ring);
                    vDot3.setImageResource(R.drawable.top_news_dot);
                    vDot4.setImageResource(R.drawable.top_news_dot_ring);
                } else {
                    vDot1.setImageResource(R.drawable.top_news_dot_ring);
                    vDot2.setImageResource(R.drawable.top_news_dot_ring);
                    vDot3.setImageResource(R.drawable.top_news_dot_ring);
                    vDot4.setImageResource(R.drawable.top_news_dot);
                }
            }
        }

    }

    public void processView() {

        View view = getView();

        vImage = (CustomImageTopcrop) view.findViewById(R.id.image);
        vTitle = (TextView) view.findViewById(R.id.news_title);
        vGroup = (RelativeLayout) view.findViewById(R.id.group);
        vDot1 = (ImageView) view.findViewById(R.id.dot1);
        vDot2 = (ImageView) view.findViewById(R.id.dot2);
        vDot3 = (ImageView) view.findViewById(R.id.dot3);
        vDot4 = (ImageView) view.findViewById(R.id.dot4);

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

}

package com.nownews.mobile.Search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ad2iction.mobileads.Ad2ictionView;
import com.nownews.R;
import com.nownews.mobile.NewsPage.NewsPage;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.SearchInfoJson.SearchInfoContent;

import java.util.List;

public class SearchPageAdapter extends RecyclerView.Adapter<SearchPageAdapter.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<SearchInfoContent> mNewsList;
    private Ad2ictionView vAD2View;
    private BitmapController mBitmapController;

    public SearchPageAdapter(Context aContext, List<SearchInfoContent> aNewsList) {
        mContext = aContext;
        mNewsList = aNewsList;
        mBitmapController = BitmapController.getInstance(mContext);
//        mBitmapController = new BitmapController(mContext);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //Image
        holder.vNewsImage.setImageDrawable(null);
        if (mNewsList.get(position) != null && mNewsList.get(position).image != null) {
            String imageUrl = mNewsList.get(position).image.originImage;
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                mBitmapController.loadImageWithOriginalSize(imageUrl, holder.vNewsImage, BitmapController.IMAGE_SRC, 0, 0, null);
            }
        }

        //Title
        if (mNewsList != null
                && mNewsList.get(position) != null
                && mNewsList.get(position).field_short_title != null
                && mNewsList.get(position).field_short_title.value != null
                && !mNewsList.get(position).field_short_title.value.trim().isEmpty()) {
            String title = mNewsList.get(position).field_short_title.value;
            holder.vNewsTitle.setText(title);
        }

        //Date
        if (mNewsList != null
                && mNewsList.get(position) != null
                && mNewsList.get(position).createdAt != null
                && !mNewsList.get(position).createdAt.trim().isEmpty()) {
            String date = Utility.processDate(mNewsList.get(position).createdAt);
            holder.vNewsDate.setText(date);
        }

        holder.vCardView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoNewsPage(position);
            }
        });

    }


    private void gotoNewsPage(int position) {

        Intent intent = new Intent();
        intent.setClass(mContext, NewsPage.class);
        intent.putExtra(NewsPage.KEY_NEWS_ID, mNewsList.get(position)._id);
        intent.putExtra(NewsPage.KEY_NEWS_INDEX, position);
        intent.putExtra(NewsPage.KEY_NEWS_TYPE, NewsPage.TYPE_SEARCH_NEWS);
        intent.putExtra(NewsPage.KEY_NEWS_CATEGORY, "搜尋頁面");
        UserDataInfo.setSearchList(mNewsList);
        mContext.startActivity(intent);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_search_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView vCardView;
        private ImageView vNewsImage;
        private TextView vNewsTitle;
        private TextView vNewsDate;

        public ViewHolder(View itemView) {
            super(itemView);

            vCardView = (CardView) itemView.findViewById(R.id.news_item);
            vNewsImage = (ImageView) itemView.findViewById(R.id.news_img);
            vNewsTitle = (TextView) itemView.findViewById(R.id.news_title);
            vNewsDate = (TextView) itemView.findViewById(R.id.news_date);

        }

    }

}

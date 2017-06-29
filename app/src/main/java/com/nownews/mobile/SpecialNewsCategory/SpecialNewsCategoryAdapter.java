package com.nownews.mobile.SpecialNewsCategory;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Json.SpecialNewsCategoryJson.*;

import java.util.HashMap;
import java.util.List;

public class SpecialNewsCategoryAdapter extends RecyclerView.Adapter<SpecialNewsCategoryAdapter.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<SpecialChannelsBean> mAlbumList;
    private HashMap<Integer, String> mImageList = new HashMap<Integer, String>();
    private OnItemClickListener mOnItemClickListener;

    public SpecialNewsCategoryAdapter(Context aContext, List<SpecialChannelsBean> aAlbumList, OnItemClickListener aOnItemClickListener) {
        mContext = aContext;
        mAlbumList = aAlbumList;
        mOnItemClickListener = aOnItemClickListener;
    }

    public void setData(List<SpecialChannelsBean> aAlbumList, OnItemClickListener aOnItemClickListener) {
        mAlbumList = aAlbumList;
        mOnItemClickListener = aOnItemClickListener;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mAlbumList == null ? 0 : mAlbumList.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.vCategoryName.setText(mAlbumList.get(position).getTitle());
        holder.vCategoryName.setTextColor(Color.BLACK);
        if(mCurrentPosition == position){
            holder.vCategoryName.setTextColor(mContext.getResources().getColor(R.color.special_news_category_bar));
        }
        holder.vCategoryNameGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                itemClick(position);

            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_special_news_category_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView vCategoryName;
        private RelativeLayout vCategoryNameGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            vCategoryName = (TextView) itemView.findViewById(R.id.category_name);
            vCategoryNameGroup = (RelativeLayout) itemView.findViewById(R.id.category_name_group);
        }

    }

    private int mCurrentPosition;
    public void itemClick(int aPosition){
        mCurrentPosition = aPosition;
        if(mOnItemClickListener!=null && mAlbumList!=null && mAlbumList.size()>0){
            mOnItemClickListener.onItemClick(aPosition, mAlbumList.get(aPosition).getTitle());
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        public void onItemClick(int aPosition, String aCategoryName);
    }

}

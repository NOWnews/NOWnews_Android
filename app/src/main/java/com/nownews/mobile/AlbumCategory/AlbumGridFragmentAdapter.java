package com.nownews.mobile.AlbumCategory;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.AlbumPage.AlbumPage;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.PhotosListJson.PhotosContent;
import com.nownews.mobile.Widget.CustomImageTopcrop;

import java.util.List;

public class AlbumGridFragmentAdapter extends RecyclerView.Adapter<AlbumGridFragmentAdapter.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<PhotosContent> mAlbumList;
    private BitmapController mBitmapController;
    private String mCategoryName;

    public AlbumGridFragmentAdapter(Context aContext, List<PhotosContent> aAlbumList, String aCategoryName) {
        mContext = aContext;
        mAlbumList = aAlbumList;
        mCategoryName = aCategoryName;
        init();
    }

    public void setData(List<PhotosContent> aAlbumList, String aCategoryName) {
        mAlbumList = aAlbumList;
        mCategoryName = aCategoryName;
        notifyDataSetChanged();
    }

    private void init() {
        mBitmapController = BitmapController.getInstance(mContext);
//        mBitmapController = new BitmapController(mContext);
    }

    @Override
    public int getItemCount() {
        return mAlbumList == null ? 0 : mAlbumList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (Utility.DEBUG) Log.e(TAG, "position: " + position);

        holder.vAlbumGroup.setVisibility(View.VISIBLE);
        //Image
        holder.vAlbumImage.setImageDrawable(null);
        if (mAlbumList != null
                && mAlbumList.get(position) != null
                && mAlbumList.get(position).thumbnail != null
                && !mAlbumList.get(position).thumbnail.trim().isEmpty()) {
            String url = mAlbumList.get(position).thumbnail;
            String front = url.substring(0, url.indexOf("&h=")+3);
            String back = url.substring(url.indexOf("&q="), url.length());
            url = front + back;
            if (Utility.DEBUG) Log.i(TAG, "front: " + front);
            if (Utility.DEBUG) Log.i(TAG, "back: " + back);
            if (Utility.DEBUG) Log.i(TAG, "url: " + url);
            mBitmapController.loadImageWithOriginalSize(url, holder.vAlbumImage, BitmapController.IMAGE_SRC, 0, 0, null);
        }

        //Title
        if (mAlbumList != null
                && mAlbumList.get(position) != null
                && mAlbumList.get(position).title != null
                && !mAlbumList.get(position).title.trim().equals("")) {
            final String title = mAlbumList.get(position).title;
            holder.vAlbumTitle.setText(title);
        }

        holder.vAlbumGroup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoAlbumPage(position);
            }
        });

    }

    protected void gotoAlbumPage(int position) {
        int albumId = mAlbumList.get(position).nodeId;
        if (Utility.DEBUG) Log.e(TAG, "albumId: " + albumId);
        Intent intent = new Intent();
        intent.setClass(mContext, AlbumPage.class);
        intent.putExtra(AlbumPage.KEY_ALBUM_ID, albumId);
        intent.putExtra(AlbumPage.KEY_ALBUM_CATEGORY, mCategoryName);
        mContext.startActivity(intent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_album_grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public void clearBitmapController() {
        if (mBitmapController != null) {
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
        }
    }

    public void unRegistContext(Context aContext) {
        if(mContext==aContext){
            mContext = null;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout vAlbumGroup;
        private CustomImageTopcrop vAlbumImage;
        private TextView vAlbumTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            vAlbumGroup = (RelativeLayout) itemView.findViewById(R.id.group);
            vAlbumImage = (CustomImageTopcrop) itemView.findViewById(R.id.album_img);
            vAlbumTitle = (TextView) itemView.findViewById(R.id.news_title);
        }

    }

}

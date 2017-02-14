package com.nownews.mobile.FavoriteAlbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.nownews.R;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Widget.CustomImageTopcrop;

import java.util.ArrayList;

public class FavoriteAlbumGridAdapter extends RecyclerView.Adapter<FavoriteAlbumGridAdapter.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private ArrayList<String> mFavoriteImageList;
    private BitmapController mBitmapController;
    private int mRequestCode = 0x999;

    public FavoriteAlbumGridAdapter(Context aContext, ArrayList<String> aFavoriteImageList) {
        mContext = aContext;
        mFavoriteImageList = aFavoriteImageList;
        mBitmapController = BitmapController.getInstance(mContext);
//        mBitmapController = new BitmapController(mContext);
    }

    public void setData(ArrayList<String> aFavoriteImageList) {
        mFavoriteImageList = aFavoriteImageList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFavoriteImageList == null ? 0 : mFavoriteImageList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //Image
        holder.vAlbumImage.setImageDrawable(null);
        String imageUrl = mFavoriteImageList.get(position);
        mBitmapController.loadImageWithOriginalSize(imageUrl, holder.vAlbumImage, BitmapController.IMAGE_SRC, 0, 0, null);

        holder.vAlbumImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoFavoriteAlbumPage(position);
            }
        });

    }

    protected void gotoFavoriteAlbumPage(int position) {
        Intent intent = new Intent();
        intent.setClass(mContext, FavoriteAlbumPage.class);
        intent.putExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_POSITION, position);
        intent.putStringArrayListExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_LIST, mFavoriteImageList);
        intent.putExtra(FavoriteAlbumPage.KEY_TYPE, FavoriteAlbumPage.TYPE_FAVORITE);
//		intent.putStringArrayListExtra(FavoriteAlbumPage.KEY_FAVORITE_ALBUM_TITLE_LIST, mFavoriteImageTitleList);
        String url = mFavoriteImageList.get(position);
        ((Activity) mContext).startActivityForResult(intent, mRequestCode);
        GoogleAnalyticsFunction.sendHitInfo(mContext, "最愛圖集", url, "");
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_favorite_album_grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CustomImageTopcrop vAlbumImage;

        public ViewHolder(View itemView) {
            super(itemView);
            vAlbumImage = (CustomImageTopcrop) itemView.findViewById(R.id.album_img);
        }

    }

    public void destoryView(){
        if(mBitmapController!=null){
            mBitmapController.clearCache();
            mBitmapController.closeBitmapController();
            mBitmapController.unregistBitmapController(mContext);
        }
    }

}

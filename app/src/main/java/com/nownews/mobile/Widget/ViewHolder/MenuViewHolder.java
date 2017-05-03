package com.nownews.mobile.Widget.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Dao.Entity.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public class MenuViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.icon)
    ImageView vItemImage;
    @BindView(R.id.title)
    TextView vTitle;
    @BindView(R.id.new_version)
    TextView vNewVersion;
    @BindView(R.id.notification_status)
    TextView vNotificationStatus;

    public MenuViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void configure(MenuItem item, boolean isHasNewVersion) {
        ReSizeLayoutParams mResize = new ReSizeLayoutParams(this.itemView.getContext());
        this.vItemImage.setLayoutParams(mResize.setOnSize(this.vItemImage, 10, 10, 10, 10));
        this.vTitle.setLayoutParams(mResize.setMargins(this.vTitle, 10, 10, 10, 10));
        mResize.setTextSize(this.vTitle);
        this.vNewVersion.setLayoutParams(mResize.setMargins(this.vNewVersion, 20, 0, 0, 0));
        mResize.setPadding(this.vNewVersion, 5, 5, 5, 5);
        mResize.setTextSize(this.vNewVersion);
        this.vNotificationStatus.setLayoutParams(mResize.setMargins(this.vNotificationStatus, 50, 0, 0, 0));
        mResize.setTextSize(this.vNotificationStatus);

        this.vTitle.setText(item.getTitle());
        if (item.isSecction()) {
            this.vItemImage.setVisibility(View.GONE);
            this.itemView.setBackgroundColor(this.itemView.getContext().getResources().getColor(R.color.menu_content_category_bg));
        } else {
            this.vItemImage.setVisibility(View.VISIBLE);
            this.vItemImage.setImageResource(item.getIcon());
            this.itemView.setBackgroundColor(this.itemView.getContext().getResources().getColor(android.R.color.transparent));
        }
        if (item.getTitle().equals(this.itemView.getContext().getString(R.string.version)) && isHasNewVersion) {
            this.vNewVersion.setVisibility(View.VISIBLE);
        } else {
            this.vNewVersion.setVisibility(View.GONE);
        }
        if (item.isNotify()) {
            this.vNotificationStatus.setVisibility(View.VISIBLE);
            if (new SharedPreferencesMethods(this.itemView.getContext()).getNotificationStatus()) {
                this.vNotificationStatus.setText("開");
                this.vNotificationStatus.setTextColor(this.itemView.getContext().getResources().getColor(android.R.color.white));
            } else {
                this.vNotificationStatus.setText("關");
                this.vNotificationStatus.setTextColor(this.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            this.vNotificationStatus.setVisibility(View.GONE);
        }
    }
}

package com.nownews.mobile.Widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Common.ReSizeLayoutParams;
import com.nownews.mobile.Common.SharedPreferencesMethods;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuContentAdapter extends BaseAdapter {

    private final String TAG = getClass().getSimpleName();
    private LayoutInflater inflater;

    private Context mContext;
    private ArrayList<HashMap<String, Object>> mList;
    private SharedPreferencesMethods mSharedPref;
    private boolean isHasNewVersion = false;
    private ReSizeLayoutParams mResize;

    public MenuContentAdapter(Context aContext, ArrayList<HashMap<String, Object>> aList) {
        mContext = aContext;
        mList = aList;
        init();
    }

    private void init() {
        inflater = LayoutInflater.from(mContext);
        mSharedPref = new SharedPreferencesMethods(mContext);
        mResize = new ReSizeLayoutParams(mContext);
    }

    public void setData(ArrayList<HashMap<String, Object>> aList) {
        mList = aList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Item item = new Item();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.widget_menu_item, null);

            item.vItem = (LinearLayout) convertView.findViewById(R.id.item);

            item.vItemImage = (ImageView) convertView.findViewById(R.id.icon);
            item.vItemImage.setLayoutParams(mResize.setOnSize(item.vItemImage, 10, 10, 10, 10));

            item.vTitle = (TextView) convertView.findViewById(R.id.title);
            item.vTitle.setLayoutParams(mResize.setMargins(item.vTitle, 10, 10, 10, 10));
            mResize.setTextSize(item.vTitle);

            item.vNewVersion = (TextView) convertView.findViewById(R.id.new_version);
            item.vNewVersion.setLayoutParams(mResize.setMargins(item.vNewVersion, 20, 0, 0, 0));
            mResize.setPadding(item.vNewVersion, 5, 5, 5, 5);
            mResize.setTextSize(item.vNewVersion);

            item.vNotificationStatus = (TextView) convertView.findViewById(R.id.notification_status);
            item.vNotificationStatus.setLayoutParams(mResize.setMargins(item.vNotificationStatus, 50, 0, 0, 0));
            mResize.setTextSize(item.vNotificationStatus);

            convertView.setTag(item);
        } else {
            item = (Item) convertView.getTag();
        }

        String itemName = (String) mList.get(position).get(MenuContent.KEY_TITLE);
        int iconId = (Integer) mList.get(position).get(MenuContent.KEY_ICON);
        String category = (String) mList.get(position).get(MenuContent.KEY_CATEGORY);
        if (iconId == 0) {
            item.vItemImage.setVisibility(View.GONE);
            item.vItem.setBackgroundColor(mContext.getResources().getColor(R.color.menu_content_category_bg));
        } else {
            item.vItemImage.setVisibility(View.VISIBLE);
            item.vItemImage.setImageResource(iconId);
            item.vItem.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
        if (itemName.equals(mContext.getString(R.string.version)) && isHasNewVersion) {
            item.vNewVersion.setVisibility(View.VISIBLE);
        } else {
            item.vNewVersion.setVisibility(View.GONE);
        }
        if (itemName.equals(mContext.getString(R.string.notification_setting))) {
            item.vNotificationStatus.setVisibility(View.VISIBLE);
            if (mSharedPref.getNotificationStatus()) {
                item.vNotificationStatus.setText("開");
                item.vNotificationStatus.setTextColor(mContext.getResources().getColor(android.R.color.white));
            } else {
                item.vNotificationStatus.setText("關");
                item.vNotificationStatus.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            item.vNotificationStatus.setVisibility(View.GONE);
        }
        item.vTitle.setText(itemName);

        return convertView;
    }

    public void setVersionStatus(boolean isHasNewVersion) {
        this.isHasNewVersion = isHasNewVersion;
        notifyDataSetChanged();
    }

    class Item {
        LinearLayout vItem;
        ImageView vItemImage;
        TextView vTitle;
        TextView vNewVersion;
        TextView vNotificationStatus;
    }


}

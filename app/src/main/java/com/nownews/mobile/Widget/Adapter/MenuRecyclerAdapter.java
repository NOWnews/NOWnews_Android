package com.nownews.mobile.Widget.Adapter;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nownews.R;
import com.nownews.mobile.Basic.BaseSideActivity;
import com.nownews.mobile.Dao.Entity.MenuItem;
import com.nownews.mobile.Widget.MenuContent;
import com.nownews.mobile.Widget.ViewHolder.MenuViewHolder;

import java.util.LinkedList;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public class MenuRecyclerAdapter<T> extends BaseRecyclerAdapter<T> {
    private BaseSideActivity baseActivity;
    private int selected = 1;
    private Fragment baseFragment;
    private MenuContent menuContent;
    private boolean isHasNewVersion = false;

    public MenuRecyclerAdapter(BaseSideActivity activity, RecyclerView v, MenuContent menuContent, LinkedList<T> datas) {
        super(activity, v, datas);
        this.baseActivity = activity;
        this.menuContent = menuContent;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.baseActivity, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_menu_item, parent, false));
    }

    @Override
    public void onClick(View v) {
        this.menuContent.itemClick((Integer) v.getTag());
    }

    @Override
    public void convert(RecyclerView.ViewHolder holder, T item, int position, boolean isScrolling) {
        if (item instanceof MenuItem) {
            ((MenuViewHolder) holder).configure((MenuItem) item, this.isHasNewVersion);
            if (!((MenuItem) item).isSecction()) {
                ((MenuViewHolder) holder).itemView.setOnClickListener(this);
                ((MenuViewHolder) holder).itemView.setTag(position);
            }
        }
    }

    public T getSelectItem() {
        return this.realDatas.get(this.selected);
    }

    public void setInitSelect() {
        this.selected = 1;
        notifyDataSetChanged();
    }

    public void setHasNewVersion(boolean value) {
        this.isHasNewVersion = value;
        notifyDataSetChanged();
    }
}

package com.nownews.mobile.Widget.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by ChengYuanChin on 2017/4/25.
 */

public abstract class BaseRecyclerAdapter<T> extends Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    protected LinkedList<T> realDatas;
    protected RecyclerView recyclerView;
    protected Context cc;

    public BaseRecyclerAdapter(Context context, RecyclerView v, final LinkedList<T> datas) {
        this.realDatas = datas;
        this.cc = context;
        this.recyclerView = v;
    }

    /**
     * Recycler Adapter填充方法
     *
     * @param holder      viewholder
     * @param item        item物件
     * @param isScrolling RecyclerView是否正在滾動
     */
    public abstract void convert(RecyclerView.ViewHolder holder, T item, int position, boolean isScrolling);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        convert(holder, realDatas.get(position), position, false);
    }

    @Override
    public int getItemCount() {
        return this.realDatas == null ? 0 : this.realDatas.size();
    }
}

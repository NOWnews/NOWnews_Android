package com.nownews.mobile.Live;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Json.LiveListJson;
import com.nownews.mobile.Widget.CSTVDownloadDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    public boolean isApiLoadingSuccess;
    private ApiController mApiController;
    private ApiHandler mApiHandler;
    private ListView vCategoryList;
    private ListView vChannelList;

    public LiveFragment() {
        // do nothing...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startFragment();

    }

    private void startFragment(){
        initController();
        processView();
        getLiveList();
    }

    private void initController(){
        mApiController = ApiController.getInstance();
        mApiHandler = new ApiHandler();
    }

    private void processView(){

        View view = getView();

        vCategoryList = (ListView)view.findViewById(R.id.category);
        vChannelList = (ListView)view.findViewById(R.id.channel_list);

    }

    private void getLiveList(){
        if(mApiController!=null){
            mApiController.getLiveList(mApiHandler);
        }
    }

    private List<LiveListJson.Data> mLiveList;
    private class ApiHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case ParameterSet.GET_LIVE_LIST_DONE:
                    mLiveList = (List<LiveListJson.Data>) msg.obj;
                    if(mLiveList!=null){
                        processList();
                    }
                    break;
                case ParameterSet.GET_LIVE_LIST_FAILED:
                    break;
            }

        }
    }

    private void processList(){
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.Data data : mLiveList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("categoryName", data.categoryName);
            items.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), items, R.layout.widget_live_gategory_item, new String[]{"categoryName"}, new int[]{R.id.gategory_item});
        vCategoryList.setAdapter(adapter);
        vCategoryList.setSelector(R.color.light_gray);
        vCategoryList.setOnItemClickListener(mCategoryItemClickListener);
        vCategoryList.setSelection(0);
        vCategoryList.performItemClick(vCategoryList.getChildAt(0), 0, R.id.gategory_item);
    }

    private AdapterView.OnItemClickListener mCategoryItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            processChannelList(position);

        }
    };

    public void reload() {
        startFragment();
    }

    private List<LiveListJson.ChannelList> mCurrentChannelList;
    private int mCurrentCategoryIndex;
    private void processChannelList(int position){

        mCurrentCategoryIndex = position;
        mCurrentChannelList = mLiveList.get(position).list;
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for(LiveListJson.ChannelList list : mCurrentChannelList){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("channelName", list.title);
            items.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), items, R.layout.widget_live_channel_item, new String[]{"channelName"}, new int[]{R.id.channel_item});
        vChannelList.setAdapter(adapter);
        vChannelList.setOnItemClickListener(mChannelListItemClickListener);

    }

    private AdapterView.OnItemClickListener mChannelListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            openDownloadDialog(position);

        }
    };

    private CSTVDownloadDialog mCSTVDownloadDialog;
    private void openDownloadDialog(int position){
        if(mCSTVDownloadDialog!=null && mCSTVDownloadDialog.isShowing()){
            return;
        }
        String title = mCurrentChannelList.get(position).title;
        String path = mCurrentChannelList.get(position).path;
        mCSTVDownloadDialog = new CSTVDownloadDialog(getActivity(), title, mLiveList, path, mCurrentCategoryIndex, position);
        mCSTVDownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                mCSTVDownloadDialog = null;

            }
        });
        mCSTVDownloadDialog.show();
    }

    @Override
    public void onDestroyView() {
        UserDataInfo.setLiveList(null);
        mApiHandler.removeCallbacks(null);
        super.onDestroyView();
    }
}

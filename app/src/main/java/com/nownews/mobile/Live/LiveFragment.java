package com.nownews.mobile.Live;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.LiveListJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    public boolean isApiLoadingSuccess;
    private ApiController mApiController;
    private BitmapController mBitmapController;
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
        mBitmapController = BitmapController.getInstance(getActivity());
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

    private LiveListJson mLiveInfoJson;
    private List<LiveListJson.Data> mLiveList;
    private class ApiHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case ParameterSet.GET_LIVE_LIST_DONE:
                    mLiveInfoJson = (LiveListJson) msg.obj;
                    if(mLiveInfoJson!=null){
                        mLiveList = mLiveInfoJson.data;
                        if(mLiveList!=null){
                            processList();
                        }
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
        String iconUrl = mLiveInfoJson.liveInfo.icon;
        if(mBitmapController!=null){
            mBitmapController.preloadOriginalImageFromUrl(iconUrl, null, BitmapController.IMAGE_SRC, 0, 0, new BitmapController.ImageLoadingListener() {
                @Override
                public void onLoadingStart(String aImageUrl, View aView) { }

                @Override
                public void onLoadingFailed(String aImageUrl, View aView, Exception aException) {

                }

                @Override
                public void onLoadingComplete(String aImageUrl, View aView, Bitmap aBitmap) {

                    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                    for(LiveListJson.ChannelList list : mCurrentChannelList){
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("channelName", list.title);
                        map.put("channelIcon", aBitmap);
                        items.add(map);
                    }
                    SimpleAdapter adapter = new SimpleAdapter(getActivity(), items, R.layout.widget_live_channel_item, new String[]{"channelName", "channelIcon"}, new int[]{R.id.channel_item, R.id.tv_icon});
                    adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        @Override
                        public boolean setViewValue(View view, Object o, String s) {

                            if(view instanceof ImageView && o instanceof Bitmap){
                                ((ImageView)view).setImageBitmap((Bitmap)o);
                                return true;
                            }

                            return false;
                        }
                    });
                    vChannelList.setAdapter(adapter);
                    vChannelList.setOnItemClickListener(mChannelListItemClickListener);

                }

                @Override
                public void onLoadingCancelled() { }

                @Override
                public void onProgressUpdate(String aImageUrl, int aProgress, int max) { }
            });
        }

    }

    private AdapterView.OnItemClickListener mChannelListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            openDownloadDialog(position);

        }
    };

    private LiveAppDownloadDialog mCSTVDownloadDialog;
    public void openDownloadDialog(int position){
        if(mCSTVDownloadDialog!=null && mCSTVDownloadDialog.isShowing()){
            return;
        }
        String title = mCurrentChannelList.get(position).title;
        String path = mCurrentChannelList.get(position).path;
        mCSTVDownloadDialog = new LiveAppDownloadDialog(getActivity(), title, mLiveList, path,
                mCurrentCategoryIndex, position, mLiveInfoJson);
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

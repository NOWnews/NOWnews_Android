package com.nownews.mobile.NewsPage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nownews.R;
import com.nownews.mobile.Api.ParameterSet;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Common.GoogleAnalyticsFunction;
import com.nownews.mobile.Common.SharedPreferencesMethods;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Controller.ApiController;
import com.nownews.mobile.Controller.BitmapController;
import com.nownews.mobile.Json.NewsInfoJson;
import com.nownews.mobile.Json.NewsInfoJson.MobileBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NewsPageRecyclerViewFragment extends Fragment {

    public final static String KEY_NEWS_URL = "newsUrl";
    public final static String KEY_NEWS_ID = "newsId";
    private final String TAG = getClass().getSimpleName();
    public boolean isOnPause = false;
    private ApiController mApiController;
    private BitmapController mBitmapController;
    private String mNewsUrl;
    private int mNewsId;
    private NewsInfoJson mNewsInfo;
    private ArrayList<String> mImageUrlList;
    private SharedPreferencesMethods mSharedPref;
    private String[] mEcoDefaultImageList;
    private boolean isRefereshing = false;

    //New View
    private TextView vError;
    private RecyclerView vContentRecyclerView;
    private RelativeLayout vLoadingLayout;
    private SwipeRefreshLayout vRefreshLayout;

    private ArrayList<ConcurrentHashMap<String, Object>> mContentList;
    public final static String KEY_CONTEXT_TEXT = "context_text";
    public final static String KEY_CONTEXT_IMAGE = "context_image";
    public final static String KEY_CONTEXT_IMAGE_TEXT = "context_image_text";

    private boolean isNewsInfoLoadSucess = false;
    public boolean isNewsInfoLoadSucess(){
        return isNewsInfoLoadSucess;
    }
    private ApiHandler mHandler;
    private static class ApiHandler extends Handler {

        private final WeakReference<NewsPageRecyclerViewFragment> mFragment;

        public ApiHandler(NewsPageRecyclerViewFragment aFragment){
            mFragment = new WeakReference<NewsPageRecyclerViewFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            NewsPageRecyclerViewFragment fragment = mFragment.get();
            if(fragment==null || !fragment.isAdded()){
                return;
            }

            switch (msg.what) {
                case ParameterSet.GET_NEWS_INFO_DONE:
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    fragment.mNewsInfo = (NewsInfoJson) msg.obj;
                    if (fragment.mNewsInfo != null) {
                        fragment.isNewsInfoLoadSucess = true;
//                        while (true) {
                            if (fragment.isAdded()) {
                                fragment.processNews();
                                break;
                            }
//                        }
                    }
                    break;
                case ParameterSet.GET_NEWS_INFO_FAILED:
                    fragment.isNewsInfoLoadSucess = false;
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    break;
                case ParameterSet.SOCKET_TIME_OUT:
                    Utility.openSocketTimeoutDialog(fragment.getActivity());
                    if (fragment.isRefereshing) {
                        // Stop refresh animation
                        fragment.isRefereshing = false;
                        fragment.vRefreshLayout.setRefreshing(false);
                    }
                    break;
            }

        }

    };

    public NewsPageRecyclerViewFragment() {
        //DO NOTHING...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_page_recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Utility.DEBUG) Log.e(TAG, "onActivityCreated");

        startFragment();

    }

    private void startFragment() {

        initController();
        processArgument();
        processView();
        getNewsInfo();

    }

    private void processArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {

            String aNewsUrl = bundle.getString(KEY_NEWS_URL);
            int aNewsId = bundle.getInt(KEY_NEWS_ID, -1);

            if (Utility.DEBUG) Log.e(TAG, "VideoNewsPageFragment");
            if (Utility.DEBUG) Log.e(TAG, "aNewsUrl: " + aNewsUrl);
            if (Utility.DEBUG) Log.e(TAG, "aNewsId: " + aNewsId);

            mNewsUrl = aNewsUrl;
            mNewsId = aNewsId;
            Log.d(TAG, "mNewsUrl: " + mNewsUrl);
            if (mNewsUrl != null && (mNewsUrl.startsWith("/n/")
                    || mNewsUrl.contains("/news/")
                    || mNewsUrl.contains("/photo/"))) {
                mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1));
            } else if (mNewsUrl != null) {
                mNewsId = Integer.parseInt(mNewsUrl.substring(mNewsUrl.lastIndexOf("/") + 1, mNewsUrl.lastIndexOf("?")));
            }
        }
    }

    private void initController() {
        mApiController = ApiController.getInstance();
        mBitmapController = BitmapController.getInstance(getActivity());
//        mBitmapController = new BitmapController(getActivity());
        mBitmapController.closeBitmapController();
        mBitmapController.clearCache();
        mSharedPref = new SharedPreferencesMethods(getActivity());
        mEcoDefaultImageList = new String[]{
                "http://s.nownews.com/w/images/mobile/defaultimg-01.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-02.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-03.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-04.jpg",
                "http://s.nownews.com/w/images/mobile/defaultimg-05.jpg"};
        mHandler = new ApiHandler(this);
    }

    public void getNewsInfo() {
        if (mApiController != null) {
            mApiController.getNewsInfo(mHandler, mNewsId);
        }
    }

    public void processView() {

        View view = getView();

        //New View
        vError = (TextView)view.findViewById(R.id.error_message);
        vContentRecyclerView = (RecyclerView)view.findViewById(R.id.content_recyclerview);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
        vRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        vRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (Utility.DEBUG) Log.e(TAG, "vList refresh!!");
                isRefereshing = true;
                getNewsInfo();
            }
        });
    }

    private LinearLayoutManager mLinearLayoutManager;
    private void processNews() {

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }
        mImageUrlList = new ArrayList<String>();
        vLoadingLayout.setVisibility(View.GONE);

        String mCategoryName = ((NewsPage) getActivity()).getCurrentNewsCategory();
        if (mNewsInfo.image != null
                && mNewsInfo.image.thumbnail != null
                && !mNewsInfo.image.thumbnail.trim().isEmpty()) {
            String bigImgUrl = mNewsInfo.image.url;
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mImageUrlList.add(bigImgUrl);
        } else if (mCategoryName.contains(getString(R.string.eco))) {
            int newsId = mNewsInfo.nodeId;
            int digit = newsId % 10;
            int imagePosition = digit % 5;
            String bigImgUrl = mEcoDefaultImageList[imagePosition];
            if (Utility.DEBUG) Log.v(TAG, "===@@###bigImgUrl: " + bigImgUrl);
            mImageUrlList.add(bigImgUrl);
        }

        if (mNewsInfo.htmlBody != null) {
            String body = mNewsInfo.htmlBody;
            processBody(body);
            processRecyclerView();
        } else if (mNewsInfo.mobileBody != null) {
            processBody(mNewsInfo.mobileBody);
            processRecyclerView();
        }

    }

    private void processRecyclerView(){

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        vContentRecyclerView.setLayoutManager(mLinearLayoutManager);
        NewsPageRecyclerViewAdapter mAdapter = new NewsPageRecyclerViewAdapter(getActivity(), mContentList, mNewsInfo, mImageUrlList);
        vContentRecyclerView.setAdapter(mAdapter);

    }

    private void processBody(List<MobileBody> jbody) {
        if (Utility.DEBUG) Log.e(TAG, "processBody(List<JsonBody> jbody)");

        if(mContentList==null){
            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
        }else{
            mContentList.clear();
        }

        for (int i = 0; i < jbody.size(); i++) {
            String type = jbody.get(i).tag;
            String content = jbody.get(i).content;
            if (Utility.DEBUG) Log.w(TAG, "type: " + type);
            if (type.equals("image")) {

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

                String img = jbody.get(i).src;
                if (img != null && !img.trim().isEmpty()) {
                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + img);
                    map.put(KEY_CONTEXT_IMAGE, img);
                    mImageUrlList.add(img);
                }

                if (content != null && !content.trim().equals("") && !content.trim().equals("▲")) {
                    map.put(KEY_CONTEXT_IMAGE_TEXT, content);
                }

                mContentList.add(map);

            } else if (type.equals("p")) {

                if (content != null && !content.trim().equals("")) {

                    if (content.contains("延伸閱讀")) {
                        break;
                    }

                    if(content.contains("&nbsp;")){
                        content = content.replace("&nbsp;", "");
                        if(content.trim().isEmpty()){
                            continue;
                        }
                    }

                    if(content.contains("&lt;")){
                        content = content.replace("&lt;", "<");
                        if(content.trim().isEmpty()){
                            continue;
                        }
                    }

                    if(content.contains("&gt;")){
                        content = content.replace("&gt;", ">");
                        if(content.trim().isEmpty()){
                            continue;
                        }
                    }

                    ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                    map.put(KEY_CONTEXT_TEXT, content);
                    mContentList.add(map);

                }
            }
        }
    }

    private void processBody(String body) {
        if (Utility.DEBUG) Log.e(TAG, "body: " + body);

        if(mContentList==null){
            mContentList = new ArrayList<ConcurrentHashMap<String, Object>>();
        }else{
            mContentList.clear();
        }

        String citeContent = null;
        body = body.replace("<br />", "$$$");
        body = body.replace("<br>", "&&&");
        body = body.replace("<strong>", "(((");
        body = body.replace("</strong>", ")))");
        body = body.replace("&nbsp;", "");
        body = body.trim();
        if (Utility.DEBUG) Log.i(TAG, "body: " + body);
        Document doc = Jsoup.parse(body);
        Elements paragraph = doc.select("p");
        for (int i = 0; i < paragraph.size(); i++) {
            Element pContent = paragraph.get(i);
            String p = pContent.text();
            p = p.replace("$$$", "\n");
            p = p.replace("&&&", "\n");
            if (Utility.DEBUG) Log.w(TAG, "p: " + p);

            Elements img = pContent.select("img[src]");
            Elements cite = pContent.select("cite");
            if (cite != null && cite.size() > 0) {
                for (int j = 0; j < cite.size(); j++) {
                    Element imgElement = cite.get(j);
                    if (!imgElement.text().trim().equals("▲")) {
                        citeContent = imgElement.text();
                        if (Utility.DEBUG) Log.w(TAG, "citeContent: " + citeContent);
                        break;
                    }
                }
            }

            if (p.contains("▲")) {
                citeContent = p;
            }

            if (citeContent != null) {
                p = p.replace(citeContent, "");
                if (Utility.DEBUG) Log.d(TAG, "[p] after replace: " + p);
            }

            if (p != null && !p.trim().isEmpty()) {
                if (p.contains("延伸閱讀")) {
                    break;
                }

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(KEY_CONTEXT_TEXT, p);
                mContentList.add(map);

            }

            if (img != null && img.size() > 0) {

                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

                for (int j = 0; j < img.size(); j++) {
                    Element imgElement = img.get(j);
                    String imgUrl = imgElement.attr("src");
                    imgUrl = Utility.getSrcFromImgapi(imgUrl);
//                    if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);

                    if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                        if (Utility.DEBUG) Log.w(TAG, "imgUrl in body: " + imgUrl);
                        map.put(KEY_CONTEXT_IMAGE, imgUrl);
                        mImageUrlList.add(imgUrl);
                    }

                }

                if (citeContent != null && !citeContent.trim().equals("") && !citeContent.trim().equals("▲")) {
                    map.put(KEY_CONTEXT_IMAGE_TEXT, citeContent);
                }

                mContentList.add(map);
            }


        }
    }


    @Override
    public void onDestroy() {
        if(Utility.DEBUG)Log.i(TAG, TAG + "%%%onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (Utility.DEBUG) Log.e(TAG, "onDestroyView()");

        if (mImageUrlList != null) {
            mImageUrlList.clear();
            mImageUrlList = null;
        }
        if(vContentRecyclerView!=null && vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).destory();
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).unRegistContext(getActivity());
            vContentRecyclerView.getRecycledViewPool().clear();
            vContentRecyclerView.setLayoutManager(null);
            vContentRecyclerView.setAdapter(null);
            vContentRecyclerView = null;
        }

        if(mSharedPref!=null){
            mSharedPref.unRegistContext(getActivity());
        }

        if(mHandler!=null){
            mHandler.removeCallbacks(null);
            mHandler = null;
        }
        super.onDestroyView();
    }

    public void changeTextSize(String aTextSize) {
        if(Utility.DEBUG)Log.v(TAG, "changeTextSize aTextSize: " + aTextSize);
        if(Utility.DEBUG)Log.v(TAG, "vContentRecyclerView is null or not?? : " + (vContentRecyclerView==null? "true":"false"));
        if(Utility.DEBUG && vContentRecyclerView!=null) {
            Log.v(TAG, "vContentRecyclerView.getAdapter() is null or not?? : " + (vContentRecyclerView.getAdapter() == null ? "true" : "false"));
        }
        if(Utility.DEBUG)Log.v(TAG, "mSharedPref is null or not?? : " + (mSharedPref==null? "true":"false"));
        if(vContentRecyclerView!=null && vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).changeTextSize(aTextSize);
        }
        if(mSharedPref!=null){
            mSharedPref.saveNewsContentTextSize(aTextSize);
        }
    }

    public void setHitInfo(String aNewsCategory) {
        String url = null;
        if (mNewsUrl != null && mNewsUrl.startsWith("/n/")) {
            url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsUrl;
        } else if (mNewsUrl != null) {
            url = WebAPIUrl.NOWNEWS_PC_DOMAIN + mNewsUrl.substring(mNewsUrl.indexOf("/n/"), mNewsUrl.lastIndexOf("?"));
        }
        if (url != null) {
            GoogleAnalyticsFunction.sendHitInfo(getActivity(), aNewsCategory, url, "");
        }
    }

    @Override
    public void onPause() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onPause()");
        isOnPause = true;
        if(vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).pauseDFP();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (Utility.DEBUG) Log.e(TAG, TAG + " onResume()");
        if(vContentRecyclerView.getAdapter()!=null){
            ((NewsPageRecyclerViewAdapter)vContentRecyclerView.getAdapter()).resumeDFP();
        }
        if (isOnPause) {
            isOnPause = false;
            getNewsInfo();
        }
        super.onResume();
    }

    public void reload() {
        startFragment();
    }

    public NewsInfoJson getCurrentNewsInfo(){
        if(mNewsInfo!=null){
            return mNewsInfo;
        }
        return null;
    }

}

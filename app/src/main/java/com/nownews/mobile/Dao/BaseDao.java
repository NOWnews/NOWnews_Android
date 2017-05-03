package com.nownews.mobile.Dao;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nownews.mobile.Baselibs.RequestCacheUtil;
import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Dao.Entity.ResponseErr;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public abstract class BaseDao<T> implements Serializable {
    ObjectMapper mObjectMapper = new ObjectMapper();
    protected Context mContext;
    protected T DataCategorys = null;
    protected String token = "NOWnewsTaiwanNumberOne";

    public BaseDao(Context context) {
        mContext = context;
    }

    protected <T extends RequestCacheUtil.Method> String doSync(T method, boolean UseCache,
                                                                String authorization, String url, String params) {
        String result = RequestCacheUtil.getRequestContent(this.mContext, authorization,
                url,
                params,
                Constants.WebSourceType.Json,
                Constants.DBContentType.Content_list, UseCache, method);
        Log.e(getClass().getSimpleName(), "result:" + result);

        return result;
    }

    protected T isErr(String result) {
        if (result != null) {
            try {
                return (T) parse(result, new TypeReference<ResponseErr>() {
                }, true);
            } catch (IOException e) {
                e.printStackTrace();
                return (T) result;
            }
        }
        return (T) result;
    }

    protected <T> T parse(T result, TypeReference type, boolean isfilter) throws IOException {
        this.mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, isfilter);
        if (result instanceof String) {
            return mObjectMapper.readValue(
                    (String) result, type);
        } else {
            return result;
        }
    }

    /**
     * 判斷錯誤碼是否為token失效
     *
     * @return
     * @throws IOException
     */
    public synchronized boolean checkToken() throws IOException {
        boolean isUsable = true;
//        if (MemberManager.getInstance().isLogin()) {
//            Log.e(getClass().getSimpleName(), "checkToken start");
//            if (checkDatetime()) {
//                Log.e(getClass().getSimpleName(), "checkToken: 需更新");
//                // token過期
//                String result = RequestCacheUtil.getRequestContent(this.mContext, MemberManager.getInstance().getAuthorization(),
//                        Constants.url.LoginApi, makePostDataByRefresh(),
//                        Constants.WebSourceType.Json,
//                        Constants.DBContentType.Content_list, false, RequestCacheUtil.Method.POST);
//                Log.e(getClass().getSimpleName(), "result:" + result);
//
//                if (!result.isEmpty() && !result.equalsIgnoreCase("{}")) {
//                    try {
//                        MemberData response = mObjectMapper.readValue(
//                                result, new TypeReference<MemberData>() {
//                                });
//                        if (response != null) {
//                            isUsable = MemberManager.getInstance().updateUserToken(response);
//                        }
//                    } catch (IOException e) {
//
//                    }
//                }
//            } else {
//                Log.e(getClass().getSimpleName(), "checkToken: 不需更新");
//                isUsable = true;
//            }
//            Log.e(getClass().getSimpleName(), "checkToken End");
//        }
        return isUsable;
    }

    /**
     * 判斷是否須更新access token
     *
     * @return true: 須更新; false: 不需更新
     */
    private synchronized boolean checkDatetime() {
//        try {
//            DateTime dt = ISODateTimeFormat.dateTime().parseDateTime(MemberManager.getInstance().getMemberData().getCreated_at());
//            Duration d = new Duration(dt, new DateTime());
//            long timelong = d.getMillis();
//            Log.e(getClass().getSimpleName(), "twoDateDistance:" + timelong);
//            Log.e(getClass().getSimpleName(), "getExpiresin:" + (MemberManager.getInstance().getMemberData().getExpiresin() * 1000));
//
//            if (timelong > (MemberManager.getInstance().getMemberData().getExpiresin() * 1000) - 60000) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (NullPointerException ex) {
//            ex.printStackTrace();
//            if (MemberManager.getInstance().getMemberData().getRefreshToken() != null) {
//                return true;
//            } else {
//                return false;
//            }
//        }
        return false;
    }

//    private synchronized String makePostDataByRefresh() {
//        return Jsonserialize.Json("grant_type", "refresh_token", "refresh_token", MemberManager.getInstance().getMemberData().getRefreshToken());
//    }

    public abstract T mapperJson();
}

package com.nownews.mobile.Dao;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Baselibs.RequestCacheUtil;
import com.nownews.mobile.Json.LiveInfoJson;

import java.io.IOException;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class LiveInfoDao<T> extends BaseDao<T> {

    public LiveInfoDao(Context context) {
        super(context);
    }

    @Override
    public T mapperJson() {
        try {
            this.DataCategorys = parse(isErr(doSync(RequestCacheUtil.Method.GET,
                    false,
                    this.token,
                    WebAPIUrl.LIVE_INFO,
                    null)),
                    new TypeReference<LiveInfoJson>() {
                    }, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.DataCategorys;
    }
}

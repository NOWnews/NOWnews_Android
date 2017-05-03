package com.nownews.mobile.Dao;

import android.content.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nownews.mobile.Api.WebAPIUrl;
import com.nownews.mobile.Baselibs.RequestCacheUtil;
import com.nownews.mobile.Json.CheckVersionJson;

import java.io.IOException;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class CheckVerDao<T> extends BaseDao<T> {

    public CheckVerDao(Context context) {
        super(context);
    }

    @Override
    public T mapperJson() {
        try {
            this.DataCategorys = parse(isErr(doSync(RequestCacheUtil.Method.GET,
                    false,
                    this.token,
                    WebAPIUrl.GET_CURRENT_APP_VERSION,
                    null)),
            new TypeReference<CheckVersionJson>() {
            }, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.DataCategorys;
    }
}

package com.nownews.mobile.Baselibs;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Jsonserialize {

    public static String Json(Object... value) {
        int sum = value.length / 2;
        JSONObject job = new JSONObject();
        for (int i = 0; i < sum; i++) {
            try {
                if (value[sum + i] != null
                        && value[sum + i].getClass().isArray()) {
                    JSONArray array = new JSONArray();
                    for (Object item : (Object[]) value[sum + i]) {
                        array.put(item);
                    }
                    job.put(value[i].toString(), array);
                } else {
                    job.put(value[i].toString(), value[sum + i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e("Json", job.toString());
        return job.toString();
    }

    public static JSONObject Json2ByCopyMove(Object... value) {
        int sum = value.length / 2;
        JSONObject job = new JSONObject();
        for (int i = 0; i < sum; i++) {
            try {
                if (value[sum + i] != null
                        && value[sum + i].getClass().isArray()) {
                    JSONArray array = new JSONArray();
                    for (int item : (int[]) value[sum + i]) {
                        array.put(item);
                    }
                    job.put(value[i].toString(), array);
                } else {
                    job.put(value[i].toString(), value[sum + i]);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block 0918132667 0809000852
                e.printStackTrace();
            }
        }
        Log.e("Json", job.toString());
        return job;
    }
}

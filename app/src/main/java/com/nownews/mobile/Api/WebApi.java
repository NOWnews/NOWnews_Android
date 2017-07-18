package com.nownews.mobile.Api;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.nownews.mobile.Common.Utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebApi {

    private final static String TAG = "WebApi";

    public static int DoPost(String aUrl, String aData1, boolean isHadHeader) throws Exception {

        long StartTime = System.currentTimeMillis();

        URL apiUrl = new URL(aUrl);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        if(isHadHeader){
            connection.setRequestProperty(WebAPIUrl.HEADER_KEY, WebAPIUrl.HEADER_VALUE);
        }
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes(aData1);

        String message = connection.getResponseMessage();
        long ProcessTime = System.currentTimeMillis() - StartTime;
        int statusCode = connection.getResponseCode();
        if (Utility.DEBUG) {
            Log.e(TAG, "----------------------------------------- \n"
                    + "statusCode: " + statusCode
                    + "\nurl: " + aUrl
                    + "\nProcessTime: " + ProcessTime + "ms"
                    + "\nmessage: " + message + "\nDoGet End!!"
                    + "\n-----------------------------------------");
        }
        if(statusCode!=HttpURLConnection.HTTP_OK){
            Log.e(TAG, "Error Message: " + connection.getResponseMessage());
            FirebaseCrash.logcat(Log.WARN, TAG, "statusCode: " + statusCode + " / Error Message: " + connection.getResponseMessage() + " / Url: " + aUrl);
            throw new HttpConnectionException(connection.getResponseMessage());
        }

        if (statusCode == 200 || statusCode == 400) {
            InputStream inputStream = null;
            if (statusCode == 200) {
                inputStream = connection.getInputStream();
            } else if (statusCode == 400) {
                inputStream = connection.getErrorStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } else if (statusCode == 500) {
            return -1;
        }

        return statusCode;
    }

    @SuppressWarnings("deprecation")
    public static String DoGet(String aUrl, boolean isHadHeader) throws Exception {
        return DoGet(aUrl, isHadHeader, null);
    }

    @SuppressWarnings("deprecation")
    public static String DoGet(String aUrl, boolean isHadHeader, ConcurrentHashMap<String, String> params) throws Exception {

        Log.w(TAG, "DoGet Start!!");

        long StartTime = System.currentTimeMillis();

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        builder.url(aUrl);
        if(isHadHeader){
            builder.addHeader(WebAPIUrl.HEADER_KEY, WebAPIUrl.HEADER_VALUE);
        }

        HttpUrl.Builder httpBuilder = HttpUrl.parse(aUrl).newBuilder();
        if(params!=null){
            for(Map.Entry<String, String> param : params.entrySet()){
                httpBuilder.addEncodedQueryParameter(param.getKey(), URLDecoder.decode(param.getValue(), "UTF-8"));
            }
        }

        Request request = builder.url(httpBuilder.build()).build();
        Response response = okHttpClient.newCall(request).execute();
        int statusCode = response.code();
        String message = response.body().string();
        long ProcessTime = System.currentTimeMillis() - StartTime;
        if (Utility.DEBUG)
            Log.e(TAG, "----------------------------------------- \n"
                    + "statusCode: " + statusCode
                    + "\nurl: " + aUrl
                    + "\nProcessTime: " + ProcessTime + "ms"
                    + "\nmessage: " + message + "\nDoGet End!!"
                    + "\n-----------------------------------------");
        if(statusCode!=HttpURLConnection.HTTP_OK){
            Log.e(TAG, "Error Message: " + response.message());
            FirebaseCrash.logcat(Log.WARN, TAG, "statusCode: " + statusCode + " / Error Message: " + response.message() + " / Url: " + aUrl);
            throw new HttpConnectionException(response.message());
        }
        response.body().close();
        return message;

//        long StartTime = System.currentTimeMillis();
//        URL apiUrl = new URL(aUrl);
//         HttpURLConnectionconnection = (HttpURLConnection) apiUrl.openConnection();
//        connection.setRequestMethod("GET");
//        if (isHadHeader) {
//            connection.setRequestProperty(WebAPIUrl.HEADER_KEY, WebAPIUrl.HEADER_VALUE);
//        }
////        connection.setConnectTimeout(8000);
////        connection.setReadTimeout(8000);
//
//        int statusCode = connection.getResponseCode();
//        if (Utility.DEBUG) Log.d(TAG, "statusCode: " + statusCode);
//
//        if (statusCode == HttpURLConnection.HTTP_OK) {
//            InputStream inputStream = null;
//            inputStream = connection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder result = new StringBuilder();
//            while(true){
//                final String line = reader.readLine();
//                if(line == null) break;
//                result.append(line);
//            }
////            while ((line = reader.readLine()) != null) {
////                result.append(line);
////            }
//            long ProcessTime = System.currentTimeMillis() - StartTime;
//            if (Utility.DEBUG)
//                Log.e(TAG, "----------------------------------------- \nurl: " + aUrl + "\nProcessTime: " + ProcessTime + "ms" + "\nresult: " + result.toString() + "\nDoGet End!!\n-----------------------------------------");
//            reader.close();
//            return result.toString();
//        } else {
//            long ProcessTime = System.currentTimeMillis() - StartTime;
//            if (Utility.DEBUG)
//                Log.e(TAG, "----------------------------------------- \nurl: " + aUrl + "\nProcessTime: " + ProcessTime + "ms DoGet End!!\n-----------------------------------------");
//            throw new HttpConnectionException(connection.getResponseMessage());
//        }

    }

    public static String DoPut(String aUrl, String aData) throws Exception {

//		Log.w(TAG, "url: " + aUrl);

        URL apiUrl = new URL(aUrl);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        if (aData != null) {
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(aData);
        }

        int statusCode = connection.getResponseCode();
        if (Utility.DEBUG) Log.d(TAG, "statusCode: " + statusCode);

        if (statusCode == 200 || statusCode == 400) {
            InputStream inputStream = null;
            if (statusCode == 200) {
                inputStream = connection.getInputStream();
            } else if (statusCode == 400) {
                inputStream = connection.getErrorStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } else if (statusCode == 500) {
            return "未知的錯誤";
        }

        return null;
    }

    public static class HttpConnectionException extends Exception {

        //Parameterless Constructor
        public HttpConnectionException() {
        }

        //Constructor that accepts a message
        public HttpConnectionException(String message) {
            super(message);
            FirebaseCrash.report(this);
        }
    }

}

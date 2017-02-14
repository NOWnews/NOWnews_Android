package com.nownews.mobile.Api;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.nownews.mobile.Common.Utility;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebApi {

    private final static String TAG = "WebApi";

    public static String DoPost(String aUrl, String aData) throws Exception {

        URL apiUrl = new URL(aUrl);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
//        connection.setConnectTimeout(8000);
//        connection.setReadTimeout(8000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes(aData);

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

    @SuppressWarnings("deprecation")
    public static String DoGet(String aUrl, boolean isHadHeader) throws Exception {

        Log.w(TAG, "DoGet Start!!");

        long StartTime = System.currentTimeMillis();

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(aUrl)
                .addHeader(WebAPIUrl.HEADER_KEY, WebAPIUrl.HEADER_VALUE)
                .build();
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

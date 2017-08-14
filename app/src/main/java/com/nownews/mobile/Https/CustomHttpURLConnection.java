package com.nownews.mobile.Https;

import android.util.Log;

import com.nownews.mobile.Baselibs.Jsonserialize;
import com.nownews.mobile.Baselibs.RequestCacheUtil;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.Config.Constants;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomHttpURLConnection {
    private static String TAG = "CustomHttpUrlConnection";

    public CustomHttpURLConnection() {
    }

    public static String PostFromWebByHttpURLConnection(String authorization, String strUrl,
                                                        String nameValuePairs, RequestCacheUtil.Method method) {
        String result = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 設置是否從httpUrlConnection讀入，默認情況是true;
            conn.setDoInput(true);
            // 設置是否向httpUrlConnection輸出，因為這個是post請求，參數放在
            // http正文内，因此需要設圍true, 默認下是false;
            conn.setDoOutput(true);
            // 設置請求的方法為"POST"，默认是GET
            conn.setRequestMethod(method.name());
            conn.setInstanceFollowRedirects(true);
            // 設置超時
            conn.setConnectTimeout(40000);
            conn.setReadTimeout(40000);
            // Post 請求不能使用緩存
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type",
                    "application/json");
            conn.setRequestProperty("X-NOWnews-api",
                    authorization);
            if(Utility.DEBUG)Log.e(TAG, "Authorization:" + authorization);
            conn.setRequestProperty("locale", getLocale());
            // 連接，從上述第2條中url.openConnection()至此的配置必須要在connect之前完成
            conn.connect();
            if (!nameValuePairs.isEmpty() && nameValuePairs != null) {
                /** 寫入參數 **/
                OutputStream os = conn.getOutputStream();
                // 封裝寫給Server的數據（需要傳遞的參數）
                DataOutputStream dos = new DataOutputStream(os);
                // name是key值不能變，編碼方式使用UTF-8可以用中文
                dos.write(nameValuePairs.getBytes("UTF-8"));
                dos.close();
            }
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                InputStreamReader inStream = new InputStreamReader(in);
                BufferedReader buffer = new BufferedReader(inStream);
                String strLine = null;
                while ((strLine = buffer.readLine()) != null) {
                    result += strLine;
                }
            } else if (!conn.getHeaderFields().containsKey(Constants.HeaderFields.statuscode)) {
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode, Constants.HeaderFields.message, conn.getResponseCode(), "");
            } else {
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode, Constants.HeaderFields.message, conn.getHeaderField(Constants.HeaderFields.statuscode), conn.getHeaderField(Constants.HeaderFields.message));

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                Set<String> headerFieldsSet = headerFields.keySet();
                Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
                while (hearerFieldsIter.hasNext()) {
                    String headerFieldKey = hearerFieldsIter.next();
                    List<String> headerFieldValue = headerFields
                            .get(headerFieldKey);
                    StringBuilder sb = new StringBuilder();
                    for (String value : headerFieldValue) {
                        sb.append(value);
                        sb.append("");
                    }
                    System.out.println(headerFieldKey + "=" + sb.toString());
                }
            }
//            Log.e(TAG, "result:" + result);
            if (result.equalsIgnoreCase("[]")
                    || result.equalsIgnoreCase("{}")) {
                result = "";
            }

            return result;
        } catch (IOException ex) {
            Log.e(TAG, "PostFromWebByHttpURLConnection：" + ex.getMessage());
            ex.printStackTrace();
            result = Jsonserialize.Json(Constants.HeaderFields.statuscode,
                    Constants.HeaderFields.message,
                    Constants.StatusCode.No_Intent_Err, "");
            return result;
        }
    }

    public static String GetFromWebByHttpURLConnection(String authorization, String strUrl, RequestCacheUtil.Method method) {
        String result = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 設置請求的方法為"POST"，默认是GET
            conn.setRequestMethod(method.name());
            // 設置超時
            conn.setConnectTimeout(40000);
            conn.setReadTimeout(40000);
            // Post 請求不能使用緩存
            conn.setUseCaches(false);
            conn.setDoOutput(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type",
                    "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("X-NOWnews-api", authorization);
            if(Utility.DEBUG)Log.e(TAG, "Authorization:" + authorization);
            conn.setRequestProperty("locale", getLocale());

            Map<String, List<String>> headerFields1 = conn.getRequestProperties();
            Set<String> headerFieldsSet1 = headerFields1.keySet();
            Iterator<String> hearerFieldsIter1 = headerFieldsSet1.iterator();
            while (hearerFieldsIter1.hasNext()) {
                String headerFieldKey = hearerFieldsIter1.next();
                List<String> headerFieldValue = headerFields1
                        .get(headerFieldKey);
                StringBuilder sb = new StringBuilder();
                for (String value : headerFieldValue) {
                    sb.append(value);
                    sb.append("");
                }
                System.out.println(headerFieldKey + "=" + sb.toString());
            }
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                InputStreamReader inStream = new InputStreamReader(in);
                BufferedReader buffer = new BufferedReader(inStream);
                String strLine = null;
                while ((strLine = buffer.readLine()) != null) {
                    result += strLine;
                }
            } else if (!conn.getHeaderFields().containsKey(Constants.HeaderFields.statuscode)) {
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode, Constants.HeaderFields.message, conn.getResponseCode(), conn.getResponseMessage());
            } else {
                Log.e(TAG, "status:" + conn.getResponseCode());
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode,
                        Constants.HeaderFields.message,
                        conn.getHeaderField(Constants.HeaderFields.statuscode),
                        conn.getHeaderField(Constants.HeaderFields.message));

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                Set<String> headerFieldsSet = headerFields.keySet();
                Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
                while (hearerFieldsIter.hasNext()) {
                    String headerFieldKey = hearerFieldsIter.next();
                    List<String> headerFieldValue = headerFields
                            .get(headerFieldKey);
                    StringBuilder sb = new StringBuilder();
                    for (String value : headerFieldValue) {
                        sb.append(value);
                        sb.append("");
                    }
                    System.out.println(headerFieldKey + "=" + sb.toString());
                }
            }
            if (result.equalsIgnoreCase("[]")
                    || result.equalsIgnoreCase("{}")) {
                result = "";
            }
            return result;
        } catch (IOException ex) {
            Log.e(TAG, "GetFromWebByHttpURLConnection：" + ex.getMessage());
            ex.printStackTrace();
            result = Jsonserialize.Json(Constants.HeaderFields.statuscode,
                    Constants.HeaderFields.message,
                    Constants.StatusCode.No_Intent_Err, "");
            return result;
        }
    }

    public static String PutFromWebByHttpURLConnection(String authorization, String strUrl, String nameValuePairs) {
        String result = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 設置請求的方法為"POST"，默认是GET
            conn.setRequestMethod(RequestCacheUtil.Method.PUT.name());
            conn.setInstanceFollowRedirects(true);
            // 設置超時
            conn.setConnectTimeout(40000);
            conn.setReadTimeout(40000);
            // Post 請求不能使用緩存
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type",
                    "application/json");
            conn.setRequestProperty("X-NOWnews-api",
                    authorization);
            if(Utility.DEBUG)Log.e(TAG, "Authorization:" + authorization);
            conn.setRequestProperty("locale", getLocale());
            // 連接，從上述第2條中url.openConnection()至此的配置必須要在connect之前完成
            conn.connect();
            if (!nameValuePairs.isEmpty() && nameValuePairs != null) {
                /** 寫入參數 **/
                OutputStream os = conn.getOutputStream();
                // 封裝寫給Server的數據（需要傳遞的參數）
                DataOutputStream dos = new DataOutputStream(os);
                // name是key值不能變，編碼方式使用UTF-8可以用中文
                dos.write(nameValuePairs.getBytes("UTF-8"));
                dos.close();
            }
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                InputStreamReader inStream = new InputStreamReader(in);
                BufferedReader buffer = new BufferedReader(inStream);
                String strLine = null;
                while ((strLine = buffer.readLine()) != null) {
                    result += strLine;
                }
            } else if (!conn.getHeaderFields().containsKey(Constants.HeaderFields.statuscode)) {
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode, Constants.HeaderFields.message, conn.getResponseCode(), conn.getResponseMessage());
            } else {
                result = Jsonserialize.Json(Constants.HeaderFields.statuscode, Constants.HeaderFields.message, conn.getHeaderField(Constants.HeaderFields.statuscode), conn.getHeaderField(Constants.HeaderFields.message));

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                Set<String> headerFieldsSet = headerFields.keySet();
                Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
                while (hearerFieldsIter.hasNext()) {
                    String headerFieldKey = hearerFieldsIter.next();
                    List<String> headerFieldValue = headerFields
                            .get(headerFieldKey);
                    StringBuilder sb = new StringBuilder();
                    for (String value : headerFieldValue) {
                        sb.append(value);
                        sb.append("");
                    }
                    System.out.println(headerFieldKey + "=" + sb.toString());
                }
            }
            Log.e(TAG, "result:" + result);
            if (result.equalsIgnoreCase("[]")
                    || result.equalsIgnoreCase("{}")) {
                result = "";
            }
            return result;
        } catch (IOException ex) {
            Log.e(TAG, "PostFromWebByHttpURLConnection：" + ex.getMessage());
            ex.printStackTrace();
            result = Jsonserialize.Json(Constants.HeaderFields.statuscode,
                    Constants.HeaderFields.message,
                    Constants.StatusCode.No_Intent_Err, "");
            return result;
        }
    }

    private static String getLocale() {
        String locale = Locale.getDefault().toString().replace("_", "-");
        if (locale.equalsIgnoreCase("zh-TW")) {
            return locale;
        } else {
            return "en-US";
        }
    }
}

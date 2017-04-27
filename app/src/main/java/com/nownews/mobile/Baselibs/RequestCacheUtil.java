package com.nownews.mobile.Baselibs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.nownews.mobile.Config.Constants;
import com.nownews.mobile.Db.DBHelper;
import com.nownews.mobile.Db.dbcolumn.RequestCacheColumn;
import com.nownews.mobile.Https.HttpUtils;
import com.nownews.mobile.Utils.CommonUtil;
import com.nownews.mobile.Utils.MD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Request Data緩存處理
 */
// 整個調用方法就是非同步,這裡不需要非同步
public class RequestCacheUtil {
    private static final String TAG = "RequestCacheUtil";

    private static LinkedHashMap<String, SoftReference<String>> RequestCache = new LinkedHashMap<String, SoftReference<String>>(
            20);

    // [start] 公開方法
    public static String getRequestContent(Context context, String authorization, String RequestUrl,
                                           String content, String source_type,
                                           String content_type, boolean UseCache, Method method) {
        Log.e(TAG, RequestUrl);
        DBHelper dbHelper = DBHelper.getInstance(context);
        String md5 = MD5.encode(RequestUrl);
        // 緩存目錄
        /* true 為可用 */
        if (!CommonUtil.sdCardIsAvailable()) {
            String cachePath = context.getCacheDir().getAbsolutePath() + "/"
                    + md5; // data裡的緩存
            return getCacheRequest(authorization, RequestUrl, content,
                    cachePath, source_type, content_type, dbHelper, UseCache, method);
        } else {
            String imagePath = getExternalCacheDir(context) + File.separator
                    + md5; // SD卡
            return getCacheRequest(authorization, RequestUrl, content,
                    imagePath, source_type, content_type, dbHelper, UseCache, method);
        }
    }

    // [end]

    // [start] 私有方法

    /**
     * 取得在SD卡中的CACHE目錄
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    private static String getExternalCacheDir(Context context) {
        // android 2.2 以後才支持
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir().getPath() + File.separator
                    + "request";
        }

        // Before Froyo we need to construct the external cache dir ourselves
        // 2.2以前必須自己組路徑
        final String cacheDir = "/Android/data/" + context.getPackageName()
                + "/cache/request/";
        return Environment.getExternalStorageDirectory().getPath() + cacheDir;
    }

    private static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    private static String getCacheRequest(String authorization, String requestUrl,
                                          String content, String requestPath,
                                          String source_type, String content_type, DBHelper dbHelper,
                                          boolean useCache, Method method) {
        // TODO Auto-generated method stub
        String result = "";
        if (useCache) {
            Log.e("CCC", "使用暫存");
            result = getStringFromSoftReference(requestUrl);
            if (!result.equals(null) && !result.equals("")) {
                return result;
            }
            result = getStringFromLocal(requestPath, requestUrl, dbHelper);
            if (!result.equals(null) && !result.equals("")) {
                putStringForSoftReference(requestUrl, result);
                return result;
            }
        } else {
            Log.e("CCC", "不使用暫存");
        }
        result = postStringFromWeb(authorization, requestPath, requestUrl,
                content, source_type, content_type, dbHelper, method);
        return result;
    }

    private synchronized static void putStringForSoftReference(String requestUrl,
                                                               String result) {
        // TODO Auto-generated method stub
        SoftReference<String> referece = new SoftReference<String>(result);
        RequestCache.put(requestUrl, referece);
    }

    private static String postStringFromWeb(String authorization,
                                            String requestPath, String requestUrl,
                                            String content, String source_type,
                                            String content_type, DBHelper dbHelper, Method method) {
        // TODO Auto-generated method stub
        String result = "";
        result = HttpUtils.postByHttpURLConnection(authorization, requestUrl,
                content, method);
        Log.e(TAG, "result" + result);
        if (result.indexOf(Constants.HeaderFields.statuscode) == -1) {
            // 更新資料庫
            Cursor cursor = getStringFromDB(requestUrl, dbHelper);
            updateDB(cursor, requestUrl, source_type, content_type, dbHelper);
            saveFileByRequestPath(requestPath, result);
            putStringForSoftReference(requestUrl, result);
        }

        return result;
    }

    private synchronized static void saveFileByRequestPath(String requestPath, String result) {
        // TODO Auto-generated method stub
        deleteFileFromLocal(requestPath);
        saveFileForLocal(requestPath, result);
    }

    private synchronized static void saveFileForLocal(String requestPath, String result) {
        // TODO Auto-generated method stub
        File file = new File(requestPath);
        if (!file.exists()) {
            try {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
                FileOutputStream fout = new FileOutputStream(file);
                byte[] buffer = result.getBytes();
                fout.write(buffer);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 更新或新增Request Data至資料庫
    private synchronized static void updateDB(Cursor cursor, String requestUrl,
                                              String source_type, String content_type, DBHelper dbHelper) {
        if (cursor.moveToFirst()) {
            // 更新
            int id = cursor.getInt(cursor
                    .getColumnIndex(RequestCacheColumn._ID));
            long timestamp = System.currentTimeMillis();
            String SQL = "update " + RequestCacheColumn.TABLE_NAME + " set "
                    + RequestCacheColumn.Timestamp + "=" + timestamp
                    + " where " + RequestCacheColumn._ID + "=" + id;
            dbHelper.ExecSQL(SQL);
        } else {
            // 添加
            String SQL = "insert into " + RequestCacheColumn.TABLE_NAME + "("
                    + RequestCacheColumn.URL + ","
                    + RequestCacheColumn.SOURCE_TYPE + ","
                    + RequestCacheColumn.Content_type + ","
                    + RequestCacheColumn.Timestamp + ") values('" + requestUrl
                    + "','" + source_type + "','" + content_type + "','"
                    + System.currentTimeMillis() + "')";
            dbHelper.ExecSQL(SQL);
        }
    }

    private static String getStringFromSoftReference(String requestUrl) {
        if (RequestCache.containsKey(requestUrl)) {
            SoftReference<String> reference = RequestCache.get(requestUrl);
            String result = (String) reference.get();
            if (result != null && !result.equals("")) {
                return result;
            }
        }
        return "";
    }

    // 判斷CACHE資料是否過期
    private static String getStringFromLocal(String requestPath,
                                             String requestUrl, DBHelper dbHelper) {
        String result = "";
        Cursor cursor = getStringFromDB(requestUrl, dbHelper);
        try {
            if (cursor.moveToFirst()) {
                Long timestamp = cursor.getLong(cursor
                        .getColumnIndex(RequestCacheColumn.Timestamp));
                String strContentType = cursor.getString(cursor
                        .getColumnIndex(RequestCacheColumn.Content_type));
                long span = getSpanTimeFromConfigs(strContentType);
                long nowTime = System.currentTimeMillis();
                if ((nowTime - timestamp) > span * 60 * 1000) {
                    // 過期
                    deleteFileFromLocal(requestPath);
                } else {
                    // 沒過期
                    result = getFileFromLocal(requestPath);
                }
            }
        } catch (IllegalStateException ex) {
        } catch (NullPointerException ex) {
        } catch (Exception ex) {
        }

        return result;
    }

    /**
     * 從DB中查詢數據
     *
     * @param requestUrl
     * @param dbHelper
     * @return
     */
    private static Cursor getStringFromDB(String requestUrl, DBHelper dbHelper) {
        String SQL = "select * from " + RequestCacheColumn.TABLE_NAME
                + " where " + RequestCacheColumn.URL + "='" + requestUrl + "'";
        Cursor cursor = dbHelper.rawQuery(SQL, new String[]{});
        return cursor;
    }

    private static String getFileFromLocal(String requestPath) {
        // TODO Auto-generated method stub
        File file = new File(requestPath);
        String result = "";
        if (file.exists()) {
            FileInputStream fileIn;
            try {
                fileIn = new FileInputStream(file);

                int length = fileIn.available();
                byte[] buffer = new byte[length];
                fileIn.read(buffer);
//                result = EncodingUtils.getString(buffer, "UTF-8");
//                android 6.0 test
                result = new String(buffer, "UTF-8");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }
        return "";
    }

    private static void deleteFileFromLocal(String requestPath) {
        // TODO Auto-generated method stub
        File file = new File(requestPath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 根據類型取得緩存時間
     *
     * @param str
     * @return
     */
    private static long getSpanTimeFromConfigs(String str) {
        long span = 0;
        if (str.equals(Constants.DBContentType.Content_list)) {
            span = Constants.DBContentCacheTime.Content_ListCacheTime;
        } else if (str.equals(Constants.DBContentType.Content_content)) {
            span = Constants.DBContentCacheTime.Content_ContentCacheTime;
        } else if (str.equals(Constants.DBContentType.Discuss)) {
            span = Constants.DBContentCacheTime.DiscussCacheTime;
        } else {
            span = Constants.DBContentCacheTime.Content_DefaultCacheTime;
        }
        return span;
    }
    // [end]

    public enum Method {
        GET,
        POST,
        DELETE,
        PUT
    }
}

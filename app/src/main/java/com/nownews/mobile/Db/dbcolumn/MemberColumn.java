package com.nownews.mobile.Db.dbcolumn;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class MemberColumn extends DatabaseColumn {
    public final static String TABLE_NAME = "Member";
    public final static String TYPE = "type";
    public final static String INFO = "info";


    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_NAME);
    private static final Map<String, String> mColumnMap = new HashMap<String, String>();

    static {
        mColumnMap.put(_ID, "integer primary key autoincrement");
        mColumnMap.put(TYPE, "text");
        mColumnMap.put(INFO, "text");
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public Uri getTableContent() {
        // TODO Auto-generated method stub
        return CONTENT_URI;
    }

    @Override
    protected Map<String, String> getTableMap() {
        // TODO Auto-generated method stub
        return mColumnMap;
    }
}

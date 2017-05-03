package com.nownews.mobile.Db.biz;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nownews.mobile.Db.DBHelper;
import com.nownews.mobile.Db.dbcolumn.MemberColumn;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class MemberDB {
    private DBHelper dbHelper;

    public MemberDB(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public void insertTokenSQL(String data) {
        String SQL = "insert into " + MemberColumn.TABLE_NAME + "("
                + MemberColumn.TYPE + ", " + MemberColumn.INFO + ") values('token', '"
                + data + "')";
        Log.e("SQL", SQL);
        dbHelper.ExecSQL(SQL);
    }

    public void insertUserInfoSQL(String data) {
        String SQL = "insert into " + MemberColumn.TABLE_NAME + "("
                + MemberColumn.TYPE + ", " + MemberColumn.INFO + ") values('info', '"
                + data + "')";
        Log.e("SQL", SQL);
        dbHelper.ExecSQL(SQL);
    }

    public void Update(String type, String data) {
        if (type.equalsIgnoreCase("token")) {
            dbHelper.delete(MemberColumn.TABLE_NAME, MemberColumn.TYPE + "='token'");
            insertTokenSQL(data);
        } else {
            dbHelper.delete(MemberColumn.TABLE_NAME, MemberColumn.TYPE + "='info'");
            insertUserInfoSQL(data);
        }
    }

    public Cursor querySQL() {
        return dbHelper.query(MemberColumn.TABLE_NAME,        //資料表名稱
                new String[]{MemberColumn.TYPE, MemberColumn.INFO},
                null, null);
    }

    public void Clear() {
        dbHelper.deleteALL(MemberColumn.TABLE_NAME);
    }

    public void dbClose() {
        dbHelper.closeDb();
    }
}

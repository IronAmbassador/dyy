package com.white.news.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.white.news.entity.HistoryInfo;

import java.util.ArrayList;
import java.util.List;

public class HistoryDbHelper extends SQLiteOpenHelper {
    private static HistoryDbHelper sHelper;
    private static final String DB_NAME = "history.db";   // 数据库名
    private static final int VERSION = 1;    // 版本号

    // 必须实现其中一个构方法
    public HistoryDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 创建单例，供使用调用该类里面的的增删改查的方法
    public synchronized static HistoryDbHelper getInstance(Context context) {
        if (null == sHelper) {
            sHelper = new HistoryDbHelper(context, DB_NAME, null, VERSION);
        }
        return sHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建history_table表
        db.execSQL("create table history_table(history_id integer primary key autoincrement, " +
                "username text," +       // 用户名
                "uniquekey text," +      // 新闻ID
                "new_json text" +       // 新闻数据
                ")");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * 添加新闻
     */
    public int addHistory(String username, String uniquekey, String new_json) {
        if (!isHistory(username, uniquekey)) {
            // 获取SQLiteDatabase实例
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            // 填充占位符
            values.put("username", username);
            values.put("uniquekey", uniquekey);
            values.put("new_json", new_json);
            String nullColumnHack = "values(null,?,?,?)";
            // 执行
            int insert = (int) db.insert("history_table", nullColumnHack, values);
            db.close();
            return insert;
        }
        return 0;
    }

    /**
     * 查询历史记录是否存在
     */
    @SuppressLint("Range")
    public boolean isHistory(String username, String uniquekey) {
        // 获取SQLiteDatabase实例
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select history_id from history_table where uniquekey=?";
        Cursor cursor;

        if (username == null) {
            sql += " and username is null";
            cursor = db.rawQuery(sql, new String[]{uniquekey});
        } else {
            sql += " and username=?";
            cursor = db.rawQuery(sql, new String[]{uniquekey, username});
        }
        boolean isAdd = cursor.moveToNext();
        cursor.close();
        db.close();
        return isAdd;
    }


    /**
     * 查询所有历史记录
     */
    @SuppressLint("Range")
    public List<HistoryInfo> queryHistoryListData(String username) {
        // 获取SQLiteDatabase实例
        SQLiteDatabase db = getReadableDatabase();
        List<HistoryInfo> list = new ArrayList<>();
        String sql = "select history_id,uniquekey,username,new_json from history_table";
        Cursor cursor;
        if (username == null || username == "") {
            cursor = db.rawQuery(sql, null);
        } else {
            cursor = db.rawQuery(sql + " where username=?", new String[]{username});
        }
        while (cursor.moveToNext()) {
            int history_id = cursor.getInt(cursor.getColumnIndex("history_id"));
            String uniquekey = cursor.getString(cursor.getColumnIndex("uniquekey"));
            String name = cursor.getString(cursor.getColumnIndex("username"));
            String new_json = cursor.getString(cursor.getColumnIndex("new_json"));
            list.add(new HistoryInfo(history_id, uniquekey, name, new_json));
        }
        cursor.close();
        db.close();
        return list;
    }

}

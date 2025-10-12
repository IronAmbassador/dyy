package com.white.news.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.white.news.entity.UserInfo;

public class UserDbHelper extends SQLiteOpenHelper {
    private static UserDbHelper sHelper;
    private static final String DB_NAME = "user.db";   // 数据库名
    private static final int VERSION = 1;    // 版本号

    public UserDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 创建单例，供使用条用改类里面的增删改查的方法
    public synchronized static UserDbHelper getInstance(Context context) {
        if (null == sHelper) {
            sHelper = new UserDbHelper(context, DB_NAME, null, VERSION);
        }
        return sHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建user_table表
        db.execSQL("create table user_table(user_id integer primary key autoincrement, " +
                "username text," +       // 用户名
                "password text," +       // 用户密码
                "nickname text" +      // 用户昵称
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 注册用户
     *
     * @param username
     * @param password
     * @return
     */
    public int register(String username, String password) {
        // 获取SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // 填充占位符
        values.put("username", username);
        values.put("password", password);
        values.put("nickname", "这个家伙很懒，什么都没留下~~");
        String nullColumnHack = "values(null,?,?,?)";
        // 执行
        int insert = (int) db.insert("user_table", nullColumnHack, values);
        db.close();
        return insert;
    }

    /**
     * 登录  根据用户名查找用户
     */
    @SuppressLint("Range")
    public UserInfo login(String username) {
        SQLiteDatabase db = getReadableDatabase();
        UserInfo userInfo = null;
        String sql = "select user_id, username, password, nickname from user_table where username=?";
        String[] selectionArgs = {username};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if (cursor.moveToNext()) {
            int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
            String name = cursor.getString(cursor.getColumnIndex("username"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            userInfo = new UserInfo(user_id, name, nickname, password);
        }
        cursor.close();
        db.close();
        return userInfo;
    }

    /**
     * 根据用户唯一 _id来修改密码
     */
    public int updatePwd(int user_id, String password) {
        // 获取SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        // 填充占位符
        ContentValues values = new ContentValues();
        values.put("password", password);
        // 执行SQL
        int update = db.update("user_table", values, " user_id=?", new String[]{user_id + ""});
        // 关闭数据库连接
        db.close();
        return update;
    }

    /**
     * 根据用户 唯一id删除用户
     */
    public int delete(int user_id) {
        // 获取SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        // 执行SQL
        int delete = db.delete("user_table", " user_id=?", new String[]{user_id + ""});
        // 关闭数据库连接
        db.close();
        return delete;
    }
}

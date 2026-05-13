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
     * 用户名最小长度
     */
    private static final int MIN_USERNAME_LENGTH = 3;

    /**
     * 用户名最大长度
     */
    private static final int MAX_USERNAME_LENGTH = 16;

    /**
     * 密码最小长度
     */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * 密码最大长度
     */
    private static final int MAX_PASSWORD_LENGTH = 20;

    /**
     * 验证用户名格式
     *
     * @param username
     * @return 验证结果码: 0=成功, -1=为空, -2=长度不足, -3=长度过长, -4=包含非法字符
     */
    public int validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return -1;
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            return -2;
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return -3;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return -4;
        }
        return 0;
    }

    /**
     * 验证密码格式
     *
     * @param password
     * @return 验证结果码: 0=成功, -1=为空, -2=长度不足, -3=长度过长
     */
    public int validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return -1;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return -2;
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return -3;
        }
        return 0;
    }

    /**
     * 检查用户名是否已存在
     *
     * @param username
     * @return true=已存在, false=不存在
     */
    @SuppressLint("Range")
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select username from user_table where username=?";
        String[] selectionArgs = {username};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * 注册用户
     *
     * @param username
     * @param password
     * @return 注册结果: >0=成功(返回用户ID), -1=用户名验证失败, -2=密码验证失败, -3=用户名已存在
     */
    public int register(String username, String password) {
        // 验证用户名
        int usernameResult = validateUsername(username);
        if (usernameResult != 0) {
            return -1;
        }

        // 验证密码
        int passwordResult = validatePassword(password);
        if (passwordResult != 0) {
            return -2;
        }

        // 检查用户名是否已存在
        if (isUsernameExists(username)) {
            return -3;
        }

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
     * 登录 - 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息，如果不存在返回 null
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
     * 登录验证 - 验证用户名和密码是否正确
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证结果码: 0=成功, -1=用户名不存在, -2=密码错误, -3=用户名为空, -4=密码为空
     */
    public int loginVerify(String username, String password) {
        // 验证用户名不为空
        if (username == null || username.isEmpty()) {
            return -3;
        }

        // 验证密码不为空
        if (password == null || password.isEmpty()) {
            return -4;
        }

        // 查询用户
        UserInfo userInfo = login(username);
        if (userInfo == null) {
            return -1;
        }

        // 验证密码
        if (!password.equals(userInfo.getPassword())) {
            return -2;
        }

        return 0;
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

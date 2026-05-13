package com.white.news.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.white.news.entity.UserInfo;

public class TestUtils {

    public static void clearUserSession(Context context) {
        UserInfo.setsUserInfo(null);

        SharedPreferences prefs = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static void simulateLogin(Context context, String username, String password, int userId) {
        UserInfo userInfo = new UserInfo(userId, username, "测试昵称", password);
        UserInfo.setsUserInfo(userInfo);

        SharedPreferences prefs = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("username", username)
                .putString("password", password)
                .putBoolean("is_login", true)
                .apply();
    }

    public static String generateUniqueUsername() {
        return "testuser_" + System.currentTimeMillis();
    }

    public static String generateUniqueNewsKey() {
        return "news_key_" + System.currentTimeMillis();
    }
}

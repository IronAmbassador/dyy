package com.white.news.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class UserDbHelperTest {
    private UserDbHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = UserDbHelper.getInstance(context);
        clearDatabase();
    }

    @After
    public void tearDown() {
        clearDatabase();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void clearDatabase() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM user_table");
            db.close();
        } catch (Exception e) {
            // 忽略清除错误
        }
    }

    @Test
    public void testSingleton() {
        UserDbHelper instance1 = UserDbHelper.getInstance(context);
        UserDbHelper instance2 = UserDbHelper.getInstance(context);
        assertEquals(instance1, instance2);
    }

    @Test
    public void testRegister() {
        String testUsername = "testuser";
        String testPassword = "testpass123";

        int result = dbHelper.register(testUsername, testPassword);
        assertTrue("注册应该成功，返回值应大于0", result > 0);
    }

    @Test
    public void testLogin_Success() {
        String testUsername = "logintest";
        String testPassword = "password123";

        int registerResult = dbHelper.register(testUsername, testPassword);
        assertTrue("注册应该成功", registerResult > 0);

        var userInfo = dbHelper.login(testUsername);
        assertNotNull("登录成功应该返回用户信息", userInfo);
        assertEquals("用户名应该匹配", testUsername, userInfo.getUsername());
        assertEquals("密码应该匹配", testPassword, userInfo.getPassword());
    }

    @Test
    public void testLogin_Failure_WrongPassword() {
        String testUsername = "loginfail";
        String correctPassword = "correct123";
        String wrongPassword = "wrong123";

        dbHelper.register(testUsername, correctPassword);

        var userInfo = dbHelper.login(testUsername);
        assertNotNull(userInfo);
        assertFalse("密码不匹配时登录应该返回false", userInfo.getPassword().equals(wrongPassword));
    }

    @Test
    public void testLogin_Failure_UserNotExist() {
        var userInfo = dbHelper.login("nonexistent_user_12345");
        assertNull("不存在的用户应该返回null", userInfo);
    }

    @Test
    public void testUpdatePassword() {
        String testUsername = "updatetest";
        String oldPassword = "oldpass123";
        String newPassword = "newpass123";

        dbHelper.register(testUsername, oldPassword);

        var userInfo = dbHelper.login(testUsername);
        assertNotNull(userInfo);

        int result = dbHelper.updatePwd(userInfo.getUser_id(), newPassword);
        assertTrue("密码更新应该成功", result > 0);

        var updatedUser = dbHelper.login(testUsername);
        assertEquals("新密码应该匹配", newPassword, updatedUser.getPassword());
    }

    @Test
    public void testDeleteUser() {
        String testUsername = "deletetest";
        String testPassword = "deletepass";

        dbHelper.register(testUsername, testPassword);

        var userInfo = dbHelper.login(testUsername);
        assertNotNull("用户应该存在", userInfo);

        int result = dbHelper.delete(userInfo.getUser_id());
        assertTrue("删除应该成功", result > 0);

        var deletedUser = dbHelper.login(testUsername);
        assertNull("删除后用户应该不存在", deletedUser);
    }
}

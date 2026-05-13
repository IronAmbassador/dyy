package com.white.news.testdesign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.white.news.db.UserDbHelper;
import com.white.news.entity.UserInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录功能 - 正交表测试 (Orthogonal Array Testing)
 *
 * 因素和水平:
 * ┌─────────────┬────────────────────────────────────────────┐
 * │ 因素 A       │ 用户名状态                                   │
 * │   - A1      │ 正确                                        │
 * │   - A2      │ 用户名不存在                                 │
 * │   - A3      │ 用户名为空                                   │
 * ├─────────────┼────────────────────────────────────────────┤
 * │ 因素 B       │ 密码状态                                     │
 * │   - B1      │ 正确                                        │
 * │   - B2      │ 密码错误                                    │
 * │   - B3      │ 密码为空                                    │
 * └─────────────┴────────────────────────────────────────────┘
 *
 * 正交表 L9(3^4):
 * ┌─────┬─────┬─────┬─────┐
 * │ 1   │ 2   │ 3   │ 4   │
 * ├─────┼─────┼─────┼─────┤
 * │ A1  │ B1  │ A1  │ B1  │ → A1B1 (正确用户名,正确密码) → 登录成功 ✓
 * │ A1  │ B2  │ B2  │ B2  │ → A1B2 (正确用户名,错误密码) → 登录失败
 * │ A1  │ B3  │ B3  │ B3  │ → A1B3 (正确用户名,空密码)  → 登录失败
 * │ A2  │ B1  │ B2  │ B3  │ → A2B1 (不存在用户,正确密码) → 登录失败
 * │ A2  │ B2  │ B3  │ A1  │ → A2B2 (不存在用户,错误密码) → 登录失败
 * │ A2  │ B3  │ A1  │ B2  │ → A2B3 (不存在用户,空密码)  → 登录失败
 * │ A3  │ B1  │ B3  │ A2  │ → A3B1 (空用户名,正确密码)  → 登录失败
 * │ A3  │ B2  │ A1  │ B3  │ → A3B2 (空用户名,错误密码)  → 登录失败
 * │ A3  │ B3  │ B1  │ A1  │ → A3B3 (空用户名,空密码)    → 登录失败
 * └─────┴─────┴─────┴─────┘
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 33)
public class LoginOrthogonalTest {
    private UserDbHelper dbHelper;
    private Context context;
    private static final String CORRECT_USERNAME = "testuser_login";
    private static final String CORRECT_PASSWORD = "correctpass";

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = UserDbHelper.getInstance(context);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM user_table");

        // 预先注册一个正确用户
        dbHelper.register(CORRECT_USERNAME, CORRECT_PASSWORD);
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * 正交表第1列: A1B1 (正确用户名, 正确密码)
     * 预期结果: 登录成功，返回 0
     */
    @Test
    public void testLogin_Orthogonal_A1B1_CorrectAll() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME, CORRECT_PASSWORD);
        assertEquals("正确用户名和密码应该返回 0", 0, result);
    }

    /**
     * 正交表第2列: A1B2 (正确用户名, 错误密码)
     * 预期结果: 登录失败，返回 -2 (密码错误)
     */
    @Test
    public void testLogin_Orthogonal_A1B2_CorrectUser_WrongPassword() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME, "wrongpass");
        assertEquals("正确用户名错误密码应该返回 -2", -2, result);
    }

    /**
     * 正交表第3列: A1B3 (正确用户名, 空密码)
     * 预期结果: 登录失败，返回 -4 (密码为空)
     */
    @Test
    public void testLogin_Orthogonal_A1B3_CorrectUser_EmptyPassword() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME, "");
        assertEquals("正确用户名空密码应该返回 -4", -4, result);
    }

    /**
     * 正交表第4列: A2B1 (不存在用户, 正确密码)
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Orthogonal_A2B1_NoUser_CorrectPassword() {
        int result = dbHelper.loginVerify("nonexistent_user_12345", CORRECT_PASSWORD);
        assertEquals("不存在的用户应该返回 -1", -1, result);
    }

    /**
     * 正交表第5列: A2B2 (不存在用户, 错误密码)
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Orthogonal_A2B2_NoUser_WrongPassword() {
        int result = dbHelper.loginVerify("nonexistent_user_12345", "wrongpass");
        assertEquals("不存在的用户应该返回 -1", -1, result);
    }

    /**
     * 正交表第6列: A2B3 (不存在用户, 空密码)
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Orthogonal_A2B3_NoUser_EmptyPassword() {
        int result = dbHelper.loginVerify("nonexistent_user_12345", "");
        assertEquals("不存在的用户应该返回 -1", -1, result);
    }

    /**
     * 正交表第7列: A3B1 (空用户名, 正确密码)
     * 预期结果: 登录失败，返回 -3 (用户名为空)
     */
    @Test
    public void testLogin_Orthogonal_A3B1_EmptyUser_CorrectPassword() {
        int result = dbHelper.loginVerify("", CORRECT_PASSWORD);
        assertEquals("空用户名应该返回 -3", -3, result);
    }

    /**
     * 正交表第8列: A3B2 (空用户名, 错误密码)
     * 预期结果: 登录失败，返回 -3 (用户名为空)
     */
    @Test
    public void testLogin_Orthogonal_A3B2_EmptyUser_WrongPassword() {
        int result = dbHelper.loginVerify("", "wrongpass");
        assertEquals("空用户名应该返回 -3", -3, result);
    }

    /**
     * 正交表第9列: A3B3 (空用户名, 空密码)
     * 预期结果: 登录失败，返回 -3 (用户名为空)
     */
    @Test
    public void testLogin_Orthogonal_A3B3_EmptyAll() {
        int result = dbHelper.loginVerify("", "");
        assertEquals("空用户名和空密码应该返回 -3", -3, result);
    }

    // ==================== 补充边界测试 ====================

    /**
     * 边界测试: 用户名正确, 密码部分正确
     * 预期结果: 登录失败，返回 -2 (密码错误)
     */
    @Test
    public void testLogin_Boundary_PartialPassword() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME, "correctpa");
        assertEquals("部分密码应该返回 -2", -2, result);
    }

    /**
     * 边界测试: 用户名多一个字符
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Boundary_UsernamePlusOne() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME + "x", CORRECT_PASSWORD);
        assertEquals("用户名多一个字符应该返回 -1", -1, result);
    }

    /**
     * 边界测试: 用户名少一个字符
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Boundary_UsernameMinusOne() {
        String shortUsername = CORRECT_USERNAME.substring(0, CORRECT_USERNAME.length() - 1);
        int result = dbHelper.loginVerify(shortUsername, CORRECT_PASSWORD);
        assertEquals("用户名少一个字符应该返回 -1", -1, result);
    }

    /**
     * 边界测试: 密码多一个字符
     * 预期结果: 登录失败，返回 -2 (密码错误)
     */
    @Test
    public void testLogin_Boundary_PasswordPlusOne() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME, CORRECT_PASSWORD + "x");
        assertEquals("密码多一个字符应该返回 -2", -2, result);
    }

    /**
     * 边界测试: 密码少一个字符
     * 预期结果: 登录失败，返回 -2 (密码错误)
     */
    @Test
    public void testLogin_Boundary_PasswordMinusOne() {
        String shortPassword = CORRECT_PASSWORD.substring(0, CORRECT_PASSWORD.length() - 1);
        int result = dbHelper.loginVerify(CORRECT_USERNAME, shortPassword);
        assertEquals("密码少一个字符应该返回 -2", -2, result);
    }

    /**
     * 边界测试: 用户名大小写敏感
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Boundary_CaseSensitive() {
        int result = dbHelper.loginVerify(CORRECT_USERNAME.toUpperCase(), CORRECT_PASSWORD);
        assertEquals("用户名大写应该返回 -1", -1, result);
    }

    /**
     * 额外测试: 空格用户名前后
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Extra_SpacePadding() {
        int result = dbHelper.loginVerify(" " + CORRECT_USERNAME + " ", CORRECT_PASSWORD);
        assertEquals("带空格的用户名应该返回 -1", -1, result);
    }

    /**
     * 额外测试: SQL注入尝试
     * 预期结果: 登录失败，返回 -1 (用户名不存在)
     */
    @Test
    public void testLogin_Extra_SQLInjection() {
        int result = dbHelper.loginVerify("' OR '1'='1", CORRECT_PASSWORD);
        assertEquals("SQL注入应该返回 -1", -1, result);
    }

    /**
     * 额外测试: 纯数字用户名
     * 预期结果: 注册成功，登录成功
     */
    @Test
    public void testLogin_Extra_NumericUsername() {
        String numericUsername = "123456";

        int registerResult = dbHelper.register(numericUsername, CORRECT_PASSWORD);
        assertTrue("纯数字用户名应该注册成功", registerResult > 0);

        int loginResult = dbHelper.loginVerify(numericUsername, CORRECT_PASSWORD);
        assertEquals("纯数字用户名登录应该成功", 0, loginResult);
    }
}

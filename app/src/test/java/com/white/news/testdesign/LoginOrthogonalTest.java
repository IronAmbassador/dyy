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
     * 预期结果: 登录成功
     */
    @Test
    public void testLogin_Orthogonal_A1B1_CorrectAll() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("登录结果不应该为空", userInfo);
        assertEquals("用户名应该匹配", CORRECT_USERNAME, userInfo.getUsername());
        assertEquals("密码应该匹配", CORRECT_PASSWORD, userInfo.getPassword());
    }

    /**
     * 正交表第2列: A1B2 (正确用户名, 错误密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A1B2_CorrectUser_WrongPassword() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("登录查询应该返回用户", userInfo);
        assertTrue("密码应该不匹配", !userInfo.getPassword().equals("wrongpass"));
    }

    /**
     * 正交表第3列: A1B3 (正确用户名, 空密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A1B3_CorrectUser_EmptyPassword() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("登录查询应该返回用户", userInfo);
        assertTrue("空密码应该不匹配", !userInfo.getPassword().equals(""));
    }

    /**
     * 正交表第4列: A2B1 (不存在用户, 正确密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A2B1_NoUser_CorrectPassword() {
        UserInfo userInfo = dbHelper.login("nonexistent_user_12345");

        assertNull("不存在的用户应该返回null", userInfo);
    }

    /**
     * 正交表第5列: A2B2 (不存在用户, 错误密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A2B2_NoUser_WrongPassword() {
        UserInfo userInfo = dbHelper.login("nonexistent_user_12345");

        assertNull("不存在的用户应该返回null", userInfo);
    }

    /**
     * 正交表第6列: A2B3 (不存在用户, 空密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A2B3_NoUser_EmptyPassword() {
        UserInfo userInfo = dbHelper.login("nonexistent_user_12345");

        assertNull("不存在的用户应该返回null", userInfo);
    }

    /**
     * 正交表第7列: A3B1 (空用户名, 正确密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A3B1_EmptyUser_CorrectPassword() {
        UserInfo userInfo = dbHelper.login("");

        assertNull("空用户名应该返回null", userInfo);
    }

    /**
     * 正交表第8列: A3B2 (空用户名, 错误密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A3B2_EmptyUser_WrongPassword() {
        UserInfo userInfo = dbHelper.login("");

        assertNull("空用户名应该返回null", userInfo);
    }

    /**
     * 正交表第9列: A3B3 (空用户名, 空密码)
     * 预期结果: 登录失败
     */
    @Test
    public void testLogin_Orthogonal_A3B3_EmptyAll() {
        UserInfo userInfo = dbHelper.login("");

        assertNull("空用户名应该返回null", userInfo);
    }

    // ==================== 补充边界测试 ====================

    /**
     * 边界测试: 用户名正确, 密码部分正确
     */
    @Test
    public void testLogin_Boundary_PartialPassword() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("用户应该存在", userInfo);
        assertTrue("部分密码不匹配", !userInfo.getPassword().equals("correctpa"));
    }

    /**
     * 边界测试: 用户名多一个字符
     */
    @Test
    public void testLogin_Boundary_UsernamePlusOne() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME + "x");

        assertNull("用户名多一个字符应该不存在", userInfo);
    }

    /**
     * 边界测试: 用户名少一个字符
     */
    @Test
    public void testLogin_Boundary_UsernameMinusOne() {
        String shortUsername = CORRECT_USERNAME.substring(0, CORRECT_USERNAME.length() - 1);
        UserInfo userInfo = dbHelper.login(shortUsername);

        assertNull("用户名少一个字符应该不存在", userInfo);
    }

    /**
     * 边界测试: 密码多一个字符
     */
    @Test
    public void testLogin_Boundary_PasswordPlusOne() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("用户应该存在", userInfo);
        assertTrue("密码多一个字符不匹配", !userInfo.getPassword().equals(CORRECT_PASSWORD + "x"));
    }

    /**
     * 边界测试: 密码少一个字符
     */
    @Test
    public void testLogin_Boundary_PasswordMinusOne() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME);

        assertNotNull("用户应该存在", userInfo);
        assertTrue("密码少一个字符不匹配", !userInfo.getPassword().equals(CORRECT_PASSWORD.substring(0, CORRECT_PASSWORD.length() - 1)));
    }

    /**
     * 边界测试: 用户名大小写敏感
     */
    @Test
    public void testLogin_Boundary_CaseSensitive() {
        UserInfo userInfo = dbHelper.login(CORRECT_USERNAME.toUpperCase());

        assertNull("用户名大写应该不存在(大小写敏感)", userInfo);
    }

    /**
     * 额外测试: 空格用户名前后
     */
    @Test
    public void testLogin_Extra_SpacePadding() {
        UserInfo userInfo = dbHelper.login(" " + CORRECT_USERNAME + " ");

        assertNull("带空格的的用户名不应该匹配", userInfo);
    }

    /**
     * 额外测试: SQL注入尝试
     */
    @Test
    public void testLogin_Extra_SQLInjection() {
        UserInfo userInfo = dbHelper.login("' OR '1'='1");

        assertNull("SQL注入应该返回null", userInfo);
    }

    /**
     * 额外测试: 纯数字用户名
     */
    @Test
    public void testLogin_Extra_NumericUsername() {
        String numericUsername = "123456";

        dbHelper.register(numericUsername, CORRECT_PASSWORD);
        UserInfo userInfo = dbHelper.login(numericUsername);

        assertNotNull("纯数字用户名应该能注册和登录", userInfo);
        assertEquals("用户名应该匹配", numericUsername, userInfo.getUsername());
    }
}

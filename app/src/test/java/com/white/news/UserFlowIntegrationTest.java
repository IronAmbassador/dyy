package com.white.news;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;
import com.white.news.db.HistoryDbHelper;
import com.white.news.db.UserDbHelper;
import com.white.news.entity.HistoryInfo;
import com.white.news.entity.NewsInfo;
import com.white.news.entity.UserInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class UserFlowIntegrationTest {
    private Context context;
    private UserDbHelper userDbHelper;
    private HistoryDbHelper historyDbHelper;
    private Gson gson;

    private String testUsername;
    private String testPassword;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        userDbHelper = UserDbHelper.getInstance(context);
        historyDbHelper = HistoryDbHelper.getInstance(context);
        gson = new Gson();

        testUsername = "flowtest_" + System.currentTimeMillis();
        testPassword = "flowpassword123";

        UserInfo.setsUserInfo(null);

        // 清理数据库
        try {
            userDbHelper.getWritableDatabase().execSQL("DELETE FROM user_table");
            historyDbHelper.getWritableDatabase().execSQL("DELETE FROM history_table");
        } catch (Exception e) {
            // 忽略
        }
    }

    @After
    public void tearDown() {
        if (userDbHelper != null) {
            userDbHelper.close();
        }
        if (historyDbHelper != null) {
            historyDbHelper.close();
        }
        UserInfo.setsUserInfo(null);
    }

    @Test
    public void testCompleteUserFlow_RegistrationAndLogin() {
        // 1. 注册新用户
        int registerResult = userDbHelper.register(testUsername, testPassword);
        assertTrue("注册应该成功", registerResult > 0);

        // 2. 尝试登录 - 密码正确
        UserInfo userInfo = userDbHelper.login(testUsername);
        assertNotNull("登录应该成功", userInfo);
        assertEquals("用户名应该匹配", testUsername, userInfo.getUsername());
        assertEquals("密码应该匹配", testPassword, userInfo.getPassword());

        // 3. 保存用户信息到单例（模拟登录状态）
        UserInfo.setsUserInfo(userInfo);
        assertNotNull("单例应该已设置", UserInfo.getsUserInfo());
        assertEquals("单例用户名应该匹配", testUsername, UserInfo.getsUserInfo().getUsername());
    }

    @Test
    public void testCompleteUserFlow_BrowsingHistory() {
        // 1. 注册并登录用户
        userDbHelper.register(testUsername, testPassword);
        UserInfo userInfo = userDbHelper.login(testUsername);
        UserInfo.setsUserInfo(userInfo);

        // 2. 创建模拟新闻数据
        NewsInfo.ResultDTO.DataDTO news1 = new NewsInfo.ResultDTO.DataDTO();
        news1.setUniquekey("news_001");
        news1.setTitle("测试新闻1");
        news1.setAuthor_name("作者1");
        news1.setUrl("http://example.com/1");

        NewsInfo.ResultDTO.DataDTO news2 = new NewsInfo.ResultDTO.DataDTO();
        news2.setUniquekey("news_002");
        news2.setTitle("测试新闻2");
        news2.setAuthor_name("作者2");
        news2.setUrl("http://example.com/2");

        // 3. 模拟浏览新闻 - 添加历史记录
        String json1 = gson.toJson(news1);
        String json2 = gson.toJson(news2);

        int result1 = historyDbHelper.addHistory(userInfo.getUsername(), news1.getUniquekey(), json1);
        int result2 = historyDbHelper.addHistory(userInfo.getUsername(), news2.getUniquekey(), json2);

        assertTrue("添加第一条历史记录应该成功", result1 > 0);
        assertTrue("添加第二条历史记录应该成功", result2 > 0);

        // 4. 查询该用户的历史记录
        List<HistoryInfo> historyList = historyDbHelper.queryHistoryListData(userInfo.getUsername());
        assertNotNull("历史记录列表不应该为空", historyList);
        assertEquals("应该有两调历史记录", 2, historyList.size());

        // 5. 验证历史记录内容
        boolean foundNews1 = false;
        boolean foundNews2 = false;
        for (HistoryInfo history : historyList) {
            if (history.getNew_json().contains("测试新闻1")) {
                foundNews1 = true;
            }
            if (history.getNew_json().contains("测试新闻2")) {
                foundNews2 = true;
            }
        }
        assertTrue("应该找到新闻1的记录", foundNews1);
        assertTrue("应该找到新闻2的记录", foundNews2);
    }

    @Test
    public void testCompleteUserFlow_UpdatePassword() {
        // 1. 注册用户
        userDbHelper.register(testUsername, testPassword);
        UserInfo userInfo = userDbHelper.login(testUsername);

        // 2. 修改密码
        String newPassword = "newpassword456";
        int updateResult = userDbHelper.updatePwd(userInfo.getUser_id(), newPassword);
        assertTrue("密码修改应该成功", updateResult > 0);

        // 3. 使用新密码登录
        UserInfo updatedUserInfo = userDbHelper.login(testUsername);
        assertNotNull("使用新密码登录应该成功", updatedUserInfo);
        assertEquals("新密码应该匹配", newPassword, updatedUserInfo.getPassword());

        // 4. 使用旧密码应该失败
        UserInfo oldLogin = userDbHelper.login(testUsername);
        assertFalse("旧密码不应该匹配", oldLogin.getPassword().equals(testPassword));
    }

    @Test
    public void testCompleteUserFlow_DeleteAccount() {
        // 1. 注册用户
        userDbHelper.register(testUsername, testPassword);
        UserInfo userInfo = userDbHelper.login(testUsername);
        assertNotNull("用户应该存在", userInfo);

        // 2. 添加历史记录
        NewsInfo.ResultDTO.DataDTO news = new NewsInfo.ResultDTO.DataDTO();
        news.setUniquekey("news_del_001");
        news.setTitle("将被删除的新闻");
        historyDbHelper.addHistory(userInfo.getUsername(), news.getUniquekey(), gson.toJson(news));

        // 3. 删除用户
        int deleteResult = userDbHelper.delete(userInfo.getUser_id());
        assertTrue("删除用户应该成功", deleteResult > 0);

        // 4. 验证用户不存在
        UserInfo deletedUserInfo = userDbHelper.login(testUsername);
        assertNull("删除后用户不应该存在", deletedUserInfo);

        // 5. 历史记录仍然存在（用户删除不影响历史记录）
        List<HistoryInfo> historyList = historyDbHelper.queryHistoryListData(userInfo.getUsername());
        assertEquals("该用户的历史记录应该为空", 0, historyList.size());
    }

    @Test
    public void testMultiUserDataIsolation() {
        // 1. 创建用户A
        String userA = "usera_" + System.currentTimeMillis();
        String passwordA = "passA";
        userDbHelper.register(userA, passwordA);
        UserInfo infoA = userDbHelper.login(userA);

        // 2. 创建用户B
        String userB = "userb_" + System.currentTimeMillis();
        String passwordB = "passB";
        userDbHelper.register(userB, passwordB);
        UserInfo infoB = userDbHelper.login(userB);

        // 3. 用户A添加历史记录
        NewsInfo.ResultDTO.DataDTO newsA = new NewsInfo.ResultDTO.DataDTO();
        newsA.setUniquekey("news_a_001");
        newsA.setTitle("用户A的新闻");
        historyDbHelper.addHistory(userA, newsA.getUniquekey(), gson.toJson(newsA));

        // 4. 用户B添加历史记录
        NewsInfo.ResultDTO.DataDTO newsB = new NewsInfo.ResultDTO.DataDTO();
        newsB.setUniquekey("news_b_001");
        newsB.setTitle("用户B的新闻");
        historyDbHelper.addHistory(userB, newsB.getUniquekey(), gson.toJson(newsB));

        // 5. 验证数据隔离
        List<HistoryInfo> historyA = historyDbHelper.queryHistoryListData(userA);
        List<HistoryInfo> historyB = historyDbHelper.queryHistoryListData(userB);

        assertEquals("用户A应该有1条记录", 1, historyA.size());
        assertEquals("用户B应该有1条记录", 1, historyB.size());

        assertTrue("用户A的记录应该包含A的新闻", historyA.get(0).getNew_json().contains("用户A的新闻"));
        assertFalse("用户A的记录不应该包含B的新闻", historyA.get(0).getNew_json().contains("用户B的新闻"));

        assertTrue("用户B的记录应该包含B的新闻", historyB.get(0).getNew_json().contains("用户B的新闻"));
        assertFalse("用户B的记录不应该包含A的新闻", historyB.get(0).getNew_json().contains("用户A的新闻"));
    }

    @Test
    public void testDuplicateHistoryPrevention() {
        // 1. 注册并登录用户
        userDbHelper.register(testUsername, testPassword);
        UserInfo userInfo = userDbHelper.login(testUsername);

        // 2. 添加同一条新闻两次
        NewsInfo.ResultDTO.DataDTO news = new NewsInfo.ResultDTO.DataDTO();
        news.setUniquekey("dup_001");
        news.setTitle("重复新闻测试");
        String json = gson.toJson(news);

        int firstAdd = historyDbHelper.addHistory(userInfo.getUsername(), news.getUniquekey(), json);
        int secondAdd = historyDbHelper.addHistory(userInfo.getUsername(), news.getUniquekey(), json);

        assertTrue("第一次添加应该成功", firstAdd > 0);
        assertEquals("第二次添加应该返回0", 0, secondAdd);

        // 3. 验证只有一条记录
        List<HistoryInfo> historyList = historyDbHelper.queryHistoryListData(userInfo.getUsername());
        assertEquals("应该只有一条记录", 1, historyList.size());
    }
}

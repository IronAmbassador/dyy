package com.white.news.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.white.news.entity.HistoryInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class HistoryDbHelperTest {
    private HistoryDbHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = HistoryDbHelper.getInstance(context);
        // 清理数据库
        try {
            dbHelper.getWritableDatabase().execSQL("DELETE FROM history_table");
        } catch (Exception e) {
            // 忽略
        }
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Test
    public void testSingleton() {
        HistoryDbHelper instance1 = HistoryDbHelper.getInstance(context);
        HistoryDbHelper instance2 = HistoryDbHelper.getInstance(context);
        assertEquals(instance1, instance2);
    }

    @Test
    public void testAddHistory() {
        String username = "user_" + System.currentTimeMillis();
        String uniquekey = "news_key_" + System.currentTimeMillis();
        String json = "{\"title\":\"测试新闻\",\"url\":\"http://test.com\"}";

        int result = dbHelper.addHistory(username, uniquekey, json);
        assertTrue("添加历史记录应该成功", result > 0);
    }

    @Test
    public void testAddHistory_Duplicate() {
        String username = "user_dup_" + System.currentTimeMillis();
        String uniquekey = "news_key_dup_" + System.currentTimeMillis();
        String json = "{\"title\":\"测试新闻\"}";

        int firstResult = dbHelper.addHistory(username, uniquekey, json);
        assertTrue("第一次添加应该成功", firstResult > 0);

        int secondResult = dbHelper.addHistory(username, uniquekey, json);
        assertEquals("重复添加应该返回0", 0, secondResult);
    }

    @Test
    public void testIsHistory_True() {
        String username = "user_check_" + System.currentTimeMillis();
        String uniquekey = "news_key_check_" + System.currentTimeMillis();
        String json = "{\"title\":\"测试新闻\"}";

        dbHelper.addHistory(username, uniquekey, json);

        boolean exists = dbHelper.isHistory(username, uniquekey);
        assertTrue("存在的历史记录应该返回true", exists);
    }

    @Test
    public void testIsHistory_False() {
        String username = "user_notexist_" + System.currentTimeMillis();
        String uniquekey = "nonexistent_key_" + System.currentTimeMillis();

        boolean exists = dbHelper.isHistory(username, uniquekey);
        assertFalse("不存在的历史记录应该返回false", exists);
    }

    @Test
    public void testQueryHistoryListData_SpecificUser() {
        String username1 = "user1_" + System.currentTimeMillis();
        String username2 = "user2_" + System.currentTimeMillis();

        dbHelper.addHistory(username1, "news_1_" + System.currentTimeMillis(), "{\"title\":\"新闻1\"}");
        dbHelper.addHistory(username1, "news_2_" + System.currentTimeMillis(), "{\"title\":\"新闻2\"}");
        dbHelper.addHistory(username2, "news_3_" + System.currentTimeMillis(), "{\"title\":\"新闻3\"}");

        List<HistoryInfo> user1History = dbHelper.queryHistoryListData(username1);
        List<HistoryInfo> user2History = dbHelper.queryHistoryListData(username2);

        assertEquals("用户1应该有2条记录", 2, user1History.size());
        assertEquals("用户2应该有1条记录", 1, user2History.size());
    }

    @Test
    public void testQueryHistoryListData_NullUsername() {
        String username = "user_null_" + System.currentTimeMillis();
        dbHelper.addHistory(username, "news_null_" + System.currentTimeMillis(), "{\"title\":\"新闻\"}");

        List<HistoryInfo> nullUserHistory = dbHelper.queryHistoryListData(null);

        assertNotNull("查询结果不应该为null", nullUserHistory);
    }

    @Test
    public void testHistoryDataIntegrity() {
        String username = "user_integrity_" + System.currentTimeMillis();
        String uniquekey = "news_integrity_" + System.currentTimeMillis();
        String json = "{\"title\":\"完整测试新闻\",\"author\":\"测试作者\",\"url\":\"http://test.com\"}";

        dbHelper.addHistory(username, uniquekey, json);

        List<HistoryInfo> historyList = dbHelper.queryHistoryListData(username);
        assertFalse("历史记录列表不应该为空", historyList.isEmpty());

        HistoryInfo history = historyList.get(historyList.size() - 1);
        assertEquals("uniquekey应该匹配", uniquekey, history.getUniquekey());
        assertEquals("username应该匹配", username, history.getUsername());
        assertTrue("JSON数据应该包含title", history.getNew_json().contains("完整测试新闻"));
    }
}

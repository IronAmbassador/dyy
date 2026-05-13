package com.white.news.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserInfoTest {

    @After
    public void tearDown() {
        UserInfo.setsUserInfo(null);
    }

    @Test
    public void testConstructor() {
        UserInfo userInfo = new UserInfo(1, "testuser", "测试昵称", "password123");

        assertEquals("用户ID应该匹配", 1, userInfo.getUser_id());
        assertEquals("用户名应该匹配", "testuser", userInfo.getUsername());
        assertEquals("昵称应该匹配", "测试昵称", userInfo.getNickname());
        assertEquals("密码应该匹配", "password123", userInfo.getPassword());
    }

    @Test
    public void testSingleton_Null() {
        UserInfo.setsUserInfo(null);
        assertNull("初始状态单例应该为null", UserInfo.getsUserInfo());
    }

    @Test
    public void testSingleton_SetAndGet() {
        UserInfo userInfo = new UserInfo(1, "user", "nickname", "pass");
        UserInfo.setsUserInfo(userInfo);

        UserInfo retrieved = UserInfo.getsUserInfo();
        assertSame("获取的应该是同一个实例", userInfo, retrieved);
    }

    @Test
    public void testSingleton_Replace() {
        UserInfo user1 = new UserInfo(1, "user1", "nick1", "pass1");
        UserInfo user2 = new UserInfo(2, "user2", "nick2", "pass2");

        UserInfo.setsUserInfo(user1);
        assertSame("第一次设置应该生效", user1, UserInfo.getsUserInfo());

        UserInfo.setsUserInfo(user2);
        assertSame("第二次设置应该覆盖", user2, UserInfo.getsUserInfo());
    }

    @Test
    public void testSetters() {
        UserInfo userInfo = new UserInfo(0, "", "", "");

        userInfo.setUser_id(10);
        userInfo.setUsername("newuser");
        userInfo.setNickname("新昵称");
        userInfo.setPassword("newpass");

        assertEquals("用户ID应该更新", 10, userInfo.getUser_id());
        assertEquals("用户名应该更新", "newuser", userInfo.getUsername());
        assertEquals("昵称应该更新", "新昵称", userInfo.getNickname());
        assertEquals("密码应该更新", "newpass", userInfo.getPassword());
    }
}

package com.white.news.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.white.news.entity.NewsInfo;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsApiTest {
    private static final String API_KEY = "226c6defa51c0f3fc7b9b0e9c7bfe78f";
    private static final String BASE_URL = "http://v.juhe.cn/toutiao/index";

    private final OkHttpClient client = new OkHttpClient();

    @Test
    public void testTopNewsApi() throws IOException {
        String url = BASE_URL + "?key=" + API_KEY + "&type=top";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("响应应该成功", response.isSuccessful());
            assertNotNull("响应体不应该为空", response.body());

            String json = response.body().string();
            assertNotNull("JSON内容不应该为空", json);
            assertTrue("JSON应该包含error_code字段", json.contains("error_code"));

            Gson gson = new Gson();
            NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

            assertNotNull("解析后的对象不应该为空", newsInfo);
            if (newsInfo.getError_code() == 0) {
                assertNotNull("result不应该为空", newsInfo.getResult());
                assertNotNull("data列表不应该为空", newsInfo.getResult().getData());
                assertFalse("data列表不应该为空", newsInfo.getResult().getData().isEmpty());
            }
        }
    }

    @Test
    public void testCategoryNewsApi() throws IOException {
        String[] categories = {"guonei", "guoji", "yule", "tiyu"};

        for (String category : categories) {
            String url = BASE_URL + "?key=" + API_KEY + "&type=" + category;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertTrue("分类[" + category + "]响应应该成功", response.isSuccessful());
                assertNotNull("分类[" + category + "]响应体不应该为空", response.body());

                String json = response.body().string();
                Gson gson = new Gson();
                NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

                assertNotNull("分类[" + category + "]解析结果不应该为空", newsInfo);
            }
        }
    }

    @Test
    public void testInvalidApiKey() throws IOException {
        String url = BASE_URL + "?key=invalid_key&type=top";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue("响应应该成功", response.isSuccessful());

            String json = response.body().string();
            Gson gson = new Gson();
            NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

            assertNotNull("解析结果不应该为空", newsInfo);
            assertTrue("无效的API Key应该返回非0错误码", newsInfo.getError_code() != 0);
        }
    }

    @Test
    public void testAsyncRequest() throws InterruptedException {
        String url = BASE_URL + "?key=" + API_KEY + "&type=top";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                error.set(e.getMessage());
                latch.countDown();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);
                        success.set(newsInfo != null);
                    }
                } finally {
                    response.close();
                    latch.countDown();
                }
            }
        });

        assertTrue("异步请求应该在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("不应该有错误", error.get());
    }

    @Test
    public void testNewsDataStructure() throws IOException {
        String url = BASE_URL + "?key=" + API_KEY + "&type=keji";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Gson gson = new Gson();
                NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

                if (newsInfo != null && newsInfo.getError_code() == 0 && newsInfo.getResult() != null) {
                    for (NewsInfo.ResultDTO.DataDTO item : newsInfo.getResult().getData()) {
                        assertNotNull("每条新闻的uniquekey不应该为空", item.getUniquekey());
                        assertNotNull("每条新闻的title不应该为空", item.getTitle());
                        assertNotNull("每条新闻的date不应该为空", item.getDate());
                    }
                }
            }
        }
    }

    @Test
    public void testResponseTime() throws IOException {
        String url = BASE_URL + "?key=" + API_KEY + "&type=top";

        long startTime = System.currentTimeMillis();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            assertTrue("响应时间应该在10秒内", responseTime < 10000);
        }
    }
}

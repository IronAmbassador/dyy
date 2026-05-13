package com.white.news.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NewsInfoTest {

    @Test
    public void testNewsInfoConstruction() {
        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setReason("success");
        newsInfo.setError_code(0);

        assertEquals("reason应该匹配", "success", newsInfo.getReason());
        assertEquals("error_code应该匹配", 0, newsInfo.getError_code());
    }

    @Test
    public void testResultDTOConstruction() {
        NewsInfo.ResultDTO resultDTO = new NewsInfo.ResultDTO();
        resultDTO.setStat("1");
        resultDTO.setPage("1");
        resultDTO.setPageSize("20");

        assertEquals("stat应该匹配", "1", resultDTO.getStat());
        assertEquals("page应该匹配", "1", resultDTO.getPage());
        assertEquals("pageSize应该匹配", "20", resultDTO.getPageSize());
    }

    @Test
    public void testDataDTOConstruction() {
        NewsInfo.ResultDTO.DataDTO dataDTO = new NewsInfo.ResultDTO.DataDTO();
        dataDTO.setUniquekey("12345");
        dataDTO.setTitle("测试新闻标题");
        dataDTO.setDate("2024-01-01");
        dataDTO.setCategory("国内");
        dataDTO.setAuthor_name("测试作者");
        dataDTO.setUrl("http://example.com/news/12345");
        dataDTO.setThumbnail_pic_s("http://example.com/image.jpg");

        assertEquals("uniquekey应该匹配", "12345", dataDTO.getUniquekey());
        assertEquals("title应该匹配", "测试新闻标题", dataDTO.getTitle());
        assertEquals("date应该匹配", "2024-01-01", dataDTO.getDate());
        assertEquals("category应该匹配", "国内", dataDTO.getCategory());
        assertEquals("author_name应该匹配", "测试作者", dataDTO.getAuthor_name());
        assertEquals("url应该匹配", "http://example.com/news/12345", dataDTO.getUrl());
        assertEquals("thumbnail_pic_s应该匹配", "http://example.com/image.jpg", dataDTO.getThumbnail_pic_s());
    }

    @Test
    public void testGsonSerialization() {
        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setReason("success");
        newsInfo.setError_code(0);

        NewsInfo.ResultDTO resultDTO = new NewsInfo.ResultDTO();
        List<NewsInfo.ResultDTO.DataDTO> dataList = new ArrayList<>();

        NewsInfo.ResultDTO.DataDTO dataDTO = new NewsInfo.ResultDTO.DataDTO();
        dataDTO.setUniquekey("001");
        dataDTO.setTitle("Gson测试新闻");
        dataDTO.setAuthor_name("测试作者");
        dataList.add(dataDTO);

        resultDTO.setData(dataList);
        newsInfo.setResult(resultDTO);

        Gson gson = new Gson();
        String json = gson.toJson(newsInfo);

        assertNotNull("JSON序列化不应该返回null", json);
        assertEquals("JSON应该包含reason字段", true, json.contains("success"));
        assertEquals("JSON应该包含title字段", true, json.contains("Gson测试新闻"));
    }

    @Test
    public void testGsonDeserialization() {
        String json = "{\"reason\":\"success\",\"error_code\":0,\"result\":{\"stat\":\"1\",\"data\":[{\"uniquekey\":\"001\",\"title\":\"解析测试\"}]}}";

        Gson gson = new Gson();
        NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

        assertEquals("error_code应该匹配", 0, newsInfo.getError_code());
        assertEquals("result不应该为null", true, newsInfo.getResult() != null);
        assertEquals("data不应该为null", true, newsInfo.getResult().getData() != null);
        assertEquals("data大小应该是1", 1, newsInfo.getResult().getData().size());
        assertEquals("title应该匹配", "解析测试", newsInfo.getResult().getData().get(0).getTitle());
    }

    @Test
    public void testMultipleThumbnails() {
        NewsInfo.ResultDTO.DataDTO dataDTO = new NewsInfo.ResultDTO.DataDTO();
        dataDTO.setThumbnail_pic_s("http://example.com/pic1.jpg");
        dataDTO.setThumbnail_pic_s02("http://example.com/pic2.jpg");
        dataDTO.setThumbnail_pic_s03("http://example.com/pic3.jpg");

        assertEquals("第一张缩略图应该匹配", "http://example.com/pic1.jpg", dataDTO.getThumbnail_pic_s());
        assertEquals("第二张缩略图应该匹配", "http://example.com/pic2.jpg", dataDTO.getThumbnail_pic_s02());
        assertEquals("第三张缩略图应该匹配", "http://example.com/pic3.jpg", dataDTO.getThumbnail_pic_s03());
    }

    @Test
    public void testErrorResponse() {
        String json = "{\"reason\":\"请求超过次数限制\",\"error_code\":10014}";

        Gson gson = new Gson();
        NewsInfo newsInfo = gson.fromJson(json, NewsInfo.class);

        assertEquals("error_code应该是10014", 10014, newsInfo.getError_code());
        assertEquals("reason应该匹配", "请求超过次数限制", newsInfo.getReason());
    }
}

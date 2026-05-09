package com.white.news;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.white.news.db.HistoryDbHelper;
import com.white.news.entity.NewsInfo;
import com.white.news.entity.UserInfo;


public class NewsDetailsActivity extends AppCompatActivity {
    private NewsInfo.ResultDTO.DataDTO dataDTO;
    private Toolbar toolbar;
    private WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        // 初始化控件
        toolbar = findViewById(R.id.toolbar);
        mWebView = findViewById(R.id.webView);

        // 获取传参的数据
        dataDTO = (NewsInfo.ResultDTO.DataDTO) getIntent().getSerializableExtra("dataDTO");

        // 设置数据
        if (null != dataDTO) {
            toolbar.setTitle(dataDTO.getTitle());
            mWebView.loadUrl(dataDTO.getUrl());

            // 添加浏览历史 - 使用当前登录用户
            String username = null;
            UserInfo userInfo = UserInfo.getsUserInfo();
            if (userInfo != null) {
                username = userInfo.getUsername();
            }
            String s = new Gson().toJson(dataDTO);
            int row = HistoryDbHelper.getInstance(NewsDetailsActivity.this).addHistory(username, dataDTO.getUniquekey(), s);
        }

        // 监听点击事件
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}
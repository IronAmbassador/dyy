package com.white.news;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.white.news.adapter.NewsListAdapter;
import com.white.news.db.HistoryDbHelper;
import com.white.news.entity.HistoryInfo;
import com.white.news.entity.NewsInfo;
import com.white.news.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class HistoryListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private NewsListAdapter mNewsListAdapter;
    private List<NewsInfo.ResultDTO.DataDTO> mDataDTOList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);
// 初始化控件
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);

// 初始化适配器
        mNewsListAdapter = new NewsListAdapter(this);
// 设置适配器
        recyclerView.setAdapter(mNewsListAdapter);

// 获取当前登录用户
        String username = null;
        UserInfo userInfo = UserInfo.getsUserInfo();
        if (userInfo != null) {
            username = userInfo.getUsername();
        }

// 获取数据 - 根据用户查询
        List<HistoryInfo> historyInfoList = HistoryDbHelper.getInstance(this).queryHistoryListData(username);
        Gson gson = new Gson();
        for (int i = 0; i < historyInfoList.size(); i++) {
            mDataDTOList.add(gson.fromJson(historyInfoList.get(i).getNew_json(), NewsInfo.ResultDTO.DataDTO.class));
        }
// 设置数据
        mNewsListAdapter.setListData(mDataDTOList);
// recyclerView点击事件
        mNewsListAdapter.setmOnItemClickListener(new NewsListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(NewsInfo.ResultDTO.DataDTO dataDTO, int position) {
// 跳转到详情页
                Intent intent = new Intent(HistoryListActivity.this, NewsDetailsActivity.class);
// 传递对象的时候，该类一定要实现Serializable
                intent.putExtra("dataDTO", dataDTO);
                startActivity(intent);
            }
        });

// 返回
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
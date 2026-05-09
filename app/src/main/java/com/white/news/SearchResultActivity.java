package com.white.news;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.white.news.adapter.NewsListAdapter;
import com.white.news.entity.NewsInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchResultActivity extends AppCompatActivity {
    private String key = "226c6defa51c0f3fc7b9b0e9c7bfe78f";
    private EditText etSearch;
    private ImageView btnBack;
    private TextView btnSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private NewsListAdapter mNewsListAdapter;
    private List<NewsInfo.ResultDTO.DataDTO> allNewsData = new ArrayList<>();
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        initViews();
        initRecyclerView();
        initListeners();

        Intent intent = getIntent();
        if (intent != null) {
            String keyword = intent.getStringExtra("keyword");
            if (!TextUtils.isEmpty(keyword)) {
                etSearch.setText(keyword);
                currentKeyword = keyword;
            }
        }
        loadAllNewsData();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initRecyclerView() {
        mNewsListAdapter = new NewsListAdapter(this);
        recyclerView.setAdapter(mNewsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsListAdapter.setmOnItemClickListener(new NewsListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(NewsInfo.ResultDTO.DataDTO dataDTO, int position) {
                Intent intent = new Intent(SearchResultActivity.this, NewsDetailsActivity.class);
                intent.putExtra("dataDTO", dataDTO);
                startActivity(intent);
            }
        });
    }

    private void initListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(keyword)) {
                    currentKeyword = keyword;
                    filterNewsByKeyword(keyword);
                } else {
                    Toast.makeText(SearchResultActivity.this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                }
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = etSearch.getText().toString().trim();
                    if (!TextUtils.isEmpty(keyword)) {
                        currentKeyword = keyword;
                        filterNewsByKeyword(keyword);
                    } else {
                        Toast.makeText(SearchResultActivity.this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void loadAllNewsData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        String[] types = {"top", "guonei", "guoji", "yule", "tiyu"};
        final int[] count = {0};
        final int totalTypes = types.length;

        for (String type : types) {
            String url = "http://v.juhe.cn/toutiao/index?key=" + key + "&type=" + type;
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    count[0]++;
                    if (count[0] >= totalTypes) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                filterNewsByKeyword(currentKeyword);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    count[0]++;
                    String data = response.body().string();
                    NewsInfo newsInfo = new Gson().fromJson(data, NewsInfo.class);
                    if (newsInfo != null && newsInfo.getError_code() == 0 && newsInfo.getResult() != null && newsInfo.getResult().getData() != null) {
                        allNewsData.addAll(newsInfo.getResult().getData());
                    }
                    if (count[0] >= totalTypes) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                filterNewsByKeyword(currentKeyword);
                            }
                        });
                    }
                }
            });
        }
    }

    private void filterNewsByKeyword(String keyword) {
        if (allNewsData.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<NewsInfo.ResultDTO.DataDTO> filteredList = new ArrayList<>();
        for (NewsInfo.ResultDTO.DataDTO news : allNewsData) {
            if (news.getTitle() != null && news.getTitle().contains(keyword)) {
                filteredList.add(news);
            } else if (news.getAuthor_name() != null && news.getAuthor_name().contains(keyword)) {
                filteredList.add(news);
            } else if (news.getCategory() != null && news.getCategory().contains(keyword)) {
                filteredList.add(news);
            }
        }

        if (filteredList.isEmpty()) {
            tvEmpty.setText("未找到包含 \"" + keyword + "\" 的新闻");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            mNewsListAdapter.setListData(filteredList);
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
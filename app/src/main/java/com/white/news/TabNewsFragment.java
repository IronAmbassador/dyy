package com.white.news;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.white.news.adapter.NewsListAdapter;
import com.white.news.entity.NewsInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TabNewsFragment extends Fragment {
    private String key = "226c6defa51c0f3fc7b9b0e9c7bfe78f";
    private String url = "http://v.juhe.cn/toutiao/index?key=" + key + "&type=";

    private View rootView;
    private RecyclerView recyclerView;

    private NewsListAdapter mNewsListAdapter;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;

            NewsInfo newsInfo = new Gson().fromJson(data, NewsInfo.class);
            if (newsInfo != null && newsInfo.getError_code() == 0) {
                // 逻辑处理
                if (null != mNewsListAdapter) {
                    mNewsListAdapter.setListData(newsInfo.getResult().getData());
                }
            } else {
                Toast.makeText(getActivity(), "获取数据失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private static final String ARG_PARAM = "title";
    private String title;


    public TabNewsFragment() {
    }


    public static TabNewsFragment newInstance(String param) {
        TabNewsFragment fragment = new TabNewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_news, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 初始化适配器
        mNewsListAdapter = new NewsListAdapter(getActivity());
        // 设置适配器
        recyclerView.setAdapter(mNewsListAdapter);
        // recyclerView列表点击事件
        mNewsListAdapter.setmOnItemClickListener(new NewsListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(NewsInfo.ResultDTO.DataDTO dataDTO, int position) {
                // 跳转到详情页
                Intent intent = new Intent(getActivity(), NewsDetailsActivity.class);
                // 传递对象的时候，该类一定要实现Serializable
                intent.putExtra("dataDTO", dataDTO);
                startActivity(intent);

            }
        });

        getHttpData();
    }

    private void getHttpData() {
        // 创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        // 构建Request对象
        Request request = new Request.Builder().url(url + title).get().build();

        // 通过OkHttpClient和Request对象来构建Call对象
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("TAG", "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                // Log.d("TAG", "onResponse: " + data);

                // 不能在耗时操作里面更新UI，需要使用Handler
                Message message = new Message();
                message.what = 100;
                message.obj = data;
                mHandler.sendMessage(message);

            }
        });
    }
}
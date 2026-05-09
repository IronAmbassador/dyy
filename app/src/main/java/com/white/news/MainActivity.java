package com.white.news;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.white.news.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<TitleInfo> titles = new ArrayList<>();

    private TabLayout tab_layout;
    private ViewPager2 viewPager;
    private NavigationView nav_view;
    private TextView tv_username;
    private TextView tv_nickname;
    private ImageView btn_open_drawerLayout;
    private DrawerLayout drawer_layout;
    private EditText et_search;
    private TextView btn_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 输出化title数据
        titles.add(new TitleInfo("推荐", "top"));
        titles.add(new TitleInfo("国内", "guonei"));
        titles.add(new TitleInfo("国际", "guoji"));
        titles.add(new TitleInfo("娱乐", "yule"));
        titles.add(new TitleInfo("体育", "tiyu"));
        titles.add(new TitleInfo("军事", "junshi"));
        titles.add(new TitleInfo("科技", "keji"));
        titles.add(new TitleInfo("财经", "caijing"));
        titles.add(new TitleInfo("游戏", "youxi"));
        titles.add(new TitleInfo("汽车", "qiche"));
        titles.add(new TitleInfo("健康", "jiangkang"));

        // 初始化控件
        tab_layout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        nav_view = findViewById(R.id.nav_view);
        btn_open_drawerLayout = findViewById(R.id.btn_open_drawerLayout);
        drawer_layout = findViewById(R.id.drawer_layout);
        tv_username = nav_view.getHeaderView(0).findViewById(R.id.tv_username);
        tv_nickname = nav_view.getHeaderView(0).findViewById(R.id.tv_nickname);
        et_search = findViewById(R.id.et_search);
        btn_search = findViewById(R.id.btn_search);

        // viewPager需要设置一个adapter
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // 滑动某一个具体的TabLayout的时候进行创建一个实例
                String title = titles.get(position).getPy_title();
                // 初始化展示容器
                TabNewsFragment tabNewsFragment = TabNewsFragment.newInstance(title);
                return tabNewsFragment;
            }

            @Override
            public int getItemCount() {
                return titles.size();
            }
        });
        // 打开抽屉事件
        btn_open_drawerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.open();
            }
        });
        // tab_layout点击事件
        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 设置viewPager选中当前页
                viewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // nav_view点击事件
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_history) {
                    // 历史记录
                    Intent intent = new Intent(MainActivity.this, HistoryListActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_updatePwd) {
                    // 修改密码
                    UserInfo userInfo = UserInfo.getsUserInfo();
                    if (userInfo == null) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.putExtra("login", "login");
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "您未登录，请先登录~~", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MainActivity.this, UpdatePwdActivity.class);
                        startActivityForResult(intent, 2000);
                    }
                } else if (item.getItemId() == R.id.nav_about) {
                    // 关于APP
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                } else if (item.getItemId() == R.id.nav_exit) {
                    // 退出登录
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                return true;
            }
        });

        // tab_layout与viewPager关联在一起
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tab_layout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // 设置顶部导航栏标题
                tab.setText(titles.get(position).getTitle());
            }
        });

        tabLayoutMediator.attach();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = et_search.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    Toast.makeText(MainActivity.this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                    intent.putExtra("keyword", keyword);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserInfo userInfo = UserInfo.getsUserInfo();
        if (userInfo != null) {
            tv_username.setText(userInfo.getUsername());
            tv_nickname.setText(userInfo.getNickname());

            tv_nickname.setVisibility(View.VISIBLE);
        } else {
            tv_username.setText("请登录");

            tv_nickname.setVisibility(View.GONE);

            // 请登录的时候点击事件
            tv_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("login", "login");
                    startActivity(intent);
                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2000) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
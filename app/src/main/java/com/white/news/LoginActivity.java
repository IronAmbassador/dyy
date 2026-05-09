package com.white.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.white.news.db.UserDbHelper;
import com.white.news.entity.UserInfo;


public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView et_username;
    private TextView et_password;
    private TextView btn_register;
    private CheckBox checkbox;
    private SharedPreferences mSharedPreferences;
    private boolean is_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        toolbar = findViewById(R.id.toolbar);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
        checkbox = findViewById(R.id.checkbox);

        // 获取传参的数据
        String update_login = getIntent().getStringExtra("login");
        if (update_login != null) {
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }


        // 获取mSharedPreferences实例
        mSharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        // 初始化登录
        is_login = mSharedPreferences.getBoolean("is_login", false);
        String username = mSharedPreferences.getString("username", "");
        String password = mSharedPreferences.getString("password", "");
        if (is_login) {
            et_username.setText(username);
            et_password.setText(password);
            checkbox.setChecked(true);
        }

        // 登录事件
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名或者密码~", Toast.LENGTH_SHORT).show();
                } else {
                    UserInfo userInfo = UserDbHelper.getInstance(LoginActivity.this).login(username);
                    if (userInfo != null) {
                        if (userInfo.getUsername().equals(username) && userInfo.getPassword().equals(password)) {
                            SharedPreferences.Editor edit = mSharedPreferences.edit();
                            edit.putString("username", username);
                            edit.putString("password", password);
                            edit.putBoolean("is_login", is_login);
                            edit.commit();
                            // 保存用户信息
                            UserInfo.setsUserInfo(userInfo);
                            // 登录成功
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "用户名或者密码错误", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "该账号未注册~~", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        // checkbox变化事件
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_login = isChecked;
            }
        });

        // 注册事件
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
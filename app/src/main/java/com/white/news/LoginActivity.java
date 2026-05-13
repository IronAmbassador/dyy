package com.white.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.white.news.db.UserDbHelper;
import com.white.news.entity.UserInfo;

public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText et_username;
    private EditText et_password;
    private TextView btn_register;
    private CheckBox checkbox;
    private SharedPreferences mSharedPreferences;
    private boolean is_login;
    private UserDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = UserDbHelper.getInstance(this);

        initViews();
        setupListeners();
        loadSavedCredentials();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
        checkbox = findViewById(R.id.checkbox);
    }

    private void setupListeners() {
        String update_login = getIntent().getStringExtra("login");
        if (update_login != null) {
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_login = isChecked;
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadSavedCredentials() {
        mSharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        is_login = mSharedPreferences.getBoolean("is_login", false);
        String username = mSharedPreferences.getString("username", "");
        String password = mSharedPreferences.getString("password", "");

        if (is_login) {
            et_username.setText(username);
            et_password.setText(password);
            checkbox.setChecked(true);
        }
    }

    private void attemptLogin() {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            et_username.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            et_password.requestFocus();
            return;
        }

        int result = dbHelper.loginVerify(username, password);

        switch (result) {
            case 0:
                UserInfo userInfo = dbHelper.login(username);
                saveCredentials(username, password);
                UserInfo.setsUserInfo(userInfo);
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                break;
            case -1:
                Toast.makeText(this, "该用户名未注册", Toast.LENGTH_SHORT).show();
                et_username.requestFocus();
                break;
            case -2:
                Toast.makeText(this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                et_password.setText("");
                et_password.requestFocus();
                break;
            case -3:
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                et_username.requestFocus();
                break;
            case -4:
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                et_password.requestFocus();
                break;
        }
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString("username", username);
        edit.putString("password", password);
        edit.putBoolean("is_login", is_login);
        edit.apply();
    }
}

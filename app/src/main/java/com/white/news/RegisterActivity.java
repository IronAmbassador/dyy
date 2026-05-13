package com.white.news;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.white.news.db.UserDbHelper;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText et_username;
    private EditText et_password;
    private EditText et_password_confirm;
    private TextView tv_username_hint;
    private TextView tv_password_hint;
    private TextView tv_password_confirm_hint;
    private TextView tv_login_link;
    private Button btn_register;
    private UserDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registery);

        dbHelper = UserDbHelper.getInstance(this);

        initViews();
        setupListeners();
        setupTextWatchers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        tv_username_hint = findViewById(R.id.tv_username_hint);
        tv_password_hint = findViewById(R.id.tv_password_hint);
        tv_password_confirm_hint = findViewById(R.id.tv_password_confirm_hint);
        tv_login_link = findViewById(R.id.tv_login_link);
        btn_register = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupTextWatchers() {
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateUsernameInput(s.toString());
            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordInput(s.toString());
                validatePasswordMatch();
            }
        });

        et_password_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch();
            }
        });
    }

    private void validateUsernameInput(String username) {
        if (username.isEmpty()) {
            tv_username_hint.setText("用户名长度3-16位，仅支持字母、数字、下划线");
            tv_username_hint.setTextColor(Color.parseColor("#AAAAAA"));
            return;
        }

        int result = dbHelper.validateUsername(username);
        switch (result) {
            case 0:
                if (dbHelper.isUsernameExists(username)) {
                    tv_username_hint.setText("该用户名已被使用");
                    tv_username_hint.setTextColor(Color.parseColor("#FF5252"));
                } else {
                    tv_username_hint.setText("用户名可用");
                    tv_username_hint.setTextColor(Color.parseColor("#4CAF50"));
                }
                break;
            case -1:
                tv_username_hint.setText("用户名不能为空");
                tv_username_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
            case -2:
                tv_username_hint.setText("用户名太短，至少需要3个字符");
                tv_username_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
            case -3:
                tv_username_hint.setText("用户名太长，最多16个字符");
                tv_username_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
            case -4:
                tv_username_hint.setText("用户名只能包含字母、数字、下划线");
                tv_username_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
        }
    }

    private void validatePasswordInput(String password) {
        if (password.isEmpty()) {
            tv_password_hint.setText("密码长度6-20位");
            tv_password_hint.setTextColor(Color.parseColor("#AAAAAA"));
            return;
        }

        int result = dbHelper.validatePassword(password);
        switch (result) {
            case 0:
                tv_password_hint.setText("密码格式正确");
                tv_password_hint.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case -1:
                tv_password_hint.setText("密码不能为空");
                tv_password_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
            case -2:
                tv_password_hint.setText("密码太短，至少需要6个字符");
                tv_password_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
            case -3:
                tv_password_hint.setText("密码太长，最多20个字符");
                tv_password_hint.setTextColor(Color.parseColor("#FF5252"));
                break;
        }
    }

    private void validatePasswordMatch() {
        String password = et_password.getText().toString();
        String passwordConfirm = et_password_confirm.getText().toString();

        if (passwordConfirm.isEmpty()) {
            tv_password_confirm_hint.setText("请确保两次输入的密码一致");
            tv_password_confirm_hint.setTextColor(Color.parseColor("#AAAAAA"));
            return;
        }

        if (password.equals(passwordConfirm)) {
            tv_password_confirm_hint.setText("两次密码输入一致");
            tv_password_confirm_hint.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tv_password_confirm_hint.setText("两次密码输入不一致");
            tv_password_confirm_hint.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    private void attemptRegister() {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString();
        String passwordConfirm = et_password_confirm.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            et_username.requestFocus();
            return;
        }

        int usernameResult = dbHelper.validateUsername(username);
        if (usernameResult != 0) {
            String errorMsg = getUsernameErrorMessage(usernameResult);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            et_username.requestFocus();
            return;
        }

        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "该用户名已被注册，请更换", Toast.LENGTH_SHORT).show();
            et_username.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            et_password.requestFocus();
            return;
        }

        int passwordResult = dbHelper.validatePassword(password);
        if (passwordResult != 0) {
            String errorMsg = getPasswordErrorMessage(passwordResult);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            et_password.requestFocus();
            return;
        }

        if (passwordConfirm.isEmpty()) {
            Toast.makeText(this, "请确认密码", Toast.LENGTH_SHORT).show();
            et_password_confirm.requestFocus();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            et_password_confirm.requestFocus();
            return;
        }

        int row = dbHelper.register(username, password);
        if (row > 0) {
            Toast.makeText(this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUsernameErrorMessage(int errorCode) {
        switch (errorCode) {
            case -1:
                return "用户名不能为空";
            case -2:
                return "用户名太短，至少需要3个字符";
            case -3:
                return "用户名太长，最多16个字符";
            case -4:
                return "用户名只能包含字母、数字、下划线";
            default:
                return "用户名格式错误";
        }
    }

    private String getPasswordErrorMessage(int errorCode) {
        switch (errorCode) {
            case -1:
                return "密码不能为空";
            case -2:
                return "密码太短，至少需要6个字符";
            case -3:
                return "密码太长，最多20个字符";
            default:
                return "密码格式错误";
        }
    }
}

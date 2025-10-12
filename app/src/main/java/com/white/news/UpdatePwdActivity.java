package com.white.news;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.white.news.db.UserDbHelper;
import com.white.news.entity.UserInfo;

public class UpdatePwdActivity extends AppCompatActivity {
    private TextView et_new_password;
    private TextView et_confirm_password;
    private Button btn_update_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pwd);

        // 初始化控件
        et_new_password = findViewById(R.id.et_new_password);
        et_confirm_password = findViewById(R.id.et_confirm_password);
        btn_update_pwd = findViewById(R.id.btn_update_pwd);

        // 修改密码点击事件
        btn_update_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_pwd = et_new_password.getText().toString();
                String confirm_pwd = et_confirm_password.getText().toString();

                if (TextUtils.isEmpty(new_pwd) || TextUtils.isEmpty(confirm_pwd)) {
                    Toast.makeText(UpdatePwdActivity.this, "信息不能为空", Toast.LENGTH_SHORT).show();
                } else if (!new_pwd.equals(confirm_pwd)) {
                    Toast.makeText(UpdatePwdActivity.this, "新密码与确认密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    UserInfo userInfo = UserInfo.getsUserInfo();
                    if (null != userInfo) {
                        int row = UserDbHelper.getInstance(UpdatePwdActivity.this).updatePwd(userInfo.getUser_id(), confirm_pwd);
                        if (row > 0) {
                            Toast.makeText(UpdatePwdActivity.this, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                            setResult(2000);
                            finish();
                        } else {
                            Toast.makeText(UpdatePwdActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // 返回
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
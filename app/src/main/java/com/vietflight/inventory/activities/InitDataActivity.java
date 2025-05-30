package com.vietflight.inventory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.vietflight.inventory.R;

public class InitDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_data);

        MaterialButton btnInitData = findViewById(R.id.btn_init_data);
        MaterialButton btnGoToLogin = findViewById(R.id.btn_go_to_login);

        btnInitData.setOnClickListener(v -> {
            Toast.makeText(this, "Dữ liệu test đã sẵn sàng!\nUsername: admin\nPassword: 123456",
                    Toast.LENGTH_LONG).show();
        });

        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(InitDataActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
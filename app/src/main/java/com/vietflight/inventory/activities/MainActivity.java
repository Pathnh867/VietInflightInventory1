package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.vietflight.inventory.R;
import com.vietflight.inventory.utils.TestDataHelper;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestDataHelper.createTestUser();
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("VietFlightInventory", MODE_PRIVATE);

        setupUserInfo();
        setupClickListeners();
    }

    private void setupUserInfo() {
        String userName = sharedPreferences.getString("USER_NAME", "");
        String userRole = sharedPreferences.getString("USER_ROLE", "");

        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvUserRole = findViewById(R.id.tv_user_role);

        tvUserName.setText(userName);
        tvUserRole.setText(getRoleDisplayName(userRole));
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Quản trị viên";
            case "staff": return "Nhân viên cung ứng";
            case "attendant": return "Tiếp viên hàng không";
            default: return role;
        }
    }

    private void setupClickListeners() {
        // Menu cards
        CardView cardCreateHandover = findViewById(R.id.card_create_handover);
        CardView cardHandoverList = findViewById(R.id.card_handover_list);
        CardView cardProducts = findViewById(R.id.card_products);
        CardView cardReports = findViewById(R.id.card_reports);

        cardCreateHandover.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateHandoverActivity.class);
            startActivity(intent);
        });

        cardHandoverList.setOnClickListener(v -> {
            Toast.makeText(this, "Danh sách bàn giao - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to HandoverListActivity
        });

        cardProducts.setOnClickListener(v -> {
            Toast.makeText(this, "Quản lý sản phẩm - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ProductsActivity
        });


        cardReports.setOnClickListener(v -> {
            Toast.makeText(this, "Báo cáo - Coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ReportsActivity
        });

        // Bottom navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            // Already on home
        });

        findViewById(R.id.nav_handovers).setOnClickListener(v -> {
            Toast.makeText(this, "Bàn giao - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
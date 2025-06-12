package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private TextView tvAvatar, tvFullname;
    private EditText etEmail, etFullname, etUsername, etPhone, etCompany, etLocation;
    private Button btnUpdate;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        setupNavHeader();
        tvAvatar = findViewById(R.id.tv_avatar);
        tvFullname = findViewById(R.id.tv_fullname);
        etEmail = findViewById(R.id.et_email);
        etFullname = findViewById(R.id.et_fullname);
        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etCompany = findViewById(R.id.et_company);
        etLocation = findViewById(R.id.et_location);
        btnUpdate = findViewById(R.id.btn_update_profile);
        String role = sharedPreferences.getString("role", "");
        android.view.Menu menu = navView.getMenu();

        if ("admin".equalsIgnoreCase(role)) {
            menu.findItem(R.id.nav_create).setVisible(false);
            menu.findItem(R.id.nav_receive).setVisible(false);
            menu.findItem(R.id.nav_report).setVisible(false);
            menu.findItem(R.id.nav_create_user).setVisible(true);
            menu.findItem(R.id.nav_manage_user).setVisible(true);
        } else {
            menu.findItem(R.id.nav_create).setVisible(true);
            menu.findItem(R.id.nav_receive).setVisible(true);
            menu.findItem(R.id.nav_report).setVisible(true);
            menu.findItem(R.id.nav_create_user).setVisible(false);
            menu.findItem(R.id.nav_manage_user).setVisible(false);
        }

        // Username: chỉ đọc
        etUsername.setEnabled(false);

        // Drawer menu
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_account) {
                // Đang ở đây
            } else if (id == R.id.nav_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            } else if (id == R.id.nav_create) {
            startActivity(new Intent(this, CreateHandoverActivity.class));
            } else if (id == R.id.nav_receive) {
                startActivity(new Intent(this, ReceiveHandoverActivity.class));
            }else if (id == R.id.nav_report) {
                startActivity(new Intent(this, ReportActivity.class));
            } else if (id == R.id.nav_create_user) {
                startActivity(new Intent(this, CreateUserActivity.class));
            } else if (id == R.id.nav_manage_user) {
                startActivity(new Intent(this, ManageUserActivity.class));
            } else if (id == R.id.nav_logout) {
                sharedPreferences.edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        userId = sharedPreferences.getString("user_id", "");
        loadProfileData();

        btnUpdate.setOnClickListener(v -> handleUpdateProfile());
    }

    private void loadProfileData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullname = doc.getString("fullname");
                        String email = doc.getString("email");
                        String username = doc.getString("username");
                        String phone = doc.getString("phone");
                        String company = doc.getString("company");
                        String location = doc.getString("location");

                        etEmail.setText(email != null ? email : "");
                        etFullname.setText(fullname != null ? fullname : "");
                        etUsername.setText(username != null ? username : "");
                        etPhone.setText(phone != null ? phone : "");
                        etCompany.setText(company != null ? company : "VIETJET");
                        etLocation.setText(location != null ? location : "");

                        tvFullname.setText(fullname != null ? fullname : "");

                        // Lấy 2 ký tự đầu tên (viết hoa) làm avatar, fallback nếu tên chỉ có 1 từ hoặc rỗng
                        if (!TextUtils.isEmpty(fullname)) {
                            String[] parts = fullname.trim().split("\\s+");
                            String initials = "";
                            if (parts.length >= 2) {
                                initials += parts[parts.length - 2].substring(0, 1);
                                initials += parts[parts.length - 1].substring(0, 1);
                            } else if (parts.length == 1) {
                                initials += parts[0].substring(0, 1);
                            }
                            tvAvatar.setText(initials.toUpperCase());
                        } else {
                            tvAvatar.setText("?");
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Không tải được dữ liệu hồ sơ!"));
    }

    private void handleUpdateProfile() {
        String email = etEmail.getText().toString().trim();
        String fullname = etFullname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validate đầu vào
        if (TextUtils.isEmpty(fullname)) {
            showToast("Vui lòng nhập họ và tên!");
            etFullname.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ!");
            etEmail.requestFocus();
            return;
        }
        if (!phone.isEmpty() && phone.length() < 8) {
            showToast("Số điện thoại chưa đúng!");
            etPhone.requestFocus();
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fullname", fullname);
        update.put("email", email);
        update.put("phone", phone);
        update.put("company", company);
        update.put("location", location);

        db.collection("users").document(userId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    showToast("Cập nhật thành công!");
                    loadProfileData(); // Reload lại thông tin để avatar, tên thay đổi nếu có
                })
                .addOnFailureListener(e -> showToast("Cập nhật thất bại!"));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void setupNavHeader() {
        android.view.View header = navView.getHeaderView(0);
        if (header != null) {
            TextView navAvatar = header.findViewById(R.id.tv_nav_avatar);
            TextView navFullname = header.findViewById(R.id.tv_nav_fullname);
            String fullnamePref = sharedPreferences.getString("fullname", "");
            navFullname.setText(fullnamePref);
            if (!TextUtils.isEmpty(fullnamePref)) {
                String[] parts = fullnamePref.trim().split("\\s+");
                String initials = "";
                if (parts.length >= 2) {
                    initials += parts[parts.length - 2].substring(0, 1);
                    initials += parts[parts.length - 1].substring(0, 1);
                } else {
                    initials += parts[0].substring(0, 1);
                }
                navAvatar.setText(initials.toUpperCase());
            } else {
                navAvatar.setText("?");
            }
        }
    }
}

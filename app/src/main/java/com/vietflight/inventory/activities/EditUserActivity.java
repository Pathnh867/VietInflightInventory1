package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private EditText etEmail, etFullname, etUsername, etPhone, etPassword, etCompany, etLocation;
    private ImageView ivShowHidePassword;
    private Button btnUpdate;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private String userId = null;
    private boolean isPasswordVisible = false;

    private Switch switchIsActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);

        etEmail = findViewById(R.id.et_email);
        etFullname = findViewById(R.id.et_fullname);
        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etCompany = findViewById(R.id.et_company);
        etLocation = findViewById(R.id.et_location);
        switchIsActive = findViewById(R.id.switch_is_active);
        btnUpdate = findViewById(R.id.btn_update_profile);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        ivShowHidePassword = findViewById(R.id.iv_show_hide_password); // Thêm ImageView vào layout như đã hướng dẫn


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

        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
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

        // Lấy userId từ intent
        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            loadUserData(userId);
        }

        btnUpdate.setOnClickListener(v -> updateUser());

        // Hiện/ẩn mật khẩu
        ivShowHidePassword.setOnClickListener(v -> togglePassword());
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etEmail.setText(doc.getString("email"));
                        etFullname.setText(doc.getString("fullname"));
                        etUsername.setText(doc.getString("username"));
                        etPhone.setText(doc.getString("phone"));
                        etPassword.setText(doc.getString("password"));
                        etCompany.setText(doc.getString("company"));
                        etLocation.setText(doc.getString("location"));
                        Boolean isActive = doc.getBoolean("is_active");
                        switchIsActive.setChecked(isActive == null || isActive);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show());
    }

    private void updateUser() {
        String email = etEmail.getText().toString().trim();
        String fullname = etFullname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        boolean isActive = switchIsActive.isChecked();


        // Validate đơn giản (có thể thêm kiểm tra nâng cao nếu muốn)
        if (email.isEmpty() || fullname.isEmpty() || username.isEmpty() ||
                phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("fullname", fullname);
        user.put("username", username);
        user.put("phone", phone);
        user.put("password", password);
        user.put("company", company);
        user.put("location", location);
        user.put("is_active", isActive);

        db.collection("users").document(userId).update(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show());
    }

    // Hiện/ẩn mật khẩu
    private void togglePassword() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowHidePassword.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivShowHidePassword.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }
}

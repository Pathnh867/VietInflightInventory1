package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;

import java.util.HashMap;
import java.util.Map;

public class CreateUserActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView ivShowHidePassword;
    private EditText etEmail, etFullname, etUsername, etPhone, etPassword, etCompany, etLocation;
    private Button btnCreateUser;
    private FirebaseFirestore db;
    private AutoCompleteTextView autoRole;

    private SharedPreferences sharedPreferences;
    private boolean isPasswordVisible = false;

    // Mảng label tiếng Việt và key tiếng Anh
    private static final String[] roleLabels = {"Nhân viên cung ứng", "Tiếp viên", "Admin"};
    private static final String[] roleValues = {"supply_staff", "flight_attendant", "admin"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        etEmail = findViewById(R.id.et_email);
        etFullname = findViewById(R.id.et_fullname);
        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etCompany = findViewById(R.id.et_company);
        etLocation = findViewById(R.id.et_location);
        btnCreateUser = findViewById(R.id.btn_update_profile);
        autoRole = findViewById(R.id.auto_role);
        ivShowHidePassword = findViewById(R.id.iv_show_hide_password);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);


        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

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


        // Adapter hiển thị tiếng Việt
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roleLabels);
        autoRole.setAdapter(roleAdapter);

        btnCreateUser.setOnClickListener(v -> handleCreateUser());
    }

    private void handleCreateUser() {
        String email = etEmail.getText().toString().trim();
        String fullname = etFullname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String roleLabel = autoRole.getText().toString().trim();

        // Map label (VN) -> value (EN)
        String role = "";
        for (int i = 0; i < roleLabels.length; i++) {
            if (roleLabels[i].equals(roleLabel)) {
                role = roleValues[i];
                break;
            }
        }
        final String finalRole = role;

        // Validate
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Vui lòng nhập email hợp lệ!");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(fullname) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(role)) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (password.length() < 6) {
            showToast("Mật khẩu ít nhất 6 ký tự!");
            return;
        }
        if (phone.length() < 8) {
            showToast("Số điện thoại không hợp lệ!");
            return;
        }

        // Kiểm tra trùng username
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        showToast("Tên đăng nhập đã tồn tại!");
                    } else {
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("fullname", fullname);
                        user.put("username", username);
                        user.put("password", password); // TODO: hash nếu cần
                        user.put("phone", phone);
                        user.put("company", company);
                        user.put("location", location);
                        user.put("role", finalRole); // đúng key tiếng Anh
                        user.put("is_active", true);
                        user.put("created_at", System.currentTimeMillis());

                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(docRef -> {
                                    showToast("Đã tạo người dùng thành công!");
                                    clearForm();
                                })
                                .addOnFailureListener(e -> showToast("Lỗi tạo tài khoản!"));
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi kết nối!"));
    }
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

    private void clearForm() {
        etEmail.setText("");
        etFullname.setText("");
        etUsername.setText("");
        etPassword.setText("");
        etPhone.setText("");
        etCompany.setText("");
        etLocation.setText("");
        autoRole.setText("");
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

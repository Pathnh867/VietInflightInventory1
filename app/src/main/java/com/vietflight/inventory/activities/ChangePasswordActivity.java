package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String userId;
    private String oldPasswordInDb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        setupNavHeader();
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_change_password);
        etConfirmPassword = findViewById(R.id.et_confirm_change_password);
        btnChangePassword = findViewById(R.id.btn_update_profile);
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
        // Drawer menu
        ImageView btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_password) {
                // Đang ở đây
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

        userId = sharedPreferences.getString("user_id", "");
        loadCurrentPassword();

        btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void loadCurrentPassword() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        oldPasswordInDb = doc.getString("password");
                    }
                })
                .addOnFailureListener(e -> showToast("Không tải được thông tin tài khoản!"));
    }

    private void handleChangePassword() {
        String current = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(current) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirm)) {
            showToast("Vui lòng nhập đủ tất cả trường!");
            return;
        }
        if (oldPasswordInDb == null) {
            showToast("Không xác thực được mật khẩu cũ!");
            return;
        }
        if (!current.equals(oldPasswordInDb)) {
            showToast("Mật khẩu hiện tại không đúng!");
            return;
        }
        if (newPass.length() < 6) {
            showToast("Mật khẩu mới tối thiểu 6 ký tự!");
            return;
        }
        if (!newPass.equals(confirm)) {
            showToast("Xác nhận mật khẩu không khớp!");
            return;
        }
        if (newPass.equals(current)) {
            showToast("Mật khẩu mới phải khác mật khẩu hiện tại!");
            return;
        }

        db.collection("users").document(userId)
                .update("password", newPass)
                .addOnSuccessListener(aVoid -> {
                    showToast("Đổi mật khẩu thành công!");
                    clearForm();
                })
                .addOnFailureListener(e -> showToast("Đổi mật khẩu thất bại!"));
    }

    private void clearForm() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setupNavHeader(){
        android.view.View header = navView.getHeaderView(0);
        if (header != null){
           TextView tvAvatar = header.findViewById(R.id.tv_nav_avatar);
           TextView tvFullname = header.findViewById(R.id.tv_nav_fullname);
           String fullname = sharedPreferences.getString("fullname","");
           tvFullname.setText(fullname);
           if(!TextUtils.isEmpty(fullname)){
               String[] parts  = fullname.trim().split("\\s+");
               String initials = "";
               if (parts.length >= 2) {
                   initials += parts[parts.length - 2].substring(0, 1);
                   initials += parts[parts.length -1].substring(0,1);

               } else {
                   initials += parts[0].substring(0,1);
               }
               tvAvatar.setText("?");
           }
        }
    }
}

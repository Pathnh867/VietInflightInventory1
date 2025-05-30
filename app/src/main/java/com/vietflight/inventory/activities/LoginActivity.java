package com.vietflight.inventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;
import com.vietflight.inventory.utils.DataSeeder;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);

        // Tạo dữ liệu test nếu cần
        createTestDataIfNeeded();

        // Kiểm tra đã đăng nhập chưa
        if (isUserLoggedIn()) {
            navigateToCreateHandover();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void createTestDataIfNeeded() {
        db.collection("users").limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        DataSeeder seeder = new DataSeeder();
                        seeder.seedAllData();
                        Log.d("LoginActivity", "Test data created");
                    }
                });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInput(username, password)) {
            showLoading(true);
            loginWithFirebase(username, password);
        }
    }

    private boolean validateInput(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginWithFirebase(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        saveUserSession(userDoc);
                        showSuccessMessage(userDoc.getString("fullname"));
                        navigateToCreateHandover();
                    } else {
                        showErrorMessage("Tên đăng nhập hoặc mật khẩu không đúng!");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showErrorMessage("Lỗi kết nối: " + e.getMessage());
                });
    }

    private void saveUserSession(DocumentSnapshot userDoc) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userDoc.getId());
        editor.putString("username", userDoc.getString("username"));
        editor.putString("fullname", userDoc.getString("fullname"));
        editor.putString("role", userDoc.getString("role"));
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnLogin.setText(show ? "Đang đăng nhập..." : "ĐĂNG NHẬP");
    }

    private void showSuccessMessage(String fullname) {
        Toast.makeText(this, "✅ Xin chào " + fullname + "!", Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToCreateHandover() {
        Intent intent = new Intent(LoginActivity.this, CreateHandoverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
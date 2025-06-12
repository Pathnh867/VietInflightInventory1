package com.vietflight.inventory.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageUserActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView rvUsers;
    private LinearLayout layoutEmpty;
    private EditText etSearch;
    private ImageButton btnRefresh;

    private UserAdapter userAdapter;
    private List<Map<String, Object>> userListAll = new ArrayList<>();
    private List<Map<String, Object>> userListDisplay = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    // Label và value cho vai trò
    private static final String[] roleLabels = {"Nhân viên cung ứng", "Tiếp viên", "Admin"};
    private static final String[] roleValues = {"supply_staff", "flight_attendant", "admin"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        setupNavHeader();
        rvUsers = findViewById(R.id.rv_users);
        layoutEmpty = findViewById(R.id.layout_empty);
        etSearch = findViewById(R.id.et_search);
        btnRefresh = findViewById(R.id.btn_refresh);

        String role = sharedPreferences.getString("role", "");
        android.view.Menu menu = navView.getMenu();

        // Ẩn/hiện menu
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

        // RecyclerView
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userListDisplay, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(Map<String, Object> user, int position) {
                // Sang EditUserActivity
                Intent intent = new Intent(ManageUserActivity.this, EditUserActivity.class);
                intent.putExtra("userId", (String) user.get("user_id")); // user_id là doc.getId()
                startActivity(intent);
            }

            @Override
            public void onDelete(Map<String, Object> user, int position) {
                showDeleteDialog(user, position);
            }
        });
        rvUsers.setAdapter(userAdapter);

        // Tìm kiếm user
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUserList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnRefresh.setOnClickListener(v -> loadAllUsers());

        loadAllUsers();
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userListAll.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> user = doc.getData();
                        if (user != null) {
                            user.put("user_id", doc.getId());
                            userListAll.add(user);
                        }
                    }
                    filterUserList(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được danh sách người dùng", Toast.LENGTH_SHORT).show());
    }

    // Đổi role sang label tiếng Việt khi tìm kiếm/hiển thị
    private String getRoleLabel(String value) {
        for (int i = 0; i < roleValues.length; i++) {
            if (roleValues[i].equals(value)) return roleLabels[i];
        }
        return value;
    }

    private void filterUserList(String keyword) {
        userListDisplay.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            for (Map<String, Object> user : userListAll) {
                userAdapter.setUserRoleLabel(user, getRoleLabel((String) user.get("role")));
                userListDisplay.add(user);
            }
        } else {
            String kw = keyword.toLowerCase(Locale.ROOT);
            for (Map<String, Object> user : userListAll) {
                String fullname = (user.get("fullname") + "").toLowerCase(Locale.ROOT);
                String username = (user.get("username") + "").toLowerCase(Locale.ROOT);
                String roleLabel = getRoleLabel((String) user.get("role")).toLowerCase(Locale.ROOT);
                if (fullname.contains(kw) || username.contains(kw) || roleLabel.contains(kw)) {
                    userAdapter.setUserRoleLabel(user, getRoleLabel((String) user.get("role")));
                    userListDisplay.add(user);
                }
            }
        }
        userAdapter.updateList(userListDisplay);
        layoutEmpty.setVisibility(userListDisplay.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDeleteDialog(Map<String, Object> user, int position) {
        String fullname = user.get("fullname") + "";
        String userId = user.get("user_id") + "";
        boolean isActive = user.get("is_active") == null || Boolean.TRUE.equals(user.get("is_active"));
        String actionLabel = isActive ? "Vô hiệu hóa" : "Kích hoạt lại";
        String message = isActive ?
                "Bạn có chắc chắn muốn vô hiệu hóa tài khoản: " + fullname + " ?" :
                "Bạn có chắc chắn muốn kích hoạt lại tài khoản: " + fullname + " ?";
        new AlertDialog.Builder(this)
                .setTitle(actionLabel + " tài khoản")
                .setMessage(message)
                .setPositiveButton(actionLabel, (dialog, which) -> {
                    db.collection("users").document(userId)
                            .update("is_active", !isActive)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, actionLabel + " thành công!", Toast.LENGTH_SHORT).show();
                                loadAllUsers();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadAllUsers();
    }
    private void setupNavHeader() {
        android.view.View header = navView.getHeaderView(0);
        if (header != null) {
            TextView tvAvatar = header.findViewById(R.id.tv_nav_avatar);
            TextView tvFullname = header.findViewById(R.id.tv_nav_fullname);
            String fullname = sharedPreferences.getString("fullname", "");
            tvFullname.setText(fullname);
            if (!TextUtils.isEmpty(fullname)) {
                String[] parts = fullname.trim().split("\\s+");
                String initials = "";
                if (parts.length >= 2) {
                    initials += parts[parts.length - 2].substring(0, 1);
                    initials += parts[parts.length - 1].substring(0, 1);
                } else {
                    initials += parts[0].substring(0, 1);
                }
                tvAvatar.setText(initials.toUpperCase());
            } else {
                tvAvatar.setText("?");
            }
        }
    }
}

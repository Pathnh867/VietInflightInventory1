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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.ProductManageAdapter;
import com.vietflight.inventory.activities.AddProductActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmpty;
    private EditText etSearch;
    private ImageButton btnRefresh;
    private View fabAdd;

    private ProductManageAdapter adapter;
    private List<Map<String, Object>> productListAll = new ArrayList<>();
    private List<Map<String, Object>> productListDisplay = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        setupNavHeader();
        rvProducts = findViewById(R.id.rv_products);
        layoutEmpty = findViewById(R.id.layout_empty);
        etSearch = findViewById(R.id.et_search);
        btnRefresh = findViewById(R.id.btn_refresh);
        fabAdd = findViewById(R.id.fab_add);

        String role = sharedPreferences.getString("role", "");
        android.view.Menu menu = navView.getMenu();
        menu.findItem(R.id.nav_products).setVisible("admin".equalsIgnoreCase(role));
        fabAdd.setVisibility("admin".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
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
            } else if (id == R.id.nav_report) {
                startActivity(new Intent(this, ReportActivity.class));
            } else if (id == R.id.nav_create_user) {
                startActivity(new Intent(this, CreateUserActivity.class));
            } else if (id == R.id.nav_manage_user) {
                startActivity(new Intent(this, ManageUserActivity.class));
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, ProductsActivity.class));
            } else if (id == R.id.nav_logout) {
                sharedPreferences.edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductManageAdapter(this, productListDisplay, new ProductManageAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Map<String, Object> product, int position) {
                Toast.makeText(ProductsActivity.this, "Chức năng sắp triển khai", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Map<String, Object> product, int position) {
                showDeleteDialog(product);
            }
        });
        rvProducts.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnRefresh.setOnClickListener(v -> loadAllProducts());
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        loadAllProducts();
    }

    private void loadAllProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productListAll.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> prod = doc.getData();
                        if (prod != null) {
                            prod.put("id", doc.getId());
                            productListAll.add(prod);
                        }
                    }
                    filterList(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show());
    }

    private void filterList(String keyword) {
        productListDisplay.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            productListDisplay.addAll(productListAll);
        } else {
            String kw = keyword.toLowerCase(Locale.ROOT);
            for (Map<String, Object> p : productListAll) {
                String name = (p.get("name") + "").toLowerCase(Locale.ROOT);
                String cat = (p.get("category") + "").toLowerCase(Locale.ROOT);
                if (name.contains(kw) || cat.contains(kw)) {
                    productListDisplay.add(p);
                }
            }
        }
        adapter.updateList(productListDisplay);
        layoutEmpty.setVisibility(productListDisplay.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDeleteDialog(Map<String, Object> product) {
        String name = product.get("name") + "";
        String productId = product.get("id") + "";
        boolean isActive = product.get("is_active") == null || Boolean.TRUE.equals(product.get("is_active"));
        String actionLabel = isActive ? "Vô hiệu hóa" : "Kích hoạt lại";
        String message = isActive ?
                "Bạn có chắc chắn muốn vô hiệu hóa sản phẩm: " + name + " ?" :
                "Bạn có chắc chắn muốn kích hoạt lại sản phẩm: " + name + " ?";
        new AlertDialog.Builder(this)
                .setTitle(actionLabel + " sản phẩm")
                .setMessage(message)
                .setPositiveButton(actionLabel, (dialog, which) -> {
                    db.collection("products").document(productId)
                            .update("is_active", !isActive)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, actionLabel + " thành công!", Toast.LENGTH_SHORT).show();
                                loadAllProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllProducts();
    }

    private void setupNavHeader() {
        View header = navView.getHeaderView(0);
        if (header != null) {
            TextView tvAvatar = header.findViewById(R.id.tv_nav_avatar);
            TextView tvFullname = header.findViewById(R.id.tv_nav_fullname);
            String fullname = sharedPreferences.getString("fullname", "");
            tvFullname.setText(fullname);
            if (!android.text.TextUtils.isEmpty(fullname)) {
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
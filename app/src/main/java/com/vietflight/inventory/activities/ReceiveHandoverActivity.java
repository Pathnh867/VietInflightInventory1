package com.vietflight.inventory.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.ProductReceiveAdapter;
import com.vietflight.inventory.models.Handover;
import com.vietflight.inventory.models.HandoverItem;
import com.vietflight.inventory.models.Product;

import androidx.core.view.GravityCompat;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ReceiveHandoverActivity extends AppCompatActivity {
    private EditText etFlightDate, etFlightCode, etSearch;
    private Spinner spinnerAircraftCode, spinnerFlightType;
    private RecyclerView rvProducts;
    private Button btnLoad, btnConfirm;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageView btnMenu;
    private LinearLayout layoutEmpty;
    private TextView tvTotalCount;
    private List<Product> productList;
    private ProductReceiveAdapter adapter;
    private String currentCategory = "HOT_MEAL";
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_handover);

        initViews();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);
        setupNavHeader();
        String role = sharedPreferences.getString("role", "");
        android.view.Menu menu = navView.getMenu();
        if ("admin".equalsIgnoreCase(role)) {
            // Chỉ hiện các mục dành cho admin
            menu.findItem(R.id.nav_create_user).setVisible(true);
            menu.findItem(R.id.nav_manage_user).setVisible(true);
            menu.findItem(R.id.nav_create).setVisible(false);
            menu.findItem(R.id.nav_receive).setVisible(false);
            menu.findItem(R.id.nav_report).setVisible(false);
            menu.findItem(R.id.nav_products).setVisible(true);
        } else {
            // Các role còn lại
            menu.findItem(R.id.nav_create_user).setVisible(false);
            menu.findItem(R.id.nav_manage_user).setVisible(false);
            menu.findItem(R.id.nav_create).setVisible(true);
            menu.findItem(R.id.nav_receive).setVisible(true);
            menu.findItem(R.id.nav_report).setVisible(true);
        }
        setupDrawer();
        setupTabs();
        setupDatePicker();
        btnLoad.setOnClickListener(v -> loadHandoverData());
        btnConfirm.setOnClickListener(v -> confirmReceive());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });



    }

    private void initViews() {
        etFlightDate = findViewById(R.id.et_flight_date);
        etFlightCode = findViewById(R.id.et_flight_code);
        etSearch = findViewById(R.id.et_search);
        spinnerAircraftCode = findViewById(R.id.spinner_aircraft_code);
        spinnerFlightType = findViewById(R.id.spinner_flight_type);
        btnLoad = findViewById(R.id.btn_load);
        btnConfirm = findViewById(R.id.btn_confirm_receive);
        rvProducts = findViewById(R.id.rv_products_receive);
        layoutEmpty = findViewById(R.id.layout_empty_receive);
        tvTotalCount = findViewById(R.id.tv_total_count);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btn_menu);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new ProductReceiveAdapter(this, productList);
        rvProducts.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("aircraft")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> aircraftCodes = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String code = doc.getString("code");
                        if (code != null) aircraftCodes.add(code);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, aircraftCodes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAircraftCode.setAdapter(adapter);
                });

        db.collection("flight_types")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> flightTypes = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) flightTypes.add(name);
                    }
                    ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, flightTypes);
                    typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFlightType.setAdapter(typeAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được loại chuyến bay", Toast.LENGTH_SHORT).show());
    }
    private void setupDatePicker() {
        etFlightDate.setFocusable(false);
        etFlightDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String dateStr = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year);
                etFlightDate.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
    private void setupDrawer() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateHandoverActivity.class));
            } else if (id == R.id.nav_receive) {
                //
            } else if (id == R.id.nav_report) {
                startActivity(new Intent(this, ReportActivity.class));
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, ProductsActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupTabs() {
        int[] tabIds = {R.id.tab_hot_meal, R.id.tab_fnb, R.id.tab_souvenir, R.id.tab_business, R.id.tab_other};
        String[] categories = {"HOT_MEAL", "FNB", "MER", "SBOSS", "OTHER"};
        for (int i = 0; i < tabIds.length; i++) {
            int finalI = i;
            findViewById(tabIds[i]).setOnClickListener(v -> {
                currentCategory = categories[finalI];
                updateTabs();
                filterByCategory();
            });
        }
    }

    private void updateTabs() {
        int[] tabIds = {R.id.tab_hot_meal, R.id.tab_fnb, R.id.tab_souvenir, R.id.tab_business, R.id.tab_other};
        for (int id : tabIds) {
            TextView tab = findViewById(id);
            tab.setTextColor(Color.parseColor("#999999"));
            tab.setBackgroundResource(R.drawable.tab_unselected);
        }
        int selectedTabId;
        switch (currentCategory) {
            case "HOT_MEAL":
                selectedTabId = R.id.tab_hot_meal;
                break;
            case "FNB":
                selectedTabId = R.id.tab_fnb;
                break;
            case "MER":
                selectedTabId = R.id.tab_souvenir;
                break;
            case "SBOSS":
                selectedTabId = R.id.tab_business;
                break;
            case "OTHER":
                selectedTabId = R.id.tab_other;
                break;
            default:
                selectedTabId = R.id.tab_hot_meal;
                break;
        }
        TextView selectedTab = findViewById(selectedTabId);
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.vietjet_red));
        selectedTab.setBackgroundResource(R.drawable.tab_selected);
    }

    private void loadHandoverData() {
        String date = etFlightDate.getText().toString().trim();
        String code = etFlightCode.getText().toString().trim().toUpperCase();
        String aircraft = spinnerAircraftCode.getSelectedItem().toString();
        String flightType = spinnerFlightType.getSelectedItem().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("handovers")
                .whereEqualTo("flightDate", date)
                .whereEqualTo("flightNumber", code)
                .whereEqualTo("aircraftId", aircraft)
                .whereEqualTo("handoverType", flightType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String status = doc.getString("status");

                        // Lấy role của user hiện tại
                        String myRole = sharedPreferences.getString("role", "");
                        // Lấy role của người tạo bàn giao
                        Map<String, Object> createdBy = (Map<String, Object>) doc.get("createdBy");
                        String creatorRole = createdBy != null ? (String) createdBy.get("role") : "";

                        // So sánh quyền
                        boolean canReceive = false;
                        if ("supply_staff".equalsIgnoreCase(myRole) && "flight_attendant".equalsIgnoreCase(creatorRole)) {
                            canReceive = true;
                        } else if ("flight_attendant".equalsIgnoreCase(myRole) && "supply_staff".equalsIgnoreCase(creatorRole)) {
                            canReceive = true;
                        }

                        if (!canReceive) {
                            btnConfirm.setVisibility(View.GONE);
                            Toast.makeText(this, "Bạn không có quyền nhận bàn giao này!", Toast.LENGTH_LONG).show();
                        } else if ("received".equalsIgnoreCase(status)) {
                            btnConfirm.setVisibility(View.GONE);
                            Toast.makeText(this, "Bàn giao đã được xác nhận, không thể nhận thêm!", Toast.LENGTH_SHORT).show();
                        } else {
                            btnConfirm.setVisibility(View.VISIBLE);
                        }

                        // Load sản phẩm như cũ
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        productList.clear();
                        for (Map<String, Object> map : items) {
                            Product p = new Product();
                            p.setCode((String) map.get("code"));
                            p.setName((String) map.get("name"));
                            p.setCategory((String) map.get("category"));
                            p.setImageName((String) map.get("imageName"));
                            p.setImageUrl((String) map.get("imageUrl"));
                            p.setPrice(((Number) map.get("price")).intValue());
                            p.setQuantity(((Number) map.get("quantity")).intValue());
                            productList.add(p);
                        }
                        filterByCategory();
                    } else {
                        btnConfirm.setVisibility(View.GONE);
                        Toast.makeText(this, "Không tìm thấy bàn giao phù hợp!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void filterByCategory() {
        List<Product> filtered = new ArrayList<>();
        int total = 0;
        for (Product p : productList) {
            if (p.getCategory().equalsIgnoreCase(currentCategory)) {
                filtered.add(p);
                total += p.getQuantity();
            }
        }
        adapter.updateList(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvTotalCount.setText("Tổng cộng: " + total);
    }

    private void filterBySearch(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    private void confirmReceive() {
        String date = etFlightDate.getText().toString().trim();
        String code = etFlightCode.getText().toString().trim().toUpperCase();
        String aircraft = spinnerAircraftCode.getSelectedItem().toString();
        String flightType = spinnerFlightType.getSelectedItem().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = sharedPreferences.getString("user_id", "");
        String fullname = sharedPreferences.getString("fullname", "");

        db.collection("handovers")
                .whereEqualTo("flightDate", date)
                .whereEqualTo("flightNumber", code)
                .whereEqualTo("aircraftId", aircraft)
                .whereEqualTo("handoverType", flightType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String status = doc.getString("status");
                        if ("received".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Bàn giao đã được xác nhận!", Toast.LENGTH_SHORT).show();
                            btnConfirm.setVisibility(View.GONE);
                            return;
                        }
                        DocumentReference docRef = doc.getReference();
                        docRef.update(
                                "status", "received",
                                "isLocked", true,
                                "receivedByUserId", userId,
                                "receivedByUserName", fullname,
                                "receivedAt", System.currentTimeMillis()
                        ).addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Xác nhận nhận bàn giao thành công", Toast.LENGTH_SHORT).show();
                            btnConfirm.setVisibility(View.GONE);
                        }).addOnFailureListener(e ->
                                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        Toast.makeText(this, "Không tìm thấy bàn giao để cập nhật", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi truy vấn dữ liệu", Toast.LENGTH_SHORT).show()
                );
    }



    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
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



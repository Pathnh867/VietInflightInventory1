package com.vietflight.inventory.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.ProductHandoverAdapter;
import com.vietflight.inventory.models.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateHandoverActivity extends AppCompatActivity {

    private EditText etFlightDate, etFlightCode;
    private Spinner spinnerAircraft, spinnerFlightType;
    private Button btnLoad, btnCreateHandover;
    private TextView tvTotalCount;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmpty;

    private FirebaseFirestore db;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> currentTabProducts = new ArrayList<>();
    private Map<String, Integer> quantityMap = new HashMap<>();

    private ImageView btnMenu;
    private String handoverId = null;
    private ProductHandoverAdapter adapter;
    private String currentCategory = "HOT_MEAL";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_handover);

        db = FirebaseFirestore.getInstance();
        initUI();
        setupSpinners();
        setupDatePicker();
        setupCategoryTabs();
        setupButtons();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);
        setupNavHeader(navView);

        String role = sharedPreferences.getString("role", "");
        android.view.Menu menu = navView.getMenu();

        if ("admin".equalsIgnoreCase(role)) {
            menu.findItem(R.id.nav_create).setVisible(false);
            menu.findItem(R.id.nav_receive).setVisible(false);
            menu.findItem(R.id.nav_report).setVisible(false);
            menu.findItem(R.id.nav_create_user).setVisible(true);
            menu.findItem(R.id.nav_manage_user).setVisible(true);
            menu.findItem(R.id.nav_products).setVisible(true);
        } else {
            menu.findItem(R.id.nav_create).setVisible(true);
            menu.findItem(R.id.nav_receive).setVisible(true);
            menu.findItem(R.id.nav_report).setVisible(true);
            menu.findItem(R.id.nav_create_user).setVisible(false);
            menu.findItem(R.id.nav_manage_user).setVisible(false);
            menu.findItem(R.id.nav_products).setVisible(false);
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

        handoverId = getIntent().getStringExtra("handoverId"); // Nếu null tức là tạo mới
        if (handoverId != null) {
            loadHandoverFromFirestore(handoverId);
        }

    }

    private void loadHandoverFromFirestore(String handoverId) {
        db.collection("handovers").document(handoverId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean isLocked = doc.getBoolean("isLocked");

                        String flightDate = doc.getString("flightDate");
                        if (flightDate != null) etFlightDate.setText(flightDate);

                        String flightCode = doc.getString("flightCode");
                        if (flightCode != null) etFlightCode.setText(flightCode);

                        String aircraft = doc.getString("aircraftCode");
                        if (aircraft != null) setSpinnerToValue(spinnerAircraft, aircraft);

                        String flightType = doc.getString("flightType");
                        if (flightType != null) setSpinnerToValue(spinnerFlightType, flightType);

                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        allProducts.clear();
                        quantityMap.clear();
                        for (Map<String, Object> map : items) {
                            Product p = new Product();
                            p.setId((String) map.get("id")); // DÙNG id (document id sản phẩm Firestore)
                            p.setCode((String) map.get("code"));
                            p.setName((String) map.get("name"));
                            p.setCategory((String) map.get("category"));
                            p.setImageName((String) map.get("imageName"));
                            p.setImageUrl((String) map.get("imageUrl"));
                            p.setImageUrl((String) map.get("imageUrl"));
                            p.setPrice(((Number) map.get("price")).intValue());
                            int qty = ((Number) map.get("quantity")).intValue();
                            allProducts.add(p);
                            quantityMap.put(p.getId(), qty); // key là id
                        }
                        filterByCategory(currentCategory);

                        // Gọi hàm handleLockedState để khóa/mở giao diện
                        handleLockedState(isLocked != null && isLocked);
                    }
                });
    }

    private void handleLockedState(boolean isLocked) {
        adapter.setEditable(!isLocked);
        btnCreateHandover.setEnabled(!isLocked);
        btnCreateHandover.setVisibility(isLocked ? View.GONE : View.VISIBLE);

        if (isLocked) {
            Toast.makeText(this, "Bàn giao đã bị khóa, bạn không thể chỉnh sửa!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        etFlightDate = findViewById(R.id.et_flight_date);
        etFlightCode = findViewById(R.id.et_flight_code);
        spinnerAircraft = findViewById(R.id.spinner_aircraft_code);
        spinnerFlightType = findViewById(R.id.spinner_flight_type);
        btnLoad = findViewById(R.id.btn_load);
        btnCreateHandover = findViewById(R.id.btn_create_handover);
        tvTotalCount = findViewById(R.id.tv_total_count);
        layoutEmpty = findViewById(R.id.layout_empty);
        rvProducts = findViewById(R.id.rv_products);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductHandoverAdapter(currentTabProducts, quantityMap, this::updateTotalCount);
        rvProducts.setAdapter(adapter);
    }

    private void setupSpinners() {
        db.collection("aircraft")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> aircraftCodes = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        String code = doc.getString("code");
                        if (code != null) aircraftCodes.add(code);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, aircraftCodes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAircraft.setAdapter(adapter);
                });

        List<String> flightTypes = Arrays.asList("COM 2025", "TOP-UP", "COM INDIA");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, flightTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFlightType.setAdapter(typeAdapter);
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

    private void setupCategoryTabs() {
        findViewById(R.id.tab_hot_meal).setOnClickListener(v -> filterByCategory("HOT_MEAL"));
        findViewById(R.id.tab_fnb).setOnClickListener(v -> filterByCategory("FNB"));
        findViewById(R.id.tab_souvenir).setOnClickListener(v -> filterByCategory("MER"));
        findViewById(R.id.tab_business).setOnClickListener(v -> filterByCategory("SBOSS"));
        findViewById(R.id.tab_other).setOnClickListener(v -> filterByCategory("OTHER"));
    }

    private void filterByCategory(String category) {
        currentCategory = category;

        // ✅ Reset tab UI
        findViewById(R.id.tab_hot_meal).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_fnb).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_souvenir).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_business).setBackgroundResource(R.drawable.tab_unselected);
        findViewById(R.id.tab_other).setBackgroundResource(R.drawable.tab_unselected);

        // ✅ Đánh dấu tab đang chọn
        switch (category) {
            case "HOT_MEAL":
                findViewById(R.id.tab_hot_meal).setBackgroundResource(R.drawable.tab_selected);
                break;
            case "FNB":
                findViewById(R.id.tab_fnb).setBackgroundResource(R.drawable.tab_selected);
                break;
            case "MER":
                findViewById(R.id.tab_souvenir).setBackgroundResource(R.drawable.tab_selected);
                break;
            case "SBOSS":
                findViewById(R.id.tab_business).setBackgroundResource(R.drawable.tab_selected);
                break;
            case "OTHER":
                findViewById(R.id.tab_other).setBackgroundResource(R.drawable.tab_selected);
                break;
        }

        // ✅ Lọc dữ liệu như cũ
        currentTabProducts.clear();
        for (Product p : allProducts) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                currentTabProducts.add(p);
            }
        }

        layoutEmpty.setVisibility(currentTabProducts.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
        updateTotalCount();
    }


    private void updateTotalCount() {
        int total = 0;
        for (int qty : quantityMap.values()) total += qty;
        tvTotalCount.setText("Tổng cộng: " + total);
    }

    private void setupButtons() {
        btnLoad.setOnClickListener(v -> loadProductTemplates());
        btnCreateHandover.setOnClickListener(v -> createHandover());
    }

    private void loadProductTemplates() {
        final String flightDate = etFlightDate.getText().toString().trim();
        final String flightCode = etFlightCode.getText().toString().trim().toUpperCase();
        final String aircraftCode = spinnerAircraft.getSelectedItem() != null ? spinnerAircraft.getSelectedItem().toString() : "";
        final String flightType = spinnerFlightType.getSelectedItem() != null ? spinnerFlightType.getSelectedItem().toString() : "";

        if (flightDate.isEmpty() || flightCode.isEmpty() || aircraftCode.isEmpty() || flightType.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin chuyến bay!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm xem đã có handover cho chuyến này chưa
        db.collection("handovers")
                .whereEqualTo("flightDate", flightDate)
                .whereEqualTo("flightNumber", flightCode)
                .whereEqualTo("aircraftId", aircraftCode)
                .whereEqualTo("handoverType", flightType)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // ĐÃ CÓ BÀN GIAO: load lại bàn giao cũ lên UI
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        handoverId = doc.getId(); // Lưu lại để nếu cần update
                        Boolean isLocked = doc.getBoolean("isLocked");

                        // Gán dữ liệu chuyến bay lên UI nếu cần
                        etFlightDate.setText(doc.getString("flightDate"));
                        etFlightCode.setText(doc.getString("flightCode"));
                        // Nếu muốn set lại spinner:
                        String aircraft = doc.getString("aircraftCode");
                        if (aircraft != null) setSpinnerToValue(spinnerAircraft, aircraft);
                        String type = doc.getString("flightType");
                        if (type != null) setSpinnerToValue(spinnerFlightType, type);

                        // Load sản phẩm
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        allProducts.clear();
                        quantityMap.clear();
                        for (Map<String, Object> map : items) {
                            Product p = new Product();
                            p.setId((String) map.get("id"));
                            p.setCode((String) map.get("code"));
                            p.setName((String) map.get("name"));
                            p.setCategory((String) map.get("category"));
                            p.setImageName((String) map.get("imageName"));
                            p.setPrice(((Number) map.get("price")).intValue());
                            int qty = ((Number) map.get("quantity")).intValue();
                            allProducts.add(p);
                            quantityMap.put(p.getId(), qty);
                        }
                        filterByCategory(currentCategory);

                        // Gọi hàm khóa nếu bị khóa
                        handleLockedState(isLocked != null && isLocked);

                        Toast.makeText(this, "Đã tải bàn giao đã tạo!", Toast.LENGTH_SHORT).show();
                    } else {
                        // CHƯA CÓ BÀN GIAO: tạo mới template như cũ
                        loadNewProductTemplatesByFlightType(flightType);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải bàn giao!", Toast.LENGTH_SHORT).show();
                });
    }

    /** Hàm này dùng để set spinner về đúng giá trị khi load bàn giao cũ */
    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /** Logic load sản phẩm template khi CHƯA có bàn giao */
    private void loadNewProductTemplatesByFlightType(String flightType) {
        db.collection("products")
                .whereEqualTo("is_active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allProducts.clear();
                    quantityMap.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Product p = doc.toObject(Product.class);
                        if (p == null) continue;
                        p.setId(doc.getId());
                        allProducts.add(p);

                        int defaultQty = 0;
                        if (!"TOP-UP".equalsIgnoreCase(flightType)) {
                            switch (flightType) {
                                case "COM 2025":
                                    if ("HOT_MEAL".equalsIgnoreCase(p.getCategory())) defaultQty = 30;
                                    else if ("FNB".equalsIgnoreCase(p.getCategory())) defaultQty = 50;
                                    else if ("MER".equalsIgnoreCase(p.getCategory())) defaultQty = 5;
                                    else if ("SBOSS".equalsIgnoreCase(p.getCategory())) defaultQty = 2;
                                    break;
                                case "COM INDIA":
                                    if ("HOT_MEAL".equalsIgnoreCase(p.getCategory())) defaultQty = 25;
                                    else if ("FNB".equalsIgnoreCase(p.getCategory())) defaultQty = 35;
                                    else if ("MER".equalsIgnoreCase(p.getCategory())) defaultQty = 3;
                                    break;
                            }
                        }
                        quantityMap.put(p.getId(), defaultQty);
                    }
                    filterByCategory(currentCategory);
                    btnCreateHandover.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show());
    }


    private void createHandover() {
        String flightDate = etFlightDate.getText().toString().trim();
        String flightCode = etFlightCode.getText().toString().trim();
        String aircraftCode = spinnerAircraft.getSelectedItem() != null ? spinnerAircraft.getSelectedItem().toString() : "";
        String flightType = spinnerFlightType.getSelectedItem() != null ? spinnerFlightType.getSelectedItem().toString() : "";

        if (flightDate.isEmpty() || flightCode.isEmpty() || aircraftCode.isEmpty() || quantityMap.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin và chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người tạo bàn giao
        String userId = sharedPreferences.getString("user_id", "");
        String fullname = sharedPreferences.getString("fullname", "");
        String role = sharedPreferences.getString("role", "");
        String employeeId = sharedPreferences.getString("employee_id", "");
        String department = sharedPreferences.getString("department", "");

        Map<String, Object> createdBy = new HashMap<>();
        createdBy.put("user_id", userId);
        createdBy.put("fullname", fullname);
        createdBy.put("role", role);
        createdBy.put("employee_id", employeeId);
        createdBy.put("department", department);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Product p : allProducts) {
            int qty = quantityMap.getOrDefault(p.getId(), 0);
            if (qty > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("code", p.getCode());
                item.put("name", p.getName());
                item.put("category", p.getCategory());
                item.put("imageName", p.getImageName());
                item.put("imageUrl", p.getImageUrl());
                item.put("price", p.getPrice());
                item.put("quantity", qty);
                items.add(item);
            }
        }

        Map<String, Object> handover = new HashMap<>();
        handover.put("flightNumber", flightCode);
        handover.put("flightDate", flightDate);
        handover.put("aircraftId", aircraftCode);
        handover.put("handoverType", flightType);
        handover.put("items", items);
        handover.put("received", false);
        handover.put("status", "pending");
        handover.put("isLocked", false);
        handover.put("createdBy", createdBy);
        handover.put("createdAt", System.currentTimeMillis());

        db.collection("handovers")
                .add(handover)
                .addOnSuccessListener(doc -> {
                    handoverId = doc.getId();
                    loadHandoverFromFirestore(handoverId);
                    Toast.makeText(this, "Đã tạo bàn giao thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo bàn giao", Toast.LENGTH_SHORT).show());
    }
    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupNavHeader(NavigationView navView){
        android.view.View header = navView.getHeaderView(0);
        if (header != null){
            TextView tvAvatar = header.findViewById(R.id.tv_nav_avatar);
            TextView tvFullname = header.findViewById(R.id.tv_nav_fullname);
            String fullname = sharedPreferences.getString("fullname","");
            tvFullname.setText(fullname);
            if (!TextUtils.isEmpty(fullname)){
                String[] parts = fullname.trim().split("\\s+");
                String initials = "";
                if (parts.length >= 2) {
                    initials += parts[parts.length - 2].substring(0,1);
                    initials += parts[parts.length - 1].substring(0,1);
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



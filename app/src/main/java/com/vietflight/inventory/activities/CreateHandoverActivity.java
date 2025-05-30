package com.vietflight.inventory.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.ProductHandoverAdapter;
import com.vietflight.inventory.models.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateHandoverActivity extends AppCompatActivity {

    private EditText etFlightDate, etFlightCode, etSearch;
    private Spinner spinnerAircraftCode, spinnerFlightType;
    private Button btnClose, btnLoad, btnCreateHandover;
    private ImageView btnMenu;
    private TextView tvTotalCount;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmpty;

    // Category tabs
    private TextView tabHotMeal, tabFnb, tabSouvenir, tabBusiness, tabOther;
    private TextView currentSelectedTab;

    // Data
    private FirebaseFirestore db;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private ProductHandoverAdapter adapter;
    private String selectedCategory = "HOT_MEAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_handover);

        initViews();
        initFirestore();
        setupSpinners();
        setupTabs();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();

        // Load products for default category
        loadProducts();
    }

    private void initViews() {
        etFlightDate = findViewById(R.id.et_flight_date);
        etFlightCode = findViewById(R.id.et_flight_code);
        etSearch = findViewById(R.id.et_search);
        spinnerAircraftCode = findViewById(R.id.spinner_aircraft_code);
        spinnerFlightType = findViewById(R.id.spinner_flight_type);
        btnClose = findViewById(R.id.btn_close);
        btnLoad = findViewById(R.id.btn_load);
        btnCreateHandover = findViewById(R.id.btn_create_handover);
        btnMenu = findViewById(R.id.btn_menu);
        tvTotalCount = findViewById(R.id.tv_total_count);
        rvProducts = findViewById(R.id.rv_products);
        layoutEmpty = findViewById(R.id.layout_empty);

        // Category tabs
        tabHotMeal = findViewById(R.id.tab_hot_meal);
        tabFnb = findViewById(R.id.tab_fnb);
        tabSouvenir = findViewById(R.id.tab_souvenir);
        tabBusiness = findViewById(R.id.tab_business);
        tabOther = findViewById(R.id.tab_other);

        currentSelectedTab = tabHotMeal;
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupSpinners() {
        // Aircraft Code Spinner
        String[] aircraftCodes = {"Chọn mã tàu", "VN-A629", "VN-A630", "VN-A631", "VN-A632"};
        ArrayAdapter<String> aircraftAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, aircraftCodes);
        aircraftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAircraftCode.setAdapter(aircraftAdapter);

        // Flight Type Spinner
        String[] flightTypes = {"Loại chuyến bay", "COM 2025", "TOP-UP", "COM INDIA"};
        ArrayAdapter<String> flightAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, flightTypes);
        flightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFlightType.setAdapter(flightAdapter);
    }

    private void setupTabs() {
        tabHotMeal.setOnClickListener(v -> selectTab(tabHotMeal, "HOT_MEAL"));
        tabFnb.setOnClickListener(v -> selectTab(tabFnb, "FNB"));
        tabSouvenir.setOnClickListener(v -> selectTab(tabSouvenir, "SOUVENIR"));
        tabBusiness.setOnClickListener(v -> selectTab(tabBusiness, "BUSINESS"));
        tabOther.setOnClickListener(v -> selectTab(tabOther, "OTHER"));
    }

    private void selectTab(TextView tab, String category) {
        // Reset previous tab
        currentSelectedTab.setBackgroundResource(R.drawable.tab_unselected);
        currentSelectedTab.setTextColor(getResources().getColor(R.color.text_secondary));

        // Set new tab
        currentSelectedTab = tab;
        tab.setBackgroundResource(R.drawable.tab_selected);
        tab.setTextColor(getResources().getColor(R.color.vietjet_red));

        selectedCategory = category;
        loadProducts();
        String flightType = spinnerFlightType.getSelectedItem().toString();
        if (!flightType.equals("Loại chuyến bay")) {
            loadDefaultQuantities(flightType);
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductHandoverAdapter(filteredProducts, new ProductHandoverAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged() {
                updateTotalCount();
                updateCreateButtonVisibility();
            }
        });
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });

        etFlightDate.setOnClickListener(v -> showDatePicker());

        btnClose.setOnClickListener(v -> onBackPressed());

        btnLoad.setOnClickListener(v -> loadFlightData());

        btnCreateHandover.setOnClickListener(v -> createHandover());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etFlightDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadProducts() {
        // For demo purposes, create sample products
        if (allProducts.isEmpty()) {
            loadAllProducts();
        }

        // Filter theo category hiện tại
        filterProducts(etSearch.getText().toString());
    }

    private void createSampleProducts() {
        allProducts.clear();

        switch (selectedCategory) {
            case "HOT_MEAL":
                allProducts.add(new Product("HM001", "Cơm gà teriyaki", "HOT_MEAL", "suất", 85000));
                allProducts.add(new Product("HM002", "Cơm bò lúc lắc", "HOT_MEAL", "suất", 95000));
                allProducts.add(new Product("HM003", "Mì Ý sốt bò bằm", "HOT_MEAL", "suất", 90000));
                allProducts.add(new Product("HM004", "Cơm chiên dương châu", "HOT_MEAL", "suất", 80000));
                break;

            case "FNB":
                allProducts.add(new Product("FB001", "Nước suối 500ml", "FNB", "chai", 15000));
                allProducts.add(new Product("FB002", "Coca Cola", "FNB", "lon", 25000));
                allProducts.add(new Product("FB003", "Cà phê đen", "FNB", "ly", 35000));
                allProducts.add(new Product("FB004", "Trà xanh", "FNB", "ly", 30000));
                allProducts.add(new Product("FB005", "Bánh quy", "FNB", "gói", 20000));
                break;

            case "SOUVENIR":
                allProducts.add(new Product("SV001", "Móc khóa VietJet", "SOUVENIR", "cái", 50000));
                allProducts.add(new Product("SV002", "Áo thun VietJet", "SOUVENIR", "cái", 250000));
                allProducts.add(new Product("SV003", "Mũ VietJet", "SOUVENIR", "cái", 150000));
                break;

            case "BUSINESS":
                allProducts.add(new Product("BS001", "Set ăn Business", "BUSINESS", "set", 200000));
                allProducts.add(new Product("BS002", "Rượu vang đỏ", "BUSINESS", "chai", 300000));
                break;

            case "OTHER":
                allProducts.add(new Product("OT001", "Khăn ướt", "OTHER", "gói", 5000));
                allProducts.add(new Product("OT002", "Túi giấy", "OTHER", "cái", 3000));
                break;
        }

        filterProducts(etSearch.getText().toString());
    }

    private void filterProducts(String query) {
        filteredProducts.clear();

        for (Product product : allProducts) {
            // Sử dụng getCategoryName() thay vì getCategory()
            String productCategory = product.getCategoryName();
            if (productCategory == null) {
                continue; // Bỏ qua sản phẩm không có category
            }

            // Lọc theo category VÀ search query
            boolean matchCategory = productCategory.equals(selectedCategory);
            boolean matchQuery = query.isEmpty() ||
                    product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCode().toLowerCase().contains(query.toLowerCase());

            if (matchCategory && matchQuery) {
                filteredProducts.add(product);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
        updateTotalCount();
    }

    private void updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void updateTotalCount() {
        int totalCount = 0;
        for (Product product : allProducts) {
            totalCount += product.getQuantity();
        }
        tvTotalCount.setText("Tổng cộng: " + totalCount);
    }

    private void updateCreateButtonVisibility() {
        boolean hasQuantity = false;
        for (Product product : allProducts) {
            if (product.getQuantity() > 0) {
                hasQuantity = true;
                break;
            }
        }
        btnCreateHandover.setVisibility(hasQuantity ? View.VISIBLE : View.GONE);
    }

    private void loadFlightData() {
        String flightDate = etFlightDate.getText().toString().trim();
        String flightCode = etFlightCode.getText().toString().trim();
        String aircraftCode = spinnerAircraftCode.getSelectedItem().toString();
        String flightType = spinnerFlightType.getSelectedItem().toString();

        if (validateFlightInput(flightDate, flightCode, aircraftCode)) {
            Toast.makeText(this, "Đã tải dữ liệu chuyến bay", Toast.LENGTH_SHORT).show();
            loadDefaultQuantities(flightType);
        }
    }

    private void loadDefaultQuantities(String flightType) {
        // Load default quantities based on flight type
        for (Product product : allProducts) {
           switch (flightType){
               case "COM 2025":
                   product.setQuantity(getDefaultDomesticQuantity(product.getCode()));
                   break;
               case "TOP-UP":
                    product.setQuantity(0);
                    break;
               case "COM INDIA":
                   product.setQuantity(getDefaultInternationalQuantity(product.getCode()));
                   break;
           }
        }
        adapter.notifyDataSetChanged();
        updateTotalCount();
        updateCreateButtonVisibility();
    }

    private int getDefaultDomesticQuantity(String productCode) {
        switch (productCode) {
            case "HM001": return 30;
            case "HM002": return 25;
            case "FB001": return 100;
            case "FB002": return 50;
            case "FB003": return 30;
            case "FB004": return 40;
            case "SV001": return 2;
            case "SV002": return 3;
            default: return 0;
        }
    }

    private int getDefaultInternationalQuantity(String productCode) {
        switch (productCode) {
            case "HM001": return 50;
            case "HM002": return 40;
            case "FB001": return 150;
            case "FB002": return 80;
            case "FB003": return 50;
            default: return 0;
        }
    }

    private void createHandover() {
        String flightDate = etFlightDate.getText().toString().trim();
        String flightCode = etFlightCode.getText().toString().trim();
        String aircraftCode = spinnerAircraftCode.getSelectedItem().toString();
        String flightType = spinnerFlightType.getSelectedItem().toString();

        if (!validateInput(flightDate, flightCode, aircraftCode, flightType)) {
            return;
        }

        // Tạo danh sách sản phẩm có số lượng > 0
        List<Map<String, Object>> items = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getQuantity() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("code", product.getCode());
                item.put("name", product.getName());
                item.put("quantity", product.getQuantity());
                item.put("price", product.getPrice());
                item.put("category", product.getCategoryName());
                items.add(item);
            }
        }

        // Tạo handover với đầy đủ thông tin
        Map<String, Object> handover = new HashMap<>();
        handover.put("flightDate", flightDate);
        handover.put("flightCode", flightCode);
        handover.put("aircraftCode", aircraftCode);
        handover.put("flightType", flightType);
        handover.put("items", items);                    // ← QUAN TRỌNG!
        handover.put("totalItems", getTotalQuantity());
        handover.put("createdAt", System.currentTimeMillis());
        handover.put("status", "CREATED");

        // Lưu vào Firebase
        FirebaseFirestore.getInstance()
                .collection("handovers")
                .add(handover)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "✅ Lưu thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateHandoverActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private int getTotalQuantity() {
        int total = 0;
        for (Product product : allProducts) {
            total += product.getQuantity();
        }
        return total;
    }
    private boolean validateInput(String flightDate, String flightCode, String aircraftCode, String flightType) {
        if (flightDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày bay", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (flightCode.isEmpty()) {
            etFlightCode.setError("Vui lòng nhập mã chuyến bay");
            etFlightCode.requestFocus();
            return false;
        }
        if (aircraftCode.equals("Chọn mã tàu")) {
            Toast.makeText(this, "Vui lòng chọn mã tàu", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (flightType.equals("Loại chuyến bay")) {
            Toast.makeText(this, "Vui lòng chọn loại chuyến bay", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateFlightInput(String flightDate, String flightCode, String aircraftCode) {
        if (flightDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày bay", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (flightCode.isEmpty()) {
            etFlightCode.setError("Vui lòng nhập mã chuyến bay");
            etFlightCode.requestFocus();
            return false;
        }
        if (aircraftCode.equals("Chọn mã tàu")) {
            Toast.makeText(this, "Vui lòng chọn mã tàu", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void loadAllProducts() {
        allProducts.clear();

        // HOT_MEAL
        Product hm001 = new Product("HM001", "Cơm gà teriyaki", "HOT_MEAL", "suất", 85000);
        hm001.setCategoryName("HOT_MEAL"); // Set categoryName để filter
        allProducts.add(hm001);

        Product hm002 = new Product("HM002", "Cơm bò lúc lắc", "HOT_MEAL", "suất", 95000);
        hm002.setCategoryName("HOT_MEAL");
        allProducts.add(hm002);

        Product hm003 = new Product("HM003", "Mì Ý sốt bò bằm", "HOT_MEAL", "suất", 90000);
        hm003.setCategoryName("HOT_MEAL");
        allProducts.add(hm003);

        Product hm004 = new Product("HM004", "Cơm chiên dương châu", "HOT_MEAL", "suất", 80000);
        hm004.setCategoryName("HOT_MEAL");
        allProducts.add(hm004);

        // FNB
        Product fb001 = new Product("FB001", "Nước suối 500ml", "FNB", "chai", 15000);
        fb001.setCategoryName("FNB");
        allProducts.add(fb001);

        Product fb002 = new Product("FB002", "Coca Cola", "FNB", "lon", 25000);
        fb002.setCategoryName("FNB");
        allProducts.add(fb002);

        Product fb003 = new Product("FB003", "Cà phê đen", "FNB", "ly", 35000);
        fb003.setCategoryName("FNB");
        allProducts.add(fb003);

        Product fb004 = new Product("FB004", "Trà xanh", "FNB", "ly", 30000);
        fb004.setCategoryName("FNB");
        allProducts.add(fb004);

        Product fb005 = new Product("FB005", "Bánh quy", "FNB", "gói", 20000);
        fb005.setCategoryName("FNB");
        allProducts.add(fb005);

        // SOUVENIR
        Product sv001 = new Product("SV001", "Móc khóa VietJet", "SOUVENIR", "cái", 50000);
        sv001.setCategoryName("SOUVENIR");
        allProducts.add(sv001);

        Product sv002 = new Product("SV002", "Áo thun VietJet", "SOUVENIR", "cái", 250000);
        sv002.setCategoryName("SOUVENIR");
        allProducts.add(sv002);

        Product sv003 = new Product("SV003", "Mũ VietJet", "SOUVENIR", "cái", 150000);
        sv003.setCategoryName("SOUVENIR");
        allProducts.add(sv003);

        // BUSINESS
        Product bs001 = new Product("BS001", "Set ăn Business", "BUSINESS", "set", 20000);
        bs001.setCategoryName("BUSINESS");
        allProducts.add(bs001);

        Product bs002 = new Product("BS002", "Rượu vang đỏ", "BUSINESS", "chai", 30000);
        bs002.setCategoryName("BUSINESS");
        allProducts.add(bs002);

        // OTHER
        Product ot001 = new Product("OT001", "Khăn ướt", "OTHER", "gói", 5000);
        ot001.setCategoryName("OTHER");
        allProducts.add(ot001);

        Product ot002 = new Product("OT002", "Túi giấy", "OTHER", "cái", 3000);
        ot002.setCategoryName("OTHER");
        allProducts.add(ot002);
    }
}
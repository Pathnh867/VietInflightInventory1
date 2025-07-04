package com.vietflight.inventory.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vietflight.inventory.R;
import com.vietflight.inventory.adapters.ReportAdapter;
import com.vietflight.inventory.models.Handover;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ReportActivity extends AppCompatActivity {

    private RecyclerView rvReport;
    private LinearLayout layoutEmpty;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ReportAdapter adapter;
    private List<Handover> handoverList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private EditText etReportDate;
    private Button btnLoadReport;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);

        rvReport = findViewById(R.id.rv_report);
        layoutEmpty = findViewById(R.id.layout_empty_report);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        rvReport.setLayoutManager(new LinearLayoutManager(this));
        String myUserIdAdapter = sharedPreferences.getString("user_id", "");
        String myEmployeeIdAdapter = sharedPreferences.getString("employee_id", "");
        adapter = new ReportAdapter(this, handoverList, myUserIdAdapter, myEmployeeIdAdapter,
                (handover, pos) -> unlockHandover(handover, pos));
        rvReport.setAdapter(adapter);
        etReportDate = findViewById(R.id.et_report_date);
        btnLoadReport = findViewById(R.id.btn_load_report);

        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        setupNavigationMenu();
        etReportDate.setOnClickListener(v -> showDatePickerDialog());

        btnLoadReport.setOnClickListener(v -> loadFilteredHandovers());

        NavigationView navView = findViewById(R.id.nav_view);

// Lấy role từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("VietFlightPrefs", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "");

// Lấy Menu từ NavigationView
        Menu menu = navView.getMenu();

        MenuItem createUserItem = menu.findItem(R.id.nav_create_user);
        MenuItem manageUserItem = menu.findItem(R.id.nav_manage_user);
        MenuItem createHandoverItem = menu.findItem(R.id.nav_create);
        MenuItem receiveHandoverItem = menu.findItem(R.id.nav_receive);
        MenuItem reportItem = menu.findItem(R.id.nav_report);

        if ("admin".equalsIgnoreCase(role)) {
            // Nếu là admin thì chỉ hiện phần quản lý user, ẩn các item bàn giao
            if (createUserItem != null) createUserItem.setVisible(true);
            if (manageUserItem != null) manageUserItem.setVisible(true);
            if (createHandoverItem != null) createHandoverItem.setVisible(false);
            if (receiveHandoverItem != null) receiveHandoverItem.setVisible(false);
            if (reportItem != null) reportItem.setVisible(false);
            menu.findItem(R.id.nav_products).setVisible(true);
        } else {
            // Nếu là user thường thì ngược lại
            if (createUserItem != null) createUserItem.setVisible(false);
            if (manageUserItem != null) manageUserItem.setVisible(false);

            if (createHandoverItem != null) createHandoverItem.setVisible(true);
            if (receiveHandoverItem != null) receiveHandoverItem.setVisible(true);
            if (reportItem != null) reportItem.setVisible(true);
        }

    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year);
            etReportDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadFilteredHandovers() {
        // Kiểm tra đã chọn ngày chưa
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUserId = sharedPreferences.getString("user_id", "");
        String myEmployeeId = sharedPreferences.getString("employee_id", "");

        db.collection("handovers")
                .whereEqualTo("flightDate", selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    handoverList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Điều kiện: mình là người tạo HOẶC mình là người nhận
                        Map<String, Object> createdBy = (Map<String, Object>) doc.get("createdBy");
                        Map<String, Object> receivedBy = (Map<String, Object>) doc.get("receivedBy");

                        String creatorId = createdBy != null ? (String) createdBy.get("user_id") : "";
                        String creatorEmp = createdBy != null ? (String) createdBy.get("employee_id") : "";
                        String receiverId = "";
                        String receiverEmp = "";
                        if (receivedBy != null) {
                            receiverId = (String) receivedBy.get("user_id");
                            Object empObj = receivedBy.get("employee_id");
                            if (empObj instanceof String) receiverEmp = (String) empObj;
                        } else if (doc.contains("receivedByUserId")) {
                            receiverId = doc.getString("receivedByUserId");
                        }

                        boolean isMine = myUserId.equals(creatorId) || myUserId.equals(receiverId)
                                || myEmployeeId.equals(creatorEmp) || myEmployeeId.equals(receiverEmp);

                        if (isMine) {
                            Handover h = doc.toObject(Handover.class);
                            if (h == null) continue;
                            h.setId(doc.getId());
                            if (createdBy != null && createdBy.get("fullname") != null) {
                                h.setCreatedByUserName((String) createdBy.get("fullname"));
                            }
                            if (receivedBy != null && receivedBy.get("fullname") != null) {
                                h.setReceivedByUserName((String) receivedBy.get("fullname"));
                                h.setReceivedByMap(receivedBy);
                                if (receivedBy.get("user_id") != null) {
                                    h.setReceivedByUserId((String) receivedBy.get("user_id"));
                                } else if (receivedBy.get("employee_id") != null) {
                                    h.setReceivedByUserId((String) receivedBy.get("employee_id"));
                                }
                            } else if (doc.contains("receivedByUserName")) {
                                h.setReceivedByUserName(doc.getString("receivedByUserName"));
                                if (doc.contains("receivedByUserId")) {
                                    Map<String, Object> rb = new HashMap<>();
                                    rb.put("user_id", doc.getString("receivedByUserId"));
                                    rb.put("fullname", doc.getString("receivedByUserName"));
                                    h.setReceivedByMap(rb);
                                    h.setReceivedByUserId(doc.getString("receivedByUserId"));
                                }
                            }
                            if (doc.contains("isLocked")) {
                                Boolean locked = doc.getBoolean("isLocked");
                                h.setLocked(locked != null && locked);
                            }
                            if (doc.contains("status")) {
                                String status = doc.getString("status");
                                h.setStatus(status != null ? status : "");
                            }

                            handoverList.add(h);
                        }
                    }
                    adapter.updateList(handoverList);
                    layoutEmpty.setVisibility(handoverList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được danh sách bàn giao", Toast.LENGTH_SHORT).show());
    }

    private void setupNavigationMenu() {
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateHandoverActivity.class));
            } else if (id == R.id.nav_receive) {
                startActivity(new Intent(this, ReceiveHandoverActivity.class));
            } else if (id == R.id.nav_report) {
                // Đang ở đây
            } else if (id == R.id.nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadAllHandovers() {
        db.collection("handovers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    handoverList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Handover h = doc.toObject(Handover.class);
                        if (h == null) continue;
                        h.setId(doc.getId());

                        // ---- Đọc các trường MAP lồng ----
                        Map<String, Object> createdBy = (Map<String, Object>) doc.get("createdBy");
                        if (createdBy != null && createdBy.get("fullname") != null) {
                            h.setCreatedByUserName((String) createdBy.get("fullname"));
                        }
                        if (createdBy != null && createdBy.get("flightCode") != null) {
                            h.setFlightNumber((String) createdBy.get("flightCode"));
                        }
                        if (createdBy != null && createdBy.get("flightType") != null) {
                            h.setHandoverType((String) createdBy.get("flightType"));
                        }
                        if (createdBy != null && createdBy.get("flightDate") != null) {
                            h.setFlightDate((String) createdBy.get("flightDate"));
                        }
                        if (createdBy != null && createdBy.get("employee_id") != null) {
                            h.setCreatedByUserId((String) createdBy.get("employee_id"));
                        }
                        if (createdBy != null && createdBy.get("department") != null) {
                            // Optional: set to notes or where you want
                        }
                        if (createdBy != null && createdBy.get("role") != null) {
                            // Optional: set to notes or where you want
                        }
                        if (createdBy != null && createdBy.get("user_id") != null) {
                            // Optional: set to notes or where you want
                        }
                        if (doc.contains("isLocked")) {
                            Boolean locked = doc.getBoolean("isLocked");
                            h.setLocked(locked != null && locked);
                        }
                        if (doc.contains("status")) {
                            String status = doc.getString("status");
                            h.setStatus(status != null ? status : "");
                        }


                        Map<String, Object> receivedBy = (Map<String, Object>) doc.get("receivedBy");
                        if (receivedBy != null && receivedBy.get("fullname") != null) {
                            h.setReceivedByUserName((String) receivedBy.get("fullname"));
                            h.setReceivedByMap(receivedBy);
                            if (receivedBy.get("employee_id") != null) {
                                h.setReceivedByUserId((String) receivedBy.get("employee_id"));
                            }
                        } else if (doc.contains("receivedByUserName")) {
                            h.setReceivedByUserName(doc.getString("receivedByUserName"));
                            if (doc.contains("receivedByUserId")) {
                                h.setReceivedByUserId(doc.getString("receivedByUserId"));
                                Map<String, Object> rb = new HashMap<>();
                                rb.put("user_id", doc.getString("receivedByUserId"));
                                rb.put("fullname", doc.getString("receivedByUserName"));
                                h.setReceivedByMap(rb);
                            }
                        }
                        // ---- END ----

                        // Tàu bay nên lấy theo aircraftCode ở ngoài
                        if (doc.getString("aircraftCode") != null) {
                            h.setAircraftId(doc.getString("aircraftCode"));
                        }


                        handoverList.add(h);
                    }
                    adapter.updateList(handoverList);
                    layoutEmpty.setVisibility(handoverList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được danh sách bàn giao", Toast.LENGTH_SHORT).show());
    }

    private void unlockHandover(Handover handover, int position) {
        // Chỉ cho phép mở khóa khi đã nhận
        if (!handover.isLocked() || !"received".equalsIgnoreCase(handover.getStatus())) {
            Toast.makeText(this, "Bàn giao chưa nhận, không cần mở khóa!", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference docRef = db.collection("handovers").document(handover.getId());
        docRef.update(
                "isLocked", false // Không đổi status về pending
        ).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Đã mở khóa bàn giao!", Toast.LENGTH_SHORT).show();
            handover.setLocked(false);
            adapter.notifyItemChanged(position);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi mở khóa!", Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

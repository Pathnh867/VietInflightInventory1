package com.vietflight.inventory.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import com.vietflight.inventory.utils.HashUtils;


public class DataSeeder {

    private static final String TAG = "DataSeeder";
    private FirebaseFirestore db;

    public DataSeeder() {
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "DataSeeder initialized");
    }

    public void seedAllData() {
        Log.d(TAG, "Starting to seed all data...");
        seedUsers();
        seedAircraft();
        seedProductCategories();
        seedProducts();
    }

    private void seedUsers() {
        Log.d(TAG, "Seeding users...");

        // User 1: Supply Staff
        Map<String, Object> user1 = new HashMap<>();
        user1.put("username", "supply01");
        user1.put("password", HashUtils.sha256("123456"));
        user1.put("fullname", "Nguyễn Văn An");
        user1.put("role", "supply_staff");
        user1.put("employee_id", "SUP001");
        user1.put("department", "Inflight Services");
        user1.put("is_active", true);

        db.collection("users").add(user1)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "User 1 added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user 1", e);
                });

        // User 2: Flight Attendant
        Map<String, Object> user2 = new HashMap<>();
        user2.put("username", "attendant01");
        user2.put("password", HashUtils.sha256("123456"));
        user2.put("fullname", "Trần Thị Bình");
        user2.put("role", "flight_attendant");
        user2.put("employee_id", "ATT001");
        user2.put("department", "Cabin Crew");
        user2.put("is_active", true);

        db.collection("users").add(user2)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "User 2 added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user 2", e);
                });

        // User 3: Admin
        Map<String, Object> user3 = new HashMap<>();
        user3.put("username", "admin");
        user3.put("password", HashUtils.sha256("admin123"));
        user3.put("fullname", "Lê Văn Quản");
        user3.put("role", "admin");
        user3.put("employee_id", "ADM001");
        user3.put("department", "IT Department");
        user3.put("is_active", true);

        db.collection("users").add(user3)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "User 3 added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user 3", e);
                });
    }

    private void seedAircraft() {
        Log.d(TAG, "Seeding aircraft...");

        String[] aircraftData = {
                "VN-A629,A320,180",
                "VN-A630,A321,230",
                "VN-A631,A330,363",
                "VN-A632,Boeing 737,189"
        };

        for (int i = 0; i < aircraftData.length; i++) {
            String data = aircraftData[i];
            String[] parts = data.split(",");
            Map<String, Object> aircraft = new HashMap<>();
            aircraft.put("code", parts[0]);
            aircraft.put("model", parts[1]);
            aircraft.put("capacity", Integer.parseInt(parts[2]));
            aircraft.put("is_active", true);

            final int index = i;
            db.collection("aircraft").add(aircraft)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Aircraft " + (index + 1) + " added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding aircraft " + (index + 1), e);
                    });
        }
    }

    private void seedProductCategories() {
        Log.d(TAG, "Seeding product categories...");

        String[] categories = {
                "Suất Ăn Nóng,1",
                "F & B,2",
                "Hàng Lưu Niệm,3",
                "Sboss Business,4"
        };

        for (int i = 0; i < categories.length; i++) {
            String data = categories[i];
            String[] parts = data.split(",");
            Map<String, Object> category = new HashMap<>();
            category.put("name", parts[0]);
            category.put("display_order", Integer.parseInt(parts[1]));
            category.put("is_active", true);

            final int index = i;
            db.collection("product_categories").add(category)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Category " + (index + 1) + " added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding category " + (index + 1), e);
                    });
        }
    }

    private void seedProducts() {
        Log.d(TAG, "Seeding products...");

        String[] products = {
        "Cơm gà teriyaki,Suất Ăn Nóng,85000,suất,product_chicken_teriyaki",
                "Mì Ý sốt bò bằm,Suất Ăn Nóng,85000,suất,product_pasta",
                "Cơm chiên dương châu,Suất Ăn Nóng,75000,suất,product_fried_rice",
                "Coca Cola,F & B,25000,lon,product_coca",
                "Pepsi,F & B,25000,lon,product_coca",
                "Nước suối,F & B,15000,chai,product_water",
                "Cà phê VietJet,F & B,35000,ly,product_coffee",
                "Trà xanh,F & B,20000,ly,product_tea",
                "Móc khóa VietJet,Hàng Lưu Niệm,50000,cái,product_keychain",
                "Áo thun VietJet,Hàng Lưu Niệm,250000,cái,product_tshirt",
                "Mũ VietJet,Hàng Lưu Niệm,150000,cái,product_cap",
                "Set ăn Business,Sboss Business,150000,set,product_business_meal"
        };

        for (int i = 0; i < products.length; i++) {
            String data = products[i];
            String[] parts = data.split(",");
            Map<String, Object> product = new HashMap<>();
            product.put("name", parts[0]);
            product.put("category", parts[1]);
            product.put("price", Integer.parseInt(parts[2]));
            product.put("unit", parts[3]);
            if (parts.length >= 5) product.put("imageName", parts[4]);
            product.put("is_active", true);

            final int index = i;
            db.collection("products").add(product)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Product " + (index + 1) + " added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding product " + (index + 1), e);
                    });
        }
    }
}
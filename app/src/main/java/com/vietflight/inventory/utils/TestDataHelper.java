package com.vietflight.inventory.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import com.vietflight.inventory.utils.HashUtils;

public class TestDataHelper {

    public static void createTestUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("username", "admin");
        user.put("password", HashUtils.sha256("123456"));
        user.put("fullName", "Administrator");
        user.put("role", "ADMIN");
        user.put("isActive", true);

        db.collection("users")
                .document("admin")
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("✅ Test user created successfully!");
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ Error creating test user: " + e.getMessage());
                });
    }
}
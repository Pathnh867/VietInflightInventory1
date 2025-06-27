package com.vietflight.inventory.utils;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PasswordMigrationUtil {
    private static final String TAG = "PasswordMigration";

    public static void migrateAllUsersToHash() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String password = doc.getString("password");
                if (password == null) continue;
                if (!password.matches("[0-9a-fA-F]{64}")) {
                    String hashed = HashUtils.sha256(password);
                    doc.getReference().update("password", hashed)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated user " + doc.getId()))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed update " + doc.getId(), e));
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Migration failed", e));
    }
}

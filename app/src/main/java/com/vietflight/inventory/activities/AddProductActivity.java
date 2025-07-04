package com.vietflight.inventory.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import com.vietflight.inventory.R;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private EditText etName, etCategory, etPrice, etUnit;
    private Uri imageUri;
    private FirebaseAuth auth;
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        ivPreview = findViewById(R.id.iv_preview);
        etName = findViewById(R.id.et_name);
        etCategory = findViewById(R.id.et_category);
        etPrice = findViewById(R.id.et_price);
        etUnit = findViewById(R.id.et_unit);
        ImageButton btnPick = findViewById(R.id.btn_pick_image);
        Button btnSave = findViewById(R.id.btn_save);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnFailureListener(e ->
                    Toast.makeText(this, "Không thể xác thực", Toast.LENGTH_SHORT).show());
        }

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(r -> saveProduct())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Không thể xác thực", Toast.LENGTH_SHORT).show());
            return;
        }
        String name = etName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("category", category);
        data.put("price", price);
        data.put("unit", unit);
        data.put("is_active", true);

        if (imageUri != null) {
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference()
                    .child("products/" + System.currentTimeMillis() + ".jpg");
            InputStream stream;
            try {
                stream = getContentResolver().openInputStream(imageUri);
                if (stream == null) {
                    Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }

                UploadTask uploadTask = ref.putStream(stream);
                uploadTask
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return ref.getDownloadUrl();
                        })
                        .addOnSuccessListener(uri -> {
                            data.put("imageUrl", uri.toString());
                            saveToFirestore(data);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show())
                        .addOnCompleteListener(task -> {
                            try {
                                stream.close();
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException e) {
                Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveToFirestore(data);
        }
    }

    private void saveToFirestore(Map<String, Object> data) {
        FirebaseFirestore.getInstance().collection("products")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Đã thêm sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi thêm sản phẩm", Toast.LENGTH_SHORT).show());
    }
}
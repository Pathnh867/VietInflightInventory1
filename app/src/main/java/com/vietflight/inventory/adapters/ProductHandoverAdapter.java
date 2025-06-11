package com.vietflight.inventory.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vietflight.inventory.R;
import com.vietflight.inventory.models.Product;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductHandoverAdapter extends RecyclerView.Adapter<ProductHandoverAdapter.ViewHolder> {

    private final List<Product> productList;
    private final Map<String, Integer> quantityMap;
    private final Runnable onQuantityChanged;
    private boolean editable = true; // mặc định là cho chỉnh sửa

    public void setEditable(boolean editable) {
        this.editable = editable;
        notifyDataSetChanged(); // update lại UI toàn bộ khi thay đổi chế độ
    }

    public ProductHandoverAdapter(List<Product> productList,
                                  Map<String, Integer> quantityMap,
                                  Runnable onQuantityChanged) {
        this.productList = productList;
        this.quantityMap = quantityMap;
        this.onQuantityChanged = onQuantityChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_handover, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // Gán tên, mã, giá sản phẩm
        holder.tvProductName.setText(product.getName());
        holder.tvProductCode.setText("Mã: " + product.getCode() + " - " + formatPrice(product.getPrice()) + " đ");

        // Gán ảnh từ drawable nếu có
        int imageResId = holder.itemView.getContext().getResources()
                .getIdentifier(product.getImageName(), "drawable", holder.itemView.getContext().getPackageName());

        if (imageResId != 0) {
            holder.ivImage.setImageResource(imageResId);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder); // fallback nếu ảnh không tồn tại
        }

        // Gán số lượng
        int quantity = quantityMap.getOrDefault(product.getId(), 0);
        holder.etQuantity.setText(String.valueOf(quantity));
        holder.btnIncrease.setEnabled(editable);
        holder.btnDecrease.setEnabled(editable);
        holder.etQuantity.setEnabled(editable);
        if (!editable) {
            holder.btnIncrease.setOnClickListener(null);
            holder.btnDecrease.setOnClickListener(null);
            holder.etQuantity.setOnFocusChangeListener(null);
        } else {
            holder.btnIncrease.setOnClickListener(v -> {
                int currentQty = quantityMap.getOrDefault(product.getId(), 0);
                int newQty = currentQty + 1;
                quantityMap.put(product.getId(), newQty);

                // ✅ Xóa focus để tránh giữ text cũ
                holder.etQuantity.clearFocus();
                holder.etQuantity.setText(String.valueOf(newQty));

                onQuantityChanged.run();
            });

            holder.btnDecrease.setOnClickListener(v -> {
                int currentQty = quantityMap.getOrDefault(product.getId(), 0);
                if (currentQty > 0) {
                    int newQty = currentQty - 1;
                    quantityMap.put(product.getId(), newQty);

                    holder.etQuantity.clearFocus();
                    holder.etQuantity.setText(String.valueOf(newQty));

                    onQuantityChanged.run();
                }
            });
            // Xử lý nhập tay
            holder.etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        int typed = Integer.parseInt(holder.etQuantity.getText().toString());
                        quantityMap.put(product.getId(), Math.max(0, typed));
                        onQuantityChanged.run();
                    } catch (NumberFormatException ignored) {}
                }
            });
        }
        // Xử lý nút tăng / giảm

    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%,.0f", price);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnIncrease, btnDecrease;
        TextView tvProductName, tvProductCode;
        EditText etQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            etQuantity = itemView.findViewById(R.id.et_quantity);
        }
    }
}


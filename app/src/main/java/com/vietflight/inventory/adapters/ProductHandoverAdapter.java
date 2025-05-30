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

import com.vietflight.inventory.R;
import com.vietflight.inventory.models.Product;

import java.util.List;

public class ProductHandoverAdapter extends RecyclerView.Adapter<ProductHandoverAdapter.ViewHolder> {

    private List<Product> products;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public ProductHandoverAdapter(List<Product> products, OnQuantityChangeListener listener) {
        this.products = products;
        this.listener = listener;
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
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductCode;
        EditText etQuantity;
        ImageView btnDecrease, btnIncrease, ivProductImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            etQuantity = itemView.findViewById(R.id.et_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }

        void bind(Product product) {
            tvProductName.setText(product.getName());

            // Set hình ảnh dựa trên mã sản phẩm
            setProductImage(product.getCode());

            // Format giá
            String priceText = "Mã: " + product.getCode() + " - " +
                    String.format("%,.0f đ", product.getPrice());
            tvProductCode.setText(priceText);

            etQuantity.setText(String.valueOf(product.getQuantity()));

            // Remove previous listeners to prevent conflicts
            etQuantity.setTag(null);

            // Xử lý thay đổi số lượng
            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (etQuantity.getTag() != null) return; // Prevent recursive calls

                    try {
                        int quantity = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                        product.setQuantity(Math.max(0, quantity));
                        if (listener != null) listener.onQuantityChanged();
                    } catch (NumberFormatException e) {
                        product.setQuantity(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Nút giảm
            btnDecrease.setOnClickListener(v -> {
                int currentQuantity = product.getQuantity();
                if (currentQuantity > 0) {
                    product.setQuantity(currentQuantity - 1);
                    etQuantity.setTag("updating");
                    etQuantity.setText(String.valueOf(product.getQuantity()));
                    etQuantity.setTag(null);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            // Nút tăng
            btnIncrease.setOnClickListener(v -> {
                int currentQuantity = product.getQuantity();
                product.setQuantity(currentQuantity + 1);
                etQuantity.setTag("updating");
                etQuantity.setText(String.valueOf(product.getQuantity()));
                etQuantity.setTag(null);
                if (listener != null) listener.onQuantityChanged();
            });
        }

        private void setProductImage(String productCode) {
            int imageResource = getImageResourceByCode(productCode);
            ivProductImage.setImageResource(imageResource);
        }

        private int getImageResourceByCode(String productCode) {
            switch (productCode) {
                // Hot Meals
                case "HM001": return R.drawable.product_chicken_teriyaki;
                case "HM002": return R.drawable.product_beef_rice;
                case "HM003": return R.drawable.product_pasta;
                case "HM004": return R.drawable.product_fried_rice;

                // F&B
                case "FB001": return R.drawable.product_water;
                case "FB002": return R.drawable.product_coca;
                case "FB003": return R.drawable.product_coffee;
                case "FB004": return R.drawable.product_tea;
                case "FB005": return R.drawable.product_cookies;

                // Souvenirs
                case "SV001": return R.drawable.product_keychain;
                case "SV002": return R.drawable.product_tshirt;
                case "SV003": return R.drawable.product_cap;

                // Business
                case "BS001": return R.drawable.product_business_meal;
                case "BS002": return R.drawable.product_wine;

                // Other
                case "OT001": return R.drawable.product_wet_tissue;
                case "OT002": return R.drawable.product_paper_bag;

                // Default fallback
                default: return R.drawable.product_default;
            }
        }
    }
}
package com.vietflight.inventory.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vietflight.inventory.R;
import com.vietflight.inventory.models.HandoverItem;
import com.vietflight.inventory.models.Product;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProductReceiveAdapter extends RecyclerView.Adapter<ProductReceiveAdapter.ViewHolder> {
    private Context context;
    private List<Product> fullProductList;
    private List<Product> displayProductList;
    private String flightType = "";
    private boolean showRestock = false;
    public ProductReceiveAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.fullProductList = new ArrayList<>(productList);
        this.displayProductList = new ArrayList<>(productList);
    }

    public void setFlightType(String flightType) {
        this.flightType = flightType != null ? flightType : "";
        notifyDataSetChanged();
    }

    public void setShowRestock(boolean show) {
        this.showRestock = show;
        notifyDataSetChanged();
    }

    public void updateList(List<Product> filteredList) {
        displayProductList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_handover_readonly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = displayProductList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvCode.setText("Mã: " + product.getCode() + " - Giá: " + product.getPrice());
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        int defaultQty = getDefaultQuantity(product.getCategory());
        int need = Math.max(0, defaultQty - product.getQuantity());
        if (showRestock && need > 0) {
            holder.tvRestock.setVisibility(View.VISIBLE);
            holder.tvRestock.setText("+" + need);
        } else {
            holder.tvRestock.setVisibility(View.GONE);
        }


        // Load image from resource name
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.product_default)
                    .into(holder.ivImage);
        } else {
            String imageName = product.getImageName();
            if (imageName != null && !imageName.isEmpty()) {
                int imageResId = context.getResources().getIdentifier(
                        imageName, "drawable", context.getPackageName());
                if (imageResId != 0) {
                    holder.ivImage.setImageResource(imageResId);
                } else {
                    holder.ivImage.setImageResource(R.drawable.product_default);
                }
            } else {
                holder.ivImage.setImageResource(R.drawable.product_default);
            }
        }
    }

    @Override
    public int getItemCount() {
        return displayProductList.size();
    }

    private int getDefaultQuantity(String category) {
        if (flightType == null) return 0;
        switch (flightType) {
            case "COM 2025":
                if ("HOT_MEAL".equalsIgnoreCase(category)) return 30;
                if ("FNB".equalsIgnoreCase(category)) return 50;
                if ("MER".equalsIgnoreCase(category)) return 5;
                if ("SBOSS".equalsIgnoreCase(category)) return 2;
                break;
            case "COM INDIA":
                if ("HOT_MEAL".equalsIgnoreCase(category)) return 25;
                if ("FNB".equalsIgnoreCase(category)) return 35;
                if ("MER".equalsIgnoreCase(category)) return 3;
                break;
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCode, tvQuantity, tvRestock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image_readonly);
            tvName = itemView.findViewById(R.id.tv_product_name_readonly);
            tvCode = itemView.findViewById(R.id.tv_product_code_readonly);
            tvQuantity = itemView.findViewById(R.id.tv_quantity_readonly);
            tvRestock = itemView.findViewById(R.id.tv_restock_needed);
        }
    }
}

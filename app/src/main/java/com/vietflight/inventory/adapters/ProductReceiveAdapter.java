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

import java.util.ArrayList;
import java.util.List;

public class ProductReceiveAdapter extends RecyclerView.Adapter<ProductReceiveAdapter.ViewHolder> {
    private Context context;
    private List<Product> fullProductList;
    private List<Product> displayProductList;

    public ProductReceiveAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.fullProductList = new ArrayList<>(productList);
        this.displayProductList = new ArrayList<>(productList);
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

        // Load image from resource name
        int imageResId = context.getResources().getIdentifier(
                product.getImageName(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            holder.ivImage.setImageResource(imageResId);
        } else {
            holder.ivImage.setImageResource(R.drawable.product_default);
        }
    }

    @Override
    public int getItemCount() {
        return displayProductList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCode, tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image_readonly);
            tvName = itemView.findViewById(R.id.tv_product_name_readonly);
            tvCode = itemView.findViewById(R.id.tv_product_code_readonly);
            tvQuantity = itemView.findViewById(R.id.tv_quantity_readonly);
        }
    }
}

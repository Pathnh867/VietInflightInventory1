package com.vietflight.inventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vietflight.inventory.R;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEdit(Map<String, Object> product, int position);
        void onDelete(Map<String, Object> product, int position);
    }

    private final Context context;
    private List<Map<String, Object>> productList;
    private final OnProductActionListener listener;

    public ProductManageAdapter(Context context, List<Map<String, Object>> list, OnProductActionListener listener) {
        this.context = context;
        this.productList = list;
        this.listener = listener;
    }

    public void updateList(List<Map<String, Object>> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> product = productList.get(position);
        Object imageUrl = product.get("imageUrl");
        if (imageUrl != null) {
            Glide.with(context).load(imageUrl).placeholder(R.drawable.product_default).into(holder.ivImage);
        } else if (product.get("imageName") != null) {
            int res = context.getResources().getIdentifier(product.get("imageName")+"", "drawable", context.getPackageName());
            if (res != 0) holder.ivImage.setImageResource(res);
            else holder.ivImage.setImageResource(R.drawable.product_default);
        } else {
            holder.ivImage.setImageResource(R.drawable.product_default);
        }
        holder.tvName.setText((String) product.get("name"));
        holder.tvCategory.setText("Loại: " + product.get("category"));
        holder.tvPrice.setText("Giá: " + product.get("price") + " "+(product.get("unit")!=null?product.get("unit"):""));
        boolean isActive = product.get("is_active") == null || Boolean.TRUE.equals(product.get("is_active"));
        holder.tvStatus.setText(isActive ? "Đang hoạt động" : "Đã khóa");
        holder.tvStatus.setTextColor(isActive ? 0xFF388E3C : 0xFFE53935);
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(product, position);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(product, position);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCategory, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

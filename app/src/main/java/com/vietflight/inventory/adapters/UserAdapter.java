package com.vietflight.inventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vietflight.inventory.R;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onEdit(Map<String, Object> user, int position);
        void onDelete(Map<String, Object> user, int position);
    }

    private final Context context;
    private List<Map<String, Object>> userList;
    private final OnUserActionListener listener;

    public UserAdapter(Context context, List<Map<String, Object>> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void updateList(List<Map<String, Object>> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        Map<String, Object> user = userList.get(position);

        holder.tvFullname.setText(user.get("fullname") + "");
        holder.tvUsername.setText("Tên đăng nhập: " + user.get("username"));
        holder.tvRole.setText(
                user.get("roleLabel") != null ? user.get("roleLabel").toString()
                        : user.get("role").toString()
        );

        boolean isActive = user.get("is_active") == null || Boolean.TRUE.equals(user.get("is_active"));
        holder.tvStatus.setText(isActive ? "Đang hoạt động" : "Đã khóa");
        holder.tvStatus.setTextColor(isActive ? 0xFF388E3C : 0xFFE53935); // Xanh/Đỏ

        // Đổi icon và contentDescription cho btnDelete
        if (isActive) {
            holder.btnDelete.setImageResource(R.drawable.ic_delete); // icon khóa, hoặc ic_delete vẫn được
            holder.btnDelete.setColorFilter(0xFFE53935); // Màu đỏ
            holder.btnDelete.setContentDescription("Vô hiệu hóa");
        } else {
            holder.btnDelete.setImageResource(R.drawable.ic_check_circle); // icon kích hoạt lại
            holder.btnDelete.setColorFilter(0xFF388E3C); // Màu xanh
            holder.btnDelete.setContentDescription("Kích hoạt lại");
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(user, position);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user, position);
        });
    }


    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void setUserRoleLabel(Map<String, Object> user, String label) {
        user.put("roleLabel", label);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullname, tvUsername, tvRole, tvStatus;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullname = itemView.findViewById(R.id.tv_fullname);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

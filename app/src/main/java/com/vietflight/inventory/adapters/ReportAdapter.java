package com.vietflight.inventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vietflight.inventory.R;
import com.vietflight.inventory.models.Handover;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<Handover> handoverList;
    private Context context;
    private OnUnlockClickListener unlockClickListener;
    private String currentUserId;
    private String currentEmployeeId;

    public interface OnUnlockClickListener {
        void onUnlock(Handover handover, int position);
    }

    public ReportAdapter(Context context, List<Handover> handoverList, String currentUserId,
                         String currentEmployeeId, OnUnlockClickListener unlockClickListener) {
        this.context = context;
        this.handoverList = handoverList;
        this.currentUserId = currentUserId;
        this.currentEmployeeId = currentEmployeeId;
        this.unlockClickListener = unlockClickListener;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_handover_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        Handover handover = handoverList.get(position);

        // Header: FlightCode + Date
        String flightCode = safe(handover.getFlightNumber());
        String flightDate = safe(handover.getFlightDate());
        holder.tvFlightCode.setText(flightCode + " | " + flightDate);

        // Trạng thái
        boolean isReceived = "received".equalsIgnoreCase(handover.getStatus());
        if (isReceived) {
            holder.tvStatus.setText("Đã nhận");
            holder.tvStatus.setTextColor(0xFF388E3C); // Xanh lá
        } else {
            holder.tvStatus.setText("Chưa nhận");
            holder.tvStatus.setTextColor(0xFFEA0029); // Đỏ
        }

        String receiverId = handover.getReceivedByUserId();
        String receiverEmp = null;
        if ((receiverId == null || receiverId.isEmpty()) && handover.getReceivedByMap() != null) {
            Map<String, Object> rb = handover.getReceivedByMap();
            Object idObj = rb.get("user_id");
            if (idObj instanceof String) receiverId = (String) idObj;
            Object empObj = rb.get("employee_id");
            if (empObj instanceof String) receiverEmp = (String) empObj;
        }
        boolean canUnlock = handover.isLocked() && isReceived && (
                (currentUserId != null && currentUserId.equals(receiverId)) ||
                        (currentEmployeeId != null && currentEmployeeId.equals(receiverEmp))
        );
        holder.btnUnlock.setVisibility(canUnlock ? View.VISIBLE : View.GONE);

        // Aircraft, loại chuyến bay
        holder.tvAircraft.setText("Tàu bay: " + safe(handover.getAircraftId()));
        holder.tvFlightType.setText("Loại chuyến bay: " + safe(handover.getHandoverType()));

        // Người tạo
        String createdBy = safe(handover.getCreatedByUserName());
        if (createdBy.equals("--")) {
            // Nếu model không có sẵn, lấy từ Firestore Map
            Map<String, Object> createdByMap = handover.getCreatedByMap();
            if (createdByMap != null) {
                createdBy = safe((String) createdByMap.get("fullname"));
            }
        }
        holder.tvCreatedBy.setText("Tạo bởi: " + createdBy);

        // Người nhận
        String receivedBy = safe(handover.getReceivedByUserName());
        if (receivedBy.equals("--")) {
            Map<String, Object> receivedByMap = handover.getReceivedByMap();
            if (receivedByMap != null) {
                receivedBy = safe((String) receivedByMap.get("fullname"));
            }
        }
        holder.tvReceivedBy.setText("Nhận bởi: " + receivedBy);

        // Button mở khóa
        holder.btnUnlock.setOnClickListener(v -> {
            if (unlockClickListener != null) unlockClickListener.onUnlock(handover, position);
        });
    }

    @Override
    public int getItemCount() {
        return handoverList.size();
    }

    public void updateList(List<Handover> newList) {
        this.handoverList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightCode, tvStatus, tvAircraft, tvFlightType, tvCreatedBy, tvReceivedBy;
        Button btnUnlock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlightCode = itemView.findViewById(R.id.tv_flight_code);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAircraft = itemView.findViewById(R.id.tv_aircraft);
            tvFlightType = itemView.findViewById(R.id.tv_flight_type);
            tvCreatedBy = itemView.findViewById(R.id.tv_created_by);
            tvReceivedBy = itemView.findViewById(R.id.tv_received_by);
            btnUnlock = itemView.findViewById(R.id.btn_unlock);
        }
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? "--" : value;
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "--";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}

package com.vietflight.inventory.models;

import java.util.List;
import java.util.Map;

public class Handover {
    private String id;
    private String flightId;
    private String flightNumber;
    private String aircraftId;
    private String flightDate;
    private String createdByUserId;
    private String createdByUserName;
    private String receivedByUserId;
    private String receivedByUserName;
    private String handoverType; // "normal", "topup"
    private String status; // "pending", "received", "returned", "locked"
    private long createdAt;
    private long receivedAt;
    private long returnedAt;
    private boolean isLocked;
    private String notes;
    private List<HandoverItem> items;

    private Map<String, Object> createdByMap;
    private Map<String, Object> receivedByMap;

    public Handover() {}

    public Handover(String flightId, String flightNumber, String aircraftId, String flightDate,
                    String createdByUserId, String createdByUserName, String handoverType) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.aircraftId = aircraftId;
        this.flightDate = flightDate;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.handoverType = handoverType;
        this.status = "pending";
        this.isLocked = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getFlightDate() { return flightDate; }
    public void setFlightDate(String flightDate) { this.flightDate = flightDate; }

    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByUserName() { return createdByUserName; }
    public void setCreatedByUserName(String createdByUserName) { this.createdByUserName = createdByUserName; }

    public String getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(String receivedByUserId) { this.receivedByUserId = receivedByUserId; }

    public String getReceivedByUserName() { return receivedByUserName; }
    public void setReceivedByUserName(String receivedByUserName) { this.receivedByUserName = receivedByUserName; }

    public String getHandoverType() { return handoverType; }
    public void setHandoverType(String handoverType) { this.handoverType = handoverType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getReceivedAt() { return receivedAt; }
    public void setReceivedAt(long receivedAt) { this.receivedAt = receivedAt; }

    public long getReturnedAt() { return returnedAt; }
    public void setReturnedAt(long returnedAt) { this.returnedAt = returnedAt; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<HandoverItem> getItems() { return items; }
    public void setItems(List<HandoverItem> items) { this.items = items; }

    public Map<String, Object> getCreatedByMap() {
        return createdByMap;
    }

    public void setCreatedByMap(Map<String, Object> createdByMap) {
        this.createdByMap = createdByMap;
    }

    public Map<String, Object> getReceivedByMap() {
        return receivedByMap;
    }

    public void setReceivedByMap(Map<String, Object> receivedByMap) {
        this.receivedByMap = receivedByMap;
    }
}
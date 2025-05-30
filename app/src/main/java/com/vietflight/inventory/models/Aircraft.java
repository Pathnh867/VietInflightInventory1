package com.vietflight.inventory.models;

public class Aircraft {
    private String id;
    private String aircraftCode;
    private String model;
    private String manufacturer;
    private int capacity;
    private String status; // "active", "maintenance", "retired"
    private long createdAt;
    private long updatedAt;

    public Aircraft() {}

    public Aircraft(String aircraftCode, String model, String manufacturer, int capacity) {
        this.aircraftCode = aircraftCode;
        this.model = model;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAircraftCode() { return aircraftCode; }
    public void setAircraftCode(String aircraftCode) { this.aircraftCode = aircraftCode; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
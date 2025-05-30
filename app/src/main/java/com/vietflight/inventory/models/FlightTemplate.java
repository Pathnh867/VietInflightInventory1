package com.vietflight.inventory.models;

import java.util.Map;

public class FlightTemplate {
    private String id;
    private String flightType;      // "domestic", "international"
    private String aircraftModel;   // "A321", "A320"
    private Map<String, Integer> defaultQuantities; // productId -> quantity
    private long createdAt;
    private long updatedAt;

    public FlightTemplate() {}

    public FlightTemplate(String flightType, String aircraftModel) {
        this.flightType = flightType;
        this.aircraftModel = aircraftModel;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFlightType() { return flightType; }
    public void setFlightType(String flightType) { this.flightType = flightType; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public Map<String, Integer> getDefaultQuantities() { return defaultQuantities; }
    public void setDefaultQuantities(Map<String, Integer> defaultQuantities) {
        this.defaultQuantities = defaultQuantities;
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
package com.vietflight.inventory.models;

import java.util.Map;

public class Flight {
    private String id;
    private String flightNumber;
    private String aircraftId;
    private String aircraftModel;
    private String flightDate;
    private String flightType; // "domestic", "international"
    private String route;
    private String departureTime;
    private String arrivalTime;
    private long createdAt;

    private Map<String, Integer> defaultQuantities;

    public Flight() {}

    // Constructor và Getters/Setters tương tự User
    public Flight(String flightNumber, String aircraftId, String flightDate, String flightType) {
        this.flightNumber = flightNumber;
        this.aircraftId = aircraftId;
        this.flightDate = flightDate;
        this.flightType = flightType;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public String getFlightDate() { return flightDate; }
    public void setFlightDate(String flightDate) { this.flightDate = flightDate; }

    public String getFlightType() { return flightType; }
    public void setFlightType(String flightType) { this.flightType = flightType; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Integer> getDefaultQuantities() { return defaultQuantities; }
    public void setDefaultQuantities(Map<String, Integer> defaultQuantities) {
        this.defaultQuantities = defaultQuantities;
    }
}
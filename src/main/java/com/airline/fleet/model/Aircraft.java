package com.airline.fleet.model;

import java.sql.Timestamp;

/**
 * Model representing an Aircraft in the fleet.
 */
public class Aircraft {
    private int aircraftId;
    private String tailNumber;
    private String model;
    private String manufacturer;
    private FleetCategory category;
    private double maxPayloadKg;
    private int seatCapacity;
    private String engineType;
    private String status;
    private double totalFlightHours;
    private double baselineSfc;
    private Timestamp createdAt;

    public Aircraft() {
        this.status = "ACTIVE";
        this.category = FleetCategory.SHORT_HAUL_REGIONAL;
    }

    public Aircraft(int aircraftId, String tailNumber, String model, String manufacturer,
                    FleetCategory category, double maxPayloadKg, int seatCapacity,
                    String engineType, String status, double totalFlightHours, double baselineSfc) {
        this.aircraftId = aircraftId;
        this.tailNumber = tailNumber;
        this.model = model;
        this.manufacturer = manufacturer;
        this.category = category;
        this.maxPayloadKg = maxPayloadKg;
        this.seatCapacity = seatCapacity;
        this.engineType = engineType;
        this.status = status;
        this.totalFlightHours = totalFlightHours;
        this.baselineSfc = baselineSfc;
    }

    // Getters and Setters
    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public String getTailNumber() { return tailNumber; }
    public void setTailNumber(String tailNumber) { this.tailNumber = tailNumber; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public FleetCategory getCategory() { return category; }
    public void setCategory(FleetCategory category) { this.category = category; }

    public double getMaxPayloadKg() { return maxPayloadKg; }
    public void setMaxPayloadKg(double maxPayloadKg) { this.maxPayloadKg = maxPayloadKg; }

    public int getSeatCapacity() { return seatCapacity; }
    public void setSeatCapacity(int seatCapacity) { this.seatCapacity = seatCapacity; }

    public String getEngineType() { return engineType; }
    public void setEngineType(String engineType) { this.engineType = engineType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalFlightHours() { return totalFlightHours; }
    public void setTotalFlightHours(double totalFlightHours) { this.totalFlightHours = totalFlightHours; }

    public double getBaselineSfc() { return baselineSfc; }
    public void setBaselineSfc(double baselineSfc) { this.baselineSfc = baselineSfc; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return tailNumber + " (" + model + " - " + category.getDisplayName() + ")";
    }
}

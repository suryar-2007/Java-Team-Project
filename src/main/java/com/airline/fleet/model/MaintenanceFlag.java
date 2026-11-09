package com.airline.fleet.model;

import java.sql.Timestamp;

/**
 * Model representing an automated maintenance alert / inspection flag.
 */
public class MaintenanceFlag {
    private int flagId;
    private int aircraftId;
    private String aircraftTailNumber;
    private Integer flightId;
    private String flightNumber;
    private String severity; // CRITICAL, WARNING, INFO
    private String flagType;
    private String description;
    private Double vibrationReadingMils;
    private String status;   // OPEN, ACKNOWLEDGED, RESOLVED
    private String resolvedBy;
    private String resolutionNotes;
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    public MaintenanceFlag() {
        this.severity = "WARNING";
        this.status = "OPEN";
    }

    public MaintenanceFlag(int aircraftId, Integer flightId, String severity, String flagType,
                           String description, Double vibrationReadingMils) {
        this.aircraftId = aircraftId;
        this.flightId = flightId;
        this.severity = severity;
        this.flagType = flagType;
        this.description = description;
        this.vibrationReadingMils = vibrationReadingMils;
        this.status = "OPEN";
    }

    // Getters and Setters
    public int getFlagId() { return flagId; }
    public void setFlagId(int flagId) { this.flagId = flagId; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftTailNumber() { return aircraftTailNumber; }
    public void setAircraftTailNumber(String aircraftTailNumber) { this.aircraftTailNumber = aircraftTailNumber; }

    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getFlagType() { return flagType; }
    public void setFlagType(String flagType) { this.flagType = flagType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getVibrationReadingMils() { return vibrationReadingMils; }
    public void setVibrationReadingMils(Double vibrationReadingMils) { this.vibrationReadingMils = vibrationReadingMils; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}

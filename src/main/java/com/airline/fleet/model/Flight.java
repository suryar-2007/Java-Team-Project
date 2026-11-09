package com.airline.fleet.model;

import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * Model representing a commercial flight operation.
 * Enforces regex validation pattern: [A-Z]{2,3}\\d{3,4}
 */
public class Flight {
    public static final String FLIGHT_NUMBER_REGEX = "^[A-Z]{2,3}\\d{3,4}$";
    private static final Pattern PATTERN = Pattern.compile(FLIGHT_NUMBER_REGEX);

    private int flightId;
    private String flightNumber;
    private int aircraftId;
    private String aircraftTailNumber; // For display
    private String origin;
    private String destination;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    private double distanceKm;
    private double payloadWeightKg;
    private int passengerCount;
    private FleetCategory category;
    private String status;
    private double totalFuelBurnedKg;
    private double calculatedSfc;
    private double carbonIntensity;
    private FleetGrade fleetGrade;
    private Timestamp createdAt;

    public Flight() {
        this.status = "COMPLETED";
        this.category = FleetCategory.SHORT_HAUL_REGIONAL;
        this.fleetGrade = FleetGrade.B;
    }

    public static boolean isValidFlightNumber(String flightNumber) {
        if (flightNumber == null) return false;
        return PATTERN.matcher(flightNumber.trim().toUpperCase()).matches();
    }

    // Getters and Setters
    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) {
        if (!isValidFlightNumber(flightNumber)) {
            throw new IllegalArgumentException("Invalid Flight Number format: '" + flightNumber + 
                "'. Must match regex: [A-Z]{2,3}\\d{3,4} (e.g. AA101, BA204, DEL3402)");
        }
        this.flightNumber = flightNumber.trim().toUpperCase();
    }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftTailNumber() { return aircraftTailNumber; }
    public void setAircraftTailNumber(String aircraftTailNumber) { this.aircraftTailNumber = aircraftTailNumber; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin != null ? origin.trim().toUpperCase() : ""; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination != null ? destination.trim().toUpperCase() : ""; }

    public Timestamp getDepartureTime() { return departureTime; }
    public void setDepartureTime(Timestamp departureTime) { this.departureTime = departureTime; }

    public Timestamp getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(Timestamp arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getPayloadWeightKg() { return payloadWeightKg; }
    public void setPayloadWeightKg(double payloadWeightKg) { this.payloadWeightKg = payloadWeightKg; }

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    public FleetCategory getCategory() { return category; }
    public void setCategory(FleetCategory category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalFuelBurnedKg() { return totalFuelBurnedKg; }
    public void setTotalFuelBurnedKg(double totalFuelBurnedKg) { this.totalFuelBurnedKg = totalFuelBurnedKg; }

    public double getCalculatedSfc() { return calculatedSfc; }
    public void setCalculatedSfc(double calculatedSfc) { this.calculatedSfc = calculatedSfc; }

    public double getCarbonIntensity() { return carbonIntensity; }
    public void setCarbonIntensity(double carbonIntensity) { this.carbonIntensity = carbonIntensity; }

    public FleetGrade getFleetGrade() { return fleetGrade; }
    public void setFleetGrade(FleetGrade fleetGrade) { this.fleetGrade = fleetGrade; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return flightNumber + " (" + origin + " -> " + destination + ")";
    }
}

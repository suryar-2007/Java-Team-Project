package com.airline.fleet.model;

import java.sql.Timestamp;

/**
 * Model representing three-phase flight telemetry sensor readings.
 */
public class TelemetryData {
    private int telemetryId;
    private int flightId;
    private FlightPhase phase;
    private double altitudeFeet;
    private double speedKnots;
    private double engine1VibrationMils;
    private double engine2VibrationMils;
    private double phaseFuelBurnedKg;
    private double exhaustGasTempC;
    private double oilPressurePsi;
    private Timestamp timestamp;

    public TelemetryData() {
        this.phase = FlightPhase.CRUISE;
        this.exhaustGasTempC = 650.0;
        this.oilPressurePsi = 60.0;
    }

    public TelemetryData(int flightId, FlightPhase phase, double altitudeFeet, double speedKnots,
                         double engine1VibrationMils, double engine2VibrationMils,
                         double phaseFuelBurnedKg, double exhaustGasTempC, double oilPressurePsi) {
        this.flightId = flightId;
        this.phase = phase;
        this.altitudeFeet = altitudeFeet;
        this.speedKnots = speedKnots;
        this.engine1VibrationMils = engine1VibrationMils;
        this.engine2VibrationMils = engine2VibrationMils;
        this.phaseFuelBurnedKg = phaseFuelBurnedKg;
        this.exhaustGasTempC = exhaustGasTempC;
        this.oilPressurePsi = oilPressurePsi;
    }

    public double getMaxVibration() {
        return Math.max(engine1VibrationMils, engine2VibrationMils);
    }

    // Getters and Setters
    public int getTelemetryId() { return telemetryId; }
    public void setTelemetryId(int telemetryId) { this.telemetryId = telemetryId; }

    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public FlightPhase getPhase() { return phase; }
    public void setPhase(FlightPhase phase) { this.phase = phase; }

    public double getAltitudeFeet() { return altitudeFeet; }
    public void setAltitudeFeet(double altitudeFeet) { this.altitudeFeet = altitudeFeet; }

    public double getSpeedKnots() { return speedKnots; }
    public void setSpeedKnots(double speedKnots) { this.speedKnots = speedKnots; }

    public double getEngine1VibrationMils() { return engine1VibrationMils; }
    public void setEngine1VibrationMils(double engine1VibrationMils) { this.engine1VibrationMils = engine1VibrationMils; }

    public double getEngine2VibrationMils() { return engine2VibrationMils; }
    public void setEngine2VibrationMils(double engine2VibrationMils) { this.engine2VibrationMils = engine2VibrationMils; }

    public double getPhaseFuelBurnedKg() { return phaseFuelBurnedKg; }
    public void setPhaseFuelBurnedKg(double phaseFuelBurnedKg) { this.phaseFuelBurnedKg = phaseFuelBurnedKg; }

    public double getExhaustGasTempC() { return exhaustGasTempC; }
    public void setExhaustGasTempC(double exhaustGasTempC) { this.exhaustGasTempC = exhaustGasTempC; }

    public double getOilPressurePsi() { return oilPressurePsi; }
    public void setOilPressurePsi(double oilPressurePsi) { this.oilPressurePsi = oilPressurePsi; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}

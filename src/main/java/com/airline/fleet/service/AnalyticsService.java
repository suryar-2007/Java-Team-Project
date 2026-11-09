package com.airline.fleet.service;

import com.airline.fleet.model.FleetCategory;
import com.airline.fleet.model.FleetGrade;

/**
 * Aviation Engineering Analytics Service
 * Implements strict aviation efficiency, carbon emissions, and vibration calculations.
 */
public class AnalyticsService {

    // Default ICAO Standard Jet-A Emission Factor (kg CO2 per kg fuel burned)
    public static final double DEFAULT_CO2_EMISSION_FACTOR = 3.16;

    // Vibration Limits in mils (milli-inches peak-to-peak)
    public static final double DEFAULT_VIBRATION_CAUTION = 2.5;
    public static final double DEFAULT_VIBRATION_CRITICAL = 4.0;

    // Default SFC Grading Thresholds (kg fuel / (kg payload * km))
    public static final double DEFAULT_SFC_GRADE_A = 0.000038;
    public static final double DEFAULT_SFC_GRADE_B = 0.000048;
    public static final double DEFAULT_OUTLIER_PCT_DEV = 25.0;

    private double co2Factor = DEFAULT_CO2_EMISSION_FACTOR;
    private double vibrationCaution = DEFAULT_VIBRATION_CAUTION;
    private double vibrationCritical = DEFAULT_VIBRATION_CRITICAL;
    private double sfcGradeA = DEFAULT_SFC_GRADE_A;
    private double sfcGradeB = DEFAULT_SFC_GRADE_B;
    private double outlierPctDev = DEFAULT_OUTLIER_PCT_DEV;

    public AnalyticsService() {}

    /**
     * Specific Fuel Consumption (SFC) calculation:
     * Formula: SFC = TotalFuelBurned / (PayloadWeight * Distance)
     *
     * @param totalFuelBurnedKg Total fuel consumed across all 3 phases (in kg)
     * @param payloadWeightKg   Actual payload weight carried (passengers + cargo in kg)
     * @param distanceKm        Stage length / flight distance (in kilometers)
     * @return Calculated SFC in kg_fuel / (kg_payload * km)
     */
    public double calculateSFC(double totalFuelBurnedKg, double payloadWeightKg, double distanceKm) {
        if (payloadWeightKg <= 0 || distanceKm <= 0) {
            throw new IllegalArgumentException("Payload weight and distance must be strictly positive values.");
        }
        if (totalFuelBurnedKg < 0) {
            throw new IllegalArgumentException("Fuel burned cannot be negative.");
        }
        return totalFuelBurnedKg / (payloadWeightKg * distanceKm);
    }

    /**
     * Fleet Carbon Intensity calculation:
     * Formula: Fleet Carbon Intensity = (FuelBurned * 3.16) / ASK
     * where ASK = Available Seat Kilometers = (Seats * Distance)
     * For pure cargo aircraft (seats == 0), ATK (Available Tonne Kilometers) is used:
     * ASK_equivalent = (PayloadCapacityTonnes * 10 * Distance) where 1 passenger ~ 100kg (0.1 tonne)
     *
     * @param fuelBurnedKg Total fuel consumed (kg)
     * @param seats        Available seat capacity of aircraft (or 0 for freighters)
     * @param payloadKg    Payload capacity in kg (used for cargo fallback)
     * @param distanceKm   Flight distance in kilometers
     * @return Carbon Intensity in kg CO2 per passenger-km (or per 100kg-km for cargo)
     */
    public double calculateCarbonIntensity(double fuelBurnedKg, int seats, double payloadKg, double distanceKm) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero.");
        }
        double totalCo2Kg = fuelBurnedKg * co2Factor;

        double capacityMetric;
        if (seats > 0) {
            capacityMetric = (double) seats * distanceKm; // ASK
        } else {
            // Cargo: Convert payload capacity into standard aviation equivalent seat-km
            // 1 standard passenger allowance ~ 100 kg = 0.1 tonne
            capacityMetric = (payloadKg / 100.0) * distanceKm;
        }

        if (capacityMetric <= 0) {
            return 0.0;
        }
        return totalCo2Kg / capacityMetric;
    }

    /**
     * Fleet Grading: A, B, C and Outlier
     * Evaluates performance based on calculated SFC versus aircraft category baseline.
     *
     * @param calculatedSfc SFC calculated for the flight
     * @param baselineSfc   Aircraft nominal baseline SFC
     * @return FleetGrade (A, B, C, or OUTLIER)
     */
    public FleetGrade evaluateFleetGrade(double calculatedSfc, double baselineSfc) {
        if (calculatedSfc <= 0) return FleetGrade.B;

        // If baseline is provided, check for Outlier deviation first
        if (baselineSfc > 0) {
            double deviationPct = ((calculatedSfc - baselineSfc) / baselineSfc) * 100.0;
            if (deviationPct >= outlierPctDev) {
                return FleetGrade.OUTLIER;
            }
        }

        if (calculatedSfc <= sfcGradeA) {
            return FleetGrade.A;
        } else if (calculatedSfc <= sfcGradeB) {
            return FleetGrade.B;
        } else {
            return FleetGrade.C;
        }
    }

    /**
     * Evaluates engine vibration level against strict safety limits:
     * Normal: < 2.5 mils
     * Caution: 2.5 - 4.0 mils
     * Critical: > 4.0 mils
     *
     * @param maxVibration Reading in mils
     * @return Status string: "NORMAL", "CAUTION", or "CRITICAL"
     */
    public String evaluateVibrationStatus(double maxVibration) {
        if (maxVibration >= vibrationCritical) {
            return "CRITICAL";
        } else if (maxVibration >= vibrationCaution) {
            return "CAUTION";
        } else {
            return "NORMAL";
        }
    }

    // Configurable getters and setters for scoring parameters
    public double getCo2Factor() { return co2Factor; }
    public void setCo2Factor(double co2Factor) { this.co2Factor = co2Factor; }

    public double getVibrationCaution() { return vibrationCaution; }
    public void setVibrationCaution(double vibrationCaution) { this.vibrationCaution = vibrationCaution; }

    public double getVibrationCritical() { return vibrationCritical; }
    public void setVibrationCritical(double vibrationCritical) { this.vibrationCritical = vibrationCritical; }

    public double getSfcGradeA() { return sfcGradeA; }
    public void setSfcGradeA(double sfcGradeA) { this.sfcGradeA = sfcGradeA; }

    public double getSfcGradeB() { return sfcGradeB; }
    public void setSfcGradeB(double sfcGradeB) { this.sfcGradeB = sfcGradeB; }

    public double getOutlierPctDev() { return outlierPctDev; }
    public void setOutlierPctDev(double outlierPctDev) { this.outlierPctDev = outlierPctDev; }
}

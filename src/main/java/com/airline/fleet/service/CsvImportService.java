package com.airline.fleet.service;

import com.airline.fleet.DBConnection;
import com.airline.fleet.FleetDAO;
import com.airline.fleet.TelemetryWorker;
import com.airline.fleet.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CsvImportService: Handles bulk import of flight operations and telemetry records.
 * Parses CSV lines, validates flight numbers using regex ([A-Z]{2,3}\\d{3,4}),
 * computes aviation efficiency metrics (SFC, Carbon Intensity, Fleet Grade),
 * and feeds data into the multithreaded TelemetryWorker.
 */
public class CsvImportService {

    private final FleetDAO fleetDAO;
    private final AnalyticsService analyticsService;
    private final TelemetryWorker telemetryWorker;

    public static class ImportResult {
        private int totalProcessed = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private final List<String> errorMessages = new ArrayList<>();

        public int getTotalProcessed() { return totalProcessed; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrorMessages() { return errorMessages; }
    }

    public CsvImportService(TelemetryWorker telemetryWorker) {
        this.fleetDAO = new FleetDAO();
        this.analyticsService = new AnalyticsService();
        this.telemetryWorker = telemetryWorker;
    }

    /**
     * Imports flight data from a CSV file.
     * Expected CSV Header:
     * flight_number,aircraft_tail,origin,destination,departure,arrival,distance_km,payload_kg,passengers,category,phase1_fuel,phase2_fuel,phase3_fuel,phase1_vib,phase2_vib,phase3_vib
     */
    public ImportResult importFlightsCsv(File csvFile) {
        ImportResult result = new ImportResult();
        if (!csvFile.exists()) {
            result.errorMessages.add("File not found: " + csvFile.getAbsolutePath());
            return result;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                // Skip empty lines or header row
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().startsWith("flight_number")) {
                    continue;
                }

                result.totalProcessed++;
                String[] parts = trimmed.split(",");
                if (parts.length < 10) {
                    result.failureCount++;
                    result.errorMessages.add(String.format("Line %d: Insufficient columns (%d/10 required)", lineNumber, parts.length));
                    continue;
                }

                try {
                    String flightNumber = parts[0].trim().toUpperCase();

                    // Regex validation: [A-Z]{2,3}\d{3,4}
                    if (!Flight.isValidFlightNumber(flightNumber)) {
                        result.failureCount++;
                        result.errorMessages.add(String.format("Line %d: Invalid flight number '%s'. Must match [A-Z]{2,3}\\d{3,4}", lineNumber, flightNumber));
                        continue;
                    }

                    String tailNumber = parts[1].trim().toUpperCase();
                    Aircraft aircraft = fleetDAO.getAircraftByTailNumber(tailNumber);
                    if (aircraft == null) {
                        result.failureCount++;
                        result.errorMessages.add(String.format("Line %d: Aircraft tail '%s' not registered in database.", lineNumber, tailNumber));
                        continue;
                    }

                    String origin = parts[2].trim().toUpperCase();
                    String destination = parts[3].trim().toUpperCase();
                    Date depDate = dateFormat.parse(parts[4].trim());
                    Date arrDate = dateFormat.parse(parts[5].trim());
                    double distanceKm = Double.parseDouble(parts[6].trim());
                    double payloadKg = Double.parseDouble(parts[7].trim());
                    int passengers = Integer.parseInt(parts[8].trim());
                    FleetCategory category = FleetCategory.fromString(parts[9].trim());

                    // Phase fuel burn
                    double p1Fuel = parts.length > 10 ? Double.parseDouble(parts[10].trim()) : 0;
                    double p2Fuel = parts.length > 11 ? Double.parseDouble(parts[11].trim()) : 0;
                    double p3Fuel = parts.length > 12 ? Double.parseDouble(parts[12].trim()) : 0;
                    double totalFuel = p1Fuel + p2Fuel + p3Fuel;
                    if (totalFuel <= 0 && parts.length > 10) {
                        totalFuel = Double.parseDouble(parts[10].trim());
                    }

                    // Phase vibrations
                    double p1Vib = parts.length > 13 ? Double.parseDouble(parts[13].trim()) : 1.5;
                    double p2Vib = parts.length > 14 ? Double.parseDouble(parts[14].trim()) : 1.6;
                    double p3Vib = parts.length > 15 ? Double.parseDouble(parts[15].trim()) : 1.4;

                    // Aviation calculations
                    double sfc = analyticsService.calculateSFC(totalFuel, payloadKg, distanceKm);
                    double carbonIntensity = analyticsService.calculateCarbonIntensity(totalFuel, aircraft.getSeatCapacity(), aircraft.getMaxPayloadKg(), distanceKm);
                    FleetGrade grade = analyticsService.evaluateFleetGrade(sfc, aircraft.getBaselineSfc());

                    // Create Flight
                    Flight flight = new Flight();
                    flight.setFlightNumber(flightNumber);
                    flight.setAircraftId(aircraft.getAircraftId());
                    flight.setOrigin(origin);
                    flight.setDestination(destination);
                    flight.setDepartureTime(new Timestamp(depDate.getTime()));
                    flight.setArrivalTime(new Timestamp(arrDate.getTime()));
                    flight.setDistanceKm(distanceKm);
                    flight.setPayloadWeightKg(payloadKg);
                    flight.setPassengerCount(passengers);
                    flight.setCategory(category);
                    flight.setStatus("COMPLETED");
                    flight.setTotalFuelBurnedKg(totalFuel);
                    flight.setCalculatedSfc(sfc);
                    flight.setCarbonIntensity(carbonIntensity);
                    flight.setFleetGrade(grade);

                    // Insert flight
                    fleetDAO.insertFlight(flight);

                    // Multi-phase telemetry submission
                    TelemetryData t1 = new TelemetryData(flight.getFlightId(), FlightPhase.TAKEOFF_CLIMB, 12000, 310, p1Vib, p1Vib * 0.95, p1Fuel, 710, 65);
                    TelemetryData t2 = new TelemetryData(flight.getFlightId(), FlightPhase.CRUISE, 36000, 480, p2Vib, p2Vib * 0.98, p2Fuel, 640, 62);
                    TelemetryData t3 = new TelemetryData(flight.getFlightId(), FlightPhase.DESCENT_LANDING, 3000, 150, p3Vib, p3Vib * 0.94, p3Fuel, 580, 59);

                    if (telemetryWorker != null) {
                        telemetryWorker.processThreePhasesAsync(flight.getFlightId(), flight, t1, t2, t3, null);
                    } else {
                        fleetDAO.insertTelemetry(t1);
                        fleetDAO.insertTelemetry(t2);
                        fleetDAO.insertTelemetry(t3);
                    }

                    result.successCount++;

                } catch (Exception ex) {
                    result.failureCount++;
                    result.errorMessages.add(String.format("Line %d processing error: %s", lineNumber, ex.getMessage()));
                }
            }

        } catch (Exception e) {
            result.errorMessages.add("Error reading CSV file: " + e.getMessage());
        }

        return result;
    }
}

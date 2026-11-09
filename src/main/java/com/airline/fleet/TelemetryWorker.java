package com.airline.fleet;

import com.airline.fleet.model.Flight;
import com.airline.fleet.model.FlightPhase;
import com.airline.fleet.model.MaintenanceFlag;
import com.airline.fleet.model.TelemetryData;
import com.airline.fleet.service.AnalyticsService;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * TelemetryWorker: Multithreaded Engine Telemetry and Vibration Processing Service.
 * Leverages Java ExecutorService for high-throughput asynchronous processing of
 * sensor streams, automated anomaly detection, and instant maintenance alert creation.
 */
public class TelemetryWorker {

    private final ExecutorService executorService;
    private final FleetDAO fleetDAO;
    private final AnalyticsService analyticsService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public interface TelemetryListener {
        void onTelemetryProcessed(TelemetryData data, String statusMessage);
        void onAlertTriggered(MaintenanceFlag alert);
        void onError(String errorMessage);
    }

    public TelemetryWorker() {
        this(Executors.newFixedThreadPool(4));
    }

    public TelemetryWorker(ExecutorService executorService) {
        this.executorService = executorService;
        this.fleetDAO = new FleetDAO();
        this.analyticsService = new AnalyticsService();
        this.isRunning.set(true);
    }

    /**
     * Asynchronously processes a single telemetry packet in a dedicated worker thread.
     * Evaluates engine vibration and triggers automatic alerts if thresholds are breached.
     */
    public Future<?> processTelemetryAsync(TelemetryData telemetry, Flight flight, TelemetryListener listener) {
        return executorService.submit(() -> {
            try {
                // 1. Persist telemetry to database
                fleetDAO.insertTelemetry(telemetry);

                // 2. Evaluate engine vibration (Eng 1 vs Eng 2)
                double maxVibration = telemetry.getMaxVibration();
                double cautionLimit = analyticsService.getVibrationCaution();
                double criticalLimit = analyticsService.getVibrationCritical();

                String status = "NORMAL";

                if (maxVibration >= criticalLimit) {
                    status = "CRITICAL";
                    // Build and insert critical alert
                    String desc = String.format(
                        "CRITICAL ALERT: %s Engine %s vibration reached %.2f mils during %s (Exceeds %.1f mil limit). Immediate inspection required.",
                        flight != null ? flight.getFlightNumber() : "Flight #" + telemetry.getFlightId(),
                        (telemetry.getEngine1VibrationMils() >= criticalLimit) ? "1" : "2",
                        maxVibration,
                        telemetry.getPhase().getDisplayName(),
                        criticalLimit
                    );

                    int aircraftId = flight != null ? flight.getAircraftId() : 1;
                    MaintenanceFlag flag = new MaintenanceFlag(
                        aircraftId,
                        telemetry.getFlightId(),
                        "CRITICAL",
                        "VIBRATION_LIMIT_EXCEEDED",
                        desc,
                        maxVibration
                    );
                    fleetDAO.insertMaintenanceFlag(flag);
                    if (listener != null) listener.onAlertTriggered(flag);

                } else if (maxVibration >= cautionLimit) {
                    status = "CAUTION";
                    String desc = String.format(
                        "WARNING ADVISORY: %s Engine %s vibration elevated at %.2f mils during %s (Advisory range: %.1f - %.1f mils).",
                        flight != null ? flight.getFlightNumber() : "Flight #" + telemetry.getFlightId(),
                        (telemetry.getEngine1VibrationMils() >= cautionLimit) ? "1" : "2",
                        maxVibration,
                        telemetry.getPhase().getDisplayName(),
                        cautionLimit,
                        criticalLimit
                    );

                    int aircraftId = flight != null ? flight.getAircraftId() : 1;
                    MaintenanceFlag flag = new MaintenanceFlag(
                        aircraftId,
                        telemetry.getFlightId(),
                        "WARNING",
                        "VIBRATION_LIMIT_EXCEEDED",
                        desc,
                        maxVibration
                    );
                    fleetDAO.insertMaintenanceFlag(flag);
                    if (listener != null) listener.onAlertTriggered(flag);
                }

                if (listener != null) {
                    String msg = String.format(
                        "Processed %s [Alt: %.0fft, Eng1: %.2f mils, Eng2: %.2f mils, Status: %s]",
                        telemetry.getPhase().getDisplayName(),
                        telemetry.getAltitudeFeet(),
                        telemetry.getEngine1VibrationMils(),
                        telemetry.getEngine2VibrationMils(),
                        status
                    );
                    listener.onTelemetryProcessed(telemetry, msg);
                }

            } catch (SQLException e) {
                if (listener != null) listener.onError("Database error during telemetry processing: " + e.getMessage());
            } catch (Exception e) {
                if (listener != null) listener.onError("Unexpected processing error: " + e.getMessage());
            }
        });
    }

    /**
     * Submits a full three-phase telemetry batch for a newly registered flight.
     */
    public void processThreePhasesAsync(int flightId, Flight flight,
                                        TelemetryData phase1, TelemetryData phase2, TelemetryData phase3,
                                        TelemetryListener listener) {
        executorService.submit(() -> {
            try {
                phase1.setFlightId(flightId);
                phase2.setFlightId(flightId);
                phase3.setFlightId(flightId);

                processTelemetryAsync(phase1, flight, listener).get();
                Thread.sleep(80); // Micro delay simulating phase transition
                processTelemetryAsync(phase2, flight, listener).get();
                Thread.sleep(80);
                processTelemetryAsync(phase3, flight, listener).get();

            } catch (Exception e) {
                if (listener != null) listener.onError("Phase batch execution interrupted: " + e.getMessage());
            }
        });
    }

    /**
     * Simulates real-time multi-threaded telemetry streaming for demonstration.
     */
    public Future<?> startLiveSimulation(int flightId, Flight flight, Consumer<TelemetryData> onTick, Runnable onComplete) {
        return executorService.submit(() -> {
            try {
                FlightPhase[] phases = { FlightPhase.TAKEOFF_CLIMB, FlightPhase.CRUISE, FlightPhase.DESCENT_LANDING };
                double[] altitudes = { 10000.0, 36000.0, 3000.0 };
                double[] speeds = { 310.0, 480.0, 160.0 };

                for (int i = 0; i < phases.length; i++) {
                    double eng1Vib = 1.2 + (Math.random() * 2.9); // Realistic variance
                    double eng2Vib = 1.1 + (Math.random() * 2.8);

                    TelemetryData t = new TelemetryData(
                        flightId,
                        phases[i],
                        altitudes[i],
                        speeds[i],
                        eng1Vib,
                        eng2Vib,
                        flight.getTotalFuelBurnedKg() / 3.0,
                        640.0 + (Math.random() * 80),
                        58.0 + (Math.random() * 6)
                    );

                    processTelemetryAsync(t, flight, null).get();
                    if (onTick != null) onTick.accept(t);
                    Thread.sleep(600); // 600ms per phase tick in simulation
                }
                if (onComplete != null) onComplete.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[TelemetryWorker] Simulation failed: " + e.getMessage());
            }
        });
    }

    /**
     * Gracefully terminates the background ExecutorService thread pool.
     */
    public void shutdown() {
        isRunning.set(false);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return isRunning.get() && !executorService.isShutdown();
    }
}

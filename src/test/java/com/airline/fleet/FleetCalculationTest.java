package com.airline.fleet;

import com.airline.fleet.model.FleetCategory;
import com.airline.fleet.model.FleetGrade;
import com.airline.fleet.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating strict aviation calculations:
 * 1. SFC = TotalFuelBurned / (PayloadWeight * Distance)
 * 2. Fleet Carbon Intensity = (FuelBurned * 3.16) / ASK
 * 3. Engine Vibration Limits (< 2.5, 2.5 - 4.0, > 4.0)
 * 4. Fleet Grading (A, B, C, Outlier)
 */
public class FleetCalculationTest {

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService();
    }

    @Test
    @DisplayName("Test SFC Calculation Formula: TotalFuel / (Payload * Distance)")
    void testSfcCalculation() {
        double fuel = 42500.0;
        double payload = 48500.0;
        double distance = 5550.0;

        double expectedSfc = fuel / (payload * distance); // ~ 0.0000371
        double actualSfc = analyticsService.calculateSFC(fuel, payload, distance);

        assertEquals(expectedSfc, actualSfc, 1e-9, "SFC must strictly follow TotalFuel / (Payload * Distance)");
        assertTrue(actualSfc > 0, "SFC must be positive");
    }

    @Test
    @DisplayName("Test SFC throws exception on non-positive payload or distance")
    void testSfcInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> analyticsService.calculateSFC(1000, 0, 500));
        assertThrows(IllegalArgumentException.class, () -> analyticsService.calculateSFC(1000, 500, -10));
        assertThrows(IllegalArgumentException.class, () -> analyticsService.calculateSFC(-50, 500, 100));
    }

    @Test
    @DisplayName("Test Fleet Carbon Intensity Formula: (FuelBurned * 3.16) / ASK")
    void testCarbonIntensity() {
        double fuel = 42500.0;
        int seats = 396;
        double distance = 5550.0;
        double payload = 48500.0;

        double ask = seats * distance; // 2,197,800 ASK
        double expectedCo2 = (fuel * 3.16) / ask;

        double actualCo2 = analyticsService.calculateCarbonIntensity(fuel, seats, payload, distance);
        assertEquals(expectedCo2, actualCo2, 1e-6, "Carbon Intensity must match (Fuel * 3.16) / ASK");
    }

    @Test
    @DisplayName("Test Vibration Monitoring Safety Thresholds")
    void testVibrationThresholds() {
        // Normal: < 2.5 mils
        assertEquals("NORMAL", analyticsService.evaluateVibrationStatus(1.45));
        assertEquals("NORMAL", analyticsService.evaluateVibrationStatus(2.49));

        // Caution: 2.5 - 4.0 mils
        assertEquals("CAUTION", analyticsService.evaluateVibrationStatus(2.50));
        assertEquals("CAUTION", analyticsService.evaluateVibrationStatus(3.85));

        // Critical: >= 4.0 mils
        assertEquals("CRITICAL", analyticsService.evaluateVibrationStatus(4.00));
        assertEquals("CRITICAL", analyticsService.evaluateVibrationStatus(4.85));
    }

    @Test
    @DisplayName("Test Fleet Grading: A, B, C and Outlier")
    void testFleetGrading() {
        double baseline = 0.000042;

        // Top tier <= 0.000038 -> Grade A
        assertEquals(FleetGrade.A, analyticsService.evaluateFleetGrade(0.000035, baseline));

        // Nominal <= 0.000048 -> Grade B
        assertEquals(FleetGrade.B, analyticsService.evaluateFleetGrade(0.000043, baseline));

        // Elevated > 0.000048 but < 25% deviation -> Grade C
        assertEquals(FleetGrade.C, analyticsService.evaluateFleetGrade(0.000050, baseline));

        // Outlier > +25% deviation above baseline (0.000042 * 1.25 = 0.0000525)
        assertEquals(FleetGrade.OUTLIER, analyticsService.evaluateFleetGrade(0.000058, baseline));
    }
}

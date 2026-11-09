package com.airline.fleet;

import com.airline.fleet.model.Flight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating flight number regex: [A-Z]{2,3}\\d{3,4}
 */
public class RegexValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "AA101",
        "BA204",
        "DL1402",
        "AI342",
        "UAE201",
        "SWA4502",
        "FD701",
        "CX908"
    })
    @DisplayName("Valid Flight Numbers matching [A-Z]{2,3}\\d{3,4}")
    void testValidFlightNumbers(String validFlightNum) {
        assertTrue(Flight.isValidFlightNumber(validFlightNum),
            "Expected valid flight number for: " + validFlightNum);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "A101",         // Only 1 letter (requires 2 or 3)
        "TOOLONG123",   // 7 letters (exceeds 3)
        "AA12",         // Only 2 digits (requires 3 or 4)
        "BA12345",      // 5 digits (exceeds 4)
        "123AA",        // Digits first
        "AA-101",       // Hyphen not allowed
        "",             // Empty
        "   "           // Whitespace only
    })
    @DisplayName("Invalid Flight Numbers rejected by [A-Z]{2,3}\\d{3,4}")
    void testInvalidFlightNumbers(String invalidFlightNum) {
        assertFalse(Flight.isValidFlightNumber(invalidFlightNum),
            "Expected invalid flight number for: " + invalidFlightNum);
    }

    @Test
    @DisplayName("Test Flight.setFlightNumber throws IllegalArgumentException on invalid format")
    void testFlightModelEnforcement() {
        Flight f = new Flight();
        assertThrows(IllegalArgumentException.class, () -> f.setFlightNumber("INVALID_12"));
        assertDoesNotThrow(() -> f.setFlightNumber("AA302"));
    }
}

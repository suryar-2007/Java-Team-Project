package com.airline.fleet.model;

/**
 * Three-phase flight operations model:
 * Phase 1: Takeoff & Climb
 * Phase 2: Cruise
 * Phase 3: Descent & Landing
 */
public enum FlightPhase {
    TAKEOFF_CLIMB("Phase 1: Takeoff & Climb"),
    CRUISE("Phase 2: Cruise"),
    DESCENT_LANDING("Phase 3: Descent & Landing");

    private final String displayName;

    FlightPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FlightPhase fromString(String text) {
        if (text == null) return CRUISE;
        for (FlightPhase p : FlightPhase.values()) {
            if (p.name().equalsIgnoreCase(text) || p.displayName.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return CRUISE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

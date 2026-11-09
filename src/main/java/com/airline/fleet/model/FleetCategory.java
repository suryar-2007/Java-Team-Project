package com.airline.fleet.model;

/**
 * Commercial Airline Fleet Categories as required by the project specifications:
 * 1. Long-Haul International
 * 2. Short-Haul Regional
 * 3. Cargo Freight
 */
public enum FleetCategory {
    LONG_HAUL_INTERNATIONAL("Long-Haul International", 0.0000360),
    SHORT_HAUL_REGIONAL("Short-Haul Regional", 0.0000420),
    CARGO_FREIGHT("Cargo Freight", 0.0000375);

    private final String displayName;
    private final double defaultBaselineSfc;

    FleetCategory(String displayName, double defaultBaselineSfc) {
        this.displayName = displayName;
        this.defaultBaselineSfc = defaultBaselineSfc;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultBaselineSfc() {
        return defaultBaselineSfc;
    }

    public static FleetCategory fromString(String text) {
        if (text == null) return SHORT_HAUL_REGIONAL;
        for (FleetCategory cat : FleetCategory.values()) {
            if (cat.name().equalsIgnoreCase(text) || cat.displayName.equalsIgnoreCase(text)) {
                return cat;
            }
        }
        return SHORT_HAUL_REGIONAL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

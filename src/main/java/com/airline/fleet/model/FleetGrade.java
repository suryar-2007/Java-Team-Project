package com.airline.fleet.model;

/**
 * Airline Fleet Fuel Efficiency Grading:
 * Grade A: Optimal Performance / Top Tier Fuel Efficiency
 * Grade B: Nominal / Standard Operational Fleet Range
 * Grade C: Below Target / Elevated Fuel Consumption
 * OUTLIER: Severe Deviations (> 25% above baseline) triggering investigation
 */
public enum FleetGrade {
    A("Grade A - Top Efficiency", "#10B981"),
    B("Grade B - Nominal Efficiency", "#3B82F6"),
    C("Grade C - Below Average", "#F59E0B"),
    OUTLIER("Outlier - Anomaly Flagged", "#EF4444");

    private final String description;
    private final String hexColor;

    FleetGrade(String description, String hexColor) {
        this.description = description;
        this.hexColor = hexColor;
    }

    public String getDescription() {
        return description;
    }

    public String getHexColor() {
        return hexColor;
    }

    public static FleetGrade fromString(String val) {
        if (val == null) return B;
        for (FleetGrade g : FleetGrade.values()) {
            if (g.name().equalsIgnoreCase(val)) return g;
        }
        return B;
    }
}

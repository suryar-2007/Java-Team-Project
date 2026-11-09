package com.airline.fleet;

import com.airline.fleet.model.*;
import java.sql.*;
import java.util.*;

/**
 * FleetDAO: Data Access Object handling all database CRUD operations,
 * telemetry persistence, maintenance alerting, and aggregated fleet metrics.
 * Uses JDBC and PreparedStatement strictly for secure, robust querying.
 */
public class FleetDAO {

    // =========================================================================
    // 1. USER AUTHENTICATION
    // =========================================================================

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, full_name, role FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    // =========================================================================
    // 2. AIRCRAFT CRUD OPERATIONS
    // =========================================================================

    public List<Aircraft> getAllAircraft() throws SQLException {
        List<Aircraft> list = new ArrayList<>();
        String sql = "SELECT * FROM aircraft ORDER BY category, tail_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAircraft(rs));
            }
        }
        return list;
    }

    public Aircraft getAircraftById(int id) throws SQLException {
        String sql = "SELECT * FROM aircraft WHERE aircraft_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAircraft(rs);
                }
            }
        }
        return null;
    }

    public Aircraft getAircraftByTailNumber(String tailNumber) throws SQLException {
        String sql = "SELECT * FROM aircraft WHERE tail_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tailNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAircraft(rs);
                }
            }
        }
        return null;
    }

    public boolean insertAircraft(Aircraft a) throws SQLException {
        String sql = "INSERT INTO aircraft (tail_number, model, manufacturer, category, max_payload_kg, " +
                     "seat_capacity, engine_type, status, total_flight_hours, baseline_sfc) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTailNumber().trim().toUpperCase());
            ps.setString(2, a.getModel());
            ps.setString(3, a.getManufacturer());
            ps.setString(4, a.getCategory().name());
            ps.setDouble(5, a.getMaxPayloadKg());
            ps.setInt(6, a.getSeatCapacity());
            ps.setString(7, a.getEngineType());
            ps.setString(8, a.getStatus());
            ps.setDouble(9, a.getTotalFlightHours());
            ps.setDouble(10, a.getBaselineSfc());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) a.setAircraftId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateAircraft(Aircraft a) throws SQLException {
        String sql = "UPDATE aircraft SET tail_number = ?, model = ?, manufacturer = ?, category = ?, " +
                     "max_payload_kg = ?, seat_capacity = ?, engine_type = ?, status = ?, " +
                     "total_flight_hours = ?, baseline_sfc = ? WHERE aircraft_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTailNumber().trim().toUpperCase());
            ps.setString(2, a.getModel());
            ps.setString(3, a.getManufacturer());
            ps.setString(4, a.getCategory().name());
            ps.setDouble(5, a.getMaxPayloadKg());
            ps.setInt(6, a.getSeatCapacity());
            ps.setString(7, a.getEngineType());
            ps.setString(8, a.getStatus());
            ps.setDouble(9, a.getTotalFlightHours());
            ps.setDouble(10, a.getBaselineSfc());
            ps.setInt(11, a.getAircraftId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAircraft(int aircraftId) throws SQLException {
        String sql = "DELETE FROM aircraft WHERE aircraft_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, aircraftId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 3. FLIGHT CRUD OPERATIONS
    // =========================================================================

    public List<Flight> getAllFlights() throws SQLException {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT f.*, a.tail_number AS aircraft_tail FROM flights f " +
                     "JOIN aircraft a ON f.aircraft_id = a.aircraft_id " +
                     "ORDER BY f.departure_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapFlight(rs));
            }
        }
        return list;
    }

    public Flight getFlightById(int id) throws SQLException {
        String sql = "SELECT f.*, a.tail_number AS aircraft_tail FROM flights f " +
                     "JOIN aircraft a ON f.aircraft_id = a.aircraft_id " +
                     "WHERE f.flight_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFlight(rs);
                }
            }
        }
        return null;
    }

    public boolean insertFlight(Flight f) throws SQLException {
        String sql = "INSERT INTO flights (flight_number, aircraft_id, origin, destination, " +
                     "departure_time, arrival_time, distance_km, payload_weight_kg, passenger_count, " +
                     "category, status, total_fuel_burned_kg, calculated_sfc, carbon_intensity, fleet_grade) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getFlightNumber());
            ps.setInt(2, f.getAircraftId());
            ps.setString(3, f.getOrigin());
            ps.setString(4, f.getDestination());
            ps.setTimestamp(5, f.getDepartureTime());
            ps.setTimestamp(6, f.getArrivalTime());
            ps.setDouble(7, f.getDistanceKm());
            ps.setDouble(8, f.getPayloadWeightKg());
            ps.setInt(9, f.getPassengerCount());
            ps.setString(10, f.getCategory().name());
            ps.setString(11, f.getStatus());
            ps.setDouble(12, f.getTotalFuelBurnedKg());
            ps.setDouble(13, f.getCalculatedSfc());
            ps.setDouble(14, f.getCarbonIntensity());
            ps.setString(15, f.getFleetGrade().name());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) f.setFlightId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateFlight(Flight f) throws SQLException {
        String sql = "UPDATE flights SET flight_number = ?, aircraft_id = ?, origin = ?, destination = ?, " +
                     "departure_time = ?, arrival_time = ?, distance_km = ?, payload_weight_kg = ?, " +
                     "passenger_count = ?, category = ?, status = ?, total_fuel_burned_kg = ?, " +
                     "calculated_sfc = ?, carbon_intensity = ?, fleet_grade = ? WHERE flight_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNumber());
            ps.setInt(2, f.getAircraftId());
            ps.setString(3, f.getOrigin());
            ps.setString(4, f.getDestination());
            ps.setTimestamp(5, f.getDepartureTime());
            ps.setTimestamp(6, f.getArrivalTime());
            ps.setDouble(7, f.getDistanceKm());
            ps.setDouble(8, f.getPayloadWeightKg());
            ps.setInt(9, f.getPassengerCount());
            ps.setString(10, f.getCategory().name());
            ps.setString(11, f.getStatus());
            ps.setDouble(12, f.getTotalFuelBurnedKg());
            ps.setDouble(13, f.getCalculatedSfc());
            ps.setDouble(14, f.getCarbonIntensity());
            ps.setString(15, f.getFleetGrade().name());
            ps.setInt(16, f.getFlightId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFlight(int flightId) throws SQLException {
        String sql = "DELETE FROM flights WHERE flight_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 4. THREE-PHASE TELEMETRY DATA OPERATIONS
    // =========================================================================

    public boolean insertTelemetry(TelemetryData t) throws SQLException {
        String sql = "INSERT INTO telemetry_data (flight_id, phase, altitude_feet, speed_knots, " +
                     "engine1_vibration_mils, engine2_vibration_mils, phase_fuel_burned_kg, " +
                     "exhaust_gas_temp_c, oil_pressure_psi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getFlightId());
            ps.setString(2, t.getPhase().name());
            ps.setDouble(3, t.getAltitudeFeet());
            ps.setDouble(4, t.getSpeedKnots());
            ps.setDouble(5, t.getEngine1VibrationMils());
            ps.setDouble(6, t.getEngine2VibrationMils());
            ps.setDouble(7, t.getPhaseFuelBurnedKg());
            ps.setDouble(8, t.getExhaustGasTempC());
            ps.setDouble(9, t.getOilPressurePsi());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) t.setTelemetryId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public List<TelemetryData> getTelemetryByFlightId(int flightId) throws SQLException {
        List<TelemetryData> list = new ArrayList<>();
        String sql = "SELECT * FROM telemetry_data WHERE flight_id = ? ORDER BY telemetry_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TelemetryData t = new TelemetryData();
                    t.setTelemetryId(rs.getInt("telemetry_id"));
                    t.setFlightId(rs.getInt("flight_id"));
                    t.setPhase(FlightPhase.fromString(rs.getString("phase")));
                    t.setAltitudeFeet(rs.getDouble("altitude_feet"));
                    t.setSpeedKnots(rs.getDouble("speed_knots"));
                    t.setEngine1VibrationMils(rs.getDouble("engine1_vibration_mils"));
                    t.setEngine2VibrationMils(rs.getDouble("engine2_vibration_mils"));
                    t.setPhaseFuelBurnedKg(rs.getDouble("phase_fuel_burned_kg"));
                    t.setExhaustGasTempC(rs.getDouble("exhaust_gas_temp_c"));
                    t.setOilPressurePsi(rs.getDouble("oil_pressure_psi"));
                    t.setTimestamp(rs.getTimestamp("timestamp"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 5. AUTOMATIC MAINTENANCE ALERTS
    // =========================================================================

    public List<MaintenanceFlag> getAllMaintenanceFlags() throws SQLException {
        List<MaintenanceFlag> list = new ArrayList<>();
        String sql = "SELECT m.*, a.tail_number AS aircraft_tail, f.flight_number " +
                     "FROM maintenance_flags m " +
                     "JOIN aircraft a ON m.aircraft_id = a.aircraft_id " +
                     "LEFT JOIN flights f ON m.flight_id = f.flight_id " +
                     "ORDER BY FIELD(m.severity, 'CRITICAL', 'WARNING', 'INFO'), m.status ASC, m.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapMaintenanceFlag(rs));
            }
        }
        return list;
    }

    public boolean insertMaintenanceFlag(MaintenanceFlag flag) throws SQLException {
        String sql = "INSERT INTO maintenance_flags (aircraft_id, flight_id, severity, flag_type, " +
                     "description, vibration_reading_mils, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, flag.getAircraftId());
            if (flag.getFlightId() != null && flag.getFlightId() > 0) {
                ps.setInt(2, flag.getFlightId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, flag.getSeverity());
            ps.setString(4, flag.getFlagType());
            ps.setString(5, flag.getDescription());
            if (flag.getVibrationReadingMils() != null) {
                ps.setDouble(6, flag.getVibrationReadingMils());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }
            ps.setString(7, flag.getStatus());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) flag.setFlagId(gk.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean resolveMaintenanceFlag(int flagId, String resolvedBy, String notes) throws SQLException {
        String sql = "UPDATE maintenance_flags SET status = 'RESOLVED', resolved_by = ?, " +
                     "resolution_notes = ?, resolved_at = CURRENT_TIMESTAMP WHERE flag_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resolvedBy);
            ps.setString(2, notes);
            ps.setInt(3, flagId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 6. FLEET AGGREGATE ANALYTICS & DASHBOARD METRICS
    // =========================================================================

    public Map<String, Object> getFleetSummaryMetrics() throws SQLException {
        Map<String, Object> metrics = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            // Total Aircraft
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM aircraft")) {
                if (rs.next()) metrics.put("totalAircraft", rs.getInt(1));
            }
            // Active Aircraft
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM aircraft WHERE status = 'ACTIVE'")) {
                if (rs.next()) metrics.put("activeAircraft", rs.getInt(1));
            }
            // Total Flights
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM flights")) {
                if (rs.next()) metrics.put("totalFlights", rs.getInt(1));
            }
            // Fleet Average SFC
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT AVG(calculated_sfc) FROM flights WHERE calculated_sfc > 0")) {
                if (rs.next()) metrics.put("avgSfc", rs.getDouble(1));
            }
            // Fleet Average Carbon Intensity
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT AVG(carbon_intensity) FROM flights WHERE carbon_intensity > 0")) {
                if (rs.next()) metrics.put("avgCarbonIntensity", rs.getDouble(1));
            }
            // Open Alerts Count (Critical & Warning)
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM maintenance_flags WHERE status = 'OPEN'")) {
                if (rs.next()) metrics.put("openAlerts", rs.getInt(1));
            }
            // Critical Alerts Count
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM maintenance_flags WHERE status = 'OPEN' AND severity = 'CRITICAL'")) {
                if (rs.next()) metrics.put("criticalAlerts", rs.getInt(1));
            }
        }
        return metrics;
    }

    public Map<String, Integer> getFleetGradeDistribution() throws SQLException {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("A", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);
        distribution.put("OUTLIER", 0);

        String sql = "SELECT fleet_grade, COUNT(*) AS cnt FROM flights GROUP BY fleet_grade";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                distribution.put(rs.getString("fleet_grade"), rs.getInt("cnt"));
            }
        }
        return distribution;
    }

    public Map<String, Double> getCategoryFuelBurn() throws SQLException {
        Map<String, Double> categoryFuel = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(total_fuel_burned_kg) as total_fuel FROM flights GROUP BY category";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categoryFuel.put(rs.getString("category"), rs.getDouble("total_fuel"));
            }
        }
        return categoryFuel;
    }

    // =========================================================================
    // 7. SYSTEM CONFIGURATION ACCESS
    // =========================================================================

    public String getConfigValue(String key, String defaultValue) {
        String sql = "SELECT config_value FROM system_config WHERE config_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("config_value");
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }

    public boolean setConfigValue(String key, String value, String desc) {
        String sql = "INSERT INTO system_config (config_key, config_value, description) " +
                     "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE config_value = ?, description = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, desc);
            ps.setString(4, value);
            ps.setString(5, desc);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // MAPPER HELPER METHODS
    // =========================================================================

    private Aircraft mapAircraft(ResultSet rs) throws SQLException {
        Aircraft a = new Aircraft();
        a.setAircraftId(rs.getInt("aircraft_id"));
        a.setTailNumber(rs.getString("tail_number"));
        a.setModel(rs.getString("model"));
        a.setManufacturer(rs.getString("manufacturer"));
        a.setCategory(FleetCategory.fromString(rs.getString("category")));
        a.setMaxPayloadKg(rs.getDouble("max_payload_kg"));
        a.setSeatCapacity(rs.getInt("seat_capacity"));
        a.setEngineType(rs.getString("engine_type"));
        a.setStatus(rs.getString("status"));
        a.setTotalFlightHours(rs.getDouble("total_flight_hours"));
        a.setBaselineSfc(rs.getDouble("baseline_sfc"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }

    private Flight mapFlight(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setFlightId(rs.getInt("flight_id"));
        f.setFlightNumber(rs.getString("flight_number"));
        f.setAircraftId(rs.getInt("aircraft_id"));
        try {
            f.setAircraftTailNumber(rs.getString("aircraft_tail"));
        } catch (SQLException ignored) {}
        f.setOrigin(rs.getString("origin"));
        f.setDestination(rs.getString("destination"));
        f.setDepartureTime(rs.getTimestamp("departure_time"));
        f.setArrivalTime(rs.getTimestamp("arrival_time"));
        f.setDistanceKm(rs.getDouble("distance_km"));
        f.setPayloadWeightKg(rs.getDouble("payload_weight_kg"));
        f.setPassengerCount(rs.getInt("passenger_count"));
        f.setCategory(FleetCategory.fromString(rs.getString("category")));
        f.setStatus(rs.getString("status"));
        f.setTotalFuelBurnedKg(rs.getDouble("total_fuel_burned_kg"));
        f.setCalculatedSfc(rs.getDouble("calculated_sfc"));
        f.setCarbonIntensity(rs.getDouble("carbon_intensity"));
        f.setFleetGrade(FleetGrade.fromString(rs.getString("fleet_grade")));
        f.setCreatedAt(rs.getTimestamp("created_at"));
        return f;
    }

    private MaintenanceFlag mapMaintenanceFlag(ResultSet rs) throws SQLException {
        MaintenanceFlag m = new MaintenanceFlag();
        m.setFlagId(rs.getInt("flag_id"));
        m.setAircraftId(rs.getInt("aircraft_id"));
        try {
            m.setAircraftTailNumber(rs.getString("aircraft_tail"));
        } catch (SQLException ignored) {}
        int fId = rs.getInt("flight_id");
        if (!rs.wasNull()) m.setFlightId(fId);
        try {
            m.setFlightNumber(rs.getString("flight_number"));
        } catch (SQLException ignored) {}
        m.setSeverity(rs.getString("severity"));
        m.setFlagType(rs.getString("flag_type"));
        m.setDescription(rs.getString("description"));
        double vib = rs.getDouble("vibration_reading_mils");
        if (!rs.wasNull()) m.setVibrationReadingMils(vib);
        m.setStatus(rs.getString("status"));
        m.setResolvedBy(rs.getString("resolved_by"));
        m.setResolutionNotes(rs.getString("resolution_notes"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        m.setResolvedAt(rs.getTimestamp("resolved_at"));
        return m;
    }
}

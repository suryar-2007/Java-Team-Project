-- ============================================================================
-- Commercial Airline Fleet Fuel Efficiency & Maintenance Analytics Engine
-- Database Schema: fleet_schema.sql
-- Target Database: airline_fleet_db (MySQL 8.0+)
-- ============================================================================

-- Create Database if not exists
CREATE DATABASE IF NOT EXISTS airline_fleet_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE airline_fleet_db;

-- ----------------------------------------------------------------------------
-- Table 1: users (Authentication and Role Management)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS maintenance_flags;
DROP TABLE IF EXISTS telemetry_data;
DROP TABLE IF EXISTS flights;
DROP TABLE IF EXISTS aircraft;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS system_config;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'FLEET_MANAGER', 'CHIEF_ENGINEER', 'ANALYST') NOT NULL DEFAULT 'FLEET_MANAGER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 2: aircraft (Fleet Inventory Management - CRUD)
-- Categories: LONG_HAUL_INTERNATIONAL, SHORT_HAUL_REGIONAL, CARGO_FREIGHT
-- ----------------------------------------------------------------------------
CREATE TABLE aircraft (
    aircraft_id INT AUTO_INCREMENT PRIMARY KEY,
    tail_number VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(60) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    category ENUM('LONG_HAUL_INTERNATIONAL', 'SHORT_HAUL_REGIONAL', 'CARGO_FREIGHT') NOT NULL,
    max_payload_kg DOUBLE NOT NULL,
    seat_capacity INT NOT NULL DEFAULT 0,
    engine_type VARCHAR(60) NOT NULL,
    status ENUM('ACTIVE', 'MAINTENANCE', 'GROUNDED') NOT NULL DEFAULT 'ACTIVE',
    total_flight_hours DOUBLE NOT NULL DEFAULT 0.0,
    baseline_sfc DOUBLE NOT NULL DEFAULT 0.000045,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 3: flights (Flight Operations - CRUD)
-- Flight Number Regex Pattern: ^[A-Z]{2,3}\\d{3,4}$
-- ----------------------------------------------------------------------------
CREATE TABLE flights (
    flight_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    aircraft_id INT NOT NULL,
    origin VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    distance_km DOUBLE NOT NULL,
    payload_weight_kg DOUBLE NOT NULL,
    passenger_count INT NOT NULL DEFAULT 0,
    category ENUM('LONG_HAUL_INTERNATIONAL', 'SHORT_HAUL_REGIONAL', 'CARGO_FREIGHT') NOT NULL,
    status ENUM('SCHEDULED', 'IN_FLIGHT', 'COMPLETED', 'DIVERTED') NOT NULL DEFAULT 'COMPLETED',
    total_fuel_burned_kg DOUBLE NOT NULL DEFAULT 0.0,
    calculated_sfc DOUBLE NOT NULL DEFAULT 0.0,
    carbon_intensity DOUBLE NOT NULL DEFAULT 0.0,
    fleet_grade ENUM('A', 'B', 'C', 'OUTLIER') NOT NULL DEFAULT 'B',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flight_aircraft FOREIGN KEY (aircraft_id) 
        REFERENCES aircraft(aircraft_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 4: telemetry_data (Three-Phase Flight Sensor Stream)
-- Phases: TAKEOFF_CLIMB, CRUISE, DESCENT_LANDING
-- ----------------------------------------------------------------------------
CREATE TABLE telemetry_data (
    telemetry_id INT AUTO_INCREMENT PRIMARY KEY,
    flight_id INT NOT NULL,
    phase ENUM('TAKEOFF_CLIMB', 'CRUISE', 'DESCENT_LANDING') NOT NULL,
    altitude_feet DOUBLE NOT NULL,
    speed_knots DOUBLE NOT NULL,
    engine1_vibration_mils DOUBLE NOT NULL,
    engine2_vibration_mils DOUBLE NOT NULL,
    phase_fuel_burned_kg DOUBLE NOT NULL,
    exhaust_gas_temp_c DOUBLE NOT NULL DEFAULT 650.0,
    oil_pressure_psi DOUBLE NOT NULL DEFAULT 60.0,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_telemetry_flight FOREIGN KEY (flight_id) 
        REFERENCES flights(flight_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 5: maintenance_flags (Automatic Anomaly & Alert Engine)
-- ----------------------------------------------------------------------------
CREATE TABLE maintenance_flags (
    flag_id INT AUTO_INCREMENT PRIMARY KEY,
    aircraft_id INT NOT NULL,
    flight_id INT NULL,
    severity ENUM('CRITICAL', 'WARNING', 'INFO') NOT NULL DEFAULT 'WARNING',
    flag_type VARCHAR(60) NOT NULL,
    description TEXT NOT NULL,
    vibration_reading_mils DOUBLE NULL,
    status ENUM('OPEN', 'ACKNOWLEDGED', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    resolved_by VARCHAR(100) NULL,
    resolution_notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    CONSTRAINT fk_flag_aircraft FOREIGN KEY (aircraft_id) 
        REFERENCES aircraft(aircraft_id) ON DELETE CASCADE,
    CONSTRAINT fk_flag_flight FOREIGN KEY (flight_id) 
        REFERENCES flights(flight_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table 6: system_config (Configurable Thresholds and Scoring Parameters)
-- ----------------------------------------------------------------------------
CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- SEED DATA: Configurable System Parameters
-- ----------------------------------------------------------------------------
INSERT INTO system_config (config_key, config_value, description) VALUES
('vibration_caution_limit', '2.5', 'Engine vibration threshold in mils triggering WARNING advisory'),
('vibration_critical_limit', '4.0', 'Engine vibration threshold in mils triggering CRITICAL emergency alert'),
('sfc_grade_a_threshold', '0.000038', 'SFC upper boundary for Grade A (Top Efficiency)'),
('sfc_grade_b_threshold', '0.000048', 'SFC upper boundary for Grade B (Standard Efficiency)'),
('sfc_outlier_pct_deviation', '25.0', 'Percentage deviation over baseline triggering Outlier alert'),
('co2_emission_factor', '3.16', 'ICAO standard emission factor (kg CO2 per kg Jet-A fuel burned)');

-- ----------------------------------------------------------------------------
-- SEED DATA: Default Users (College Project Demo Accounts)
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', 'admin123', 'Chief Fleet Director', 'ADMIN'),
('engineer', 'engineer123', 'Lead Maintenance Engineer', 'CHIEF_ENGINEER'),
('analyst', 'analyst123', 'Fuel & Carbon Analyst', 'ANALYST');

-- ----------------------------------------------------------------------------
-- SEED DATA: Fleet Aircraft across all 3 Categories
-- ----------------------------------------------------------------------------
INSERT INTO aircraft (tail_number, model, manufacturer, category, max_payload_kg, seat_capacity, engine_type, status, total_flight_hours, baseline_sfc) VALUES
-- Long-Haul International
('N771AA', 'Boeing 777-300ER', 'Boeing', 'LONG_HAUL_INTERNATIONAL', 69850.0, 396, 'GE90-115B', 'ACTIVE', 4820.5, 0.0000375),
('F-GZCP', 'Airbus A350-900', 'Airbus', 'LONG_HAUL_INTERNATIONAL', 53300.0, 325, 'Rolls-Royce Trent XWB', 'ACTIVE', 2910.0, 0.0000340),
('VT-ANK', 'Boeing 787-8 Dreamliner', 'Boeing', 'LONG_HAUL_INTERNATIONAL', 43300.0, 256, 'GEnx-1B', 'ACTIVE', 3450.2, 0.0000355),

-- Short-Haul Regional
('N8701M', 'Boeing 737-MAX8', 'Boeing', 'SHORT_HAUL_REGIONAL', 20880.0, 178, 'CFM LEAP-1B', 'ACTIVE', 1890.4, 0.0000420),
('VT-NEO', 'Airbus A320neo', 'Airbus', 'SHORT_HAUL_REGIONAL', 20000.0, 186, 'CFM LEAP-1A', 'ACTIVE', 2145.8, 0.0000410),
('PR-ZIJ', 'Embraer E195-E2', 'Embraer', 'SHORT_HAUL_REGIONAL', 16150.0, 132, 'PW1900G', 'MAINTENANCE', 1230.0, 0.0000445),

-- Cargo Freight
('N701FD', 'Boeing 777F Freighter', 'Boeing', 'CARGO_FREIGHT', 102010.0, 0, 'GE90-110B1L', 'ACTIVE', 5230.0, 0.0000360),
('N856GT', 'Boeing 747-8F Freighter', 'Boeing', 'CARGO_FREIGHT', 137700.0, 0, 'GEnx-2B67', 'ACTIVE', 7640.5, 0.0000390);

-- ----------------------------------------------------------------------------
-- SEED DATA: Flight Operations (with Regex Flight Numbers)
-- ----------------------------------------------------------------------------
INSERT INTO flights (flight_number, aircraft_id, origin, destination, departure_time, arrival_time, distance_km, payload_weight_kg, passenger_count, category, status, total_fuel_burned_kg, calculated_sfc, carbon_intensity, fleet_grade) VALUES
-- AA101: JFK to LHR (Long-Haul, A350/B777)
('AA101', 1, 'JFK', 'LHR', '2026-11-01 18:30:00', '2026-11-02 06:20:00', 5550.0, 48500.0, 310, 'LONG_HAUL_INTERNATIONAL', 'COMPLETED', 42500.0, 0.0000371, 0.0610, 'A'),

-- BA204: LHR to DXB (Long-Haul)
('BA204', 2, 'LHR', 'DXB', '2026-11-02 12:15:00', '2026-11-02 23:05:00', 5500.0, 41200.0, 290, 'LONG_HAUL_INTERNATIONAL', 'COMPLETED', 38200.0, 0.0000363, 0.0573, 'A'),

-- AI342: DEL to BOM (Short-Haul)
('AI342', 5, 'DEL', 'BOM', '2026-11-03 07:00:00', '2026-11-03 09:10:00', 1150.0, 16800.0, 172, 'SHORT_HAUL_REGIONAL', 'COMPLETED', 3450.0, 0.0000438, 0.0509, 'B'),

-- DL4502: ATL to MCO (Short-Haul)
('DL4502', 4, 'ATL', 'MCO', '2026-11-04 10:30:00', '2026-11-04 12:00:00', 650.0, 18200.0, 165, 'SHORT_HAUL_REGIONAL', 'COMPLETED', 2150.0, 0.0000481, 0.0588, 'C'),

-- FX801: MEM to FRA (Cargo Freight)
('FX801', 7, 'MEM', 'FRA', '2026-11-05 21:00:00', '2026-11-06 11:30:00', 7420.0, 91500.0, 0, 'CARGO_FREIGHT', 'COMPLETED', 66500.0, 0.0000365, 0.0768, 'A'),

-- CX908: HKG to ANC (Cargo Freight - Outlier due to headwinds & compressor stall)
('CX908', 8, 'HKG', 'ANC', '2026-11-06 01:45:00', '2026-11-06 14:15:00', 8200.0, 112000.0, 0, 'CARGO_FREIGHT', 'COMPLETED', 98500.0, 0.0000572, 0.1202, 'OUTLIER');

-- ----------------------------------------------------------------------------
-- SEED DATA: Three-Phase Telemetry Data for Flights
-- ----------------------------------------------------------------------------
-- Flight 1 (AA101):
INSERT INTO telemetry_data (flight_id, phase, altitude_feet, speed_knots, engine1_vibration_mils, engine2_vibration_mils, phase_fuel_burned_kg, exhaust_gas_temp_c, oil_pressure_psi) VALUES
(1, 'TAKEOFF_CLIMB', 12000.0, 310.0, 1.45, 1.52, 9200.0, 710.0, 65.0),
(1, 'CRUISE', 38000.0, 485.0, 1.62, 1.58, 29800.0, 640.0, 62.0),
(1, 'DESCENT_LANDING', 3500.0, 160.0, 1.30, 1.35, 3500.0, 580.0, 60.0);

-- Flight 3 (AI342):
INSERT INTO telemetry_data (flight_id, phase, altitude_feet, speed_knots, engine1_vibration_mils, engine2_vibration_mils, phase_fuel_burned_kg, exhaust_gas_temp_c, oil_pressure_psi) VALUES
(3, 'TAKEOFF_CLIMB', 15000.0, 290.0, 1.80, 1.75, 1200.0, 690.0, 64.0),
(3, 'CRUISE', 34000.0, 450.0, 1.95, 2.10, 1850.0, 625.0, 61.0),
(3, 'DESCENT_LANDING', 2500.0, 145.0, 1.50, 1.60, 400.0, 560.0, 59.0);

-- Flight 6 (CX908): High vibration detected on Engine 1 during Cruise & Descent!
INSERT INTO telemetry_data (flight_id, phase, altitude_feet, speed_knots, engine1_vibration_mils, engine2_vibration_mils, phase_fuel_burned_kg, exhaust_gas_temp_c, oil_pressure_psi) VALUES
(6, 'TAKEOFF_CLIMB', 14000.0, 320.0, 2.65, 1.80, 19500.0, 745.0, 66.0),
(6, 'CRUISE', 36000.0, 470.0, 4.35, 1.95, 71000.0, 785.0, 54.0),
(6, 'DESCENT_LANDING', 4000.0, 165.0, 4.10, 1.70, 8000.0, 690.0, 52.0);

-- ----------------------------------------------------------------------------
-- SEED DATA: Automatic Maintenance Alerts Generated by System
-- ----------------------------------------------------------------------------
INSERT INTO maintenance_flags (aircraft_id, flight_id, severity, flag_type, description, vibration_reading_mils, status, resolved_by, resolution_notes) VALUES
(8, 6, 'CRITICAL', 'VIBRATION_LIMIT_EXCEEDED', 'Engine 1 vibration peaked at 4.35 mils during Phase 2 (Cruise) exceeding critical threshold of 4.0 mils. Immediate borescope inspection required.', 4.35, 'OPEN', NULL, NULL),
(8, 6, 'WARNING', 'SFC_ANOMALY', 'Flight CX908 calculated SFC (0.0000572) represents +46.6% deviation above category baseline (0.0000390). Outlier fuel burn detected.', NULL, 'OPEN', NULL, NULL),
(6, NULL, 'WARNING', 'VIBRATION_LIMIT_EXCEEDED', 'Engine 2 N2 vibration advisory at 2.78 mils during post-flight ground run. Scheduled fan blade balancing.', 2.78, 'ACKNOWLEDGED', 'Lead Engineer Mark', 'Scheduled for overnight maintenance hangar bay 4');

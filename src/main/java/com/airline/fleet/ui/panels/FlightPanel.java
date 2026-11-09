package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.TelemetryWorker;
import com.airline.fleet.model.*;
import com.airline.fleet.service.AnalyticsService;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * FlightPanel: Flight Operations Management with Three-Phase Data Entry.
 * Enforces regex pattern [A-Z]{2,3}\\d{3,4} on Flight Numbers,
 * computes SFC and Carbon Intensity, and dispatches to multithreaded TelemetryWorker.
 */
public class FlightPanel extends JPanel {

    private final FleetDAO fleetDAO;
    private final AnalyticsService analyticsService;
    private final TelemetryWorker telemetryWorker;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryCombo;
    private JTextField searchField;
    private JLabel statusLabel;

    public FlightPanel(TelemetryWorker telemetryWorker) {
        this.fleetDAO = new FleetDAO();
        this.analyticsService = new AnalyticsService();
        this.telemetryWorker = telemetryWorker;

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadData();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftToolbar.setOpaque(false);

        JLabel title = new JLabel("Flight Operations & Three-Phase Fuel Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        leftToolbar.add(title);

        leftToolbar.add(new JLabel("  Category:"));
        categoryCombo = new JComboBox<>(new String[]{"ALL CATEGORIES", "LONG_HAUL_INTERNATIONAL", "SHORT_HAUL_REGIONAL", "CARGO_FREIGHT"});
        categoryCombo.setFont(Theme.FONT_BODY);
        categoryCombo.addActionListener(e -> filterData());
        leftToolbar.add(categoryCombo);

        leftToolbar.add(new JLabel("  Search:"));
        searchField = new JTextField(10);
        searchField.setFont(Theme.FONT_BODY);
        searchField.addActionListener(e -> filterData());
        leftToolbar.add(searchField);

        JButton filterBtn = Theme.createOutlineButton("Filter");
        filterBtn.addActionListener(e -> filterData());
        leftToolbar.add(filterBtn);

        topPanel.add(leftToolbar, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton newFlightBtn = Theme.createPrimaryButton("+ 3-Phase Flight Entry");
        newFlightBtn.addActionListener(e -> openThreePhaseFlightDialog());
        actionPanel.add(newFlightBtn);

        JButton deleteBtn = Theme.createDangerButton("🗑 Delete Flight");
        deleteBtn.addActionListener(e -> deleteSelectedFlight());
        actionPanel.add(deleteBtn);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Flight No", "Aircraft", "Route", "Dist (km)", "Payload (kg)", "Pax", "Category", "Fuel (kg)", "SFC", "CO2/ASK", "Grade", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom status bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        statusLabel = new JLabel("Regex Pattern Enforced: [A-Z]{2,3}\\d{3,4} (e.g. AA101, BA204, DEL340)");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JButton refreshBtn = Theme.createOutlineButton("↻ Reload");
        refreshBtn.addActionListener(e -> loadData());
        bottomBar.add(refreshBtn, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Flight> list = fleetDAO.getAllFlights();
            for (Flight f : list) {
                tableModel.addRow(new Object[]{
                    f.getFlightId(),
                    f.getFlightNumber(),
                    f.getAircraftTailNumber() != null ? f.getAircraftTailNumber() : "ID:" + f.getAircraftId(),
                    f.getOrigin() + " → " + f.getDestination(),
                    String.format("%,.0f", f.getDistanceKm()),
                    String.format("%,.0f", f.getPayloadWeightKg()),
                    f.getPassengerCount(),
                    f.getCategory().getDisplayName(),
                    String.format("%,.0f", f.getTotalFuelBurnedKg()),
                    String.format("%.7f", f.getCalculatedSfc()),
                    String.format("%.4f", f.getCarbonIntensity()),
                    f.getFleetGrade() != null ? f.getFleetGrade().name() : "B",
                    f.getStatus()
                });
            }
            statusLabel.setText("Loaded " + list.size() + " flights. Real-time calculations synced.");
        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void filterData() {
        String cat = (String) categoryCombo.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        try {
            List<Flight> list = fleetDAO.getAllFlights();
            for (Flight f : list) {
                boolean catMatch = "ALL CATEGORIES".equals(cat) || f.getCategory().name().equals(cat);
                boolean queryMatch = query.isEmpty() ||
                    f.getFlightNumber().toLowerCase().contains(query) ||
                    f.getOrigin().toLowerCase().contains(query) ||
                    f.getDestination().toLowerCase().contains(query) ||
                    (f.getAircraftTailNumber() != null && f.getAircraftTailNumber().toLowerCase().contains(query));

                if (catMatch && queryMatch) {
                    tableModel.addRow(new Object[]{
                        f.getFlightId(),
                        f.getFlightNumber(),
                        f.getAircraftTailNumber() != null ? f.getAircraftTailNumber() : "ID:" + f.getAircraftId(),
                        f.getOrigin() + " → " + f.getDestination(),
                        String.format("%,.0f", f.getDistanceKm()),
                        String.format("%,.0f", f.getPayloadWeightKg()),
                        f.getPassengerCount(),
                        f.getCategory().getDisplayName(),
                        String.format("%,.0f", f.getTotalFuelBurnedKg()),
                        String.format("%.7f", f.getCalculatedSfc()),
                        String.format("%.4f", f.getCarbonIntensity()),
                        f.getFleetGrade() != null ? f.getFleetGrade().name() : "B",
                        f.getStatus()
                    });
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Error filtering flights: " + e.getMessage());
        }
    }

    private void deleteSelectedFlight() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String fltNum = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete flight " + fltNum + " (ID: " + id + ") and its telemetry?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (fleetDAO.deleteFlight(id)) {
                    JOptionPane.showMessageDialog(this, "Flight " + fltNum + " deleted successfully.");
                    loadData();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Three-Phase Flight Data Entry Dialog
     */
    private void openThreePhaseFlightDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Three-Phase Flight Data Entry", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(580, 680);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Section 1: General Flight Info
        JPanel generalCard = Theme.createCardPanel();
        generalCard.setLayout(new GridLayout(6, 2, 8, 8));
        generalCard.setBorder(BorderFactory.createTitledBorder("Flight Operations Profile"));

        JTextField flightNumField = new JTextField("AA105");
        JComboBox<Aircraft> aircraftCombo = new JComboBox<>();
        try {
            List<Aircraft> aircraftList = fleetDAO.getAllAircraft();
            for (Aircraft a : aircraftList) aircraftCombo.addItem(a);
        } catch (SQLException ignored) {}

        JTextField originField = new JTextField("JFK");
        JTextField destField = new JTextField("LHR");
        JTextField distField = new JTextField("5550");
        JTextField payloadField = new JTextField("45000");

        generalCard.add(new JLabel("Flight No (Regex: [A-Z]{2,3}\\d{3,4}):")); generalCard.add(flightNumField);
        generalCard.add(new JLabel("Assigned Aircraft:")); generalCard.add(aircraftCombo);
        generalCard.add(new JLabel("Origin (ICAO/IATA):")); generalCard.add(originField);
        generalCard.add(new JLabel("Destination (ICAO/IATA):")); generalCard.add(destField);
        generalCard.add(new JLabel("Distance (km):")); generalCard.add(distField);
        generalCard.add(new JLabel("Payload Weight (kg):")); generalCard.add(payloadField);
        mainPanel.add(generalCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Section 2: Three-Phase Flight Fuel & Vibration Entry
        JPanel phaseCard = Theme.createCardPanel();
        phaseCard.setLayout(new GridLayout(4, 3, 8, 8));
        phaseCard.setBorder(BorderFactory.createTitledBorder("Three-Phase Fuel & Vibration Sensor Readings"));

        phaseCard.add(new JLabel("Flight Phase"));
        phaseCard.add(new JLabel("Fuel Burned (kg)"));
        phaseCard.add(new JLabel("Engine Vibration (mils)"));

        // Phase 1
        phaseCard.add(new JLabel("Phase 1: Takeoff & Climb"));
        JTextField p1Fuel = new JTextField("9200");
        JTextField p1Vib = new JTextField("1.45");
        phaseCard.add(p1Fuel); phaseCard.add(p1Vib);

        // Phase 2
        phaseCard.add(new JLabel("Phase 2: Cruise"));
        JTextField p2Fuel = new JTextField("28500");
        JTextField p2Vib = new JTextField("1.60");
        phaseCard.add(p2Fuel); phaseCard.add(p2Vib);

        // Phase 3
        phaseCard.add(new JLabel("Phase 3: Descent & Landing"));
        JTextField p3Fuel = new JTextField("3300");
        JTextField p3Vib = new JTextField("1.35");
        phaseCard.add(p3Fuel); phaseCard.add(p3Vib);

        mainPanel.add(phaseCard);
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton cancelBtn = Theme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = Theme.createPrimaryButton("Submit & Compute Analytics");
        saveBtn.addActionListener(e -> {
            try {
                String fltNo = flightNumField.getText().trim().toUpperCase();
                // Regex Validation
                if (!Flight.isValidFlightNumber(fltNo)) {
                    JOptionPane.showMessageDialog(dialog,
                        "Invalid Flight Number: '" + fltNo + "'\nMust match regex [A-Z]{2,3}\\d{3,4}\nExamples: AA101, BA204, DEL3402",
                        "Regex Validation Failed",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Aircraft selectedAc = (Aircraft) aircraftCombo.getSelectedItem();
                if (selectedAc == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select an aircraft.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double dist = Double.parseDouble(distField.getText().trim());
                double payload = Double.parseDouble(payloadField.getText().trim());

                double fuel1 = Double.parseDouble(p1Fuel.getText().trim());
                double fuel2 = Double.parseDouble(p2Fuel.getText().trim());
                double fuel3 = Double.parseDouble(p3Fuel.getText().trim());
                double totalFuel = fuel1 + fuel2 + fuel3;

                double vib1 = Double.parseDouble(p1Vib.getText().trim());
                double vib2 = Double.parseDouble(p2Vib.getText().trim());
                double vib3 = Double.parseDouble(p3Vib.getText().trim());

                // Aviation Calculations
                double sfc = analyticsService.calculateSFC(totalFuel, payload, dist);
                double co2Intensity = analyticsService.calculateCarbonIntensity(totalFuel, selectedAc.getSeatCapacity(), selectedAc.getMaxPayloadKg(), dist);
                FleetGrade grade = analyticsService.evaluateFleetGrade(sfc, selectedAc.getBaselineSfc());

                Flight f = new Flight();
                f.setFlightNumber(fltNo);
                f.setAircraftId(selectedAc.getAircraftId());
                f.setOrigin(originField.getText().trim().toUpperCase());
                f.setDestination(destField.getText().trim().toUpperCase());
                f.setDepartureTime(new Timestamp(System.currentTimeMillis() - 7200000));
                f.setArrivalTime(new Timestamp(System.currentTimeMillis()));
                f.setDistanceKm(dist);
                f.setPayloadWeightKg(payload);
                f.setPassengerCount(selectedAc.getSeatCapacity());
                f.setCategory(selectedAc.getCategory());
                f.setStatus("COMPLETED");
                f.setTotalFuelBurnedKg(totalFuel);
                f.setCalculatedSfc(sfc);
                f.setCarbonIntensity(co2Intensity);
                f.setFleetGrade(grade);

                if (fleetDAO.insertFlight(f)) {
                    // Create Telemetry Packets
                    TelemetryData t1 = new TelemetryData(f.getFlightId(), FlightPhase.TAKEOFF_CLIMB, 12000, 310, vib1, vib1 * 0.95, fuel1, 710, 65);
                    TelemetryData t2 = new TelemetryData(f.getFlightId(), FlightPhase.CRUISE, 36000, 480, vib2, vib2 * 0.98, fuel2, 640, 62);
                    TelemetryData t3 = new TelemetryData(f.getFlightId(), FlightPhase.DESCENT_LANDING, 3000, 150, vib3, vib3 * 0.94, fuel3, 580, 59);

                    // Multithreaded telemetry processing & alert checking
                    if (telemetryWorker != null) {
                        telemetryWorker.processThreePhasesAsync(f.getFlightId(), f, t1, t2, t3, null);
                    } else {
                        fleetDAO.insertTelemetry(t1);
                        fleetDAO.insertTelemetry(t2);
                        fleetDAO.insertTelemetry(t3);
                    }

                    JOptionPane.showMessageDialog(dialog,
                        String.format("Flight %s saved successfully!\n\nCalculated Metrics:\n• Total Fuel: %,.0f kg\n• SFC: %.7f kg/(kg*km)\n• Carbon Intensity: %.4f kg CO2/ASK\n• Fleet Grade: %s",
                            fltNo, totalFuel, sfc, co2Intensity, grade.name()),
                        "Flight Processed",
                        JOptionPane.INFORMATION_MESSAGE);

                    dialog.dispose();
                    loadData();
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for distance, payload, fuel, and vibration.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

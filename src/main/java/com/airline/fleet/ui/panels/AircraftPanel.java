package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.model.Aircraft;
import com.airline.fleet.model.FleetCategory;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * AircraftPanel: Fleet Inventory Management Panel.
 * Implements full CRUD operations (Create, Read, Update, Delete) with real MySQL persistence.
 */
public class AircraftPanel extends JPanel {

    private final FleetDAO fleetDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryFilterCombo;
    private JTextField searchField;
    private JLabel statusLabel;

    public AircraftPanel() {
        this.fleetDAO = new FleetDAO();
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

        JLabel title = new JLabel("Aircraft Fleet Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        leftToolbar.add(title);

        leftToolbar.add(new JLabel("  Category:"));
        categoryFilterCombo = new JComboBox<>(new String[]{"ALL CATEGORIES", "LONG_HAUL_INTERNATIONAL", "SHORT_HAUL_REGIONAL", "CARGO_FREIGHT"});
        categoryFilterCombo.setFont(Theme.FONT_BODY);
        categoryFilterCombo.addActionListener(e -> filterData());
        leftToolbar.add(categoryFilterCombo);

        leftToolbar.add(new JLabel("  Search:"));
        searchField = new JTextField(10);
        searchField.setFont(Theme.FONT_BODY);
        searchField.addActionListener(e -> filterData());
        leftToolbar.add(searchField);

        JButton searchBtn = Theme.createOutlineButton("Filter");
        searchBtn.addActionListener(e -> filterData());
        leftToolbar.add(searchBtn);

        topPanel.add(leftToolbar, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("+ Add Aircraft");
        addBtn.addActionListener(e -> openAircraftDialog(null));
        actionPanel.add(addBtn);

        JButton editBtn = Theme.createOutlineButton("✎ Edit Selected");
        editBtn.addActionListener(e -> editSelectedAircraft());
        actionPanel.add(editBtn);

        JButton deleteBtn = Theme.createDangerButton("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteSelectedAircraft());
        actionPanel.add(deleteBtn);

        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tail Number", "Model", "Manufacturer", "Category", "Max Payload (kg)", "Seats", "Engine", "Hours", "Base SFC", "Status"};
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
        statusLabel = new JLabel("Ready.");
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
            List<Aircraft> list = fleetDAO.getAllAircraft();
            for (Aircraft a : list) {
                tableModel.addRow(new Object[]{
                    a.getAircraftId(),
                    a.getTailNumber(),
                    a.getModel(),
                    a.getManufacturer(),
                    a.getCategory().getDisplayName(),
                    String.format("%,.0f", a.getMaxPayloadKg()),
                    a.getSeatCapacity(),
                    a.getEngineType(),
                    String.format("%.1f", a.getTotalFlightHours()),
                    String.format("%.7f", a.getBaselineSfc()),
                    a.getStatus()
                });
            }
            statusLabel.setText("Loaded " + list.size() + " aircraft records from MySQL.");
        } catch (SQLException e) {
            statusLabel.setText("Failed to load aircraft: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterData() {
        String cat = (String) categoryFilterCombo.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        try {
            List<Aircraft> list = fleetDAO.getAllAircraft();
            int matched = 0;
            for (Aircraft a : list) {
                boolean catMatch = "ALL CATEGORIES".equals(cat) || a.getCategory().name().equals(cat);
                boolean queryMatch = query.isEmpty() ||
                    a.getTailNumber().toLowerCase().contains(query) ||
                    a.getModel().toLowerCase().contains(query) ||
                    a.getManufacturer().toLowerCase().contains(query);

                if (catMatch && queryMatch) {
                    tableModel.addRow(new Object[]{
                        a.getAircraftId(),
                        a.getTailNumber(),
                        a.getModel(),
                        a.getManufacturer(),
                        a.getCategory().getDisplayName(),
                        String.format("%,.0f", a.getMaxPayloadKg()),
                        a.getSeatCapacity(),
                        a.getEngineType(),
                        String.format("%.1f", a.getTotalFlightHours()),
                        String.format("%.7f", a.getBaselineSfc()),
                        a.getStatus()
                    });
                    matched++;
                }
            }
            statusLabel.setText("Showing " + matched + " matching aircraft.");
        } catch (SQLException e) {
            statusLabel.setText("Error filtering: " + e.getMessage());
        }
    }

    private void editSelectedAircraft() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an aircraft from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Aircraft a = fleetDAO.getAircraftById(id);
            if (a != null) {
                openAircraftDialog(a);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching record: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAircraft() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an aircraft to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String tail = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete aircraft " + tail + " (ID: " + id + ")?\nThis will cascade delete associated flights and telemetry.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (fleetDAO.deleteAircraft(id)) {
                    JOptionPane.showMessageDialog(this, "Aircraft " + tail + " deleted successfully.");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete aircraft.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAircraftDialog(Aircraft existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existing == null ? "Add Aircraft" : "Edit Aircraft", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(10, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 24, 15, 24));

        JTextField tailField = new JTextField(existing != null ? existing.getTailNumber() : "");
        JTextField modelField = new JTextField(existing != null ? existing.getModel() : "");
        JTextField mfgField = new JTextField(existing != null ? existing.getManufacturer() : "");
        JComboBox<FleetCategory> catCombo = new JComboBox<>(FleetCategory.values());
        if (existing != null) catCombo.setSelectedItem(existing.getCategory());

        JTextField payloadField = new JTextField(existing != null ? String.valueOf(existing.getMaxPayloadKg()) : "50000");
        JTextField seatsField = new JTextField(existing != null ? String.valueOf(existing.getSeatCapacity()) : "180");
        JTextField engineField = new JTextField(existing != null ? existing.getEngineType() : "Turbofan Engine");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "MAINTENANCE", "GROUNDED"});
        if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

        JTextField hoursField = new JTextField(existing != null ? String.valueOf(existing.getTotalFlightHours()) : "0.0");
        JTextField sfcField = new JTextField(existing != null ? String.valueOf(existing.getBaselineSfc()) : "0.0000420");

        form.add(new JLabel("Tail Number:")); form.add(tailField);
        form.add(new JLabel("Aircraft Model:")); form.add(modelField);
        form.add(new JLabel("Manufacturer:")); form.add(mfgField);
        form.add(new JLabel("Fleet Category:")); form.add(catCombo);
        form.add(new JLabel("Max Payload (kg):")); form.add(payloadField);
        form.add(new JLabel("Seat Capacity:")); form.add(seatsField);
        form.add(new JLabel("Engine Model:")); form.add(engineField);
        form.add(new JLabel("Operating Status:")); form.add(statusCombo);
        form.add(new JLabel("Total Flight Hours:")); form.add(hoursField);
        form.add(new JLabel("Baseline SFC:")); form.add(sfcField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton cancelBtn = Theme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = Theme.createPrimaryButton("Save Aircraft");
        saveBtn.addActionListener(e -> {
            try {
                String tail = tailField.getText().trim().toUpperCase();
                String model = modelField.getText().trim();
                String mfg = mfgField.getText().trim();
                if (tail.isEmpty() || model.isEmpty() || mfg.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tail Number, Model, and Manufacturer are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double payload = Double.parseDouble(payloadField.getText().trim());
                int seats = Integer.parseInt(seatsField.getText().trim());
                double hours = Double.parseDouble(hoursField.getText().trim());
                double sfc = Double.parseDouble(sfcField.getText().trim());

                Aircraft a = existing != null ? existing : new Aircraft();
                a.setTailNumber(tail);
                a.setModel(model);
                a.setManufacturer(mfg);
                a.setCategory((FleetCategory) catCombo.getSelectedItem());
                a.setMaxPayloadKg(payload);
                a.setSeatCapacity(seats);
                a.setEngineType(engineField.getText().trim());
                a.setStatus((String) statusCombo.getSelectedItem());
                a.setTotalFlightHours(hours);
                a.setBaselineSfc(sfc);

                boolean ok = existing == null ? fleetDAO.insertAircraft(a) : fleetDAO.updateAircraft(a);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Aircraft " + tail + " saved successfully.");
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save aircraft record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for payload, seats, hours, and SFC.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.model.MaintenanceFlag;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * MaintenancePanel: Automatic Maintenance Alert & Anomaly Resolution Center.
 * Tracks engine vibration alerts, SFC consumption outliers, and technician sign-offs.
 */
public class MaintenancePanel extends JPanel {

    private final FleetDAO fleetDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;
    private JLabel countBadge;

    public MaintenancePanel() {
        this.fleetDAO = new FleetDAO();
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadAlerts();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);

        JLabel title = new JLabel("Engine Vibration & Automatic Maintenance Alerts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        leftBlock.add(title);

        leftBlock.add(new JLabel("  Filter:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL ALERTS", "OPEN ONLY", "CRITICAL ONLY", "RESOLVED"});
        statusFilterCombo.setFont(Theme.FONT_BODY);
        statusFilterCombo.addActionListener(e -> loadAlerts());
        leftBlock.add(statusFilterCombo);

        countBadge = new JLabel("0 Active Alerts");
        countBadge.setFont(Theme.FONT_BOLD);
        countBadge.setForeground(Theme.CRITICAL_RED);
        leftBlock.add(countBadge);

        topPanel.add(leftBlock, BorderLayout.WEST);

        // Action Buttons
        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBlock.setOpaque(false);

        JButton resolveBtn = Theme.createPrimaryButton("✓ Resolve Selected Alert");
        resolveBtn.addActionListener(e -> resolveSelectedAlert());
        rightBlock.add(resolveBtn);

        JButton testAlertBtn = Theme.createDangerButton("⚡ Test Vibration Anomaly Alert");
        testAlertBtn.addActionListener(e -> triggerTestAlert());
        rightBlock.add(testAlertBtn);

        topPanel.add(rightBlock, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Flag ID", "Aircraft", "Flight No", "Severity", "Flag Type", "Vib (mils)", "Description", "Status", "Resolved By", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom severity column cell renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String sev = String.valueOf(value);
                if ("CRITICAL".equalsIgnoreCase(sev)) {
                    l.setForeground(Theme.CRITICAL_RED);
                    l.setFont(Theme.FONT_BOLD);
                } else if ("WARNING".equalsIgnoreCase(sev)) {
                    l.setForeground(Theme.WARNING_AMBER);
                    l.setFont(Theme.FONT_BOLD);
                } else {
                    l.setForeground(Theme.TEXT_MUTED);
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Legend
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JLabel legend = new JLabel("Engine Vibration Standards: Normal < 2.5 mils  |  Caution 2.5 - 4.0 mils (Warning)  |  Critical > 4.0 mils (Immediate Grounding/Inspection)");
        legend.setFont(Theme.FONT_SMALL);
        legend.setForeground(Theme.TEXT_MUTED);
        bottomBar.add(legend, BorderLayout.WEST);

        JButton refreshBtn = Theme.createOutlineButton("↻ Reload Alerts");
        refreshBtn.addActionListener(e -> loadAlerts());
        bottomBar.add(refreshBtn, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadAlerts() {
        tableModel.setRowCount(0);
        String filter = (String) statusFilterCombo.getSelectedItem();

        try {
            List<MaintenanceFlag> list = fleetDAO.getAllMaintenanceFlags();
            int openCount = 0;
            int critCount = 0;

            for (MaintenanceFlag m : list) {
                boolean isOpen = "OPEN".equalsIgnoreCase(m.getStatus());
                boolean isCrit = "CRITICAL".equalsIgnoreCase(m.getSeverity());
                if (isOpen) openCount++;
                if (isOpen && isCrit) critCount++;

                boolean include = false;
                if ("ALL ALERTS".equals(filter)) include = true;
                else if ("OPEN ONLY".equals(filter) && isOpen) include = true;
                else if ("CRITICAL ONLY".equals(filter) && isCrit && isOpen) include = true;
                else if ("RESOLVED".equals(filter) && "RESOLVED".equalsIgnoreCase(m.getStatus())) include = true;

                if (include) {
                    tableModel.addRow(new Object[]{
                        m.getFlagId(),
                        m.getAircraftTailNumber() != null ? m.getAircraftTailNumber() : "ID:" + m.getAircraftId(),
                        m.getFlightNumber() != null ? m.getFlightNumber() : "-",
                        m.getSeverity(),
                        m.getFlagType(),
                        m.getVibrationReadingMils() != null ? String.format("%.2f", m.getVibrationReadingMils()) : "-",
                        m.getDescription(),
                        m.getStatus(),
                        m.getResolvedBy() != null ? m.getResolvedBy() : "-",
                        m.getCreatedAt() != null ? m.getCreatedAt().toString() : "-"
                    });
                }
            }

            countBadge.setText(openCount + " Open Alerts (" + critCount + " Critical)");
            countBadge.setForeground(critCount > 0 ? Theme.CRITICAL_RED : Theme.TEXT_MUTED);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load maintenance alerts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resolveSelectedAlert() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an alert from the table to resolve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int flagId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 7);
        if ("RESOLVED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This alert has already been resolved.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resolve Maintenance Alert #" + flagId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setBorder(new EmptyBorder(15, 20, 15, 20));

        form.add(new JLabel("Certifying Engineer / Technician Name:"));
        JTextField engineerField = new JTextField("Chief Engineer Mark S.");
        form.add(engineerField);

        form.add(new JLabel("Inspection & Resolution Notes:"));
        JTextArea notesArea = new JTextArea("Borescope inspection completed. High pressure turbine fan blades balanced. Post-run vibration verified nominal at 1.25 mils.");
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        form.add(notesScroll);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton cancelBtn = Theme.createOutlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton submitBtn = Theme.createPrimaryButton("Mark as RESOLVED");
        submitBtn.addActionListener(e -> {
            String eng = engineerField.getText().trim();
            String notes = notesArea.getText().trim();
            if (eng.isEmpty() || notes.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Engineer name and resolution notes are mandatory for sign-off.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (fleetDAO.resolveMaintenanceFlag(flagId, eng, notes)) {
                    JOptionPane.showMessageDialog(dialog, "Alert #" + flagId + " successfully marked as RESOLVED.");
                    dialog.dispose();
                    loadAlerts();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database update error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(submitBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void triggerTestAlert() {
        try {
            double testVib = 4.45; // Above 4.0 critical limit
            MaintenanceFlag flag = new MaintenanceFlag(
                1,
                null,
                "CRITICAL",
                "VIBRATION_LIMIT_EXCEEDED",
                "TEST ANOMALY: Simulated high engine vibration of 4.45 mils detected during ground run. Critical safety threshold exceeded.",
                testVib
            );
            if (fleetDAO.insertMaintenanceFlag(flag)) {
                JOptionPane.showMessageDialog(this,
                    "Automated Critical Maintenance Alert successfully generated and committed to MySQL!\nEngine vibration: 4.45 mils (Critical limit: 4.0 mils).",
                    "Alert Triggered",
                    JOptionPane.WARNING_MESSAGE);
                loadAlerts();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to create test alert: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

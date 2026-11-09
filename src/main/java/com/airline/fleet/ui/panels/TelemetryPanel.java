package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.TelemetryWorker;
import com.airline.fleet.model.*;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TelemetryPanel: Real-time Multithreaded Engine Vibration & Telemetry Monitor.
 * Visualizes 3-phase sensor streams and demonstrates background thread processing
 * via ExecutorService.
 */
public class TelemetryPanel extends JPanel implements TelemetryWorker.TelemetryListener {

    private final FleetDAO fleetDAO;
    private final TelemetryWorker telemetryWorker;

    private JComboBox<Flight> flightSelector;
    private JTable telemetryTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JProgressBar eng1VibBar;
    private JProgressBar eng2VibBar;
    private JLabel eng1ValLabel;
    private JLabel eng2ValLabel;
    private JLabel statusBadge;
    private JButton simBtn;

    public TelemetryPanel(TelemetryWorker telemetryWorker) {
        this.fleetDAO = new FleetDAO();
        this.telemetryWorker = telemetryWorker;

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadFlights();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel leftBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBlock.setOpaque(false);

        JLabel title = new JLabel("Engine Vibration & Multithreaded Telemetry Stream");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        leftBlock.add(title);

        leftBlock.add(new JLabel("  Select Flight:"));
        flightSelector = new JComboBox<>();
        flightSelector.setFont(Theme.FONT_BODY);
        flightSelector.setPreferredSize(new Dimension(240, 30));
        flightSelector.addActionListener(e -> loadFlightTelemetry());
        leftBlock.add(flightSelector);

        topPanel.add(leftBlock, BorderLayout.WEST);

        // Simulation Button
        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBlock.setOpaque(false);

        simBtn = Theme.createPrimaryButton("▶ Run Live Multithreaded Simulation");
        simBtn.addActionListener(e -> runSimulation());
        rightBlock.add(simBtn);

        topPanel.add(rightBlock, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Left = Telemetry Table + Vibration Gauges, Right = Event Log
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);

        // LEFT CARD: Sensor Data & Gauges
        JPanel leftCard = Theme.createCardPanel();
        leftCard.setLayout(new BorderLayout(10, 10));

        // Vibration Gauges Header
        JPanel gaugePanel = new JPanel(new GridLayout(3, 1, 6, 6));
        gaugePanel.setOpaque(false);
        gaugePanel.setBorder(BorderFactory.createTitledBorder("Real-time Engine Vibration (Mils pk-pk)"));

        // Engine 1
        JPanel e1Panel = new JPanel(new BorderLayout(8, 0));
        e1Panel.setOpaque(false);
        e1Panel.add(new JLabel("Engine 1 N2:"), BorderLayout.WEST);
        eng1VibBar = new JProgressBar(0, 60); // 0.0 to 6.0 mils
        eng1VibBar.setValue(16);
        eng1VibBar.setForeground(Theme.SUCCESS_GREEN);
        e1Panel.add(eng1VibBar, BorderLayout.CENTER);
        eng1ValLabel = new JLabel("1.60 mils (Normal)");
        eng1ValLabel.setFont(Theme.FONT_BOLD);
        e1Panel.add(eng1ValLabel, BorderLayout.EAST);
        gaugePanel.add(e1Panel);

        // Engine 2
        JPanel e2Panel = new JPanel(new BorderLayout(8, 0));
        e2Panel.setOpaque(false);
        e2Panel.add(new JLabel("Engine 2 N2:"), BorderLayout.WEST);
        eng2VibBar = new JProgressBar(0, 60);
        eng2VibBar.setValue(15);
        eng2VibBar.setForeground(Theme.SUCCESS_GREEN);
        e2Panel.add(eng2VibBar, BorderLayout.CENTER);
        eng2ValLabel = new JLabel("1.50 mils (Normal)");
        eng2ValLabel.setFont(Theme.FONT_BOLD);
        e2Panel.add(eng2ValLabel, BorderLayout.EAST);
        gaugePanel.add(e2Panel);

        // Status Badge
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badgePanel.setOpaque(false);
        statusBadge = new JLabel("Status: VIBRATION NORMAL (< 2.5 mils)");
        statusBadge.setFont(Theme.FONT_BOLD);
        statusBadge.setForeground(Theme.SUCCESS_GREEN);
        badgePanel.add(statusBadge);
        gaugePanel.add(badgePanel);

        leftCard.add(gaugePanel, BorderLayout.NORTH);

        // Telemetry Data Table
        String[] cols = {"ID", "Phase", "Alt (ft)", "Speed (kts)", "Eng1 Vib", "Eng2 Vib", "Fuel (kg)", "EGT (°C)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        telemetryTable = new JTable(tableModel);
        Theme.styleTable(telemetryTable);

        JScrollPane tableScroll = new JScrollPane(telemetryTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        leftCard.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(leftCard);

        // RIGHT CARD: Background Thread Log
        JPanel rightCard = Theme.createCardPanel();
        rightCard.setLayout(new BorderLayout(8, 8));

        JLabel logTitle = new JLabel("ExecutorService Asynchronous Thread Log");
        logTitle.setFont(Theme.FONT_SUBTITLE);
        logTitle.setForeground(Theme.NAVY_DARK);
        rightCard.add(logTitle, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 23, 42)); // Dark console navy
        logArea.setForeground(new Color(147, 197, 253)); // Soft light blue console text
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(logArea);
        rightCard.add(logScroll, BorderLayout.CENTER);

        JButton clearLogBtn = Theme.createOutlineButton("Clear Console");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        rightCard.add(clearLogBtn, BorderLayout.SOUTH);

        centerPanel.add(rightCard);

        add(centerPanel, BorderLayout.CENTER);

        appendLog("Telemetry Engine initialized. Thread pool active with 4 worker threads.");
    }

    public void loadFlights() {
        flightSelector.removeAllItems();
        try {
            List<Flight> list = fleetDAO.getAllFlights();
            for (Flight f : list) {
                flightSelector.addItem(f);
            }
            if (!list.isEmpty()) {
                flightSelector.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            appendLog("Error loading flight list: " + e.getMessage());
        }
    }

    private void loadFlightTelemetry() {
        Flight selected = (Flight) flightSelector.getSelectedItem();
        if (selected == null) return;

        tableModel.setRowCount(0);
        try {
            List<TelemetryData> list = fleetDAO.getTelemetryByFlightId(selected.getFlightId());
            double maxEng1 = 0;
            double maxEng2 = 0;

            for (TelemetryData t : list) {
                tableModel.addRow(new Object[]{
                    t.getTelemetryId(),
                    t.getPhase().getDisplayName(),
                    String.format("%,.0f", t.getAltitudeFeet()),
                    String.format("%.0f", t.getSpeedKnots()),
                    String.format("%.2f mils", t.getEngine1VibrationMils()),
                    String.format("%.2f mils", t.getEngine2VibrationMils()),
                    String.format("%,.0f", t.getPhaseFuelBurnedKg()),
                    String.format("%.0f", t.getExhaustGasTempC())
                });
                if (t.getEngine1VibrationMils() > maxEng1) maxEng1 = t.getEngine1VibrationMils();
                if (t.getEngine2VibrationMils() > maxEng2) maxEng2 = t.getEngine2VibrationMils();
            }

            updateVibrationDisplay(maxEng1, maxEng2);
            appendLog(String.format("Loaded %d telemetry frames for %s (Eng1 Peak: %.2f mils, Eng2 Peak: %.2f mils)",
                list.size(), selected.getFlightNumber(), maxEng1, maxEng2));

        } catch (SQLException e) {
            appendLog("Error querying telemetry: " + e.getMessage());
        }
    }

    private void updateVibrationDisplay(double eng1, double eng2) {
        eng1VibBar.setValue((int) (eng1 * 10));
        eng2VibBar.setValue((int) (eng2 * 10));

        double peak = Math.max(eng1, eng2);
        if (peak >= 4.0) {
            statusBadge.setText(String.format("Status: ⚠ CRITICAL VIBRATION EXCEEDED (%.2f mils > 4.0 mils limit)", peak));
            statusBadge.setForeground(Theme.CRITICAL_RED);
            eng1VibBar.setForeground(Theme.CRITICAL_RED);
            eng2VibBar.setForeground(Theme.CRITICAL_RED);
        } else if (peak >= 2.5) {
            statusBadge.setText(String.format("Status: ⚠ CAUTION ADVISORY (%.2f mils in 2.5-4.0 range)", peak));
            statusBadge.setForeground(Theme.WARNING_AMBER);
            eng1VibBar.setForeground(Theme.WARNING_AMBER);
            eng2VibBar.setForeground(Theme.WARNING_AMBER);
        } else {
            statusBadge.setText(String.format("Status: NOMINAL (Peak: %.2f mils < 2.5 mils)", peak));
            statusBadge.setForeground(Theme.SUCCESS_GREEN);
            eng1VibBar.setForeground(Theme.SUCCESS_GREEN);
            eng2VibBar.setForeground(Theme.SUCCESS_GREEN);
        }

        eng1ValLabel.setText(String.format("%.2f mils", eng1));
        eng2ValLabel.setText(String.format("%.2f mils", eng2));
    }

    private void runSimulation() {
        Flight selected = (Flight) flightSelector.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        simBtn.setEnabled(false);
        appendLog(">>> Initiating asynchronous multithreaded telemetry simulation on thread pool...");

        telemetryWorker.startLiveSimulation(selected.getFlightId(), selected, t -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.addRow(new Object[]{
                    "SIM-" + t.getPhase().name().substring(0, 3),
                    t.getPhase().getDisplayName(),
                    String.format("%,.0f", t.getAltitudeFeet()),
                    String.format("%.0f", t.getSpeedKnots()),
                    String.format("%.2f mils", t.getEngine1VibrationMils()),
                    String.format("%.2f mils", t.getEngine2VibrationMils()),
                    String.format("%,.0f", t.getPhaseFuelBurnedKg()),
                    String.format("%.0f", t.getExhaustGasTempC())
                });
                updateVibrationDisplay(t.getEngine1VibrationMils(), t.getEngine2VibrationMils());
                appendLog(String.format("[%s] Thread: %s - Processed %s [Eng1: %.2f mils, Eng2: %.2f mils]",
                    new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()),
                    Thread.currentThread().getName(),
                    t.getPhase().getDisplayName(),
                    t.getEngine1VibrationMils(),
                    t.getEngine2VibrationMils()));
            });
        }, () -> {
            SwingUtilities.invokeLater(() -> {
                simBtn.setEnabled(true);
                appendLog(">>> Telemetry simulation complete. Automatic maintenance checks performed.");
                loadFlightTelemetry();
            });
        });
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onTelemetryProcessed(TelemetryData data, String statusMessage) {
        appendLog("[TelemetryWorker] " + statusMessage);
    }

    @Override
    public void onAlertTriggered(MaintenanceFlag alert) {
        appendLog(String.format("[ALERT TRIGGERED] Severity: %s | Anomaly: %s", alert.getSeverity(), alert.getDescription()));
    }

    @Override
    public void onError(String errorMessage) {
        appendLog("[ERROR] " + errorMessage);
    }
}

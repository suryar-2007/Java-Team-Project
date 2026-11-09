package com.airline.fleet.ui.panels;

import com.airline.fleet.FleetDAO;
import com.airline.fleet.ui.Theme;
import com.airline.fleet.ui.components.ChartPanel;
import com.airline.fleet.ui.components.KpiCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * OverviewPanel: Executive Fleet Analytics Dashboard.
 * Displays real-time KPI metrics, Category Fuel Consumption bar charts,
 * and Fleet Efficiency Grade (A, B, C, Outlier) doughnut charts.
 */
public class OverviewPanel extends JPanel {

    private final FleetDAO fleetDAO;
    private KpiCard kpiAircraft;
    private KpiCard kpiFlights;
    private KpiCard kpiSfc;
    private KpiCard kpiCarbon;
    private ChartPanel categoryFuelChart;
    private ChartPanel gradeDoughnutChart;
    private JLabel lastUpdatedLabel;

    public OverviewPanel() {
        this.fleetDAO = new FleetDAO();
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Fleet Operations & Fuel Analytics Overview");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(Theme.NAVY_DARK);
        titleBlock.add(pageTitle);

        lastUpdatedLabel = new JLabel("Real-time data synced with MySQL (airline_fleet_db)");
        lastUpdatedLabel.setFont(Theme.FONT_SMALL);
        lastUpdatedLabel.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(lastUpdatedLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JButton refreshBtn = Theme.createOutlineButton("↻ Refresh Metrics");
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Cards + Charts
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 15, 15));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 115));

        kpiAircraft = new KpiCard("Total Fleet Size", "0", "0 Active in Service", Theme.LIGHT_BLUE);
        kpiFlights = new KpiCard("Logged Flights", "0", "3 Flight Categories", new Color(99, 102, 241));
        kpiSfc = new KpiCard("Fleet Average SFC", "0.0000000", "kg fuel / (kg * km)", Theme.SUCCESS_GREEN);
        kpiCarbon = new KpiCard("Fleet Carbon Intensity", "0.0000", "kg CO2 / ASK (Factor: 3.16)", Theme.WARNING_AMBER);

        kpiRow.add(kpiAircraft);
        kpiRow.add(kpiFlights);
        kpiRow.add(kpiSfc);
        kpiRow.add(kpiCarbon);
        centerPanel.add(kpiRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 15, 15));
        chartsRow.setOpaque(false);

        categoryFuelChart = new ChartPanel("Fuel Consumption by Flight Category", ChartPanel.ChartType.BAR_CATEGORY_FUEL);
        gradeDoughnutChart = new ChartPanel("Fleet Efficiency Grade Distribution (A, B, C, Outlier)", ChartPanel.ChartType.DOUGHNUT_FLEET_GRADE);

        chartsRow.add(categoryFuelChart);
        chartsRow.add(gradeDoughnutChart);
        centerPanel.add(chartsRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 3. Engine Safety Banner
        ChartPanel safetyPanel = new ChartPanel("Engine Vibration Health & Threshold Standards", ChartPanel.ChartType.VIBRATION_STATUS_GAUGE);
        safetyPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 170));
        centerPanel.add(safetyPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            Map<String, Object> summary = fleetDAO.getFleetSummaryMetrics();
            int totalAc = (int) summary.getOrDefault("totalAircraft", 0);
            int activeAc = (int) summary.getOrDefault("activeAircraft", 0);
            int totalFlts = (int) summary.getOrDefault("totalFlights", 0);
            double avgSfc = (double) summary.getOrDefault("avgSfc", 0.0);
            double avgCo2 = (double) summary.getOrDefault("avgCarbonIntensity", 0.0);
            int criticalAlerts = (int) summary.getOrDefault("criticalAlerts", 0);

            kpiAircraft.setValue(String.valueOf(totalAc));
            kpiAircraft.setSubText(activeAc + " Active in Fleet");

            kpiFlights.setValue(String.valueOf(totalFlts));
            kpiFlights.setSubText(criticalAlerts > 0 ? "⚠ " + criticalAlerts + " Critical Alerts" : "All Nominal");

            kpiSfc.setValue(String.format("%.7f", avgSfc));
            kpiSfc.setSubText("kg_fuel / (kg * km)");

            kpiCarbon.setValue(String.format("%.4f", avgCo2));
            kpiCarbon.setSubText("kg CO2 / ASK");

            // Update Charts
            categoryFuelChart.updateData(fleetDAO.getCategoryFuelBurn());
            gradeDoughnutChart.updateData(fleetDAO.getFleetGradeDistribution());

            lastUpdatedLabel.setText("Real-time data synced with MySQL (" + new java.util.Date() + ")");

        } catch (SQLException e) {
            lastUpdatedLabel.setText("Database connection error: " + e.getMessage());
        }
    }
}

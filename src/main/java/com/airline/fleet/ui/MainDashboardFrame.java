package com.airline.fleet.ui;

import com.airline.fleet.DBConnection;
import com.airline.fleet.TelemetryWorker;
import com.airline.fleet.model.User;
import com.airline.fleet.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MainDashboardFrame: Central Host Window for the Airline Fleet Analytics Engine.
 * Features a collapsible Navy Blue sidebar, dynamic CardLayout, and active status bar.
 */
public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final TelemetryWorker telemetryWorker;

    private JPanel contentCards;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    // Sub-panels
    private OverviewPanel overviewPanel;
    private AircraftPanel aircraftPanel;
    private FlightPanel flightPanel;
    private TelemetryPanel telemetryPanel;
    private MaintenancePanel maintenancePanel;
    private AnalyticsPanel analyticsPanel;
    private DataExchangePanel dataExchangePanel;
    private SettingsPanel settingsPanel;

    private JLabel dbStatusLabel;

    public MainDashboardFrame(User user) {
        this.currentUser = user != null ? user : new User(1, "admin", "Fleet Administrator", "ADMIN");
        this.telemetryWorker = new TelemetryWorker();

        setTitle("Commercial Airline Fleet Fuel Efficiency & Maintenance Analytics Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_LIGHT);

        initUI();

        // Graceful shutdown hook for multithreaded worker
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                telemetryWorker.shutdown();
            }
        });
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setOpaque(false);

        // 1. TOP HEADER (Navy Bar)
        JPanel headerBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, Theme.NAVY_DARK, getWidth(), getHeight(), Theme.NAVY_ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerBar.setPreferredSize(new Dimension(0, 56));
        headerBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        brandPanel.setOpaque(false);
        JLabel brandLogo = new JLabel("✈ AIRLINE FLEET ANALYTICS");
        brandLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandLogo.setForeground(Theme.WHITE);
        brandPanel.add(brandLogo);

        JLabel tagLine = new JLabel("|  Fuel Efficiency & Maintenance Engine");
        tagLine.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagLine.setForeground(Theme.LIGHT_BLUE_CYAN);
        brandPanel.add(tagLine);
        headerBar.add(brandPanel, BorderLayout.WEST);

        // User Profile & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        userPanel.setOpaque(false);

        JLabel userBadge = new JLabel("👤 " + currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        userBadge.setFont(Theme.FONT_BOLD);
        userBadge.setForeground(Theme.WHITE);
        userPanel.add(userBadge);

        JButton logoutBtn = Theme.createOutlineButton("Log Out");
        logoutBtn.addActionListener(e -> performLogout());
        userPanel.add(logoutBtn);

        headerBar.add(userPanel, BorderLayout.EAST);
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. LEFT SIDEBAR (Navy Nav)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.NAVY_ACCENT);

        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setBackground(Theme.NAVY_ACCENT);
        navList.setBorder(new EmptyBorder(15, 10, 15, 10));

        addNavItem(navList, "Overview", "📊 Fleet Overview");
        addNavItem(navList, "Aircraft", "✈ Aircraft Fleet (CRUD)");
        addNavItem(navList, "Flights", "🛫 Flight Operations (CRUD)");
        addNavItem(navList, "Telemetry", "⚡ Telemetry & Vibration");
        addNavItem(navList, "Maintenance", "🛠 Maintenance Alerts");
        addNavItem(navList, "Analytics", "📈 Fuel & Carbon Analytics");
        addNavItem(navList, "DataExchange", "📁 CSV Import / TXT Export");
        addNavItem(navList, "Settings", "⚙ MySQL Configuration");

        sidebar.add(navList, BorderLayout.NORTH);

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new GridLayout(2, 1, 0, 4));
        sidebarFooter.setBackground(Theme.NAVY_DARK);
        sidebarFooter.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel verLbl = new JLabel("Engine Version 1.0.0");
        verLbl.setFont(Theme.FONT_SMALL);
        verLbl.setForeground(Theme.TEXT_MUTED);
        sidebarFooter.add(verLbl);

        JLabel stdLbl = new JLabel("ICAO Fuel Baseline 3.16");
        stdLbl.setFont(Theme.FONT_SMALL);
        stdLbl.setForeground(Theme.LIGHT_BLUE_CYAN);
        sidebarFooter.add(stdLbl);

        sidebar.add(sidebarFooter, BorderLayout.SOUTH);
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 3. CENTER CONTENT (CardLayout)
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        overviewPanel = new OverviewPanel();
        aircraftPanel = new AircraftPanel();
        flightPanel = new FlightPanel(telemetryWorker);
        telemetryPanel = new TelemetryPanel(telemetryWorker);
        maintenancePanel = new MaintenancePanel();
        analyticsPanel = new AnalyticsPanel();
        dataExchangePanel = new DataExchangePanel(telemetryWorker, currentUser);
        settingsPanel = new SettingsPanel();

        contentCards.add(overviewPanel, "Overview");
        contentCards.add(aircraftPanel, "Aircraft");
        contentCards.add(flightPanel, "Flights");
        contentCards.add(telemetryPanel, "Telemetry");
        contentCards.add(maintenancePanel, "Maintenance");
        contentCards.add(analyticsPanel, "Analytics");
        contentCards.add(dataExchangePanel, "DataExchange");
        contentCards.add(settingsPanel, "Settings");

        rootPanel.add(contentCards, BorderLayout.CENTER);

        // 4. BOTTOM STATUS BAR
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setPreferredSize(new Dimension(0, 28));
        statusBar.setBackground(Theme.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
            new EmptyBorder(4, 16, 4, 16)
        ));

        dbStatusLabel = new JLabel("Database: Connected to MySQL (airline_fleet_db)");
        dbStatusLabel.setFont(Theme.FONT_SMALL);
        dbStatusLabel.setForeground(DBConnection.isConnected() ? Theme.SUCCESS_GREEN : Theme.CRITICAL_RED);
        statusBar.add(dbStatusLabel, BorderLayout.WEST);

        JLabel workerStatus = new JLabel("Multithreaded Telemetry Engine: Active (4 Threads) | Windows Local Runtime");
        workerStatus.setFont(Theme.FONT_SMALL);
        workerStatus.setForeground(Theme.TEXT_MUTED);
        statusBar.add(workerStatus, BorderLayout.EAST);

        rootPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);

        // Highlight initial selection
        selectNav("Overview");
    }

    private void addNavItem(JPanel container, String cardKey, String label) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFont(Theme.FONT_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(Theme.NAVY_ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addActionListener(e -> selectNav(cardKey));
        navButtons.put(cardKey, btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void selectNav(String cardKey) {
        cardLayout.show(contentCards, cardKey);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(cardKey)) {
                entry.getValue().setBackground(Theme.LIGHT_BLUE);
                entry.getValue().setForeground(Theme.WHITE);
            } else {
                entry.getValue().setBackground(Theme.NAVY_ACCENT);
                entry.getValue().setForeground(new Color(203, 213, 225));
            }
        }

        // Refresh dynamic data upon opening specific panels
        if ("Overview".equals(cardKey)) overviewPanel.refreshData();
        else if ("Aircraft".equals(cardKey)) aircraftPanel.loadData();
        else if ("Flights".equals(cardKey)) flightPanel.loadData();
        else if ("Telemetry".equals(cardKey)) telemetryPanel.loadFlights();
        else if ("Maintenance".equals(cardKey)) maintenancePanel.loadAlerts();

        // Update DB status bar
        dbStatusLabel.setText(DBConnection.isConnected() ?
            "Database: Connected to MySQL (airline_fleet_db)" :
            "Database: OFFLINE - Configure connection in MySQL Settings tab");
        dbStatusLabel.setForeground(DBConnection.isConnected() ? Theme.SUCCESS_GREEN : Theme.CRITICAL_RED);
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of the Fleet Analytics Console?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            telemetryWorker.shutdown();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }
}

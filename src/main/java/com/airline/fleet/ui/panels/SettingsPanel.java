package com.airline.fleet.ui.panels;

import com.airline.fleet.DBConnection;
import com.airline.fleet.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * SettingsPanel: MySQL Database Configuration & Schema Initializer.
 * Enables live testing of credentials and 1-click execution of fleet_schema.sql.
 */
public class SettingsPanel extends JPanel {

    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextField userField;
    private JPasswordField passField;
    private JTextArea statusArea;

    public SettingsPanel() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Database Configuration & MySQL Environment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.NAVY_DARK);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        centerPanel.setOpaque(false);

        // LEFT CARD: MySQL Settings Form
        JPanel configCard = Theme.createCardPanel();
        configCard.setLayout(new BorderLayout(10, 10));

        JLabel formTitle = new JLabel("MySQL Connection Parameters");
        formTitle.setFont(Theme.FONT_SUBTITLE);
        formTitle.setForeground(Theme.NAVY_DARK);
        configCard.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setOpaque(false);

        hostField = new JTextField("localhost");
        portField = new JTextField("3306");
        dbField = new JTextField("airline_fleet_db");
        userField = new JTextField(DBConnection.getDbUser() != null ? DBConnection.getDbUser() : "root");
        passField = new JPasswordField(DBConnection.getDbPassword() != null ? DBConnection.getDbPassword() : "root");

        form.add(new JLabel("MySQL Host:")); form.add(hostField);
        form.add(new JLabel("Port Number:")); form.add(portField);
        form.add(new JLabel("Database Name:")); form.add(dbField);
        form.add(new JLabel("Username:")); form.add(userField);
        form.add(new JLabel("Password:")); form.add(passField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);

        JButton testBtn = Theme.createOutlineButton("⚡ Test Connection");
        testBtn.addActionListener(e -> testConnection());
        btnRow.add(testBtn);

        JButton saveBtn = Theme.createPrimaryButton("Save & Reconnect");
        saveBtn.addActionListener(e -> saveAndReconnect());
        btnRow.add(saveBtn);

        form.add(new JLabel("")); form.add(btnRow);
        configCard.add(form, BorderLayout.CENTER);

        // Schema Bootstrap Button
        JPanel schemaSection = new JPanel(new BorderLayout(8, 8));
        schemaSection.setOpaque(false);
        schemaSection.setBorder(BorderFactory.createTitledBorder("Database Schema Bootstrap"));

        JButton runSchemaBtn = Theme.createPrimaryButton("▶ Execute database/fleet_schema.sql");
        runSchemaBtn.addActionListener(e -> runSchemaFile());
        schemaSection.add(runSchemaBtn, BorderLayout.CENTER);

        JLabel schemaNotice = new JLabel("Creates airline_fleet_db, tables (aircraft, flights, telemetry, flags) & seeds sample data.");
        schemaNotice.setFont(Theme.FONT_SMALL);
        schemaNotice.setForeground(Theme.TEXT_MUTED);
        schemaSection.add(schemaNotice, BorderLayout.SOUTH);

        configCard.add(schemaSection, BorderLayout.SOUTH);
        centerPanel.add(configCard);

        // RIGHT CARD: Status Log
        JPanel rightCard = Theme.createCardPanel();
        rightCard.setLayout(new BorderLayout(8, 8));

        JLabel logTitle = new JLabel("Connectivity Diagnostics & Setup Instructions");
        logTitle.setFont(Theme.FONT_SUBTITLE);
        logTitle.setForeground(Theme.NAVY_DARK);
        rightCard.add(logTitle, BorderLayout.NORTH);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusArea.setBackground(new Color(15, 23, 42));
        statusArea.setForeground(new Color(147, 197, 253));
        statusArea.setText(
            "LOCAL MYSQL SETUP INSTRUCTIONS:\n\n" +
            "1. Ensure MySQL Server 8.0 is running on Windows (Service name: MySQL80).\n" +
            "2. If your root password is not 'root', update the password field above and click 'Save & Reconnect'.\n" +
            "3. Click 'Execute database/fleet_schema.sql' to automatically create the schema and populate seed data.\n" +
            "4. Alternatively, you can open MySQL Command Line Client / Workbench and run:\n" +
            "   mysql -u root -p < database/fleet_schema.sql\n"
        );
        statusArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        rightCard.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        centerPanel.add(rightCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void testConnection() {
        statusArea.append("\n>>> Testing MySQL connection...\n");
        String diag = DBConnection.testConnectionDiagnostic();
        statusArea.append("Result: " + diag + "\n");
        if (diag.startsWith("SUCCESS")) {
            JOptionPane.showMessageDialog(this, diag, "Connection Succeeded", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, diag, "Connection Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveAndReconnect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String db = dbField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            DBConnection.updateCredentials(host, port, db, user, pass);
            statusArea.append("\n>>> Updated database credentials and saved to src/main/resources/db.properties\n");

            testConnection();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port must be a valid integer (e.g. 3306).", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runSchemaFile() {
        File schemaFile = new File("database/fleet_schema.sql");
        statusArea.append("\n>>> Executing " + schemaFile.getAbsolutePath() + "...\n");
        boolean ok = DBConnection.initializeDatabaseSchema(schemaFile);
        if (ok) {
            statusArea.append("SUCCESS: Database schema and seed data created in MySQL!\n");
            JOptionPane.showMessageDialog(this, "Database schema and seed records initialized successfully!", "Schema Initialized", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusArea.append("FAILED: Could not execute schema. Ensure MySQL is running and password is correct.\n");
            JOptionPane.showMessageDialog(this, "Failed to execute schema. Check password and MySQL status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

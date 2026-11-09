package com.airline.fleet;

import com.airline.fleet.ui.LoginFrame;

import javax.swing.*;
import java.io.File;

/**
 * MainApp: Application Entry Point.
 * Commercial Airline Fleet Fuel Efficiency & Maintenance Analytics Engine.
 */
public class MainApp {

    public static void main(String[] args) {
        // Enable high-DPI scaling and font anti-aliasing for modern Windows presentation
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Use native Windows look and feel if available
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Graceful fallback to default cross-platform look and feel
        }

        // Initialize and test database connectivity
        System.out.println("==================================================================");
        System.out.println("  Commercial Airline Fleet Fuel Efficiency & Maintenance Engine   ");
        System.out.println("==================================================================");
        System.out.println("[MainApp] Loading database configuration...");
        DBConnection.loadProperties();

        String diag = DBConnection.testConnectionDiagnostic();
        System.out.println("[MainApp] MySQL Connection Diagnostic: " + diag);

        if (DBConnection.isConnected()) {
            System.out.println("[MainApp] MySQL connected! Verifying database schema...");
            File schemaFile = new File("database/fleet_schema.sql");
            if (schemaFile.exists()) {
                // Auto bootstrap schema if needed
                DBConnection.initializeDatabaseSchema(schemaFile);
            }
        } else {
            System.err.println("[MainApp] Notice: MySQL not immediately reachable with default password.");
            System.err.println("[MainApp] You can configure database credentials in src/main/resources/db.properties or inside the GUI Settings tab.");
        }

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

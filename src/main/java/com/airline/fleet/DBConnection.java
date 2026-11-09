package com.airline.fleet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBConnection: Centralized JDBC Connection Manager for airline_fleet_db.
 * Handles MySQL connection initialization, properties loading, connectivity testing,
 * and automatic schema bootstrapping using fleet_schema.sql.
 */
public class DBConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DB = "airline_fleet_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    /**
     * Loads database credentials from db.properties or falls back to standard defaults.
     */
    public static synchronized void loadProperties() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try loading from file in working directory / resources
        File localFile = new File("src/main/resources/db.properties");
        if (localFile.exists()) {
            try (InputStream in = new FileInputStream(localFile)) {
                props.load(in);
                loaded = true;
            } catch (Exception ignored) {}
        }

        // 2. Try loading from classpath
        if (!loaded) {
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {}
        }

        if (loaded) {
            dbUrl = props.getProperty("db.url", "jdbc:mysql://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            dbUser = props.getProperty("db.user", DEFAULT_USER);
            dbPassword = props.getProperty("db.password", DEFAULT_PASS);
        } else {
            dbUrl = "jdbc:mysql://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            dbUser = DEFAULT_USER;
            dbPassword = DEFAULT_PASS;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] Warning: MySQL JDBC Driver class not found in classpath. Ensure mysql-connector-j is included.");
        }
    }

    /**
     * Updates and persists database credentials dynamically at runtime.
     */
    public static synchronized void updateCredentials(String host, int port, String database, String user, String password) {
        dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
        dbUser = user;
        dbPassword = password;

        // Persist to src/main/resources/db.properties if directory exists
        try {
            File propFile = new File("src/main/resources/db.properties");
            if (propFile.getParentFile() != null && propFile.getParentFile().exists()) {
                Properties props = new Properties();
                props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                props.setProperty("db.url", dbUrl);
                props.setProperty("db.user", dbUser);
                props.setProperty("db.password", dbPassword);
                try (FileOutputStream out = new FileOutputStream(propFile)) {
                    props.store(out, "Updated via Airline Fleet Management GUI");
                }
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Could not persist properties file: " + e.getMessage());
        }
    }

    /**
     * Obtains a live JDBC Connection to the MySQL database.
     *
     * @return java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests connectivity to the database and returns a diagnostic message.
     *
     * @return Diagnostic result string
     */
    public static String testConnectionDiagnostic() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                return "SUCCESS: Connected to MySQL database (" + conn.getCatalog() + ") successfully!";
            }
            return "FAILED: Connection opened but returned null/closed.";
        } catch (SQLException e) {
            return "ERROR (" + e.getErrorCode() + "): " + e.getMessage();
        }
    }

    /**
     * Checks if the database is currently reachable.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Automatically initializes the database schema from database/fleet_schema.sql.
     *
     * @return true if successful
     */
    public static boolean initializeDatabaseSchema(File schemaFile) {
        if (!schemaFile.exists()) {
            System.err.println("[DBConnection] Schema file does not exist: " + schemaFile.getAbsolutePath());
            return false;
        }

        try {
            String script = Files.readString(schemaFile.toPath());
            // Connect to server root first to create database if not exists
            String serverUrl = dbUrl.substring(0, dbUrl.indexOf("3306/") + 5) + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            
            try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement()) {
                
                // Split statements by semicolon
                String[] queries = script.split(";");
                for (String q : queries) {
                    String trimmed = q.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException ex) {
                            // Ignore minor non-critical warnings
                            System.err.println("[DBConnection] Script statement notice: " + ex.getMessage());
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Failed to initialize schema: " + e.getMessage());
            return false;
        }
    }

    public static String getDbUrl() { return dbUrl; }
    public static String getDbUser() { return dbUser; }
    public static String getDbPassword() { return dbPassword; }
}

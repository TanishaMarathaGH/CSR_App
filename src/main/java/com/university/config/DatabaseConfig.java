package com.university.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import javafx.application.Platform;

public class DatabaseConfig {
    // Default XAMPP credentials
    private static final String URL = "jdbc:mysql://localhost:3306/campus_service_request?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // XAMPP typically has no password for root

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null || isConnectionClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                showDatabaseError("MySQL JDBC Driver not found. Please ensure MySQL Connector/J is in your classpath.");
                return null;
            } catch (SQLException e) {
                showDatabaseError(e.getMessage());
                return null;
            }
        }
        return connection;
    }

    public static boolean testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            showDatabaseError("MySQL JDBC Driver not found. Please ensure MySQL Connector/J is in your classpath.");
            return false;
        } catch (SQLException e) {
            showDatabaseError(e.getMessage());
            return false;
        }
    }

    private static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed() || !connection.isValid(2);
        } catch (SQLException e) {
            return true;
        }
    }

    private static void showDatabaseError(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Failed to connect to database");
            alert.setContentText("Please ensure that:\n" +
                    "1. XAMPP/MySQL server is running on port 3306\n" +
                    "2. Database 'campus_service_request' exists\n" +
                    "3. Using correct credentials (default: root with no password)\n" +
                    "4. No other application is using port 3306\n\n" +
                    "Error Details:\n" + errorMessage + "\n\n" +
                    "Try these steps:\n" +
                    "1. Start/Restart XAMPP\n" +
                    "2. Check MySQL service status\n" +
                    "3. Verify database exists\n" +
                    "4. Check MySQL error log");
            alert.showAndWait();
        });
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    public static void reconnect() {
        closeConnection();
        getConnection();
    }
} 
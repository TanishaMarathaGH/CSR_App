package com.university.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.scene.control.Alert;

public class DatabaseConfig {
    // Default XAMPP credentials
    private static final String URL = "jdbc:mysql://localhost:3306/campus_service_request";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // XAMPP typically has no password for root

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
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
        } catch (ClassNotFoundException | SQLException e) {
            showDatabaseError(e.getMessage());
            return false;
        }
    }

    private static void showDatabaseError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Failed to connect to database");
        alert.setContentText("Please ensure that:\n" +
                "1. XAMPP/MySQL server is running\n" +
                "2. Database 'campus_service_request' exists\n" +
                "3. Using correct credentials (default: root with no password)\n\n" +
                "Error: " + errorMessage);
        alert.showAndWait();
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 
package com.university.controller;

import com.university.dao.UserDAO;
import com.university.model.User;
import com.university.config.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "ADMIN"));
        roleComboBox.setValue("STUDENT");

        try {
            if (DatabaseConfig.testConnection()) {
                userDAO = new UserDAO();
            } else {
                errorLabel.setText("Database connection failed. Please check your database settings.");
            }
        } catch (Exception e) {
            errorLabel.setText("Error connecting to database: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        clearError();
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            showError("Please fill in all fields");
            return;
        }

        if (userDAO == null) {
            showError("Database connection is not available");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                if (user.getRole().toString().equals(selectedRole)) {
                    loadDashboard(user);
                } else {
                    showError("Invalid role selected for this user");
                }
            } else {
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            showError("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Error loading registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboard(User user) {
        try {
            String fxmlFile = user.getRole() == User.UserRole.ADMIN ? 
                "/fxml/AdminDashboard.fxml" : "/fxml/StudentDashboard.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Initialize the controller
            if (user.getRole() == User.UserRole.ADMIN) {
                AdminDashboardController controller = loader.getController();
                controller.initData(user);
            } else {
                StudentDashboardController controller = loader.getController();
                controller.initData(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearError() {
        errorLabel.setText("");
    }
} 
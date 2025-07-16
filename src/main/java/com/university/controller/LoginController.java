package com.university.controller;

import com.university.dao.UserDAO;
import com.university.model.User;
import com.university.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserDAO userDAO;

    public LoginController() {
        if (DatabaseConfig.testConnection()) {
            userDAO = new UserDAO();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        if (userDAO == null) {
            errorLabel.setText("Database connection is not available");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                loadDashboard(user);
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            errorLabel.setText("Error during login: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Error loading registration page");
            e.printStackTrace();
        }
    }

    private void loadDashboard(User user) {
        try {
            String fxmlFile = user.getRole() == User.UserRole.ADMIN ? "/fxml/AdminDashboard.fxml" : "/fxml/StudentDashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Error loading dashboard");
            e.printStackTrace();
        }
    }
} 
package com.university.controller;

import com.university.dao.UserDAO;
import com.university.model.User;
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

public class RegisterController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField departmentField;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "ADMIN"));
    }

    @FXML
    private void handleRegister() {
        // Clear previous error
        errorLabel.setText("");

        // Validate input
        if (!validateInput()) {
            return;
        }

        try {
            // Create new user
            User user = new User(
                0, // ID will be set by database
                usernameField.getText().trim(),
                emailField.getText().trim(),
                fullNameField.getText().trim(),
                User.UserRole.valueOf(roleComboBox.getValue()),
                departmentField.getText().trim()
            );
            user.setPassword(passwordField.getText().trim());

            // Save user to database
            userDAO.createUser(user);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Your account has been created successfully!");
            alert.showAndWait();

            // Return to login screen
            handleBackToLogin();

        } catch (Exception e) {
            errorLabel.setText("Error creating account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            errorLabel.setText("Error returning to login page");
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            errorLabel.setText("Username is required");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            errorLabel.setText("Email is required");
            return false;
        }

        if (fullNameField.getText().trim().isEmpty()) {
            errorLabel.setText("Full name is required");
            return false;
        }

        if (passwordField.getText().trim().isEmpty()) {
            errorLabel.setText("Password is required");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match");
            return false;
        }

        if (roleComboBox.getValue() == null) {
            errorLabel.setText("Please select a role");
            return false;
        }

        if (departmentField.getText().trim().isEmpty()) {
            errorLabel.setText("Department is required");
            return false;
        }

        return true;
    }
} 
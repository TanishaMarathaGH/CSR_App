package com.university.controller;

import com.university.dao.ComplaintDAO;
import com.university.model.Complaint;
import com.university.model.User;
import com.university.util.ImageHandler;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class StudentDashboardController {
    @FXML private TableView<Complaint> myComplaintsTable;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private TextField subjectField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView imagePreview;
    @FXML private Label imageLabel;
    @FXML private Button submitButton;
    @FXML private Button logoutButton;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;
    @FXML private TextField ticketNumberField;
    @FXML private ComboBox<Complaint.Priority> priorityComboBox;

    private User currentUser;
    private ComplaintDAO complaintDAO = new ComplaintDAO();
    private String uploadedImagePath;
    private FilteredList<Complaint> filteredComplaints;

    @FXML
    public void initialize() {
        // Initialize category dropdown
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Computer Lab", "Library", "Internet", "Electrical", "Plumbing", "Classroom", "Other"
        ));

        // Initialize priority dropdown
        priorityComboBox.setItems(FXCollections.observableArrayList(
            Complaint.Priority.values()
        ));

        // Initialize status filter
        filterStatusComboBox.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"
        ));
        filterStatusComboBox.setValue("All");
        filterStatusComboBox.setOnAction(e -> applyFilters());

        // Style the components
        submitButton.getStyleClass().add("primary-button");
        logoutButton.getStyleClass().add("secondary-button");
    }

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName());
        initializeUI();
        loadMyComplaints();
    }

    private void initializeUI() {
        // Initialize table columns
        TableColumn<Complaint, String> idCol = new TableColumn<>("Ticket #");
        idCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getComplaintId())));

        TableColumn<Complaint, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Complaint, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject()));

        TableColumn<Complaint, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPriority().toString()));

        TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        TableColumn<Complaint, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<Complaint, String>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setOnAction(e -> handleViewComplaint(getTableView().getItems().get(getIndex())));
                viewButton.getStyleClass().add("action-button");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        myComplaintsTable.getColumns().addAll(idCol, categoryCol, subjectCol, priorityCol, statusCol, actionCol);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Error during logout: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageUpload() {
        try {
            String imagePath = ImageHandler.uploadImage((Stage) submitButton.getScene().getWindow());
            if (imagePath != null) {
                uploadedImagePath = imagePath;
                File imageFile = new File(imagePath);
                Image image = new Image(imageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(200);
                imagePreview.setFitHeight(200);
                imagePreview.setPreserveRatio(true);
                imageLabel.setText("Image uploaded successfully");
            }
        } catch (Exception e) {
            showError("Error uploading image: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmitComplaint() {
        if (!validateForm()) {
            return;
        }

        try {
            Complaint complaint = new Complaint(
                currentUser.getUserId(),
                categoryComboBox.getValue(),
                subjectField.getText().trim(),
                descriptionArea.getText().trim()
            );
            complaint.setImagePath(uploadedImagePath);
            complaint.setPriority(priorityComboBox.getValue());
            complaint.setTicketNumber(generateTicketNumber());
            
            complaintDAO.createComplaint(complaint);
            
            // Clear form
            clearForm();
            
            // Show success message and reload complaints
            showSuccess("Complaint submitted successfully! Ticket #" + complaint.getTicketNumber());
            loadMyComplaints();
            
        } catch (SQLException e) {
            showError("Error submitting complaint: " + e.getMessage());
        }
    }

    private void clearForm() {
        categoryComboBox.setValue(null);
        subjectField.clear();
        descriptionArea.clear();
        imagePreview.setImage(null);
        uploadedImagePath = null;
        imageLabel.setText("No image uploaded");
        priorityComboBox.setValue(null);
    }

    private String generateTicketNumber() {
        return String.format("TKT-%d-%d", currentUser.getUserId(), System.currentTimeMillis() % 10000);
    }

    private boolean validateForm() {
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            showError("Please select a category");
            return false;
        }
        
        if (subjectField.getText().trim().isEmpty()) {
            showError("Please enter a subject");
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showError("Please enter a description");
            return false;
        }

        if (priorityComboBox.getValue() == null) {
            showError("Please select a priority level");
            return false;
        }
        
        return true;
    }

    private void loadMyComplaints() {
        try {
            List<Complaint> complaints = complaintDAO.getComplaintsByUserId(currentUser.getUserId());
            filteredComplaints = new FilteredList<>(FXCollections.observableArrayList(complaints));
            myComplaintsTable.setItems(filteredComplaints);
            applyFilters();
        } catch (SQLException e) {
            showError("Error loading complaints: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String statusFilter = filterStatusComboBox.getValue();
        filteredComplaints.setPredicate(complaint -> {
            if ("All".equals(statusFilter)) {
                return true;
            }
            return complaint.getStatus().toString().equals(statusFilter);
        });
    }

    private void handleViewComplaint(Complaint complaint) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Complaint Details");
        dialog.setHeaderText("Ticket #" + complaint.getTicketNumber());
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Category: " + complaint.getCategory()),
            new Label("Subject: " + complaint.getSubject()),
            new Label("Description: " + complaint.getDescription()),
            new Label("Priority: " + complaint.getPriority()),
            new Label("Status: " + complaint.getStatus())
        );
        
        if (complaint.getImagePath() != null) {
            try {
                Image image = new Image(new File(complaint.getImagePath()).toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                content.getChildren().add(new Label("Attached Image:"));
                content.getChildren().add(imageView);
            } catch (Exception e) {
                content.getChildren().add(new Label("Error loading image: " + e.getMessage()));
            }
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }
} 
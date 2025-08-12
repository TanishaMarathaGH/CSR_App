package com.university.controller;

import com.university.dao.ComplaintDAO;
import com.university.dao.UserDAO;
import com.university.model.Complaint;
import com.university.model.User;
import com.university.util.EmailSender;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private BarChart<String, Number> complaintsByDepartmentChart;
    @FXML private PieChart statusDistributionChart;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<Complaint.Priority> priorityFilter;
    @FXML private TextField searchField;
    @FXML private Label totalComplaintsLabel;
    @FXML private VBox chartContainer;
    @FXML private Button logoutButton;
    @FXML private Label welcomeLabel;

    private User currentUser;
    private ComplaintDAO complaintDAO;
    private UserDAO userDAO;
    private FilteredList<Complaint> filteredComplaints;

    public void initData(User user) {
        this.currentUser = user;
        this.complaintDAO = new ComplaintDAO();
        this.userDAO = new UserDAO();
        welcomeLabel.setText("Welcome, " + user.getFullName());
        initializeUI();
        loadComplaints();
        updateCharts();
    }

    private void initializeUI() {
        // Initialize filters
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"
        ));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilters());

        categoryFilter.setItems(FXCollections.observableArrayList(
            "All", "Computer Lab", "Library", "Internet", "Electrical", "Plumbing", "Classroom", "Other"
        ));
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> applyFilters());

        priorityFilter.setItems(FXCollections.observableArrayList(
            Complaint.Priority.values()
        ));
        priorityFilter.setPromptText("All Priorities");
        priorityFilter.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Initialize table columns
        TableColumn<Complaint, String> ticketCol = new TableColumn<>("Ticket #");
        ticketCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTicketNumber()));

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

        complaintsTable.getColumns().addAll(ticketCol, categoryCol, subjectCol, priorityCol, statusCol, actionCol);

        // Style components
        logoutButton.getStyleClass().add("secondary-button");
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

    private void loadComplaints() {
        try {
            List<Complaint> complaints = complaintDAO.getAllComplaints();
            filteredComplaints = new FilteredList<>(FXCollections.observableArrayList(complaints));
            complaintsTable.setItems(filteredComplaints);
            applyFilters();
            totalComplaintsLabel.setText("Total Complaints: " + complaints.size());
        } catch (SQLException e) {
            showError("Error loading complaints: " + e.getMessage());
        }
    }

    private void applyFilters() {
        try {
            String statusValue = statusFilter.getValue();
            String categoryValue = categoryFilter.getValue();
            Complaint.Priority priorityValue = priorityFilter.getValue();
            String searchValue = searchField.getText().toLowerCase();

            filteredComplaints.setPredicate(complaint -> {
                boolean matchesStatus = "All".equals(statusValue) || 
                                      complaint.getStatus().toString().equals(statusValue);
                
                boolean matchesCategory = "All".equals(categoryValue) || 
                                        complaint.getCategory().equals(categoryValue);
                
                boolean matchesPriority = priorityValue == null || 
                                        complaint.getPriority() == priorityValue;
                
                boolean matchesSearch = searchValue.isEmpty() || 
                                      complaint.getTicketNumber().toLowerCase().contains(searchValue) ||
                                      complaint.getSubject().toLowerCase().contains(searchValue) ||
                                      complaint.getDescription().toLowerCase().contains(searchValue);
                
                return matchesStatus && matchesCategory && matchesPriority && matchesSearch;
            });

            int totalComplaints = complaintDAO.getAllComplaints().size();
            totalComplaintsLabel.setText(String.format("Showing %d of %d complaints", 
                filteredComplaints.size(), totalComplaints));
        } catch (SQLException e) {
            showError("Error applying filters: " + e.getMessage());
        }
    }

    private void updateCharts() {
        try {
            List<Complaint> allComplaints = complaintDAO.getAllComplaints();
            
            // Update department chart
            Map<String, Long> departmentStats = allComplaints.stream()
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));
            
            XYChart.Series<String, Number> departmentSeries = new XYChart.Series<>();
            departmentStats.forEach((dept, count) -> 
                departmentSeries.getData().add(new XYChart.Data<>(dept, count)));
            
            complaintsByDepartmentChart.getData().clear();
            complaintsByDepartmentChart.getData().add(departmentSeries);
            
            // Update status chart
            Map<Complaint.Status, Long> statusStats = allComplaints.stream()
                .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));
            
            statusDistributionChart.setData(
                statusStats.entrySet().stream()
                    .map(entry -> new PieChart.Data(entry.getKey().toString(), entry.getValue()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList))
            );
            
        } catch (SQLException e) {
            showError("Error updating charts: " + e.getMessage());
        }
    }

    private void handleViewComplaint(Complaint complaint) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Complaint Details");
            dialog.setHeaderText("Ticket #" + complaint.getTicketNumber());
            
            VBox content = new VBox(10);
            content.getChildren().addAll(
                new Label("Category: " + complaint.getCategory()),
                new Label("Subject: " + complaint.getSubject()),
                new Label("Description: " + complaint.getDescription()),
                new Label("Priority: " + complaint.getPriority()),
                new Label("Current Status: " + complaint.getStatus())
            );
            
            ComboBox<Complaint.Status> statusUpdate = new ComboBox<>(
                FXCollections.observableArrayList(Complaint.Status.values())
            );
            statusUpdate.setValue(complaint.getStatus());
            
            content.getChildren().add(new Label("Update Status:"));
            content.getChildren().add(statusUpdate);
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK && statusUpdate.getValue() != complaint.getStatus()) {
                    try {
                        complaint.setStatus(statusUpdate.getValue());
                        complaintDAO.updateComplaint(complaint);
                        
                        // Get user email and send notification
                        User complaintUser = userDAO.getUserById(complaint.getUserId());
                        if (complaintUser != null) {
                            EmailSender.sendComplaintStatusUpdate(
                                complaintUser.getEmail(),
                                complaint.getTicketNumber(),
                                complaint.getStatus().toString()
                            );
                        }
                        
                        loadComplaints();
                        updateCharts();
                        
                    } catch (SQLException e) {
                        showError("Error updating complaint: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            showError("Error viewing complaint: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
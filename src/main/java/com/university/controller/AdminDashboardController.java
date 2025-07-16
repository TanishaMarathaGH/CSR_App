package com.university.controller;

import com.university.dao.ComplaintDAO;
import com.university.model.Complaint;
import com.university.model.User;
import com.university.util.EmailSender;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private BarChart<String, Number> complaintsByDepartmentChart;
    @FXML private PieChart statusDistributionChart;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label totalComplaintsLabel;
    @FXML private VBox chartContainer;

    private User currentUser;
    private ComplaintDAO complaintDAO = new ComplaintDAO();

    public void initData(User user) {
        this.currentUser = user;
        initializeUI();
        loadComplaints();
        updateCharts();
    }

    private void initializeUI() {
        // Initialize status filter
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"
        ));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> loadComplaints());

        // Initialize table columns
        TableColumn<Complaint, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getComplaintId())));

        TableColumn<Complaint, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        TableColumn<Complaint, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<Complaint, String>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setOnAction(e -> handleViewComplaint(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        complaintsTable.getColumns().addAll(idCol, categoryCol, statusCol, actionCol);
    }

    private void loadComplaints() {
        try {
            List<Complaint> complaints;
            String selectedStatus = statusFilter.getValue();
            
            if ("All".equals(selectedStatus)) {
                complaints = complaintDAO.getAllComplaints();
            } else {
                complaints = complaintDAO.getComplaintsByStatus(Complaint.Status.valueOf(selectedStatus));
            }
            
            complaintsTable.setItems(FXCollections.observableArrayList(complaints));
            totalComplaintsLabel.setText("Total Complaints: " + complaints.size());
            
        } catch (SQLException e) {
            showError("Error loading complaints: " + e.getMessage());
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
            dialog.setHeaderText("Complaint #" + complaint.getComplaintId());
            
            VBox content = new VBox(10);
            content.getChildren().addAll(
                new Label("Category: " + complaint.getCategory()),
                new Label("Subject: " + complaint.getSubject()),
                new Label("Description: " + complaint.getDescription()),
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
                        
                        // Send email notification
                        EmailSender.sendComplaintStatusUpdate(
                            "student@university.edu", // You would get this from the user object
                            complaint.getComplaintId(),
                            complaint.getStatus().toString()
                        );
                        
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
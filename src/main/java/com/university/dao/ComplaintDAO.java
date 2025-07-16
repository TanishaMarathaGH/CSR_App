package com.university.dao;

import com.university.config.DatabaseConfig;
import com.university.model.Complaint;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {
    private Connection connection;

    public ComplaintDAO() {
        this.connection = DatabaseConfig.getConnection();
    }

    public Complaint createComplaint(Complaint complaint) throws SQLException {
        String query = "INSERT INTO complaints (user_id, category, subject, description, image_path, status, priority) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, complaint.getUserId());
            stmt.setString(2, complaint.getCategory());
            stmt.setString(3, complaint.getSubject());
            stmt.setString(4, complaint.getDescription());
            stmt.setString(5, complaint.getImagePath());
            stmt.setString(6, complaint.getStatus().toString());
            stmt.setString(7, complaint.getPriority().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating complaint failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    complaint.setComplaintId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating complaint failed, no ID obtained.");
                }
            }
        }
        return complaint;
    }

    public void updateComplaint(Complaint complaint) throws SQLException {
        String query = "UPDATE complaints SET category = ?, subject = ?, description = ?, " +
                      "image_path = ?, status = ?, priority = ?, updated_at = CURRENT_TIMESTAMP " +
                      "WHERE complaint_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, complaint.getCategory());
            stmt.setString(2, complaint.getSubject());
            stmt.setString(3, complaint.getDescription());
            stmt.setString(4, complaint.getImagePath());
            stmt.setString(5, complaint.getStatus().toString());
            stmt.setString(6, complaint.getPriority().toString());
            stmt.setInt(7, complaint.getComplaintId());
            
            stmt.executeUpdate();
        }
    }

    public Complaint getComplaintById(int complaintId) throws SQLException {
        String query = "SELECT * FROM complaints WHERE complaint_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, complaintId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractComplaintFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Complaint> getComplaintsByUserId(int userId) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                complaints.add(extractComplaintFromResultSet(rs));
            }
        }
        return complaints;
    }

    public List<Complaint> getAllComplaints() throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String query = "SELECT * FROM complaints ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                complaints.add(extractComplaintFromResultSet(rs));
            }
        }
        return complaints;
    }

    public List<Complaint> getComplaintsByStatus(Complaint.Status status) throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE status = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                complaints.add(extractComplaintFromResultSet(rs));
            }
        }
        return complaints;
    }

    private Complaint extractComplaintFromResultSet(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint(
            rs.getInt("user_id"),
            rs.getString("category"),
            rs.getString("subject"),
            rs.getString("description")
        );
        complaint.setComplaintId(rs.getInt("complaint_id"));
        complaint.setImagePath(rs.getString("image_path"));
        complaint.setStatus(Complaint.Status.valueOf(rs.getString("status")));
        complaint.setPriority(Complaint.Priority.valueOf(rs.getString("priority")));
        complaint.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        complaint.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) {
            complaint.setResolvedAt(resolvedAt.toLocalDateTime());
        }
        
        return complaint;
    }
} 
package com.university.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "csrmlsu@gmail.com"; // Replace with your email
    private static final String PASSWORD = "anzuenhzugpcypry"; // Replace with your app password
    
    private static Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    private static Session getEmailSession() {
        return Session.getInstance(getEmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static void sendComplaintStatusUpdate(String recipientEmail, int complaintId, String status) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Complaint Status Update - #" + complaintId);
            
            String emailBody = String.format(
                "Dear Student,\n\n" +
                "Your complaint (ID: %d) has been updated to status: %s\n\n" +
                "Please log in to the Campus Service Request System for more details.\n\n" +
                "Best regards,\n" +
                "Campus Service Request Team",
                complaintId, status
            );
            
            message.setText(emailBody);
            Transport.send(message);
            
        } catch (MessagingException e) {
            e.printStackTrace();
            // Consider logging this error or handling it appropriately
        }
    }

    public static void sendNewComplaintNotification(String adminEmail, int complaintId, String category) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(adminEmail));
            message.setSubject("New Complaint Received - #" + complaintId);
            
            String emailBody = String.format(
                "Dear Administrator,\n\n" +
                "A new complaint has been submitted:\n" +
                "Complaint ID: %d\n" +
                "Category: %s\n\n" +
                "Please log in to the Campus Service Request System to review and process this complaint.\n\n" +
                "Best regards,\n" +
                "Campus Service Request System",
                complaintId, category
            );
            
            message.setText(emailBody);
            Transport.send(message);
            
        } catch (MessagingException e) {
            e.printStackTrace();
            // Consider logging this error or handling it appropriately
        }
    }
} 
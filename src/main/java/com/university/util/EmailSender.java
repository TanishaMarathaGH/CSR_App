package com.university.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "your-email@gmail.com"; // Replace with your email
    private static final String PASSWORD = "your-app-password"; // Replace with your app password
    
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

    public static void sendComplaintStatusUpdate(String recipientEmail, String ticketNumber, String status) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Complaint Status Update - Ticket #" + ticketNumber);
            
            String emailBody = String.format(
                "Dear User,\n\n" +
                "Your complaint (Ticket #%s) has been updated.\n\n" +
                "New Status: %s\n\n" +
                "Please log in to the Campus Service Request System to view more details.\n\n" +
                "Best regards,\n" +
                "Campus Service Request Team",
                ticketNumber, status
            );
            
            message.setText(emailBody);
            Transport.send(message);
            
        } catch (MessagingException e) {
            e.printStackTrace();
            // Log the error but don't throw it to avoid disrupting the main application flow
        }
    }

    public static void sendNewComplaintNotification(String adminEmail, String ticketNumber, String category, String priority) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(adminEmail));
            message.setSubject("New Complaint Received - Ticket #" + ticketNumber);
            
            String emailBody = String.format(
                "Dear Administrator,\n\n" +
                "A new complaint has been submitted:\n\n" +
                "Ticket Number: %s\n" +
                "Category: %s\n" +
                "Priority: %s\n\n" +
                "Please log in to the Campus Service Request System to review and process this complaint.\n\n" +
                "Best regards,\n" +
                "Campus Service Request System",
                ticketNumber, category, priority
            );
            
            message.setText(emailBody);
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            // Log the error but don't throw it to avoid disrupting the main application flow
        }
    }
} 
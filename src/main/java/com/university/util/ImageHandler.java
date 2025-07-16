package com.university.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImageHandler {
    private static final String UPLOAD_DIR = "uploads/complaints/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    static {
        createUploadDirectory();
    }

    private static void createUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String uploadImage(Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Complaint Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return null;
        }

        // Validate file
        validateFile(selectedFile);

        // Generate unique filename
        String uniqueFileName = generateUniqueFileName(selectedFile.getName());
        Path destinationPath = Paths.get(UPLOAD_DIR, uniqueFileName);

        // Copy file to upload directory
        Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return destinationPath.toString();
    }

    private static void validateFile(File file) throws IOException {
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        // Check file extension
        String extension = getFileExtension(file.getName()).toLowerCase();
        boolean isValidExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isValidExtension = true;
                break;
            }
        }
        if (!isValidExtension) {
            throw new IOException("Invalid file type. Allowed types: JPG, JPEG, PNG, GIF");
        }

        // Validate that it's actually an image file
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Selected file is not a valid image");
        }
    }

    private static String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = getFileExtension(originalFileName);
        return timestamp + "_" + System.nanoTime() + extension;
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    public static void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 
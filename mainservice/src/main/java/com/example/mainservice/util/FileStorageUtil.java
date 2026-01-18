package com.example.mainservice.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageUtil {

    // Use absolute path relative to project directory
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/reports/";

    public static String saveFile(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("Cannot save empty file");
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "file";
        }

        String filename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        // Save file
        file.transferTo(filePath.toFile());

        return filePath.toString();
    }
}
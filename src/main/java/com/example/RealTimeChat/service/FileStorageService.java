package com.example.RealTimeChat.service;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

//    public String storeFile(MultipartFile file) throws IOException {
//        // Create directory if not exists
//        Path uploadPath = Paths.get(uploadDir);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//        Path filePath = uploadPath.resolve(fileName);
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        return fileName;
//    }

    private static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(UPLOAD_DIR); // create folder if missing
    }

    public String storeFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        Path target = UPLOAD_DIR.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "http://localhost:8080/uploads/" + filename;
        System.out.println("File stored at: " + fileUrl);

        return filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
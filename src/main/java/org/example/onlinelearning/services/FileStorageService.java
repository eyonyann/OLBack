package org.example.onlinelearning.services;

import org.example.onlinelearning.exceptions.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, Long courseId) {
        try {
            String fileName = generateFileName(courseId, Objects.requireNonNull(file.getOriginalFilename()));
            Path targetLocation = Paths.get(uploadDir).resolve("courses").resolve(fileName);

            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    private String generateFileName(Long courseId, String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return "course-" + courseId + extension;
    }
}
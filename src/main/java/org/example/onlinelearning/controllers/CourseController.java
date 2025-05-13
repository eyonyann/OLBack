package org.example.onlinelearning.controllers;

import jakarta.validation.Valid;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.InvalidRequestException;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.models.Course;
import org.example.onlinelearning.services.CourseService;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @GetMapping("")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping(value = "/images", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getAllCoursesImagesAsBase64() throws IOException {
        List<CourseDTO> courses = courseService.getAllCourses();
        Map<String, String> imageMap = new HashMap<>();

        for (CourseDTO course : courses) {
            Path imagePath = Paths.get(course.getImagePath());
            if (Files.exists(imagePath)) {
                try {
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    imageMap.put(course.getId().toString(), base64Image);
                } catch (IOException e) {
                    // Логирование ошибки чтения файла
                    System.err.println("Error reading image for course " + course.getId() + ": " + e.getMessage());
                }
            } else {
                System.err.println("Image not found for course: " + course.getId());
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(imageMap);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable("id") Long id) {
        CourseDTO course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getCourseImage(@PathVariable Long id) throws IOException {
        CourseDTO courseDTO = courseService.getCourseById(id);
        Path imagePath = Paths.get(courseDTO.getImagePath());

        Resource resource = new InputStreamResource(
                Files.newInputStream(imagePath)
        );

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseDTO> createCourse(
            @RequestPart("courseData") @Valid CourseDTO courseDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Валидация изображения
            if (image == null || image.isEmpty()) {
                throw new InvalidRequestException("Image is required");
            }

            // Создание курса
            CourseDTO savedCourse = courseService.createCourse(courseDTO, image);

            // Логирование действия
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle(String.format(
                    "Создал курс " + savedCourse.getId() + " " + savedCourse.getTitle()
            ));
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            // Формирование ответа
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedCourse.getId())
                    .toUri();

            return ResponseEntity.created(location).body(savedCourse);

        } catch (InvalidRequestException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Course creation failed", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable("id") Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO existingCourse = courseService.getCourseById(id);
        if (existingCourse == null) {
            return ResponseEntity.notFound().build();
        }
        courseDTO.setId(id); // Ensure the path ID is set to the course
        CourseDTO updatedCourseDTO = courseService.saveCourse(courseDTO);
        return ResponseEntity.ok(updatedCourseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("id") Long id) {
        boolean isDeleted = courseService.deleteCourse(id);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
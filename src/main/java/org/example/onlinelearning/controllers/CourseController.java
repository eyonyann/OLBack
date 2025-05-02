package org.example.onlinelearning.controllers;

import jakarta.validation.Valid;
import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.exceptions.InvalidRequestException;
import org.example.onlinelearning.models.Course;
import org.example.onlinelearning.services.CourseService;
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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping("")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping(value = "/images", produces = "application/zip")
    public ResponseEntity<Resource> getAllCoursesImagesAsZip() throws IOException {
        List<CourseDTO> courses = courseService.getAllCourses();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (CourseDTO course : courses) {
            Path imagePath = Paths.get(course.getImagePath());
            ZipEntry entry = new ZipEntry(course.getId() + "_image.jpg");
            zos.putNextEntry(entry);
            Files.copy(imagePath, zos);
            zos.closeEntry();
        }
        zos.finish();

        byte[] zipBytes = baos.toByteArray();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(zipBytes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=courses_images.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipBytes.length)
                .body(resource);
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
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // Добавить валидацию
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException("Image is required");
        }

        CourseDTO savedCourse = courseService.createCourse(courseDTO, image);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCourse.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedCourse);
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
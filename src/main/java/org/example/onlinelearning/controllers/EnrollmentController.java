package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.EnrollmentDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/enrollments/{id}")
    public ResponseEntity<?> getEnrollmentById(@PathVariable Long id) {
        try {
            EnrollmentDTO enrollmentDTO = enrollmentService.getEnrollmentById(id);
            return ResponseEntity.ok(enrollmentDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Enrollment not found", e.getMessage()));
        }
    }

    @PostMapping("/courses/{course_id}/enrollments")
    public ResponseEntity<?> createEnrollment(
            @PathVariable("course_id") Long courseId,
            @RequestBody EnrollmentDTO enrollmentDTO) {
        try {
            EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(courseId, enrollmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEnrollment);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", e.getMessage()));
        }
    }

    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long id) {
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Enrollment not found", e.getMessage()));
        }
    }
}
package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.dtos.EnrollmentDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.AssignmentService;
import org.example.onlinelearning.services.CourseService;
import org.example.onlinelearning.services.EnrollmentService;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @Autowired
    private CourseService courseService;

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
            @RequestBody EnrollmentDTO enrollmentDTO,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Обрабатываем запись на курс
            int lessonOrder = enrollmentService.processEnrollment(courseId, enrollmentDTO);

            // Получаем информацию о курсе для лога
            CourseDTO course = courseService.getCourseById(courseId);

            // Логируем действие
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle(String.format(
                    "Записался на курс [ID:%d] '%s'",
                    courseId,
                    course.getTitle()
            ));
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.ok(Map.of(
                    "lessonOrder", lessonOrder,
                    "message", "Enrollment successful"
            ));

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            HttpStatus.NOT_FOUND.value(),
                            "Resource not found",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Enrollment failed",
                            "Internal server error"
                    ));
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
package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.ReviewDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.CourseService;
import org.example.onlinelearning.services.LogService;
import org.example.onlinelearning.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/reviews/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            ReviewDTO reviewDTO = reviewService.getReviewById(id);
            return ResponseEntity.ok(reviewDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Review not found", e.getMessage()));
        }
    }

    @GetMapping("/courses/{course_id}/reviews")
    public ResponseEntity<?> getReviewsByCourseId(
            @PathVariable("course_id") Long courseId) {
            List<ReviewDTO> reviews = reviewService.getAllReviewsByCourseId(courseId);
            return ResponseEntity.ok(reviews);
    }

    @PostMapping("/courses/{course_id}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable("course_id") Long courseId,
            @RequestBody ReviewDTO reviewDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Создаем отзыв
            ReviewDTO createdReview = reviewService.createReview(courseId, reviewDTO);

            // Получаем информацию о курсе
            CourseDTO course = courseService.getCourseById(courseId);

            // Логируем действие
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle(String.format(
                    "Оценил курс [ID:%d] '%s' на %d звёзд",
                    courseId,
                    course.getTitle(),
                    reviewDTO.getRating()
            ));
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                            "Resource not found",
                            e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Review creation failed",
                            "Internal server error"));
        }
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO);
            return ResponseEntity.ok(updatedReview);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Review not found", e.getMessage()));
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Review not found", e.getMessage()));
        }
    }
}
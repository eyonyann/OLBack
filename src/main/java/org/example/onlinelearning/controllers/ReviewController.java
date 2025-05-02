package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.ReviewDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

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
            @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO createdReview = reviewService.createReview(courseId, reviewDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", e.getMessage()));
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
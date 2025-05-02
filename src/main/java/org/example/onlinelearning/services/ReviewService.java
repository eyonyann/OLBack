package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.ReviewDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.ReviewMapper;
import org.example.onlinelearning.models.Lesson;
import org.example.onlinelearning.models.Review;
import org.example.onlinelearning.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewMapper reviewMapper,
            CourseService courseService,
            UserService userService
            ) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.courseService = courseService;
        this.userService = userService;
    }

    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
        return reviewMapper.toReviewDTO(review);
    }

    public ReviewDTO createReview(Long courseId, ReviewDTO reviewDTO) {
        Review review = reviewMapper.toReview(reviewDTO);

        review.setCourse(
                courseService.getCourseEntityById(courseId)
        );

        review.setUser(
                userService.getUserEntityById(reviewDTO.getUserId())
        );

        review.setReviewTime(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toReviewDTO(savedReview);
    }

    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));

        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        Review updatedReview = reviewRepository.save(existingReview);
        return reviewMapper.toReviewDTO(updatedReview);
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new NotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    public List<ReviewDTO> getAllReviewsByCourseId(Long courseId) {
        List<Review> reviews = reviewRepository.findAllByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Reviews not found"));
        return reviews.stream()
                .map(reviewMapper::toReviewDTO)
                .toList();
    }
}
package org.example.onlinelearning.repositories;

import org.example.onlinelearning.models.Lesson;
import org.example.onlinelearning.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<List<Review>> findAllByCourseId(Long courseId);
}
package org.example.onlinelearning.repositories;

import org.example.onlinelearning.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByCourseIdAndLessonOrder(Long courseId, Integer lessonOrder);
    Optional<List<Lesson>> findAllByCourseId(Long courseId);
}

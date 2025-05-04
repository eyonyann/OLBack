package org.example.onlinelearning.repositories;

import org.example.onlinelearning.models.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.assignment.lesson.course.id = :courseId ORDER BY s.submissionDate DESC")
    List<Submission> findLatestByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            Pageable pageable
    );

    Optional<Submission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);
}
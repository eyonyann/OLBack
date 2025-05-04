package org.example.onlinelearning.repositories;

import org.example.onlinelearning.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAssignmentId(Long assignmentId);

    Optional<Answer> findByAssignmentIdAndUserId(Long assignmentId, Long userId);
}
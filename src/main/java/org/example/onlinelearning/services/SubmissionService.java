package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.AnswerDTO;
import org.example.onlinelearning.dtos.SubmissionDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.SubmissionMapper;
import org.example.onlinelearning.models.Submission;
import org.example.onlinelearning.repositories.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final AssignmentService assignmentService;
    private final UserService userService;

    @Autowired
    public SubmissionService(
            SubmissionRepository submissionRepository,
            SubmissionMapper submissionMapper,
            AssignmentService assignmentService,
            UserService userService
            ) {
        this.submissionRepository = submissionRepository;
        this.submissionMapper = submissionMapper;
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    public SubmissionDTO getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Submission not found with id: " + id));
        return submissionMapper.toSubmissionDTO(submission);
    }

    public SubmissionDTO createSubmission(Long assignmentId, SubmissionDTO submissionDTO) {
        Submission submission = submissionMapper.toSubmission(submissionDTO);

        submission.setAssignment(
                assignmentService.getAssignmentEntityById(assignmentId)
        );

        submission.setUser(
                userService.getUserEntityById(submissionDTO.getUserId())
        );

        submission.setSubmissionDate(LocalDateTime.now());

        Submission savedSubmission = submissionRepository.save(submission);
        return submissionMapper.toSubmissionDTO(savedSubmission);
    }

    public SubmissionDTO updateSubmission(Long id, SubmissionDTO submissionDTO) {
        Submission existingSubmission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Submission not found with id: " + id));

        existingSubmission.setContent(submissionDTO.getContent());
        existingSubmission.setGrade(submissionDTO.getGrade());

        Submission updatedSubmission = submissionRepository.save(existingSubmission);
        return submissionMapper.toSubmissionDTO(updatedSubmission);
    }

    public void deleteSubmission(Long id) {
        if (!submissionRepository.existsById(id)) {
            throw new NotFoundException("Submission not found with id: " + id);
        }
        submissionRepository.deleteById(id);
    }

    public Submission findLatestSubmission(Long userId, Long courseId) {
        Pageable pageable = PageRequest.of(0, 1);
        List<Submission> submissions = submissionRepository.findLatestByUserIdAndCourseId(
                userId,
                courseId,
                pageable
        );
        return submissions.isEmpty() ? null : submissions.get(0);
    }

    public SubmissionDTO getUserSubmissionForAssignment(Long assignmentId, Long userId) {
        return submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .map(submissionMapper::toSubmissionDTO)
                .orElseThrow(() -> new NotFoundException("Submission not found for assignment and user"));
    }
}
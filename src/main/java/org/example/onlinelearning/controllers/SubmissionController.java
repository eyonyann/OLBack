package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.AnswerDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.SubmissionDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/submissions/{id}")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long id) {
        try {
            SubmissionDTO submissionDTO = submissionService.getSubmissionById(id);
            return ResponseEntity.ok(submissionDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Submission not found", e.getMessage()));
        }
    }

    @GetMapping("/assignments/{assignmentId}/my-submission")
    public ResponseEntity<?> getUserSubmissionForAssignment(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtTokenProvider.getUserId(authHeader.substring(7));
            SubmissionDTO submission = submissionService.getUserSubmissionForAssignment(assignmentId, userId);
            return ResponseEntity.ok(submission);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "submission not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication error", e.getMessage()));
        }
    }

    @PostMapping("/assignments/{assignment_id}/submissions")
    public ResponseEntity<?> createSubmission(
            @PathVariable("assignment_id") Long assignmentId,
            @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO createdSubmission = submissionService.createSubmission(assignmentId, submissionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubmission);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", e.getMessage()));
        }
    }

    @PutMapping("/submissions/{id}")
    public ResponseEntity<?> updateSubmission(
            @PathVariable Long id,
            @RequestBody SubmissionDTO submissionDTO) {
        try {
            SubmissionDTO updatedSubmission = submissionService.updateSubmission(id, submissionDTO);
            return ResponseEntity.ok(updatedSubmission);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Submission not found", e.getMessage()));
        }
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        try {
            submissionService.deleteSubmission(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Submission not found", e.getMessage()));
        }
    }
}
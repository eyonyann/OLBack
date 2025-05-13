package org.example.onlinelearning.controllers;

import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.AnswerDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.AnswerService;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @GetMapping("/assignments/{assignmentId}/answers")
    public ResponseEntity<?> getAllAnswersByAssignmentId(@PathVariable Long assignmentId) {
        try {
            List<AnswerDTO> answers = answerService.getAllAnswersByAssignmentId(assignmentId);
            return ResponseEntity.ok(answers);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        }
    }

    @GetMapping("/assignments/{assignmentId}/my-answer")
    public ResponseEntity<?> getUserAnswerForAssignment(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtTokenProvider.getUserId(authHeader.substring(7));
            AnswerDTO answer = answerService.getUserAnswerForAssignment(assignmentId, userId);
            return ResponseEntity.ok(answer);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Answer not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication error", e.getMessage()));
        }
    }

    @PostMapping("/assignments/{assignmentId}/answers")
    public ResponseEntity<?> createAnswer(
            @PathVariable Long assignmentId,
            @RequestBody AnswerDTO answerDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Создаем ответ
            AnswerDTO createdAnswer = answerService.createAnswer(assignmentId, answerDTO);

            // Логируем действие
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle("Создал ответ " + createdAnswer.getContent() + " на вопрос задания с id " + createdAnswer.getAssignmentId());
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/answers/{id}")
    public ResponseEntity<?> updateAnswer(
            @PathVariable Long id,
            @RequestBody AnswerDTO answerDTO) {
        try {
            AnswerDTO updatedAnswer = answerService.updateAnswer(id, answerDTO);
            return ResponseEntity.ok(updatedAnswer);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Answer not found", e.getMessage()));
        }
    }

    @DeleteMapping("/answers/{id}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long id) {
        try {
            answerService.deleteAnswer(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Answer not found", e.getMessage()));
        }
    }

    @GetMapping("/answers/{id}")
    public ResponseEntity<?> getAnswerById(@PathVariable Long id) {
        try {
            AnswerDTO answerDTO = answerService.getAnswerById(id);
            return ResponseEntity.ok(answerDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Answer not found", e.getMessage()));
        }
    }
}
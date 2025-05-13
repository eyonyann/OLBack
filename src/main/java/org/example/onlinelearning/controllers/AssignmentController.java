package org.example.onlinelearning.controllers;

import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.AssignmentDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.AssignmentService;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @GetMapping("/assignments/{id}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        try {
            AssignmentDTO assignmentDTO = assignmentService.getAssignmentById(id);
            return ResponseEntity.ok(assignmentDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        }
    }

    @PostMapping("/lessons/{lesson_id}/assignments")
    public ResponseEntity<?> createAssignment(
            @PathVariable("lesson_id") Long lessonId,
            @RequestBody AssignmentDTO assignmentDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Создаем задание
            AssignmentDTO createdAssignment = assignmentService.createAssignment(lessonId, assignmentDTO);

            // Логируем действие
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle("Создал задание '" + createdAssignment.getTitle()
                    + "' для урока ID: " + lessonId);
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (NotFoundException e) {;
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Lesson not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
            return ResponseEntity.ok(updatedAssignment);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        }
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        }
    }

    @GetMapping("/lessons/{lessonId}/assignment")
    public ResponseEntity<?> getAssignmentForLesson(@PathVariable Long lessonId) {
        try {
            AssignmentDTO assignmentDTO = assignmentService.getAssignmentByLessonId(lessonId);
            return ResponseEntity.ok(assignmentDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Assignment not found", e.getMessage()));
        }
    }
}
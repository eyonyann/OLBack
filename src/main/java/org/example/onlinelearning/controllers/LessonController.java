package org.example.onlinelearning.controllers;


import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.LessonDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.models.Course;
import org.example.onlinelearning.models.Lesson;
import org.example.onlinelearning.services.CourseService;
import org.example.onlinelearning.services.LessonService;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LessonController {
    @Autowired
    private LessonService lessonService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @GetMapping("/lessons/{id}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable("id") Long id) {
        LessonDTO lessonDTO = lessonService.getLessonById(id);
        if (lessonDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lessonDTO);
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonOrder}")
    public ResponseEntity<LessonDTO> getLessonByCourseIdAndLessonOrder(
            @PathVariable("courseId") Long courseId,
            @PathVariable("lessonOrder") Integer lessonOrder) {
        LessonDTO lessonDTO = lessonService.getLessonByCourseAndOrder(courseId, lessonOrder);
        if (lessonDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lessonDTO);
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourseId(
            @PathVariable("courseId") Long courseId) {
        List<LessonDTO> lessons = lessonService.getAllLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<LessonDTO> createLesson(
            @RequestBody LessonDTO lessonDTO,
            @PathVariable("courseId") Long courseId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Получаем ID пользователя из токена
            String token = authHeader.substring(7);
            Long userId = jwtTokenProvider.getUserId(token);

            // Создаем урок
            LessonDTO createdLessonDTO = lessonService.createLesson(courseId, lessonDTO);

            // Логируем действие
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle(String.format(
                    "Создал урок [ID:%d] '%s' в курсе [ID:%d]",
                    createdLessonDTO.getId(),
                    createdLessonDTO.getTitle(),
                    courseId
            ));
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdLessonDTO);

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lesson creation failed", e);
        }
    }

    @PutMapping("lessons/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(
            @RequestBody LessonDTO lessonDTO,
            @PathVariable("lessonId") Long lessonId) {
        LessonDTO updatedLessonDTO = lessonService.updateLesson(lessonDTO);
        return ResponseEntity.ok(updatedLessonDTO);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable("lessonId") Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
}

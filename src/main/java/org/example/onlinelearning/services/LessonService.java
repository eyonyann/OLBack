package org.example.onlinelearning.services;

import org.example.onlinelearning.dtos.LessonDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.LessonMapper;
import org.example.onlinelearning.models.Course;
import org.example.onlinelearning.models.Lesson;
import org.example.onlinelearning.repositories.CourseRepository;
import org.example.onlinelearning.repositories.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    @Autowired
    public LessonService(
            LessonRepository lessonRepository,
            CourseRepository courseRepository,
            LessonMapper lessonMapper
    ) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.lessonMapper = lessonMapper;
    }

    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return lessonMapper.toLessonDTO(lesson);
    }

    public LessonDTO getLessonByCourseAndOrder(Long courseId, Integer lessonOrder) {
        Lesson lesson = lessonRepository.findByCourseIdAndLessonOrder(courseId, lessonOrder)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return lessonMapper.toLessonDTO(lesson);
    }

    public List<LessonDTO> getAllLessonsByCourseId(Long courseId) {
        List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Lessons not found"));
        return lessons.stream()
                .map(lessonMapper::toLessonDTO)
                .toList();
    }

    public void deleteLesson(Long lessonId) {
        Optional<Lesson> lesson = lessonRepository.findById(lessonId);
        lesson.ifPresent(lessonRepository::delete);
    }

    @Transactional
    public LessonDTO createLesson(Long courseId, LessonDTO lessonDTO) {
        // Преобразование DTO -> Entity с маппером
        Lesson lesson = lessonMapper.toLesson(lessonDTO);

        // Ручное связывание с курсом
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        lesson.setCourse(course);

        // Сохранение и возврат DTO
        Lesson savedLesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonDTO(savedLesson);
    }

    public LessonDTO updateLesson(LessonDTO lessonDTO) {
        Lesson existingLesson = lessonRepository.findById(lessonDTO.getId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        // Обновляем поля
        existingLesson.setTitle(lessonDTO.getTitle());
        existingLesson.setContent(lessonDTO.getContent());
        existingLesson.setVideoURL(lessonDTO.getVideoURL());
        existingLesson.setLessonOrder(lessonDTO.getLessonOrder());

        // Если нужно обновить курс
        if(lessonDTO.getCourseId() != null) {
            Course course = courseRepository.findById(lessonDTO.getCourseId())
                    .orElseThrow(() -> new NotFoundException("Course not found"));
            existingLesson.setCourse(course);
        }

        Lesson updatedLesson = lessonRepository.save(existingLesson);
        return lessonMapper.toLessonDTO(updatedLesson);
    }
}
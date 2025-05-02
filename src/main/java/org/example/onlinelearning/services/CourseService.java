package org.example.onlinelearning.services;

import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.CourseMapper;
import org.example.onlinelearning.models.Course;
import org.example.onlinelearning.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private CourseMapper courseMapper;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toCourseDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long id) {
        return courseRepository.findById(id)
                .map(courseMapper::toCourseDTO)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    public CourseDTO saveCourse(CourseDTO courseDTO) {
        Course course = courseMapper.toCourse(courseDTO);
        return courseMapper.toCourseDTO(courseRepository.save(course));
    }

    public Boolean deleteCourse(Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            courseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO, MultipartFile image) {
        // Маппинг DTO -> Entity
        Course newCourse = courseMapper.toCourse(courseDTO);
        newCourse.setRating(String.valueOf(-1));

        // Первое сохранение для получения ID
        Course savedCourse = courseRepository.save(newCourse);

        // Сохранение изображения
        String imagePath = fileStorageService.storeFile(image, savedCourse.getId());
        savedCourse.setImagePath(imagePath);

        // Обновление и возврат DTO
        Course updatedCourse = courseRepository.save(savedCourse);
        return courseMapper.toCourseDTO(updatedCourse);
    }
}

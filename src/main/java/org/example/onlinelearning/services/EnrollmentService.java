package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.EnrollmentDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.EnrollmentMapper;
import org.example.onlinelearning.models.Enrollment;
import org.example.onlinelearning.repositories.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            EnrollmentMapper enrollmentMapper,
            UserService userService,
            CourseService courseService
            ) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.userService = userService;
        this.courseService = courseService;
    }

    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found with id: " + id));
        return enrollmentMapper.toEnrollmentDTO(enrollment);
    }

    public EnrollmentDTO createEnrollment(Long courseId, EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentMapper.toEnrollment(enrollmentDTO);

        enrollment.setUser(
                userService.getUserEntityById(enrollmentDTO.getUserId())
        );

        enrollment.setCourse(
                courseService.getCourseEntityById(courseId)
        );

        enrollment.setEnrollmentTime(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toEnrollmentDTO(savedEnrollment);
    }

    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new NotFoundException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }
}
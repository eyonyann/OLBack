package org.example.onlinelearning.services;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.dtos.AssignmentDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.AssignmentMapper;
import org.example.onlinelearning.mappers.LessonMapper;
import org.example.onlinelearning.models.Assignment;
import org.example.onlinelearning.repositories.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final LessonService lessonService;
    private final LessonMapper lessonMapper;

    @Autowired
    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssignmentMapper assignmentMapper,
            LessonService lessonService,
            LessonMapper lessonMapper
    ) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
        this.lessonService = lessonService;
        this.lessonMapper = lessonMapper;

    }

    public AssignmentDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found with id: " + id));
        return assignmentMapper.toAssignmentDTO(assignment);
    }

    public AssignmentDTO createAssignment(Long lessonId, AssignmentDTO assignmentDTO) {
        Assignment assignment = assignmentMapper.toAssignment(assignmentDTO);
        assignment.setLesson(lessonMapper.toLesson(lessonService.getLessonById(lessonId)));
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return assignmentMapper.toAssignmentDTO(savedAssignment);
    }

    public AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        Assignment existingAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found with id: " + id));

        existingAssignment.setTitle(assignmentDTO.getTitle());
        existingAssignment.setDescription(assignmentDTO.getDescription());
        existingAssignment.setDueDate(assignmentDTO.getDueDate());

        Assignment updatedAssignment = assignmentRepository.save(existingAssignment);
        return assignmentMapper.toAssignmentDTO(updatedAssignment);
    }

    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new NotFoundException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }


    public AssignmentDTO getAssignmentByLessonId(Long lessonId) {
        // Получаем Optional<Assignment> из репозитория
        Assignment assignment = assignmentRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Assignment not found for lesson id: " + lessonId));

        return assignmentMapper.toAssignmentDTO(assignment);
    }

    public Assignment getAssignmentEntityById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
    }
}
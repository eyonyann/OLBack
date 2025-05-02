package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.AssignmentDTO;
import org.example.onlinelearning.models.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentDTO toAssignmentDTO(Assignment assignment);

    @Mapping(target = "lesson.id", source = "lessonId")
    Assignment toAssignment(AssignmentDTO assignmentDTO);
}
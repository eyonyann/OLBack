package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.LessonDTO;
import org.example.onlinelearning.models.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Service;

@Service
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessonMapper {
    @Mapping(target = "courseId", source = "course.id")
    LessonDTO toLessonDTO(Lesson lesson);

    @Mapping(target = "course.id", source = "courseId")
    Lesson toLesson(LessonDTO lessonDTO);
}

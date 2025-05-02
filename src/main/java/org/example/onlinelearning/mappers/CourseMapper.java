package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.models.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "authorId", source = "author.id")
    CourseDTO toCourseDTO(Course course);

    @Mapping(target = "author.id", source = "authorId")
    Course toCourse(CourseDTO courseDTO);
}
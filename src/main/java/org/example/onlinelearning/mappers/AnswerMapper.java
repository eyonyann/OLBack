package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.AnswerDTO;
import org.example.onlinelearning.models.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "userId", source = "user.id")
    AnswerDTO toAnswerDTO(Answer answer);

    @Mapping(target = "assignment.id", source = "assignmentId")
    @Mapping(target = "user.id", source = "userId")
    Answer toAnswer(AnswerDTO answerDTO);
}
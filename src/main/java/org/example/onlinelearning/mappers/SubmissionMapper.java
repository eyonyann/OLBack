package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.SubmissionDTO;
import org.example.onlinelearning.models.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Service;

@Service
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubmissionMapper {
    SubmissionDTO toSubmissionDTO(Submission submission);
    Submission toSubmission(SubmissionDTO submissionDTO);
}

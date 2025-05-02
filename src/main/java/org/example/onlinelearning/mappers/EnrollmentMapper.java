package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.EnrollmentDTO;
import org.example.onlinelearning.models.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Service;

@Service
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnrollmentMapper {
    EnrollmentDTO toEnrollmentDTO(Enrollment enrollment);
    Enrollment toEnrollment(EnrollmentDTO enrollmentDTO);
}

package org.example.onlinelearning.mappers;

import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.models.Log;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LogMapper {
    @Mapping(target = "userId", source = "user.id")
    LogDTO toLogDTO(Log log);

    @Mapping(target = "user.id", source = "userId")
    Log toLog(LogDTO logDTO);
}
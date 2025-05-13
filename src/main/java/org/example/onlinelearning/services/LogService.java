package org.example.onlinelearning.services;

import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.mappers.LogMapper;
import org.example.onlinelearning.models.Log;
import org.example.onlinelearning.repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LogService {
    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogMapper logMapper;

    public List<LogDTO> getAllLogs() {
        return logRepository.findAll()
                .stream()
                .map(logMapper::toLogDTO)
                .collect(Collectors.toList());
    }

    public LogDTO getLogById(Long id) {
        return logRepository.findById(id)
                .map(logMapper::toLogDTO)
                .orElseThrow(() -> new NotFoundException("Log not found"));
    }

    public LogDTO saveLog(LogDTO logDTO) {
        Log log = logMapper.toLog(logDTO);
        return logMapper.toLogDTO(logRepository.save(log));
    }

    public Boolean deleteLog(Long id) {
        Optional<Log> log = logRepository.findById(id);
        if (log.isPresent()) {
            logRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
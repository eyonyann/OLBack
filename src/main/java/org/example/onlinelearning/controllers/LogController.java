package org.example.onlinelearning.controllers;

import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    @Autowired
    private LogService logService;

    @GetMapping
    public ResponseEntity<List<LogDTO>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogDTO> getLogById(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getLogById(id));
    }

    @PostMapping
    public ResponseEntity<LogDTO> createLog(@RequestBody LogDTO logDTO) {
        LogDTO savedLog = logService.saveLog(logDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedLog.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedLog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LogDTO> updateLog(
            @PathVariable Long id,
            @RequestBody LogDTO logDTO
    ) {
        logDTO.setId(id);
        return ResponseEntity.ok(logService.saveLog(logDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        return logService.deleteLog(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
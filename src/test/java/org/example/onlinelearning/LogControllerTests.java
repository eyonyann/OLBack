package org.example.onlinelearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.onlinelearning.controllers.LogController;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.LogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllLogs_ReturnsListOfLogs() throws Exception {
        LogDTO logDTO = createTestLogDTO();
        List<LogDTO> logs = Collections.singletonList(logDTO);

        Mockito.when(logService.getAllLogs()).thenReturn(logs);

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Log"));
    }

    @Test
    void getLogById_ExistingId_ReturnsLog() throws Exception {
        LogDTO logDTO = createTestLogDTO();
        Mockito.when(logService.getLogById(1L)).thenReturn(logDTO);

        mockMvc.perform(get("/api/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Log"));
    }

    @Test
    void getLogById_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(logService.getLogById(999L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/logs/999"))
                .andExpect(status().isNotFound());
    }



    @Test
    void deleteLog_ExistingId_ReturnsNoContent() throws Exception {
        Mockito.when(logService.deleteLog(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/logs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLog_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(logService.deleteLog(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/logs/999"))
                .andExpect(status().isNotFound());
    }

    private LogDTO createTestLogDTO() {
        LogDTO logDTO = new LogDTO();
        logDTO.setId(1L);
        logDTO.setUserId(123L);
        logDTO.setTitle("Test Log");
        logDTO.setLogTime(LocalDateTime.now());
        return logDTO;
    }
}
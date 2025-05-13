package org.example.onlinelearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.controllers.CourseController;
import org.example.onlinelearning.dtos.CourseDTO;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.exceptions.InvalidRequestException;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.services.CourseService;
import org.example.onlinelearning.services.LogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LogService logService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CourseDTO createTestCourseDTO() {
        CourseDTO dto = new CourseDTO();
        dto.setId(1L);
        dto.setTitle("Test Course");
        dto.setDescription("Test Description");
        dto.setImagePath("/test/path.jpg");
        return dto;
    }

    @Test
    void getAllCourses_ReturnsCoursesList() throws Exception {
        CourseDTO course = createTestCourseDTO();
        List<CourseDTO> courses = Collections.singletonList(course);

        Mockito.when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Course"));
    }


    @Test
    void getCourseById_ExistingId_ReturnsCourse() throws Exception {
        CourseDTO course = createTestCourseDTO();

        Mockito.when(courseService.getCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Course"));
    }

    @Test
    void getCourseById_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(courseService.getCourseById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void createCourse_ValidRequest_ReturnsCreatedCourse() throws Exception {
        CourseDTO newCourse = createTestCourseDTO();
        newCourse.setId(null);
        String token = "validToken";

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        MockMultipartFile courseData = new MockMultipartFile(
                "courseData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(newCourse)
        );

        Mockito.when(jwtTokenProvider.getUserId(token)).thenReturn(1L);
        Mockito.when(courseService.createCourse(Mockito.any(), Mockito.any()))
                .thenReturn(createTestCourseDTO());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/courses")
                        .file(courseData)
                        .file(image)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(logService).saveLog(Mockito.any(LogDTO.class));
    }


    @Test
    void updateCourse_ValidRequest_ReturnsUpdatedCourse() throws Exception {
        CourseDTO updatedCourse = createTestCourseDTO();
        updatedCourse.setTitle("Updated Title");

        Mockito.when(courseService.getCourseById(1L)).thenReturn(updatedCourse);
        Mockito.when(courseService.saveCourse(Mockito.any())).thenReturn(updatedCourse);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteCourse_ExistingId_ReturnsNoContent() throws Exception {
        Mockito.when(courseService.deleteCourse(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCourse_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(courseService.deleteCourse(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/courses/999"))
                .andExpect(status().isNotFound());
    }
}
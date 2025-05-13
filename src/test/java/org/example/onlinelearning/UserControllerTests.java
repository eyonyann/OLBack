package org.example.onlinelearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.controllers.UserController;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.dtos.UpdateUserDTO;
import org.example.onlinelearning.dtos.UserDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.exceptions.SecurityException;
import org.example.onlinelearning.services.LogService;
import org.example.onlinelearning.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LogService logService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void getUsers_AdminRole_ReturnsAllUsers() throws Exception {
        List<UserDTO> users = List.of(new UserDTO(), new UserDTO());
        Mockito.when(userService.findAllUsers()).thenReturn(users);
        Mockito.when(jwtTokenProvider.getRole("validToken")).thenReturn("ADMIN");

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getUsers_MissingToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void updateUser_ValidRequest_UpdatesUserAndLogs() throws Exception {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(1L);
        updatedUser.setUsername("user1");

        Mockito.when(jwtTokenProvider.getUserId("validToken")).thenReturn(1L);
        Mockito.when(userService.updateUser(updateDTO)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));

        Mockito.verify(logService).saveLog(Mockito.any(LogDTO.class));
    }

    @Test
    void updateUser_WrongUserId_ReturnsForbidden() throws Exception {
        Mockito.when(jwtTokenProvider.getUserId("validToken")).thenReturn(2L);

        mockMvc.perform(put("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ValidRequest_DeletesUserAndLogs() throws Exception {
        Mockito.when(jwtTokenProvider.getUserId("validToken")).thenReturn(1L);
        Mockito.doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isNoContent());

        Mockito.verify(logService).saveLog(Mockito.any(LogDTO.class));
    }

    @Test
    void deleteUser_UserNotFound_ReturnsNotFound() throws Exception {
        Mockito.when(jwtTokenProvider.getUserId("validToken")).thenReturn(1L);
        Mockito.doThrow(new NotFoundException("User not found")).when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/users/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isNotFound());
    }
}
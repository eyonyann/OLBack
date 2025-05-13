package org.example.onlinelearning.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.LogDTO;
import org.example.onlinelearning.dtos.UpdateUserDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.UserDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.exceptions.SecurityException;
import org.example.onlinelearning.services.LogService;
import org.example.onlinelearning.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LogService logService;

    @GetMapping("/id/{userId}")
    public ResponseEntity<?> getUserDTO(@PathVariable("userId") Long userId) {
        try {
            UserDTO userDTO = userService.findUserById(userId);

            String token = jwtTokenProvider.generateToken(
                    userDTO.getUsername(),
                    userDTO.getRole(),
                    userDTO.getId()
            );

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.getJwtConfig().getPrefix() + token).body(userDTO);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication failed", e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Проверка наличия заголовка Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new SecurityException("Invalid or missing Authorization header");
            }

            String token = authHeader.substring(7);
            String currentUserRole = jwtTokenProvider.getRole(token);

            // Проверка прав доступа
            if (!currentUserRole.equalsIgnoreCase("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                                "Access denied", "Insufficient privileges"));
            }

            return ResponseEntity.ok().body(userService.findAllUsers());

        } catch (SecurityException | IllegalArgumentException e) {
            // Обработка невалидного токена или проблем аутентификации
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "Authentication failed", e.getMessage()));

        } catch (ExpiredJwtException e) {
            // Обработка просроченного токена
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "Token expired", "JWT token has expired"));

        } catch (Exception e) {
            // Общая обработка непредвиденных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Server error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserDTO(@PathVariable("username") String username) {
        try {
            UserDTO userDTO = userService.findUserByUsername(username);

            String token = jwtTokenProvider.generateToken(
                    userDTO.getUsername(),
                    userDTO.getRole(),
                    userDTO.getId()
            );

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.getJwtConfig().getPrefix() + token).body(userDTO);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication failed", e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserDTO updateDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Проверка прав доступа
            String token = authHeader.substring(7);
            Long currentUserId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);

            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(403)
                        .body(new ErrorResponse(403, "Forbidden", "You can only update your own profile"));
            }

            // Обновление пользователя
            UserDTO updated = userService.updateUser(updateDTO);

            // Логирование
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle("Обновил информацию о себе: " + username);
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.ok(updated);

        } catch (NotFoundException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(404, "User not found", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse(403, "Forbidden", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Проверка прав доступа
            String token = authHeader.substring(7);
            Long currentUserId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);

            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(403)
                        .body(new ErrorResponse(403, "Forbidden", "You can only delete your own account"));
            }

            // Удаление пользователя
            userService.deleteUserById(userId);

            // Логирование
            LogDTO logDTO = new LogDTO();
            logDTO.setUserId(userId);
            logDTO.setTitle("Удалил аккаунт: " + username);
            logDTO.setLogTime(LocalDateTime.now());
            logService.saveLog(logDTO);

            return ResponseEntity.noContent().build();

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    private String generateNewToken(UserDTO user) {
        return jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole(),
                user.getId()
        );
    }

    private ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Operation forbidden",
                        e.getMessage()
                ));
    }
}

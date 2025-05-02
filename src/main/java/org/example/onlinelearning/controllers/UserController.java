package org.example.onlinelearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.dtos.UpdateUserDTO;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.UserDTO;
import org.example.onlinelearning.exceptions.NotFoundException;
import org.example.onlinelearning.exceptions.SecurityException;
import org.example.onlinelearning.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
            @RequestBody @Valid UpdateUserDTO updateDTO
    ) {
        try {
            UserDTO updated = userService.updateUser(updateDTO);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(404, "User not found", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse(403, "Forbidden", e.getMessage()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUserById(userId);
            return ResponseEntity.noContent().build();

        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found", e.getMessage()));
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

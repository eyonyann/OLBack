package org.example.onlinelearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.example.onlinelearning.exceptions.AuthResponse;
import org.example.onlinelearning.exceptions.ErrorResponse;
import org.example.onlinelearning.dtos.UserDTO;
import org.example.onlinelearning.exceptions.SecurityException;
import org.example.onlinelearning.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
        try {
            UserDTO authenticatedUser = userService.authenticateUser(
                    userDTO.getUsername(),
                    userDTO.getPassword()
            );

            String token = jwtTokenProvider.generateToken(
                    authenticatedUser.getUsername(),
                    authenticatedUser.getRole(),
                    authenticatedUser.getId()
            );


            String roleUpper = authenticatedUser.getRole().toUpperCase();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.getJwtConfig().getPrefix() + token)
                    .body(new AuthResponse(
                            token,
                            authenticatedUser.getId(),
                            roleUpper
                    ));

        } catch (SecurityException e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            UserDTO newUser = userService.registerUser(userDTO);

            String token = jwtTokenProvider.generateToken(
                    newUser.getUsername(),
                    newUser.getRole(),
                    newUser.getId()
            );

            String roleUpper = newUser.getRole().toUpperCase();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.getJwtConfig().getPrefix() + token)
                    .body(new AuthResponse(
                            token,
                            newUser.getId(),
                            roleUpper
                    ));

        } catch (SecurityException e) {
            return buildErrorResponse(HttpStatus.CONFLICT, "Registration failed", e);
        }
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            Exception e
    ) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        status.value(),
                        message,
                        e.getMessage()
                ));
    }
}

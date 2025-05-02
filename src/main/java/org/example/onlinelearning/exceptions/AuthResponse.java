package org.example.onlinelearning.exceptions;

public record AuthResponse(String token, Long userId, String role) {}
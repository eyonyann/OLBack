package org.example.onlinelearning.exceptions;

public record ErrorResponse(
        int status,
        String error,
        String details
) {}
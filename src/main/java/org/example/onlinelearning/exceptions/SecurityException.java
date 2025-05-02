// SecurityException.java
package org.example.onlinelearning.exceptions;

public class SecurityException extends RuntimeException {
    // Добавляем оба конструктора
    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
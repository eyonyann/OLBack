package org.example.onlinelearning.exceptions;

import java.io.IOException;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, IOException ex) {
        super(message);
    }
}

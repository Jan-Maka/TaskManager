package com.example.taskmanager.component;


/**
 * Handles exceptions to file size
 */
public class FileSizeExceededError extends RuntimeException {

    public FileSizeExceededError(String message) {
        super(message);
    }
}

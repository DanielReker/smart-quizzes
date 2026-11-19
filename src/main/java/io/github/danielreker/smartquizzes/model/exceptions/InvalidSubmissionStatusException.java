package io.github.danielreker.smartquizzes.model.exceptions;

public class InvalidSubmissionStatusException extends RuntimeException {
    public InvalidSubmissionStatusException(String message) {
        super(message);
    }
}

package io.github.Piotr7421.courses.exception;

public class DatabaseConstraintException extends RuntimeException {

    public DatabaseConstraintException(String message) {
        super(message);
    }
}

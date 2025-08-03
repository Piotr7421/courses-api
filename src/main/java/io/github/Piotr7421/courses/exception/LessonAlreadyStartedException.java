package io.github.Piotr7421.courses.exception;

public class LessonAlreadyStartedException extends RuntimeException {

    public LessonAlreadyStartedException(String message) {
        super(message);
    }
}
